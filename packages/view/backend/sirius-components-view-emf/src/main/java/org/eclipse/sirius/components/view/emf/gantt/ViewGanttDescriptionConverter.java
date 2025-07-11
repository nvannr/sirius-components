/*******************************************************************************
 * Copyright (c) 2023, 2025 Obeo.
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
package org.eclipse.sirius.components.view.emf.gantt;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.emf.DomainClassPredicate;
import org.eclipse.sirius.components.gantt.description.GanttDescription;
import org.eclipse.sirius.components.gantt.description.TaskDescription;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.GetOrCreateRandomIdProvider;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.Operation;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.emf.IRepresentationDescriptionConverter;
import org.eclipse.sirius.components.view.emf.ViewIconURLsProvider;
import org.eclipse.sirius.components.view.emf.operations.api.IOperationExecutor;
import org.springframework.stereotype.Service;

/**
 * Converts a View-based gantt description into an equivalent {@link GanttDescription}.
 *
 * @author lfasani
 */
@Service
public class ViewGanttDescriptionConverter implements IRepresentationDescriptionConverter {

    private static final String DEFAULT_GANTT_LABEL = "Gantt";

    private static final String DEFAULT_GANTT_DESCRIPTION_LABEL = "Gantt Description";

    private final IIdentityService identityService;

    private final ILabelService labelService;

    private final IOperationExecutor operationExecutor;

    private final Function<VariableManager, String> semanticTargetIdProvider;

    private final Function<VariableManager, String> semanticTargetKindProvider;

    private final Function<VariableManager, String> semanticTargetLabelProvider;

    private final GanttIdProvider ganttIdProvider;

    public ViewGanttDescriptionConverter(IIdentityService identityService, ILabelService labelService, IOperationExecutor operationExecutor, GanttIdProvider ganttIdProvider) {
        this.identityService = Objects.requireNonNull(identityService);
        this.labelService = Objects.requireNonNull(labelService);
        this.operationExecutor = Objects.requireNonNull(operationExecutor);
        this.ganttIdProvider = Objects.requireNonNull(ganttIdProvider);
        this.semanticTargetIdProvider = variableManager -> this.self(variableManager).map(this.identityService::getId).orElse(null);
        this.semanticTargetKindProvider = variableManager -> this.self(variableManager).map(this.identityService::getKind).orElse(null);
        this.semanticTargetLabelProvider = variableManager -> this.self(variableManager).map(this.labelService::getStyledLabel).map(Object::toString).orElse(null);
    }

    @Override
    public boolean canConvert(RepresentationDescription representationDescription) {
        return representationDescription instanceof org.eclipse.sirius.components.view.gantt.GanttDescription;
    }

