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

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

import com.zeus.eclipsePlugin.ColourManager;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Colour;
import com.zeus.eclipsePlugin.consts.ExternalPreference;

/**
 * A control that wraps a StyledText box. The box can have a sequence of strings
 * added to it each with their own text styles. When update() is called the 
 * StyledText is redrawn with any changes to the styles.
 */
public class TrafficScriptPreview
{
   private StyledText preview;
   private LinkedList<TextSection> textSections = new LinkedList<TextSection>();
   private StringBuffer rawText = new StringBuffer();

   /**
    * Create the control in the specified composite.
    * @param parent The composite to add this control to.
    */
   public TrafficScriptPreview( Composite parent )
   {
      preview = new StyledText( parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.BORDER );
      preview.setEditable( false );
                  
      SWTUtil.fontPreference( preview, ExternalPreference.FONT_EDITOR_TEXT );
      SWTUtil.gridDataColSpan( preview, 2 );
      SWTUtil.gridDataFillVertical( preview );
      SWTUtil.gridDataFillHorizontal( preview );
   }
   
   /**
    * Append a section of text to the preview. The preview is NOT updated until
    * update() is called.
    * @param text The text to append to the preview.
    * @param style The ColourSetting used to style the text in the preview.
    */
   public void addSection( String text, ColourSetting style )
   {
      textSections.add( new TextSection( text, style ) );
      rawText.append( text );
   }
   
   /**
    * Update the preview, re-colouring the text with the all the ColourSettings
    * that have been added to this preview.
    */
   public void update()
   {
      ZDebug.print( 4, "update()" );
      preview.setText( rawText.toString() );
      
      ColourManager colours = ZXTMPlugin.getDefault().getColourManager();
      
      int offset = 0;
      for( TextSection section : textSections )
      {
         try {
            String text = section.getText();
            ColourSetting style = section.getStyle();
            
            StyleRange range = new StyleRange( 
               offset, text.length(), colours.getLocalColor( style.getColour() ),
               colours.getColour( Colour.WHITE ), style.getStyleInt()
            );
            range.underline = style.isUnderline();
            range.strikeout = style.isStrikethrough();
            
            preview.setStyleRange( range );
            
            offset += text.length();
         } catch ( RuntimeException e ) {
            ZDebug.printStackTrace( e, "Failed to style text for preview" );
         }
      }
      
  }
   
   /**
    * Class used to internally store a section of the preview text.
    */
   private class TextSection
   {
      private String text;
      private ColourSetting style;
      private ColourSetting defaultStyle;
      
      /**
       * Creates the TextSection object.
       * @param text The text to style.
       * @param style The style which will be applied to the text.
       */
      private TextSection( String text, ColourSetting style )
      {
         this.text = text;
         this.style = style;
         this.defaultStyle = new ColourSetting( style.getPreference() );
         defaultStyle.resetToDefault();
      }

      /**
       * Get the text that is being styled.
       * @return The text that is being styled.
       */
      public String getText()
      {
         return text;
      }

      /**
       * Get the style that should be displayed in the preview.
       * @return The style that should be displayed in the preview.
       */
      public ColourSetting getStyle()
      {
         if( style.isDefault() ) {
            return defaultStyle;
         } else {
            return style;
         }
      }      
      
      
   }
   
}
