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

package com.zeus.eclipsePlugin.zxtmview;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;

import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.Rule;

/**
 * Class that handles double clicks, opening the appropriate editor if clicking
 * on a rule.
 */
public class ZXTMViewInputManager implements IDoubleClickListener
{

   /**
    * The callback function for the double click listener. Finds what was 
    * clicked on, if its a rule show it in the editor (or focus on the already
    * open editor).
    */
   /* Override */
   public void doubleClick( DoubleClickEvent event )
   {
       if( event.getSelection() instanceof TreeSelection ) {
          TreeSelection selected = (TreeSelection) event.getSelection();
          if( selected.getFirstElement() != null ) {
             switch( ((ModelElement) selected.getFirstElement()).getModelType() ) {
                
                case RULE:
                   TrafficScriptEditor.openEditorForRule( (Rule) selected.getFirstElement() );
                break;
                
             }
          }
       }
   }

}
