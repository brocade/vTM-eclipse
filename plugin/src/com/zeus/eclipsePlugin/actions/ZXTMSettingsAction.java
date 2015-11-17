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
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.consts.Command;
import com.zeus.eclipsePlugin.consts.Ids;
import com.zeus.eclipsePlugin.model.ModelSelection;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.zxtmview.ZXTMViewer;

/**
 * This action displays the project properties for the selected ZXTM, on the ZXTM Settings
 * page.
 */
public class ZXTMSettingsAction extends ZAction
{
   private ZXTM zxtm = null;
   
   /** 
    * Standard constructor. The action will find the selection from the ZXTM
    * Viewer when it is activated. 
    */
   public ZXTMSettingsAction() {}
   
   /**
    * Constructor that stores a selection. This is used by the action every time
    * is is run. This also sets up the text and image of the Action.
    * @param selection The selection to use every time this action is run.
    */
   public ZXTMSettingsAction( ModelSelection selection )
   {
      if( selection != null && selection.isOnlyOneZXTM()  ) {
         this.setText( ZLang.ZL_EditSettings );  
         this.zxtm = selection.firstZXTM();
                  
      } else {
         // Should not create without a proper selection
         ZDebug.dumpStackTrace( "Settings created without proper selection." ); 
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
    * Run the command, displaying a new project properties dialog on the ZXTM
    * settings page.
    */
   /* Override */
   public void run( ExecutionEvent event )
   {
      ZXTM currentZXTM = zxtm;
      
      // No selection, check if the ZXTM Viewer has one currently.
      if( currentZXTM == null ) {
         ModelSelection currentSelection = ZXTMViewer.getSelectionForOpenViewer();
         if( currentSelection != null && currentSelection.isOnlyOneZXTM() ) {
            currentZXTM = currentSelection.firstZXTM();
         }
      }
      
      if( currentZXTM == null ) {
         return;
      }
      
      // Display the settings page.
      IProject project = ZXTMProject.getProjectForZXTM( currentZXTM );       
      PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn( 
         Display.getCurrent().getActiveShell(), project, Ids.RES_PREFS_ZXTM, 
         null, null 
      );
      
      dialog.open();
   }
}
