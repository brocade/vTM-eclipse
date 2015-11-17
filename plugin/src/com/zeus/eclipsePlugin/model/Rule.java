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
 * Abstract model of a ZXTM TrafficScript Rule
 */
public abstract class Rule extends ModelElement implements Comparable< Rule >
{  
   /** Always returns RULE */
   /* Override */
   public Type getModelType()
   {
      return Type.RULE;
   }
   
   /** The name of the rule. */
   public abstract String getName();
   
   /** The rule traffic script code. If this is a RuleBuilder rule the special
    * comments should NOT be returned. */
   public abstract String getTrafficScriptCode();
   
   /**
    * This is like getCode but will return the special RuleBuilder comments.
    * @return The code of this rule, including special comments.
    */
   public abstract String getRawCode();
   
   /** 
    * Any errors in the code. 
    */
   public abstract RuleProblem[] getCodeErrors();
      
   /** 
    * Set the code for this rule. This checks the code for syntax errors, and if
    * valid uploads it to the server. If invalid the implementing MAY not be 
    * able to upload the code, making the rule invalid.
    * @param code The code to upload 
    * @throws RuleCodeException If the code contains errors this exception may
    * be thrown. 
    * @throws ModelException If an error occurs whilst seting the value
    */
   public abstract boolean setCode( String code ) throws RuleCodeException, ModelException;
   
   /** 
    * Returns true if this is a rule builder rule. 
    */
   public abstract boolean isRulebuilder();
      
   /** 
    * Returns the name of the rule.
    */
   /* Override */
   public String toString()
   {
      return getName();
   }

   /**
    * Does a comparison of the rules name. Used to sort rules alphabetically 
    * using Java's sorting functions.
    */
   /* Override */
   public int compareTo( Rule o )
   {      
      return this.getName().compareTo( o.getName() );
   }

   /**
    * If the rules name and parent ZXTM are the same, the 2 rules are equal
    * @param o The rule to check equivalence with
    * @return True if the 2 rules are equal, false otherwise.
    */
   public boolean equals( Rule o ) 
   {
      ZXTM thisParent  = (ZXTM) this.getModelParent();
      ZXTM oParent  = (ZXTM) o.getModelParent();
      
      return   ( this.getName().equals( o.getName() ) ) && 
               ( thisParent.compareTo( oParent ) == 0 );
   }
   
   /**
    * Remove RuleBuilder special comments from a rule's code.
    * @param rule The code to remove the comments from
    * @return The code without the comments.
    */
   protected static String removeRulebuilderComments( String rule ) 
   {
      return rule.replaceAll( "(^|\n)#.*", "" );
   }
   
   
}
