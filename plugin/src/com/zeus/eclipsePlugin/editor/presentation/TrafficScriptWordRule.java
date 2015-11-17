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

import java.util.Collection;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.codedata.CodePossibility;
import com.zeus.eclipsePlugin.codedata.VersionCodeData;
import com.zeus.eclipsePlugin.codedata.CodePossibility.Type;

/**
 * This matches various bits of TrafficScript code and returns the appropriate 
 * style token. It uses the editors code data to determine what to colour.
 */
public class TrafficScriptWordRule implements IRule
{
   private VersionCodeData version;
   private IToken functionToken, keywordToken, deprecatedToken, defaultToken;
   
   /**
    * Create the TrafficScript scanner, for the specified version.
    * @param version The code data version for the ZXTM that this code is stored
    * on.  
    * @param functionToken The style token for functions
    * @param keywordToken The style token for keywords.
    * @param deprecatedToken The style token for deprecated functions.
    * @param defaultToken The default token for code.
    */
   public TrafficScriptWordRule( VersionCodeData version, 
      IToken functionToken, IToken keywordToken, IToken deprecatedToken, 
      IToken defaultToken )
   {
      this.version = version;
      this.functionToken = functionToken;
      this.keywordToken = keywordToken;
      this.defaultToken = defaultToken;
      this.deprecatedToken = deprecatedToken;
   }   
   
   int readCount;

   /**
    * Read the next bit of text that could possibly be a colourable bit of code.
    * If it matches a keyword/function then return the appropriate token, 
    * otherwise return the default token.
    * @param scanner The scanner that is reading the code.
    * @return The appropriate token for the code we have scanned.
    */
   /* Override */
   public synchronized IToken evaluate( ICharacterScanner scanner )
   {
      ZDebug.print( 4, "evaluate( ", scanner, " )" );
      readCount = 0;
      
      // Read in code until we hit something that can't be part of a keyword or
      // function
      char c;
      StringBuffer buffer = new StringBuffer( 50 );
      
      while( (c = readChar( scanner )) != 0 ) {  
         if( !Character.isLetterOrDigit( c ) && c != '.' ) {
            ZDebug.print( 8, "Breaking on: ", c );
            break;         
         }
         if( readCount == 1 && !Character.isLetter( c ) ) {
            ZDebug.print( 8, "Breaking on: ", c );
            break;  
         }
         
         buffer.append( c );      
      }
      rewind( scanner );
      
      ZDebug.print( 4, "Processing word: '", buffer, "'" );
      
      // Use code matching functions to find any keywords/functions that match.
      if( buffer.length() > 0 ) {
         Collection<CodePossibility> possibilities = version.getPossiblilities( 
            buffer.toString(), new Type[] { Type.KEYWORD }
         );
         
         if( possibilities.size() > 0 ) {
            ZDebug.print( 5, "Word is keyword." );
            return keywordToken;
         }
         
         possibilities = version.getPossiblilities( 
            buffer.toString(), new Type[] { Type.FUNCTION, Type.GROUP }
         );
         
         if( possibilities.size() > 0 ) {
            if( possibilities.size() == 1  ) {
               CodePossibility first = possibilities.iterator().next();
               if( first.getFunction() != null && first.getFunction().isDeprecated() ) {
                  ZDebug.print( 5, "Word is deprecated." );
                  return deprecatedToken;
               }
            }
            ZDebug.print( 5, "Word is function." );
            return functionToken;
         }
      } else {
         rewindToBeginning( scanner );
         return Token.UNDEFINED;
      }
      
      if( defaultToken.isUndefined() ) {
         rewindToBeginning( scanner );
      }
      return defaultToken;
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
