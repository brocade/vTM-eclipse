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
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.zxtmview.ZXTMViewer;

/**
 * This action creates a new rule. It invokes the NewRuleWizard, passing it the
 * currently selected rule in the ZXTM Viewer (if one is selected)
 */
public class PasteRuleAction extends ZAction
{
   private ZXTM zxtm = null;
   private ModelSelection selection = null;
   
   /** 
    * Standard constructor. The action will find the selection from the ZXTM
    * Viewer when it is activated. 
    */
   public PasteRuleAction() {}
   
   /**
    * Constructor that stores a selection. This is used by the action every time
    * is is run. This also sets up the text and image of the Action.
    * @param selection The selection to use every time this action is run.
    */
   public PasteRuleAction( ModelSelection selection )
   {
      if( selection != null && selection.getSelectedZXTM() != null  ) {
         this.selection = selection;
         
         this.setText( ZLang.ZL_Paste );
         this.setImageFile( ImageFile.PASTE );
         this.zxtm = selection.getSelectedZXTM();
                  
      } else {
         // Should not use this constructor without a valid selection
         ZDebug.dumpStackTrace( "PasteRuleAction created without proper selection." ); 
         this.setText( "!!" );
      }
   }
   
   /**
    * Get the command we are using (in this case the standard copy command)
    */
   /* Override */
   protected Command getCommand()
   {
      return Command.EDIT_PASTE;
   }
   
   /**
    * Run the action, pasting the rules into the selected ZXTM.
    */
   /* Override */
   protected void run( ExecutionEvent event )
   {
      ModelSelection currentSelection = selection;
      ZXTM currentZXTM = zxtm;
      
      if( currentSelection == null || currentZXTM == null ) {
         currentSelection = ZXTMViewer.getSelectionForOpenViewer();
         if( currentSelection == null ) return;
         currentZXTM = currentSelection.getSelectedZXTM();
      }
      
      if( currentZXTM != null ) {          
         ZDebug.print( 3, "Pasting rules to ", currentZXTM );
         ZXTMViewer.pasteRulesFromMemory( currentZXTM );
      }
   }
}
