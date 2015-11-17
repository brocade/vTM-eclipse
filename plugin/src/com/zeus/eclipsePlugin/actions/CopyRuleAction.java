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

import org.eclipse.core.commands.ExecutionEvent;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.consts.Command;
import com.zeus.eclipsePlugin.consts.ImageFile;
import com.zeus.eclipsePlugin.model.ModelSelection;
import com.zeus.eclipsePlugin.zxtmview.ZXTMViewer;

/**
 * This action copies the selected rules into memory (not the clip-board
 * however).
 */
public class CopyRuleAction extends ZAction
{
   private ModelSelection selection = null;
      
   /** 
    * Standard constructor. The action will find the selection from the ZXTM
    * Viewer when it is activated. 
    */
   public CopyRuleAction() {}
   
   /**
    * Constructor that stores a selection. This is used by the action every time
    * is is run. This also sets up the text and image of the Action.
    * @param selection The selection to use every time this action is run.
    */
   public CopyRuleAction( ModelSelection selection )
   {
      if( selection != null && selection.isOnlyRules()  ) {
         this.selection = selection;
         
         this.setText( ZLang.ZL_Copy );    
         this.setImageFile( ImageFile.COPY );
                  
      } else {
         // Should not use this constructor without a valid selection
         ZDebug.dumpStackTrace( "CopyRuleAction created without proper selection." ); 
         this.setText( "!!" );
      }
   }
   
   /**
    * Get the command we are using (in this case the standard copy command)
    */
   /* Override */
   protected Command getCommand()
   {
      return Command.EDIT_COPY;
   }
      
   /**
    * Run the action, copying the selected rules to memory in RuleCopy objects.
    */
   /* Override */
   protected void run( ExecutionEvent event )
   {
      ModelSelection currentSelection = selection;
      
      // No selection, check if the ZXTM Viewer has one currently.
      if( currentSelection == null ) {
         currentSelection = ZXTMViewer.getSelectionForOpenViewer();
      }
      
      if( currentSelection != null && currentSelection.isOnlyRules()  ) {      
         ZXTMViewer.copyRules( currentSelection );
      }
   }

}
