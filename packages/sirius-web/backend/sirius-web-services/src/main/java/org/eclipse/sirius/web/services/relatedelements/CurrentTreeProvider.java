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
package org.eclipse.sirius.web.services.relatedelements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.sirius.components.compatibility.emf.properties.api.IPropertiesValidationProvider;
import org.eclipse.sirius.components.compatibility.forms.WidgetIdProvider;
import org.eclipse.sirius.components.compatibility.services.ImageConstants;
import org.eclipse.sirius.components.core.api.IObjectService;
import org.eclipse.sirius.components.forms.TreeNode;
import org.eclipse.sirius.components.forms.components.TreeComponent;
import org.eclipse.sirius.components.forms.description.TreeDescription;
import org.eclipse.sirius.components.representations.VariableManager;

/**
 * Provides the definition of the tree widget for the "Current" panel in the "Related Elements" view. It displays two
 * top-level categories, "Parent" and "Children", implemented as plain strings.
 * <ul>
 * <li>Inside "Parent" there is zero or one semantic element, the parent of the selected element (if it has one).</li>
 * <li>Inside "Children" there is one sub-category per non-empty containment reference of the selected element. The
 * categories are implemented using the corresponding EReference.</li>
 * <li>Finally, inside a given reference sub-category there is one node per semantic element contained through that
 * reference.
 * <li>
 * </ul>
 *
 * @author pcdavid
 */
public class CurrentTreeProvider {

    private static final String WIDGET_ID = "related/current";

    private static final String CATEGORY_PARENT = "Parent";

    private static final String CATEGORY_CHILDREN = "Children";

    private static final String TITLE = "Current";

    private static final String WIDGET_ICON_URL = "/images/arrow_downward_black_24dp.svg";

    private static final String FOLDER_ICON_URL = "/images/folder_black_24dp.svg";

    private static final String CHILDREN_CATEGORY_ICON_URL = "/images/subdirectory_arrow_right_black_24dp.svg";

    private static final String CATEGORY_KIND = "siriusWeb://category";

    private static final String CONTAINMENT_REFERENCE_KIND = "siriusWeb://category/containment-reference";

    private final IObjectService objectService;

    private final AdapterFactory adapterFactory;

    private final IPropertiesValidationProvider propertiesValidationProvider = new IPropertiesValidationProvider.NoOp();

    public CurrentTreeProvider(IObjectService objectService, AdapterFactory adapterFactory) {
        this.objectService = Objects.requireNonNull(objectService);
        this.adapterFactory = Objects.requireNonNull(adapterFactory);
    }

    public TreeDescription getTreeDescription() {
        // @formatter:off
        return TreeDescription.newTreeDescription(WIDGET_ID)
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(variableManager -> variableManager.get(VariableManager.SELF, Object.class).map(this.objectService::getId).orElse(null))
                .diagnosticsProvider(this.propertiesValidationProvider.getDiagnosticsProvider())
                .kindProvider(this.propertiesValidationProvider.getKindProvider())
                .messageProvider(this.propertiesValidationProvider.getMessageProvider())
                .labelProvider(variableManager -> TITLE)
                .iconURLProvider(variableManager -> WIDGET_ICON_URL)
                .nodeIdProvider(this::getNodeId)
                .nodeLabelProvider(this::getNodeLabel)
                .nodeImageURLProvider(this::getNodeImageURL)
                .nodeKindProvider(this::getNodeKind)
                .nodeSelectableProvider(this::isNodeSelectable)
                .childrenProvider(this::getCurrentChildren)
                .expandedNodeIdsProvider(this::collectAllNodeIds)
                .build();
        // @formatter:on
    }

    private List<?> getCurrentChildren(VariableManager variableManager) {
        var self = variableManager.get(VariableManager.SELF, Object.class);
        var root = variableManager.get(TreeComponent.ROOT_VARIABLE, EObject.class);
        var ancestors = variableManager.get(TreeComponent.ANCESTORS_VARIABLE, List.class);
        if (root.isPresent() && self.isPresent() && ancestors.isPresent()) {
            return this.getCurrentChildren(self.get(), root.get(), ancestors.get());
        } else {
            return List.of();
        }
    }

    private List<?> getCurrentChildren(Object self, EObject root, List<?> ancestors) {
        List<?> result = List.of();
        if (ancestors.isEmpty()) {
            result = List.of(CATEGORY_PARENT, CATEGORY_CHILDREN);
        } else if (self.equals(CATEGORY_PARENT) && root.eContainer() != null) {
            result = List.of(root.eContainer());
        } else if (self.equals(CATEGORY_CHILDREN)) {
            result = this.getNonEmptyContainmentReferences(root);
        } else if (self instanceof EReference) {
            result = this.readReference(root, (EReference) self);
        }
        return result;
    }

