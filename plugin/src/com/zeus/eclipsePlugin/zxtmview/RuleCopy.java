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

package com.zeus.eclipsePlugin.zxtmview;

/**
 * Used to store a rule in local memory.
 */
public class RuleCopy
{
   private String name;
   private String rule;
   
   /**
    * Create a copy of the rule.
    * @param name The name of the rule.
    * @param rule The contents of the rule. This must be the RAW contents of the
    * rule, e.g. rule.getRawCode() and NOT rule.getCode().
    */
   public RuleCopy( String name, String rule )
   {
      super();
      this.name = name;      
      this.rule = rule;
   }

   /**
    * Get the name of the rule stored in this class.
    * @return The name of the rule.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the code of the stored rule
    * @return The rules code.
    */
   public String getContents()
   {
      return rule;
   }
   
   
}
