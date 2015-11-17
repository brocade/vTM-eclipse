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

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.model.ZXTM;

/**
 * Control which is used to change a ZXTM settings (hostname, port and 
 * password).
 */
public class ZXTMControl
{
   private Composite parent;
   private Text textHostname, textPort, textUsername, textPassword;
   private Button checkStorePW;
   
   private String error, message;
   private boolean finished = false;
   private ZXTMControlListener zxtmListener = new ZXTMControlListener();
   private VerifyListener listener;
   
   private String hostname, username, password;
   private boolean storePW;
   private int port;
   
   private SWTSet controlSet;
   
   /**
    * Create the control in the specified composite.
    * @param parent The composite to add this control to. 
    * @param listener The verify listener that is called when this control is 
    * modified. Note that the listener callback will be called with null for
    * the event.
    * @param zxtm The ZXTM this is altering, or null to show blank settings.
    */
   public ZXTMControl( Composite parent, VerifyListener listener, ZXTM zxtm )
   {
      this.parent = parent;
      this.listener = listener;
      
      SWTSet hostSet = SWTUtil.addLabeledText( parent, ZLang.ZL_HostnameLabel, SWTUtil.FILL );
      textHostname = hostSet.text();
      if( zxtm != null ) textHostname.setText( zxtm.getHostname() );
      textHostname.addModifyListener( zxtmListener );
      
      SWTSet portSet = SWTUtil.addLabeledText( parent, ZLang.ZL_PortLabel, 100 );
      textPort = portSet.text();
      if( zxtm != null ) {
         textPort.setText( "" + zxtm.getAdminPort() );
      } else {
         textPort.setText( "9090" );
      }
      textPort.addModifyListener( zxtmListener );
      
      SWTSet usernameSet = SWTUtil.addLabeledText( parent, ZLang.ZL_AdminUserLabel, 200 );
      textUsername = usernameSet.text();
      if( zxtm != null ) {
         textUsername.setText( zxtm.getUserName() );
      } else {
         textUsername.setText( "admin" );
      }
      textUsername.addModifyListener( zxtmListener );
      
      SWTSet passwordSet = SWTUtil.addLabeledPasswordText( parent, ZLang.ZL_AdminPasswordLabel, SWTUtil.FILL );
      textPassword = passwordSet.text(); 
      if( zxtm != null ) textPassword.setText( zxtm.getPassword() );
      textPassword.addModifyListener( zxtmListener );
      
      SWTUtil.createBlankGrid( parent );      
      checkStorePW = SWTUtil.addCheckButton( parent, ZLang.ZL_StorePasswordWithProject, false );
      if( zxtm != null ) checkStorePW.setSelection( zxtm.getStorePassword() );
      checkStorePW.addSelectionListener( zxtmListener );
      
      Label warn = SWTUtil.addLabel( parent, ZLang.ZL_YourPasswordIsStoredLocally ); 
      SWTUtil.gridDataFillHorizontal( warn );
      SWTUtil.gridDataColSpan( warn, 2 );
      SWTUtil.gridDataPreferredWidth( warn, 150 );
      
      controlSet = new SWTSet(
         hostSet.item(0), hostSet.item(1), portSet.item(0), portSet.item(1),
         passwordSet.item(0), passwordSet.item(1), checkStorePW, warn,
         usernameSet.item(0), usernameSet.item(1)
      );
      
      zxtmListener.update();
   }
   
   /**
    * Get the parent composite for this control.
    * @return The parent composite for this control.
    */
   public Composite getParent()
   {
      return parent;
   }

   /**
    * Get the current problem with what the user has entered.
    * @return The current problem with what the user has entered.
    */
   public String getError()
   {
      return error;
   }

   /**
    * Get the current message, telling the user which field needs to be 
    * completed next. 
    * @return The message string.
    */
   public String getMessage()
   {
      return message;
   }

   /**
    * Is all the information for this control filled in and valid?
    * @return True if this information is OK to apply.
    */
   public boolean isFinished()
   {
      return finished;
   }
   
