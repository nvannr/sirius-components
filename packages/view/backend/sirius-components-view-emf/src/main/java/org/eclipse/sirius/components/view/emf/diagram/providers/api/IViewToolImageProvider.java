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
package org.eclipse.sirius.components.view.emf.diagram.providers.api;

import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;

/**
 * Used to compute the image of a tool.
 *
 * @author sbegaudeau
 */
public interface IViewToolImageProvider {
    String getImage(DiagramElementDescription diagramElementDescription);

    /**
     * Implementation which does nothing, used for mocks in unit tests.
     *
     * @author sbegaudeau
     */
    class NoOp implements IViewToolImageProvider {

        @Override
        public String getImage(DiagramElementDescription diagramElementDescription) {
            return "";
        }

    }
}
