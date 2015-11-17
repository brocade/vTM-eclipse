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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.ModelListener;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.model.ModelElement.Event;
import com.zeus.eclipsePlugin.model.ModelElement.State;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.project.operations.RefreshResourceOp;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * Listens to the model and refreshes the rules directory when something 
 * changes.
 */
public class ZXTMFileSystemRefresher implements ModelListener
{
   /**
    * Adds this as a listener to the new child and runs the main update method.
    */
   /* Override */
   public void childAdded( ModelElement parent, ModelElement child )
   {
      child.addListener( this );
      modelUpdated( child, ModelElement.Event.CHANGED );      
   }

   /**
    * Refreshes the correct rules directory for this element's ZXTM.
    */
   /* Override */
   public void modelUpdated( ModelElement element, Event event )
   {
      ZDebug.print( 2, "modelUpdated( ", element, ", ", event, " )" );
      
      // Find the ZXTM for the current element
      ModelElement current = element;     
      while( current != null && current.getModelType() != ModelElement.Type.ZXTM ){
         current = current.getModelParent();
      }
      
      if( current == null ) return;
      
      ZXTM zxtm = (ZXTM) current;            
      ZDebug.print( 2, "Parent ZXTM: ", zxtm );
      
      // Get the project for this ZXTM
      IProject project = ZXTMProject.getProjectForZXTM( zxtm );
      
      if( project == null || !project.isOpen() || !project.isAccessible() ) {
         ZDebug.print( 4, "Could not project to refresh for ZXTM: ", zxtm );
         return;
      }
      
      // Refresh the rules directory
      try {   
         ZDebug.print( 4, "Refreshing project: ", project.getName() );
         IFolder rulesFolder = project.getFolder( ZXTMFileSystem.RULE_PATH );
         if( ZXTMPlugin.isEclipseLoaded() && rulesFolder.exists() ) {
            SWTUtil.progressBackground( new RefreshResourceOp( 
               rulesFolder, IResource.DEPTH_ONE
            ), false );            
         }
      } catch( InvocationTargetException e ) {
         ZDebug.printStackTrace( e, "Failed to referesh project: ", project );
      }   
      
      
   }

   /** This just runs the main update method. */
   /* Override */
   public void stateChanged( ModelElement element, State state )
   {
      modelUpdated( element, ModelElement.Event.CHANGED );      
   }
   
   
}
