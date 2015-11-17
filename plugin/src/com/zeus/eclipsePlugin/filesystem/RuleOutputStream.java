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

package com.zeus.eclipsePlugin.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.ZXTM;

/**
 * Version of an byte array output stream that writes to a rule when the stream
 * is closed.
 */
public class RuleOutputStream extends ByteArrayOutputStream
{
   private ZXTM zxtm;
   private Rule rule;
   private String ruleName;
   
   /**
    * Change the contents of a rule.
    * 
    * @param ruleName
    * @param zxtm
    * @param ruleFileStore
    */
   public RuleOutputStream( Rule rule, ZXTM zxtm, RuleFileStore ruleFileStore )
   {
      this.rule = rule;
      this.ruleName = rule.getName();
      this.zxtm = zxtm;
   }
   
   /**
    * If this rule does not exist, this constructor will create it.
    * 
    * @param ruleName
    * @param zxtm
    * @param ruleFileStore
    */
   public RuleOutputStream( String ruleName, ZXTM zxtm, RuleFileStore ruleFileStore )
   {
      this.zxtm = zxtm;
      this.ruleName = ruleName;
   }

   /* Override */
   public void close() throws IOException
   {
      ZDebug.print( 4, "RuleOutputStream::close() - ", ruleName  );
      super.close();
      try {
         if( rule != null ) {
            rule.setCode( this.toString() );
         } else if( zxtm.getRule( ruleName ) != null ){
            zxtm.getRule( ruleName ).setCode( this.toString() );
         } else {
            
            try {                             
               zxtm.addRule( ruleName, this.toString() );
            } catch( Exception e ) {              
               ZDebug.printStackTrace( e, "Add rule for outstream failed: ", ruleName );
               throw new IOException( e.getMessage() );
            } 
         }
   
      } catch( Exception e ) {
         ZDebug.printStackTrace( e, "Rule outstream failed: ", ruleName );
         throw new IOException( e.getMessage() );
      }
   }
   
   
   
   
}
