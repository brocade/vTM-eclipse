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

package com.zeus.eclipsePlugin.swt;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import com.zeus.eclipsePlugin.ColourManager;
import com.zeus.eclipsePlugin.PreferenceManager;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.consts.Preference.Format;

/**
 * Wraps around a colour preference and updates the string representation of the
 * style.
 */
public class ColourSetting
{
   private RGB colour;
   private boolean bold, italic, strikethrough, underline; 
   private boolean isDefault;
   private Preference pref;
   
   public static final int RED = 0;
   public static final int GREEN = 1;
   public static final int BLUE = 2;
   public static final int BOLD = 3;
   public static final int ITALIC = 4;
   public static final int STRIKETHROUGH = 5;
   public static final int UNDERLINE = 6;
   
   /**
    * Setup the colour setting with a preference to wrap. Gets the preference's
    * current setting from the preference store and loads it.
    * @param pref The preference this ColourSetting class manages, it must be
    * a COLOUR format preference.
    */
   public ColourSetting( Preference pref )
   {
      if( pref.getFormat() != Format.COLOUR ) {
         throw new RuntimeException( "Preference " + pref + " is not a colour" );
      }
      
      this.pref = pref;
      
      isDefault = PreferenceManager.isPreferenceDefault( pref );      
      String value = PreferenceManager.getPreference( pref );         
            
      loadPreferenceString( value );      
   }
   
   /**
    * Load a preference's colour settings from it's string representation.
    * @param value The string representation to load, must be valid.
    */
   private void loadPreferenceString( String value )
   {
      ZDebug.print( 5, "loadPreferenceString( ", value, " )" );
      value = value.replaceAll( "\\s+", "" );
      
      String[] parts = value.split( "," );
      int[] colourData = new int[parts.length];
      
      for( int i = 0; i < parts.length; i++ ) {
         colourData[i] = Integer.parseInt( parts[i] );
      }
      
      this.colour = new RGB( colourData[RED], colourData[GREEN], colourData[BLUE] );
      this.bold = colourData[BOLD] != 0;
      this.italic = colourData[ITALIC] != 0;
      this.strikethrough = colourData[STRIKETHROUGH] != 0;
      this.underline = colourData[UNDERLINE] != 0;
   }

   /**
    * Get the colour of this setting.
    * @return The setting's colour.
    */
   public RGB getColour()
   {
      return colour;
   }

   /**
    * Set the colour of this setting
    * @param colour The new colour for this setting.
    */
   public void setColour( RGB colour )
   {
      this.colour = colour;
   }

   /**
    * Returns true if this setting's style is bold.
    * @return True if this setting's style is bold.
    */
   public boolean isBold()
   {
      return bold;
   }

   /**
    * Change if this setting's style should be bold or not.
    * @param bold Set this to true to make the setting bold
    */
   public void setBold( boolean bold )
   {
      this.bold = bold;
   }

   /**
    * Returns true if this setting's style is italic.
    * @return True if this setting's style is italic.
    */
   public boolean isItalic()
   {
      return italic;
   }

   /**
    * Change if this setting's style should be italic or not.
    * @param bold Set this to true to make the setting italic
    */
   public void setItalic( boolean italic )
   {
      this.italic = italic;
   }

   /**
    * Returns true if this setting's style is strike-through.
    * @return True if this setting's style is strike-through.
    */
   public boolean isStrikethrough()
   {
      return strikethrough;
   }

   /**
    * Change if this setting's style should be strike-through or not.
    * @param bold Set this to true to make the setting strike-through
    */
   public void setStrikethrough( boolean strikethrough )
   {
      this.strikethrough = strikethrough;
   }

   /**
    * Returns true if this setting's style is underlined.
    * @return True if this setting's style is underlined.
    */
   public boolean isUnderline()
   {
      return underline;
   }

   /**
    * Change if this setting's style should be underlined or not.
    * @param bold Set this to true to make the setting underlined
    */
   public void setUnderline( boolean underline )
   {
      this.underline = underline;
   }
   
   /**
    * Get the preference this class is managing.
    * @return 
    */
   public Preference getPreference()
   {
      return pref;
   }

   /**
    * Is this setting currently set to the default setting?
    * @return True if this setting is currently default.
    */
   public boolean isDefault()
   {
      return isDefault;
   }

   /**
    * Sets if this setting should use the default value.
    * @param isDefault True if this setting should use the default value.
    */
   public void setDefault( boolean isDefault )
   {
      this.isDefault = isDefault;
   }
   
   /**
    * Reset this preference, setting it to use the default value and resetting
    * the colour and style settings to their default values.
    */
   public void resetToDefault()
   {
      isDefault = true;
      loadPreferenceString( pref.getDefault().toString() );
   }

   /**
    * Builds up and returns the string representation of this setting
    * @return The string representation of this settings style/colour.
    */
   public String toPreferenceString()
   {
      StringBuffer buffer = new StringBuffer( 15 );
      buffer.append( colour.red ).append( "," );
      buffer.append( colour.green ).append( "," );
      buffer.append( colour.blue ).append( "," );
      buffer.append( bold ? "1" : "0" ).append( "," );
      buffer.append( italic ? "1" : "0" ).append( "," );
      buffer.append( strikethrough ? "1" : "0" ).append( "," );
      buffer.append( underline ? "1" : "0" );
      
      return buffer.toString();
   }
   
   /**
    * Gets the style of this setting as an SWT const int. Can be used to set
    * the style of SWT fonts.
    * @return The integer representing this settings style values.
    */
   public int getStyleInt()
   {
      int styleInt = 0;
      
      if( bold ) styleInt |= SWT.BOLD;
      if( italic ) styleInt |= SWT.ITALIC;
      
      return styleInt;
   }

   /**
    * Returns the string representation of this setting.
    */
   /* Override */
   public String toString()
   {
      return toPreferenceString();
   }
   
   /**
    * Create a token that styles text. Used by code formatting stuff.
    * @return A token with this settings style information.
    */
   public IToken createToken()
   {
      int styleInt = getStyleInt();
      if( strikethrough ) styleInt |= TextAttribute.STRIKETHROUGH;
      if( underline ) styleInt |= TextAttribute.UNDERLINE;
      
      ColourManager colours = ZXTMPlugin.getDefault().getColourManager();
      
      IToken token = new Token( 
         new TextAttribute( 
            colours.getLocalColor( colour ),  
            null,
            styleInt
         )
      );
      
      return token;
   }
   
   
}
