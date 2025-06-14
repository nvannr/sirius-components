/*******************************************************************************
 * Copyright (c) 2022, 2025 Obeo.
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

import { WorkbenchViewContribution } from './Workbench.types';

export interface PanelsProps {
  editingContextId: string;
  readOnly: boolean;
  leftContributions: WorkbenchViewContribution[];
  rightContributions: WorkbenchViewContribution[];
  mainArea: JSX.Element;
  leftPanelInitialSize: number;
  rightPanelInitialSize: number;
}
