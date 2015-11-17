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

package com.zeus.eclipsePlugin.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.zeus.eclipsePlugin.Encrypter;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.operations.RefreshResourceOp;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * This class manages the .zxtmConf file on disk, which stores ZXTM specific
 * project properties.
 */
public class ProjectProperties
{
   /** The file that stores ZXTMs properties */
   public static final String PROJECT_PROPERTIES_FILE = ".zxtmConf";
   
   /** Key used to store the non-plain text password */
   public static final String CRYPT_KEY = "Crypt";

   /** Key used to store the administration port for the project's ZXTM */
   public static final String PORT_KEY = "Port";

   /** Key used to store the hostname/IP address for the project's ZXTM */
   public static final String HOSTNAME_KEY = "Hostname";
   
   /** Key used to store the user-name for authentication */
   public static final String USERNAME_KEY = "Username";
   
   private HashMap<String,String> dataTable = new HashMap<String,String>();
   
   private IProject project = null;
   private String path = null;
   private long lastLoaded = 0;

   /**
    * Setup the properties manager with a project.
    * @param project The project this class manages the ZXTM settings for.
    */
   public ProjectProperties( IProject project )
   {
      this.project = project;
      this.path = project.getLocation().toOSString();

      loadProperties();
   }
   
   /**
    * Get an appropriate BufferedReader for the properties file
    * @return The BufferedReader for the properties file, may return null if the
    * files does not exist.
    * @throws Exception If an error occurred whilst creating the reader.
    */
   private BufferedReader getPropertiesReader()  
   {
      try {
         if( path != null ) {
            File file = new File( path + File.separator + ProjectProperties.PROJECT_PROPERTIES_FILE );
            
            if( file.exists() ) {
               return new BufferedReader( new FileReader( file ) );
            } 
         }
      } catch ( IOException e ) {
         ZDebug.printStackTrace( e, "Failed to open buffered reader - ", path );
      }
            
      if( project.isAccessible() ) {
         IFile file = project.getFile( ProjectProperties.PROJECT_PROPERTIES_FILE );
         
         if( file.exists() ) {
            try {
               file.refreshLocal( IResource.DEPTH_ONE, null );
               return new BufferedReader( new InputStreamReader( file.getContents() ) );
            } catch( Exception e ) {
               // Throws if doesn't exist.
            }            
         }   
      } 
      
      return null;
   }
   
   /**
    * (Re)load the properties from disk into memory.
    */
   public void loadProperties()
   {
      try {
         BufferedReader reader = getPropertiesReader();
         dataTable.clear();
         
         if( reader == null ) return;
         
         String current = null;
         while( (current = reader.readLine()) != null ) {
            String[] data = current.split( "\\s+", 2 );
            if( data.length != 2 | data[0].startsWith( "#" ) ) continue;
            
            dataTable.put( data[0], data[1] );               
         }
         
         reader.close();
         
         lastLoaded = System.currentTimeMillis();
         
         
      } catch( IOException e ) {
         ZDebug.printStackTrace( e, "Failed to load properties - ", path );
      }
   }
   
   /**
    * Were any properties loaded? 
    * @return Returns true if the property manager loaded any properties to 
    * memory.
    */
   public boolean hasProperties()
   {
      return !this.dataTable.isEmpty();
   }
   
   /**
    * Save the current properties in memory to disk.
    * @return True if the save was successful.
    */
   public boolean saveProperties()
   {
      try {
         FileWriter out;
         if( !project.isAccessible() ) {
            File file = new File( path + File.separator + ProjectProperties.PROJECT_PROPERTIES_FILE );
            out = new FileWriter( file );
         } else {
            IFile file = project.getFile( ProjectProperties.PROJECT_PROPERTIES_FILE );
            out = new FileWriter(file.getLocation().toOSString());
         }

         for( String key : dataTable.keySet() ) {
            out.write( key + "\t" + dataTable.get( key ) + "\n" );
         }
         
         out.close();
         
         if( project.isAccessible() ) {
            SWTUtil.progressBackground( new RefreshResourceOp( project, IResource.DEPTH_ONE ), false );
         }
         
         return true;
         
      } catch( IOException e ) {
         ZDebug.printStackTrace( e, "Failed to save properties - ", path );
      } catch( InvocationTargetException e ) {
         ZDebug.printStackTrace( e, "Failed to do properties refresh - ", path );
      } 
      return false;
   }
   
   /**
    * Get a property's value. 
    * @param key The key for the property
    * @return The value of the property
    */
   public String get( String key )
   {
      return dataTable.get( key );
   }
   
   /**
    * Set the value of a property. These are only in memory until 
    * saveProperties() is called.
    * @param key The key for the property
    * @param value The new value of the property.
    */
   public void set( String key, String value )
   {
      dataTable.put( key, value );
   }
   
   /**
    * Get the last time the properties file was loaded from disk to memory.
    * @return
    */
   public long getLastLoaded()
   {
      return lastLoaded;
   }

   /**
    * Load data from a ZXTM object.
    * @param zxtm The ZXTM to store the properties of
    * @return True if the properties stored were changed/
    */
   public boolean loadZXTMData( ZXTM zxtm )
   {
      boolean changed = false;
      
      // Admin Port
      String zxtmPort = "" + zxtm.getAdminPort();    
      String port = get( ProjectProperties.PORT_KEY );
      ZDebug.print( 6, "ZXTM Port: ", zxtmPort, " vs ", port );
      if( port == null || !zxtmPort.equals( port ) ) {
         changed = true;
         set( ProjectProperties.PORT_KEY, zxtmPort );
         ZDebug.print( 6, "ZXTM Port: CHANGED" );
      }
      
      // Hostname IP address
      String hostname = get( ProjectProperties.HOSTNAME_KEY );
      ZDebug.print( 6, "ZXTM Name: ", zxtm.getHostname(), " vs ", hostname );
      if( hostname == null || !zxtm.getHostname().equals( hostname ) ) {
         changed = true;
         set( ProjectProperties.HOSTNAME_KEY, zxtm.getHostname() );
         ZDebug.print( 6, "ZXTM Name: CHANGED" );
      }
      
      // Username
      String username = get( ProjectProperties.USERNAME_KEY );
      ZDebug.print( 6, "Username: ", zxtm.getUserName(), " vs ", username );
      if( username == null || !zxtm.getUserName().equals( username ) ) {
         changed = true;
         set( ProjectProperties.USERNAME_KEY, zxtm.getUserName() );
         ZDebug.print( 6, "ZXTM Name: CHANGED" );
      }
      
      // Password
      String password = get( ProjectProperties.CRYPT_KEY );
      
      try {
         if( password != null ) password = Encrypter.decrypt( password );
      } catch (Exception e) {
         password = null;
      }
      
      if( zxtm.getStorePassword() ) {
         if( password == null || !zxtm.getPassword().equals( password ) ) {
            try {
               set( ProjectProperties.CRYPT_KEY, 
                  Encrypter.encrypt( zxtm.getPassword() )
               );
               changed = true;
               ZDebug.print( 6, "ZXTM PW: CHANGED" );
            } catch( RuntimeException e ) {
               ZDebug.printStackTrace( e, "Encrypt failed" );
            }
         }         
      } else {
         if( password != null ) {
            dataTable.remove( ProjectProperties.CRYPT_KEY );
            changed = true;
            ZDebug.print( 6, "ZXTM PW: CHANGED (No Store)" );
         }
      }
      
      return changed;
   }
   
   
}
