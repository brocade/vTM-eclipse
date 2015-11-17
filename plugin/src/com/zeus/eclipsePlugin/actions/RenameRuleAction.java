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
import com.zeus.eclipsePlugin.model.ModelSelection;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.operations.RenameRuleOp;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.dialogs.CustomResult;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.DialogOption;
import com.zeus.eclipsePlugin.zxtmview.RenameValidator;
import com.zeus.eclipsePlugin.zxtmview.ZXTMViewer;

/**
 * This renames the rule currently selected in the ZXTM viewer.
 */
public class RenameRuleAction extends ZAction
{
   private ModelSelection selection;
   private Rule rule;
   
   /** 
    * Standard constructor. The action will find the selection from the ZXTM
    * Viewer when it is activated. 
    */
   public RenameRuleAction() {}
   
   /**
    * Constructor that stores a selection. This is used by the action every time
    * is is run. This also sets up the text and image of the Action.
    * @param selection The selection to use every time this action is run.
    */
   public RenameRuleAction( ModelSelection selection )
   {
      this.selection = selection;
      
      if( selection != null && selection.isOnlyRules() && selection.getSize() == 1 ) {
         this.rule = selection.firstRule();
         
         this.setText( ZLang.ZL_Rename );
         
      } else {
         // Should not use this constructor without a valid selection
         ZDebug.dumpStackTrace( "RenameRuleAction created without proper selection." ); 
         this.setText( "!!" );
      }
   }
   
   /**
    * Get the command we are using (in this case the standard copy command)
    */
   /* Override */
   protected Command getCommand()
   {
      return Command.EDIT_RENAME;
   }

   /**
    * Run the action, displaying a custom input dialog to get the new name for 
    * the rule. It then runs a RenameRuleOp in a progress dialog to do the 
    * renaming.
    */
   /* Override */
   protected void run( ExecutionEvent event )
   {
      Rule currentRule = rule;
      ModelSelection currentSelection = selection;
      
      // No selection, check if the ZXTM Viewer has one currently.
      if( currentRule == null  ) {
         currentSelection = ZXTMViewer.getSelectionForOpenViewer();
         if( currentSelection == null || !currentSelection.isOnlyRules() || 
             currentSelection.size() != 1 ) 
         {
            return;
         }
         currentRule = currentSelection.firstRule();
      }
      
      if( currentRule == null ) return;
      
      // Show input dialog
      ZXTM zxtm = (ZXTM) currentRule.getModelParent();

      CustomResult inputResult = ZDialog.showCustomInputDialog( 
         ZLang.ZL_RenameDialogTitle, 
         ZLang.bind( ZLang.ZL_RenameDialogMessage, currentRule.getName() ),
         null, new RenameValidator( zxtm ), currentRule.getName(),
         DialogOption.RENAME, DialogOption.RENAME, DialogOption.CANCEL
      );
      
      String newName = inputResult.getInput();
      
      if( inputResult.getOption() == DialogOption.CANCEL  ||
          newName == null || newName == currentRule.getName() ) 
      {
         return;
      }
   
      // Do the renaming using a progress dialog
      RenameRuleOp op = new RenameRuleOp( currentRule, newName );
      
      try {
         SWTUtil.progressDialog( op );
      } catch( InvocationTargetException e ) {          
         ZDialog.showErrorDialog( ZLang.ZL_RenameFailedTitle, 
            ZLang.bind( 
               ZLang.ZL_RenameFailedMessage, 
               currentRule.getName(), ZUtil.getRootCauseMessage( e )
            )
         );
      }
      
   }
}
