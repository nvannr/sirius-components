/*******************************************************************************
 * Copyright (c) 2023 Obeo.
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

import { Node } from 'reactflow';
import { Diagram, NodeData } from '../DiagramRenderer.types';
import { DiagramNodeType } from '../node/NodeTypes.types';
import { ImageNodeLayoutHandler } from './ImageNodeLayoutHandler';
import { ILayoutEngine, INodeLayoutHandler } from './LayoutEngine.types';
import { ListNodeLayoutHandler } from './ListNodeLayoutHandler';
import { RectangleNodeLayoutHandler } from './RectangleNodeLayoutHandler';

export class LayoutEngine implements ILayoutEngine {
  nodeLayoutHandlers: INodeLayoutHandler<NodeData>[] = [
    new RectangleNodeLayoutHandler(),
    new ListNodeLayoutHandler(),
    new ImageNodeLayoutHandler(),
  ];

  public layoutNodes(
    previousDiagram: Diagram | null,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    nodesToLayout: Node<NodeData, DiagramNodeType>[]
  ) {
    nodesToLayout.forEach((node) => {
      const nodeLayoutHandler: INodeLayoutHandler<NodeData> | undefined = this.nodeLayoutHandlers.find((handler) =>
        handler.canHandle(node)
      );
      if (nodeLayoutHandler) {
        const directChildren = visibleNodes.filter((visibleNode) => visibleNode.parentNode === node.id);
        nodeLayoutHandler.handle(this, previousDiagram, node, visibleNodes, directChildren);

        node.style = {
          ...node.style,
          width: `${node.width}px`,
          height: `${node.height}px`,
        };
      }
    });
  }
}
