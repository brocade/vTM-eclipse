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

package com.zeus.eclipsePlugin.project.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.swt.EmptyMonitor;

/**
 * This operation changes the password of a ZXTM. Will throw an exception if the
 * password is rejected by the ZXTM.
 */
public class ChangeZXTMAuthOp extends WorkspaceModifyOperation
{
   private ZXTM zxtm;
   private String user, password;
   private boolean store;
   
   /**
    * Setup the change password operation.
    * @param zxtm The ZXTM who's password you are changing.
    * @param user The user to authenticate with. 
    * @param password The new password value.
    * @param store Should the password be stored locally?
    */
   public ChangeZXTMAuthOp( ZXTM zxtm, String user, String password, boolean store )
   {
      this.zxtm = zxtm;
      this.user = user;
      this.password = password;
      this.store = store;
   }

   /**
    * Attempts to update the password, throws an exception if it fails.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.bind( ZLang.ZL_ChangingPasswordForZXTM, zxtm ), 2 );
      monitor.subTask( ZLang.ZL_ConnectingToZXTM );
      
      try {
         synchronized( ZXTMPlugin.getDefault().getProjectManager() ) {
            zxtm.setUserAndPassword( user, password );
            zxtm.setStorePassword( store );  
            monitor.worked( 1 );           
                    
            monitor.subTask( ZLang.ZL_UpdatingProjectSettings );            
            ZXTMPlugin.getDefault().getProjectManager().update( false );
            monitor.worked( 1 );
         }
         
      } catch( Exception e ) {
         throw new WorkbenchException( e.getLocalizedMessage(), e );
      }     
      monitor.done();
   }

}
