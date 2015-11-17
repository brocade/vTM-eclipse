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
import com.zeus.eclipsePlugin.swt.EmptyMonitor;

/**
 * Workspace operation that closes the specified project.
 */
public class CloseProjectOp extends WorkspaceModifyOperation
{
   private IProject project;
   
   /**
    * Setup the close project operation.
    * @param project The project that needs to be closed.
    */
   public CloseProjectOp( IProject project )
   {
      this.project = project;   
   }

   /**
    * Closes the project. May throw an exception if the close operation fails.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.bind( ZLang.ZL_ClosingProject, project.getName() ), 1 );
      
      monitor.subTask( "" );      
      project.close( monitor );
      monitor.worked( 1 );
      
      monitor.done();
   }

}
