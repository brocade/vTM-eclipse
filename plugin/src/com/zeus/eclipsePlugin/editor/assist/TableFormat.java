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

import org.eclipse.swt.custom.StyleRange;

import com.zeus.eclipsePlugin.ZDebug;

/**
 * Used to render a table using styled text.
 */
public class TableFormat
{
   private LinkedList<LinkedList<String>> rows = new LinkedList<LinkedList<String>>();
   private LinkedList<String> currentRow;
   private LinkedList<StyleRange> styleList;
   
   int maxWidth, pos;
   private String formatted;

   public TableFormat( int maxWidth, int pos )
   {
      this.maxWidth = maxWidth;
      this.pos = pos;
   }
   
   public void startRow() 
   {
      currentRow = new LinkedList<String>();
   }
   
   public void endRow() 
   {
      if( currentRow != null ) rows.add( currentRow );
      currentRow = null;
   }
   
   public void td( String str ) 
   {
      if( currentRow == null ) return;
      currentRow.add( str );
   }
   
   public void th( String str )
   {
      if( currentRow == null ) return;
      currentRow.add( str );
   }
   
   public void end()
   {
      if( currentRow != null ) rows.add( currentRow );
      currentRow = null;
      
      int[] maxColWidths = new int[30];
      for( int i = 0; i < 30; i++ ) maxColWidths[i] = -1;
      
      // Find maxWidth of column
      for( LinkedList<String> row : rows ) {
         int col = 0;
         for( String data : row ) {
            String clean = data.replaceAll( "<[^> ]*>", "" );
            
            if( clean.length() > maxColWidths[col] ) {
               maxColWidths[col] = clean.length();
            }
            
            col++;
         }
      }
      
      StringBuffer out = new StringBuffer( 1000 );
      out.append( "\n\n" );
      
      for( LinkedList<String> row : rows ) {
         int col = 0;
         ZDebug.print( 8, "TR: '" );
         for( String data : row ) {
            ZDebug.print( 9, "   TD: '", data, "' Size: ", maxColWidths[col] );
            
            Math.max( 1, maxColWidths[col] - data.length() );
            
            out.append( String.format( "%-" + maxColWidths[col] + "s", data ) );
            //out.append( data );
            if( col < row.size() - 1 ) out.append( "\t" );
            
            col++;
         }
         out.append( "\n" );
      }
      out.append( "\n" );
      
      HTMLFormat html = new HTMLFormat( pos );
      html.format( out.toString() );
      
      formatted = html.getBuffer();
      styleList = html.getStyleList();
   }

   public LinkedList<StyleRange> getStyleList()
   {
      return styleList;
   }

   public String getFormatted()
   {
      return formatted;
   }
   
   
}
