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
package org.eclipse.sirius.components.view.emf.diagram;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.sirius.components.collaborative.diagrams.api.IParametricSVGImageRegistry;
import org.eclipse.sirius.components.collaborative.forms.services.api.IPropertiesDescriptionRegistry;
import org.eclipse.sirius.components.collaborative.forms.services.api.IPropertiesDescriptionRegistryConfigurer;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.diagrams.NodeType;
import org.eclipse.sirius.components.emf.services.EditingContext;
import org.eclipse.sirius.components.forms.SelectStyle;
import org.eclipse.sirius.components.forms.components.SelectComponent;
import org.eclipse.sirius.components.forms.description.AbstractControlDescription;
import org.eclipse.sirius.components.forms.description.GroupDescription;
import org.eclipse.sirius.components.forms.description.ImageDescription;
import org.eclipse.sirius.components.forms.description.PageDescription;
import org.eclipse.sirius.components.forms.description.SelectDescription;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.ColorPalette;
import org.eclipse.sirius.components.view.LabelStyle;
import org.eclipse.sirius.components.view.UserColor;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.ViewPackage;
import org.eclipse.sirius.components.view.diagram.BorderStyle;
import org.eclipse.sirius.components.view.diagram.DiagramPackage;
import org.eclipse.sirius.components.view.diagram.IconLabelNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.ImageNodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.RectangularNodeStyleDescription;
import org.eclipse.sirius.components.view.emf.CustomImageMetadata;
import org.eclipse.sirius.components.view.emf.ICustomImageMetadataSearchService;
import org.eclipse.sirius.components.view.emf.compatibility.IPropertiesConfigurerService;
import org.eclipse.sirius.components.view.emf.compatibility.IPropertiesWidgetCreationService;
import org.eclipse.sirius.components.view.emf.compatibility.PropertiesConfigurerService;
import org.springframework.stereotype.Component;

/**
 * Customizes the properties view for some of the View DSL elements.
 *
 * @author pcdavid
 */
@Component
public class NodeStylePropertiesConfigurer implements IPropertiesDescriptionRegistryConfigurer {

    private static final String EMPTY = "";
    private static final String CUSTOM = "/custom/%s";
    private final List<IParametricSVGImageRegistry> parametricSVGImageRegistries;
    private final ICustomImageMetadataSearchService customImageSearchService;
    private final IPropertiesConfigurerService propertiesConfigurerService;
    private final IPropertiesWidgetCreationService propertiesWidgetCreationService;
    private final IObjectService objectService;

    public NodeStylePropertiesConfigurer(ICustomImageMetadataSearchService customImageSearchService,
            List<IParametricSVGImageRegistry> parametricSVGImageRegistries, PropertiesConfigurerService propertiesConfigurerService, IPropertiesWidgetCreationService propertiesWidgetCreationService, IObjectService objectService) {
        this.customImageSearchService = Objects.requireNonNull(customImageSearchService);
        this.parametricSVGImageRegistries = parametricSVGImageRegistries;
        this.propertiesConfigurerService = Objects.requireNonNull(propertiesConfigurerService);
        this.propertiesWidgetCreationService = Objects.requireNonNull(propertiesWidgetCreationService);
        this.objectService = Objects.requireNonNull(objectService);
    }

    @Override
    public void addPropertiesDescriptions(IPropertiesDescriptionRegistry registry) {
        registry.add(this.getRectangularNodeStyleProperties());
        registry.add(this.getIconLabelNodeStyleProperties());
        registry.add(this.getImageNodeStyleProperties());
    }

    private PageDescription getImageNodeStyleProperties() {
        String id = UUID.nameUUIDFromBytes("nodestyle".getBytes()).toString();

        List<AbstractControlDescription> controls = new ArrayList<>();
        controls.add(this.createShapeSelectionField());
        controls.add(this.createShapePreviewField());
        controls.addAll(this.getGeneralControlDescription(NodeType.NODE_IMAGE));

        GroupDescription groupDescription = this.propertiesWidgetCreationService.createSimpleGroupDescription(controls);

        Predicate<VariableManager> canCreatePagePredicate = variableManager ->  variableManager.get(VariableManager.SELF, Object.class)
                    .filter(ImageNodeStyleDescription.class::isInstance)
                    .isPresent();

        return this.propertiesWidgetCreationService.createSimplePageDescription(id, groupDescription, canCreatePagePredicate);
    }

