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

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;

import javax.net.ssl.SSLException;

import org.apache.zeusaxis.AxisFault;
import org.apache.zeusaxis.AxisProperties;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.ModelError;
import com.zeus.eclipsePlugin.model.ModelException;
import com.zeus.eclipsePlugin.model.ZXTM;

/**
 * The main class for the SOAP Model. Responsible for updating ZXTMs and 
 * allowing users to add and remove new ZXTMs.
 */
public class SOAPModelController extends ModelController
{
   protected HashMap <String,SOAPZXTM> connectedZXTMs = new HashMap <String,SOAPZXTM> ();
   protected SOAPZXTM[] zxtms = new SOAPZXTM[0];
   
   /**
    * Disables certificate checking for Axis only.
    */
   public SOAPModelController()
   {
      AxisProperties.setProperty(
         "axis.socketSecureFactory",
      	"org.apache.zeusaxis.components.net.SunFakeTrustSocketFactory"
      ); 
   }
   
   /**
    * Generates the key we use to add ZXTMs to the connectedZXTMs hash.
    * @param zxtm The ZXTM to get the unique key for.
    * @return The unique key for this ZXTM.
    */
   private String zxtmKey( ZXTM zxtm )
   {
      return zxtmKey( zxtm.getHostname(), zxtm.getAdminPort() );
   }
   
   /**
    * Generates the key we use to add ZXTMs to the connectedZXTMs hash.
    * @param hostname The hostname of the ZXTM
    * @param port The admin port of the ZXTM.
    * @return The unique key for this ZXTM.
    */
   private String zxtmKey( String hostname, int port )
   {
      return "[" + hostname + "]:" + port;
   }

   /**
    * Create a ZXTM object and do a full update of it. If anything fails throw
    * an exception.
    */
   /* Override */
   public synchronized ZXTM addZXTM( String hostname, int port, String user, String pw ) throws ModelException
   {      
      SOAPZXTM zxtm = null;
      
      if( connectedZXTMs.containsKey( zxtmKey( hostname, port ) ) ) {
         throw new ModelException( this, ModelError.ELEMENT_EXISTS );
      }
      
      try {
         // Create ZXTM object and connect
         zxtm = new SOAPZXTM( this, user, pw, hostname, port, false );
         zxtm.updateAndThrow(); 
           
         // Connection succeeded put it into the internal store
         connectedZXTMs.put( zxtmKey( hostname, port ), zxtm );
         updateZXTMArray();
         updateListenersChild( zxtm );
         
         zxtm.startUpdater();
         
         return zxtm;
      
      } catch( Exception e ) {
         if( zxtm != null ) zxtm.deleted();
         if( e instanceof ModelException ) throw (ModelException) e;
         throw getModelException( null, e );
      }      
   }
   
   /**
    * Create a ZXTM object but do not update it. This operation should always
    * succeed.
    */
   /* Override */
   public synchronized ZXTM forceAddZXTM( String hostname, int port, String user, String pw, boolean disconnect )
   {
      if( connectedZXTMs.containsKey( zxtmKey( hostname, port ) ) ) {
         return connectedZXTMs.get( zxtmKey( hostname, port ) );
      }
      
      SOAPZXTM zxtm = new SOAPZXTM( this, user, pw, hostname, port, disconnect );
      connectedZXTMs.put( zxtmKey( hostname, port ), zxtm );
      updateZXTMArray();
      updateListenersChild( zxtm );
      zxtm.startUpdater();
      return zxtm;
   }

   /**
    * Get the ZXTM with the passed hostname and port. Returns null if none is 
    * found.
    */
   /* Override */
   public ZXTM getZXTM( String hostname, int port )
   {
      return connectedZXTMs.get( zxtmKey( hostname, port ) );
   }

   /**
    * Get a sorted list of ZXTMs.
    */
   /* Override */
   public ZXTM[] getSortedZXTMs()
   {
      return zxtms;
   }

   /**
    * Remove a ZXTM from the controller. This stops it updating and sets its 
    * state to deleted.
    */
   /* Override */
   public synchronized void removeZXTM( ZXTM zxtm )
   {
      SOAPZXTM soapZXTM = connectedZXTMs.get( zxtmKey( zxtm ) );
      if( soapZXTM == null ) return;
      
      connectedZXTMs.remove( zxtmKey( zxtm ) );
      updateZXTMArray();
      soapZXTM.deleted();      
   }
   
   /**
    * This is the root element in the model so always returns null.
    */
   /* Override */
   public ModelElement getModelParent()
   {
       return null;
   }
   
   /**
    * Remove all ZXTMs
    */
   /* Override */
   public void dispose()
   {
      for( SOAPZXTM zxtm : zxtms ) {
         zxtm.deleted();
      }
   }

   /**
    * Update the sorted array of ZXTMs.
    */
   private void updateZXTMArray()
   {
      zxtms = connectedZXTMs.values().toArray( new SOAPZXTM[connectedZXTMs.size()] );
      Arrays.sort( zxtms );
   }
   
   /**
    * Process exceptions thrown by axis, and format them in a pretty exception.
    * Use the ModelError enum to easily determine what went wrong.
    * @param e The exception that was thrown.
    */
   public static ModelException getModelException( ModelElement source, Exception e ) 
   {
      ZDebug.print( 3, "getModelException( ", e, " )" );
    
      if( e instanceof ModelException ) {
         return (ModelException) e;
      }
      
      // Connection problems
      if( e.getCause() != null ) {
         Throwable cause = e.getCause();
         if( cause instanceof ConnectException ) {
            return new ModelException( source, ModelError.CONNECTION_REFUSED, 
               e 
            );
         } else if( cause instanceof UnknownHostException ) {
            return new ModelException( source, ModelError.CANNOT_RESOLVE, e );
         } else if( cause instanceof SSLException ) {
            return new ModelException( source, ModelError.SSL_ERROR, cause.getMessage(), e );
         }
      }
      
      // Axis fault
      if( e instanceof AxisFault ) {
         AxisFault fault = (AxisFault) e;
         
         if( fault.getFaultReason().startsWith( "(401)" ) ) {            
            return new ModelException( source, ModelError.AUTH_FAILED, e );
            
         } else if( fault.getFaultString().contains( "MalformedByte" ) ) {
            return new ModelException( source, ModelError.BAD_SOAP_RESPONSE, 
               ZLang.ZL_SOAPResponseContainedMalformedBytes, e 
            );
                     
         } else {
            return new ModelException( source, ModelError.BAD_SOAP_RESPONSE, 
               fault.getFaultString(), e 
            );
         }
      }
      
      // Unknown problems
      ZDebug.printStackTrace( e, "Unknown exception in SOAP model for ", source );
      return new ModelException( source, ModelError.UNKNOWN, e.getMessage(), e );      
   }
   
}
