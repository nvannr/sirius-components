/*******************************************************************************
 * Copyright (c) 2021, 2023 Obeo.
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
import org.eclipse.sirius.components.forms.MultiSelectStyle;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.VariableManager;

/**
 * The description of the multi-select widget.
 *
 * @author pcdavid
 * @author arichard
 */
@Immutable
public final class MultiSelectDescription extends AbstractWidgetDescription {

    private Function<VariableManager, String> idProvider;

    private Function<VariableManager, String> labelProvider;

    private Function<VariableManager, String> iconURLProvider;

    private Function<VariableManager, List<?>> optionsProvider;

    private Function<VariableManager, String> optionIdProvider;

    private Function<VariableManager, String> optionLabelProvider;

    private Function<VariableManager, String> optionIconURLProvider;

    private Function<VariableManager, List<String>> valuesProvider;

    private BiFunction<VariableManager, List<String>, IStatus> newValuesHandler;

    private Function<VariableManager, MultiSelectStyle> styleProvider;

    private MultiSelectDescription() {
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

    public Function<VariableManager, List<?>> getOptionsProvider() {
        return this.optionsProvider;
    }

    public Function<VariableManager, String> getOptionIdProvider() {
        return this.optionIdProvider;
    }

    public Function<VariableManager, String> getOptionLabelProvider() {
        return this.optionLabelProvider;
    }

    public Function<VariableManager, String> getOptionIconURLProvider() {
        return this.optionIconURLProvider;
    }

    public Function<VariableManager, List<String>> getValuesProvider() {
        return this.valuesProvider;
    }

    public BiFunction<VariableManager, List<String>, IStatus> getNewValuesHandler() {
        return this.newValuesHandler;
    }

    public Function<VariableManager, MultiSelectStyle> getStyleProvider() {
        return this.styleProvider;
    }

    public static Builder newMultiSelectDescription(String id) {
        return new Builder(id);
    }

    @Override
    public String toString() {
        String pattern = "{0} '{'id: {1}'}'";
        return MessageFormat.format(pattern, this.getClass().getSimpleName(), this.getId());
    }

    /**
     * Builder used to create the select description.
     *
     * @author pcdavid
     * @author arichard
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public static final class Builder {

        private String id;

        private Function<VariableManager, String> idProvider;

        private Function<VariableManager, String> targetObjectIdProvider;

        private Function<VariableManager, String> labelProvider;

        private Function<VariableManager, String> iconURLProvider = variableManager -> null;

        private Function<VariableManager, Boolean> isReadOnlyProvider = variableManager -> false;

        private Function<VariableManager, List<?>> optionsProvider;

        private Function<VariableManager, String> optionIdProvider;

        private Function<VariableManager, String> optionLabelProvider;

        private Function<VariableManager, String> optionIconURLProvider = variableManager -> null;

        private Function<VariableManager, List<String>> valuesProvider;

        private BiFunction<VariableManager, List<String>, IStatus> newValuesHandler;

        private Function<VariableManager, List<?>> diagnosticsProvider;

        private Function<Object, String> kindProvider;

        private Function<Object, String> messageProvider;

        private Function<VariableManager, String> helpTextProvider;

        private Function<VariableManager, MultiSelectStyle> styleProvider = vm -> null;

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

        public Builder optionsProvider(Function<VariableManager, List<?>> optionsProvider) {
            this.optionsProvider = Objects.requireNonNull(optionsProvider);
            return this;
        }

        public Builder optionIdProvider(Function<VariableManager, String> optionIdProvider) {
            this.optionIdProvider = Objects.requireNonNull(optionIdProvider);
            return this;
        }

        public Builder optionLabelProvider(Function<VariableManager, String> optionLabelProvider) {
            this.optionLabelProvider = Objects.requireNonNull(optionLabelProvider);
            return this;
        }

        public Builder optionIconURLProvider(Function<VariableManager, String> optionIconURLProvider) {
            this.optionIconURLProvider = Objects.requireNonNull(optionIconURLProvider);
            return this;
        }

        public Builder valuesProvider(Function<VariableManager, List<String>> valuesProvider) {
            this.valuesProvider = Objects.requireNonNull(valuesProvider);
            return this;
        }

        public Builder newValuesHandler(BiFunction<VariableManager, List<String>, IStatus> newValuesHandler) {
            this.newValuesHandler = Objects.requireNonNull(newValuesHandler);
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

        public Builder styleProvider(Function<VariableManager, MultiSelectStyle> styleProvider) {
            this.styleProvider = Objects.requireNonNull(styleProvider);
            return this;
        }

        public Builder helpTextProvider(Function<VariableManager, String> helpTextProvider) {
            this.helpTextProvider = Objects.requireNonNull(helpTextProvider);
            return this;
        }

        public MultiSelectDescription build() {
            MultiSelectDescription multiSelectDescription = new MultiSelectDescription();
            multiSelectDescription.id = Objects.requireNonNull(this.id);
            multiSelectDescription.targetObjectIdProvider = Objects.requireNonNull(this.targetObjectIdProvider);
            multiSelectDescription.idProvider = Objects.requireNonNull(this.idProvider);
            multiSelectDescription.labelProvider = Objects.requireNonNull(this.labelProvider);
            multiSelectDescription.iconURLProvider = Objects.requireNonNull(this.iconURLProvider);
            multiSelectDescription.isReadOnlyProvider = Objects.requireNonNull(this.isReadOnlyProvider);
            multiSelectDescription.optionsProvider = Objects.requireNonNull(this.optionsProvider);
            multiSelectDescription.optionIdProvider = Objects.requireNonNull(this.optionIdProvider);
            multiSelectDescription.optionLabelProvider = Objects.requireNonNull(this.optionLabelProvider);
            multiSelectDescription.optionIconURLProvider = Objects.requireNonNull(this.optionIconURLProvider);
            multiSelectDescription.valuesProvider = Objects.requireNonNull(this.valuesProvider);
            multiSelectDescription.newValuesHandler = Objects.requireNonNull(this.newValuesHandler);
            multiSelectDescription.diagnosticsProvider = Objects.requireNonNull(this.diagnosticsProvider);
            multiSelectDescription.kindProvider = Objects.requireNonNull(this.kindProvider);
            multiSelectDescription.messageProvider = Objects.requireNonNull(this.messageProvider);
            multiSelectDescription.styleProvider = Objects.requireNonNull(this.styleProvider);
            multiSelectDescription.helpTextProvider = this.helpTextProvider; // Optional on purpose
            return multiSelectDescription;
        }

    }
}