    private PageDescription getIconLabelNodeStyleProperties() {
        String id = UUID.nameUUIDFromBytes("iconlabelnodestyle".getBytes()).toString();

        List<AbstractControlDescription> controls = this.getGeneralControlDescription(NodeType.NODE_ICON_LABEL);

        GroupDescription groupDescription = this.propertiesWidgetCreationService.createSimpleGroupDescription(controls);

        Predicate<VariableManager> canCreatePagePredicate = variableManager ->  variableManager.get(VariableManager.SELF, Object.class)
                .filter(IconLabelNodeStyleDescription.class::isInstance)
                .isPresent();

        return this.propertiesWidgetCreationService.createSimplePageDescription(id, groupDescription, canCreatePagePredicate);
    }

    private PageDescription getRectangularNodeStyleProperties() {

        String id = UUID.nameUUIDFromBytes("rectangularnodestyle".getBytes()).toString();

        List<AbstractControlDescription> controls = new ArrayList<>();
        controls.add(this.propertiesWidgetCreationService.createCheckbox("nodestyle.isWithHeader", "With Header",
            style -> ((RectangularNodeStyleDescription) style).isWithHeader(),
            (style, newWithHeaderValue) -> ((RectangularNodeStyleDescription) style).setWithHeader(newWithHeaderValue),
                DiagramPackage.Literals.RECTANGULAR_NODE_STYLE_DESCRIPTION__WITH_HEADER, Optional.empty()));

        controls.addAll(this.getGeneralControlDescription(NodeType.NODE_RECTANGLE));

        GroupDescription groupDescription = this.propertiesWidgetCreationService.createSimpleGroupDescription(controls);

        Predicate<VariableManager> canCreatePagePredicate = variableManager ->  variableManager.get(VariableManager.SELF, Object.class)
                .filter(RectangularNodeStyleDescription.class::isInstance)
                .isPresent();

        return this.propertiesWidgetCreationService.createSimplePageDescription(id, groupDescription, canCreatePagePredicate);
    }

