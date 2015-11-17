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

package com.zeus.eclipsePlugin.editor;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.consts.Ids;

/**
 * This is a set of utility functions that help with finding and creating 
 * markers. 
 */
public class MarkerUtil
{
   /** The owner of a marker, will always be this plugin's id */
   public static final String MARKER_OWNER = "com.zeus.Owner";
   
   /** The type of the marker. Will be a MarkerType enum */ 
   public static final String MARKER_TYPE = "com.zeus.Type";
   
   /**
    * Enum that represents the different marker that can be added to a resource.
    * Markers created with MarkerUtil will have the attribute MARKER_TYPE, which
    * will have one of these enums as it's value.
    */
   public enum MarkerType {
      ERROR          ( IMarker.PROBLEM, IMarker.SEVERITY_ERROR, false ),
      WARNING        ( IMarker.PROBLEM, IMarker.SEVERITY_WARNING, false ),
      INFO           ( IMarker.PROBLEM, IMarker.SEVERITY_INFO, false ),
      
      TASK_LOW       ( IMarker.TASK, IMarker.PRIORITY_LOW, true ),
      TASK_NORMAL    ( IMarker.TASK, IMarker.PRIORITY_NORMAL, true ),
      TASK_HIGH      ( IMarker.TASK, IMarker.PRIORITY_HIGH, true ),
      ;
      
      private String markerType;
      private int severity;
      private boolean isTask;
            
      private MarkerType( String markerType, int severity, boolean task )
      {
         this.markerType = markerType;
         this.severity = severity;
         this.isTask = task;
      }

      public String getMarkerType()
      {
         return markerType;
      }

      public Integer getSeverity()
      {
         if( isTask ) throw new InvalidParameterException( "Tasks do not have severity" );
         return severity;
      }
      
      public Integer getPriority()
      {
         if( !isTask ) throw new InvalidParameterException( "Non-tasks do not have priority" );
         return severity;
      }
      
      public boolean isTask() {
         return isTask;
      }
   }
   
   /**
    * Function converts a TaskTag object into a MarkerType.
    * @param tag The tag to convert
    * @return The MarkerType that matches this task tag.
    */
   public static MarkerType getTaskTagMarkerType( TaskTag tag )
   {
      switch( tag.getPriority() ) {
         case LOW: return MarkerType.TASK_LOW;
         case NORMAL: return MarkerType.TASK_NORMAL;
         case HIGH: return MarkerType.TASK_HIGH;
         
         default: return MarkerType.TASK_NORMAL;
      }
   }
   
   /**
    * Create a marker and add it to a file.
    * @param type The type of the marker.
    * @param file The file to add it to.
    * @param line The line the marker is on.
    * @param start The start of the marker (position form start of file)
    * @param end The end of the marker (position form start of file)
    * @param message The message associated with this marker
    * @return A new marker, or null if something went wrong.
    */
   public static IMarker createMarker( MarkerType type, IFile file, int line, int start, int end, String message ) 
   {
      try {        
         IMarker marker = file.createMarker( type.getMarkerType() );
         
         marker.setAttribute( MARKER_OWNER, Ids.PLUGIN );
         marker.setAttribute( MARKER_TYPE, type.toString() );
         marker.setAttribute( IMarker.LINE_NUMBER, line );
         
         if( type.isTask() ) {
            marker.setAttribute( IMarker.PRIORITY, type.getPriority() );
         } else {
            marker.setAttribute( IMarker.SEVERITY, type.getSeverity() );
         }
         
         marker.setAttribute( IMarker.MESSAGE, message );
         marker.setAttribute( IMarker.CHAR_START, start );
         marker.setAttribute( IMarker.CHAR_END, end );
         marker.setAttribute( IMarker.USER_EDITABLE, false );
         
         return marker;
      } catch( Exception e ) {
         ZDebug.printStackTrace( e, "Error whilst creating marker" );
      }
      
      return null;
   }
   
