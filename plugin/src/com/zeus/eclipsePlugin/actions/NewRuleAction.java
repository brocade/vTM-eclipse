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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;

import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Command;
import com.zeus.eclipsePlugin.consts.ImageFile;
import com.zeus.eclipsePlugin.model.ModelSelection;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.model.ModelElement.State;
import com.zeus.eclipsePlugin.wizards.NewRuleWizard;
import com.zeus.eclipsePlugin.zxtmview.ZXTMViewer;

/**
 * This action creates a new rule. It invokes the NewRuleWizard, passing it the
 * currently selected rule in the ZXTM Viewer (if one is selected)
 */
public class NewRuleAction extends ZAction
{  
   private ModelSelection selection;
   
   /** 
    * Standard constructor. The action will find the selection from the ZXTM
    * Viewer when it is activated. 
    */
   public NewRuleAction() {
      this.setImageFile( ImageFile.RULE_ADD );
      this.setText( ZLang.ZL_AddNewRule );
   }
   
   /**
    * Constructor that stores a selection. This is used by the action every time
    * is is run. This also sets up the text and image of the Action.
    * @param selection The selection to use every time this action is run.
    */
   public NewRuleAction( ModelSelection selection )
   {
      this.selection = selection;
      
      ZXTM zxtm = null;
      if( selection != null && (zxtm = selection.getSelectedZXTM()) != null &&
          zxtm.getModelState() != State.DISCONNECTED ) 
      {
         this.setText( 
            ZLang.ZL_AddNewRuleToZXTM 
         );
         
      } else {
         this.setText( ZLang.ZL_AddNewRule );
      }
      
      this.setImageFile( ImageFile.RULE_ADD );
   }
   
   /**
    * There is no related command with this action (this is a toggle command)
    */
   /* Override */
   protected Command getCommand()
   {
      return null; // No way to specify a parameter to this command? Command.NEW_WIZARD;
   }

   /**
    * Run this action, creating a NewRuleWizard in a dialog.
    */
   /* Override */
   public void run( ExecutionEvent event )
   {
      ModelSelection currentSelection = selection;
      
      // No selection, check if the ZXTM Viewer has one currently.
      if( currentSelection == null ) {
         currentSelection = ZXTMViewer.getSelectionForOpenViewer();        
      }
      
      IWorkbench workbench = ZXTMPlugin.getDefault().getWorkbench();

      // Create a WizardDailog and put the NewRuleWizard in it
      NewRuleWizard wizard = new NewRuleWizard();
      wizard.init( workbench, currentSelection );

      WizardDialog dialog = new WizardDialog( 
         workbench.getActiveWorkbenchWindow().getShell(),
         wizard
      );
      
      dialog.open();
   }
  
}
