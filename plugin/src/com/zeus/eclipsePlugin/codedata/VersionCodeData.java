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

package com.zeus.eclipsePlugin.codedata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.codedata.CodePossibility.Type;

/**
 * This represents the code information for a particular ZXTM version.
 */
public class VersionCodeData implements Iterable<FunctionGroup>
{
   private int major, minor;
   private TreeSet<String> keywords = new TreeSet<String>();
   
   private HashMap<String, FunctionGroup> groupTable = new HashMap<String, FunctionGroup>();
   private TreeSet<FunctionGroup> groupTree = new TreeSet<FunctionGroup>();
   
   /** The longest possible group name */
   private int maxGroupLength = 0;

   /**
    * Creates a empty TrafficScriptVersion class for the specified version.
    * @param major The major version, e.g. for 5.1r3 = 5
    * @param minor The minor version, e.g. for 5.1r3 = 1
    */
   public VersionCodeData( int major, int minor )
   {
      this.major = major;
      this.minor = minor;
   }
   
   /**
    * The keywords used in TrafficScript for this ZXTM version.
    * @return An array of keywords.
    */
   public Set<String> getKeywords() 
   {      
      return keywords;
   }
   
   /**
    * Add a function group.
    * @param group The FunctionGroup to add.
    */
   public void addGroup( FunctionGroup group ) 
   {
      groupTable.put( group.getLowerCaseName(), group );
      groupTree.add( group );
      if( group.getName().length() > maxGroupLength ) {
         maxGroupLength = group.getName().length();
      }
   }
   
   /**
    * Get a function group with the specified name. Case insensitive.
    * @param name The name of group to return.
    * @return The group with the specified name or null if it does not exist.
    */
   public FunctionGroup getGroup( String name ) 
   {
      return groupTable.get( name.toLowerCase() );
   }

   /** 
    * Get a sorted collection of all the groups in this code data version.
    * @return A sorted collection of function groups.
    */
   public Collection<FunctionGroup> getGroups() 
   {
      return groupTree;
   }
   
   /**
    * Iterate over all the function groups in this TrafficScript version. 
    * @return An iterator for all the function groups.
    */
   /* Override */
   public Iterator<FunctionGroup> iterator()
   {
      return groupTree.iterator();
   }

   /**
    * Get the major version.
    * @return The major version
    */
   public int getMajorVersion()
   {
      return major;
   }

   /**
    * Get the minor version.
    * @return The minor version
    */
   public int getMinorVersion()
   {
      return minor;
   }

   /**
    * Set the version. Can only be called by a sub class, and should not be used
    * after loading data.
    * @param major The major version.
    * @param minor The minor version.
    */
   protected void setVersion( int major, int minor )
   {
      this.major = major;
      this.minor = minor;
   }
   
   /**
    * Set the keywords that this version of TrafficScript uses.
    * @param keywords An array of keywords
    */
   public void setKeywords( String[] keywords )
   {
      this.keywords.clear();
      for( String keyword : keywords ) {
         this.keywords.add( keyword );
      }      
   }

   /**
    * Change a major and a minor version into a string.
    * @param major The major version
    * @param minor The minor version
    * @return A string representation of the passed version.
    */
   public static String createVersionString( int major, int minor )
   {
      return major + "." + minor;
   }
   
   /**
    * Get the version of this code data as a String.
    * @return The String representing this data's version.
    */
   public String getVersionString() 
   {
      return createVersionString( major, minor );
   }
   
   /**
    * Get the function matching the passed in string. The name includes the 
    * group prefix.
    * @param fullName The name of the function data you want, including function
    * group at the front.
    * @return The function data for the named function, or null if there is no
    * such function.
    */
   public Function getFunctionMatching( String fullName )
   {
      int dot = fullName.lastIndexOf( '.' );
      if( dot == fullName.length() - 1 || dot == -1 ) return null;
      
      String groupName = fullName.substring( 0, dot );

      FunctionGroup group = this.getGroup( groupName );
      if( group == null ) return null;
      
      String functionName = fullName.substring( dot + 1 );
      return group.getFunction( functionName );      
   }
   
