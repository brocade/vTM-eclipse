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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.xml.rpc.ServiceException;

import org.apache.zeusaxis.client.Call;
import org.apache.zeusaxis.client.Stub;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.editor.CodeLine;
import com.zeus.eclipsePlugin.editor.CodeUtil;
import com.zeus.eclipsePlugin.model.JavaExtension;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.ModelError;
import com.zeus.eclipsePlugin.model.ModelException;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.RuleProblem;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.soap.CatalogRuleLocator;
import com.zeus.soap.CatalogRulePort_PortType;
import com.zeus.soap.CatalogRuleSyntaxCheck;
import com.zeus.soap.SystemCacheCacheContent;
import com.zeus.soap.SystemCacheCacheContentInfo;
import com.zeus.soap.SystemCacheLocator;
import com.zeus.soap.SystemCachePort_PortType;
import com.zeus.soap.SystemCacheProtocol;
import com.zeus.soap.SystemLicenseKeysLicenseKey;
import com.zeus.soap.SystemLicenseKeysLocator;
import com.zeus.soap.SystemLicenseKeysPort_PortType;
import com.zeus.soap.SystemMachineInfoLocator;
import com.zeus.soap.SystemMachineInfoPort_PortType;
import com.zeus.soap.VirtualServerLocator;
import com.zeus.soap.VirtualServerPort_PortType;

/**
 * Implementation of the ZXTM model object for the SOAP model.
 */
public class SOAPZXTM extends ZXTM implements SOAPUpdatable
{
   protected SOAPModelController parent;
   protected SOAPUpdater updater = null;
   
   protected String user, pw;
   protected String name;
   protected int port;
   protected int major, minor;
   
   protected HashMap<String, SOAPRule> rules = new HashMap<String, SOAPRule>();
   protected SOAPRule[] sortedRules = new SOAPRule[0];
   
   boolean deleted = false;

   private boolean disconnected = false;
   
   // SOAP Interfaces
   protected VirtualServerPort_PortType vsInterface = null;
   protected CatalogRulePort_PortType ruleInterface = null;
   protected SystemMachineInfoPort_PortType infoInterface = null;
   protected SystemLicenseKeysPort_PortType licenceInterface = null;
   protected SystemCachePort_PortType cacheInterface = null;
  
   /**
    * Create a SOAPZXTM with all the details required to connect to it over 
    * SOAP.
    * @param parent The ModelController that stores this ZXTM
    * @param user The user to authenticate SOAP calls
    * @param pw The password to authenticate SOAP calls
    * @param name The hostname / IP address of the ZXTM server
    * @param port The port of the ZXTM admin server
    * @param disconnect Should this ZXTM start disconnected
    */
   public SOAPZXTM( SOAPModelController parent, String user, String pw,
      String name, int port, boolean disconnect )
   {
      super();
      this.parent = parent;
      this.user = user;
      this.pw = pw;
      this.name = name;
      this.port = port;
      
      // Create this ZXTM's updater thread
      updater = new SOAPUpdater( name + ":" + port );
      
      if( disconnect ) {
         this.disconnected = disconnect;
         this.setModelState( State.DISCONNECTED );
      } else {
         this.setModelState( State.WAITING_FOR_FIRST_UPDATE );
         updater.add( this );
      }      
   }
   
   /**
    * Start the updater thread. Must only be called once, and must be called
    * for it to update asynchronously.
    */
   public void startUpdater()
   {
      updater.start();
   }
   
   /**
    * Gets the URL to use for SOAP actions.
    * @return The URL to connect to the SOAP interface of this ZXTM.
    * @throws Exception If we can't encode the URL
    */
   public String getSOAPAdminURL() {    
      
      String url = "https://" + name + ":" + port + "/soap";      
      return url;
   }
   
   /**
    * Sets the user name and password for a SOAP interface.
    * @param soapInterface The SOAP interface to set the username and password
    * for.
    */
   private void setUserNameAndPassword( java.rmi.Remote soapInterface )
   {      
      try {
         ((Stub) soapInterface)._setProperty( Call.USERNAME_PROPERTY, user );
         ((Stub) soapInterface)._setProperty( Call.PASSWORD_PROPERTY, pw );
      } catch (Throwable e) {
         e.printStackTrace();
      }
   }
   
