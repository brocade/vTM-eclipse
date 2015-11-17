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
 * Operation that refreshes a resource in eclipse.
 */
public class RefreshResourceOp extends WorkspaceModifyOperation
{
   private IResource resource;
   private int depth;
   
   /**
    * Setup the resource refresh operation.
    * @param resource The resource to refresh
    * @param depth The depth to refresh it (use the IResource.DEPTH_* constants)
    */
   public RefreshResourceOp( IResource resource, int depth )
   {
      this.resource = resource;
      this.depth = depth;      
   }

   /**
    * Refreshes the resource, throwing an exception if something goes wrong.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.bind( ZLang.ZL_RefreshingResource, resource.getName() ), 1 );
      monitor.subTask( "" );
      
      resource.refreshLocal( depth, monitor );
      
      monitor.worked( 1 );
      monitor.done();
   }

}
