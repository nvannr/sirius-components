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
package org.eclipse.sirius.components.view.emf.diagram;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramDescriptionService;
import org.eclipse.sirius.components.collaborative.diagrams.dto.SingleClickOnDiagramElementTool;
import org.eclipse.sirius.components.collaborative.diagrams.dto.SingleClickOnTwoDiagramElementsTool;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.core.api.IURLParser;
import org.eclipse.sirius.components.diagrams.FreeFormLayoutStrategy;
import org.eclipse.sirius.components.diagrams.Size;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.eclipse.sirius.components.diagrams.description.LabelDescription;
import org.eclipse.sirius.components.diagrams.description.LabelStyleDescription;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.EdgePalette;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.EdgeToolSection;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.NodeToolSection;
import org.eclipse.sirius.components.view.emf.IViewRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.view.emf.configuration.ViewPaletteToolsConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

/**
 * Unit tests of the ViewPaletteProvider.
 *
 * @author frouene
 */
public class ViewPaletteProviderTests {

    private static EdgeDescription getEdgeDescription() {
        EdgeDescription edgeDescription = DiagramFactory.eINSTANCE.createEdgeDescription();
        EdgePalette edgePalette = DiagramFactory.eINSTANCE.createEdgePalette();
        edgeDescription.setPalette(edgePalette);
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        edgePalette.getNodeTools().add(nodeTool);
        EdgeToolSection toolSection = DiagramFactory.eINSTANCE.createEdgeToolSection();
        edgePalette.getToolSections().add(toolSection);
        NodeTool nodeToolInToolSection = DiagramFactory.eINSTANCE.createNodeTool();
        toolSection.getNodeTools().add(nodeToolInToolSection);
        return edgeDescription;
    }

    private static org.eclipse.sirius.components.view.diagram.NodeDescription getNodeDescription() {
        org.eclipse.sirius.components.view.diagram.NodeDescription nodeDescription = DiagramFactory.eINSTANCE.createNodeDescription();
        NodePalette nodePalette = DiagramFactory.eINSTANCE.createNodePalette();
        nodeDescription.setPalette(nodePalette);
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        EdgeTool edgeTool = DiagramFactory.eINSTANCE.createEdgeTool();
        nodePalette.getNodeTools().add(nodeTool);
        nodePalette.getEdgeTools().add(edgeTool);
        NodeToolSection toolSection = DiagramFactory.eINSTANCE.createNodeToolSection();
        nodePalette.getToolSections().add(toolSection);
        NodeTool nodeToolInToolSection = DiagramFactory.eINSTANCE.createNodeTool();
        toolSection.getNodeTools().add(nodeToolInToolSection);
        EdgeTool edgeToolInToolSection = DiagramFactory.eINSTANCE.createEdgeTool();
        toolSection.getEdgeTools().add(edgeToolInToolSection);
        return nodeDescription;
    }

    private static org.eclipse.sirius.components.view.diagram.DiagramDescription getDiagramDescription() {
        org.eclipse.sirius.components.view.diagram.DiagramDescription diagramDescription = DiagramFactory.eINSTANCE.createDiagramDescription();
        DiagramPalette diagramPalette = DiagramFactory.eINSTANCE.createDiagramPalette();
        diagramDescription.setPalette(diagramPalette);
        NodeTool nodeTool = DiagramFactory.eINSTANCE.createNodeTool();
        diagramPalette.getNodeTools().add(nodeTool);
        DiagramToolSection diagramToolSection = DiagramFactory.eINSTANCE.createDiagramToolSection();
        diagramPalette.getToolSections().add(diagramToolSection);
        NodeTool nodeToolInToolSection = DiagramFactory.eINSTANCE.createNodeTool();
        diagramToolSection.getNodeTools().add(nodeToolInToolSection);
        return diagramDescription;
    }