   /**
    * Get the VirtualServer interface. This method should always be called to 
    * use this interface, do not access it directly.
    * @return The interface to make SOAP calls with
    * @throws Exception If there was a problem creating the interface.
    */
   public synchronized VirtualServerPort_PortType getVirtualServerInterface() throws Exception 
   {      
      if( vsInterface == null ) {
         VirtualServerLocator vsl = new VirtualServerLocator();
         vsl.setVirtualServerPortEndpointAddress( getSOAPAdminURL() );
         vsInterface = vsl.getVirtualServerPort();
         setUserNameAndPassword( vsInterface );
      }
      
      return vsInterface;
   }
   
   /**
    * Get the Catalog.Rule interface. This method should always be called to 
    * use this interface, do not access it directly.
    * @return The interface to make SOAP calls with
    * @throws ServiceException If there was a problem creating the interface.
    */
   public synchronized CatalogRulePort_PortType getRuleInterface() throws ServiceException 
   {
      if( ruleInterface == null ) {
         CatalogRuleLocator rl = new CatalogRuleLocator();
         rl.setCatalogRulePortEndpointAddress( getSOAPAdminURL() );
         ruleInterface = rl.getCatalogRulePort();
         setUserNameAndPassword( ruleInterface );        
      }  
      
      return ruleInterface;
   }
   
   /**
    * Get the System.MachineInfo interface. This method should always be called
    * to use this interface, do not access it directly.
    * @return The interface to make SOAP calls with
    * @throws Exception If there was a problem creating the interface.
    */
   public synchronized SystemMachineInfoPort_PortType getMachineInfoInterface() throws Exception 
   {
      if( infoInterface == null ) {
         SystemMachineInfoLocator il = new SystemMachineInfoLocator();
         il.setSystemMachineInfoPortEndpointAddress( getSOAPAdminURL() );
         infoInterface = il.getSystemMachineInfoPort();
         setUserNameAndPassword( infoInterface );        
      }  
      
      return infoInterface;
   }
   
   /**
    * Get the System.LicenceKeys interface. This method should always be called
    * to use this interface, do not access it directly.
    * @return The interface to make SOAP calls with
    * @throws Exception If there was a problem creating the interface.
    */
   public synchronized SystemLicenseKeysPort_PortType getLicenceKeyInterface() throws Exception 
   {
      if( licenceInterface == null ) {
         SystemLicenseKeysLocator ll = new SystemLicenseKeysLocator();
         ll.setSystemLicenseKeysPortEndpointAddress( getSOAPAdminURL() );
         licenceInterface = ll.getSystemLicenseKeysPort();
         setUserNameAndPassword( licenceInterface );
      }  
      
      return licenceInterface;
   }
   
   /**
    * Get the System.LicenceKeys interface. This method should always be called
    * to use this interface, do not access it directly.
    * @return The interface to make SOAP calls with
    * @throws Exception If there was a problem creating the interface.
    */
   public synchronized SystemCachePort_PortType getCacheInterface() throws Exception 
   {
      if( cacheInterface == null ) {
         SystemCacheLocator ll = new SystemCacheLocator();
         ll.setSystemCachePortEndpointAddress( getSOAPAdminURL() );
         cacheInterface = ll.getSystemCachePort();
         setUserNameAndPassword( cacheInterface );
      }  
      
      return cacheInterface;
   }
   
   /**
    * Deletes all the SOAP interfaces, will be recreated next time they are 
    * used.
    */
   private void resetSOAPInterfaces()
   {
      ruleInterface = null;
      vsInterface = null;
      infoInterface = null;
      licenceInterface = null;
   }
         
