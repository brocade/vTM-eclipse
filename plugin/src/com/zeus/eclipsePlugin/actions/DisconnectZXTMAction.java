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

package com.zeus.eclipsePlugin.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.consts.Command;
import com.zeus.eclipsePlugin.consts.ImageFile;
import com.zeus.eclipsePlugin.model.ModelSelection;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.model.ModelElement.State;
import com.zeus.eclipsePlugin.project.operations.DisconnectZXTMsOp;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;
import com.zeus.eclipsePlugin.zxtmview.ZXTMViewer;

/**
 * This action disconnects or reconnects one or more ZXTM.
 */
public class DisconnectZXTMAction extends ZAction
{
   private Collection<ZXTM> zxtms;
   
   // Disconnect all ZXTMs on true, reconnect on false.
   private boolean disconnect; 
   
   /** 
    * Standard constructor. The action will find the selection from the ZXTM
    * Viewer when it is activated. 
    */
   public DisconnectZXTMAction() {}
   
   /**
    * Constructor that stores a selection. This is used by the action every time
    * is is run. This also sets up the text and image of the Action.
    * @param selection The selection to use every time this action is run.
    */
   public DisconnectZXTMAction( ModelSelection selection )
   {
      if( selection != null && selection.isOnlyZXTMs() && selection.getSize() > 0 ) {
         this.zxtms = selection.getSelectedZXTMs();
         
         // If only one ZXTM, alter text accordingly
         if( zxtms.size() == 1 ) {            
            if( selection.firstZXTM().getModelState() == State.DISCONNECTED ) {
               disconnect = false;
               this.setText( ZLang.ZL_ReconnectZXTM );
               this.setImageFile( ImageFile.ZXTM );
            } else {
               disconnect = true;
               this.setText( ZLang.ZL_DisconnectZXTM );
               this.setImageFile( ImageFile.ZXTM_GREY );
            }
            
         // If more than one ZXTM, reconnect is used if ANY of the ZXTMs are 
         // disconnected
         } else {
            disconnect = true;
            for( ZXTM zxtm : zxtms ) {
               if( zxtm.getModelState() == State.DISCONNECTED ) {
                  disconnect = false;
                  break;
               }
            }
            
            if( disconnect ) {
               this.setText( ZLang.ZL_DisconnectZXTMs );
               this.setImageFile( ImageFile.ZXTM_GREY );
            } else {
               this.setText( ZLang.ZL_ReconnectZXTMs );
               this.setImageFile( ImageFile.ZXTM );
            }
         }
      
      } else {
         // Should not use this constructor without a valid selection
         ZDebug.dumpStackTrace( "DisconnectZXTMAction created without proper selection." ); 
         this.setText( "!!" );
      }
   }
   
   /**
    * There is no related command with this action (this is a toggle command)
    */
   /* Override */
   protected Command getCommand()
   {
      return null;
   }
  
   /**
    * Run the action, uses a DisconnectZXTMsOp and a progress dialog to 
    * disconnect or reconnect the ZXTMs.
    */
   /* Override */
   public void run( ExecutionEvent event )
   {
      Collection<ZXTM> currentZXTMs = zxtms;
      
      // No selection, check if the ZXTM Viewer has one currently.
      if( currentZXTMs == null ) {
         ModelSelection selection = ZXTMViewer.getSelectionForOpenViewer();
         if( selection == null ) return;
         currentZXTMs = selection.getSelectedZXTMs();
      }
      
      if( currentZXTMs == null || currentZXTMs.size() == 0 ) {
         return;
      }
      
      // Run the DisconnectZXTMsOp operation in a progress dialog.
      DisconnectZXTMsOp op = new DisconnectZXTMsOp( zxtms, disconnect );
      
      try {
         SWTUtil.progressDialog( op );
      } catch( InvocationTargetException e ) {
         String title = null, message = null;
         if( disconnect ) {
            title = ZLang.ZL_DisconnectFailedTitle;
            message = ZLang.ZL_DisconnectFailedMessage;
         } else {
            title = ZLang.ZL_ReconnectZXTMFailedTitle;
            message = ZLang.ZL_ReconnectZXTMFailedMessage;
         }
         
         ZDialog.showErrorDialog( title,
            ZLang.bind( message, ZUtil.getRootCauseMessage( e ) )
         );
      }
   }
}
