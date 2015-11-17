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

package com.zeus.eclipsePlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.zeus.eclipsePlugin.codedata.CodeDataLoader;
import com.zeus.eclipsePlugin.codedata.CodeDefinitionXML;
import com.zeus.eclipsePlugin.codedata.TrafficScriptCodeData;
import com.zeus.eclipsePlugin.codedata.VersionCodeData;
import com.zeus.eclipsePlugin.consts.Ids;
import com.zeus.eclipsePlugin.editor.MarkerManager;
import com.zeus.eclipsePlugin.editor.TrafficScriptPartitioner;
import com.zeus.eclipsePlugin.filesystem.ZXTMFileSystemRefresher;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.soap.SOAPModelController;
import com.zeus.eclipsePlugin.project.ProjectManager;
import com.zeus.eclipsePlugin.swt.PasswordUpdater;

/**
 * The activator class controls the plug-in life cycle
 */
public class ZXTMPlugin extends AbstractUIPlugin implements CodeDataLoader
{
   /** The path to where the code information is stored in the plug-in */
   public static final String CODE_DATA_DIR = "code-data";
   
   // The shared instance
   private static ZXTMPlugin plugin;
   
   // Management class instances
   private TrafficScriptPartitioner partitioner = null;
   private TrafficScriptCodeData codeData = null;
   private ColourManager colourManager = null;
   private ImageManager imageManager = null;
   private ModelController modelController = null;
   
   private ZXTMFileSystemRefresher fileSystemRefresher = new ZXTMFileSystemRefresher();
   private ProjectManager projectManager = new ProjectManager();
   
   // Code data loading stuff
   List<String> codeDataXMLFiles = null;
   
   public ZXTMPlugin()
   {
      setDebugging();
   }

   /**
    * This method sets the debugging options at the start. It overrides the
    * preference settings for debugging.
    */
   public void setDebugging()
   {
      //ZDebug.setDebug( true );
      //ZDebug.addFile( "MainPage", 10 );
      //ZDebug.addFile( "PreferenceManager.java", 10 );
      //ZDebug.addFile( "ZXTMPreferencePage.java", 10 );

   }

   /**
    * Method called when the plug-in is first used. Sets up classes needed by 
    * the plug-in.
    */
   /* Override */
   public void start( BundleContext context ) throws Exception
   {
      super.start( context );
      plugin = this;
      
      PreferenceManager.initialiseListeners();
      
      projectManager.update( false );
      projectManager.setPriority( 2 );
      projectManager.start();
      
   }

   /**
    * Method called when the plug-in is being unloaded (like when Eclipse quits)
    * Destroys all global objects used by the plug-in.
    */
   public void stop( BundleContext context ) throws Exception
   {
      projectManager.stopChecker();
      projectManager.update( true );
      
      if( modelController != null ) {
         modelController.dispose();
         modelController = null;
      }
      plugin = null;
      
      super.stop( context );
   }

   /**
    * Returns the shared instance of this plug-in.
    * @return the shared instance
    */
   public static ZXTMPlugin getDefault()
   {
      if( plugin == null ) {
         ZDebug.dumpStackTrace( "WARNING: ZXTMPlugin.getDefault() is about to return a null value."  );         
      }
      
      return plugin;
   }
 
   /**
    * Returns an image descriptor for the image file at the given plug-in
    * relative path.
    * @param path
    * @return the image descriptor
    */
   public static ImageDescriptor getImageDescriptor( String path )
   {
      return imageDescriptorFromPlugin( Ids.PLUGIN, path );
   }
   
   /**
    * Returns the TrafficScript image manager for this plug-in instance.
    * @return The TrafficScript image manager.
    */
   public ModelController getModelController() 
   {
      if( modelController == null ) {
         modelController = new SOAPModelController();
         modelController.addListener( fileSystemRefresher );
         modelController.addListener( new MarkerManager() );
         modelController.setPasswordCallback( new PasswordUpdater() );
      }
      return modelController;
   }
   
   /**
    * Returns the TrafficScript colour manager for this plug-in instance.
    * @return The TrafficScript colour manager.
    */
   public ColourManager getColourManager() 
   {
      if( colourManager == null ) {
         colourManager = new ColourManager();
      }
      return colourManager;
   }
   
   /**
    * Returns the TrafficScript image manager for this plug-in instance.
    * @return The TrafficScript image manager.
    */
   public ImageManager getImageManager() 
   {
      if( imageManager == null ) {
         imageManager = new ImageManager();
      }
      return imageManager;
   }

   /**
    * Returns the TrafficScript partitioner for this plug-in instance.
    * @return The TrafficScript partitioner.
    */
   public TrafficScriptPartitioner getTrafficScriptPartitioner()
   {
      if( partitioner == null ) {
         partitioner = new TrafficScriptPartitioner();
      }
      return partitioner;
   }
   
   
   /**
    * Returns the TrafficScript code data for this plug-in instance.
    * @return The TrafficScript code data.
    */
   public TrafficScriptCodeData getTrafficScriptCodeData() 
   {
      if( codeData == null ) {
         codeData = new TrafficScriptCodeData();
         codeData.setDataLoader( this );
      }
      return codeData;
   }
   
