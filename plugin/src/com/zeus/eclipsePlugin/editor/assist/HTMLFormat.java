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

package com.zeus.eclipsePlugin.editor.assist;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

import com.zeus.eclipsePlugin.ZDebug;

/**
 * This class strips the tags from some HTML and formats the resulting text with
 * a set of StyleRange objects.
 */
public class HTMLFormat
{
   private int start, pos, length, offset;

   private StringBuffer buffer, td;
   private LinkedList<StyleRange> styleList;

   private boolean wordWrap = true;

   /**
    * Create a standard HTML format.
    */
   public HTMLFormat() 
   {
      offset = 0;
   }
   
   /**
    * Create a HTML offset that adds an offset to the returned style ranges 
    * positions.
    * @param offset The offset to add to the position of the style ranges.
    */
   public HTMLFormat( int offset ) 
   {
      this.offset = offset;
   }
   
   /**
    * Append a string to the string buffer, and update the internal variables.
    */
   private void append( String str ) 
   {
      if( td != null ) return;
      
      buffer.append( str );
      length += str.length();
      pos += str.length();
   }
   
   /**
    * Append a character to the string buffer, and update the internal variables.
    */
   private void append( char c ) 
   {
      if( td != null ) return;
      
      buffer.append( c );
      length += 1;
      pos += 1;
   }
   
   /**
    * Processes an input string, striping tags and updating the style ranges 
    * list.
    * 
    * SFT: This needs some tidying up
    * 
    * @param input The HTML to process.
    */
   public void format( String input )
   {      
      buffer = new StringBuffer( input.length() );
      styleList = new LinkedList<StyleRange>();
      start = 0; length = 0; pos = 0;
      
      StringBuffer tag = null;
      td = null;
      boolean inTag = false;
      boolean newLineStrip = true;
      TableFormat table = null;
       
      boolean italic = false, bold = false;
      
      // Iterate through the input string
      for( int i = 0; i < input.length(); i++ ) {         
         char c = input.charAt( i );
         
         // If we are in a table td/th tag, append the character data to an 
         // alternate buffer.
         if( td != null ) {
            td.append( c );
         }
         
         // We are starting a tag. Create a new tag buffer.
         if( c == '<' ) {
            tag = new StringBuffer( 10 );
            inTag = true;
            
         // This is the end of the tag. Process the tag now we have all of it.
         } else if( inTag && c == '>' ) {
            
            inTag = false;
            
            // Process the html tag when it's finished
            String t = tag.toString().trim();
            
            // Is this an end tag of a 2 tag set (e.g. <tag> ... </tag>)?
            boolean end = t.startsWith( "/" );
            String split[] = t.split( "[\\s\\/]+", 0 );
            
            // Get the start type
            String type = split[ split[0].equals( "" ) ? 1 : 0 ];
            
            boolean change = false;
            boolean newBold = bold, newItalic = italic;
            
            ZDebug.print( 8, "Tag: '", t, "' Type: '", type, "'" );
            
            // Check the single character stuff first
            if( t.length() == (end ? 2 : 1) ) {
               char switchChar = type.charAt( 0 );

               switch( switchChar ) {
                  case 'b': newBold = !end; change = true; break;
                  case 'i': newItalic = !end; change = true; break;
                  case 'p': {
                     if( end ) { 
                        if( buffer.charAt( buffer.length() - 1) == '\n' ) {
                           if( buffer.charAt( buffer.length() - 2) != '\n' ) {
                              append( "\n" );
                           }
                        } else {
                           append( "\n\n" );
                        }
                        newLineStrip = true;
                     }
                  }
               }
            } else if( type.equals( "br" ) || (type.equals( "li" ) && end) ) {
               append( "\n" );
               newLineStrip = true;
            } else if( type.equals( "ul" ) ) {
               if( !end ) {
                  append( "\n" );
               }
               newLineStrip = true;
           
               
            } else if( type.equals( "li" ) && !end ) {
               append( "  " );
               append( (char) 9679 );
               append( "  " );
               
            } else if( type.equals( "table" ) ) {
               wordWrap = false;
               if( !end ) {
                  table = new TableFormat( 100, pos );
               } else if( table != null ) {
                  table.end();
                  append( table.getFormatted() );
                  styleList.addAll( table.getStyleList() );
                  table = null;
               }
               
            } else if( type.equals( "tr" ) ) {
               if( !end && table != null ) {
                  table.startRow();
               } else if( table != null ) {
                  table.endRow();
               }
               
            } else if( type.equals( "td" ) || type.equals( "th" ) ) {
               if( !end && table != null ) {
                  td = new StringBuffer( 100 );
               } else if( table != null ) {
                  String tdFinal = td.toString().replaceAll( "</t[hd]>", "" );
                  
                  if( type.equals( "th" ) ) {
                     table.th( tdFinal );
                  } else {
                     table.td( tdFinal );
                  }
                  td = null;
               }
               
            // NOT A REAL TAG!!! Dodgy unescaped < and > chars 
            // used in certain places
            } else {
               
               String fulltag = tag.toString().replaceAll( "&amp;", "&" );
               append( '<' );
               append( fulltag );
               append( '>' );
            }
            
            // If changes effect 'style' (e.g. bold/italic) we set them here
            if( change && (italic || bold) ) {
               int mode = 0;
               if( italic ) mode |= SWT.ITALIC;
               if( bold ) mode |= SWT.BOLD;
               
               ZDebug.print( 6, "StyleRange: ", start, " - ", length );
               styleList.add( new StyleRange( start + offset, length, null, null, mode ) );               
            }
            
            // Reset region stuff
            if( change ) {
               ZDebug.print( 6, "Change - ", bold, " - ", italic );
               start = pos;
               length = 0;
            }
            
            bold = newBold;
            italic = newItalic;
         
         } else if( inTag ) {
            // Store current tag in buffer if we're between < and >
            tag.append( c );
            
         
         } else if( c == '&' ) {            
            String htmlEncoded = input.substring( i );
            
            ZDebug.print( 6, "HTML Encode: '", htmlEncoded.substring( 0, 4 ), "'" );
            
            if( htmlEncoded.startsWith( "&amp;" ) ) {
               i += 4;
               append( '&' );
            } else {
               append( c );
            }
            
         } else if( newLineStrip ) {
            // Strip the first whitespace character after a newline and certain tags.
            if( c != ' ' && c != '\n' ) {
               append( c );
            }  
            newLineStrip = false;
         } else {
            // Store data to main buffer if they're normal chars.
            append( c );
         }         
      }
      
      if( italic || bold ) {
         int mode = 0;
         if( italic ) mode |= SWT.ITALIC;
         if( bold ) mode |= SWT.BOLD;
         
         ZDebug.print( 6, "StyleRange: ", start, " - ", length );
         styleList.add( new StyleRange( start + offset, length, null, null, mode ) );  
      }
      
      if( inTag ) {
         append( "<" + tag.toString().replaceAll( "&amp;", "&" ) );
      }

   }

   /**
    * Get the tag stripped string.
    * @return The raw text of the passed in HTML code.
    */
   public String getBuffer()
   {
      return buffer.toString();
   }

   /**
    * Get the list of StyleRanges that can be used to colour and style the raw
    * text from getBuffer().
    * @return The StyleRanges to style the raw text.
    */
   public LinkedList<StyleRange> getStyleList()
   {
      return styleList;
   }

   /** 
    * Does this require no wrapping (set when we have tables).
    * @return Should this be displayed without word wrap.
    */
   public boolean isWordWrap()
   {
      return wordWrap;
   }
   
   
   
}
