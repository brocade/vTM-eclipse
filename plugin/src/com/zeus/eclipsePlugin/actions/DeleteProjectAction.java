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

package com.zeus.eclipsePlugin.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;

import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.consts.Command;
import com.zeus.eclipsePlugin.consts.ImageFile;
import com.zeus.eclipsePlugin.model.ModelSelection;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.project.operations.DeleteProjectsOp;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.dialogs.ToggleConfirmResult;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;
import com.zeus.eclipsePlugin.zxtmview.ZXTMViewer;

/**
 * This action deletes one or more ZXTM projects.
 */
public class DeleteProjectAction extends ZAction
{
   private ModelSelection selection = null;
   
   /** 
    * Standard constructor. The action will find the selection from the ZXTM
    * Viewer when it is activated. 
    */
   public DeleteProjectAction() {}
   
   /**
    * Constructor that stores a selection. This is used by the action every time
    * is is run. This also sets up the text and image of the Action.
    * @param selection The selection to use every time this action is run.
    */
   public DeleteProjectAction( ModelSelection selection )
   {
      if( selection != null && selection.isOnlyZXTMs()  ) {
         if( selection.getSize() == 1 ) {
            this.setText( ZLang.ZL_RemoveZXTMProject );  
         } else {
            this.setText( ZLang.ZL_RemoveZXTMProjects );  
         }
         this.setImageFile( ImageFile.DELETE );
         
         this.selection = selection;
                  
      } else {
         // Should not create without a proper selection
         System.err.println( "Delete project action created without proper selection." );
         Thread.dumpStack(); 
         this.setText( "!!" );
      }
   }
   
   /**
    * There is no related command with this action (this is a toggle command)
    */
   /* Override */
   protected Command getCommand()
   {
      return null;
   }

   /**
    * Run the command, deleting the projects via a workspace operation.
    */
   /* Override */
   public void run( ExecutionEvent event )
   {
      ModelSelection currentSelection = selection;
      
      // No selection, check if the ZXTM Viewer has one currently.
      if( currentSelection == null ) {
         currentSelection = ZXTMViewer.getSelectionForOpenViewer();         
      }
      
      if( currentSelection == null || !currentSelection.isOnlyZXTMs() ) {
         return;
      }
      
      String message;
      if( currentSelection.size() == 1 ) {
         ZXTM zxtm = currentSelection.firstZXTM();
         IProject project = ZXTMProject.getProjectForZXTM( zxtm );
         
         message = ZLang.bind( ZLang.ZL_ConfirmDeleteSingleProject, project.getName(), zxtm.toString() );
      } else {
         message = ZLang.bind( ZLang.ZL_ConfirmDeleteMultipleProjects, currentSelection.getSize() );
      }
      
      ToggleConfirmResult result = ZDialog.showConfirmToggleDialog( 
         ZLang.ZL_ConfirmDeleteTitle, message, 
         ZLang.ZL_DeleteProjectContents, false 
      );
      
      if( result.cancelSelected() ) return;
      
      IProject[] projects = new IProject[selection.getSize()];
      int i = 0;
      for( ZXTM zxtm : currentSelection.getSelectedZXTMs() ) {
         projects[i++] = ZXTMProject.getProjectForZXTM( zxtm );
      }
      
      DeleteProjectsOp op = new DeleteProjectsOp( projects, result.getToggleValue() );
      
      try {
         SWTUtil.progressDialog( op );
      } catch( InvocationTargetException e ) {
         ZDialog.showErrorDialog( ZLang.ZL_DeletingProjectsFailedTitle,
            ZLang.bind( ZLang.ZL_DeletingProjectsFailedMessage, 
               ZUtil.getRootCauseMessage( e )
            )
         );
      }
   }
}