   /**
    * Updates the server information, including which rules the ZXTM 
    * has. If anything goes wrong an exception is thrown
    * @return False if this ZXTM should not be updated any more, such as when it 
    * is deleted.
    * @throws Exception If a error occurs whilst updating the information on the
    * ZXTM.
    */
   public synchronized boolean updateAndThrow() throws Exception
   {      
      ZDebug.print( 6, "update() - ", this  );
      
      if( deleted ) {
         return false;
      }
           
      // No updates as we are disconnected from this ZXTM currently
      if( disconnected ) {
         this.setModelState( State.DISCONNECTED );
         return true;
      }
      
      // If no password is set (because it was not stored with the project),
      // show a dialog if the workspace is ready.
      if( pw == null ) {
         setModelState( State.WAITING_FOR_FIRST_UPDATE );
         passwordRequired( false );
         return true;
      }
               
      try {
	      // Check it can edit TrafficScript (Not an S?LB)
	      SystemLicenseKeysPort_PortType licence = getLicenceKeyInterface();
	      SystemLicenseKeysLicenseKey[] currentLicence = 
	         licence.getLicenseKeys( new int[] { licence.getCurrentLicenseKey() } 
	      );
	      
	      if( currentLicence.length >= 1 ) {
	         ZDebug.print( 7, "Features: ", ZUtil.join( ", ", currentLicence[0].getFeatures() ) );
	         for( String feature : currentLicence[0].getFeatures() ) {
	            if( feature.equals( "ZXTM_RULEBUILDER" ) ) {
	               throw new ModelException( this, ModelError.NO_TRAFFIC_SCRIPT );
	            }
	         }
	      }
      } catch ( org.apache.zeusaxis.AxisFault e ) {
         // Bug in Traffic Manager causes this exception to be raised in
         // developer mode.  However, developer mode permits editing of
         // TrafficScript, so do nothing here.
         if( ! (e.detail instanceof org.xml.sax.SAXException ) ) {
            throw e;
         }
         ZDebug.print( 8 , e.getFaultReason() );
      }
      
      // Update the list of rules
      updateRules();
      
      // Update the version information
      String versionString = getMachineInfoInterface().getProductVersion();
      String[] verParts = versionString.split( "[^\\d]+" );
      major = Integer.parseInt( verParts[0] );
      minor = Integer.parseInt( verParts[1] );
      ZDebug.print( 7, "ZXTM Version: ", major, ".", minor );
      
      
      // TODO Delete below!
      
      SystemCacheCacheContentInfo results = 
         getCacheInterface().getCacheContent( SystemCacheProtocol.http, "*", "*", 100 );
      
      for( SystemCacheCacheContent item : results.getMatching_items() ) {
         System.out.println( "Cache hits for '" + item.getPath() + "': " + item.getHits() );  
      }      
            
      setModelState( State.UP_TO_DATE );
              
      return true;
   }
   
   /**
    * Updates the rule listings, and creates new SOAPRule objects for any new
    * rules on ZXTM.
    * @throws Exception If SOAP communications fail.
    */
   private synchronized void updateRules() throws Exception
   {
      String[] rulesNames = getRuleInterface().getRuleNames();
      
      // Hash that will replace the old rules hash
      HashMap<String, SOAPRule> newRules = new HashMap<String, SOAPRule>();
      
      // Store any new rules so that we can inform listeners at the end.
      LinkedList<SOAPRule> addedRulesList = new LinkedList<SOAPRule>();
      
      // Add rules on the server to the new hash
      for( String ruleName : rulesNames ) {
         if( rules.get( ruleName ) == null ) {
            ZDebug.print( 5, "Rule added: ", ruleName );
            SOAPRule newRule = new SOAPRule( this, ruleName );
            newRules.put( ruleName, newRule );
            addedRulesList.add( newRule );
            newRule.updateFromZXTM();
         } else {
            newRules.put( ruleName, rules.get( ruleName ) );
         }
      }
      
      // Check if any rules have been deleted on the server
      for( String ruleName : rules.keySet() ) {
         if( newRules.get( ruleName ) == null ) {
            ZDebug.print( 5, "Rule ", ruleName, " deleted" );
            SOAPRule deletedRule = rules.get( ruleName );
            updater.remove( deletedRule );
         }
      }
      
      // Change the data structure to the new version
      rules = newRules;
      sortedRules = rules.values().toArray( new SOAPRule[rules.size()] );
      Arrays.sort( sortedRules );
      
      // Update listeners with the new children
      for( SOAPRule rule : addedRulesList ) {
         updateListenersChild( rule );
         updater.add( rule );
      }
   }

