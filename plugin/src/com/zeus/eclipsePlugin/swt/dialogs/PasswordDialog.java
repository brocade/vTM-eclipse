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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.DialogOption;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.Icon;

/**
 * Dialog that requests a password from the user.
 */
public class PasswordDialog extends IconAndMessageDialog
{
   private String title;
   private ZXTM zxtm;
   private Icon icon;
   
   private Text textUser, textPassword;
   private Button checkStorePW;
   
   private String resultPassword;
   private boolean resultStore;
   private String resultUser;
   private DialogOption resultOptions;
   
   private Listener listener = new Listener();

   /**
    * Setup the dialog with the messages to be displayed.
    * @param parentShell The parent shell of this dialog
    * @param title The dialog's window title.
    * @param message The message to display above the password entry box.
    * @param icon The icon to display next to the message, or null if no icon
    * is needed.
    * @param zxtm The ZXTM whose password we are asking for, or null if this info
    * should not be shown.
    */
   public PasswordDialog( Shell parentShell, String title, String message,
         Icon icon, ZXTM zxtm )
   {
      super( parentShell );
      this.title = title;
      this.message = message;
      this.zxtm = zxtm;
      this.icon = icon;
   }
   
   /**
    * Set the shells title
    */
   /* Override */
   protected void configureShell( Shell newShell )
   {
      super.configureShell( newShell );
      newShell.setText( title );
   }

   /**
    * Returns the image that the dialog displays, based on the value provided
    * in the constructor.
    */
   /* Override */
   protected Image getImage()
   {
      if( icon == null ) {
         return null;
      }
      switch( icon ) {
         case INFO: return getInfoImage();
         case WARNING: return getWarningImage();
         case ERROR: return getErrorImage();
         case QUESTION: return getQuestionImage();
      }
      return getQuestionImage();
   }

   /**
    * Create a the UI controls.
    */
   /* Override */
   protected Control createDialogArea( Composite parent )
   {
      Composite composite = (Composite) super.createDialogArea( parent );
      composite.setLayout( SWTUtil.createGridLayout( 2 ) );
      
      Composite message = SWTUtil.createGridLayoutComposite( composite, 3, 15, 3 );
      SWTUtil.removeLayoutMargins( message.getLayout() );
      SWTUtil.gridDataFillCols( message, 2 );
      
      createMessageArea( message );
      if( imageLabel == null ) {
         SWTUtil.gridDataColSpan( messageLabel, 3 );
      } else {
         SWTUtil.gridDataColSpan( messageLabel, 2 );
      }
      
      boolean currentStore = false;
      if( zxtm != null ) {
         if( imageLabel != null ) {
            SWTUtil.gridDataRowSpan( imageLabel, 4 );
         }
         
         currentStore = zxtm.getStorePassword();
         resultStore = zxtm.getStorePassword();
         
         SWTUtil.createBlankGrid( message );
         SWTUtil.createBlankGrid( message );
         
         Label labelHostname = SWTUtil.addLabel( message, ZLang.ZL_ZXTMLabel );
         SWTUtil.fontStyle( labelHostname, SWT.BOLD );
         
         SWTUtil.addLabel( message, zxtm.getNamePort() );         
         
         IProject project = ZXTMProject.getProjectForZXTM( zxtm );
         String projectName = (project == null) ? ZLang.ZL_Unknown : project.getName();
         
         Label labelProject = SWTUtil.addLabel( message, ZLang.ZL_ProjectLabel );
         SWTUtil.fontStyle( labelProject, SWT.BOLD );
         
         SWTUtil.addLabel( message, projectName );
      }

      SWTUtil.createBlankHorizontalFill( composite, 10 );
      
      textUser = SWTUtil.addLabeledText( composite, ZLang.ZL_AdminUserLabel, 200 ).text();
      if( zxtm != null ) {
         textUser.setText( zxtm.getUserName() );
      } else {
         textUser.setText( "admin" );
      }
      textUser.addModifyListener( listener );
      
      textPassword = SWTUtil.addLabeledPasswordText( composite, ZLang.ZL_PasswordLabel, SWTUtil.FILL ).text();
      textPassword.addModifyListener( listener );

      SWTUtil.createBlankGrid( composite );
      
      checkStorePW = SWTUtil.addCheckButton( composite, ZLang.ZL_StorePasswordWithProject, currentStore );
      checkStorePW.addSelectionListener( listener );
      
      SWTUtil.createBlankHorizontalFill( composite, 0 );
      
      Label warn = SWTUtil.addLabel( composite, ZLang.ZL_YourPasswordIsStoredLocally );
      SWTUtil.gridDataFillHorizontal( warn );
      SWTUtil.gridDataColSpan( warn, 2 );
      SWTUtil.gridDataPreferredWidth( warn, 400 );
      
      textPassword.setFocus();
      
      composite.layout( true, true );
      composite.pack();
      
      return composite;
   }
   
