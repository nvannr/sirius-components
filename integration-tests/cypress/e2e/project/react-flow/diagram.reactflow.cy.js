/*******************************************************************************
 * Copyright (c) 2023 Obeo.
 * This program and the accompanying materials
 * are made available under the erms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
describe('/projects/:projectId/edit - Diagram', () => {
  beforeEach(() => {
    cy.deleteAllProjects();
    cy.createProject('Cypress Project').then((res) => {
      const projectId = res.body.data.createProject.project.id;
      const robot_flow_id = 'c26b6086-b444-3ee6-b8cd-9a4fde5956a7';
      cy.createDocument(projectId, robot_flow_id, 'robot').then(() => {
        cy.visit(`/projects/${projectId}/edit`);
      });
    });
  });

  it('shows a Topography diagram when clicked in the explorer', () => {
    cy.getByTestId('robot').dblclick();
    cy.getByTestId('Robot').dblclick();
    cy.getByTestId('Robot-more').click();

    cy.getByTestId('treeitem-contextmenu').findByTestId('new-representation').click();

    cy.getByTestId('name').clear();
    cy.getByTestId('name').type('Topography1__REACT_FLOW');
    cy.getByTestId('representationDescription').click();
    cy.getByTestId('Topography with auto layout').click();
    cy.getByTestId('create-representation').click();

    cy.getByTestId('rf__wrapper').should('exist');
    cy.get('.react-flow__edgelabel-renderer').children().should('have.length', 7);
    cy.get('.react-flow__nodes').children().should('have.length', 14);
    cy.get('.react-flow__node-rectangularNode').should('have.length', 2);
    cy.get('.react-flow__node-imageNode').should('have.length', 10);
  });

  it('can share the representation', () => {
    cy.getByTestId('share').should('not.exist');

    cy.getByTestId('robot').dblclick();
    cy.getByTestId('Robot').dblclick();
    cy.getByTestId('Robot-more').click();
    cy.getByTestId('treeitem-contextmenu').findByTestId('new-representation').click();

    cy.getByTestId('name').clear();
    cy.getByTestId('name').type('Topography1__REACT_FLOW');
    cy.getByTestId('representationDescription').click();
    cy.getByTestId('Topography with auto layout').click();
    cy.getByTestId('create-representation').click();

    cy.getByTestId('share').should('be.enabled');
    cy.getByTestId('rf__wrapper').should('exist');

    cy.getByTestId('share').click();
    cy.url().then(($url) => {
      cy.task('getClipboard').should('eq', $url);
    });
  });

  it('can switch between representations', () => {
    cy.getByTestId('robot').dblclick();
    cy.getByTestId('Robot').dblclick();
    cy.getByTestId('Robot-more').click();

    cy.getByTestId('treeitem-contextmenu').findByTestId('new-representation').click();

    cy.getByTestId('name').clear();
    cy.getByTestId('name').type('Topography1__REACT_FLOW');
    cy.getByTestId('create-representation').click();

    cy.getByTestId('Robot-more').click();
    cy.getByTestId('treeitem-contextmenu').findByTestId('new-representation').click();

    cy.getByTestId('name').clear();
    cy.getByTestId('name').type('Topography2__REACT_FLOW');
    cy.getByTestId('create-representation').click();

    cy.getByTestId('Topography1__REACT_FLOW').click();

    cy.getByTestId('representation-tab-Topography1__REACT_FLOW').should('have.attr', 'data-testselected', 'true');
    cy.getByTestId('representation-tab-Topography2__REACT_FLOW').should('have.attr', 'data-testselected', 'false');

    cy.getByTestId('Topography2__REACT_FLOW').click();
    cy.getByTestId('representation-tab-Topography1__REACT_FLOW').should('have.attr', 'data-testselected', 'false');
    cy.getByTestId('representation-tab-Topography2__REACT_FLOW').should('have.attr', 'data-testselected', 'true');

    cy.getByTestId('representation-tab-Topography1__REACT_FLOW').click();

    cy.getByTestId('representation-tab-Topography1__REACT_FLOW').should('have.attr', 'data-testselected', 'true');
    cy.getByTestId('representation-tab-Topography2__REACT_FLOW').should('have.attr', 'data-testselected', 'false');
  });
});
