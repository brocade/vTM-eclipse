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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.project.ZXTMProject;

/**
 * This class is the main class for the ZXTM file-system. It is responsible for 
 * decoding URIs and creating file store objects (classes that represent a file
 * in the file-system), when they are requested by eclipse.
 */
public class ZXTMFileSystem extends FileSystem
{
   /** 
    * The file extension for TrafficScript rules in the file-system. 
    * IMPORTANT: Must match the settings in plugin.xml
    */
   public static final String TS_FILE_EXTENSION = ".zts";
   
   /** The linked directory that stored the rules in the ZXTM project */
   public static final String RULE_PATH = "Rules";
   
   /**
    * The URL protocol for the ZXTM file-system. E.g. zxtm://foo:9090/
    * IMPORTANT: Must match the settings in plugin.xml
    */
   public static final String PROTOCOL = "zxtm";
   
   private HashMap< String, IFileStore > filesTable = new HashMap< String, IFileStore >();
  
      
   /** Default constructor, writes some debug info. */
   public ZXTMFileSystem()
   {
      ZDebug.print( 1, "ZXTMFileSystem() - ", this.hashCode() );
   }
   
   /**
    * Gets the appropriate file-store for the URI. Should always return 
    * something, even if the files does not exist. Also should always return 
    * the same file store for the same URI.
    */
   /* Override */
   public IFileStore getStore( URI uri )
   {
    
      uri = uri.normalize();
      ZDebug.print( 3, "getStore( ", uri, " )"   );
      
      // If host is null this is not valid at all
      if( uri.getHost() == null ) {
         ZDebug.print( 4, "Returning EmptyFileStore, no host in URI"  );
         return new EmptyFileStore( this, uri );            
      }
      
      // Check for alternate names ending or missing '/'
      IFileStore hashedStore = filesTable.get( uri.toString() );
      if( hashedStore == null ) {
         String uString = uri.toString();
         if( uString.endsWith( "/" ) ) {
            uString = uString.substring( 0, uString.length() - 1 );
         } else {
            uString += "/";
         }
         
         hashedStore = filesTable.get( uString );
      }
      
      // If we have a hashed file store return that.
      if( hashedStore != null && !(hashedStore instanceof EmptyFileStore) ) {
         ZDebug.print( 4, "returning hashed File: ", hashedStore  );
         return hashedStore;
      }
      
      ModelController controller = ZXTMPlugin.getDefault().getModelController();
      
      // Find the ZXTM for the URI
      ZXTM zxtm = controller.getZXTM( uri.getHost(), uri.getPort() );
      if( zxtm == null ) {
         ZDebug.print( 5, "ZXTM is null, searching for project."  );
         
         IProject project = ZXTMProject.getProjectForZXTM( uri.getHost(), uri.getPort() );
         if( project == null ) {
            ZDebug.print( 5, "Could not find project!"  );
         } else {
            zxtm = ZXTMProject.getZXTMForProject( project );
         }       
                  
         if( zxtm == null ) {
            ZDebug.print( 5, "Could not find zxtm!"  );
            return new EmptyFileStore( this, uri );
         }
      }
      
      ZDebug.print( 6, "ZXTM for ", uri, " is ", zxtm );
      
      // Split up the path to get the directories in it
      String pathLong[] = uri.getRawPath().split( "[\\/\\\\]" );
      String path[] = new String[Math.max( pathLong.length - 1, 0 )];
      
      for( int i = 1; i < pathLong.length; i++ ) {
         path[i-1] = dec( pathLong[i] );
      }
      
      ZDebug.print( 8, (Object[]) path );
      
      //// Determine the file store to return ////
      
      // No directories, they want the ZXTM root
      if( path.length == 0 ) {
         ZDebug.print( 5, " Path length 0"   );
         hashedStore =  new ZXTMDirectoryFileStore( this, uri );
         
      // One entry 
      } else if( path.length == 1 ) {
         ZDebug.print( 6, " Path length 1 - '", path[0], "'" );
         
         if( path[0].trim().equals( "" ) ) {
            ZDebug.print( 6, " Creating ZXTM dir store"   );
            hashedStore = new ZXTMDirectoryFileStore( this, uri );
            
         // The rule 'virtual' directory
         } else if( path[0].equals( ZXTMFileSystem.RULE_PATH ) ) {
            ZDebug.print( 6, " Creating Rule dir store"    );
         
            hashedStore =  new RuleDirectoryFileStore( ZXTMFileSystem.RULE_PATH, zxtm,
               (ZXTMDirectoryFileStore) getStore( URI.create( getURIForModelElement( zxtm ) + "/" ) ),
               uri, this 
            );
         }
         
      } else if( path.length == 2 ) {   
         ZDebug.print( 5, " Path length 2 - '", path[0], "' / '", path[1], "'" );
         
         // A rule file
         if( path[0].equals( ZXTMFileSystem.RULE_PATH ) && path[1].endsWith( ZXTMFileSystem.TS_FILE_EXTENSION ) ) {
            String ruleName = toZXTMName( path[1].substring( 0, path[1].length() - 4 ) );
            
            ZDebug.print( 5, " Creating rule - '", ruleName, "'"  );
            
            Rule rule = zxtm.getRule( ruleName );
            
            // The rule exists
            if( rule != null ) {                  
               hashedStore = new RuleFileStore( 
                  this, rule, zxtm,
                  (RuleDirectoryFileStore) getStore( URI.create( getURIForModelElement( zxtm ) + "/" + ZXTMFileSystem.RULE_PATH ) ),
                  uri
               );
               
            // The rule does not exist
            } else {
               hashedStore = new RuleFileStore( 
                  this, ruleName, zxtm,
                  (RuleDirectoryFileStore) getStore( URI.create( getURIForModelElement( zxtm ) + "/" + ZXTMFileSystem.RULE_PATH ) ),
                  uri
               );
            }
            
         }
      }
      
      // We haven't found a valid file store, must not exist (and can never
      // exist)
      if( hashedStore == null ) {
         String parent = getURIForModelElement(zxtm).toString();
         for( int i = 0; i < path.length - 1; i++ ) {
            parent += "/" + path[i];
         }
         
         return new EmptyFileStore(this, path[path.length - 1], getStore( URI.create( parent ) ) );
      }
      
      //
      filesTable.put( uri.toString(), hashedStore );
      ZDebug.print( 2, "Cached '", uri.toString(), "' -> ", hashedStore );
      return hashedStore;
      
   }
   
