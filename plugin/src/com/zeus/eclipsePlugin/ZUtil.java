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

import java.util.regex.Pattern;

import com.zeus.eclipsePlugin.model.ModelException;

/**
 * General utility functions that don't fit anywhere else.
 */
public class ZUtil
{
   private static final Pattern PAT_CONTROL = Pattern.compile( "[\\000-\\037]" ); //$NON-NLS-1$
   
   private static final Pattern IP_PATTERN = Pattern.compile(
      "b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?).)" + //$NON-NLS-1$
       "{3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)b" //$NON-NLS-1$
   );
   
   private static final Pattern HOST_PATTERN = Pattern.compile( "^(([a-zA-Z0-9][-a-zA-Z0-9_]*[a-zA-Z0-9])|" + //$NON-NLS-1$
   "([a-zA-Z0-9](?:[-a-zA-Z0-9_]*[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[-a-zA-Z0-9_]*[a-zA-Z0-9])?)*\\.?))$" ); //$NON-NLS-1$
   
   private static final Pattern HOST_NEG = Pattern.compile( "^\\d+$|\\.\\d[^\\.]*\\.?$" ); //$NON-NLS-1$
   
   private static final Pattern USERNAME_VALID = Pattern.compile( "^[a-zA-Z0-9-_\\.@]+$" ); //$NON-NLS-1$
   
   /**
    * Function for validating rule names.
    * @param name The name you want to validate.
    * @return The problem with the rule name, or null if there is no error
    */
   public static String validateRuleName( String name )
   {
      if( name.length() > 128 )     return ZLang.ZL_ValidationRuleNameIsTooLong;
      if( name.trim().length() == 0 )   return ZLang.ZL_ValidationYouMustSpecifyAName;
      if( name.startsWith( "." ) )  return ZLang.ZL_ValidationRuleCannotStartWithAPeriod; //$NON-NLS-1$
      if( name.startsWith( "_" ) )  return ZLang.ZL_ValidationRuleCannotStartWithAUnderscore; //$NON-NLS-1$
      if( name.startsWith( "#" ) )  return ZLang.ZL_ValidationRuleCannotStartWithAHash; //$NON-NLS-1$
      if( name.endsWith( "~" ) )    return ZLang.ZL_ValidationRuleCannotEndWithATilde; //$NON-NLS-1$
      
      if( PAT_CONTROL.matcher( name ).matches() ) {
         return ZLang.ZL_ValidationRuleCannotContainControlChars;
      }
      
      if( name.indexOf( "*" ) != -1 ) return ZLang.ZL_ValidationAstriskNotAllowedAsAName; //$NON-NLS-1$
      if( name.indexOf( "`" ) != -1 ) return ZLang.ZL_ValidationRuleCannotContainBacktick; //$NON-NLS-1$
      if( name.indexOf( '!' ) != -1 )  return ZLang.ZL_ValidationRuleCannotContainExcalmation;
      if( name.indexOf( '/' ) != -1 )  return ZLang.ZL_ValidationRuleCannotContainForwardSlash;
      if( name.indexOf( '\\' ) != -1 ) return ZLang.ZL_ValidationRuleCannotContainBackslash;
      if( Character.isWhitespace( name.charAt( 0 ) ) ) {
         return ZLang.ZL_ValidationRuleCannotStartWithWhitespace;
      }
      if( Character.isWhitespace( name.charAt( name.length() - 1 ) ) ) {
         return ZLang.ZL_ValidationRuleCannotEndWithWhitespace;
      }
      
      
      return null;
   }
   
   /**
    * Function for validating hostnames.
    * @param name The hostname you want to validate.
    * @return The problem with the hostname, or null if there is no error.
    */
   public static String validateHostname( String hostname )
   {
      if( ( !IP_PATTERN.matcher( hostname ).matches() && 
            !HOST_PATTERN.matcher( hostname ).matches() )
          || HOST_NEG.matcher( hostname ).matches() ) 
      {
         return ZLang.bind( ZLang.ZL_ValidationHostnameIsInvalid, hostname );
      }
      
      return null;
   }
   
   /**
    * Function for validating ZXTM user-names.
    * @param username The user-name to validate.
    * @return The problem with the user-name or null if it's fine.
    */
   public static String validateUserName( String username )
   {
      if( !USERNAME_VALID.matcher( username ).matches() ) {
         return ZLang.ZL_ValidationUsernameIsInvalid;
      }
      return null;
   }
      
   /**
    * Get the exception message from the root cause of the specified exception.
    * Will stop traversing the cause stack if it hits one of our nice exceptions
    * (such as ModelException).
    * @param e The exception to get the root cause message from.
    * @return The root cause message, or 'Unknown error' if all the messages 
    * were null.
    */
   public static String getRootCauseMessage( Throwable e )
   {
      Throwable cause = e;
      String message = null;
      while( true ) {
         if( cause.getLocalizedMessage() != null ) {
            message = cause.getLocalizedMessage();
         } else if( cause.getMessage() != null ) {
            message = cause.getMessage();
         }
         
         if( cause.getCause() == null || cause.getCause() == cause ||
            cause instanceof ModelException ) 
         {
            break;            
         } else {
            cause = cause.getCause();
         }
      }
      
      return (message != null) ? message : ZLang.ZL_UnknownError;
   }
   
   /**
    * Ensures the provided value is always within the passed arrays index 
    * bounds, i.e. 0 to array.length - 1 inclusive.
    * @param array The array to cap the value for
    * @param val The value you want capping.
    * @return The capped value.
    */
   public static int capRange( Object[] array, int val )
   {
      return capRange( 0, array.length - 1, val );
   }
   
   /**
    * Ensure the passed value falls within a certain range.
    * @param min The minimum value, inclusive.
    * @param max The maximum value, inclusive
    * @param val The value to cap.
    * @return The capped value.
    */
   public static int capRange( int min, int max, int val )
   {
      if( val < min ) return min;
      if( val > max ) return max;
      return val;
   }
   
   /**
    * Join an array with the specified delimiter.
    * @param delimiter The string to put in between each object.
    * @param array An array of any type. Each objects toString method is used.
    * @return The string containing all the arrays values.
    */
   public static String join( String delimiter, Object[] array )
   {
      StringBuffer buffer = new StringBuffer( 100 );
      for( Object obj : array ) {
         if( buffer.length() > 0 ) buffer.append( delimiter );
         buffer.append( obj );
      }
      return buffer.toString();
   }
   
   /**
    * Join an iterable object with the specified delimiter.
    * @param delimiter The string to put in between each object.
    * @param array An iterable object of any type. Each objects toString method 
    * is used.
    * @return The string containing all the iteratable's values.
    */
   public static <C> String join( String del, Iterable<C> array )
   {
      StringBuffer buffer = new StringBuffer( 100 );
      for( C obj : array ) {
         if( buffer.length() > 0 ) buffer.append( del );
         buffer.append( obj );
      }
      return buffer.toString();
   }
   
   /**
    * Join a byte array with the specified delimiter.
    * @param delimiter The string to put in between each object.
    * @param array A byte array to join.
    * @return The string containing all the arrays values.
    */
   public static String join( String delimiter, byte[] array )
   {
      StringBuffer buffer = new StringBuffer( 100 );
      for( byte b : array ) {
         if( buffer.length() > 0 ) buffer.append( delimiter );
         buffer.append( b );
      }
      return buffer.toString();
   }

   private ZUtil() {}
}
