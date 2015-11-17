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

package com.zeus.eclipsePlugin.editor;

import org.eclipse.jface.text.Region;

/**
 * A class that stores region information and some text.
 */
public class CodeRegion extends Region
{
   private String text;
   
   /**
    * Create a code region.
    * @param text The text to store with the region information.
    * @param offset The offset into the code.
    * @param length The length of the region.
    */
   public CodeRegion( String text, int offset, int length )
   {
      super( offset, length );
      this.text = text;
   }

   /**
    * The stored text
    * @return The text stored with this region.
    */
   public String getText()
   {
      return text;
   }
   
}