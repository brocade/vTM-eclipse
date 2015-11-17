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

package com.zeus.eclipsePlugin;

import java.util.HashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.zeus.eclipsePlugin.consts.Colour;

/**
 * Class that manages colours for the ZXTM Plug-in
 */
public class ColourManager
{     
   private HashMap< RGB, Color > localColorTable = 
      new HashMap< RGB, Color >( 100 );
   
   /**
    * Disposes of stored colours (frees up memory).
    */
   public void dispose() 
   {
      for( Color colour : localColorTable.values() ) {
         colour.dispose();
      }
   }
   
   /**
    * Get the colour for a particular ID.
    * @param colourConst The Colour enum you want the colour for.
    * @return The corresponding colour.
    */
   public Color getColour( Colour colourConst ) {
      RGB colour = colourConst.getRGB();         
      return getLocalColor( colour );      
   }
      
   /**
   * Return the colour that is stored in the colour table under the given RGB
   * value.
   * @param rgb the RGB value
   * @return the colour stored in the colour table for the given RGB value
   */
  public Color getLocalColor( RGB rgb )
   {
      Color color = localColorTable.get( rgb );      
      
      if( color == null ) {
         color = new Color( Display.getCurrent(), rgb );
         localColorTable.put( rgb, color );
      }
      return color;
   }
   
}