   /**
    * Turns a 2 version integers into a single long that can be used for 
    * comparisons.
    * @param major The major version
    * @param minor The minor version
    * @return
    */
   private static long versionToLong( int major, int minor )
   {
      return major * 1000 + minor * 10;
   }
   
   /**
    * Callback for the CodeDataLoader interface. Gets code data for the version
    * specified, or the closest version.
    */
   /* Override */
   public VersionCodeData getTrafficScriptVersion( int major, int minor )
   {
      ZDebug.print( 3, "getTrafficScriptVersion( ", major, ", ", minor, " )" );   
      
      long wantedVersion = versionToLong( major, minor );
      long aboveVersion = 10000000;
      long belowVersion = -10000000;
      
      String above = null, below = null;
      
      // Load code data files if we haven't already
      if( codeDataXMLFiles == null ) {
         codeDataXMLFiles = getPluginFilesInPath( ZXTMPlugin.CODE_DATA_DIR );
      }
      
      for( String current : codeDataXMLFiles ) {
         try {
            if( !current.matches( "^" + ZXTMPlugin.CODE_DATA_DIR  + "/\\d+\\.\\d+_.+\\.xml$" ) ) {
               ZDebug.dumpStackTrace( "Bad XML Filename: ", current );
               continue;
            }
            
            // Extract major and minor version info from code data
            String file = current.replaceFirst( ZXTMPlugin.CODE_DATA_DIR + "/", "" );
            String[] parts = file.split( "[\\._]" );
            
            long currentVersion = versionToLong( 
               Integer.parseInt( parts[0] ), Integer.parseInt( parts[1] )
            );
            
            long diff = currentVersion - wantedVersion;
            if( diff == 0 ) {
               above = current;
               below = current;
               break;
            }
            if( diff < 0 && diff > belowVersion) {
               belowVersion = diff;
               below = current;
            } else if( diff > 0 && diff < aboveVersion ) {
               aboveVersion = diff;
               above = current;
            }
         } catch( Exception e ) {
            ZDebug.printStackTrace( e, "Failed to compare code data file" );
         }
      }
      
      ZDebug.print( 5, "above: ", above, "   below: ", below );
      
      String xmlFile = null;      
      if( above != null ) { 
         xmlFile = above;
      } else if( below != null ) {
         xmlFile = below;
      } else {
         return null;
      }
      
      // Load the code data from the XML file
      InputStream xmlStream = getPluginFileStream( xmlFile );
      
      if( xmlStream != null ) return new CodeDefinitionXML( xmlStream );      
      return null;
   }

   /**
    * Gets an input stream for a file in the plug-in's jar file.
    * @param relativePath The path to the file
    * @return An input stream to the file, or null if it could not be opened.
    */
   public InputStream getPluginFileStream( String relativePath ) 
   {
      try {
         Bundle bundle = Platform.getBundle( Ids.PLUGIN );
         Path path = new Path( relativePath );
         
         return FileLocator.openStream( bundle, path, false );        
      } catch( IOException e ) {      
         ZDebug.printStackTrace( e, "Trying to open file in bundle failed - ", relativePath );
      }
      return null;
   }
   
   /**
    * Gets the files in the specified directory.
    * @param relativePath The path to the directory
    * @return The list of files in this path.
    */
   @SuppressWarnings("unchecked")
   public List<String> getPluginFilesInPath( String relativePath )
   {
      ZDebug.print( 3, "getPluginFilesInPath( ", relativePath, " )" );
      try {
         Bundle bundle = Platform.getBundle( Ids.PLUGIN );

         Enumeration paths = bundle.getEntryPaths( relativePath );
         
         LinkedList<String> filesList = new LinkedList<String>();
         while( paths.hasMoreElements() ) {
            String file = paths.nextElement().toString();
            ZDebug.print( 5, file );
            filesList.add( file );
         }
         
        return filesList;         
         
      } catch( Exception e ) {
         ZDebug.printStackTrace( e, "Failed to search bundle for files at: ", relativePath );
      }
      
      return new ArrayList<String>(0);
   }
      
   
   /**
    * Get the project manager, which is used to make workspace changes.
    * @return The project manager.
    */
   public ProjectManager getProjectManager()
   {
      return projectManager;
   }
   
   /**
    * Returns the file system refresher, which ensures eclipse is always 
    * showing what's in the model in the filesystem.
    * @return The file system referesher.
    */
   public ZXTMFileSystemRefresher getFileSystemRefresher()
   {
      return fileSystemRefresher;
   }
   
   private static boolean pluginLoaded, workbenchLoaded;
   
   /**
    * This function returns true if the plugin and workbench are fully loaded /
    * not shut down.
    * @return True if the workbench is ready.
    */
   public static boolean isEclipseLoaded()
   {
      try {
         if( plugin != null ) {
            if( !pluginLoaded ) {
               pluginLoaded = true;
               ZDebug.print( 4, "Plugin Loaded" );
            }         
         } else {
            return false;
         }
         
         if( plugin.getWorkbench() != null ) {
            if( !workbenchLoaded ) {
               workbenchLoaded = true;
               ZDebug.print( 4, "Workbench Loaded" );
            }         
         } else {
            return false;
         }    
         
         if( plugin.getWorkbench().getDisplay().isDisposed() ) {
            return false;
         }
         
         return true;
      } catch( Throwable e ) {
         return false;
      }
   }
   
   
}
