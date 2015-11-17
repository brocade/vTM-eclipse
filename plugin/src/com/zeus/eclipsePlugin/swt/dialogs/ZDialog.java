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
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Shell;

import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * Standard dialogs that can be easily created. Does not require to be run from
 * the SWT Thread, as it does that all for you.
 */
public class ZDialog
{
   /**
    * The different buttons that can be put at the bottom of a custom dialog.
    */
   public enum DialogOption
   {
      OK             ( ZLang.ZL_DialogOk, IDialogConstants.OK_ID ),
      RENAME         ( ZLang.ZL_DialogRename, IDialogConstants.OK_ID ),
      SAVE           ( ZLang.ZL_DialogSave, IDialogConstants.OK_ID  ),
      RETRY          ( ZLang.ZL_DialogRetry, IDialogConstants.OK_ID ),
      
      CANCEL         ( ZLang.ZL_DialogCancel, IDialogConstants.CANCEL_ID ),
      DISCONNECT     ( ZLang.ZL_DialogDisconnectZXTM, IDialogConstants.CANCEL_ID ),
      DISCARD        ( ZLang.ZL_DialogDiscardChanges, IDialogConstants.CANCEL_ID ),
      REPLACE        ( ZLang.ZL_DialogReplace, IDialogConstants.CANCEL_ID ),
      REPLACE_REMAINING    ( ZLang.ZL_DialogReplaceRemaining, IDialogConstants.CANCEL_ID ),
      SKIP           ( ZLang.ZL_DialogSkip, IDialogConstants.CANCEL_ID ),
      SKIP_REMAINING ( ZLang.ZL_DialogSkipRemaining, IDialogConstants.CANCEL_ID ), 
      
      /** Special option should not be used as a button */
      CLOSED_DIALOG  ( "!!!!!", 0 ),                                      
      ;
      
      private String text;
      private int id;
      
      private DialogOption( String text, int id )
      {
         this.text = text;
         this.id = id;
      }

      public String getText()
      {
         return text;
      }

      public int getId()
      {
         return id;
      }
   }
   
   /**
    * The different icons that can be added to custom dialogs.
    */
   public enum Icon 
   {
      QUESTION,
      WARNING,
      ERROR,
      INFO
   }
   
   /**
    * Get the current workbench shell.
    * @return The current workbench shell.
    */
   public static Shell getShell()
   {
      return ZXTMPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
   }
   
   /**
    * Display the standard Eclipse error dialog.
    * @param title The dialog window's title.
    * @param message The error message to display in the dialog.
    */
   public static void showErrorDialog( String title, String message )
   {      
      DialogRunner runner = new DialogRunner( DialogType.ERROR, title, message );
      SWTUtil.exec( runner );
   }
   
   /**
    * Show the standard Eclipse confirm dialog.
    * @param title The dialog window's title.
    * @param message The message asking an yes/no answer
    * @return True if the user OKed the question.
    */
   public static boolean showConfirmDialog( String title, String message )
   {
      DialogRunner runner = new DialogRunner( DialogType.CONFIRM, title, message );
      SWTUtil.exec( runner );
      return runner.getResultBoolean();
   }
   
   /**
    * Show the standard Eclipse confirm dialog, with a check box as well.
    * @param title The dialog window's title.
    * @param message The message asking an yes/no answer
    * @param toggleMessage The message next to the check box.
    * @param toggleState Should the check box be selected initially?
    * @return True if the user OKed the question.
    */
   public static ToggleConfirmResult showConfirmToggleDialog( String title, 
      String message, String toggleMessage, boolean toggleState )
   {
      DialogRunner runner = new DialogRunner( 
         DialogType.CONFIRM_TOGGLE, title, message 
      );
      runner.setToggleMessage( toggleMessage );
      runner.setToggleState( toggleState );
      
      SWTUtil.exec( runner );
      
      return new ToggleConfirmResult( 
         runner.getResultBoolean(), 
         runner.getResultOption() == DialogOption.OK
      );      
   }

   /**
    * Display the ZXTM password dialog.
    * @param title The dialog window's title.
    * @param message The message explaining why a password is being requested.
    * @param icon The icon to display with the message, or null for no icon.
    * @param zxtm The ZXTM whose password is being requested.
    * @return The options entered into the password dialog.
    */
   public static PasswordResult showPasswordDialog( String title,
      String message, Icon icon, ZXTM zxtm )
   {
      DialogRunner runner = new DialogRunner( 
         DialogType.PASSWORD, title, message 
      );      
      runner.setZXTM( zxtm );
      runner.setIcon( icon );
      
      SWTUtil.exec( runner );
      
      return new PasswordResult( 
         runner.getResultString2(), runner.getResultString(), 
         runner.getResultBoolean(), runner.getResultOption() 
      );
   }
   
