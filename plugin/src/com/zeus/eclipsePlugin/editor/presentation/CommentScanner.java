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

package com.zeus.eclipsePlugin.editor.presentation;

import java.util.LinkedList;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import com.zeus.eclipsePlugin.PreferenceManager;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.editor.TaskTag;
import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;

/**
 * This is the scanner for the comment parts of the code. It applies code 
 * colouring and text style to the comment partition.
 */
public class CommentScanner extends ZXTMScanner
{
   /**
    * Create a comment scanner for the passed editor.
    * @param editor The TrafficScript editor this scanner updates text 
    * styling for.
    */
   public CommentScanner( TrafficScriptEditor editor )
   {
      super( editor );
   } 

   /**
    * This method is called when the we need to recreate the scanning rules. 
    * It is called when the scanner is created and when colour preferences
    * and task preferences change. 
    */
   /* Override */
   protected void update()
   {
      LinkedList<IRule> rules = new LinkedList<IRule>();
 
      this.setDefaultReturnToken( getTokenForPreference( Preference.COLOUR_COMMENT ) );
      
      // Add whitespace matcher
      rules.add( new WhitespaceRule( new WhiteSpaceDetector() ) );
      
      WordRule taskRule = new WordRule( new WordDetector(), getTokenForPreference( Preference.COLOUR_COMMENT ) );
      
      // Add all the task tags 
      for( String tag : TaskTag.getTaskTagStrings( 
         PreferenceManager.getPreference( Preference.TASK_TAGS ) ) ) 
      {
         taskRule.addWord( tag, getTokenForPreference( Preference.COLOUR_TASK ) );
      }

      rules.add( taskRule );
      
      // Set the rules
      this.setRules( rules.toArray( new IRule[rules.size()] ) );   
   }
   
   
}