    private List<AbstractControlDescription> getGeneralControlDescription(String nodeType) {
        List<AbstractControlDescription> controls = new ArrayList<>();

        var widthExpression = this.propertiesWidgetCreationService.createExpressionField("nodestyle.widthExpression", "Width Expression",
                style -> ((NodeStyleDescription) style).getWidthComputationExpression(),
                (style, newWidthExpression) -> ((NodeStyleDescription) style).setWidthComputationExpression(newWidthExpression),
                DiagramPackage.Literals.NODE_STYLE_DESCRIPTION__WIDTH_COMPUTATION_EXPRESSION);
        controls.add(widthExpression);

        var heightExpression = this.propertiesWidgetCreationService.createExpressionField("nodestyle.heightExpression", "Height Expression",
               style -> ((NodeStyleDescription) style).getHeightComputationExpression(),
               (style, newHeightExpression) -> ((NodeStyleDescription) style).setHeightComputationExpression(newHeightExpression),
               DiagramPackage.Literals.NODE_STYLE_DESCRIPTION__HEIGHT_COMPUTATION_EXPRESSION);
        controls.add(heightExpression);

        var showIcon = this.propertiesWidgetCreationService.createCheckbox("nodestyle.showIcon", "Show Icon",
                style -> ((NodeStyleDescription) style).isShowIcon(),
                (style, newValue) -> ((NodeStyleDescription) style).setShowIcon(newValue),
                DiagramPackage.Literals.NODE_STYLE_DESCRIPTION__SHOW_ICON,
                Optional.of(variableManager -> "Show an icon near the label, use the default one if no custom icon is set."));
        controls.add(showIcon);

        controls.add(this.createIconSelectionField());

        Function<VariableManager, List<?>> colorOptionsProvider = variableManager -> this.getColorsFromColorPalettesStream(variableManager).toList();
        var labelColor = this.propertiesWidgetCreationService.createReferenceWidget("nodestyle.labelColor", "Label Color", DiagramPackage.Literals.NODE_STYLE_DESCRIPTION__LABEL_COLOR, colorOptionsProvider);
        controls.add(labelColor);

        if (!Objects.equals(nodeType, NodeType.NODE_IMAGE)) {
            var color = this.propertiesWidgetCreationService.createReferenceWidget("nodestyle.color", "Color", DiagramPackage.Literals.STYLE__COLOR, colorOptionsProvider);
            controls.add(color);
        }
        var borderColor = this.propertiesWidgetCreationService.createReferenceWidget("nodestyle.borderColor", "Border Color", DiagramPackage.Literals.BORDER_STYLE__BORDER_COLOR, colorOptionsProvider);
        controls.add(borderColor);

        var borderRadius = this.propertiesWidgetCreationService.createTextField("nodestyle.borderRadius", "Border Radius",
                             style -> String.valueOf(((NodeStyleDescription) style).getBorderRadius()),
                             (style, newBorderRadius) -> {
                                 try {
                                     ((NodeStyleDescription) style).setBorderRadius(Integer.parseInt(newBorderRadius));
                                 } catch (NumberFormatException nfe) {
                                     // Ignore.
                                 }
                             },
                             DiagramPackage.Literals.BORDER_STYLE__BORDER_RADIUS);
        controls.add(borderRadius);

        var borderSize = this.propertiesWidgetCreationService.createTextField("nodestyle.borderSize", "Border Size",
                             style -> String.valueOf(((NodeStyleDescription) style).getBorderSize()),
                             (style, newBorderSize) -> {
                                 try {
                                     ((NodeStyleDescription) style).setBorderSize(Integer.parseInt(newBorderSize));
                                 } catch (NumberFormatException nfe) {
                                     // Ignore.
                                 }
                             },
                             DiagramPackage.Literals.BORDER_STYLE__BORDER_SIZE);
        controls.add(borderSize);

        var borderStyle = this.createBorderLineStyleSelectionField();
        controls.add(borderStyle);

        var fontSize = this.propertiesWidgetCreationService.createTextField("nodestyle.fontSize", "Font Size",
                             style -> String.valueOf(((LabelStyle) style).getFontSize()),
                             (style, newColor) -> {
                                 try {
                                     ((LabelStyle) style).setFontSize(Integer.parseInt(newColor));
                                 } catch (NumberFormatException nfe) {
                                     // Ignore.
                                 }
                             },
                             ViewPackage.Literals.LABEL_STYLE__FONT_SIZE);
        controls.add(fontSize);

        var italic = this.propertiesWidgetCreationService.createCheckbox("nodestyle.italic", "Italic",
                style -> ((LabelStyle) style).isItalic(),
                (style, newItalic) -> ((LabelStyle) style).setItalic(newItalic),
                ViewPackage.Literals.LABEL_STYLE__ITALIC, Optional.empty());
        controls.add(italic);

        var bold = this.propertiesWidgetCreationService.createCheckbox("nodestyle.bold", "Bold",
                style -> ((LabelStyle) style).isBold(),
                (style, newBold) -> ((LabelStyle) style).setBold(newBold),
                ViewPackage.Literals.LABEL_STYLE__BOLD, Optional.empty());
        controls.add(bold);

        var underline = this.propertiesWidgetCreationService.createCheckbox("nodestyle.underline", "Underline",
                style -> ((LabelStyle) style).isUnderline(),
                (style, newUnderline) -> ((LabelStyle) style).setUnderline(newUnderline),
                ViewPackage.Literals.LABEL_STYLE__UNDERLINE, Optional.empty());
        controls.add(underline);

        var strikeThrough = this.propertiesWidgetCreationService.createCheckbox("nodestyle.strikeThrough", "Strike Through",
                style -> ((LabelStyle) style).isStrikeThrough(),
                (style, newStrikeThrough) -> ((LabelStyle) style).setStrikeThrough(newStrikeThrough),
                ViewPackage.Literals.LABEL_STYLE__STRIKE_THROUGH, Optional.empty());
        controls.add(strikeThrough);

        return controls;
    }