   /**
    * This method is called by the SOAP updater. If there is a problem updating
    * ZXTMs information the state is set to cannot sync.
    */
   /* Override */
   public synchronized boolean updateFromZXTM()
   {
      try {
         return updateAndThrow();
         
      } catch( Exception e ) {
         // Invalidate children
         clearData();
         
         ModelException modelException = SOAPModelController.getModelException( this, e );
         
         // If there's a authorisation error ask the user for a new password.
         if( modelException.getError() == ModelError.AUTH_FAILED ) {
            passwordRequired( true );
         }
         
         this.setModelState( State.CANNOT_SYNC, modelException );
      }
      return true;
   }
   
   /**
    * Set the password. This checks the password is valid and throws an 
    * exception if its not (or there was a problem communicating with the 
    * server).
    */
   /* Override */
   public synchronized void setPassword( String password ) throws ModelException
   {
      String oldPw = pw;
      pw = password;
      
      // Destroy all the SOAP interface classes, so that they get recreated 
      // with the new password.
      resetSOAPInterfaces();
      
      if( this.getModelState() == State.CANNOT_SYNC ) {
         setModelState( State.WAITING_FOR_FIRST_UPDATE );
      }
      
      try {
         updateAndThrow();
      } catch( Exception e ) {
         pw = oldPw;
         resetSOAPInterfaces();
         
         throw SOAPModelController.getModelException( this, e );
      }
   }
   
   /**
    * Set the user-name. This checks the user-name is valid and throws an 
    * exception if its not (or there was a problem communicating with the 
    * server).
    */
   /* Override */
   public synchronized void setUserName( String username ) throws ModelException
   {
      ZDebug.print( 3, "setUserName( ", username, " )" );
      String oldUser = user;
      user = username;
      
      // Destroy all the SOAP interface classes, so that they get recreated 
      // with the new password.
      resetSOAPInterfaces();
      
      if( this.getModelState() == State.CANNOT_SYNC ) {
         setModelState( State.WAITING_FOR_FIRST_UPDATE );
      }
      
      try {
         updateAndThrow();
      } catch( Exception e ) {
         user = oldUser;
         resetSOAPInterfaces();
         
         throw SOAPModelController.getModelException( this, e );
      }
   }
   
   /**
    * Set the user-name and password.
    */
   /* Override */
   public synchronized void setUserAndPassword( String username, String password ) throws ModelException
   {
      ZDebug.print( 3, "setUserAndPassword( ", username, ", ? )" );
      
      String oldUser = user;
      user = username;
      
      String oldPw = pw;
      pw = password;
      
      // Destroy all the SOAP interface classes, so that they get recreated 
      // with the new password.
      resetSOAPInterfaces();
      
      if( this.getModelState() == State.CANNOT_SYNC ) {
         setModelState( State.WAITING_FOR_FIRST_UPDATE );
      }
      
      try {
         updateAndThrow();
      } catch( Exception e ) {
         user = oldUser;
         pw = oldPw;
         resetSOAPInterfaces();
         
         throw SOAPModelController.getModelException( this, e );
      }
   }

   /**
    * Adds a rule to ZXTM with the supplied contents. The contents must be 
    * valid, or this class will throw an exception. It will also throw an 
    * exception if a communication error occurs.
    */
   /* Override */
   public synchronized void addRule( String name, String code ) throws ModelException
   {
      name = name.trim();
      SOAPRule newRule = null;
      try {
         getRuleInterface().addRule( new String[] { name }, new String[] { code } );
         
         this.updateAndThrow();
         if( this.getRule( name ) == null ) {
            throw new ModelException( this, ModelError.INTERNAL,
               ZLang.ZL_SOAPRuleAddSucceededButDoesNotExist
            );
         }
     
      } catch( Exception e ) {
         if( newRule != null ) updater.remove( newRule );
         throw SOAPModelController.getModelException( this, e );
      }
   }
   
 
   /**
    * Add a blank rule, just calls addRule( name, content ) with a blank string.
    */
   /* Override */
   public synchronized void addRule( String name ) throws ModelException
   {
      addRule( name, "" );
   }

