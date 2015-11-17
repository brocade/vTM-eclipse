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

package com.zeus.eclipsePlugin.swt.dialogs;

import java.net.URI;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.filesystem.ZXTMFileSystem;
import com.zeus.eclipsePlugin.swt.SWTEnableListener;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.ZXTMControl;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.DialogOption;

/**
 * Dialog that asks how to fix a broken ZXTM project (project has a ZXTM nature
 * but no .zxmtConf file).
 */
public class BrokenZXTMDialog extends Dialog
{
   /**
    * Enumeration listing the different choices that can be made.
    */
   public enum Choice
   {
      GENERAL_PROJECT,
      CLOSE_PROJECT,
      DELETE,
      CONFIG,
      CLOSED_DIALOG
   }
   
   private IProject project;
   private ZXTMControl control;
   private Button radioGeneral, radioDelete, radioConfig, radioClose;

   private BrokenZXTMListener listener = new BrokenZXTMListener();
   private Button checkDeleteContent; 

   private Choice choice;
   private boolean deleteContents;
   private String hostname, user, password;
   private int port;
   private boolean storePW;
   
   /**
    * Setup the dialog, specifying the broken project.
    * @param parentShell The parent shell of this dialog
    * @param project The project that is broken.
    */
   public BrokenZXTMDialog( Shell parentShell, IProject project )
   {
      super( parentShell );
      this.project = project;
   }
   
   /**
    * Setup the shell (sets its title)
    */
   /* Override */
   protected void configureShell( Shell newShell )
   {
      super.configureShell( newShell );
      newShell.setText( ZLang.ZL_ProblemWithZXTMProjectConf );
      newShell.addShellListener( new ShellAdapter() {

         /* Override */
         public void shellClosed( ShellEvent e )
         {
            e.doit = false; // Do NOT close the shell please
            
            // We should't close the shell, but eclipse seems to do it anyway
            // so we have a special option just in case...
            choice = Choice.CLOSED_DIALOG; 
         }
         
      } );
   }
     
   /**
    * Create the UI controls for this dialog.
    */
   /* Override */
   protected Control createDialogArea( Composite parent )
   {     
      Composite composite = (Composite) super.createDialogArea( parent );
      composite.setLayout( SWTUtil.createGridLayout( 2 ) );
      
      Label text = SWTUtil.addLabel( composite, ZLang.bind( 
         ZLang.ZL_ProjectIsZXTMButConfMissing, project.getName()
      ) );
      SWTUtil.gridDataFillCols( text, 2 );
      
      radioClose = SWTUtil.addRadioButton( composite, 
         ZLang.ZL_CloseTheProject, false 
      );
      SWTUtil.gridDataColSpan( radioClose, 2 );
      
      radioGeneral = SWTUtil.addRadioButton( composite, 
         ZLang.ZL_MakeItAGeneralProject, false 
      );
      SWTUtil.gridDataColSpan( radioGeneral, 2 );      
      
      radioDelete = SWTUtil.addRadioButton( composite, 
         ZLang.ZL_RemoveTheProject, false 
      );
      SWTUtil.gridDataColSpan( radioDelete, 2 );
      
      SWTUtil.createBlankGrid( composite, 10, 1 );
      checkDeleteContent = SWTUtil.addCheckButton( 
         composite, ZLang.ZL_DeleteContentsToo, false 
      );
      
      radioConfig = SWTUtil.addRadioButton( composite, 
         ZLang.ZL_RestoreZXTMConfiguration, true 
      );
      SWTUtil.gridDataColSpan( radioConfig, 2 );
      
      SWTUtil.createBlankGrid( composite, 10, 1 );
      
      Composite zxtmComposite = SWTUtil.createGridLayoutComposite( composite, 2 );
      control = new ZXTMControl( 
         zxtmComposite, listener, null 
      );
            
      radioConfig.addSelectionListener( 
         new SWTEnableListener( 
            control.getControlSet(), radioConfig.getSelection(), radioConfig 
         ) 
      );
      radioDelete.addSelectionListener( 
         new SWTEnableListener( 
            checkDeleteContent, radioDelete.getSelection(), radioDelete 
         ) 
      );
      
      radioGeneral.addSelectionListener( listener );
      radioDelete.addSelectionListener( listener );
      radioConfig.addSelectionListener( listener );
      radioClose.addSelectionListener( listener );
            
      return composite;
   }
   
