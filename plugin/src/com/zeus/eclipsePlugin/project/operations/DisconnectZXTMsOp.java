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
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.swt.EmptyMonitor;

/**
 * Operation that disconnects or reconnects multiple ZXTMs. Will just throw an
 * exception
 */
public class DisconnectZXTMsOp extends WorkspaceModifyOperation
{
   private Collection<ZXTM> zxtms;
   private boolean disconnect;
   
   /**
    * Setup the disconnect/reconnect operation. 
    * @param zxtms The ZXTMs to delete.
    * @param disconnect If true disconnect the ZXTMs otherwise reconnect.
    */
   public DisconnectZXTMsOp( Collection<ZXTM> zxtms, boolean disconnect )
   {
      this.zxtms = zxtms;
      this.disconnect = disconnect;
   }
   
   /**
    * Disconnect the ZXTMs in sequence. If anything goes wrong the operation
    * throws an exception.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.ZL_DisconnectOpDisconnectingSelected, zxtms.size() );
      
      for( ZXTM zxtm : zxtms ) {
         ZDebug.print( 4, "Dis/reconnecting ", zxtm ); //$NON-NLS-1$
         monitor.subTask( ZLang.bind( 
            disconnect ? ZLang.ZL_DisconnectOPDisconnectZXTM :
                         ZLang.ZL_DisconnectOPReconnectZXTM, zxtm
         ) );
         
         IProject project = ZXTMProject.getProjectForZXTM( zxtm );
         ZDebug.print( 4, "Project: ", project ); //$NON-NLS-1$
         
         if( disconnect ) {            
            try {
               project.close( null );
               
            } catch ( CoreException e ) {
               throw new WorkbenchException( ZLang.bind(
                     ZLang.ZL_DisconnectOpDisconnectFailed,
                     zxtm, e.getMessage() 
                  ), e  
               );
            }
         
         } else {           
            try {
               project.open( null );
               
            } catch ( CoreException e ) {
               throw new WorkbenchException( ZLang.bind(
                  ZLang.ZL_DisconnectOpReconnectFailed,
                  zxtm, e.getMessage() 
               ), e  
            );
            }
         }
         
         monitor.worked( 1 );
      }
      
      monitor.done();      
   }

}
