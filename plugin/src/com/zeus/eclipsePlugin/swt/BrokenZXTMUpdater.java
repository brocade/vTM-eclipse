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

package com.zeus.eclipsePlugin.swt;

import java.net.URI;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.filesystem.ZXTMFileSystem;
import com.zeus.eclipsePlugin.project.operations.UpdateProjectZXTMConfOp;
import com.zeus.eclipsePlugin.swt.dialogs.BrokenZXTMDialog;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;

/**
 * Class that creates the broken ZXTM dialog, which is used to fix ZXTM projects
 * with no .zxtmConf files. 
 */
public class BrokenZXTMUpdater extends AsyncExec
{
   public static BrokenZXTMUpdater singleton = null;
   
   /**
    * Static method that creates the broken ZXTM dialog.
    * @param project The project that we want to fix.
    */
   public static synchronized void brokenZXTM( IProject project )
   {
      if( singleton == null ) {
         singleton = new BrokenZXTMUpdater();
      }
      
      singleton.start( project );
   }
   
   private BrokenZXTMUpdater() {}
   
   /**
    * Starts the broken ZXTM dialog if there isn't one already running.
    * @param project 
    */
   public void start( IProject project )
   {
      this.startAsync( project );
   }

   /**
    * The main run method. Displays the dialog, waits for it to finish then 
    * performs the selected action.
    */
   /* Override */
   protected void runAsync( Object[] userData )
   {
      IProject project = (IProject) userData[0];
      
      boolean retry = true;
      while( retry ) {
         retry = false;
         
         // Open the dialog
         BrokenZXTMDialog dialog = new BrokenZXTMDialog( ZDialog.getShell(), project );
         
         dialog.setBlockOnOpen( true );
         dialog.open();
         
         // Perform the users chosen operation
         try {         
            switch( dialog.getChoice() ) {
               // If the dialog was closed using the close button (can't seem to
               // disable) re-open the dialog.
               case CLOSED_DIALOG: {
                  ZDebug.print( 5, "Closed Dialog, reopening!" );
                  retry = true;
                  break;
               }
               
               // Remove the project nature, making 
               case GENERAL_PROJECT: {
                  if( project.isAccessible() && project.getDescription() != null ) {
                     synchronized( ZXTMPlugin.getDefault().getProjectManager() ) {
                        IProjectDescription description = project.getDescription();
                        description.setNatureIds( new String[] {} );
                        
                        project.setDescription( description, null );
                        
                        IFolder folder = project.getFolder( ZXTMFileSystem.RULE_PATH );
                        if( folder.exists() ) {
                           URI ruleDirURI = folder.getLocationURI();
                           
                           if( ruleDirURI.getScheme().equals( ZXTMFileSystem.PROTOCOL ) ) { 
                              folder.delete( true, null );
                           }
                        }   
                     }
                  } else {
                     throw new Exception( ZLang.ZL_ProjectWasClosed );
                  }
                  break;
               }
               
               // Close the project
               case CLOSE_PROJECT: {
                  ZXTMPlugin.getDefault().getProjectManager().closeProject( project );
                  break;
               }
               
               // Delete the project
               case DELETE: {
                  synchronized( ZXTMPlugin.getDefault().getProjectManager() ) {
                     project.delete( dialog.getDeleteContents(), true, null );
                  }
                  break;
               }
               
               // Recreate the .zxmtConf file, and attempt to connect to the 
               // configured ZXTM
               case CONFIG: {
                  ZDebug.print( 5, "Creating new ZXTM..." );
                  
                  UpdateProjectZXTMConfOp op =  new UpdateProjectZXTMConfOp(
                     dialog.getHostname(),
                     dialog.getUserName(),
                     dialog.getPassword(),
                     dialog.getPort(),
                     dialog.getStorePassword(),
                     project
                  );
                  
                  SWTUtil.progressDialog( op );
                  break;
               }
            }
                    
         // Something went wrong, report the error.
         } catch( Exception e ) {
            ZDebug.printStackTrace( e, "Fix project failed - ", project );
            ZDialog.showErrorDialog( ZLang.ZL_CouldNotAlterProject, 
               ZUtil.getRootCauseMessage( e ) 
            );            
         }
      }
   }

}