    @Override
    public IRepresentationDescription convert(RepresentationDescription viewRepresentationDescription, List<RepresentationDescription> allRepresentationDescriptions, AQLInterpreter interpreter) {
        org.eclipse.sirius.components.view.gantt.GanttDescription viewGanttDescription = (org.eclipse.sirius.components.view.gantt.GanttDescription) viewRepresentationDescription;

        Map<org.eclipse.sirius.components.view.gantt.TaskDescription, String> taskDescription2Ids = new LinkedHashMap<>();
        this.computeTaskDescription2Ids(viewGanttDescription.getTaskElementDescriptions(), taskDescription2Ids);


        List<TaskDescription> taskDescriptions = viewGanttDescription.getTaskElementDescriptions().stream()
                .map(taskDescription -> this.convert(taskDescription, interpreter, taskDescription2Ids))
                .toList();

        return GanttDescription.newGanttDescription(this.ganttIdProvider.getId(viewGanttDescription))
                .label(Optional.ofNullable(viewGanttDescription.getName()).orElse(DEFAULT_GANTT_DESCRIPTION_LABEL))
                .idProvider(new GetOrCreateRandomIdProvider())
                .canCreatePredicate(variableManager -> this.canCreate(viewGanttDescription.getDomainType(), viewGanttDescription.getPreconditionExpression(), variableManager, interpreter))
                .labelProvider(variableManager -> this.computeGanttLabel(viewGanttDescription, variableManager, interpreter))
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .createTaskProvider(Optional.ofNullable(viewGanttDescription.getCreateTool()).map(tool -> this.getOperationsHandler(tool.getBody(), interpreter)).orElse(variable -> {
                }))
                .editTaskProvider(Optional.ofNullable(viewGanttDescription.getEditTool()).map(tool -> this.getOperationsHandler(tool.getBody(), interpreter)).orElse(variable -> {
                }))
                .deleteTaskProvider(Optional.ofNullable(viewGanttDescription.getDeleteTool()).map(tool -> this.getOperationsHandler(tool.getBody(), interpreter)).orElse(variable -> {
                }))
                .dropTaskProvider(Optional.ofNullable(viewGanttDescription.getDropTool()).map(tool -> this.getOperationsHandler(tool.getBody(), interpreter)).orElse(variable -> {
                }))
                .createTaskDependencyProvider(Optional.ofNullable(viewGanttDescription.getCreateTaskDependencyTool()).map(tool -> this.getOperationsHandler(tool.getBody(), interpreter))
                        .orElse(variable -> {
                        }))
                .deleteTaskDependencyProvider(Optional.ofNullable(viewGanttDescription.getDeleteTaskDependencyTool()).map(tool -> this.getOperationsHandler(tool.getBody(), interpreter))
                        .orElse(variable -> {
                        }))
                .taskDescriptions(taskDescriptions)
                .dateRoundingProvider(variableManager -> this.evaluateString(interpreter, variableManager, viewGanttDescription.getDateRoundingExpression()))
                .iconURLsProvider(new ViewIconURLsProvider(interpreter, viewGanttDescription.getIconExpression()))
                .build();
    }

    private void computeTaskDescription2Ids(List<org.eclipse.sirius.components.view.gantt.TaskDescription> taskDescriptions,
            Map<org.eclipse.sirius.components.view.gantt.TaskDescription, String> taskDescription2Ids) {
        if (taskDescriptions != null) {
            taskDescriptions.forEach(taskDescription -> {
                taskDescription2Ids.put(taskDescription, this.ganttIdProvider.getId(taskDescription));

                this.computeTaskDescription2Ids(taskDescription.getSubTaskElementDescriptions(), taskDescription2Ids);
            });
        }
    }

    private Consumer<VariableManager> getOperationsHandler(List<Operation> operations, AQLInterpreter interpreter) {
        return variableManager -> this.operationExecutor.execute(interpreter, variableManager, operations);
    }

    private TaskDescription convert(org.eclipse.sirius.components.view.gantt.TaskDescription viewTaskDescription, AQLInterpreter interpreter, Map<org.eclipse.sirius.components.view.gantt.TaskDescription, String> taskDescription2Ids) {
        List<String> reusedTaskDescriptionIds = viewTaskDescription.getReusedTaskElementDescriptions().stream()
                .map(taskDescription2Ids::get)
                .toList();

        List<TaskDescription> subTaskDescriptions = Optional.ofNullable(viewTaskDescription.getSubTaskElementDescriptions())
                .stream()
                .flatMap(Collection::stream)
                .map(viewTaskDesc -> this.convert(viewTaskDesc, interpreter, taskDescription2Ids))
                .toList();

        TaskDescription taskDescription = TaskDescription.newTaskDescription(taskDescription2Ids.get(viewTaskDescription))
                .semanticElementsProvider(variableManager -> this.getSemanticCandidateElements(variableManager, interpreter, viewTaskDescription))
                .nameProvider(variableManager -> this.evaluateExpression(variableManager, interpreter, viewTaskDescription.getNameExpression(), String.class, ""))
                .descriptionProvider(variableManager -> this.evaluateExpression(variableManager, interpreter, viewTaskDescription.getDescriptionExpression(), String.class, ""))
                .startTimeProvider(variableManager -> this.getTemporalFromExpression(variableManager, interpreter, viewTaskDescription.getStartTimeExpression()))
                .endTimeProvider(variableManager -> this.getTemporalFromExpression(variableManager, interpreter, viewTaskDescription.getEndTimeExpression()))
                .progressProvider(variableManager -> this.evaluateExpression(variableManager, interpreter, viewTaskDescription.getProgressExpression(), Integer.class, 0))
                .computeStartEndDynamicallyProvider(variableManager -> this.evaluateExpression(variableManager, interpreter, viewTaskDescription.getComputeStartEndDynamicallyExpression(), Boolean.class, false))
                .taskDependenciesProvider(variableManager -> this.getTaskDependencies(variableManager, interpreter, viewTaskDescription.getTaskDependenciesExpression()))
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .targetObjectKindProvider(this.semanticTargetKindProvider)
                .targetObjectLabelProvider(this.semanticTargetLabelProvider)
                .reusedTaskDescriptionIds(reusedTaskDescriptionIds)
                .subTaskDescriptions(subTaskDescriptions)
                .build();
        return taskDescription;
    }

