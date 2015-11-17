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

import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import com.zeus.eclipsePlugin.ZDebug;

/**
 * Matches escape characters in a string.
 */
public class EscapeCharacterRule implements IRule
{
   private IToken escapeToken;
   private Pattern escapePattern;
   
   /** This regex pattern matches the escape characters */
   public static final Pattern ESCAPE_PATTERN =  Pattern.compile(
      "^\\\\(.|[0-7]{1,3})$"
   );
   
   /** This regex pattern matches the escape characters for single quoted strings */
   public static final Pattern QUOTE_PATTERN =  Pattern.compile(
      "^\\\\'$"
   );
   
   /**
    * Create the escape character rule.
    * @param escapeToken The style token for the escape character.
    * @param isQuoteString True if this is a single quoted string, which will 
    * only escape "\'".
    */
   public EscapeCharacterRule( IToken escapeToken, boolean isQuoteString )
   {
      this.escapeToken = escapeToken;
      this.escapePattern = isQuoteString ? QUOTE_PATTERN : ESCAPE_PATTERN;
   }   
   
   int readCount;

   /**
    * Checks if the current position of the scanner is a escape sequence, 
    * otherwise rewinds and returns undefined.
    * @param scanner The scanner that is reading some code
    * @return The escape token if the current text is an escape sequence, 
    * Token.UNDEFINED otherwise. 
    */
   /* Override */
   public synchronized IToken evaluate( ICharacterScanner scanner )
   {
      ZDebug.print( 3, "evaluate( ", scanner, " )" );
      readCount = 0;
      
      char c;
      StringBuffer buffer = new StringBuffer( 50 );
      
      // Read in 3 characters if it starts with a backslash
      while( (c = readChar( scanner )) != 0 ) {  
         if( (readCount == 1 && c != '\\') || readCount > 4 ) {
            break;         
         } 
         buffer.append( c );      
      }
      rewind( scanner );
      
      ZDebug.print( 4, "Processing word: '", buffer, "'" );
      
      // Match the longest escape pattern we can with the characters me have
      while( buffer.length() > 0 && !escapePattern.matcher( buffer.toString() ).matches() ) {
         rewind( scanner );
         buffer.setLength( buffer.length() - 1 );
      }
      
      // If we matched something then this is an escape sequence
      if( buffer.length() > 0 ) {
         return escapeToken;
         
      // Otherwise we rewind and match nothing.
      } else {
         rewindToBeginning( scanner );
         return Token.UNDEFINED;
      }
   }
   
   /**
    * Reads a single character, increment internal counters.
    * @param scanner The scanner to read from
    * @return The char read, or 0 if we are at the end of the file.
    */
   public char readChar( ICharacterScanner scanner ) 
   {
      int c = scanner.read();
      readCount++;
      
      if( c == ICharacterScanner.EOF ) {
         return 0;
      } else {      
         return (char) c;
      }
   }
   
   /**
    * Rewind to where we started
    * @param scanner The scanner to rewind.
    */
   public void rewindToBeginning( ICharacterScanner scanner ) 
   {
      for( int i = 0; i < readCount; i++ ) {
         scanner.unread();
      }
      readCount = 0;
   }
   
   /**
    * Go back a single character, update internal counters.
    * @param scanner The scanner to rewind.
    */
   public void rewind( ICharacterScanner scanner ) {
      if( readCount > 0 ) {
         scanner.unread();
         readCount--;
      }
   }

}
