/*******************************************************************************
 * Copyright (c) 2024 Obeo.
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
package org.eclipse.sirius.web.application.object.dto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.sirius.components.core.api.IPayload;

/**
 * The payload for the "getRelationshipsByRelatedElement" REST API on success.
 *
 * @author arichard
 */
public record GetRelationshipsByRelatedElementRestSuccessPayload(UUID id, List<Object> relationships) implements IPayload {
    public GetRelationshipsByRelatedElementRestSuccessPayload {
        Objects.requireNonNull(id);
        Objects.requireNonNull(relationships);
    }
}