   /**
    * Delete a rule from ZXTM. Throws an exception if something goes wrong.
    */
   /* Override */
   public synchronized void deleteRule( String rule ) throws ModelException
   {
      rule = rule.trim();
      try {
         if( this.getRule( rule ) == null ) {
            throw new ModelException( this, ModelError.INTERNAL,
               ZLang.ZL_SOAPCouldNotDeleteDoesntExist 
            );
         }
         
         getRuleInterface().deleteRule( new String[] { rule } );
         
         this.updateAndThrow();
         if( this.getRule( rule ) != null ) {
            throw new ModelException( this, ModelError.INTERNAL,
               ZLang.ZL_SOAPRuleDeleteSuccessButNotDeleted 
            );
         }
         
         this.updateListeners( Event.CHANGED );
         
      } catch( Exception e ) {
         ZDebug.printStackTrace( e, "Delete rule failed - ", this );
         throw SOAPModelController.getModelException( this, e );
      }
   }
   
   /**
    * Rename a rule to the new name. Old rule must exist and new name must not 
    * already exist. Exception thrown if anything goes wrong.
    */
   public synchronized void renameRule( String oldName, String newName ) throws ModelException
   {
      newName = newName.trim();
      
      // Does this rule exist?
      if( this.getRule( oldName ) == null ) {
         throw new ModelException( this, ModelError.INTERNAL, 
            ZLang.ZL_SOAPRuleDoesNotExist 
         );
      }
      
      try {
         SOAPRule rule = rules.get( oldName );
         
         // We need the lock on the rule so that it can't update (and thinks it's
         // deleted) while we're doing the rename.
         synchronized( rule ) {
            getRuleInterface().renameRule( new String[] { oldName }, new String[] { newName } );
            
            rules.remove( oldName );
            rules.put( newName, rule );
            
            rule.renamed( newName );
         }
         
      } catch( Exception e ) {
         ZDebug.printStackTrace( e, "Rename rule failed - ", this );
         throw SOAPModelController.getModelException( this, e );
      }
   }
   
   /**
    * Disconnect this ZXTM. This stops it talking to ZXTM via SOAP.
    */
   /* Override */
   public synchronized void setDisconnected( boolean value )
   {
      if( disconnected == value ) return;
      
      disconnected = value;
      
      if( disconnected ) { 
         clearData();
         this.setModelState( State.DISCONNECTED );     
         updater.remove( this );
      } else {
         this.setModelState( State.WAITING_FOR_FIRST_UPDATE );
         updater.add( this );
      }     
   }

   /** Get the ZXTM's administration port */
   /* Override */
   public int getAdminPort()
   {      
      return port;
   }

   /** 
    * Get the ZXTMs java extensions.
    * SFT: currently not implemented
    */
   /* Override */
   public JavaExtension[] getJavaExtentions()
   {
      return null;
   }

   /** Get the ZXTMs hostname */
   /* Override */
   public String getHostname()
   {
      return name;
   }

   /** Gets a sorted array of rules*/
   /* Override */
   public Rule[] getRules()
   {      
      return sortedRules;
   }

   /** Get this ZXTMs password */
   /* Override */
   public String getPassword()
   {
      return pw;
   }

   /** Get the user-name used to authenticate SOAP calls */
   /* Override */
   public String getUserName()
   {
      return user;
   }

   /** Returns the SOAP model controller */
   public SOAPModelController getSOAPParent()
   {
      return parent;
   }

   /** Returns the SOAP model controller */
   /* Override */
   public ModelElement getModelParent()
   {
      return parent;
   }

   /** Get the rule with this name, or null if it does not exist. */
   /* Override */
   public Rule getRule( String name )
   {      
      return rules.get( name );
   }
   
