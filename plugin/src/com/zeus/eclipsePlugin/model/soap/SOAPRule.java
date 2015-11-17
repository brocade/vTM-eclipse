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

package com.zeus.eclipsePlugin.model.soap;

import org.apache.zeusaxis.AxisFault;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.ModelError;
import com.zeus.eclipsePlugin.model.ModelException;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.RuleCodeException;
import com.zeus.eclipsePlugin.model.RuleProblem;
import com.zeus.soap.CatalogRuleRuleInfo;

/**
 * The Rule object for the SOAP model interface. Stores the code currently in 
 * the rule on ZXTM.
 */
public class SOAPRule extends Rule implements SOAPUpdatable
{
   protected SOAPZXTM parent;
   protected String name;
   
   protected String text = null, rawText = null;
   
   protected boolean isRuleBuilder = false;
   private RuleProblem[] errors = new RuleProblem[0];
   
   /**
    * Create a rule with the specified name and ZXTM parent.
    * @param parent The parent ZXTM
    * @param name The name of the rule
    */
   public SOAPRule( SOAPZXTM parent, String name )
   {
      super();
      this.parent = parent;
      this.name = name;
   }
   
   /**
    * The SOAP update method. Gets the rules contents from ZXTM and compares it
    * to what it has currently. If the contents have changed inform listeners.
    */
   /* Override */
   public synchronized boolean updateFromZXTM()
   {
      if( getModelState() == State.DELETED ) {
         ZDebug.dumpStackTrace( "Rule '", this, "' still being used after deletion!" );
         return false;
      }
      
      try {
         ZDebug.print( 5, "update() - ", name );
         
         // Get rule info from the SOAP interface
         CatalogRuleRuleInfo[] info = 
            parent.getRuleInterface().getRuleDetails( new String[] { name } );
         
         if( info.length == 0 ) {
            throw new ModelException( this, ModelError.INTERNAL,
               ZLang.bind( ZLang.ZL_SOAPRuleDoesNotExistOnZXTM, parent )
            );
         }
         
         // Store old text
         String oldText = text;         
         text = info[0].getRule_text();
         rawText = text;
         
         // Check if it's a RuleBuilder rule
         boolean wasRuleBuilder = isRuleBuilder;
         if( text.startsWith( "#CQualifier" ) ) {
            isRuleBuilder = true;
            text = removeRulebuilderComments( text );
         } else {
            isRuleBuilder = false;
         }
         
         // Check if old != new. Inform listeners if something has changed.
         ZDebug.print( 9, "Old Text:\n", oldText );
         if( oldText == null || !oldText.equals( text ) || wasRuleBuilder != isRuleBuilder ) {
            ZDebug.print( 6, "Rule changed - ", name );
            ZDebug.print( 9, "Rule contents - ", text );
            
            errors = parent.checkTrafficScriptCode( text );                        
            this.updateListeners( Event.CHANGED );
         }
         
         setModelState( State.UP_TO_DATE );
         
      // Some kind of problem has occurred!
      } catch( ModelException e ) {
         setModelState( State.CANNOT_SYNC, e );
         
      // It's bad to catch Exception, but its thrown by the axis code 
      } catch( Exception e ) {
         
         ZDebug.print( 4, "Remote Exception: ", e.getMessage() );
         text = null;
             
         // Check if its ZXTM telling us the rule doesn't exist
         if( e instanceof AxisFault ) {
            AxisFault fault = (AxisFault) e;
            
            if( fault.getFaultString().startsWith( "Unknown rule" ) ) {
               updateListeners( Event.PRE_DELETE );    
               text = null;
               setModelState( State.DELETED );
               updateListeners( Event.DELETED );                  
               return false;
            }
         }
                  
         // Otherwise its some kind of SOAP error, process it and change the 
         // model state
         setModelState( State.CANNOT_SYNC, 
            SOAPModelController.getModelException( this, e ) 
         );
      } 
      
      return true;
   }

   /**
    * Get this rule's code. Should only be called if its state is UP_TO_DATE
    */
   /* Override */
   public String getTrafficScriptCode()
   {      
      if( getModelState() == State.DELETED ) {
         ZDebug.dumpStackTrace( "Rule '", this, "' still being used after deletion!" );
         return "";
      }
        
      return (text != null) ? text : "";
   }
   
   /**
    * Returns the 'raw code'; that is without RuleBuilder comments stripped.
    */
   /* Override */
   public String getRawCode()
   {      
      if( getModelState() == State.DELETED ) {
         ZDebug.dumpStackTrace( "Rule '", this, "' still being used after deletion!" );
         return "";
      }
        
      return (rawText != null) ? rawText : "";
   }

   /**
    * Return the problems with the current code.
    */
   /* Override */
   public RuleProblem[] getCodeErrors()
   {
      return errors;
   }

   /** Get this rule name */
   /* Override */
   public String getName()
   {
      return name;
   }

   /**
    * Attempt to set the code of this rule. The code is first checked for errors
    * and warnings, if no errors are found it is sent to ZXTM. The update method
    * is then run, which should update the code inside this class.
    */
   /* Override */
   public synchronized boolean setCode( String code ) throws RuleCodeException, ModelException
   {         
      RuleProblem[] codeErrors = parent.checkTrafficScriptCode( code );
      
      // Check if we have errors (not just warnings)
      boolean hasErrors = false;
      for( RuleProblem error : codeErrors ) {
         if( error.isError() ) {
            hasErrors = true;
            break;
         }
      }
      
      // If there are errors we cannot save
      if( hasErrors ) {
         throw new RuleCodeException( this, codeErrors );
      }      
    
      try {               
         parent.getRuleInterface().setRuleText( 
            new String[] { name }, 
            new String[] { code } 
         );
         
         // This should set all the variables, e.g. text, rawText  
         updateFromZXTM(); 
         
         return true;
         
      // Something went wrong, report it with a ModelException
      } catch( Exception e ) {
         ZDebug.printStackTrace( e, "Set rule text failed - ", name );
         throw SOAPModelController.getModelException( this, e );        
      } 
   }
   
   /**
    * Return the ZXTM parent of this rule.
    */
   /* Override */
   public ModelElement getModelParent()
   {
      return parent;
   }

   /**
    * Get the update priority. All SOAP data is updated in the background in a
    * update thread, a higher priority means it will be updated more often. 
    * This is a pretty low priority.
    */
   /* Override */
   public int getPriority()
   {     
      return 30;
   }

   /** Is this a RuleBuilder rule? */
   /* Override */
   public boolean isRulebuilder()
   {
      return isRuleBuilder;
   }
   
   /**
    * This method is called to inform a rule that its name has been changed. 
    * This should only be called by it's parent ZXTM when the user requests a 
    * rename. 
    * @param newName The new name of the rule.
    */
   void renamed( String newName )
   {
      this.name = newName;
      updateFromZXTM();
      updateListeners( Event.RENAMED );
   }
}
