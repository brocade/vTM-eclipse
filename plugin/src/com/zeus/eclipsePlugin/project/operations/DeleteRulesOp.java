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
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.swt.EmptyMonitor;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.DialogOption;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.Icon;
import com.zeus.eclipsePlugin.zxtmview.RuleCopy;

/**
 * Operation that deletes one or more rules. Provides nice dialogs if stuff goes
 * wrong.
 */
public class DeleteRulesOp extends WorkspaceModifyOperation
{
   private Collection<Rule> rules;
   
   /**
    * Setup the operation. 
    * @param rules The rules to delete
    */
   public DeleteRulesOp( Collection<Rule> rules )
   {
      this.rules = rules;
   }

   /**
    * Deletes each rule in sequence. If the deletion fails shows an dialog
    * with options to abort or retry.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.ZL_DeleteOpDeletingSelectedRules, rules.size() );
      
      LinkedList<RuleCopy> backups = new LinkedList<RuleCopy>();
      
      // Loop through the rules to delete
      for( Rule rule : rules ) {
         ZXTM zxtm = (ZXTM) rule.getModelParent();
         monitor.subTask( rule.getName() );
         
         boolean retry = true;
         while( retry ) {
            retry = false;
            
            try {
               // Close the rules editor
               TrafficScriptEditor.closeEditorForRule( rule, false );
               
               backups.add( new RuleCopy( rule.getName(), rule.getRawCode() ) );
               
               // Delete the rule using file deletions if we can, Eclipse likes
               // it better that way
               IFile file = ZXTMProject.getFileForRule( rule );
               if( file != null ) {
                  file.delete( true, null );
               } else {
                  zxtm.deleteRule( rule.getName() );
               }
               
            // Something went wrong, offer them the chance to retry, or just 
            // skip the deletion of this rule.
            } catch( Exception e ) {
               ZDebug.printStackTrace( e, "Delete Rules op failed for rule ", rule );
               
               DialogOption result = ZDialog.showCustomDialog( 
                  ZLang.ZL_DeleteOpDeleteFailedTitle,
                  ZLang.bind( ZLang.ZL_DeleteOpDeleteFailedMessage,
                     rule.getName(), e.getLocalizedMessage() 
                  ),
                  Icon.ERROR, DialogOption.RETRY, DialogOption.RETRY, 
                  DialogOption.SKIP, DialogOption.SKIP_REMAINING
               );
               
               switch( result ) {
                  case RETRY: retry = true; break;
                  case SKIP_REMAINING: return;
               }    
            }
            
            monitor.worked( 1 );
         }
      }
      
      monitor.done();
   }

}
