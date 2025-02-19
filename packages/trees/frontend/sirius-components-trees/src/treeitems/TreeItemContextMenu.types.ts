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
import { Selection } from '@eclipse-sirius/sirius-components-core';
import { GQLTreeItem } from '../views/TreeView.types';
import {
  TreeItemContextMenuComponentProps,
  TreeItemContextMenuContributionProps,
} from './TreeItemContextMenuContribution.types';

export type TreeItemContextMenuContextValue = React.ReactElement<TreeItemContextMenuContributionProps>[];

export interface TreeItemContextMenuProps {
  menuAnchor: Element;
  item: GQLTreeItem;
  editingContextId: string;
  treeId: string;
  readOnly: boolean;
  treeItemMenuContributionComponents: ((props: TreeItemContextMenuComponentProps) => JSX.Element)[];
  depth: number;
  onExpand: (id: string, depth: number) => void;
  onExpandAll: (treeItem: GQLTreeItem) => void;
  selection: Selection;
  setSelection: (selection: Selection) => void;
  enterEditingMode: () => void;
  onClose: () => void;
}

export interface GQLDeleteTreeItemData {
  deleteTreeItem: GQLDeleteTreeItemPayload;
}
export interface GQLDeleteTreeItemPayload {
  __typename: string;
}

export interface GQLSuccessPayload extends GQLDeleteTreeItemPayload {
  id: string;
}

export interface GQLErrorPayload extends GQLDeleteTreeItemPayload {
  message: string;
}

export interface GQLDeleteTreeItemVariables {
  input: GQLDeleteTreeItemInput;
}

export interface GQLDeleteTreeItemInput {
  id: string;
  editingContextId: string;
  representationId: string;
  treeItemId: string;
}

export interface TreeItemContextMenuState {
  message: string | null;
}
