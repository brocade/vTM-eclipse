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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.editor.MarkerManager;
import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;
import com.zeus.eclipsePlugin.filesystem.ZXTMFileSystem;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.swt.EmptyMonitor;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.DialogOption;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.Icon;

/**
 * Operation that renames a rule, showing a Dialog if the rule is currently 
 * being edited.
 */
public class RenameRuleOp extends WorkspaceModifyOperation
{
   private Rule rule;
   private String newName;
   
   /**
    * Setup the rename operation with the rule object and its new name.
    * @param rule The rule to rename.
    * @param newName The new name of the rule.
    */
   public RenameRuleOp( Rule rule, String newName )
   {
      this.rule = rule;
      this.newName = newName;
   }

   /**
    * Renames the rule, checking if the rule is being edited, and giving the 
    * option to save it. Throws an exception if the rename fails.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.bind( ZLang.ZL_RenameOpRenameRuleFromFooToBar, 
         rule, newName ), 2 
      );
      monitor.subTask( "" );
            
      ZXTM zxtm = (ZXTM) rule.getModelParent();
            
      TrafficScriptEditor editor = TrafficScriptEditor.getEditorForRule( rule );
      ZDebug.print( 5, "Editor for rule: ", editor );
      
      if( editor != null ) {
         boolean save = false;
         if( editor.isDirty() ) {
            DialogOption result = ZDialog.showCustomDialog( 
               ZLang.ZL_RenameOpEditorNeedsToSaveTitle, 
               ZLang.ZL_RenameOpEditorNeedsToSaveMessage, 
               Icon.QUESTION,
               DialogOption.SAVE, DialogOption.SAVE, DialogOption.DISCARD, DialogOption.CANCEL 
            );
            if( result == DialogOption.CANCEL ) return;
            save = (result == DialogOption.SAVE);
         } 
         
         if( save ) {
            editor.doSave( null );
         }
         
         TrafficScriptEditor.closeEditorForRule( rule, save );
      }
      
      monitor.worked( 1 );
      if( monitor.isCanceled() ) {
         return;
      }
      
      try {
         zxtm.renameRule( rule.getName(), newName );
         
         if( editor != null ) {
            IProject project = ZXTMProject.getProjectForZXTM( zxtm );
            if( project != null ) {
               IFolder folder = project.getFolder( ZXTMFileSystem.RULE_PATH );            
               folder.refreshLocal( IResource.DEPTH_ONE, monitor );
            }
               
            MarkerManager.update( rule );
            TrafficScriptEditor.openEditorForRule( rule );
         }
      } catch( Exception e ) {         
         throw new WorkbenchException( ZLang.ZL_RenameOpFailed, e ); 
      }  
      
      monitor.worked( 1 );
      monitor.done();
   }

}
