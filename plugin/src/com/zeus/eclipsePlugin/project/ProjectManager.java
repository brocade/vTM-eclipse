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

package com.zeus.eclipsePlugin.project;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import com.zeus.eclipsePlugin.BackgroundThread;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Ids;
import com.zeus.eclipsePlugin.filesystem.ZXTMFileSystem;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.operations.CloseProjectOp;
import com.zeus.eclipsePlugin.project.operations.CreateLinkedFolderOp;
import com.zeus.eclipsePlugin.project.operations.DeleteResourceOp;
import com.zeus.eclipsePlugin.project.operations.OpenProjectOp;
import com.zeus.eclipsePlugin.project.operations.RefreshResourceOp;
import com.zeus.eclipsePlugin.swt.BrokenZXTMUpdater;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * This class maintains the link between eclipse and the model. It ensures ZXTM 
 * projects are loaded into the model, and that they have all the needed files.
 * 
 * IMPORTANT: Any operations that modify the model (closing projects so they get
 * disconnected), MUST be done using this class. If you add any methods they must 
 * be synchronised to ensure the manager does not overwrite your changes.
 */
public class ProjectManager extends BackgroundThread
{   
   
   /**
    * Create the project manager. Sets the thread label.
    */
   public ProjectManager()
   {
      super( "Project Manager" );
      setPause( 5000 );
   }
   
   /**
    * This runs the auto-update thread. It makes sure that the ZXTM projects
    * reflect what's currently in the model.
    */
   /* Override */
   public void run()
   {     
      while( !ZXTMPlugin.isEclipseLoaded() ) {
         return;
      }

      update( false );
   }
   
   /**
    * Stop the project update thread. This blocks until its finished.
    */
   public void stopChecker() {
      stop();
   }
   
   /**
    * Check all the projects in the current workspace. If they are ZXTM check 
    * that they are in the model and are valid.
    */
   public synchronized void update( boolean saveProperties )
   {
      ZDebug.print( 4, "ProjectChecker got ProjectChecker lock" );
      
      if( !ZXTMPlugin.isEclipseLoaded() ) return;
      
      IWorkspaceRoot root =  ResourcesPlugin.getWorkspace().getRoot();
      IProject[] projects = root.getProjects();
      HashMap<String,ZXTM> seenZXTMs = new HashMap<String,ZXTM>();
       
      for( IProject project : projects ) {
         ZDebug.print( 3, "Checking project: ", project.getName()  );
         try {
            // Check if this is a ZXTM project
            if( project.isAccessible() && !project.getDescription().hasNature( Ids.ZXTM_PROJECT_NATURE ) ) { 
               ZDebug.print( 4, " ", project.getName(), " is not a ZXTM project"  );
               continue;
            }
            
            ZXTM zxtm = ZXTMProject.getZXTMForProject( project );
            if( zxtm == null ) {
               ZDebug.print( 4, "Could not find ZXTM for project: ", project );
               
               // Try fix a borked project
               if( project.isAccessible() && project.getDescription().hasNature( Ids.ZXTM_PROJECT_NATURE ) &&
                  ZXTMPlugin.isEclipseLoaded() ) 
               {
                  BrokenZXTMUpdater.brokenZXTM( project );              
               }
               continue; // Not a full ZXTM project            
            }
            
            seenZXTMs.put( zxtm.getNamePort(), zxtm );
            
            // If the project is closed, disconnect the ZXTM
            ZDebug.print( 4, "Setting disconnected for zxtm '", zxtm, "': ", !project.isOpen() );
            zxtm.setDisconnected( !project.isOpen() );
            
            // We can't do folder linking if the project isn't open or we don't
            // have ZXTM info
            if( !project.isAccessible() || zxtm == null ) continue;
            
            // Check project properties exist and are correct
            ProjectProperties properties = ZXTMProject.getProjectProperties( project );

            // Reload properties every 120 seconds (this ensures that if the user
            // deletes the config it gets restored)
            if( saveProperties ||
               properties.getLastLoaded() < System.currentTimeMillis() - 120 * 1000 ) 
            {
               ZDebug.print( 5, "Reloading properties for ", zxtm );
               properties.loadProperties();
            }
            
            if( properties.loadZXTMData( zxtm ) ) {
               ZDebug.print( 5, "Saving properties for ", zxtm, " due to change" );
               properties.saveProperties();
            }
            
            // Check the rule folder exists, if not recreate it.
            IFolder folder = project.getFolder( ZXTMFileSystem.RULE_PATH );
            URI folderURI = ZXTMFileSystem.getRuleDirForZXTM( zxtm );
            
            ZDebug.print( 4, "Rules Folder URI: ", folder.getLocationURI() );
            
            if( folder.exists() && ( !folder.isLinked() || !folder.getLocationURI().equals( folderURI ) ) ) {
               ZDebug.print( 4, project.getName(), " rule folder is invalid, deleting..."  );
               SWTUtil.progressBackground( new DeleteResourceOp( folder, true ), true );
            }
            
            if( !folder.exists() ) {
               ZDebug.print( 4, project.getName(), " does not have a Rules folder, recreating."  );
               
               SWTUtil.progressBackground( new CreateLinkedFolderOp( 
                  folder, ZXTMFileSystem.getRuleDirForZXTM( zxtm ) 
               ), true );
               
               SWTUtil.progressBackground( new RefreshResourceOp( folder, IResource.DEPTH_ONE ), true );
                              
               continue;
            } 

            /*for( IResource file : folder. ) {
               Debug.print( 5, "Checking rule file: " + file  );
            }*/
            
            
         } catch( Throwable e ) {
            ZDebug.printStackTrace( e, "Project update failed for ", project );
         }
      }
     
      ModelController model = ZXTMPlugin.getDefault().getModelController();   
      ZXTM[] zxtms = model.getSortedZXTMs();
      
      for( ZXTM zxtm : zxtms ) {
         if( seenZXTMs.get( zxtm.getNamePort() ) == null ) {
            // Project no longer exists, remove ZXTM from the model
            model.removeZXTM( zxtm );
         }
      }
      ZDebug.print( 4, "ProjectChecker returned ProjectChecker lock" );  
   }
   
