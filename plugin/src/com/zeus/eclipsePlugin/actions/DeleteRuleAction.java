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
import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.consts.Command;
import com.zeus.eclipsePlugin.consts.ImageFile;
import com.zeus.eclipsePlugin.model.ModelSelection;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.project.operations.DeleteRulesOp;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;
import com.zeus.eclipsePlugin.zxtmview.ZXTMViewer;

/**
 * This action deletes the rules currently selected in the ZXTM Viewer.
 */
public class DeleteRuleAction extends ZAction
{
   private ModelSelection selection;
   private Collection<Rule> rules;
   
   /** 
    * Standard constructor. The action will find the selection from the ZXTM
    * Viewer when it is activated. 
    */
   public DeleteRuleAction() {}
   
   /**
    * Constructor that stores a selection. This is used by the action every time
    * is is run. This also sets up the text and image of the Action.
    * @param selection The selection to use every time this action is run.
    */
   public DeleteRuleAction( ModelSelection selection )
   {
      this.selection = selection;
      
      if( selection != null && selection.isOnlyRules() ) {
         this.rules = selection.getSelectedRules();
         
         this.setText( ZLang.ZL_Delete );
         this.setImageFile( ImageFile.DELETE );
         
      } else {
         // Should not use this constructor without a valid selection
         ZDebug.dumpStackTrace( "DeleteRuleAction created without proper selection." ); 
         this.setText( "!!" );
      }
   }
   
   /**
    * Get the command we are using (in this case the standard delete command)
    */
   /* Override */
   protected Command getCommand()
   {
      return Command.EDIT_DELETE;
   }

   /**
    * Run the action, first confirming with the user they actually want to 
    * delete these rules, then using a DeleteRulesOp to delete the rules 
    * (invokes a progress dialog).
    */
   /* Override */
   protected void run( ExecutionEvent event )
   {      
      Collection<Rule> currentRules = rules;
      ModelSelection currentSelection = selection;
      
      // No selection, check if the ZXTM Viewer has one currently.
      if( currentRules == null  ) {
         currentSelection = ZXTMViewer.getSelectionForOpenViewer();
         if( currentSelection == null ) return;
         
         currentRules = currentSelection.getSelectedRules();
      }
      
      if( currentRules == null || currentRules.size() == 0 ) return;
     
      // Create a confirmation dialog, altering text if we have more than one
      // rule
      String title, text;
      if( currentRules.size() == 1 ) {
         Rule rule = currentSelection.firstRule();
         title = ZLang.bind( ZLang.ZL_DeleteSingleTitle, rule.getName() ); 
         text = ZLang.bind( ZLang.ZL_DeleteSingleText, rule.getName() );
      } else {
         title = ZLang.ZL_DeleteSelectedRules;
         text = ZLang.bind( ZLang.ZL_DeleteMultiTitle, currentRules.size() );
      }
      
      if( ZDialog.showConfirmDialog( title, text ) ) {
         
         // Run the DeleteRulesOp with a progress dialog
         DeleteRulesOp op = new DeleteRulesOp( currentRules );
         
         try {
            SWTUtil.progressDialog( op );
         } catch( InvocationTargetException e ) {
            ZDialog.showErrorDialog( ZLang.ZL_DeleteFailedTitle,
               ZLang.bind( ZLang.ZL_DeleteFailedMessage, ZUtil.getRootCauseMessage( e ) )
            );
         }
         
      }
   }
}
