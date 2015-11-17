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

import java.util.HashMap;

public class Function implements Comparable<Function>
{
   /** This is returned by the getMaxParams function if there is no limit to the
    *  amount of parameters. */
   public static final int INFINITE = -1;
   
   /** The feature restriction that determines if a function is deprecated */ 
   public static final String DEPRECATED = "FEATURE_DEPRECATED";
   
   private String name;
   private FunctionGroup group;
   private final String lcName;
   private String fullDesc;
   
   private String[] params;
   private int minParams, maxParams;
   private HashMap<String,Boolean> restrictions = null;

   /**
    * Create a new function.
    * @param group Its parent group.
    * @param name The name of the function, NOT including group prefix.
    * @param desc The HTML description of this function.
    * @param minParams The minimum number of parameters.
    * @param maxParams The maximum number of parameters or INFINITE if infinite.
    * @param restrictions The feature restrictions of this function.
    * @param params The names of each of the functions parameters.
    */
   public Function( FunctionGroup group, String name, String desc, int minParams, int maxParams, 
      String[] restrictions, String[] params )
   {
      this.name = name;
      this.group = group;
      this.lcName = name.toLowerCase();
      this.params = params;
      this.minParams = minParams;
      this.maxParams = maxParams;
      this.restrictions = new HashMap<String,Boolean>();
      
      // Put all the restrictions in a hash table
      for( String restrict : restrictions ) {
         this.restrictions.put( restrict, true );
      }
      
      // Build up the full HTML description
      StringBuffer buff = new StringBuffer( desc.length() + 100 );
      buff.append( "<p><b>" );
      buff.append( group ).append( '.' );
      buff.append( name );
      
      // Parameters
      buff.append( "(" );
      if( params.length > 0 ) {
         buff.append( " " );
         int i = 0;
         for( String param : params ) {
            if( i > 0 )  buff.append( ", " );            
            if( i >= minParams ) buff.append( "[" );
            
            buff.append( param );
            
            if( i >= minParams ) buff.append( "]" );
            i++;
         }
         buff.append( " " );
         if( maxParams == INFINITE ) {
            buff.append( "... " );
         }
      } else if( maxParams == INFINITE ) {
         buff.append( " ... " );
      }
            
      buff.append( ")" );
      
      // Put the passed in description at the end
      buff.append( "</b></p>" );
      buff.append( desc.replaceAll( "\\s+|&nbsp;", " " ).trim() );
      
      this.fullDesc = buff.toString();      
   }
   
   /** 
    * Comparator implementation. Should only be used to find functions with names
    * less or greater than the supplied name.
    * 
    * E.g.
    * <pre>function.compareTo( new Function( "foo" ) );</pre>
    */
   Function( String name )
   {
      this.lcName = name.toLowerCase();
   }

   /**
    * Get the function's name, not including group prefix.
    * @return The name of the function.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * Get the lower-case version of this name. Its computed when the function is
    * constructed.
    * @return The lower-case version of this function's name.
    */
   public String getLowerCaseName()
   {
      return lcName;
   }

   /**
    * Returns a full HTML description of the function. Is usually pulled from
    * the code data XML files.
    * @return The full HTML documentation of this function. 
    */
   public String getFullDescription()
   {
      return fullDesc;
   }

   /**
    * Get the names of all this function's parameters, in order.
    * @return This functions parameters in a String array.
    */
   public String[] getParams()
   {
      return params;
   }
   
   /** 
    * Get the minimum number of parameters.
    * @return The minimum number of parameters. 
    */
   public int getMinParams()
   {
      return minParams;
   }

   /**
    * Get the maximum number of parameters, or INIFINATE if there is no limit.
    * @return The maximum number of parameters, or the INFINITE constant.  
    */
   public int getMaxParams()
   {
      return maxParams;
   }

   /**
    * Returns true if this function has the specified restriction.
    * @param restriction The restriction to check for.
    * @return True if this function has this restriction.
    */
   public boolean hasRestriction( String restriction )
   {
      return restrictions.containsKey( restriction );
   }
   
   /**
    * Returns true if this function is deprecated.
    * @return True if this function is deprecated.
    */
   public boolean isDeprecated()
   {
      return hasRestriction( DEPRECATED );
   }

   /**
    * Returns the parent group of this function.
    * @return This functions group.
    */
   public FunctionGroup getFunctionGroup()
   {
      return group;
   }
   
   /**
    * Returns the name in lower case.
    * @return The name of this function in lower case.
    */
   /* Override */
   public String toString()
   {
      return lcName;
   }

   /**
    * Compares this functions toString representation with the passed in 
    * objects.  
    */
   /* Override */
   public int compareTo( Function o )
   {
      return lcName.compareTo( o.getLowerCaseName() );
   }

   
}