   /**
    * Close a ZXTM project. This will disconnect the ZXTM as well.
    * @param project The project to close.
    */
   public void closeProject( IProject project )
   {
      closeProject( project, ZXTMProject.getZXTMForProject( project ) );
   }
   
   /**
    * Close the ZXTM project that this ZXTM is stored in. This will disconnect 
    * the ZXTM in the model as well.
    * @param zxtm The ZXTM who's project we will close.
    */
   public void closeProjectForZXTM( ZXTM zxtm )
   {
      closeProject( ZXTMProject.getProjectForZXTM( zxtm ), zxtm );
   }
   
   /**
    * Close a ZXTM Project, and disconnect the linked ZXTM.
    * @param project The project to close.
    * @param zxtm The ZXTM linked to this project.
    */
   public synchronized void closeProject( IProject project, ZXTM zxtm )
   {
      if( zxtm == null ) {
         ZDebug.dumpStackTrace( "ZXTM was null for close project call." );
         return;
      }
      try {
         if( project.isOpen() ) {
            SWTUtil.progressBackground( new CloseProjectOp( project ), true );
         }
      } catch( InvocationTargetException e ) {
         ZDebug.printStackTrace( e, "Close project failed for ", zxtm, " - ", project );
      }
      
      zxtm.setDisconnected( true );
   }
   
   /**
    * Close a ZXTM project. This will reconnect the ZXTM as well.
    * @param project The project to open.
    */
   public void openProject( IProject project )
   {
      openProject( project, ZXTMProject.getZXTMForProject( project ) );
   }
   
   /**
    * Open the ZXTM project that this ZXTM is stored in. This will reconnect 
    * the ZXTM in the model as well.
    * @param zxtm The ZXTM who's project we will open.
    */
   public void openProjectForZXTM( ZXTM zxtm )
   {
      openProject( ZXTMProject.getProjectForZXTM( zxtm ), zxtm );
   }
   
   /**
    * Open a ZXTM Project, and reconnect the linked ZXTM.
    * @param project The project to open.
    * @param zxtm The ZXTM linked to this project. Can be null.
    */
   public synchronized void openProject( IProject project, ZXTM zxtm )
   {
      try {
         if( !project.isOpen() ) {
            SWTUtil.progressBackground( new OpenProjectOp( project ), true );
         }
      } catch( InvocationTargetException e ) {
         ZDebug.printStackTrace( e, "Open project failed for ", zxtm, " - ", project );
      }
      
      if( zxtm != null ) {
         zxtm.setDisconnected( false );
      }
   }
   
   
}
