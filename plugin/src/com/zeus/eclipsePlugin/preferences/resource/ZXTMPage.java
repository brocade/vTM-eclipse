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

package com.zeus.eclipsePlugin.preferences.resource;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

import com.zeus.eclipsePlugin.Encrypter;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.ProjectManager;
import com.zeus.eclipsePlugin.project.ProjectProperties;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.project.operations.ChangeZXTMAuthOp;
import com.zeus.eclipsePlugin.project.operations.UpdateProjectZXTMConfOp;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.ZXTMControl;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;

/**
 * UI page that allows editing ZXTM settings from the project properties page.
 */
public class ZXTMPage extends PropertyPage
{
   private IProject project;
   
   private StatusListener status = new StatusListener();
   private ZXTMControl zxtmControl;
   
   /** Disable the default and apply buttons. */ 
   public ZXTMPage()
   {
      this.noDefaultAndApplyButton();
   }
   
   /**
    * Create the contents of the page, uses the ZXTMControl.
    */
   /* Override */
   protected Control createContents( Composite parent )
   {
      ZDebug.print( 4, "createContents( ", parent, " )" );
      
      if( this.getElement() instanceof IProject ) {
         project = (IProject) this.getElement();         
      } else {
         return new Composite( parent, SWT.NONE );
      }
      
      Composite mainComposite = SWTUtil.createGridLayoutComposite( parent, 2 );
      SWTUtil.gridDataFillHorizontal( mainComposite );
      SWTUtil.removeLayoutMargins( (GridLayout) mainComposite.getLayout() );
      
      zxtmControl = new ZXTMControl( mainComposite, status, 
         ZXTMProject.getZXTMForProject( project )
      );
      
      status.update();
      
      return mainComposite;
   }
   
   

   /**
    * Commit the changes made in the UI.
    */
   /* Override */
   public boolean performOk()
   {
      ProjectProperties properties = ZXTMProject.getProjectProperties( project );
      ZXTM zxtm = ZXTMProject.getZXTMForProject( project );
      ProjectManager projectManager = ZXTMPlugin.getDefault().getProjectManager();
      
      // If there's an active ZXTM for this project..
      if( zxtm != null ) {
         String hostname = zxtmControl.getHostname().trim();
         int port = zxtmControl.getPort();
         String password = zxtmControl.getPassword();
         String username = zxtmControl.getUserName();
         
         // If the user has changed the hostname or port, we need to delete the
         // old ZXTM and create a new one. This is done using a progress
         // operation.
         if( !zxtm.getHostname().equals( hostname ) || port != zxtm.getAdminPort() ) {
            ZDebug.print( 5, "Host/port change" );
            
            UpdateProjectZXTMConfOp op = new UpdateProjectZXTMConfOp( 
               zxtmControl.getHostname(), 
               zxtmControl.getUserName(),
               zxtmControl.getPassword(),
               zxtmControl.getPort(),
               zxtmControl.getStorePassword(),
               project                  
            );
            
            try {
               SWTUtil.progressDialog( op );
            } catch( InvocationTargetException e ) {
               ZDebug.printStackTrace( e, "Update conf op failed ", zxtm );
               ZDialog.showErrorDialog( ZLang.ZL_ConfigUpdateFailedTitle,
                  ZLang.bind( ZLang.ZL_ConfigUpdateFailedMessage,
                     ZUtil.getRootCauseMessage( e )
                  )
               );
               return false;
            }
            
         // If the user has just changed the password settings, update the ZXTM
         // settings and test them using a progress operation.
         } else {
            ZDebug.print( 5, "Password/User change" );
                       
            try {
               SWTUtil.progressBusyCursor( 
                  new ChangeZXTMAuthOp( 
                     zxtm, username, password, zxtmControl.getStorePassword()
                  )
               ); 
            } catch( InvocationTargetException e ) {
               ZDebug.printStackTrace( e, "Update password failed - ", zxtm );
               ZDialog.showErrorDialog( ZLang.ZL_ConfigUpdateFailedTitle,
                  ZLang.bind( ZLang.ZL_ConfigUpdateFailedMessage,
                     ZUtil.getRootCauseMessage( e )
                  )
               );
               return false;
            }
            
            projectManager.update( false );
            
         }
      
      // No ZXTM to change, just alter the settings on disk.
      } else {
         ZDebug.print( 5, "Could not find ZXTM change" );
         
         // We are messing with project stuff, so get the project lock
         synchronized( projectManager )
         {
            properties.set( ProjectProperties.HOSTNAME_KEY, zxtmControl.getHostname().trim() );
            properties.set( ProjectProperties.PORT_KEY, "" + zxtmControl.getPort() );
            properties.set( ProjectProperties.USERNAME_KEY, zxtmControl.getUserName() );
            
            if( zxtmControl.getStorePassword() ) {
               properties.set( ProjectProperties.CRYPT_KEY, null );
            } else {
               try {
                  properties.set( ProjectProperties.CRYPT_KEY, 
                     Encrypter.encrypt( zxtmControl.getPassword() ) 
                  );
               } catch( RuntimeException e ) {
                  ZDebug.printStackTrace( e, "Properties change failed" );
               }
            }
            
            projectManager.update( false );
         }
      }
      
      
      return true;
   }

   /**
    * Listens for changes to the settings UI. Updates error messages on the UI
    * if there are problems.
    */
   private class StatusListener implements ModifyListener, SelectionListener, VerifyListener
   {
      /**
       * Updates the pages error messages if there is a problem and disables the
       * OK and apply buttons. Hides the error and enabled apply/OK otherwise.
       */
      public void update()
      {
         ZDebug.print( 5, "update()" );
         
         if( ! zxtmControl.isFinished() ) {
            String error = zxtmControl.getError();
            if( error != null ) {
               setErrorMessage( error );
               setValid( false );
               return;
            } 
            
            if( zxtmControl.getHostname().trim().length() == 0 ) {
               setErrorMessage( ZLang.ZL_HostnameCannotBeEmpty );
            } else if( zxtmControl.getUserName().trim().length() == 0 ) {
               setErrorMessage( ZLang.ZL_UsernameCannotBeEmpty );
            } else if( zxtmControl.getPassword().trim().length() == 0 ) {
               setErrorMessage( ZLang.ZL_PasswordCannotBeEmpty );
            } else if( zxtmControl.getPort() == -1 ) {
               setErrorMessage( ZLang.ZL_PortCannotBeEmpty );
            }
            
            setValid( false );
            return;
         }
                  
         setErrorMessage( null );        
         setValid( true );
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

      /* Override */
      public void modifyText( ModifyEvent e )
      {
         update();
      }
      
   }

}