    @Test
    public void getDiagramPaletteTest() {
        ViewPaletteProvider viewPaletteProvider = this.createViewPaletteProvider();

        DiagramDescription diagramDescription = this.createDiagramDescription();

        VariableManager variableManager = new VariableManager();
        AQLInterpreter interpreter = new AQLInterpreter(List.of(), List.of(EcorePackage.eINSTANCE));
        var result = viewPaletteProvider.getDiagramPalette(diagramDescription, getDiagramDescription(), variableManager, interpreter);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("siriusComponents://diagramPalette?diagramId=sourceElementId");
        assertThat(result.tools()).hasSize(1);
        assertThat(result.tools().get(0)).isInstanceOf(SingleClickOnDiagramElementTool.class);
        assertThat(((SingleClickOnDiagramElementTool) result.tools().get(0)).appliesToDiagramRoot()).isTrue();
        assertThat(result.toolSections()).hasSize(1);
        assertThat(result.toolSections().get(0).tools()).hasSize(1);
        assertThat(result.toolSections().get(0).tools().get(0)).isInstanceOf(SingleClickOnDiagramElementTool.class);
        assertThat(((SingleClickOnDiagramElementTool) result.toolSections().get(0).tools().get(0)).appliesToDiagramRoot()).isTrue();
    }

    @Test
    public void getNodePaletteTest() {
        ViewPaletteProvider viewPaletteProvider = this.createViewPaletteProvider();

        VariableManager variableManager = new VariableManager();
        AQLInterpreter interpreter = new AQLInterpreter(List.of(), List.of(EcorePackage.eINSTANCE));
        var result = viewPaletteProvider.getNodePalette(this.createDiagramDescription(), this.createNodeDescription(), List.of(), variableManager, interpreter);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("siriusComponents://nodePalette?nodeId=sourceElementId");
        assertThat(result.tools()).hasSize(2);
        assertThat(result.tools().get(0)).isInstanceOf(SingleClickOnDiagramElementTool.class);
        assertThat(((SingleClickOnDiagramElementTool) result.tools().get(0)).appliesToDiagramRoot()).isFalse();
        assertThat(result.tools().get(1)).isInstanceOf(SingleClickOnTwoDiagramElementsTool.class);
        assertThat(((SingleClickOnTwoDiagramElementsTool) result.tools().get(1)).candidates()).isNotEmpty();
        assertThat(result.toolSections()).hasSize(1);
        assertThat(result.toolSections().get(0).tools()).hasSize(2);
    }

    @Test
    public void getEdgePaletteTest() {
        ViewPaletteProvider viewPaletteProvider = this.createViewPaletteProvider();
        org.eclipse.sirius.components.diagrams.description.EdgeDescription edgeDescription =
                org.eclipse.sirius.components.diagrams.description.EdgeDescription.newEdgeDescription("edgeDescriptionId")
                        .deleteHandler(vm -> new Success())
                        .labelEditHandler((vm, edgeKind, newLabel) -> new Success())
                        .semanticElementsProvider(vm -> List.of(new Object()))
                        .sourceNodeDescriptions(List.of())
                        .sourceNodesProvider(vm -> List.of())
                        .styleProvider(vm -> null)
                        .targetNodeDescriptions(List.of())
                        .targetNodesProvider(vm -> List.of())
                        .targetObjectIdProvider(vm -> "")
                        .targetObjectKindProvider(vm -> "")
                        .targetObjectLabelProvider(vm -> "")
                        .build();

        VariableManager variableManager = new VariableManager();
        AQLInterpreter interpreter = new AQLInterpreter(List.of(), List.of(EcorePackage.eINSTANCE));
        var result = viewPaletteProvider.getEdgePalette(edgeDescription, List.of(), variableManager, interpreter);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("siriusComponents://edgePalette?edgeId=sourceElementId");
        assertThat(result.tools()).hasSize(1);
        assertThat(result.tools().get(0)).isInstanceOf(SingleClickOnDiagramElementTool.class);
        assertThat(((SingleClickOnDiagramElementTool) result.tools().get(0)).appliesToDiagramRoot()).isFalse();
        assertThat(result.toolSections()).hasSize(1);
        assertThat(result.toolSections().get(0).tools()).hasSize(1);
    }

