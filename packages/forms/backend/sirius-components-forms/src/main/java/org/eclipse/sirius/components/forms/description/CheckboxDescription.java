/*******************************************************************************
 * Copyright (c) 2019, 2023 Obeo.
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
package org.eclipse.sirius.components.forms.description;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.sirius.components.annotations.Immutable;
import org.eclipse.sirius.components.forms.CheckboxStyle;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.VariableManager;

/**
 * The description of the checkbox widget.
 *
 * @author sbegaudeau
 */
@Immutable
public final class CheckboxDescription extends AbstractWidgetDescription {

    private Function<VariableManager, String> idProvider;

    private Function<VariableManager, String> labelProvider;

    private Function<VariableManager, String> iconURLProvider;

    private Function<VariableManager, Boolean> valueProvider;

    private BiFunction<VariableManager, Boolean, IStatus> newValueHandler;

    private Function<VariableManager, CheckboxStyle> styleProvider;

    private CheckboxDescription() {
        // Prevent instantiation
    }

    public Function<VariableManager, String> getIdProvider() {
        return this.idProvider;
    }

    public Function<VariableManager, String> getLabelProvider() {
        return this.labelProvider;
    }

    public Function<VariableManager, String> getIconURLProvider() {
        return this.iconURLProvider;
    }

    public Function<VariableManager, Boolean> getValueProvider() {
        return this.valueProvider;
    }

    public BiFunction<VariableManager, Boolean, IStatus> getNewValueHandler() {
        return this.newValueHandler;
    }

    public Function<VariableManager, CheckboxStyle> getStyleProvider() {
        return this.styleProvider;
    }

    public static Builder newCheckboxDescription(String id) {
        return new Builder(id);
    }

    @Override
    public String toString() {
        String pattern = "{0} '{'id: {1}'}'";
        return MessageFormat.format(pattern, this.getClass().getSimpleName(), this.getId());
    }

    /**
     * Builder used to create the checkbox description.
     *
     * @author sbegaudeau
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public static final class Builder {

        private String id;

        private Function<VariableManager, String> idProvider;

        private Function<VariableManager, String> targetObjectIdProvider;

        private Function<VariableManager, String> labelProvider;

        private Function<VariableManager, String> iconURLProvider = variableManager -> null;

        private Function<VariableManager, Boolean> isReadOnlyProvider = variableManager -> false;

        private Function<VariableManager, Boolean> valueProvider;

        private BiFunction<VariableManager, Boolean, IStatus> newValueHandler;

        private Function<VariableManager, List<?>> diagnosticsProvider;

        private Function<Object, String> kindProvider;

        private Function<Object, String> messageProvider;

        private Function<VariableManager, String> helpTextProvider;

        private Function<VariableManager, CheckboxStyle> styleProvider = vm -> null;

        private Builder(String id) {
            this.id = Objects.requireNonNull(id);
        }

        public Builder idProvider(Function<VariableManager, String> idProvider) {
            this.idProvider = Objects.requireNonNull(idProvider);
            return this;
        }

        public Builder targetObjectIdProvider(Function<VariableManager, String> targetObjectIdProvider) {
            this.targetObjectIdProvider = Objects.requireNonNull(targetObjectIdProvider);
            return this;
        }

        public Builder labelProvider(Function<VariableManager, String> labelProvider) {
            this.labelProvider = Objects.requireNonNull(labelProvider);
            return this;
        }

        public Builder iconURLProvider(Function<VariableManager, String> iconURLProvider) {
            this.iconURLProvider = Objects.requireNonNull(iconURLProvider);
            return this;
        }

        public Builder isReadOnlyProvider(Function<VariableManager, Boolean> isReadOnlyProvider) {
            this.isReadOnlyProvider = Objects.requireNonNull(isReadOnlyProvider);
            return this;
        }

        public Builder valueProvider(Function<VariableManager, Boolean> valueProvider) {
            this.valueProvider = Objects.requireNonNull(valueProvider);
            return this;
        }

        public Builder newValueHandler(BiFunction<VariableManager, Boolean, IStatus> newValueHandler) {
            this.newValueHandler = Objects.requireNonNull(newValueHandler);
            return this;
        }

        public Builder diagnosticsProvider(Function<VariableManager, List<?>> diagnosticsProvider) {
            this.diagnosticsProvider = Objects.requireNonNull(diagnosticsProvider);
            return this;
        }

        public Builder kindProvider(Function<Object, String> kindProvider) {
            this.kindProvider = Objects.requireNonNull(kindProvider);
            return this;
        }

        public Builder messageProvider(Function<Object, String> messageProvider) {
            this.messageProvider = Objects.requireNonNull(messageProvider);
            return this;
        }

        public Builder styleProvider(Function<VariableManager, CheckboxStyle> styleProvider) {
            this.styleProvider = Objects.requireNonNull(styleProvider);
            return this;
        }

        public Builder helpTextProvider(Function<VariableManager, String> helpTextProvider) {
            this.helpTextProvider = Objects.requireNonNull(helpTextProvider);
            return this;
        }

        public CheckboxDescription build() {
            CheckboxDescription checkboxDescription = new CheckboxDescription();
            checkboxDescription.id = Objects.requireNonNull(this.id);
            checkboxDescription.targetObjectIdProvider = Objects.requireNonNull(this.targetObjectIdProvider);
            checkboxDescription.idProvider = Objects.requireNonNull(this.idProvider);
            checkboxDescription.labelProvider = Objects.requireNonNull(this.labelProvider);
            checkboxDescription.iconURLProvider = Objects.requireNonNull(this.iconURLProvider);
            checkboxDescription.isReadOnlyProvider = Objects.requireNonNull(this.isReadOnlyProvider);
            checkboxDescription.valueProvider = Objects.requireNonNull(this.valueProvider);
            checkboxDescription.newValueHandler = Objects.requireNonNull(this.newValueHandler);
            checkboxDescription.diagnosticsProvider = Objects.requireNonNull(this.diagnosticsProvider);
            checkboxDescription.kindProvider = Objects.requireNonNull(this.kindProvider);
            checkboxDescription.messageProvider = Objects.requireNonNull(this.messageProvider);
            checkboxDescription.styleProvider = Objects.requireNonNull(this.styleProvider);
            checkboxDescription.helpTextProvider = this.helpTextProvider; // Optional on purpose
            return checkboxDescription;
        }
    }
}
