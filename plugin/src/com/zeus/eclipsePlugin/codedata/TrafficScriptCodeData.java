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

package com.zeus.eclipsePlugin.codedata;

import java.util.HashMap;
import java.util.Iterator;

import com.zeus.eclipsePlugin.ZDebug;

/**
 * This class is used to define keywords and functions within the TrafficScript
 * language. This information is loaded from XML files.
 */
public class TrafficScriptCodeData implements Iterable< VersionCodeData >
{
   private HashMap<String, VersionCodeData > versionTable = new HashMap< String, VersionCodeData >();
   
   private CodeDataLoader dataLoader = null;  
   
   /**
    * Set the data loader callback class. This class will be used to load 
    * TrafficScriptVersions when we don't currently have one in memory.
    * @param loader The callback class that will load TrafficScript code 
    * information.
    */
   public void setDataLoader( CodeDataLoader loader )
   {
      this.dataLoader = loader;
   }
   
   /**
    * Add a TrafficScript version entry which stores information about a 
    * particular version of TrafficScript.  
    * @param ver
    */
   public void addVersion( VersionCodeData ver ) 
   {
      versionTable.put( ver.getVersionString(), ver );
   }
   
   /**
    * Looks up the specified version. If we don't have the specified version
    * we use the dataLoader callback class to try and load that version 
    * information (or the version nearest to it). 
    * 
    * @param major
    * @param minor
    * @return
    */
   public VersionCodeData getVersion( int major, int minor ) 
   {
      String ver = VersionCodeData.createVersionString( major, minor );
      VersionCodeData version = versionTable.get( ver );
      if( version != null ) return version;
      
      if( dataLoader != null ) {
         version = dataLoader.getTrafficScriptVersion( major, minor );
         if( version != null ) {
            String newVer = version.getVersionString();
            versionTable.put( newVer, version );
            
            // If the version we have been given isn't the version we want, this
            // must be the closest version available.
            if( !newVer.equals( ver ) ) {
               ZDebug.print( 5, "No exact match for ", ver, " using: ", newVer );
               versionTable.put( ver, version );
            }
            
            return version;
         }
      }
      
      return null;
   }
   
   /**
    * Returns an array of all TrafficScriptVersions currently loaded.
    * @return An array of all TrafficScriptVersions currently loaded.
    */
   public VersionCodeData[] getVersions() 
   {
      return versionTable.values().toArray( new VersionCodeData[ versionTable.size() ] );
   }

   /**
    * Iterate over all the currently loaded TrafficScriptVersions. More 
    * efficient for enumerating over the versions than using getVersions.
    * @return An iterator for all the currently loaded TrafficScriptVersion 
    * objects.
    */
   /* Override */
   public Iterator< VersionCodeData > iterator()
   {
      return versionTable.values().iterator();
   }

   
         
}
