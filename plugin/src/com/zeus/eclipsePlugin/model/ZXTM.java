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

import java.net.InetAddress;

/**
 * Represents a ZXTM.
 */
public abstract class ZXTM extends ModelElement implements Comparable< ZXTM >
{
   private boolean storePassword;
  
   /* Override */
   public Type getModelType()
   {
      return Type.ZXTM;
   }
      
   /**
    * Returns the host-name of this ZXTM.
    * @return The host-name of this ZXTM or null if non is set (will have an IP address instead).
    */
   public abstract String getHostname();
         
   /**
    * Returns the admin port of this ZXTM.
    * @return The admin port.
    */
   public abstract int getAdminPort();
   
   /**
    * Returns the user name used to log in to the admin interface/soap.
    * @return The user name used to login to this ZXTM.
    */
   public abstract String getUserName();
   
   /**
    * Returns the password used to login to the admin interface / soap.
    * @return The password used to login to this ZXTM.
    */
   public abstract String getPassword();
   
   /**
    * Set the user name for this ZXTM.
    * @param username The user-name to authenticate with ZXTM.
    * @throws ModelException If the password is invalid, or a communication error 
    * occurs.
    */
   public abstract void setUserName( String username ) throws ModelException;
   
   /**
    * Set the password of this ZXTM.
    * @param password The password to access this ZXTMs administration password.
    * @throws ModelException If the password is invalid, or a communication error 
    * occurs.
    */
   public abstract void setPassword( String password ) throws ModelException;
   
  /**
   * Set the user-name and password of this ZXTM.
   * @param user The user-name to connect to ZXTM with.
   * @param password The password to connect to ZXTM with.
   */
   public abstract void setUserAndPassword( String user, String password ) throws ModelException;
   
   /**
    * Should the password be stored on disk?
    * @return Returns true if this password should be stored locally, false 
    * otherwise;
    */
   public boolean getStorePassword()
   {
      return storePassword;
   }
   
   /**
    * Should the password be stored on disk?
    * @param storePassword Set to true to store the password on disk.
    */
   public void setStorePassword( boolean storePassword )
   {
      this.storePassword = storePassword;
   }

   /**
    * Add a new blank rule to this ZXTM.
    * @param name The name of the new rule.
    */
   public abstract void addRule( String name ) throws ModelException;
   
   /**
    * Add a new rule to this ZXTM, with the specified code.
    * @param name The name of the new rule.
    * @param code The TS code.
    */
   public abstract void addRule( String name, String code ) throws ModelException;
   
   /**
    * Delete a rule from this ZXTM.
    * @param name The name of the rule to delete.
    * @throws Exception If delete operation failed.
    */
   public abstract void deleteRule( String name ) throws ModelException;
   
   /**
    * Get all the rules in this ZXTM. Sorted alphabetically by rule name.
    * @return The rules in this ZXTM.
    */
   public abstract Rule[] getRules();
   
   /**
    * Get a specific rule by name.
    * @param name The name of the rule you want.
    * @return The requested rule, or null if it does not exist.
    */
   public abstract Rule getRule( String name );
   
   /**
    * Rename the specified rule.
    * @param oldName The name of the rule currently.
    * @param newName What you want the rule to be named.
    * @throws Exception If the operation fails.
    */
   public abstract void renameRule( String oldName, String newName ) throws ModelException;
   
   /**
    * Get all Java Extensions in this ZXTM.
    * @return An array of JavaExtension objects.
    */
   public abstract JavaExtension[] getJavaExtentions();
   
   /**
    * Returns a pretty string representing this ZXTM.
    */
   /* Override */
   public String toString()
   {
      return getHostname() + ":" + getAdminPort();
   }
   
   /**
    * Disconnect this ZXTM (stop it updating)
    * @param value Set to true if you want the ZXTM to stop updating.
    */
   public abstract void setDisconnected( boolean value );
   
   /**
    * Get the major version of this ZXTM. E.g. for 5.1r1 return 5
    * @return The major version of this ZXTM;
    */
   public abstract int getMajorVersion();
   
   /**
    * Get the minor version of this ZXTM. E.g. for 5.1r1 return 1
    * @return The minor version of this ZXTM;
    */
   public abstract int getMinorVersion();
   
   /**
    * Get the name and port of this ZXTM as a string (e.g. foo:9090)
    * @return The hostname and port of this ZXTM.
    */
   public String getNamePort()
   {      
      return getHostname() + ":" + getAdminPort();
   }
   
   /**
    * Checks the passed code for problems.
    * @param text The code to check
    * @return Any problems with the code.
    */
   public abstract RuleProblem[] checkTrafficScriptCode( String text );
   
   /**
    * Compare the hostname of this and another, if any of the resolved IP 
    * addresses match then the 2 hostnames are the same.
    * @param host The hostname to compare with this one.
    * @return If the 2 hostnames are equal returns 0. 
    */
   private int hostNameCompare( String host ) 
   {
      try {
         InetAddress[] otherAddrs = InetAddress.getAllByName( host );
         InetAddress[] thisAddrs = InetAddress.getAllByName( this.getHostname() );
         
         for( InetAddress thisAddr : thisAddrs ) {
            for( InetAddress otherAddr : otherAddrs ) {
               if( thisAddr.equals( otherAddr ) ) {
                  return 0;
               }
            }
         }
         
      } catch( Exception e ) {}
      
      return this.getHostname().compareTo( host );
   }

   /**
    * Compare this ZXTM to another using hostname and port.
    * @param host The host/IP address of the ZXTM to compare this ZXTM to
    * @param port The port of the ZXTM you are comparing this ZXTM to
    * @param fast Should this be a quick check (no hostname resolving checks)
    * @return The comparison value of the passed ZXTM and this ZXTM.
    */
   private int compareTo( String host, int port, boolean fast ) 
   {
      int comp = this.getHostname().compareTo( host );      
      if( comp != 0 && !fast ) {
         comp = hostNameCompare( host );
      }
      
      if( comp != 0 ) {
         return comp;
      }
      
      return this.getAdminPort() - port;
   }
   
   /** Compares one ZXTM to another, does NOT perform hostname resolving */
   /* Override */
   public int compareTo( ZXTM o )
   {    
      return compareTo( o.getHostname(), o.getAdminPort(), true );
   }
   
   /** Returns true if the 2 ZXTMs hostname and port are the same */
   public boolean equals( ZXTM zxtm )
   {
      return this.compareTo( zxtm ) == 0;
   }
   
   /**
    * Like the standard equals function, but also resolves hostnames to make 
    * sure the IP addresses aren't the same, so may take a bit longer.
    * @param name The host name of the ZXTM you're checking against.
    * @param port The admin port of the ZXTM you are checking.
    * @return True if the passed ZXTM is the same as this one.
    */
   public boolean equalsFull( String name, int port ) 
   {
      return this.compareTo( name, port, false ) == 0;
   }
   
   /**
    * Should be called by subclasses when the password needs to be updated 
    * because it is invalid.
    * @param passwordError True if this is being called because the current 
    * password is invalid.
    */
   protected void passwordRequired( boolean passwordError ) 
   {
      ((ModelController) getModelParent()).updatePassword( this, passwordError );
   }

}