    private Temporal getTemporalFromExpression(VariableManager variableManager, AQLInterpreter interpreter, String expression) {
        Temporal result = null;

        var optionalObject = interpreter.evaluateExpression(variableManager.getVariables(), expression).asObject();
        if (optionalObject.isPresent()) {
            var object = optionalObject.get();
            if (object instanceof Temporal temporal) {
                result = temporal;
            } else if (object instanceof String string) {
                try {
                    result = Instant.parse(string);
                } catch (DateTimeParseException e) {
                    try {
                        result = LocalDate.parse(string);
                    } catch (DateTimeParseException e2) {
                    }
                }
            }
        }
        return result;
    }


    private <T> T evaluateExpression(VariableManager variableManager, AQLInterpreter interpreter, String expression, Class<T> type, T defaultValue) {
        T value = interpreter.evaluateExpression(variableManager.getVariables(), expression)
                .asObject()
                .filter(type::isInstance)
                .map(type::cast)
                .orElse(defaultValue);

        return value;
    }

    private List<EObject> getSemanticCandidateElements(VariableManager variableManager, AQLInterpreter interpreter, org.eclipse.sirius.components.view.gantt.TaskDescription viewTaskDescription) {
        List<EObject> semanticObjects = interpreter.evaluateExpression(variableManager.getVariables(), viewTaskDescription.getSemanticCandidatesExpression())
                .asObjects()
                .orElseGet(List::of).stream()
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast)
                .filter(candidate -> new DomainClassPredicate(Optional.ofNullable(viewTaskDescription.getDomainType()).orElse("")).test(candidate.eClass()))
                .toList();
        return semanticObjects;
    }

    private List<Object> getTaskDependencies(VariableManager variableManager, AQLInterpreter interpreter, String expression) {
        List<Object> semanticObjects = interpreter.evaluateExpression(variableManager.getVariables(), expression)
                .asObjects()
                .orElseGet(List::of).stream()
                .filter(EObject.class::isInstance)
                .toList();
        return semanticObjects;
    }

    private boolean canCreate(String domainType, String preconditionExpression, VariableManager variableManager, AQLInterpreter interpreter) {
        boolean result = false;
        Optional<EClass> optionalEClass = variableManager.get(VariableManager.SELF, EObject.class).map(EObject::eClass).filter(new DomainClassPredicate(domainType));
        if (optionalEClass.isPresent()) {
            if (preconditionExpression != null && !preconditionExpression.isBlank()) {
                result = interpreter.evaluateExpression(variableManager.getVariables(), preconditionExpression).asBoolean().orElse(false);
            } else {
                result = true;
            }
        }
        return result;
    }

    private String computeGanttLabel(org.eclipse.sirius.components.view.gantt.GanttDescription viewGanttDescription, VariableManager variableManager, AQLInterpreter interpreter) {
        String title = variableManager.get(GanttDescription.LABEL, String.class)
                .orElseGet(() -> this.evaluateString(interpreter, variableManager, viewGanttDescription.getTitleExpression()));
        if (title.isBlank()) {
            return DEFAULT_GANTT_LABEL;
        } else {
            return title;
        }
    }

    private String evaluateString(AQLInterpreter interpreter, VariableManager variableManager, String expression) {
        return interpreter.evaluateExpression(variableManager.getVariables(), expression).asString().orElse("");
    }

    private Optional<Object> self(VariableManager variableManager) {
        return variableManager.get(VariableManager.SELF, Object.class);
    }
}
