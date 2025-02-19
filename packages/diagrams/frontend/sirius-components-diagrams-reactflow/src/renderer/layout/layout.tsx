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

import { ApolloClient, InMemoryCache } from '@apollo/client/core';
import { ApolloProvider } from '@apollo/client/react';
import { MessageOptions, ServerContext, ToastContext, theme } from '@eclipse-sirius/sirius-components-core';
import { ThemeProvider } from '@material-ui/core/styles';
import ELK, { ElkExtendedEdge, ElkLabel, ElkNode, LayoutOptions } from 'elkjs/lib/elk.bundled.js';
import { Fragment, createElement } from 'react';
import ReactDOM from 'react-dom';
import { Edge, Node, ReactFlowProvider } from 'reactflow';
import { Diagram } from '../DiagramRenderer.types';
import { Label } from '../Label';
import { DiagramDirectEditContextProvider } from '../direct-edit/DiagramDirectEditContext';
import { ListNode } from '../node/ListNode';
import { ListNodeData } from '../node/ListNode.types';
import { RectangularNodeData } from '../node/RectangularNode.types';
import { LayoutEngine } from './LayoutEngine';

const emptyNodeProps = {
  selected: false,
  isConnectable: true,
  dragging: false,
  xPos: 0,
  yPos: 0,
  zIndex: -1,
};

const emptyListNodeProps = {
  ...emptyNodeProps,
  type: 'listNode',
};

const elk = new ELK();

export const prepareLayoutArea = (diagram: Diagram, renderCallback: () => void): HTMLDivElement => {
  const hiddenContainer: HTMLDivElement = document.createElement('div');
  hiddenContainer.id = 'hidden-container';
  hiddenContainer.style.display = 'inline-block';
  hiddenContainer.style.position = 'absolute';
  hiddenContainer.style.visibility = 'hidden';
  hiddenContainer.style.zIndex = '-1';
  document.body.appendChild(hiddenContainer);
  const elements: JSX.Element[] = [];
  const visibleNodes = diagram.nodes.filter((node) => !node.hidden);

  // Render all label first
  const labelElements: JSX.Element[] = [];
  visibleNodes.forEach((node, index) => {
    if (hiddenContainer && node.data.label) {
      const children: JSX.Element[] = [
        createElement(Label, {
          diagramElementId: node.id,
          label: node.data.label,
          faded: false,
          transform: '',
          key: node.data.label.id,
        }),
      ];
      const element: JSX.Element = createElement('div', {
        id: `${node.id}-label-${index}`,
        key: `${node.id}-label-${index}`,
        children,
      });
      labelElements.push(element);
    }
  });

  // The container used to render label is a flex container authorizing wrapping.
  // It wrap direct children, and thus, prevent label to wrap (because their container will have always enough place to render on the line, unless the label is larger than the screen).
  const labelContainerElement: JSX.Element = createElement('div', {
    id: 'hidden-label-container',
    key: 'hidden-label-container',
    style: {
      display: 'flex',
      flexWrap: 'wrap',
      alignItems: 'flex-start',
    },
    children: labelElements,
  });
  elements.push(labelContainerElement);

  // then, render list node with list item only
  const listNodeElements: JSX.Element[] = [];
  visibleNodes.forEach((node, index) => {
    if (hiddenContainer && node.type === 'listNode') {
      const data = node.data as ListNodeData;
      const listNode = createElement(ListNode, {
        ...emptyListNodeProps,
        id: node.id,
        data,
        key: `${node.id}-${index}`,
      });

      const element: JSX.Element = createElement('div', {
        id: `${node.id}-${index}`,
        key: node.id,
        children: listNode,
      });
      listNodeElements.push(element);
    }
  });
  const nodeListContainerElement: JSX.Element = createElement('div', {
    id: 'hidden-nodeList-container',
    key: 'hidden-nodeList-container',
    style: {
      display: 'flex',
      flexWrap: 'wrap',
      alignItems: 'flex-start',
    },
    children: listNodeElements,
  });
  elements.push(nodeListContainerElement);

  const hiddenContainerContentElements: JSX.Element = createElement(Fragment, { children: elements });

  const element = (
    <ReactFlowProvider>
      <ApolloProvider client={new ApolloClient({ cache: new InMemoryCache(), uri: '' })}>
        <ThemeProvider theme={theme}>
          <ServerContext.Provider value={{ httpOrigin: '' }}>
            <ToastContext.Provider value={{ enqueueSnackbar: (_body: string, _options?: MessageOptions) => {} }}>
              <DiagramDirectEditContextProvider>{hiddenContainerContentElements}</DiagramDirectEditContextProvider>
            </ToastContext.Provider>
          </ServerContext.Provider>
        </ThemeProvider>
      </ApolloProvider>
    </ReactFlowProvider>
  );

  ReactDOM.render(element, hiddenContainer, renderCallback);
  return hiddenContainer;
};

export const cleanLayoutArea = (container: HTMLDivElement) => {
  if (container?.parentNode) {
    container.parentNode.removeChild(container);
  }
};

const gap = 20;

export const layout = (previousDiagram: Diagram | null, diagram: Diagram): Diagram => {
  layoutDiagram(previousDiagram, diagram);
  return diagram;
};