    private List<?> getNonEmptyContainmentReferences(EObject self) {
        // @formatter:off
        return self.eClass().getEAllReferences().stream()
                   .filter(EReference::isContainment)
                   .filter(eReference -> {
                       if (eReference.isMany()) {
                           return !((EList<?>) self.eGet(eReference)).isEmpty();
                       } else {
                           return self.eGet(eReference) != null;
                       }
                   })
                   .sorted(Comparator.comparing(EStructuralFeature::getName))
                   .toList();
        // @formatter:on
    }

    private List<?> readReference(EObject self, EReference eReference) {
        List<?> result;
        if (eReference.isMany()) {
            result = (EList<?>) self.eGet(eReference);
        } else {
            result = Optional.ofNullable(self.eGet(eReference)).stream().toList();
        }
        return result;
    }

    private String getNodeId(VariableManager variableManager) {
        String result = null;
        var self = variableManager.get(VariableManager.SELF, Object.class).orElse(null);
        if (self instanceof String) {
            result = "category/" + (String) self;
        } else if (self instanceof EReference) {
            result = "reference/" + ((EReference) self).getName();
        } else if (self != null) {
            result = this.objectService.getId(self);
        }
        return result;
    }

    private String getNodeLabel(VariableManager variableManager) {
        String result = null;
        var self = variableManager.get(VariableManager.SELF, Object.class).orElse(null);
        if (self instanceof String) {
            result = (String) self;
        } else if (self instanceof EReference) {
            var optionalRootEObject = variableManager.get(TreeComponent.ROOT_VARIABLE, EObject.class);
            if (optionalRootEObject.isPresent()) {
                result = this.getContainmentReferenceLabel(optionalRootEObject.get(), (EReference) self);
            }
            if (result == null) {
                result = this.objectService.getLabel(self);
            }
        } else if (self != null) {
            result = this.objectService.getLabel(self);
        }
        return result;
    }

    /**
     * EMF Edit does not generate IItemPropertyDescriptors for containment references, so we have to resort to looking
     * up the label directly in the ResourceLocator.
     */
    private String getContainmentReferenceLabel(EObject eObject, EReference eReference) {
        Adapter adapter = this.adapterFactory.adapt(eObject, IItemLabelProvider.class);
        if (adapter instanceof ItemProviderAdapter) {
            ItemProviderAdapter editingDomainItemProvider = (ItemProviderAdapter) adapter;
            String key = String.format("_UI_%s_%s_feature", eReference.getEContainingClass().getName(), eReference.getName());
            try {
                return editingDomainItemProvider.getString(key);
            } catch (MissingResourceException mre) {
                // Expected for dynamic instances.
            }
        }
        return null;
    }

    private String getNodeImageURL(VariableManager variableManager) {
        String result = null;
        var self = variableManager.get(VariableManager.SELF, Object.class).orElse(null);
        if (self instanceof String) {
            result = FOLDER_ICON_URL;
        } else if (self instanceof EReference) {
            result = CHILDREN_CATEGORY_ICON_URL;
        } else if (self != null) {
            result = this.objectService.getImagePath(self);
        }
        return Optional.ofNullable(result).orElse(ImageConstants.DEFAULT_SVG);
    }

    private String getNodeKind(VariableManager variableManager) {
        String result = null;
        var self = variableManager.get(VariableManager.SELF, Object.class).orElse(null);
        if (self instanceof String) {
            result = CATEGORY_KIND;
        } else if (self instanceof EReference) {
            result = CONTAINMENT_REFERENCE_KIND;
        } else if (self != null) {
            result = this.objectService.getKind(self);
        }
        return result;
    }

    private boolean isNodeSelectable(VariableManager variableManager) {
        var self = variableManager.get(VariableManager.SELF, Object.class).orElse(null);
        return self instanceof EObject && !(self instanceof EReference);
    }

    private List<String> collectAllNodeIds(VariableManager variableManager) {
        List<String> result = new ArrayList<>();
        for (var element : variableManager.get(TreeComponent.NODES_VARIABLE, List.class).orElse(List.of())) {
            if (element instanceof TreeNode) {
                TreeNode node = (TreeNode) element;
                result.add(node.getId());
            }
        }
        return result;
    }

}
