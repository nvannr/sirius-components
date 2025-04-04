/*******************************************************************************
 * Copyright (c) 2025 Obeo.
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
package org.eclipse.sirius.components.diagrams.components;

import java.util.Set;

import org.eclipse.sirius.components.diagrams.INodeStyle;

/**
 * Wrapper for a node style and the list of its customized style properties.
 *
 * @param style
 * @param customizedStyleProperties
 * @author nvannier
 */
public record NodeAppearance(INodeStyle style, Set<String> customizedStyleProperties) {

    public static final String NODE_APPEARANCE = "NODE_APPEARANCE";

    public static final String PREVIOUS_NODE_APPEARANCE = "PREVIOUS_NODE_APPEARANCE";

}
