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

import com.zeus.eclipsePlugin.ZLang;

/**
 * An exception that is thrown when trying to use code with errors in them.
 */
public class RuleCodeException extends Exception
{
   private static final long serialVersionUID = -8260108586119554632L;
   
   private Rule rule;
   private RuleProblem[] errors;

   /**
    * Create an exception for a rule.
    * @param rule The rule that is throwing the exception.
    * @param errors The things that were wrong with the code.
    */
   public RuleCodeException( Rule rule, RuleProblem[] errors )
   {
      super( ZLang.ZL_RuleContainedSyntaxErrors );
      this.errors = errors;
      this.rule = rule;
   }

   /**
    * Returns the rule that had these errors.
    * @return The rule that had these errors.
    */
   public Rule getRule()
   {
      return rule;
   }

   /**
    * Returns the errors this exception is reporting.
    * @return The errors this exception is reporting.
    */
   public RuleProblem[] getErrors()
   {
      return errors;
   }
      
   
}
