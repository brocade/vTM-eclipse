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

import java.util.EnumMap;
import java.util.LinkedList;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;
import com.zeus.eclipsePlugin.swt.ColourSetting;

/**
 * Super class for all scanners used in this plug-in. It updates the rules in 
 * all of its sub classes when colour and task preferences change.
 */
public abstract class ZXTMScanner extends RuleBasedScanner
{
   protected TrafficScriptEditor editor;
   private boolean invalidated = false;
   private boolean disposed = false;
   
   private static EnumMap<Preference,IToken> tokenMap = new EnumMap<Preference,IToken>( Preference.class );
   private static LinkedList<ZXTMScanner> scanners = new LinkedList<ZXTMScanner>();
   
   /**
    * Create a scanner for the passed editor. This will run the update function.
    * @param editor The editor this scanner scans for.
    */
   protected ZXTMScanner( TrafficScriptEditor editor )
   {
      this.editor = editor;
      scanners.add( this );
      update();
   }
   
   /**
    * Should be called when this scanner is no longer being used. Will stop the
    * scanner being updated when preferences change. 
    */
   public void dispose()
   {
      scanners.remove( this );
      disposed = true;
   }
   
   /** 
    * This function should be used by sub-classes to update the scanner 
    * rules. 
    */ 
   protected abstract void update();
   
   /**
    * Force the all scanners to update their rules when they are next used. 
    */
   public static synchronized void invalidateAllScanners()
   {
      tokenMap.clear();
      for( ZXTMScanner scanner : scanners ) {
         scanner.invalidate();
      }
   }
   
   /**
    * Invalidate this scanner.
    */
   private synchronized void invalidate()
   {
      invalidated = true;
   }
   
   /**
    * Get a token for the passed preference. MUST be a colour preference. All 
    * sub classes should use this to create their tokens.
    * 
    * @param pref The colour preference you want the appropriate colour for.
    * @return The token for the passed colour.
    */
   protected static synchronized IToken getTokenForPreference( Preference pref )
   {
      IToken token = tokenMap.get( pref );
      if( token == null ) {
         ColourSetting style = new ColourSetting( pref );   
         token = style.createToken();
         tokenMap.put( pref, token );
      } 
      
      return token;
   }
   
   /**
    * This method is designed to be overridden by the sub class if it needs to 
    * be invalidated under certain circumstances. It is checked every time the
    * scanner is used.
    * @return True if the scanner needs an update. Always returns false.
    */
   protected boolean needUpdate() 
   {
      return false;
   }
   
   /**
    * This method is overridden so we can check if this scanner needs an update.
    */
   /* Override */
   public void setRange( IDocument document, int offset, int length )
   {
      ZDebug.print( 4, "setRange( ", document, ", ", offset, ", ", length );
      if( !disposed && (invalidated || needUpdate()) ) {
         update();
         invalidated = false;
      }
      super.setRange( document, offset, length );
   }

   
   
}
