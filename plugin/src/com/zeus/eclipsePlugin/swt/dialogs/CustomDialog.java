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

import java.util.EnumMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.DialogOption;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.Icon;

/**
 * A simple custom dialog class. Displays a message, a custom set of buttons and
 * a input box (if enabled).
 */
public class CustomDialog extends IconAndMessageDialog
{
   private String title;
   private DialogOption[] buttons;
   private DialogOption defaultOption;
   private IInputValidator validator;
   private String initial;
   
   private DialogOption result;
   private String resultString;
   
   private Text textInput;
   private Label labelError;
   private Icon icon;
   
   private EnumMap<DialogOption,Button> buttonMap = 
      new EnumMap<DialogOption,Button>( DialogOption.class );

   /**
    * Create the custom dialog, setting all of its configurable options.
    * @param parentShell The parent shell for this dialog
    * @param title The title bar text
    * @param message The main message text
    * @param validator The validator for the input box. If null no input box
    * will be created.
    * @param initial The initial value for the input box.
    * @param icon The icon to display beside the message, set to null for no
    * icon.
    * @param defaultOption The default option (button) for the dialog.
    * @param buttons The buttons for this dialog, must include the 
    * defaultOption.
    */
   public CustomDialog( Shell parentShell, String title, String message, 
      IInputValidator validator, String initial, Icon icon,
      DialogOption defaultOption,  DialogOption ... buttons )
   {
      super( parentShell );
      this.title = title;
      this.message = message;
      this.buttons = buttons;
      this.defaultOption = defaultOption;
      this.validator = validator;
      this.initial = initial;
      this.icon = icon;
   }
   
   /**
    * Configure the shell the dialog is displayed in. Sets the title.
    */
   /* Override */
   protected void configureShell( Shell newShell )
   {
      super.configureShell( newShell );
      newShell.setText( title );
      
      newShell.addShellListener( new ShellAdapter() {

         /* Override */
         public void shellClosed( ShellEvent e )
         {
            e.doit = false; // Do NOT close the shell please            
            
            // We should't close the shell, but eclipse seems to do it anyway
            // so we have a special option just in case...
            result = DialogOption.CLOSED_DIALOG;
         }
         
      } );
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
    * Creates the dialog controls in the passed composite.
    */
   /* Override */
   protected Control createDialogArea( Composite parent )
   {
      Composite composite = (Composite) super.createDialogArea( parent );
      composite.setLayout( SWTUtil.createGridLayout( 2 ) );
      
      createMessageArea( composite );
      if( imageLabel == null ) {
         SWTUtil.gridDataColSpan( messageLabel, 2 );
      }
           
      if( validator != null ) {
         SWTUtil.createBlankHorizontalFill( composite, 3 );
         
         textInput = SWTUtil.addText( composite, initial, SWTUtil.FILL );
         SWTUtil.gridDataColSpan( textInput, 2 );
         SWTUtil.gridDataPreferredWidth( textInput, 350 );
         textInput.setSelection( 0, initial.length() );
         
         labelError = SWTUtil.addLabel( composite, "" );
         SWTUtil.gridDataFillHorizontal( labelError );
         SWTUtil.gridDataColSpan( labelError, 2 );
         labelError.setVisible( false );
         
         SWTUtil.gridDataColSpan( textInput, 2 );
         
         textInput.addModifyListener( new ModifyListener() {
            /* Override */ public void modifyText( ModifyEvent e )
            {
               setError( validator.isValid( textInput.getText() ) );
               resultString = textInput.getText();
            }            
         });
         
         setError( validator.isValid( textInput.getText() ) );
      }
      
      composite.layout( true, true );
      composite.pack();
      
      return composite;
   }
   
   /**
    * Create the buttons for this dialog based on the options in the 
    * constructor.
    */
   /* Override */
   protected void createButtonsForButtonBar( Composite parent )
   {
      for( DialogOption option : buttons )
      {
         Button button = this.createButton( 
            parent, option.getId(), 
            option.getText(), option == defaultOption
         );
         button.addSelectionListener( new ButtonListener( option ) );
         buttonMap.put( option, button );
      }
      
      if( textInput != null ) {
         setError( validator.isValid( textInput.getText() ) );
         resultString = textInput.getText();
      }
   }
   
   /**
    * Get the button for the specified option.
    * @param option The option that you want a button for.
    * @return The SWT button for the specified option.
    */
   protected Button getButton( DialogOption option ) 
   {
      return buttonMap.get( option );
   }
      
   /**
    * Update the error message for this dialog. Also enabled / disables the 
    * OK button for this dialog.
    * @param valid The error message, or null if there is no error.
    */
   protected void setError( String valid )
   {
      if( getButton( IDialogConstants.OK_ID ) != null ) {
         getButton( IDialogConstants.OK_ID ).setEnabled( valid == null );
      }

      labelError.setVisible( valid != null );
      if( valid != null ) {
         labelError.setText( valid );
      } else {
         labelError.setText( "" );
      }
   }
   
   /**
    * Get the option that was chosen by the user.
    * @return The option that was chosen.
    */
   public DialogOption getResult()
   {
      return result;
   }
   
   /**
    * If this dialog had an input box, the value the user entered into it.
    * @return The value entered into the input box, or null if there was no 
    * input box.
    */
   public String getResultString()
   {
      return resultString;
   }
   
   /**
    * Listener that updates the selected option value when a button is selected.
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
         result = option;
      }

      /* Override */
      public void widgetSelected( SelectionEvent arg0 )
      {
         result = option;
      }
      
   }


}
