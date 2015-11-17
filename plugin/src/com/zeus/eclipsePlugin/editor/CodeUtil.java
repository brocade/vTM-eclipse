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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.consts.Partition;


/**
 * Utilities for parsing TrafficScript code.
 */
public class CodeUtil
{
   private static HashMap<String,Partition> partitionMap = null;
   
   /** 
    * Enumeration for the different parts of a line of code; the comments, 
    * strings and the code itself.
    */
   public enum AreaType {
      CODE,
      STRING,
      COMMENT
   }
   
   /**
    * Get the partition enum for a particular partition ID.
    * @param name The ID of the partition
    * @return The partition enum that corresponds to the ID, or null if it isn't
    * a TrafficScript partition type.
    */
   public synchronized static Partition getPartition( String name )
   {
      if( partitionMap == null ) {
         partitionMap = new HashMap<String,Partition> ();
         for( Partition partition : EnumSet.allOf( Partition.class ) ) {
            partitionMap.put( partition.getId(), partition );
         }
      }
      return partitionMap.get( name );
   }
   
   public static boolean isStringChar( char c ) 
   {
      return c == '"' || c == '\'';
   }
   
   /**
    * Process a line of code and return a CodeLine object, which lists the 
    * various sections in a line of code.
    * 
    * @param line The entire line NOT including the newline.
    * @param offset All areas will be offset by this value. Useful if you want
    * the positions returned to be relative to the start of a file.
    * @param inString If this line starts in a String (i.e a multiline string)
    * @return A CodeLine object listing the different areas of a line.
    */
   public static CodeLine getLineAreas( String line, int offset, Character inString ) 
   {
      ZDebug.print( 3, "getLineAreas( '", line, "', ", offset, ", ", inString, " )" ); 
      CodeLine lineData = new CodeLine();

      lineData.num = -1; // We don't know the line
      lineData.start = offset;
      lineData.end = offset + line.length();
      lineData.length += line.length() + 1;
      
      lineData.commentStart = lineData.end;
      lineData.commentEnd = lineData.end;
      
      LinkedList<Region> stringAreas = new LinkedList<Region>();
      int stringStart = -1, codeStart = -1, codeEnd = -1;

      for( int i = 0; i < line.length(); i++ ) {
         char currentChar = line.charAt( i );
         char lastChar = line.charAt( i == 0 ? i : i - 1 );
         
         if( inString != null ) {
            if( currentChar == inString.charValue() && lastChar != '\\' ) {
               inString = null;
               stringAreas.add( 
                  new Region( stringStart + offset, i - stringStart) 
               );
            }
            
         } else if( currentChar == '#' ) {
            lineData.commentStart = offset + i;
            break;
         } else if( isStringChar( currentChar ) ) {
            inString = currentChar;
            stringStart = i;
         }
         
         if( !Character.isWhitespace( currentChar ) ) {
            if( codeStart == -1 ) {
               codeStart = i;
            }            
            codeEnd = i;            
         } 
      }
     
      lineData.codeStart = lineData.start + Math.max( 0, codeStart );
      lineData.codeEnd = lineData.start + Math.max( 0, codeEnd + 1 );
      lineData.unterminatedString = inString;
      
      if( inString != null ) {
         stringAreas.add( 
            new Region( stringStart + offset, (lineData.end  + 1)- stringStart) 
         );
      }
      
      lineData.stringAreas = stringAreas.toArray( 
         new Region[stringAreas.size()] 
      );
      
      return lineData;
   }
   
