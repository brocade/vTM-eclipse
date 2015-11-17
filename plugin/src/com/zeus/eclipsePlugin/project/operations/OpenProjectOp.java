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
 * Operation that opens a project.
 */
public class OpenProjectOp extends WorkspaceModifyOperation
{
   private IProject project;
   
   /**
    * Setup the operation with the specified project.
    * @param project The project to open.
    */
   public OpenProjectOp( IProject project )
   {
      this.project = project;   
   }

   /**
    * Opens the project. May throw an exception is somthing goes wrong.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.bind( ZLang.ZL_OpeningProject, project.getName() ), 1 );
      
      project.open( monitor );
      
      monitor.done();
   }

}
