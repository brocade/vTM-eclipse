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

package com.zeus.eclipsePlugin.model;

/**
 * Represents an error or warning in a TrafficScript rule.
 */
public class RuleProblem
{
   protected String description;
   protected int line, start, end;
   protected boolean error;
   
   /**
    * Creates a Rule problem with line and position info.
    * @param description The problem description
    * @param line The line the problem is on 
    * @param start The start position of the problem, from the start of the file
    * @param end The end position of the problem, from the start of the file
    * @param error True if its an error, false if its a warning.
    */
   public RuleProblem( String description, int line, int start, int end, boolean error )
   {
      super();
      this.description = description;
      this.line = line;
      this.start = start;
      this.end = end;
      this.error = error;
   }

   /**
    * Get the description of the problem
    * @return The description of the problem
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * Get the line the problem occurred on
    * @return The line the problem is on
    */
   public int getLine()
   {
      return line;
   }
   
   /**
    * Get the offset into the code the problem starts at
    * @return The problems start offset
    */
   public int getStart()
   {
      return start;
   }
   
   /**
    * Get the offset into the code the problem ends at
    * @return The problems end offset
    */
   public int getEnd()
   {
      return end;
   }

   /**
    * Prints the location and description of the error
    */
   /* Override */
   public String toString()
   {      
      return "[ " + line + ", " + start + " ]  " + description;
   }

   /**
    * Is this problem an error?
    * @return True if it's an error, false if its a warning.
    */
   public boolean isError()
   {
      return error;
   }
   
   /**
    * Is this problem an error?
    * @return True if it's an warning, false if its a error.
    */
   public boolean isWarning()
   {
      return !error;
   }
   
}
