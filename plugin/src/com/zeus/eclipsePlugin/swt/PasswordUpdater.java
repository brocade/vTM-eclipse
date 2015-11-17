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

package com.zeus.eclipsePlugin.swt;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.model.PassswordCallback;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.operations.ChangeZXTMAuthOp;
import com.zeus.eclipsePlugin.swt.dialogs.PasswordResult;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.DialogOption;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.Icon;

/**
 * Class that listens for password update requests from the model and displays
 * a dialog asking for the user to update their password. Will only ever display
 * a single password dialog at a time
 */
public class PasswordUpdater implements PassswordCallback
{
   private static PasswordDialogRunner runner;
   
   /**
    * Shows the password dialog using the PasswordDialogRunner class. Creates 
    * the class if it hasn't already been created.
    * @param zxtm The ZXTM to show the error for
    * @param isError Is the dialog being shown because there's been a problem.
    */
   private synchronized void showDialog( ZXTM zxtm, boolean isError )
   {
      if( runner == null ) {
         runner = new PasswordDialogRunner();
      }
      runner.startAsync( zxtm, isError );
   }
   
   /**
    * Callback function. Run when the model needs the user to re-enter their 
    * password.
    */
   /* Override */
   public void passwordRequired( ZXTM zxtm, boolean isError )
   {
      ZDebug.print( 5, "passwordRequired( ", zxtm, ", ", isError, " )" );      
      this.showDialog( zxtm, isError );
   }

   /**
    * Class which creates the dialog an updates the password in the model.
    */
   private static class PasswordDialogRunner extends AsyncExec 
   {   
      /**
       * The main run function. Displays the password dialog and waits for the 
       * user to fill it in and close it. Then performs the selected action 
       * (either disconnecting the ZXTM or updating the password)
       */
      /* Override */
      protected void runAsync( Object[] userData )
      {
         ZXTM zxtm = (ZXTM) userData[0];
         boolean isError = (Boolean) userData[1];
         
         // Setup the message
         String title, message;
         if( isError ) {
            title = ZLang.ZL_IncorrectPasswordTitle;
            message = ZLang.ZL_IncorrectPasswordMessage;
         } else {
            title = ZLang.ZL_NeedPasswordTitle;
            message = ZLang.ZL_NeedPasswordMessage;
         }      
         
         // We may need to try again if ZXTM doesn't like the new password.
         boolean retry = true;
         while( retry ) {
            retry = false;
            
            // Display the dialog
            ZDebug.print( 5, "Updating password..." );
            PasswordResult result = ZDialog.showPasswordDialog( title, message, 
               isError ? Icon.WARNING : Icon.QUESTION, zxtm 
            );
            
            if( result.getOption() == DialogOption.OK ) {
               try {
                  // Change the password.
                  SWTUtil.progressBusyCursor( 
                     new ChangeZXTMAuthOp( 
                        zxtm, result.getUserName(), result.getPassword(),
                        result.getStorePassword()
                     )
                  );        
              
               // Something went wrong (e.g wrong password) show the dialog
               // again with the error.
               } catch( Exception e ) {
                  retry = true;
                  title = ZLang.ZL_ErrorWhilstChangingPasswordTitle;
                  isError = true;
                  message = ZLang.bind( 
                     ZLang.ZL_ErrorWhilstChangingPasswordMessage,
                     e.getCause().getLocalizedMessage() 
                  );
               }
            
            // The user wants us to close the project and disconnect the ZXTM.
            } else {
               ZXTMPlugin.getDefault().getProjectManager().closeProjectForZXTM( zxtm );         
            }
         } 
      }
      
      
   }
    
}
