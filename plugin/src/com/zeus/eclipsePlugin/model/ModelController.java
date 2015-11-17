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
 * This class is the master of an entire model. It contains the ZXTMs that
 * this model is representing.
 */
public abstract class ModelController extends ModelElement
{
   private PassswordCallback passwordCallback = null;

   /**
    * Get all ZXTMs in this model. Result is sorted by hostname then port.
    * @return An array of ZXTMs.
    */
   public abstract ZXTM[] getSortedZXTMs();   
   
   /**
    * Add a new ZXTM to the model, and try and update it. If the ZXTM cannot be
    * contacted this operation will fail.
    * @param hostname The hostname / IP address of the box the ZXTM is hosted on.
    * @param port The port of the ZXTM's admin server.
    * @param user The user to log in as, should probably be admin
    * @param pw The password of the user you are logging in with.
    * @return A ZXTM object representing the new ZXTM.
    * @throws Exception If the host cannot be contacted.
    */
   public abstract ZXTM addZXTM( String hostname, int port, String user, String pw ) throws ModelException;
   
   /**
    * Add a new ZXTM to the model even if the host is currently not contactable.
    * @param hostname The hostname / IP address of the box the ZXTM is hosted on.
    * @param port The port of the ZXTM's admin server.
    * @param user The user to log in as, should probably be admin
    * @param pw The password of the user you are logging in with.
    * @param disconnected If true the ZXTM will start disconnected.
    * @return A ZXTM object representing the new ZXTM.
    */
   public abstract ZXTM forceAddZXTM( String hostname, int port, String user, String pw, boolean disconnected );
  
   /**
    * Get a ZXTM object with the specified hostname and port.
    * @param hostname The hostname / IP address of the box the ZXTM is hosted on.
    * @param port The port of the ZXTM's admin server.
    * @return A ZXTM object representing the ZXTM, or null if the specified ZXTM
    * does not exist.
    */
   public abstract ZXTM getZXTM( String hostname, int port );
   
   /**
    * Removes a ZXTM from the model.
    * @param hostname The hostname / IP address of the box the ZXTM is hosted on.
    * @param port The port of the ZXTM's admin server.
    */
   public void removeZXTM( String hostname, int port )
   {
      ZXTM zxtm = this.getZXTM( hostname, port );
      if( zxtm != null ) removeZXTM( zxtm );
   }
   
   /**
    * Removes a ZXTM from the model.
    * @param zxtm The ZXTM to remove.
    */
   public abstract void removeZXTM( ZXTM zxtm );
   
   /**
    * Called when the system is shutting down.
    */
   public abstract void dispose();
   
   /**
    * Returns this models type. Always returns CONTROLLER.
    */
   /* Override */
   public Type getModelType()
   {
      return Type.CONTROLLER;
   }
   
   /**
    * Function used by sub-classes to inform listeners something has been 
    * updated.
    * @param event The event that has occurred.
    */
   protected void updateListeners( Event event ) 
   {
      for( ModelListener listener : getListeners()  ) {
         listener.modelUpdated( this, event );
      }
   }
   
   /**
    * Sub classes should call this function when a new child is added (i.e. a 
    * new ZXTM) in order to inform listeners of its arrival.
    * @param child The child that has been added.
    */
   protected void updateListenersChild( ModelElement child ) 
   {
      for( ModelListener listener : getListeners() ) {
         listener.childAdded( this, child );
      }
   }
   
   /**
    * The password callback is called when a password or needed, or one 
    * previously provided is incorrect.
    * @param callback The PasswordCallback class that will handle password 
    * problems.
    */
   public void setPasswordCallback( PassswordCallback callback )
   {
      this.passwordCallback = callback;
   }
   
   /**
    * Should be called by a ZXTM who's password is invalid in some way. Runs the
    * password callback, asking the user to provide a new password.
    * @param zxtm The ZXTM that has an invalid password.
    * @param passwordError Is this a password error? True if its an error, false
    * if its because the ZXTM never had a password.
    */
   void updatePassword( ZXTM zxtm, boolean passwordError )
   {
      if( passwordCallback != null ) {
         passwordCallback.passwordRequired( zxtm, passwordError );
      }
   }
   
   /**
    * This is a thorough check, as it resolves any hostnames. 2 ZXTMs with the 
    * same resolved IP address (and admin port) are said to be identical.
    * @param hostname The hostname of the ZXTM you want to find
    * @param port The port of the ZXTM you want to find
    * @return The ZXTM with the passed hostname/port, or null if it does not
    * exist.
    */
   public ZXTM getZXTMFull( String hostname, int port )
   {
      for( ZXTM currentZXTM : getSortedZXTMs() ) {
         if( currentZXTM.equalsFull( hostname, port ) ) {
            return currentZXTM;           
         }
      }      
      return null;
   }
      
}
