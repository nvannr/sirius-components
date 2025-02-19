/*******************************************************************************
 * Copyright (c) 2022, 2023 Obeo.
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
package org.eclipse.sirius.components.forms;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.eclipse.sirius.components.annotations.Immutable;
import org.eclipse.sirius.components.forms.validation.Diagnostic;

/**
 * The flexbox container widget.
 *
 * @author arichard
 */
@Immutable
public final class FlexboxContainer extends AbstractWidget {

    private String flexDirection;

    private String flexWrap;

    private int flexGrow;

    private List<AbstractWidget> children;

    private ContainerBorderStyle borderStyle;

    private FlexboxContainer() {
        // Prevent instantiation
    }

    public String getFlexDirection() {
        return this.flexDirection;
    }

    public String getFlexWrap() {
        return this.flexWrap;
    }

    public int getFlexGrow() {
        return this.flexGrow;
    }

    public List<AbstractWidget> getChildren() {
        return this.children;
    }

    public ContainerBorderStyle getBorderStyle() {
        return this.borderStyle;
    }

    public static Builder newFlexboxContainer(String id) {
        return new Builder(id);
    }

    @Override
    public String toString() {
        String pattern = "{0} '{'id: {1}, label: {2}', flexDirection: {3}', flexWrap: {4}', flexGrow: {5}'}'";
        return MessageFormat.format(pattern, this.getClass().getSimpleName(), this.getId(), this.label, this.flexDirection, this.flexWrap, this.flexGrow);
    }

    /**
     * The builder used to create the flexbox container.
     *
     * @author arichard
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public static final class Builder {
        private String id;

        private String label;

        private String iconURL;

        private String flexDirection;

        private String flexWrap;

        private int flexGrow;

        private List<AbstractWidget> children;

        private List<Diagnostic> diagnostics;

        private Supplier<String> helpTextProvider;

        private boolean readOnly;

        private ContainerBorderStyle borderStyle;

        private Builder(String id) {
            this.id = Objects.requireNonNull(id);
        }

        public Builder label(String label) {
            this.label = Objects.requireNonNull(label);
            return this;
        }

        public Builder iconURL(String iconURL) {
            this.iconURL = Objects.requireNonNull(iconURL);
            return this;
        }

        public Builder flexDirection(String flexDirection) {
            this.flexDirection = Objects.requireNonNull(flexDirection);
            return this;
        }

        public Builder flexWrap(String flexWrap) {
            this.flexWrap = Objects.requireNonNull(flexWrap);
            return this;
        }

        public Builder flexGrow(int flexGrow) {
            this.flexGrow = Objects.requireNonNull(flexGrow);
            return this;
        }

        public Builder children(List<AbstractWidget> children) {
            this.children = Objects.requireNonNull(children);
            return this;
        }

        public Builder diagnostics(List<Diagnostic> diagnostics) {
            this.diagnostics = Objects.requireNonNull(diagnostics);
            return this;
        }

        public Builder helpTextProvider(Supplier<String> helpTextProvider) {
            this.helpTextProvider = Objects.requireNonNull(helpTextProvider);
            return this;
        }

        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public Builder borderStyle(ContainerBorderStyle borderStyle) {
            this.borderStyle = Objects.requireNonNull(borderStyle);
            return this;
        }

        public FlexboxContainer build() {
            FlexboxContainer flexboxContainer = new FlexboxContainer();
            flexboxContainer.id = Objects.requireNonNull(this.id);
            flexboxContainer.label = Objects.requireNonNull(this.label);
            flexboxContainer.iconURL = this.iconURL; // Optional on purpose
            flexboxContainer.flexDirection = Objects.requireNonNull(this.flexDirection);
            flexboxContainer.flexWrap = Objects.requireNonNull(this.flexWrap);
            flexboxContainer.flexGrow = Objects.requireNonNull(this.flexGrow);
            flexboxContainer.children = Objects.requireNonNull(this.children);
            flexboxContainer.diagnostics = Objects.requireNonNull(this.diagnostics);
            flexboxContainer.helpTextProvider = this.helpTextProvider; // Optional on purpose
            flexboxContainer.readOnly = this.readOnly;
            flexboxContainer.borderStyle = this.borderStyle; // Optional on purpose
            return flexboxContainer;
        }
    }
}