   /** Get this ZXTM's major version number */
   /* Override */
   public int getMajorVersion()
   {
      return major;
   }

   /** Get this ZXTM's minor version number */
   /* Override */
   public int getMinorVersion()
   {   
      return minor;
   }

   /** Get this ZXTMs priority. Relatively high. */
   /* Override */
   public int getPriority()
   {     
      return 50;
   }
      
   /** 
    * This method should be called if the ZXTM is deleted. It stops the update 
    * thread.
    */ 
   void deleted()
   {
      this.deleted = true;
      clearData();
      updater.stop();
      this.updateListeners( Event.PRE_DELETE );
      this.setModelState( State.DELETED );
      this.updateListeners( Event.DELETED );
   }
   
   /**
    * If the settings become invalid (this ZXTM is disconnected or there is a
    * problem communicating to ZXTM) this method should be run.
    */
   private void clearData()
   {
      for( SOAPRule rule : sortedRules ) {
         updater.remove( rule );
      }
      
      rules.clear();
      sortedRules = new SOAPRule[0];
   }

   /**
    * Method processes the rule error information from ZXTM.
    * @param list List to add the errors to.
    * @param input The string containing error information.
    * @param error Is this an error or warning string.
    * @param lineStarts Information on each line of the rule.
    */
   private void processErrors( LinkedList<RuleProblem> list, String input, boolean error, CodeLine[] lineStarts ) {
      StringTokenizer tk = new StringTokenizer( input, "\n" );
      while( tk.hasMoreTokens() ) {
         String line = tk.nextToken();
         if( line.startsWith( "Error:" ) || line.startsWith( "Warning:" ) ) {
            String numStart = line.substring( line.indexOf( "line " ) + 5 );
            String num = numStart.substring( 0, numStart.indexOf( ':' ) );
                              
            String desc = numStart.substring( numStart.indexOf( ':' ) + 1 );
            desc = desc.trim();
            desc = desc.substring( 0, 1 ).toUpperCase() + desc.substring( 1 );
            
            ZDebug.print( 5, "Error/Warning: line ", num, ": ", desc );      
            int lineNum = Integer.parseInt( num );
            
            list.add( new RuleProblem( 
               desc, lineNum, 
               lineStarts[ZUtil.capRange( lineStarts, lineNum-1 )].codeStart, 
               lineStarts[ZUtil.capRange( lineStarts, lineNum-1 )].codeEnd, 
               error 
            ) );
            
         }
      }
   }
   
   /**
    * Check the passed code. Returns RuleProblem objects if there are problems 
    * with the rule.
    */
   /* Override */
   public RuleProblem[] checkTrafficScriptCode( String code )
   {
      if( getModelState() == State.DELETED ) {
         System.err.println( "Rule Still being used after deletion!" );
         Thread.dumpStack();
         return new RuleProblem[0];
      }
      
      try {
         CatalogRuleSyntaxCheck[] errors = getRuleInterface().checkSyntax( 
            new String[] { code } 
         );
         
         if( errors.length != 1 ) { 
            throw new IOException( ZLang.ZL_SOAPZXTMDidNotReturnCorrectNumberOfRules );
         }
         
         CatalogRuleSyntaxCheck error = errors[0];      
                                     
         ZDebug.print( 4, "Errors:\n", error.getErrors()  );
         ZDebug.print( 4, "Warnings:\n", error.getWarnings() );
         
         CodeLine[] lineStarts = CodeUtil.getAllLineAreas( code );
         LinkedList<RuleProblem> list = new LinkedList<RuleProblem>();
         
         if( error.getErrors() != null ) {
            processErrors( list, error.getErrors(), true, lineStarts );
         }            
         if( error.getWarnings() != null ) {
            processErrors( list, error.getWarnings(), false, lineStarts );
         }
         
         return list.toArray( new RuleProblem[list.size()] );
       
         
      } catch( Exception e ) {
         ZDebug.printStackTrace( e, "Exception whilst checking code:\n", code, "\n" );
      }
      
      return new RuleProblem[] {};  
   }

}
