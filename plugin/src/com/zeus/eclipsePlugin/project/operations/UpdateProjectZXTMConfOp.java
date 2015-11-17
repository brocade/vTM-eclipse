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
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.ProjectProperties;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.swt.EmptyMonitor;

/**
 * This operation alters a ZXTM projects settings. It creates a new ZXTM with
 * the new settings and removes the old one.
 */
public class UpdateProjectZXTMConfOp extends WorkspaceModifyOperation
{
   private String hostname, user, password;
   private int port;
   private boolean storePW;
   private IProject project;
   
   /**
    * Setup the operation with the new values.
    * @param hostname The new hostname / IP address
    * @param user The user to login with
    * @param password The new password
    * @param port The new port
    * @param storePW Should the password be stored locally?
    * @param project The project which is being changed.
    */
   public UpdateProjectZXTMConfOp( String hostname, String user, String password, int port,
      boolean storePW, IProject project )
   {
      super( project );
      this.hostname = hostname;
      this.password = password;
      this.user = user;
      this.port = port;
      this.storePW = storePW;
      this.project = project;
   }
   
   /**
    * Runs the operation. First checks that the ZXTM doesn't already exist, then
    * attempts to create a ZXTM with the new settings. If this is all OK it 
    * updates the project settings.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.ZL_UpdateOpUpdatingProject, 4 );
      
      // Check this ZXTM isn't added twice      
      monitor.subTask( ZLang.ZL_CheckingForDuplicateZXTMs );      
      ModelController model =  ZXTMPlugin.getDefault().getModelController();
      ZXTM duplicateZXTM = model.getZXTMFull( hostname, port );
      if( duplicateZXTM != null) {
         IProject duplicateProject = ZXTMProject.getProjectForZXTM( duplicateZXTM );
            
         throw new WorkbenchException( 
            ZLang.bind( ZLang.ZL_ZXTMAlreadyExistsInProject, 
               duplicateProject.getName()
            )
         );        
      }
      
      monitor.worked( 1 );
      
      if( monitor.isCanceled() ) return;
      
      try {   
         synchronized( ZXTMPlugin.getDefault().getProjectManager() ) {
            ProjectProperties properties = ZXTMProject.getProjectProperties( project );       
            
            // Create the new ZXTM
            ZDebug.print( 5, "Adding the ZXTM" ); 
            monitor.subTask( ZLang.ZL_UpdateOpConnectingToZXTM );
            ZXTM newZXTM = model.addZXTM( hostname, port, user, password ); 
            newZXTM.setStorePassword( storePW );
            monitor.worked( 1 );
            
            // Update project properties
            ZDebug.print( 5, "Added the ZXTM, setting properies." ); 
            monitor.subTask( ZLang.ZL_UpdateOpSavingProperties );
            properties.loadZXTMData( newZXTM );
            
            ZDebug.print( 5, "Saving properies." );
            if( !properties.saveProperties() ) {
               throw new Exception( ZLang.ZL_UpdateOpPropertiesSaveFailed );
            }
            monitor.worked( 1 );
            
            // Re-sync the workspace, this will delete the old ZXTM
            monitor.subTask( ZLang.ZL_UpdateOpResyncingWorkspace );
            ZXTMPlugin.getDefault().getProjectManager().update( false );
            monitor.worked( 1 );
         }
      } catch( Exception e ) {
         throw new WorkbenchException( e.getMessage(), e );
      } finally {
         monitor.done();
      }
   }

}
