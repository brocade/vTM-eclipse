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

package com.zeus.eclipsePlugin.consts;

import org.eclipse.swt.graphics.RGB;

/**
 * Standard colour constants used throughout the plug-in. 
 * 
 * Note that if you want a colour that can be configured by the user you should
 * create a preference instead.
 */
public enum Colour
{
   // Standard colours
   WHITE                   ( 255, 255, 255 ),
   BLACK                   ( 0, 0, 0 ),
   
   // Fixed colours used by the plug-in.
   TS_ASSIST_BG            ( 255, 255, 255 ),
   TS_NOTIFY_BG            ( 255, 255, 255 ),
   TS_CONTEXT_BG           ( 255, 255, 255 ),
   TS_CONTEXT_NORM         ( 50,  50,  50  ),
   TS_CONTEXT_BOLD         ( 0,   0,   0  ),
   ;
   
   private RGB colour;
   
   /**
    * Create a colour constant, with the passed in RGB values.
    * @param red Colour red component
    * @param green Colour green component
    * @param blue Colour blue component
    */
   private Colour( int red, int green, int blue )
   {
      colour = new RGB( red, green, blue );
   }

   /**
    * Returns the RGB value of this colour. Should use the ColourManager to 
    * create the Color object for it.
    * @return The colour represented by this constant.
    */
   public RGB getRGB()
   {
      return colour;
   }
   
   
   
}
