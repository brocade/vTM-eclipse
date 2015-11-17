/*******************************************************************************
 * Copyright (C) 2015 Brocade Communications Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://github.com/brocade/vTM-eclipse/LICENSE
 * This software is distributed "AS IS".
 *
 * Contributors:
 *     Brocade Communications Systems - Main Implementation
 ******************************************************************************/

package com.zeus.eclipsePlugin.project.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.swt.EmptyMonitor;

/**
 * Workspace operation that deletes a project.
 */
public class DeleteProjectsOp extends WorkspaceModifyOperation
{
   private IProject[] projects;
   private boolean contents;
   
   /**
    * Setup the delete project operation.
    * @param project The project to delete
    * @param contents Set this to true to delete the project on disk
    */
   public DeleteProjectsOp( IProject[] projects, boolean contents )
   {
      this.projects = projects;   
      this.contents = contents;
   }

   /**
    * Deletes the project and its linked ZXTM. Might throw an exception 
    * if it fails.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.ZL_DeletingProjects, projects.length );
      
      for( IProject project : projects )
      {
         monitor.subTask( project.getName() );
         ZXTM zxtm = ZXTMProject.getZXTMForProject( project );
         ModelController model = ZXTMPlugin.getDefault().getModelController();
         
         synchronized( ZXTMPlugin.getDefault().getProjectManager() ) {            
            project.delete( contents, false, monitor );
            if( zxtm != null ) model.removeZXTM( zxtm );
         }
         
         monitor.worked( 1 );      
      }

      monitor.done();
   }

}