   /**
    * Create the buttons for this dialog, which is just the OK button.
    */
   /* Override */
   protected void createButtonsForButtonBar( Composite parent )
   {
      this.createButton( parent, DialogOption.OK.getId(),
         DialogOption.OK.getText(), true 
      );
      
      IFolder folder = project.getFolder( ZXTMFileSystem.RULE_PATH );
      if( folder.exists() ) {
         URI ruleDirURI = folder.getLocationURI();
         
         if( ruleDirURI.getScheme().equals( ZXTMFileSystem.PROTOCOL ) ) { 
            if( ruleDirURI.getHost() != null ) { 
               control.setHostname( ruleDirURI.getHost() );
            }
            if( ruleDirURI.getPort() > 0 ) {
               control.setPort( ruleDirURI.getPort() );
            }
         }
      }
      
      listener.update();
   }
   
   /**
    * When OK is pressed, stores the current values in the UI so that they can
    * be used after the dialog is disposed.
    */
   /* Override */
   protected void okPressed()
   {     
      ZDebug.print( 4, "okPressed()" );
      if( radioGeneral.getSelection() ) {
         choice = Choice.GENERAL_PROJECT;
      } else if( radioDelete.getSelection() ) {
         choice = Choice.DELETE;
      } else if( radioConfig.getSelection() ) {
         choice = Choice.CONFIG;
      } else if( radioClose.getSelection() ) {
         choice = Choice.CLOSE_PROJECT;
      }
      
      deleteContents = checkDeleteContent.getSelection();
      
      hostname = control.getHostname();
      user = control.getUserName();
      port = control.getPort();
      password = control.getPassword();
      storePW = control.getStorePassword();            
      
      super.okPressed();      
   }
   
   /**
    * Get the choice that was made in the dialog.
    * @return The choice that was made
    */
   public Choice getChoice()
   {
      return choice;
   }

   /**
    * Get the value of the delete contents of project check box. Only applicable
    * if the DELETE choice was used.
    * @return True if the user wants to delete the contents of the project
    */
   public boolean getDeleteContents()
   {
      return deleteContents;
   }

   /**
    * Get the hostname for the fixed project configuration. Only applicable for
    * the CONFIG choice.
    * @return The hostname (or IP address) to use to recreate the .zxtmConf 
    * file.
    */
   public String getHostname()
   {
      return hostname;
   }

   /**
    * Get the password for the fixed project configuration. Only applicable for
    * the CONFIG choice.
    * @return The password to use to recreate the .zxtmConf file.
    */
   public String getPassword()
   {
      return password;
   }
   
   /**
    * Get the user-name for authentication that was entered. Only applicable for
    * the CONFIG choice.
    * @return The password to use to recreate the .zxtmConf file.
    */
   public String getUserName()
   {
      return user;
   }

   /**
    * Get the admin port for the fixed project configuration. Only applicable 
    * for the CONFIG choice.
    * @return The port to use to recreate the .zxtmConf file.
    */
   public int getPort()
   {
      return port;
   }

   /**
    * Should the password be stored in the fixed project configuration? Only 
    * applicable for the CONFIG choice.
    * @return If the password should be stored locally.
    */
   public boolean getStorePassword()
   {
      return storePW;
   }

   /**
    * Listens for control changes and updates the OK button to be disabled if 
    * the dialog contains invalid values.
    */
   private class BrokenZXTMListener implements VerifyListener, SelectionListener
   {
      /**
       * Update the OK button, enabling it if the UI's settings are valid.
       */
      public void update()
      {
         Button ok = getButton( DialogOption.OK.getId() );
         
         if( radioGeneral.getSelection() || radioDelete.getSelection() || radioClose.getSelection() ) {
            ok.setEnabled( true );
         } else if( control.isFinished() ) {
            ok.setEnabled( true );
         } else {
            ok.setEnabled( false );
         }
      }

      /* Override */
      public void verifyText( VerifyEvent e )
      {
         update();
      }

      /* Override */
      public void widgetDefaultSelected( SelectionEvent e )
      {
         update();
      }

      /* Override */
      public void widgetSelected( SelectionEvent e )
      {
         update();
      }
            
   }
   
   
}