   /**
    * Get the entered hostname / IP address.
    * @return The entered hostname / IP address.
    */
   public String getHostname()
   {
      return hostname;
   }

   /**
    * Get the password entered by the user into this control.
    * @return The entered password.
    */
   public String getPassword()
   {
      return password;
   }
   
   /**
    * Get the user-name that was entered by the user.
    * @return The entered user-name.
    */
   public String getUserName()
   {
      return username;
   }

   /**
    * Did the user opt to store the password locally?
    * @return True if the password should be stored locally, false otherwise.
    */
   public boolean getStorePassword()
   {
      return storePW;
   }

   /**
    * Returns the admin port entered by the user.
    * @return The admin port the user entered.
    */
   public int getPort()
   {
      return port;
   }

   /**
    * Get all the sub-controls in this control as a SWT set.
    * @return The sub-controls that make up this ZXTMControl.
    */
   public SWTSet getControlSet()
   {
      return controlSet;
   }
   
   /**
    * Set the current hostname.
    * @param host The hostname or IP address to put into the appropriate text
    * box.
    */
   public void setHostname( String host )
   {
      textHostname.setText( host );
   }
   
   /**
    * Set the current port.
    * @param host The admin port to put into the appropriate text box.
    */
   public void setPort( int port ) 
   {
      textPort.setText( "" + port );
   }

   /**
    * Listens for modifications to the controls form, and updates 
    * errors / messages accordingly. Then runs the VerifyListeners callback
    * method. 
    */
   private class ZXTMControlListener implements ModifyListener, SelectionListener
   {
      /**
       * Updates the error, message and finished fields.
       */
      public void update()
      {
         ZDebug.print( 5, "update()" );
         
         hostname = textHostname.getText();
         password = textPassword.getText();
         username = textUsername.getText();
         storePW = checkStorePW.getSelection();
         
         try {
            port = Integer.parseInt( textPort.getText() );
         } catch( NumberFormatException e ) {
            port = -1;
         }
         
         if( textHostname.getText().equals( "" ) ) {
            message = ZLang.ZL_EnterDetailsAboutZXTM;
            finished = false;
            error = null;
            return;
         }
         
         String host = textHostname.getText();
         error = ZUtil.validateHostname( host );
         if( error != null ) {
            finished = false;
            return;       
         } 
         
         if( textPort.getText().equals( "" ) ) {
            message = ZLang.ZL_EnterDetailsAboutZXTM;
            finished = false;
            error = null;
            return;
         } 
         
         int port;
         try {
            port = Integer.parseInt( textPort.getText() );
            
            if( port <= 0 || port > 65535 ) {
               error = ZLang.ZL_ValidatePortMustBeInRange;
               finished = false;
               return;     
            }
         } catch( NumberFormatException e ) {
            error = ZLang.ZL_ValidatePortMustBeANumber;
            finished = false;
            return;     
         }
         
         // Check user name

         if( textUsername.getText().equals( "" ) ) {
            message = ZLang.ZL_EnterTheAdminUser;
            finished = false;
            error = null;
            return;
         } 
         
         error = ZUtil.validateUserName( username );
         if( error != null ) {
            finished = false;
            return;       
         }
                           
         // Check password
         if( textPassword.getText().equals( "" ) ) {
            message = ZLang.ZL_EnterTheAdminPassword;
            finished = false;
            error = null;
            return;
         } 
         
         finished  = true;
         error = null;
         message = ZLang.ZL_VerifyTheseAreTheSettingsYouWant;
      }

      /* Override */
      public void modifyText( ModifyEvent e )
      {
         ZDebug.print( 4, "modifyText( ", e, " )" );
         update();
         listener.verifyText( null );
      }

      /* Override */
      public void widgetDefaultSelected( SelectionEvent e )
      {
         ZDebug.print( 4, "widgetDefaultSelected( ", e, " )" );
         update();
         listener.verifyText( null );
      }

      /* Override */
      public void widgetSelected( SelectionEvent e )
      {
         ZDebug.print( 4, "widgetSelected( ", e, " )" );
         update();
         listener.verifyText( null );
      }
      
   }

   
}
