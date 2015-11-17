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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Command;
import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;

/**
 * This causes the content assist suggestions to appear in the active 
 * TrafficScript editor window.
 */
public class ContentAssistAction extends ZAction
{
   /**
    * Get the command we are using (in this case the standard proposal command)
    */
   /* Override */
   protected Command getCommand()
   {      
      return Command.EDITOR_PROPOSALS;
   }

   /**
    * Run the action (find the current active editor window if there is one, and
    * if it's a TrafficScript editor activate the completions popup.
    */
   /* Override */
   protected void run( ExecutionEvent event )
   {
      if( !ZXTMPlugin.isEclipseLoaded() ) return;
      
      // Find the editor
      IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      if( window == null ) return;
      
      IWorkbenchPage page = window.getActivePage();
      if( page == null ) return;
      
      IEditorPart editor = page.getActiveEditor();
      if( editor == null || !(editor instanceof TrafficScriptEditor) ) return;
      
      if( ((TrafficScriptEditor) editor).getRule() != null && 
          ((TrafficScriptEditor) editor).getRule().isRulebuilder() ) return;
      
      // Show the completions.
      ((TrafficScriptEditor) editor).getAssistant().showPossibleCompletions();    
   }

}