   /** As a general rule you can write to the file system, though file stores
    *  themselves have the last say. */
   /* Override */
   public boolean canWrite()
   {     
      ZDebug.print( 4, "canWrite()"  );
      return true;
   }

   /** As a general rule you can delete bits of the file system, though file 
    *  stores themselves have the last say. */
   /* Override */
   public boolean canDelete()
   {      
      ZDebug.print( 4, "canDelete()"  );
      return true;
   }
   
   /**
    * Returns the file-system manager for ZXTM.
    * @return The file-system manager for ZXTM.
    */
   public static ZXTMFileSystem getFileSystem()
   {
      try {
         return (ZXTMFileSystem) EFS.getFileSystem( ZXTMFileSystem.PROTOCOL );
      } catch ( CoreException e ) {
         ZDebug.printStackTrace( e, "Getting ZXTM FileSystem failed" );
      }
      return null;
   }
   
   /**
    * Convert a name for something on a ZXTM to one that is safe to use on the
    * local file-system. Escapes incompatible chars with !x (where x is the 
    * char's unique identifier).
    * @param zxtmName The name used on ZXTM
    * @return The name that should be used on the local file-system.
    */
   public static String toOSName( String zxtmName )
   {
      return zxtmName.replaceAll( ":", "!c" );
   }
   
   /**
    * Convert an operating system filename to something to be used on ZXTM. 
    * Un-escapes !x sequences.
    * @param osName The name of the file to be stored on a ZXTM.
    * @return The ZXTM safe string.
    */
   public static String toZXTMName( String osName )
   {
      return osName.replaceAll( "!c", ":" );
   }

   /**
    * Turns a ModelElement into a URI.
    * @param element The element to create a URI for.
    * @return A URI for the model element.
    */
   public static URI getURIForModelElement( ModelElement element ) {
            
      try {
         switch( element.getModelType() ) {
            case ZXTM: {
               ZXTM zxtm = (ZXTM) element;
               return new URI( ZXTMFileSystem.PROTOCOL + "://" + zxtm.getHostname() + ":" + zxtm.getAdminPort() + "/" );
            }
               
            case RULE: {
               Rule rule = (Rule) element;
               return new URI(
                  getURIForModelElement( rule.getModelParent() ) + "/" + 
                  ZXTMFileSystem.RULE_PATH + "/" + enc( toOSName( rule.getName() ) ) + ZXTMFileSystem.TS_FILE_EXTENSION
               );
            }
            
            default: {
               throw new RuntimeException(
                  "Cannot create URI for " + element.getModelType() 
               );
            }
         }
      } catch( URISyntaxException e ) {
         ZDebug.printStackTrace( e, "Creating URI failed for element: ", element );
      } 
      
      return null;
   }
   
   /**
    * Gets the URI of the Rules directory for the specified ZXTM.
    * @param zxtm The ZXTM who's rule directory you want
    * @return The URI for the Rules directory.
    */
   public static URI getRuleDirForZXTM( ZXTM zxtm )
   {
      try {
         return new URI(
            getURIForModelElement( zxtm ) + "/" + ZXTMFileSystem.RULE_PATH + "/" 
         );
      } catch( URISyntaxException e ) {
         ZDebug.printStackTrace( e, "Creating Rules dir URI failed" );
      }
      return null;
   }
   
   /**
    * URL encodes a string 
    * @param name The string to encode
    * @return The encoded string.
    */
   public static String enc( String name ) 
   {
      try {
         return URLEncoder.encode( name, "UTF-8" );
      } catch( UnsupportedEncodingException e ) {
         ZDebug.printStackTrace( e, "Failed to encode: ", name );
      }
      
      return null;
   }
   
   /**
    * URL decodes a string 
    * @param url The string to decode
    * @return The decoded string.
    */
   public static String dec( String url ) 
   {
      try {
         return URLDecoder.decode( url, "UTF-8" );
      } catch( UnsupportedEncodingException e ) {
         ZDebug.printStackTrace( e, "Failed to decode: ", url );
      } catch( IllegalArgumentException e ) {
         ZDebug.printStackTrace( e, "Failed to decode: ", url );
      }
      return null;
   }
}
