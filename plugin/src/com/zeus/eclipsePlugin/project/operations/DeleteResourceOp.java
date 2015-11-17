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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.swt.EmptyMonitor;

/**
 * Workspace operation that deletes a resource.
 */
public class DeleteResourceOp extends WorkspaceModifyOperation
{
   private IResource resource;
   private boolean force;
   
   /**
    * Setup the Delete operation.
    * @param resource The resource to delete
    * @param force Set this to true to override read-only settings etc.
    */
   public DeleteResourceOp( IResource resource, boolean force )
   {
      this.resource = resource;   
      this.force = force;
   }

   /**
    * Deletes the resource. Might throw an exception if it fails.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.bind( ZLang.ZL_DeletingResource, resource.getName() ), 1 );
      monitor.subTask( "" );
      
      resource.delete( force, monitor );
      
      monitor.worked( 1 );      
      monitor.done();
   }

}
