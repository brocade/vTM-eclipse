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
import java.net.URI;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.swt.EmptyMonitor;

/**
 * Workspace operation that creates a linked folder (such as the folder that 
 * stores the rules)
 */
public class CreateLinkedFolderOp extends WorkspaceModifyOperation
{
   private IFolder folder;
   private URI linkURI;
   
   /**
    * Setup the operation with the target folder and its URI.
    * @param folder The folder to create, should not currently exist.
    * @param linkURI The URI the folder should link to.
    */
   public CreateLinkedFolderOp( IFolder folder, URI linkURI )
   {
      this.folder = folder;
      this.linkURI = linkURI;
   }

   /**
    * Creates the linked folder. May throw an exception if the link cannot be 
    * created.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.bind( ZLang.ZL_CreatingFolderLink, folder.getName() ), 1 );
      
      monitor.subTask( "" );
      folder.createLink( 
         linkURI, 
         IResource.ALLOW_MISSING_LOCAL | IResource.BACKGROUND_REFRESH | IResource.REPLACE , 
         null 
      );
      monitor.worked( 1 );
      
      monitor.done();
   }

}
