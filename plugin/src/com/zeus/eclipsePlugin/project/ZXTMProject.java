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

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.zeus.eclipsePlugin.Encrypter;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Ids;
import com.zeus.eclipsePlugin.filesystem.ZXTMFileSystem;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.ZXTM;

/**
 * Class that implements the ZXTM project nature (marker that indicates that 
 * this is a ZXTM project). Also has many static functions that link the model
 * and Eclipse project system together.
 */
public class ZXTMProject implements IProjectNature
{
   protected IProject project;
   
   private static HashMap<String,ProjectProperties> properties = 
      new HashMap<String, ProjectProperties>();
   
   /**
    * Configure function for the ZXTM nature. Currently does nothing.
    */
   /* Override */
   public void configure() throws CoreException
   {
      ZDebug.print( 4, "\nconfigure()"  );
   }

   /**
    * Deconfiguring function for the ZXTM nature. Currently does nothing.
    */
   /* Override */
   public void deconfigure() throws CoreException
   {
   }

   /**
    * Get the project for this nature class.
    */
   /* Override */
   public IProject getProject()
   {
      return project;
   }

   /**
    * Set the project for this nature class.
    */
   /* Override */
   public void setProject( IProject project )
   {
      ZDebug.print( 4, "\nsetProject( ", project.getName(), " )" );
      this.project = project;
   }
   
   /**
    * Get the project properties for a particular project.
    * @param project The project to get the properties for
    * @return The properties class 
    */
   public static ProjectProperties getProjectProperties( IProject project )
   {
      ProjectProperties props = properties.get( project.getName() );
      if( props == null ) {
         props = new ProjectProperties( project );
         properties.put( project.getName(), props );
      }
      
      return props;      
   }
   
   /**
    * Find the project for the specified ZXTM
    * @param name The hostname/IP address of the ZXTM
    * @param port The administration port of the ZXTM
    * @return The project for this ZXTM, or null if it doesn't exist.
    */
   public static IProject getProjectForZXTM( String name, int port ) 
   {      
      ZDebug.print( 4, "getProjectForZXTM( ", name, ", ", port, " )" );
      
      try {
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         IWorkspaceRoot root = workspace.getRoot();
         
         ZDebug.print( 5, "Number of projects: ", root.getProjects().length );
         
         for( IProject project : root.getProjects() ) {
            ZDebug.print( 5, "Checking project '", project.getName(), "'"  );
            if( !project.exists() ) continue;

            // Check if this is a ZXTM project
            if( project.isAccessible() && !project.getDescription().hasNature( Ids.ZXTM_PROJECT_NATURE ) ) { 
               ZDebug.print( 5, project.getName(), " is not a ZXTM project"  );
               continue;
            }
            
            ProjectProperties properties = getProjectProperties( project );
            
            String projName = properties.get( ProjectProperties.HOSTNAME_KEY );
            ZDebug.print( 6, "Project stored hostname: ", projName  );
             
            if( projName != null ) {
               String portString = properties.get( ProjectProperties.PORT_KEY );
               int projPort = Integer.parseInt( portString );
               ZDebug.print( 6, "Project stored port: ", projPort  );
               
               if( name.trim().equals( projName.trim() ) && projPort == port ) {
                  return project;
               }            
            }      
         }
      } catch( Exception e ) {
         ZDebug.printStackTrace( e, "Failed to get project for ZXTM ", name, ":", port );
      }
      
      ZDebug.print( 4, "Returning null!"  );
      
      return null;
   }
   
   /**
    * Get the ZXTM for a particular project. If the ZXTM does not currently 
    * exist it will be created.
    * @param project The project you want the ZXTM for
    */
   public static ZXTM getZXTMForProject( IProject project )
   {
      ZDebug.print( 4, "getZXTMForProject( ", project.getName(), " )" );
      
      ModelController model = ZXTMPlugin.getDefault().getModelController();
      
      // Get the properties file for this project. 
      ProjectProperties properties = getProjectProperties( project );
      if( !properties.hasProperties() ) {
         ZDebug.print( 5, "No ZXTM properties for: ", project );
         return null;
      }
      
      // Get ZXTM properties from the properties file
      String projName = properties.get( ProjectProperties.HOSTNAME_KEY );
      
      if( projName != null ) {
         String portString = properties.get( ProjectProperties.PORT_KEY );
         
         int projPort = -1;
         try {
            projPort = Integer.parseInt( portString );
         } catch( NumberFormatException e ) {}
            
         
         // Is this ZXTM loaded? If so return it.
         ZXTM zxtm = model.getZXTM( projName, projPort );
         if( zxtm != null ) {
            return zxtm;
         }         
         
         String user = properties.get( ProjectProperties.USERNAME_KEY );
         if( user == null ) user = "admin";
         
         String crypt = properties.get( ProjectProperties.CRYPT_KEY );
         String password = null;
         if( crypt != null ) {
            try {
               password = Encrypter.decrypt( crypt );
            } catch( RuntimeException e ) {
               ZDebug.printStackTrace( e, "Encryption failed for project ", project );
            }
         }
         
         // Create the ZXTM in the model if it was not found.    
         zxtm = model.forceAddZXTM( projName, projPort, user, password, !project.isAccessible() );
         if( password != null ) zxtm.setStorePassword( true );

         return zxtm;
      }
   
      
      return null;
   }

   /**
    * Find the project for the specified ZXTM
    * @param zxtm The ZXTM you want the project of.
    * @return The project for this ZXTM, or null if it doesn't exist.
    */
   public static IProject getProjectForZXTM( ZXTM zxtm )
   {
      return getProjectForZXTM( zxtm.getHostname(), zxtm.getAdminPort() );
   }
   
   /**
    * Get the eclipse file for a particular rule.
    * @param rule The rule whose file you want
    * @return The file for the rule, or null if it could not be found.
    */
   public static IFile getFileForRule( Rule rule ) 
   {
      IProject project = ZXTMProject.getProjectForZXTM( (ZXTM) rule.getModelParent() );
      if( project == null ) {
         return null;
      }
      
      return project.getFile( ZXTMFileSystem.RULE_PATH + "/" + ZXTMFileSystem.toOSName( rule.getName() ) + ZXTMFileSystem.TS_FILE_EXTENSION ); 
   }
      

}
