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
import com.zeus.eclipsePlugin.wizards.NewZXTMWizard;

/**
 * This action runs the NewZXTMWizard (which creates a Eclipse project which
 * links to a specified ZXTM). 
 */
public class NewZXTMAction extends ZAction
{
   /**
    * Constructor sets up the actions text and image.
    */
   public NewZXTMAction()
   {
      this.setText( ZLang.ZL_AddNewZXTMProject );
      this.setImageFile( ImageFile.ZXTM_ADD );
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
    * Runs the action, showing the add ZXTM wizard.
    */
   /* Override */
   public void run( ExecutionEvent event )
   {
      IWorkbench workbench = ZXTMPlugin.getDefault().getWorkbench();

      NewZXTMWizard wizard = new NewZXTMWizard();
      wizard.init( workbench, null );

      WizardDialog dialog = new WizardDialog( 
         workbench.getActiveWorkbenchWindow().getShell(),
         wizard
      );
      
      dialog.open();
   }
}
