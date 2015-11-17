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
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.swt.EmptyMonitor;
import com.zeus.eclipsePlugin.swt.dialogs.CustomResult;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.DialogOption;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.Icon;
import com.zeus.eclipsePlugin.zxtmview.RenameValidator;
import com.zeus.eclipsePlugin.zxtmview.RuleCopy;

/**
 * Operation that adds a set of rules to a ZXTM. The rules are in RuleCopy 
 * objects and do not come directly from another ZXTM.
 */
public class PasteRulesOp extends WorkspaceModifyOperation
{
   private RuleCopy[] copies;
   private ZXTM zxtm;
   
   private static final DialogOption[] SINGLE_OPTIONS = {
      DialogOption.REPLACE,
      DialogOption.RENAME, 
      DialogOption.SKIP, 
   };
   
   private static final DialogOption[] MULTI_OPTIONS = {
      DialogOption.REPLACE,
      DialogOption.REPLACE_REMAINING,
      DialogOption.RENAME, 
      DialogOption.SKIP, 
      DialogOption.SKIP_REMAINING
   };

   /**
    * Setup the operation with the rule copies and target ZXTM.
    * @param copies The rule copies to add to the ZXTM.
    * @param zxtm The ZXTM to add the rules to.
    */
   public PasteRulesOp( RuleCopy[] copies, ZXTM zxtm )
   {
      this.copies = copies;
      this.zxtm = zxtm;
   }

   /**
    * Adds all the copies in sequence to the ZXTM. Queries the user if it 
    * encounters problems.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.bind( ZLang.ZL_PasteOpAddingRulesTo, zxtm ), copies.length );
      
      boolean replaceAll = false;
      
      // Loop through all the copies
      for( RuleCopy rule : copies ) {
         boolean retry = true;
         while( retry ) {
            retry = false;
            monitor.subTask( ZLang.bind( ZLang.ZL_PasteOpAddingRule, rule.getName() ) );
            
            try {
               String name = rule.getName();
               
               // If rule already exists show a dialog box.
               if( zxtm.getRule( name ) != null ) {
                  
                  // Ask if they want to replace / rename               
                  if( !replaceAll ) {
                     DialogOption result = ZDialog.showCustomDialog(
                        ZLang.ZL_PasteOpRuleAllreadyExistsTitle,                   
                        ZLang.bind( ZLang.ZL_PasteOpRuleAllreadyExistsMessage,
                           name 
                        ), 
                        Icon.QUESTION, DialogOption.REPLACE,
                        (copies.length == 1) ? SINGLE_OPTIONS : MULTI_OPTIONS
                     );
                     
                     if( result == DialogOption.SKIP_REMAINING ) break;                  
                     if( result == DialogOption.SKIP ) continue;
                     if( result == DialogOption.REPLACE_REMAINING ) replaceAll = true;
                     
                     // If they want to rename the rule, show another dialog 
                     // box!
                     if( result == DialogOption.RENAME ) {
                        CustomResult inputResult = ZDialog.showCustomInputDialog( 
                           ZLang.ZL_PasteOpRenameTitle, 
                           ZLang.ZL_PasteOpRenameMessage,
                           Icon.QUESTION, new RenameValidator( zxtm ), name,
                           DialogOption.RENAME, DialogOption.RENAME, DialogOption.SKIP
                        );
                        
                        if( inputResult.getOption() == DialogOption.RENAME ) {
                           name = inputResult.getInput();
                        } else {
                           continue;
                        }
                     }
                  }
               }
               
               // If the old rule exists inject the new code.
               Rule existingRule = zxtm.getRule( name );
               if( existingRule != null ) {
                  
                  existingRule.setCode( rule.getContents() );                 
               
               // Otherwise create a new rule 
               } else {
                  zxtm.addRule( name, rule.getContents() );
               }
           
            // Something went wrong, display a retry/ignore dialog
            } catch( Exception e ) {
               ZDebug.printStackTrace( e, "Paste op failed" );
               
               DialogOption result = ZDialog.showCustomDialog( 
                  ZLang.ZL_PasteOpPasteFailedTitle, 
                  ZLang.bind( ZLang.ZL_PasteOpPasteFailedMessage, e.getMessage() ), 
                  Icon.ERROR, DialogOption.RETRY, DialogOption.RETRY, 
                  DialogOption.SKIP, DialogOption.SKIP_REMAINING
               );
               
               switch( result ) {
                  case RETRY: retry = true; break;
                  case SKIP_REMAINING: return;
               }
               
            }
         }
         
         monitor.worked( 1 );
      }
      
      monitor.done();
   }
   
   

}