const layoutDiagram = (previousDiagram: Diagram | null, diagram: Diagram) => {
  const allVisibleNodes = diagram.nodes.filter((node) => !node.hidden);
  const nodesToLayout = allVisibleNodes.filter((node) => !node.parentNode);

  new LayoutEngine().layoutNodes(previousDiagram, allVisibleNodes, nodesToLayout);

  // Update position of root nodes
  nodesToLayout.forEach((node, index) => {
    const previousNode = (previousDiagram?.nodes ?? []).find((previousNode) => previousNode.id === node.id);

    if (previousNode) {
      node.position = previousNode.position;
    } else {
      node.position = { x: 0, y: 0 };
      const previousSibling = nodesToLayout[index - 1];
      if (previousSibling) {
        node.position = { x: previousSibling.position.x + (previousSibling.width ?? 0) + gap, y: 0 };
      }
    }
  });
};

export const performDefaultAutoLayout = (nodes: Node[], edges: Edge[]): Promise<{ nodes: Node[] }> => {
  const layoutOptions: LayoutOptions = {
    'elk.algorithm': 'layered',
    'elk.layered.spacing.nodeNodeBetweenLayers': '100',
    'org.eclipse.elk.hierarchyHandling': 'INCLUDE_CHILDREN',
    'layering.strategy': 'NETWORK_SIMPLEX',
    'elk.spacing.nodeNode': '80',
    'elk.direction': 'DOWN',
    'elk.layered.spacing.edgeNodeBetweenLayers': '30',
  };
  return performAutoLayout(nodes, edges, layoutOptions);
};

export const performAutoLayout = (
  nodes: Node[],
  edges: Edge[],
  layoutOptions: LayoutOptions
): Promise<{ nodes: Node[] }> => {
  const graph: ElkNode = {
    id: 'root',
    layoutOptions,
    children: [],
    edges: [],
  };
  const hiddenNodes = nodes.filter((node) => node.hidden);
  const visibleNodes = nodes.filter((node) => !node.hidden);
  const nodeId2Node = new Map<string, Node>();
  visibleNodes.forEach((node) => nodeId2Node.set(node.id, node));
  const nodeId2ElkNode = new Map<String, ElkNode>();
  visibleNodes.forEach((node) => {
    const elkNode: ElkNode = {
      id: node.id,
      children: [],
      labels: [],
      layoutOptions: {
        'nodeLabels.placement': '[H_CENTER, V_TOP, INSIDE]',
      },
    };
    if (node.type === 'rectangularNode') {
      const rectangularNodeData: RectangularNodeData = node.data as RectangularNodeData;

      const label = document.querySelector<HTMLDivElement>(`[data-id="${rectangularNodeData.label?.id}"]`);
      if (label) {
        const elkLabel: ElkLabel = {
          width: label.getBoundingClientRect().width,
          height: label.getBoundingClientRect().height,
          text: rectangularNodeData.label?.text,
        };

        elkNode.labels?.push(elkLabel);
      }
    }

    const element = document.querySelector(`[data-id="${node.id}"]`);
    if (element) {
      elkNode.width = element.getBoundingClientRect().width;
      elkNode.height = element.getBoundingClientRect().height;
    }

    nodeId2ElkNode.set(elkNode.id, elkNode);
  });
  visibleNodes.forEach((node) => {
    if (graph.children) {
      if (!!node.parentNode && !!nodeId2ElkNode.get(node.parentNode)) {
        const elknodeChild = nodeId2ElkNode.get(node.id);
        const elkNodeParent = nodeId2ElkNode.get(node.parentNode);
        if (elkNodeParent && elkNodeParent.children && elknodeChild) {
          elkNodeParent.children.push(elknodeChild);
        }
      } else {
        const elkNodeRoot = nodeId2ElkNode.get(node.id);
        if (elkNodeRoot) {
          graph.children.push(elkNodeRoot);
        }
      }
    }
  });

  edges
    .filter((edge) => !edge.hidden)
    .forEach((edge) => {
      if (graph.edges) {
        const elkEdge: ElkExtendedEdge = {
          id: edge.id,
          sources: [edge.source],
          targets: [edge.target],
        };
        graph.edges.push(elkEdge);
      }
    });

  return elk
    .layout(graph)
    .then((laidoutGraph) => elkToReactFlow(laidoutGraph.children ?? [], nodeId2Node))
    .then((laidoutNodes) => {
      return {
        nodes: [...laidoutNodes, ...hiddenNodes],
      };
    });
};

const elkToReactFlow = (elkNodes: ElkNode[], nodeId2Node: Map<string, Node>): Node[] => {
  const nodes: Node[] = [];
  elkNodes.forEach((elkNode) => {
    const node = nodeId2Node.get(elkNode.id);
    if (node) {
      if (!node.position) {
        node.position = { x: 0, y: 0 };
      }
      node.position.x = elkNode.x ?? 0;
      node.position.y = elkNode.y ?? 0;
      node.width = elkNode.width ?? 150;
      node.height = elkNode.height ?? 70;
      if (node.style) {
        if (node.style.width) {
          node.style.width = `${node.width}px`;
        }
        if (node.style.height) {
          node.style.height = `${node.height}px`;
        }
      }
      nodes.push(node);
    }
    if (elkNode.children && elkNode.children.length > 0) {
      const laidoutChildren = elkToReactFlow(elkNode.children, nodeId2Node);
      nodes.push(...laidoutChildren);
    }
  });
  return nodes;
};
