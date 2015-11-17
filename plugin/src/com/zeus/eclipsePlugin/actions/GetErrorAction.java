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

import org.eclipse.core.commands.ExecutionEvent;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.consts.Command;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.ModelSelection;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;
import com.zeus.eclipsePlugin.zxtmview.ZXTMViewer;

/**
 * This action displays the exception dialog. It shows the problems the 
 * currently selected rule has.
 */
public class GetErrorAction extends ZAction
{
   private ModelElement element = null;
   
   /** 
    * Standard constructor. The action will find the selection from the ZXTM
    * Viewer when it is activated. 
    */
   public GetErrorAction() {}
   
   /**
    * Constructor that stores a selection. This is used by the action every time
    * is is run. This also sets up the text and image of the Action.
    * @param selection The selection to use every time this action is run.
    */
   public GetErrorAction( ModelSelection selection )
   {
      
      if( selection != null && selection.getSize() == 1 ) {
         this.setText( ZLang.ZL_GetErrorInfo );  
         element = (ModelElement) selection.getFirstElement();
         
      } else {
         // Should not use this constructor without a valid selection
         ZDebug.dumpStackTrace( "GetErrorAction created without proper selection." ); 
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
    * Run the action, showing a dialog with details on the CANNOT_SYNC error of
    * the selected model object.
    */
   /* Override */
   public void run( ExecutionEvent event )
   {
      ModelElement currentElement = element;
      
      // No selection, check if the ZXTM Viewer has one currently.
      if( currentElement == null ) {
         ModelSelection currentSelection = ZXTMViewer.getSelectionForOpenViewer();
         if( currentSelection == null || currentSelection.getSize() != 1 ) {
            return;
         }
         currentElement = (ModelElement) currentSelection.getFirstElement();
      }
      
      if( currentElement == null || currentElement.getLastError() == null ) {
         return;
      }
      
      // Display the error dialog
      ZDialog.showExceptionDialog(
         ZLang.bind( ZLang.ZL_GetErrorTitle, element ), 
         ZLang.bind( ZLang.ZL_GetErrorMessage, element ), 
         currentElement.getLastError()
      );
   }
}