   /**
    * Show the exception info dialog.
    * @param title The dialog window's title.
    * @param message The message to display above the exception info.
    * @param e The exception to show the info on.
    */
   public static void showExceptionDialog( String title, String message, Exception e )
   {
      DialogRunner runner = new DialogRunner( 
         DialogType.ERROR_EXCEPTION, title, message
      );
      runner.setException( e );
      
      SWTUtil.exec( runner );
   }
   
   /**
    * Show the standard Eclipse input dialog. 
    * @param title The dialog window's title.
    * @param message The message displayed at the top of the dialog window.
    * @param initialText The initial text for the dialog
    * @param validator The text validator.
    * @return The string entered, or null if cancel was selected.
    */
   public static String showInputDialog( String title, String message,
      String initialText, IInputValidator validator )
   {
      DialogRunner runner = new DialogRunner( 
         DialogType.INPUT, title, message
      );
      runner.setInitialString( initialText );
      runner.setValidator( validator );

      SWTUtil.exec( runner );
      
      return runner.getResultString();
   }
   
   /**
    * Show the platform's standard colour chooser.
    * @param title The title for the chooser dialog.
    * @param defaultColour The initial colour that is selected.
    * @return The colour that was selected by the user, or null if the user
    * cancelled.
    */
   public static RGB showColourDialog( String title,
      RGB defaultColour )
   {
      DialogRunner runner = new DialogRunner( 
         DialogType.COLOUR, title, ""
      );
      runner.setRgbDefault( defaultColour );
      
      SWTUtil.exec( runner );
      
      return runner.getResultRGB();
   }
   
   /**
    * Show a custom dialog with a message and configurable buttons.
    * @param title The dialog window's title.
    * @param message The message to display in the dialog
    * @param icon The icon to display next to the message, or null to not show
    * an icon.
    * @param defaultOption The default option (button)
    * @param options The different options (buttons) the dialog should have.
    * @return The dialog option that was selected.
    */
   public static DialogOption showCustomDialog( String title, String message, 
      Icon icon, DialogOption defaultOption, DialogOption ... options )
   {
      DialogRunner runner = new DialogRunner( 
         DialogType.CUSTOM, title, message
      );
      runner.setIcon( icon );
      runner.setDefaultOption( defaultOption );
      runner.setOptions( options );
      
      SWTUtil.exec( runner );
      
      return runner.getResultOption();
   }
   
   /**
    * Show a custom input dialog with a message and configurable buttons.
    * @param title The dialog window's title.
    * @param message The message to display in the dialog
    * @param icon The icon to display next to the message, or null to not show
    * an icon.
    * @param validator Class to check a the entered text is valid. Must be set.
    * @param initial The initial text of the input text box.
    * @param defaultOption The default option (button)
    * @param options The different options (buttons) the dialog should have.
    * @return The dialog option that was selected.
    */
   public static CustomResult showCustomInputDialog( String title, String message, 
      Icon icon,
      IInputValidator validator, String initial,
      DialogOption defaultOption, DialogOption ... options )
   {
      DialogRunner runner = new DialogRunner( 
         DialogType.CUSTOM, title, message
      );
      runner.setIcon( icon );
      runner.setValidator( validator );
      runner.setInitialString( initial );
      runner.setDefaultOption( defaultOption );
      runner.setOptions( options );
      
      SWTUtil.exec( runner );
      
      return new CustomResult( runner.getResultString(), runner.getResultOption() );
   }
   
   /**
    * The different types of dialog box that can be created.
    */
   private enum DialogType
   {
      ERROR,
      PASSWORD,      
      CONFIRM,
      CONFIRM_TOGGLE,
      ERROR_EXCEPTION,
      INPUT,
      CUSTOM,
      COLOUR,
      BROKEN_ZXTM,
   }
   
   /**
    * This runnable class creates all the different dialog boxes in the SWT 
    * display thread. Stores appropriate results for access after its run.
    */
   private static class DialogRunner implements Runnable
   {
      private DialogType type;
      private String title, message, initial;
      private ZXTM zxtm;
      private Exception e;
      private IInputValidator validator;
      private DialogOption defaultOption;
      private DialogOption[] options;
      private RGB rgbDefault;
      private Icon icon;
      private IProject project;
      private String toggleMessage;
      private boolean toggleState;
      
      // The result (selection) of the dialog.
      private String resultString  = null, resultString2 = null;
      private boolean resultBoolean;
      private DialogOption resultOption = null;
      private RGB resultRGB = null;
      private ZXTM resultZXTM = null;

