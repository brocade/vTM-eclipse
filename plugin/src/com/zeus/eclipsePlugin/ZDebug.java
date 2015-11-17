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

import java.io.PrintStream;
import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.zeus.eclipsePlugin.consts.Ids;

/**
 * Functions for debugging the system.
 */
public class ZDebug
{      
   private static boolean debug = false;
   private static HashMap<String,Integer> filesTable = new HashMap<String,Integer>();   
   private static PrintStream debugStream = System.out;
   
   /**
    * Enable or disable debugging for the entire plug-in. 
    * @param value If true debugging is enabled.
    */
   public static void setDebug( boolean value ) 
   {
      debug = value;
   }
   
   /**
    * Clear the internal table of class files.
    */
   public static void resetFiles()
   {
      filesTable.clear();
   }
   
   /**
    * Add a class/file that you want to debug
    * @param file The file name or class
    * @param level The detail level to debug the file.
    */
   public static void addFile( String file, int level  ) 
   {
      filesTable.put( file, level );
   }
   
   /**
    * Get the StackTraceElement n levels below the current one
    * @param level The stack element level you want
    * @return A stack element for the level, or null if the stack isn't that 
    * deep
    */
   private static StackTraceElement getLineInfo( int level )
   {
      StackTraceElement[] elements = Thread.currentThread().getStackTrace();
      
      // Different Java versions give us different stack traces
      int i = 0;
      while( !elements[i++].getMethodName().equals( "getStackTrace" ) ) {
         level++;
      }
      
      if( elements.length <= level ) return null;
      return elements[level];
   }
   
   /**
    * Check if the calling function matches a file/classes we are interested in.
    * @param level The debugging level that the is caller is.
    * @param back How many calls back is the original caller.
    * @return The file/class string if the check passes, null otherwise.
    */
   private static String check( int level, int back )
   {
      if( !debug ) return null;
      StackTraceElement element = getLineInfo( back );
      String[] classbits = element.getClassName().split( "\\." );
      String className = classbits[classbits.length - 1];
      
      String location = element.getFileName() + ":" + element.getLineNumber();
      //System.out.println( location );
      
      Integer fileLevel = filesTable.get( element.getFileName() );
      if( fileLevel != null && fileLevel >= level ) {
         return location;      
      } 
      
      Integer classLevel = filesTable.get( className );
      if( classLevel != null && classLevel >= level ) {
         return location;      
      } 
      
      return null;
   }
   
   /**
    * Print debugging information if the specified level is high enough and we
    * are interested in the callers file. 
    * @param level The debugging level. The higher the level, the less stuff 
    * will be printed.
    * @param toPrint Objects to print in sequence.
    */
   public static void print( int level, Object ... toPrint )
   {
      if( !debug || filesTable.isEmpty() ) return;
      print( level, toPrint, 5 );
   }
      
   /**
    * Internal print method 
    */
   private static void print( int level, Object[] toPrint, int back )
   {
      String location = check( level, back );
      if( location == null ) return;
      
      // Print to Eclipse's log
      try {
         if( ZXTMPlugin.getDefault() != null ) {
            StringBuffer buffer = new StringBuffer( 200 );
            buffer.append( "ZDBG:" );
            for( int i = 0; i <= level; i++ ) buffer.append( ' ' );
            for( Object obj : toPrint ) {
               buffer.append( obj );
            }
            buffer.append( "    (" + location + ")"  );
            
            ZXTMPlugin.getDefault().getLog().log( new Status(
               IStatus.INFO, Ids.PLUGIN, buffer.toString()
            ));
         }
      } catch( RuntimeException e ) {}
      
      // Print to stdout
      while( level-- > 0 ) {
         debugStream.print( " " );
      }
      
      for( Object obj : toPrint ) {
         debugStream.print( obj );
      }
      
      debugStream.println( "    (" + location + ")" );
   }
   
   /**
    * Internal stack trace printer
    * @param elements The stacktrace to print
    * @param ignoreFirst Ignore the first n elements of the trace
    */
   private static void printStackTrace( StackTraceElement[] elements, 
      int ignoreFirst )
   {
      for( StackTraceElement element : elements ) {
         if( ignoreFirst-- > 0 ) continue;
         System.out.println( "    " + element );
      }
   }

   /**
    * Print a stack trace from an exception. Only does anything if debugging is
    * enabled.
    * @param e The exception to print the stack trace from.
    * @param toPrint The message to print with the stack trace.
    */
   public static void printStackTrace( Throwable e, Object ... toPrint )
   {
      // Log to eclipse's log
      try {
         if( ZXTMPlugin.isEclipseLoaded() ) {
            ZXTMPlugin.getDefault().getLog().log( 
               new Status( IStatus.WARNING, Ids.PLUGIN, 
                  ZUtil.join( "", toPrint ), e
               )
            );
         }
      } catch( RuntimeException e2 ) {}
                  
      if( !debug ) return;
      
      // Print text if there is any
      if( toPrint.length > 0 ) {
         for( Object obj : toPrint ) {
            debugStream.print( obj );
         }
         debugStream.println();
      }
      
      Throwable cause = e; 
      boolean first = true;
      while( cause != null ) {
         if( first ) {
            first = false;
         } else {
            debugStream.println( "\nException caused by:" );
         }
                  
         debugStream.println( cause.getClass() + ": " + cause.getMessage() );
         printStackTrace( cause.getStackTrace(), 0 );
         
         if( cause == cause.getCause() ) break;
         cause = cause.getCause();
      }
            
   }

   /**
    * Dump the current stack if debugging is enabled.
    * @param toPrint A message to print above the stack trace.
    */
   public static void dumpStackTrace( Object ... toPrint )
   {      
      // Log to eclipse's log
      try {
         if( ZXTMPlugin.isEclipseLoaded() ) {
            ZXTMPlugin.getDefault().getLog().log( 
               new Status( IStatus.ERROR, Ids.PLUGIN, 
                  ZUtil.join( "", toPrint ), new Exception( "Internal Error" )
               )
            );
         }
      } catch( RuntimeException e ) {}
      
      
      if( !debug ) return;
      
      // Print text if there is any
      if( toPrint.length > 0 ) {
         for( Object obj : toPrint ) {
            debugStream.print( obj );
         }
         debugStream.println();
      }
      
      printStackTrace( Thread.currentThread().getStackTrace(), 2 );     
   }
         
}
