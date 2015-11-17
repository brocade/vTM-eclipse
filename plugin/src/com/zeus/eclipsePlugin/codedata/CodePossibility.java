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

/**
 * Represents a possible completion of a half completed piece of code.
 */
public class CodePossibility implements Comparable<CodePossibility>
{
   /**
    * The type of completion.
    */
   public enum Type {
      GROUP,
      FUNCTION,
      KEYWORD
   };
   
   private String name;
   private String currentText;
   
   private Function function = null;
   private FunctionGroup functionGroup = null;
   private Type type;
   
   /**
    * Create a code possibility for a function.
    * @param function The function that is a possibility.
    * @param currentText The current text that matches the start of this 
    * function.
    */
   public CodePossibility( Function function, String currentText ) 
   {
      this.name = function.getLowerCaseName();
      this.currentText = currentText;
      this.function = function;
      this.type = Type.FUNCTION;
   }
   
   /**
    * Create a code possibility for a function group.
    * @param functionGroup The group that is a possibility.
    * @param currentText The current text that matches the start of this 
    * function group.
    */
   public CodePossibility( FunctionGroup functionGroup, String currentText ) 
   {
      this.name = functionGroup.getLowerCaseName();
      this.currentText = currentText;
      this.functionGroup = functionGroup;
      this.type = Type.GROUP;
   }
   
   /**
    * Create a code possibility for a keyword.
    * @param keyword The keyword that is a possibility.
    * @param currentText The current text that matches the start of this 
    * keyword.
    */
   public CodePossibility( String keyword, String currentText ) 
   {
      this.name = keyword;
      this.currentText = currentText;
      this.type = Type.KEYWORD;
   }
   
   /**
    * Get the type of the possibility.
    * @return The type of this possibility.
    */
   public Type getType() 
   {
      return type;
   }
   
   /**
    * Get the name of the possibility, lower case.
    * @return The name of the possibility object in lower case.
    */
   public String getName()
   {
      return name;
   }
      
   /**
    * The text used to match this possibility. 
    * E.g. stri for string.append()...
    * @return The text used to match this possibility.
    */
   public String getCurrentText()
   {
      return currentText;
   }

   /**
    * Get the possible function.
    * @return The function if this is possibility is of type FUNCTION, null 
    * otherwise.
    */
   public Function getFunction()
   {
      return function;
   }

   /**
    * Get the possible function group.
    * @return The group if this is possibility is of type GROUP, null 
    * otherwise.
    */
   public FunctionGroup getFunctionGroup()
   {
      return functionGroup;
   }

   /**
    * Compare this possibility to another, using the lower case name.
    */
   /* Override */
   public int compareTo( CodePossibility o )
   {
      return this.getName().compareTo( o.getName() );
   }
   
   
}