   /**
    * Get the line area information for a line at a particular offset in a 
    * document.
    * @param doc The document you want a line from
    * @param offset The offset that is on the line you want info on.
    * @return A {@link CodeLine} object with info on the line.
    */
   public static CodeLine getLineAreas( IDocument doc, int offset ) 
   {
      try {
         IRegion lineRegion = doc.getLineInformationOfOffset( offset );
         String line = doc.get( lineRegion.getOffset(), lineRegion.getLength() );
         Character inString = null;
         if( lineRegion.getOffset() - 1 >= 0 ) {
            Partition partition = getPartitionType( doc, Math.max( 0, lineRegion.getOffset() - 1 ) );
            if( partition == Partition.STRING ) inString = '"';
            else if( partition == Partition.QUOTE_STRING ) inString = '\'';
         }
         return getLineAreas( line, lineRegion.getOffset(), inString );
         
      } catch( BadLocationException e ) {
         ZDebug.printStackTrace( e, "Error whilst working out line areas" );
      }
      
      return null;
   }
   
   /**
    * Get the CodeLines for an entire file. CodeLines list the start and end 
    * of different parts of the code, such as comments and string constants.
    * @param text The entire file as a string.
    * @return Returns an array of CodeLine objects, one for each line in the 
    * file. The lines are in order (so codelines[i] gives you the (i + 1)th 
    * line).
    */
   public static CodeLine[] getAllLineAreas( String text ) {
      LinkedList<CodeLine> startList = new LinkedList<CodeLine>();
      
      int len = 0, lineNum = 0;
      Character inString = null;
      for( String line : text.split( "\n", -10000 ) ) {
         lineNum++;
         CodeLine lineData = getLineAreas( line, len, inString );
         inString = lineData.unterminatedString;
         lineData.num = lineNum;
               
         len += line.length() + 1;
         startList.add( lineData );   
      }
      
      return startList.toArray( new CodeLine[startList.size()] );
   }
   
   /**
    * Finds the type of area for a particular position in a TrafficScript 
    * document. This uses the eclipse partitioner.
    * 
    * @param doc The eclipse document that contains the offset you want to find 
    * the type of.
    * @param offset The offset into the document you want to find the area type
    * of. Starts at 0 for the first character.
    * @return A string id of the partition at the offset.
    */
   public static Partition getPartitionType( IDocument doc, int offset ) 
   {
      try {
         if( doc instanceof IDocumentExtension3 ) {
            IDocumentExtension3 ext3 = (IDocumentExtension3) doc;
            
            String id = ext3.getContentType( TrafficScriptPartitioner.TS_PARTITIONER, offset, false );
            return getPartition( id );
         }
        
      } catch( BadPartitioningException e ) {
         return null; // Not a TS file, but we are using the TS editor.
      } catch( Exception e ) {
         ZDebug.printStackTrace( e, "Exception when getting partiton type" );
      }
      return null;
   }
   
   /**
    * Get the function and region for the passed offset.
    * @param line The line we are searching for a function in.
    * @param offset The offset that is in the function.
    * @return The function at this offset, or null if it there is not function
    * here.
    */
   public static CodeRegion getFunctionAtOffset( String line, int offset )
   {
      if( offset < 0 || offset >= line.length() ) {
         return null;
      }
      
      char c = line.charAt( offset );
      if( !Character.isLetterOrDigit( c ) && c != '.' ) {
         return null;
      }
      
      int start = offset, end = offset;
      
      // Expand left
      ZDebug.print( 7, "Expand Left" );
      while( start >= 0 ) {
         c = line.charAt( start );
         ZDebug.print( 9, c, " - ", start );
         if( Character.isLetterOrDigit( c ) || c == '.' ) {
            start--;
         } else if( c == '$' ) {
            return null;
         } else {
            break;
         }
      }      
      start++;
      
      // Expand right
      ZDebug.print( 7, "Expand Right" );
      while( end < line.length() ) {
         c = line.charAt( end );
         ZDebug.print( 9, c, " - ", end );
         if( Character.isLetterOrDigit( c ) || c == '.' ) {
            end++;
         } else if( c == '(' || c == ' ' ) {
            break;
         } else {
            return null;
         }
      }
      
      String func = line.substring( start, end );
      if( func.length() == 0 ||func.matches( "^\\d" ) ) return null;
      
      return new CodeRegion( func, start, end - start );
   }
   
   private CodeUtil() {} // Cannot be instantiated.
}
