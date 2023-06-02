/*******************************************************************************
 * Copyright (c) 2023 Obeo and others.
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

import { NodeData } from './DiagramRenderer.types';
import { Label } from './Label.types';

export interface ListNodeData extends NodeData {
  label: Label;
  style: React.CSSProperties;
  listItems: ListItemData[];
}

export interface ListItemData {
  id: string;
  label: Label;
  style: React.CSSProperties;
}
