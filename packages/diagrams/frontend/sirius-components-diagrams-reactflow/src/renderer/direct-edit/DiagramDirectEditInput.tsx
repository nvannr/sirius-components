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
import { gql, useMutation, useQuery } from '@apollo/client';
import { useMultiToast } from '@eclipse-sirius/sirius-components-core';
import TextField from '@material-ui/core/TextField';
import { useContext, useEffect, useRef, useState } from 'react';
import { DiagramContext } from '../../contexts/DiagramContext';
import { DiagramContextValue } from '../../contexts/DiagramContext.types';
import {
  DiagramDirectEditInputProps,
  DiagramDirectEditInputState,
  GQLErrorPayload,
  GQLInitialDirectEditElementLabelData,
  GQLInitialDirectEditElementLabelInput,
  GQLRenameElementPayload,
  GQLSuccessPayload,
} from './DiagramDirectEditInput.types';

const initialDirectEditElementLabeQuery = gql`
  query initialDirectEditElementLabel($editingContextId: ID!, $diagramId: ID!, $labelId: ID!) {
    viewer {
      editingContext(editingContextId: $editingContextId) {
        representation(representationId: $diagramId) {
          description {
            ... on DiagramDescription {
              initialDirectEditElementLabel(labelId: $labelId)
            }
          }
        }
      }
    }
  }
`;

export const editLabelMutationOp = gql`
  mutation editLabel($input: EditLabelInput!) {
    editLabel(input: $input) {
      __typename
      ... on EditLabelSuccessPayload {
        diagram {
          id
        }
      }
      ... on ErrorPayload {
        message
      }
    }
  }
`;

const isErrorPayload = (payload: GQLRenameElementPayload): payload is GQLErrorPayload =>
  payload.__typename === 'ErrorPayload';
const isSuccessPayload = (payload: GQLRenameElementPayload): payload is GQLSuccessPayload =>
  payload.__typename === 'SuccessPayload';

export const DiagramDirectEditInput = ({ labelId, editingKey, onClose, transform }: DiagramDirectEditInputProps) => {
  const initialLabel = editingKey === null || editingKey === '' ? '' : editingKey;
  const [state, setState] = useState<DiagramDirectEditInputState>({
    newLabel: initialLabel,
  });

  const { addErrorMessage } = useMultiToast();

  const { editingContextId, diagramId } = useContext<DiagramContextValue>(DiagramContext);

  const textInput = useRef<HTMLInputElement | HTMLTextAreaElement | null>(null);

  const { data: initialLabelItemData, error: initialLabelItemError } = useQuery<
    GQLInitialDirectEditElementLabelData,
    GQLInitialDirectEditElementLabelInput
  >(initialDirectEditElementLabeQuery, {
    variables: {
      editingContextId: editingContextId,
      diagramId: diagramId,
      labelId: labelId,
    },
  });

  const [renameElement, { data: editLabelData, error: editLabelError }] = useMutation(editLabelMutationOp);
  useEffect(() => {
    if (editLabelError) {
      addErrorMessage('An unexpected error has occurred, please refresh the page');
    }
    if (editLabelData) {
      const { editLabel } = editLabelData;
      if (isErrorPayload(editLabel)) {
        addErrorMessage('An unexpected error has occurred, please refresh the page');
      } else if (isSuccessPayload(editLabel)) {
        if (editLabel.__typename === 'SuccessPayload') {
          onClose();
        }
      }
    }
  }, [editLabelData, editLabelError]);

  useEffect(() => {
    let cleanup: (() => void) | undefined = undefined;
    if (initialLabelItemError) {
      addErrorMessage('An unexpected error has occurred, please refresh the page');
    }
    if (initialLabelItemData?.viewer.editingContext.representation.description.initialDirectEditElementLabel) {
      const initialLabel =
        initialLabelItemData?.viewer.editingContext.representation.description.initialDirectEditElementLabel;
      if (!editingKey) {
        setState((prevState) => {
          return { ...prevState, newLabel: initialLabel };
        });
        const timeOutId = setTimeout(() => {
          textInput.current?.select();
        }, 0);
        cleanup = () => clearTimeout(timeOutId);
      }
    }
    return cleanup;
  }, [initialLabelItemError, initialLabelItemData]);

  const doRename = () => {
    const isNameValid = state.newLabel.length >= 1;
    if (isNameValid) {
      renameElement({
        variables: {
          input: {
            id: crypto.randomUUID(),
            editingContextId: editingContextId,
            representationId: diagramId,
            labelId: labelId,
            newText: state.newLabel,
          },
        },
      });
    }
    onClose();
  };

  const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const newLabel = event.target.value;

    setState((prevState) => {
      return { ...prevState, newLabel: newLabel };
    });
  };

  const onFinishEditing = (event: React.KeyboardEvent<HTMLDivElement>) => {
    const { key } = event;
    if (key === 'Enter') {
      doRename();
    } else if (key === 'Escape') {
      onClose();
    }
  };
  return (
    <>
      <TextField
        name="name"
        size="small"
        inputRef={textInput}
        placeholder={'Enter the new name'}
        value={state.newLabel}
        onChange={handleChange}
        onKeyDown={onFinishEditing}
        onBlur={doRename}
        autoFocus
        data-testid="name-edit"
        style={{ transform }}
      />
    </>
  );
};
