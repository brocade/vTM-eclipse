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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.project.operations.CreateProjectOp;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;

/**
 * Wizard for creating a new ZXTM project.
 */
public class NewZXTMWizard extends Wizard implements INewWizard
{
   // Pages
   private NewZXTMWizardPage page1 = new NewZXTMWizardPage();
   
   /**
    * Set the window title of the wizard.
    */
   /* Override */
   public void init( IWorkbench workbench, IStructuredSelection selection )
   {
      this.setWindowTitle( ZLang.ZL_CreateANewZXTMProjectTitle );
   }
   
   /**
    * Add the solitary page to this wizard.
    */
   /* Override */
   public void addPages()
   {
      addPage( page1 );
   }

   /**
    * Dispose the page when when we get disposed.
    */
   /* Override */
   public void dispose()
   {
      super.dispose();
      page1.dispose();
   }

   /**
    * User completed the wizard. Create the new project using a workspace 
    * operation. This operation can fail if the ZXTM is un-contactable, which
    * will display an error dialog.
    */
   /* Override */
   public boolean performFinish()
   {      

      if( page1.isPageComplete() ) {

         try {
           
            // Create the project!
            CreateProjectOp op = new CreateProjectOp(
               page1.getProjectName().trim(),
               page1.getHostName(),
               page1.getPort(),
               page1.getUserName(),
               page1.getPassword(),
               page1.storePassword(),
               page1.useCustomLocation(),
               page1.getPath()
            );
            
            SWTUtil.progressDialog( op );          
           
         } catch( InvocationTargetException e ) {
            ZDebug.printStackTrace( e, "New Wizard failed" );
            ZDialog.showErrorDialog( 
               ZLang.ZL_ErrorOccured, ZLang.bind(
                  ZLang.ZL_CouldNotCreateZXTMProject, 
                  ZUtil.getRootCauseMessage( e ) 
             ) );           
            return false;
         }
      }

      return true;
   }

}