   /**
    * Remove markers of the specified types.
    * @param file The file to remove markers from
    * @param types The types you want to remove.
    */
   public static void removeMarkersOfType( IFile file, MarkerType ... types )  
   {
      HashMap<String,Boolean> typeMap = new HashMap<String,Boolean>();
      for( MarkerType type : types ) {
         typeMap.put( type.toString(), true );
      }
      
      try {
         IMarker[] markers = file.findMarkers( null, true, IResource.DEPTH_ZERO );
         
         for( IMarker marker : markers ) {
            try {
               if( !marker.exists() ) continue;
               
               String owner = (String) marker.getAttribute( MARKER_OWNER );
               if( owner == null || !owner.equals( Ids.PLUGIN ) ) continue;
               
               String type = (String) marker.getAttribute( MARKER_TYPE );
               if( type == null || !typeMap.containsKey( type ) ) continue;
               
               marker.delete();
            } catch( CoreException e ) {
               ZDebug.printStackTrace( e, "Exception for marker ", marker );
            }
         }
         
      } catch( CoreException e ) {
         ZDebug.printStackTrace( e, "Exception when finding markers");
      }
   }
   
   /**
    * Check that a marker is a particular type (or set of types) 
    * @param marker The marker to check
    * @param types The types you want to check the marker against.
    * @return True if the marker's type matches one of the types passed.
    */
   public static boolean markerIsOfType( IMarker marker, MarkerType ... types )
   {
      if( !marker.exists() ) return false;
      try {
         String markerType = (String) marker.getAttribute( MARKER_TYPE );
         
         for( MarkerType type : types ) {
            if( markerType.equals( type.toString() ) ) return true;
         }
      } catch( CoreException e ) {
         ZDebug.printStackTrace( e, "Exception when finding markers of type" );
      }
      
      return false;
   }
   
   /**
    * Get the marker at the specified position
    * @param file The file to search for markers.
    * @param position The position from the start of the file
    * @return The marker at this position, or null if none exists.
    */
   public static IMarker findMarkerForPos( IFile file, int position )
   {
      try {
         IMarker[] markers = file.findMarkers( null, true, IResource.DEPTH_ZERO );
         
         for( IMarker marker : markers ) {
            try {
               if( !marker.exists() ) continue;
               
               String owner = (String) marker.getAttribute( MARKER_OWNER );
               if( owner == null || !owner.equals( Ids.PLUGIN ) ) continue;
                  
               int start = (Integer) marker.getAttribute( IMarker.CHAR_START );
               int end = (Integer) marker.getAttribute( IMarker.CHAR_END );
               
               if( position >= start && position <= end ) {
                  return marker;
               }
            } catch( CoreException e ) {         
               ZDebug.printStackTrace( e, "Exception for marker ", marker );
            }
         }
         
      } catch( CoreException e ) {         
         ZDebug.printStackTrace( e, "Exception when finding markers of type" );
      }
      
      return null;
   }

   /**
    * Get all markers on the specified line of a file.
    * @param file The file the line is in.
    * @param line The line number (starting at 1)
    * @return The Markers on this line as an array.
    */
   public static IMarker[] findMarkersForLine( IFile file, int line )
   {
      LinkedList<IMarker> markerList = new LinkedList<IMarker>();
      
      try {
         IMarker[] markers = file.findMarkers( null, true, IResource.DEPTH_ZERO );
         
         for( IMarker marker : markers ) {
            try {
               if( !marker.exists() ) continue;
               
               String owner = (String) marker.getAttribute( MARKER_OWNER );
               if( owner == null || !owner.equals( Ids.PLUGIN ) ) continue;      
               
               int markerLine = (Integer) marker.getAttribute( IMarker.LINE_NUMBER );
               if( markerLine == line ) {
                  markerList.add( marker );
               }
            } catch( CoreException e ) {         
               ZDebug.printStackTrace( e, "Exception for marker ", marker );
            }
         }
         
      } catch( CoreException e ) {         
         ZDebug.printStackTrace( e, "Exception when finding markers of type" );
      }
      
      return markerList.toArray( new IMarker[markerList.size()] );
   }
   
   
   private MarkerUtil() {} // Cannot instantiate
}