   /**
    * Create the dialog's buttons: An OK button and a disconnect button.
    */
   /* Override */
   protected void createButtonsForButtonBar( Composite parent )
   {
      createButton( 
         parent, IDialogConstants.OK_ID, 
         DialogOption.OK.getText(), true
      ).addSelectionListener( new ButtonListener( DialogOption.OK ) );
      
      createButton( 
         parent, IDialogConstants.CANCEL_ID, 
         DialogOption.DISCONNECT.getText(), false
      ).addSelectionListener( new ButtonListener( DialogOption.DISCONNECT ) );
      
      listener.update();
   }

   /**
    * Get the entered password.
    * @return The password entered into the dialog
    */
   public String getPassword()
   {      
      return resultPassword;
   }
   
   /**
    * Get the entered user name;
    * @return The user-name that was entered.
    */
   public String getUserName()
   {
      return resultUser;
   }
   
   /**
    * Should the password be stored locally?
    * @return True if the password should be stored locally.
    */
   public boolean getStorePassword()
   {
      return resultStore;
   }
      
   /**
    * Get the button pressed to close the dialog
    * @return The selected dialog option.
    */
   public DialogOption getSelectedOption()
   {
      return resultOptions;
   }

   /**
    * Stores the selected option so that when the Dialog is dispose it can
    * still be accessed.
    */
   class ButtonListener implements SelectionListener {
      
      private DialogOption option;

      public ButtonListener( DialogOption option )
      {
         this.option = option;
      }

      /* Override */
      public void widgetDefaultSelected( SelectionEvent arg0 )
      {
         resultOptions = option;
      }

      /* Override */
      public void widgetSelected( SelectionEvent arg0 )
      {
         resultOptions = option;
      }
      
   }

   /**
    * Stores the input password so it can be accessed after the dialog is 
    * disposed. Also disabled the OK button if the password box is empty.
    */
   class Listener extends SelectionAdapter implements ModifyListener, ControlListener
   {
      public void update()
      {
         if( textPassword.isDisposed() ) return;
         
         boolean valid = ( 
            textUser.getText().length() != 0  && 
            ZUtil.validateUserName( textUser.getText() ) == null &&
            textPassword.getText().length() != 0
         );
              
         Button ok = getButton( IDialogConstants.OK_ID );
         if( ok != null ) {
            ok.setEnabled( valid );
         }
         
         resultPassword = textPassword.getText();
         resultUser = textUser.getText();
         resultStore = checkStorePW.getSelection();
      }
      
      /* Override */
      public void widgetSelected( SelectionEvent e )
      {
         update();  
      }
      
      /* Override */
      public void modifyText( ModifyEvent e )
      {
         update();         
      }

      /* Override */
      public void controlMoved( ControlEvent arg0 )
      {
         update();
      }

      /* Override */
      public void controlResized( ControlEvent arg0 )
      {
         update();
      }
      
   }
   

}
