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

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.codedata.VersionCodeData;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;

/**
 * This is the scanner for the actual TrafficScript code. It applies colouring
 * and styles to an editor's text.
 */
public class TrafficScriptCodeScanner extends ZXTMScanner
{
   private int currentMajor = -1, currentMinor = -1;
   private VersionCodeData version;
   
   /**
    * Create the scanner.
    * @param editor The TrafficScript editor this scanner is working on.
    */
   public TrafficScriptCodeScanner( TrafficScriptEditor editor )
   {      
      super( editor );
      ZDebug.print( 3, "TrafficScriptCodeScanner( ", editor, " )" );
   }
   
   /**
    * This method is called when the we need to recreate the scanning rules. 
    * It is called when the scanner is created and when colour preferences
    * change.
    */
   /* Override */
   protected void update()
   {
      ZDebug.print( 4, "update()" );
      
      LinkedList< IRule > rules = new LinkedList< IRule >();
      
      this.setDefaultReturnToken( getTokenForPreference( Preference.COLOUR_DEFAULT ) );
      
      // Add whitespace matcher
      rules.add( new WhitespaceRule( new WhiteSpaceDetector() ) );
      
      // Add variables
      WordRule variableRule = new WordRule( 
         new VariableDetecter(), 
         getTokenForPreference( Preference.COLOUR_VARIABLE ) 
      );
      rules.add( variableRule );
      
      // Add numbers
      WordRule numberRule = new WordRule( 
         new NumberDetector(), 
         getTokenForPreference( Preference.COLOUR_NUMBER ) 
      );
      rules.add( numberRule );
     
      // Add TrafficScript code objects
      if( version != null ) {
         rules.add( new TrafficScriptWordRule( 
            version, 
            getTokenForPreference( Preference.COLOUR_FUNCTION ), 
            getTokenForPreference( Preference.COLOUR_KEYWORD ),
            getTokenForPreference( Preference.COLOUR_DEPRECATED ),
            getTokenForPreference( Preference.COLOUR_DEFAULT ) 
         ) );
      }
      
      // Set the rules
      setRules( rules.toArray( new IRule[rules.size()] ) );      
   }

   /**
    * This method is used to check if the scanner needs an update.
    * @return True if the scanner needs an update.
    */
   /* Override */
   protected boolean needUpdate()
   {
      version = editor.getCodeDataVersion();
      int major = version.getMajorVersion();
      int minor = version.getMinorVersion();
      
      if( major == currentMajor && minor == currentMinor ) {
         ZDebug.print( 5, "We have setup for this version already." );
         return false;
      }
      
      currentMajor = major;
      currentMinor = minor;
      
      return true;
   }


      

}