    private ViewPaletteProvider createViewPaletteProvider() {
        IURLParser urlParser = url -> Map.of(IDiagramIdProvider.SOURCE_ELEMENT_ID, List.of("sourceElementId"));

        IViewRepresentationDescriptionSearchService viewRepresentationDescriptionSearchService = new IViewRepresentationDescriptionSearchService.NoOp() {

            @Override
            public Optional<RepresentationDescription> findById(String representationDescriptionId) {
                org.eclipse.sirius.components.view.diagram.DiagramDescription diagramDescription = getDiagramDescription();
                return Optional.of(diagramDescription);
            }

            @Override
            public Optional<org.eclipse.sirius.components.view.diagram.NodeDescription> findViewNodeDescriptionById(String nodeDescriptionId) {

                org.eclipse.sirius.components.view.diagram.NodeDescription nodeDescription = getNodeDescription();
                return Optional.of(nodeDescription);
            }

            @Override
            public Optional<EdgeDescription> findViewEdgeDescriptionById(String edgeDescriptionId) {
                EdgeDescription edgeDescription = getEdgeDescription();
                return Optional.of(edgeDescription);
            }
        };
        ViewPaletteToolsConfiguration viewPaletteToolsConfiguration = new ViewPaletteToolsConfiguration(urlParser, iRepresentationDescription -> true,
                viewRepresentationDescriptionSearchService, new IObjectService.NoOp());
        return new ViewPaletteProvider(viewPaletteToolsConfiguration, new IDiagramDescriptionService.NoOp(),
                new IDiagramIdProvider.NoOp(), List.of(), new StaticApplicationContext());
    }

    private DiagramDescription createDiagramDescription() {
        return DiagramDescription.newDiagramDescription("diagramDescriptionID")
                .label("DiagramDescriptionTest")
                .targetObjectIdProvider(variableManager -> "diagramTargetObjectId")
                .canCreatePredicate(variableManager -> true)
                .labelProvider(variableManager -> "Diagram")
                .palettes(List.of())
                .nodeDescriptions(List.of())
                .edgeDescriptions(List.of())
                .dropHandler(variableManager -> new Failure(""))
                .build();
    }

    private NodeDescription createNodeDescription() {
        LabelStyleDescription styleDescription = LabelStyleDescription.newLabelStyleDescription()
                .colorProvider(variableManager -> "")
                .fontSizeProvider(variableManager -> 0)
                .boldProvider(variableManager -> false)
                .italicProvider(variableManager -> false)
                .underlineProvider(variableManager -> false)
                .strikeThroughProvider(variableManager -> false)
                .iconURLProvider(variableManager -> "")
                .build();

        LabelDescription labelDescription = LabelDescription.newLabelDescription("nodeId")
                .idProvider(variableManager -> "")
                .textProvider(variableManager -> "")
                .styleDescriptionProvider(variableManager -> styleDescription)
                .build();

        return NodeDescription.newNodeDescription("nodeId")
                .typeProvider(variableManager -> "")
                .targetObjectIdProvider(variableManager -> "")
                .targetObjectKindProvider(variableManager -> "")
                .targetObjectLabelProvider(variableManager -> "")
                .semanticElementsProvider(variableManager -> List.of())
                .labelDescription(labelDescription)
                .styleProvider(variableManager -> null)
                .childrenLayoutStrategyProvider(variableManager -> new FreeFormLayoutStrategy())
                .sizeProvider(variableManager -> Size.UNDEFINED)
                .borderNodeDescriptions(List.of())
                .childNodeDescriptions(List.of())
                .labelEditHandler((variableManager, newLabel) -> new Success())
                .deleteHandler(variableManager -> new Success())
                .build();
    }
}
