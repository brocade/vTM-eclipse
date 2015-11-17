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

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.consts.Command;
import com.zeus.eclipsePlugin.consts.ImageFile;
import com.zeus.eclipsePlugin.model.ModelSelection;
import com.zeus.eclipsePlugin.project.operations.DeleteRulesOp;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;
import com.zeus.eclipsePlugin.zxtmview.ZXTMViewer;

/**
 * This action copies the selection to memory (not the clip-board) and deletes 
 * the rules.
 */
public class CutRuleAction extends ZAction
{
   private ModelSelection selection = null;
   
   /** 
    * Standard constructor. The action will find the selection from the ZXTM
    * Viewer when it is activated. 
    */
   public CutRuleAction() {}
   
   /**
    * Constructor that stores a selection. This is used by the action every time
    * is is run. This also sets up the text and image of the Action.
    * @param selection The selection to use every time this action is run.
    */
   public CutRuleAction( ModelSelection selection )
   {
      if( selection != null && selection.isOnlyRules() && selection.size() > 0  ) {
         this.selection = selection;
         
         this.setText( ZLang.ZL_Cut );
         this.setImageFile( ImageFile.CUT );
         
      } else {
      // Should not use this constructor without a valid selection
         ZDebug.dumpStackTrace( "CopyRuleAction created without proper selection." ); 
         this.setText( "!!" );
      }
   }
   
   /**
    * Get the command we are using (in this case the standard cut command)
    */
   /* Override */
   protected Command getCommand()
   {
      return Command.EDIT_CUT;
   }

   /**
    * Runs the command copying the selection to memory and deleting the selected
    * rules. The deletion is done using the DeleteRulesOp and uses a progress 
    * dialog.
    */
   /* Override */
   protected void run( ExecutionEvent event )
   {
      ModelSelection currentSelection = selection;
      
      // No selection, check if the ZXTM Viewer has one currently.
      if( currentSelection == null ) {
         currentSelection = ZXTMViewer.getSelectionForOpenViewer();
      }
      
      if( currentSelection != null && currentSelection.isOnlyRules() && currentSelection.size() > 0  ) {
         ZXTMViewer.copyRules( currentSelection );
         
         // Run a delete rules operation, using a progress dialog
         DeleteRulesOp op = new DeleteRulesOp( currentSelection.getSelectedRules() );
         
         try {
            SWTUtil.progressDialog( op );
         } catch( InvocationTargetException e ) {
            ZDialog.showErrorDialog( ZLang.ZL_DeletingRulesFailed,
               ZLang.bind( ZLang.ZL_CouldNotDeleteRules, ZUtil.getRootCauseMessage( e ) )
            );
         }
      }
      
   }

   
}