   /**
    * Return all groups starting with the passed in prefix.
    * @param start The start of a groups you want to match.
    * @return A collection of groups that match the passed string.
    */
   public Collection<FunctionGroup> getGroupsStartingWith( String start )
   {
      if( start.length() == 0 ) {
         return groupTree;
      }
      start = start.toLowerCase();
      
      if( start.length() > maxGroupLength )
         return new TreeSet<FunctionGroup>();
      try {
         // Uses tree subset to find the groups greater than start or less than 
         return groupTree.subSet( 
            new FunctionGroup( start ), 
            new FunctionGroup( getSearchLimit( start ) ) 
         );
      } catch( IllegalArgumentException e ) {
         ZDebug.printStackTrace( e, "Invalid search limit" );         
      }
      return new TreeSet<FunctionGroup>();

   }
   
   /**
    * Return all keywords starting with the passed in prefix.
    * @param start The start of a keywords you want to match.
    * @return A collection of keywords that match the passed string.
    */
   public Collection<String> getKeywordsStartingWith( String start )
   {
      if( start.length() == 0 ) {
         return keywords;
      }
      start = start.toLowerCase();
      
      return keywords.subSet( start, getSearchLimit( start ) );
   }
   
   
   /**
    * Get a set of possibilities to complete the passed word.
    * @param word The word you want to complete
    * @param types The types of completions you want.
    * @return A set of possibilities to complete the code.
    */
   public Collection<CodePossibility> getPossiblilities( String word, Type[] types ) 
   {
      ZDebug.print( 4, "getPossiblilities( ", word, " )" );
      
      // Optimisation
      if( word.length() > 0 && !Character.isLetter( word.charAt( 0 ) ) ) {
         return new ArrayList<CodePossibility>(0);
      }
      
      // Get what types of possibility the caller wants
      boolean listGroups = false, listFunctions = false, listKeywords = false;
      
      
  
      for( Type type : types ) {
         switch( type ) {
            case FUNCTION: listFunctions = true; break;
            case GROUP: listGroups = true; break;
            case KEYWORD: listKeywords = true; break;
         }
      }
         
      // Split up into parts (for functions and groups)
      String[] parts = word.split( "\\.", -1 );         
      if( parts.length < 1 ) return new HashSet<CodePossibility>(0);
         
      String groupName = "";
      String funcName = null;
      for( int i = 0; i < parts.length - 1; i++ ) {
         if( i != 0 ) groupName += ".";
         groupName += parts[i];
      }
      
      ZDebug.print( 5, "Word Before: '", word, "'" );
      
      FunctionGroup currentGroup = this.getGroup( groupName );
      
      if( parts.length > 0 ) {
         funcName = parts[parts.length - 1];
      }
   
      TreeSet<CodePossibility> list = new TreeSet<CodePossibility>();
      
      // Suggest some keywords
      if( listKeywords ) {
         for( String keyword : getKeywordsStartingWith( word ) ) {            
            list.add( new CodePossibility( keyword, word ) );            
         }
      }
      
      // Suggest some groups  
      if( listGroups ) {        
         ZDebug.print( 5, "Getting groups that start with: ", word );
         
         for( FunctionGroup group : getGroupsStartingWith( word ) ) {
            list.add( new CodePossibility( group, word ) );
         }
      }
   
      // Suggest some functions
      if( listFunctions ) {
         if( currentGroup != null ) {
            ZDebug.print( 5, "Getting functions in '", currentGroup, "' that start with: ", funcName );
            for( Function function : currentGroup.getFunctionsStartingWith( funcName ) ) {
               list.add( new CodePossibility( function, funcName ) );
            }
         }
      }
         
      return list;
   }
   
   /**
    * For searching for strings that start with a character using TreeSets 
    * subSet function. Returns the string that should be used as the upper
    * limit for the subset.
    * @param The string you want the subset that matches the start of.
    * @return The upper limit of the subset, exclusive.
    */
   static final String getSearchLimit( String start )
   {
      String front = start.substring( 0, start.length() - 1 );
      front += (char)(start.charAt( start.length() - 1 ) + 1);
      return front;
   }
   
}
