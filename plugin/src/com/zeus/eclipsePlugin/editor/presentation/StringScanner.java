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

import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;

/**
 * This is the scanner for the string parts of the code. It applies code 
 * colouring and text style to the string partition.
 */
public class StringScanner extends ZXTMScanner
{   
   /**
    * Create a comment scanner for the passed editor.
    * @param editor The TrafficScript editor this scanner updates text 
    * styling for.
    */
   public StringScanner( TrafficScriptEditor editor )
   {
      super( editor );
   }
   
   /**
    * Returns if this string scanner scans single quoted strings. Should be 
    * overridden by subclasses that are quoted.
    * @return True if this scanner works on quoted strings.
    */
   protected boolean isQuoteString() { return false; }

   /**
    * This method is called when the we need to recreate the scanning rules. 
    * It is called when the scanner is created and when colour preferences
    * change.
    */
   /* Override */
   protected void update()
   {
      LinkedList<IRule> rules = new LinkedList<IRule>();      
      this.setDefaultReturnToken( getTokenForPreference( Preference.COLOUR_STRING ) );
      
      // Add the escape character rule (e.g. \n \123)
      rules.add( new EscapeCharacterRule( 
         getTokenForPreference( Preference.COLOUR_ESCAPE ),
         isQuoteString()
      ) );
      
      // Set the rules
      this.setRules( rules.toArray( new IRule[rules.size()] ) );   
   }
   
   
}
