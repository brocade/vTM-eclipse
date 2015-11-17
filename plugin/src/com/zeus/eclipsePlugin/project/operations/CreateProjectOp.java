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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Ids;
import com.zeus.eclipsePlugin.filesystem.ZXTMFileSystem;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.ProjectProperties;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.swt.EmptyMonitor;

/**
 * This operation creates a new project, sets it up and adds a ZXTM to the 
 * model. 
 */
public class CreateProjectOp extends WorkspaceModifyOperation
{
   private String name;
   private String hostname; 
   private int port;
   private String  user, password;
   private boolean storePW;
   private String path;
   private boolean customLocation;
   
   /**
    * Setup the operation, requires all the details of the new ZXTM and the new
    * project. 
    * @param name The name of the project
    * @param hostname The hostname / IP address of the ZXTM
    * @param port The admin port of the ZXTM
    * @param username The user to access ZXTM with.
    * @param password The password to access the ZXTM
    * @param storePW Should the password be stored locally?
    * @param customLocation Should the project be stored in a custom location?
    * @param path The path to store the project.
    */
   public CreateProjectOp( String name, String hostname, int port,
      String username, String password, boolean storePW, boolean customLocation, String path )
   {
      this.name = name;
      this.hostname = hostname;
      this.port = port;
      this.user = username;
      this.password = password;
      this.storePW = storePW;
      this.customLocation = customLocation;
      this.path = path;
   }

   /**
    * Create the new ZXTM project. First checks for ZXTM duplicates, then 
    * tries creating an connecting to the ZXTM. If this succeeds it creates the
    * project and sets up the Rules directory.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.ZL_CreatingNewZXTMProject, 10 );
      
      // Check this ZXTM isn't added twice
      monitor.subTask( ZLang.ZL_CheckingForDuplicateZXTMs );
      ModelController model =  ZXTMPlugin.getDefault().getModelController();
      ZXTM duplicateZXTM = model.getZXTMFull( hostname, port );
      if( duplicateZXTM != null) {
         IProject duplicateProject = ZXTMProject.getProjectForZXTM( duplicateZXTM );
            
         throw new WorkbenchException( 
            ZLang.bind( ZLang.ZL_ZXTMAlreadyExistsInProject, duplicateProject.getName() )
         );        
      }
      monitor.worked( 1 );
      
      if( monitor.isCanceled() ) return;
      
      synchronized( ZXTMPlugin.getDefault().getProjectManager() ) 
      {
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         IWorkspaceRoot root = workspace.getRoot();
         
         // Adding ZXTM to model
         monitor.subTask( ZLang.bind( ZLang.ZL_ConnectingToZXTMFoo, hostname + ":" + port ) ); 
         
         ZXTM zxtm;
         try {
            zxtm = model.addZXTM( hostname, port, user, password );
            zxtm.setStorePassword( storePW );
         } catch( Exception e ) {
            throw new WorkbenchException( 
               ZLang.bind( ZLang.ZL_CouldNotCreateZXTMProject, e.getMessage() ), e
            );
         }
         
         monitor.worked( 1 );
         
         if( monitor.isCanceled() ) {
            model.removeZXTM( zxtm );
            return;
         }
         
         // Creating project
         monitor.subTask( ZLang.ZL_CreatingProject );
         
         IProject project = root.getProject( name );           
         
         IProjectDescription description = workspace.newProjectDescription( name );
         description.setNatureIds( new String[] { Ids.ZXTM_PROJECT_NATURE } );
         
         if( customLocation ) {
            description.setLocation( Path.fromOSString( path ) );
         }

         project.create( description, monitor );
         project.open( monitor );
         
         monitor.worked( 1 );
         
         if( monitor.isCanceled() ) {
            model.removeZXTM( zxtm );
            project.delete( true, true, monitor );
            return;
         }
      
         monitor.subTask( ZLang.ZL_SavingProperties );
         
         // Save properties file
         ProjectProperties properties = ZXTMProject.getProjectProperties( project );    
         properties.loadZXTMData( zxtm );
         properties.saveProperties();         
         monitor.worked( 1 );
         
         if( monitor.isCanceled() ) {
            model.removeZXTM( zxtm );
            project.delete( true, true, monitor );
            return;
         }
         
         monitor.subTask( ZLang.ZL_CreatingRulesFolder );
         
         // Create linked folder
         try {
            IFolder rulesFolder = project.getFolder( ZXTMFileSystem.RULE_PATH );
            
            rulesFolder.createLink(
               ZXTMFileSystem.getRuleDirForZXTM( zxtm ), 
               IResource.ALLOW_MISSING_LOCAL | IResource.BACKGROUND_REFRESH | IResource.REPLACE , 
               monitor 
            );
         } catch( CoreException e ) {
            model.removeZXTM( zxtm );
            project.delete( true, true, monitor );
            throw e;
         }
         
         monitor.worked( 1 );
         
         if( monitor.isCanceled() ) {
            model.removeZXTM( zxtm );
            project.delete( true, true, monitor );
            return;
         }
     
         project.refreshLocal( IResource.DEPTH_INFINITE, monitor );
         
         monitor.done();
      }
   }
   
}