      public DialogRunner( DialogType type, String title, String message )
      {
         this.type = type;
         this.title = title;
         this.message = message;
      }
      
      public void setInitialString( String initial )
      {
         this.initial = initial;
      }

      public void setZXTM( ZXTM zxtm )
      {
         this.zxtm = zxtm;
      }
      
      public void setException( Exception e )
      {
         this.e = e;
      }
      
      public void setValidator( IInputValidator validator )
      {
         this.validator = validator;
      }
      
      public void setDefaultOption( DialogOption defaultOption )
      {
         this.defaultOption = defaultOption;
      }

      public void setOptions( DialogOption[] options )
      {
         this.options = options;
      }
      
      public void setIcon( Icon icon )
      {
         this.icon = icon;
      }
      public void setProject( IProject project )
      {
         this.project = project;
      }
      
      public void setRgbDefault( RGB rgbDefault )
      {
         this.rgbDefault = rgbDefault;
      }
      
      public void setToggleMessage( String toggleMessage )
      {
         this.toggleMessage = toggleMessage;
      }
      
      public void setToggleState( boolean selected )
      {
         this.toggleState = selected;
      }

      public String getResultString()
      {
         return resultString;
      }
      
      public String getResultString2()
      {
         return resultString2;
      }

      public boolean getResultBoolean()
      {
         return resultBoolean;
      }
      
      public DialogOption getResultOption()
      {
         return resultOption;
      }
      
      public RGB getResultRGB()
      {
         return resultRGB;
      }
      
      public ZXTM getResultZXTM()
      {
         return resultZXTM;
      }

      /**
       * The main run method. Creates the appropriate dialog box and waits for
       * it to finish, then stores the results of the dialog.
       */
      /* Override */
      public void run()
      {
         if( !ZXTMPlugin.isEclipseLoaded() ) return;
         
         switch( type ) {
            case ERROR: {
               MessageDialog.openError( getShell(), title, message );
               break;               
            }           
            case CONFIRM: {
               resultBoolean = MessageDialog.openConfirm( 
                  getShell(), title, message 
               );
               break;
            }
            
            case CONFIRM_TOGGLE: {
               MessageDialogWithToggle dialog = 
                  MessageDialogWithToggle.openOkCancelConfirm( getShell(), 
                     title, message, toggleMessage, toggleState, null, null
                  );
               
               resultBoolean = dialog.getToggleState();
               resultOption = 
                  (dialog.getReturnCode() == MessageDialogWithToggle.OK) ?
                  DialogOption.OK : DialogOption.CANCEL;
               
               break;
            }
            
            case INPUT: {
               InputDialog dialog = new InputDialog( 
                  getShell(), title, message, initial, validator  
               );
               
               dialog.setBlockOnOpen( true );
               dialog.open();
               
               if( dialog.getReturnCode() == InputDialog.OK) {
                  resultString = dialog.getValue();
                  resultBoolean = true;
               } else {
                  resultString = null;
                  resultBoolean = false;
               }
               break;
            }
            case PASSWORD: {
               PasswordDialog dialog = new PasswordDialog(
                  getShell(), title, message, icon, zxtm
               );
               
               dialog.setBlockOnOpen( true );
               dialog.open();
               
               resultString = dialog.getPassword();
               resultString2 = dialog.getUserName();
               resultBoolean = dialog.getStorePassword();
               resultOption = dialog.getSelectedOption();
               break;
            }       
            case COLOUR: {
               ColorDialog dialog = new ColorDialog( getShell() );
               dialog.setText( title );
               dialog.setRGB( rgbDefault );
               
               dialog.open();
               
               resultRGB = dialog.getRGB();               
               break;
            }
               
            case ERROR_EXCEPTION: {
               ExceptionDialog dialog = new ExceptionDialog(
                  getShell(), title, message, e
               );
               
               dialog.setBlockOnOpen( true );
               dialog.open();
               break;
            }
            case CUSTOM: {
               
               CustomDialog dialog = null;
               do {
                  dialog = new CustomDialog( getShell(), title, message, 
                     validator, initial, icon, defaultOption, options 
                  );
                  
                  dialog.setBlockOnOpen( true );
                  dialog.open();
                  
               } while( dialog.getResult() == DialogOption.CLOSED_DIALOG );
               
               resultOption = dialog.getResult();
               resultString = dialog.getResultString();
               break;
            }
            case BROKEN_ZXTM: {
               BrokenZXTMDialog dialog = new BrokenZXTMDialog( getShell(), project );
               
               dialog.setBlockOnOpen( true );
               dialog.open();

               break;
            }
           
         }
      }      
   }

   
}
