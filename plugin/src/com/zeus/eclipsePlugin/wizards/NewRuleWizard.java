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

package com.zeus.eclipsePlugin.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.operations.NewRuleOp;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;

/**
 * Eclipse wizard that can create a new rule.
 */
public class NewRuleWizard extends Wizard implements INewWizard
{
   private NewRuleWizardPage page1;

   /**
    * Create the single page that makes up this wizard. Set the windows title.
    */
   /* Override */
   public void init( IWorkbench workbench, IStructuredSelection selection )
   {
      page1 = new NewRuleWizardPage( selection );
      
      this.setWindowTitle( ZLang.ZL_AddANewTSRule );
   }
   
   /**
    * Add the page to the wizard.
    */
   /* Override */
   public void addPages()
   {
      addPage( page1 );
      super.addPages();
   }
   
   /**
    * Dispose the page when when we get disposed.
    */
   /* Override */
   public void dispose()
   {
      super.dispose();
      if( page1 != null ) page1.dispose();
   }

   /**
    * User completed the wizard. Create the rule using a WorkspaceOperation, and
    * display an progress dialog.
    */
   /* Override */
   public boolean performFinish()
   {
      if( page1.isPageComplete() ) {
         try {
            ZXTM zxtm = page1.getZXTM();                 
            SWTUtil.progressDialog( new NewRuleOp( zxtm,  page1.getName() ) );
            
         } catch( Exception e ) {
            ZDebug.printStackTrace( e, "Error when creating rule in wizard" );
            
            ZDialog.showErrorDialog( 
               ZLang.ZL_RuleCreationFailedTitle, 
               ZLang.bind( ZLang.ZL_RuleCreationFailedMessage, 
                  ZUtil.getRootCauseMessage( e ) 
               )
            );
                
            return false;
         }
      }
      return true;
   }

   

}
