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

import { Box, Node, Rect, boxToRect, rectToBox } from 'reactflow';
import { Diagram, NodeData } from '../DiagramRenderer.types';
import { DiagramNodeType } from '../node/NodeTypes.types';
import { RectangularNodeData } from '../node/RectangularNode.types';
import { ILayoutEngine, INodeLayoutHandler } from './LayoutEngine.types';

const gap = 20;
const rectangularNodePadding = 8;
const borderLeftAndRight = 2;
const defaultWidth = 150;
const defaultHeight = 70;

export class RectangleNodeLayoutHandler implements INodeLayoutHandler<RectangularNodeData> {
  public canHandle(node: Node<NodeData, DiagramNodeType>) {
    return node.type === 'rectangularNode';
  }

  public handle(
    layoutEngine: ILayoutEngine,
    previousDiagram: Diagram | null,
    node: Node<RectangularNodeData, 'rectangularNode'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    directChildren: Node<NodeData, DiagramNodeType>[]
  ) {
    if (directChildren.length > 0) {
      this.handleParentNode(layoutEngine, previousDiagram, node, visibleNodes, directChildren);
    } else {
      this.handleLeafNode(previousDiagram, node, visibleNodes);
    }
  }

  private handleParentNode(
    layoutEngine: ILayoutEngine,
    previousDiagram: Diagram | null,
    node: Node<RectangularNodeData, 'rectangularNode'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[],
    directChildren: Node<NodeData, DiagramNodeType>[]
  ) {
    layoutEngine.layoutNodes(previousDiagram, visibleNodes, directChildren);

    const nodeIndex = this.findNodeIndex(visibleNodes, node.id);
    const labelElement = document.getElementById(`${node.id}-label-${nodeIndex}`);

    // Update children position to be under the label and at the right padding.
    directChildren.forEach((child, index) => {
      child.position = {
        x: rectangularNodePadding,
        y: rectangularNodePadding + (labelElement?.getBoundingClientRect().height ?? 0) + rectangularNodePadding,
      };
      const previousSibling = directChildren[index - 1];
      if (previousSibling) {
        child.position = { ...child.position, x: previousSibling.position.x + (previousSibling.width ?? 0) + gap };
      }
    });

    // Update node to layout size
    // WARN: We suppose label are always on top of children (that wrong)
    const childrenFootprint = this.getChildrenFootprint(directChildren);
    const childrenAwareNodeWidth = childrenFootprint.x + childrenFootprint.width + rectangularNodePadding;
    const labelOnlyWidth =
      rectangularNodePadding + (labelElement?.getBoundingClientRect().width ?? 0) + rectangularNodePadding;
    const nodeWidth = Math.max(childrenAwareNodeWidth, labelOnlyWidth);
    node.width = this.getNodeOrMinWidth(nodeWidth + borderLeftAndRight);
    node.height = this.getNodeOrMinHeight(
      rectangularNodePadding +
        (labelElement?.getBoundingClientRect().height ?? 0) +
        rectangularNodePadding +
        childrenFootprint.height +
        rectangularNodePadding
    );
  }

  private handleLeafNode(
    previousDiagram: Diagram | null,
    node: Node<RectangularNodeData, 'rectangularNode'>,
    visibleNodes: Node<NodeData, DiagramNodeType>[]
  ) {
    const nodeIndex = this.findNodeIndex(visibleNodes, node.id);
    const labelElement = document.getElementById(`${node.id}-label-${nodeIndex}`);

    const labelWidth =
      rectangularNodePadding +
      (labelElement?.getBoundingClientRect().width ?? 0) +
      rectangularNodePadding +
      borderLeftAndRight;
    const labelHeight =
      rectangularNodePadding + (labelElement?.getBoundingClientRect().height ?? 0) + rectangularNodePadding;
    const nodeWidth = this.getNodeOrMinWidth(labelWidth);
    const nodeHeight = this.getNodeOrMinHeight(labelHeight);
    node.width = nodeWidth;
    node.height = nodeHeight;

    const previousNode = (previousDiagram?.nodes ?? []).find((previousNode) => previousNode.id === node.id);
    if (previousNode && previousNode.width && previousNode.height) {
      node.width = previousNode.width;
      node.height = previousNode.height;
    }
  }

  private getChildrenFootprint(children: Node<NodeData>[]): Rect {
    const footPrint: Box = children.reduce(
      (currentFootPrint, node) => {
        const { x, y } = node.position;
        const nodeBox = rectToBox({
          x,
          y,
          width: node.width ?? 0,
          height: node.height ?? 0,
        });

        return this.getBoundsOfBoxes(currentFootPrint, nodeBox);
      },
      { x: Infinity, y: Infinity, x2: -Infinity, y2: -Infinity }
    );
    return boxToRect(footPrint);
  }

  private getBoundsOfBoxes(box1: Box, box2: Box): Box {
    return {
      x: Math.min(box1.x, box2.x),
      y: Math.min(box1.y, box2.y),
      x2: Math.max(box1.x2, box2.x2),
      y2: Math.max(box1.y2, box2.y2),
    };
  }

  private findNodeIndex(nodes: Node<NodeData>[], nodeId: string): number {
    return nodes.findIndex((node) => node.id === nodeId);
  }

  private getNodeOrMinWidth(nodeWidth: number | undefined): number {
    return Math.max(nodeWidth ?? -Infinity, defaultWidth);
  }

  private getNodeOrMinHeight(nodeHeight: number | undefined): number {
    return Math.max(nodeHeight ?? -Infinity, defaultHeight);
  }
}
