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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;

/**
 * Represents a group of functions.
 */
public class FunctionGroup implements Iterable<Function>, Comparable<FunctionGroup>
{
   private String name;
   private final String lcName;
   private String description;
   private String fullDesc = null;
   
   private HashMap<String, Function> functionTable = null;
   private TreeSet<Function> functionTree = null;
   
   /**
    * Creates a function group.
    * @param name The name of the group.
    * @param desc The HTML description of the group.
    */
   public FunctionGroup( String name, String desc )
   {
      super();
      this.name = name;
      this.description = desc.replaceAll( "\\s+|&nbsp;", " " ).trim();
      this.lcName = name.toLowerCase();
      this.functionTable = new HashMap<String, Function>();
      this.functionTree = new TreeSet<Function>();
   }
   
   /** 
    * Comparator implementation. Should only be used to find groups with names
    * less or greater than the supplied name.
    */
   FunctionGroup( String name ) 
   {
      ZDebug.print( 5, "FunctionGroup - ", name );
      this.lcName = name.toLowerCase();
   }
  
   /**
    * Add a function to this group.
    * @param function The Function object to be added.
    */
   public void addFunction( Function function )
   {
      functionTable.put( function.getLowerCaseName(), function );
      functionTree.add( function );
   }
   
   /**
    * Get a function with the specified name. Case will be ignored.
    * @param name The name of the function, not including group prefix.
    * @return The Function with this name, or null if there is no function.
    */
   public Function getFunction( String name ) 
   {
      return functionTable.get( name.toLowerCase() );
   }

   /**
    * Get all the functions in this group.
    * @return A sorted collection of all the functions in this group.
    */
   public Collection<Function> getFunctions() 
   {
      return functionTree;
   }

   /**
    * Returns an iterator for all the functions in this group. This is more 
    * efficient than using getFunctions to enumerate over the groups functions.
    */
   /* Override */
   public Iterator<Function> iterator()
   {
      return functionTree.iterator();
   }


   /**
    * Get the name of this group.
    * @return The name of this group. 
    */
   public String getName()
   {
      return name;
   }

   /**
    * Returns the number of functions in this group.
    * @return The number of functions in this group.
    */
   public int getNumberOfFunctions() 
   {
      return functionTable.size();
   }

   /**
    * Gets the HTML description of this group. Lists all the functions in the 
    * group. 
    * @return A HTML description of this group.
    */
   public String getDescription()
   {
      if( fullDesc == null ) {
         StringBuffer buff = new StringBuffer( 100 );
         buff.append( "<p><b>" );
         buff.append( name );
         
         buff.append( "</b></p><p>" );
         buff.append( description );
         
         buff.append( "</p>" );
         buff.append( ZLang.ZL_ContainsMethods );
         buff.append(  "<ul>" );
         
         int i = 0;
         for( Function func : functionTable.values() ) {
            buff.append( "<li>" );
            buff.append( func.getName() );
            buff.append( "</li>" );
            i++;
         }
         buff.append( "</ul>" );
         
         fullDesc = buff.toString();
      }
      
      return fullDesc;
   }
   
   /**
    * Returns all the functions in this group starting with the passed in 
    * string. This is case insensitive.
    * @param start The start of a function we want to find matches for.
    * @return A collection of functions that start with the passed in string.
    */
   public Collection<Function> getFunctionsStartingWith( String start )
   {
      if( start.length() == 0 ) {
         return functionTree;
      }
      
      start = start.toLowerCase();
      
      // Uses tree subset to find the groups greater than start or less than 
      return functionTree.subSet( 
         new Function( start ), 
         new Function( VersionCodeData.getSearchLimit( start ) ) 
      );
   }
   
   /**
    * Get the name of this group in lower-case.
    * @return The name of this group in lower-case.
    */
   public String getLowerCaseName()
   {
      return lcName;
   }

   /**
    * Returns the lower-case name of this group.
    */
   /* Override */
   public String toString()
   {
      return lcName;
   }

   /**
    * Compares this groups lower-case name to the toString of the comparator.
    */
   /* Override */
   public int compareTo( FunctionGroup o )
   {
      return lcName.compareTo( o.getLowerCaseName() );
   }

}
