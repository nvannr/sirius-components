/*******************************************************************************
 * Copyright (c) 2024, 2025 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.web.application.controllers.forms;

import static org.eclipse.sirius.components.forms.tests.assertions.FormAssertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.jayway.jsonpath.JsonPath;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.sirius.components.collaborative.forms.dto.EditTextfieldInput;
import org.eclipse.sirius.components.collaborative.forms.dto.FormRefreshedEventPayload;
import org.eclipse.sirius.components.core.api.SuccessPayload;
import org.eclipse.sirius.components.forms.Form;
import org.eclipse.sirius.components.forms.Textfield;
import org.eclipse.sirius.components.forms.tests.graphql.EditTextfieldMutationRunner;
import org.eclipse.sirius.components.forms.tests.navigation.FormNavigator;
import org.eclipse.sirius.web.AbstractIntegrationTests;
import org.eclipse.sirius.web.application.views.details.dto.DetailsEventInput;
import org.eclipse.sirius.web.data.StudioIdentifiers;
import org.eclipse.sirius.web.tests.data.GivenSiriusWebServer;
import org.eclipse.sirius.web.tests.graphql.DetailsEventSubscriptionRunner;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.sirius.web.tests.services.representation.RepresentationIdBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import graphql.execution.DataFetcherResult;
import reactor.test.StepVerifier;

/**
 * Integration tests of the custom nodes details view.
 *
 * @author frouene
 */

@Transactional
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomNodeDetailsViewControllerTests extends AbstractIntegrationTests {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private DetailsEventSubscriptionRunner detailsEventSubscriptionRunner;

    @Autowired
    private EditTextfieldMutationRunner editTextfieldMutationRunner;

    @Autowired
    private RepresentationIdBuilder representationIdBuilder;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @GivenSiriusWebServer
    @DisplayName("Given an EllipseNodeStyleDescription, when we subscribe to its properties events, then the form is sent")
    public void givenEllipseNodeStyleDescriptionWhenWeSubscribeToItsPropertiesEventsThenTheFormIsSent() {
        var detailRepresentationId = representationIdBuilder.buildDetailsRepresentationId(List.of(StudioIdentifiers.ELLIPSE_NODE_STYLE_DESCRIPTION_OBJECT.toString()));
        var input = new DetailsEventInput(UUID.randomUUID(), StudioIdentifiers.SAMPLE_STUDIO_EDITING_CONTEXT_ID.toString(), detailRepresentationId);
        var flux = this.detailsEventSubscriptionRunner.run(input);

        Predicate<Form> formPredicate = form -> {
            var groupNavigator = new FormNavigator(form).page("EllipseNodeStyleDescription").group("Core Properties");

            var borderSizeTextField = groupNavigator.findWidget("Border Size", Textfield.class);
            assertThat(borderSizeTextField).hasValue("1");

            return true;
        };

        Predicate<Object> formContentMatcher = object -> Optional.of(object)
                .filter(DataFetcherResult.class::isInstance)
                .map(DataFetcherResult.class::cast)
                .map(DataFetcherResult::getData)
                .filter(FormRefreshedEventPayload.class::isInstance)
                .map(FormRefreshedEventPayload.class::cast)
                .map(FormRefreshedEventPayload::form)
                .filter(formPredicate)
                .isPresent();

        StepVerifier.create(flux)
                .expectNextMatches(formContentMatcher)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @GivenSiriusWebServer
    @DisplayName("Given an EllipseNodeStyleDescription, when border size is edited, then its value is updated")
    public void givenEllipseNodeStyleDescriptionWhenBorderSizeIsEditedThenItsValueIsUpdated() {
        var detailRepresentationId = representationIdBuilder.buildDetailsRepresentationId(List.of(StudioIdentifiers.ELLIPSE_NODE_STYLE_DESCRIPTION_OBJECT.toString()));
        var input = new DetailsEventInput(UUID.randomUUID(), StudioIdentifiers.SAMPLE_STUDIO_EDITING_CONTEXT_ID.toString(), detailRepresentationId);
        var flux = this.detailsEventSubscriptionRunner.run(input)
                .filter(DataFetcherResult.class::isInstance)
                .map(DataFetcherResult.class::cast)
                .map(DataFetcherResult::getData)
                .filter(FormRefreshedEventPayload.class::isInstance)
                .map(FormRefreshedEventPayload.class::cast);

        var formId = new AtomicReference<String>();
        var textfieldId = new AtomicReference<String>();

        Consumer<FormRefreshedEventPayload> initialFormContentConsumer = payload -> Optional.of(payload)
                .map(FormRefreshedEventPayload::form)
                .ifPresentOrElse(form -> {
                    formId.set(form.getId());

                    var groupNavigator = new FormNavigator(form).page("EllipseNodeStyleDescription").group("Core Properties");
                    var textfield = groupNavigator.findWidget("Border Size", Textfield.class);

                    textfieldId.set(textfield.getId());
                }, () -> fail("Missing form"));

        Runnable editTextfield = () -> {
            var editTextfieldInput = new EditTextfieldInput(UUID.randomUUID(), StudioIdentifiers.SAMPLE_STUDIO_EDITING_CONTEXT_ID.toString(), formId.get(), textfieldId.get(), "3");
            var result = this.editTextfieldMutationRunner.run(editTextfieldInput);

            String typename = JsonPath.read(result, "$.data.editTextfield.__typename");
            assertThat(typename).isEqualTo(SuccessPayload.class.getSimpleName());
        };

        Consumer<FormRefreshedEventPayload> updatedFormContentConsumer = payload -> Optional.of(payload)
                .map(FormRefreshedEventPayload::form)
                .ifPresentOrElse(form -> {
                    var groupNavigator = new FormNavigator(form).page("EllipseNodeStyleDescription").group("Core Properties");
                    var textfield = groupNavigator.findWidget("Border Size", Textfield.class);

                    assertThat(textfield).hasValue("3");
                }, () -> fail("Missing form"));

        StepVerifier.create(flux)
                .consumeNextWith(initialFormContentConsumer)
                .then(editTextfield)
                .consumeNextWith(updatedFormContentConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

}

