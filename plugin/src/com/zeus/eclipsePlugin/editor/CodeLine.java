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

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.consts.Partition;

/**
 * Class used to describe the various parts of a line of code, such as comments
 * white space and actual code.
 */
public class CodeLine
{
   public int num, length, start, end, codeStart, codeEnd, commentStart, 
              commentEnd;
   
   public Character unterminatedString;
   
   public Region[] stringAreas;

   /* Override */
   public String toString()
   {
      return "Line " + num + ": " + start + " - " + end + " (" + length + ")" +
             " Code: " + codeStart + " - " + codeEnd + 
             " Comment: " + commentStart + " - " + commentEnd +
             " StrNoTerm: " + unterminatedString + "\n" + 
             ZUtil.join( ", ", stringAreas );
   }
   
   /**
    * Get the partition type for the specified offset. The offset must be within 
    * this line.
    * @param offset The offset relative to the beginning of the document.
    * @return The partition that the offset is in, or null if it is outside the 
    * range of this line.
    */
   public Partition getRegionType( int offset )
   {
      if( offset >= start && offset <= commentStart ) {
         for( Region region : stringAreas ) {
            if( offset > region.getOffset() && 
               offset <= region.getOffset() + region.getLength() ) 
            {
               return Partition.STRING;
            }
         }
         return Partition.CODE;
      }
      if( offset > commentStart ) {
         return Partition.COMMENT;
      }
      
      ZDebug.dumpStackTrace( "Offset out of range? ", offset );
      return null;
   }
   

}