    private SelectDescription createBorderLineStyleSelectionField() {
        return SelectDescription.newSelectDescription("nodestyle.borderstyle")
                                .idProvider(variableManager -> "nodestyle.borderstyle")
                                .targetObjectIdProvider(this.propertiesConfigurerService.getSemanticTargetIdProvider())
                                .labelProvider(variableManager -> "Border Line Style")
                                .valueProvider(variableManager -> variableManager.get(VariableManager.SELF, BorderStyle.class).map(BorderStyle::getBorderLineStyle).map(LineStyle::toString).orElse(EMPTY))
                                .optionsProvider(variableManager -> LineStyle.VALUES.stream().toList())
                                .optionIdProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, LineStyle.class).map(LineStyle::getLiteral).orElse(EMPTY))
                                .optionLabelProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, LineStyle.class).map(LineStyle::getName).orElse(EMPTY))
                                .optionIconURLProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, Object.class).map(this.objectService::getImagePath).orElse(""))
                                .newValueHandler((variableManager, newValue) -> {
                                    var optionalBorderStyle = variableManager.get(VariableManager.SELF, BorderStyle.class);
                                    if (optionalBorderStyle.isPresent()) {
                                        if (newValue != null && LineStyle.get(newValue) != null) {
                                            optionalBorderStyle.get().setBorderLineStyle(LineStyle.get(newValue));
                                        } else {
                                            optionalBorderStyle.get().setBorderLineStyle(LineStyle.SOLID);
                                        }
                                        return new Success();
                                    }
                                    return new Failure("");
                                })
                                .diagnosticsProvider(this.propertiesConfigurerService.getDiagnosticsProvider(DiagramPackage.Literals.BORDER_STYLE__BORDER_LINE_STYLE))
                                .kindProvider(this.propertiesConfigurerService.getKindProvider())
                                .messageProvider(this.propertiesConfigurerService.getMessageProvider())
                                .build();
    }


    private Stream<UserColor> getColorsFromColorPalettesStream(VariableManager variableManager) {
        return variableManager.get(IEditingContext.EDITING_CONTEXT, IEditingContext.class)
                .filter(EditingContext.class::isInstance)
                .map(EditingContext.class::cast)
                .map(EditingContext::getDomain)
                .map(EditingDomain::getResourceSet)
                .map(ResourceSet::getResources)
                .stream()
                .flatMap(EList::stream)
                .map(Resource::getContents)
                .flatMap(EList::stream)
                .filter(View.class::isInstance)
                .map(View.class::cast)
                .map(View::getColorPalettes)
                .flatMap(EList::stream)
                .map(ColorPalette::getColors)
                .flatMap(EList::stream);
    }

    private SelectDescription createIconSelectionField() {
        return SelectDescription.newSelectDescription("nodestyle.iconLabelSelector")
                .idProvider(variableManager -> "nodestyle.iconLabelSelector")
                .targetObjectIdProvider(this.propertiesConfigurerService.getSemanticTargetIdProvider())
                .labelProvider(variableManager -> "Custom Icon")
                .styleProvider(vm -> SelectStyle.newSelectStyle().showIcon(true).build())
                .valueProvider(variableManager -> variableManager.get(VariableManager.SELF, NodeStyleDescription.class).map(NodeStyleDescription::getLabelIcon).orElse(EMPTY))
                .optionsProvider(variableManager -> variableManager.get(IEditingContext.EDITING_CONTEXT, IEditingContext.class)
                        .map(IEditingContext::getId)
                        .map(this.customImageSearchService::getAvailableImages)
                        .orElse(List.of())
                )
                .optionIdProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, CustomImageMetadata.class)
                        .map(customImageMetadataEntity -> String.format(CUSTOM, customImageMetadataEntity.getId().toString()))
                        .orElse(EMPTY))
                .optionLabelProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, CustomImageMetadata.class)
                        .map(CustomImageMetadata::getLabel)
                        .orElse(EMPTY))
                .optionIconURLProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, CustomImageMetadata.class)
                        .map(customImageMetadataEntity -> String.format(CUSTOM, customImageMetadataEntity.getId().toString()))
                        .orElse(EMPTY))
                .newValueHandler(this.getIconLabelValueHandler())
                .diagnosticsProvider(this.propertiesConfigurerService.getDiagnosticsProvider(DiagramPackage.Literals.NODE_STYLE_DESCRIPTION__LABEL_ICON))
                .kindProvider(this.propertiesConfigurerService.getKindProvider())
                .messageProvider(this.propertiesConfigurerService.getMessageProvider())
                .helpTextProvider(variableManager -> "Set a custom icon for the label, use in association with Show Icon property")
                .build();
    }

    private SelectDescription createShapeSelectionField() {
        return SelectDescription.newSelectDescription("nodestyle.shapeSelector")
                .idProvider(variableManager -> "nodestyle.shapeSelector")
                .targetObjectIdProvider(this.propertiesConfigurerService.getSemanticTargetIdProvider())
                .labelProvider(variableManager -> "Shape")
                .valueProvider(variableManager -> variableManager.get(VariableManager.SELF, ImageNodeStyleDescription.class).map(ImageNodeStyleDescription::getShape).orElse(EMPTY))
                .optionsProvider(variableManager -> {
                    Optional<String> optionalEditingContextId = variableManager.get(IEditingContext.EDITING_CONTEXT, IEditingContext.class).map(IEditingContext::getId);

                    Stream<CustomImageMetadata> parametricSVGs = this.parametricSVGImageRegistries.stream()
                        .flatMap(service-> service.getImages().stream())
                        .map(image -> new CustomImageMetadata(image.getId(), optionalEditingContextId, image.getLabel(), "image/svg+xml"));

                    List<CustomImageMetadata> customImages = optionalEditingContextId.map(this.customImageSearchService::getAvailableImages).orElse(List.of());

                    return Stream.concat(parametricSVGs, customImages.stream())
                            .sorted(Comparator.comparing(CustomImageMetadata::getLabel))
                            .toList();
                })
                .optionIdProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, CustomImageMetadata.class)
                        .map(CustomImageMetadata::getId)
                        .map(UUID::toString)
                        .orElse(EMPTY))
                .optionLabelProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, CustomImageMetadata.class)
                        .map(CustomImageMetadata::getLabel)
                        .orElse(EMPTY))
                .optionIconURLProvider(variableManager -> variableManager.get(SelectComponent.CANDIDATE_VARIABLE, Object.class).map(this.objectService::getImagePath).orElse(""))
                .newValueHandler(this.getNewShapeValueHandler())
                .diagnosticsProvider(this.propertiesConfigurerService.getDiagnosticsProvider(DiagramPackage.Literals.IMAGE_NODE_STYLE_DESCRIPTION__SHAPE))
                .kindProvider(this.propertiesConfigurerService.getKindProvider())
                .messageProvider(this.propertiesConfigurerService.getMessageProvider())
                .build();
    }

    private ImageDescription createShapePreviewField() {
        return ImageDescription.newImageDescription("nodestyle.shapePreview")
                .targetObjectIdProvider(this.propertiesConfigurerService.getSemanticTargetIdProvider())
                .idProvider(variableManager -> "nodestyle.shapePreview")
                .labelProvider(variableManager -> "Shape Preview")
                .urlProvider(variableManager -> {
                    var optionalShape = variableManager.get(VariableManager.SELF, ImageNodeStyleDescription.class).map(ImageNodeStyleDescription::getShape);
                    if (optionalShape.isPresent()) {
                        return String.format(CUSTOM, optionalShape.get());
                    }
                    return "";
                })
                .maxWidthProvider(variableManager -> "300px")
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(this.propertiesConfigurerService.getKindProvider())
                .messageProvider(this.propertiesConfigurerService.getMessageProvider())
                .build();
    }

    private BiFunction<VariableManager, String, IStatus> getIconLabelValueHandler() {
        return (variableManager, newValue) -> {
            var optionalNodeStyle = variableManager.get(VariableManager.SELF, NodeStyleDescription.class);
            if (optionalNodeStyle.isPresent()) {
                String newIcon = newValue;
                if (newValue != null && newValue.isBlank()) {
                    newIcon = null;
                }
                optionalNodeStyle.get().setLabelIcon(newIcon);
                return new Success();
            }
            return new Failure("");
        };
    }

    private BiFunction<VariableManager, String, IStatus> getNewShapeValueHandler() {
        return (variableManager, newValue) -> {
            var optionalNodeStyle = variableManager.get(VariableManager.SELF, ImageNodeStyleDescription.class);
            if (optionalNodeStyle.isPresent()) {
                String newShape = newValue;
                if (newValue != null && newValue.isBlank()) {
                    newShape = null;
                }
                optionalNodeStyle.get().setShape(newShape);
                return new Success();
            }
            return new Failure("");
        };
    }

}
