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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;

/**
 * Class representing the different tags that auto create task markers in a 
 * TrafficScript document.
 */
public class TaskTag
{
   /**
    * The priority of tasks.
    */
   public enum Priority 
   {
      LOW      ( ZLang.ZL_PriorityLow ),
      NORMAL   ( ZLang.ZL_PriorityMedium ),
      HIGH     ( ZLang.ZL_PriorityHigh ),
      ;
      
      private String text;
      
      private Priority( String text )
      {
         this.text = text;
      }
      
      public String getText()
      {
         return this.text;
      }

      /* Override */
      public String toString()
      {
         return getText();
      }
   }
   
   private String tag;
   private Priority priority;
   
   /**
    * Create a task tag with the specified tag and priority.
    * @param tag The tag
    * @param priority
    */
   public TaskTag( String tag, Priority priority )
   {
      this.tag = tag;
      this.priority = priority;
   }
   
   public String getTag()
   {
      return tag;
   }

   public Priority getPriority()
   {
      return priority;
   }
   
   /* Override */
   public String toString()
   {
      return tag + ":" + priority.name(); 
   }

   /**
    * Creates a list of task tags from a string (used to store tags as a 
    * preference value).
    * @param tagString The string of tags to process. Must be in the form:
    * TAG:PRIORITY,...
    * @return A list of TaskTag objects.
    */
   public static List<TaskTag> getTaskTags( String tagString )
   {
      LinkedList<TaskTag> list = new LinkedList<TaskTag>();
      
      String[] tasks = tagString.split( "[\\s,]+" ); 
      
      for( String task : tasks ) {
         String[] taskParts = task.split( ":" ); 
         if( taskParts.length != 2 ) continue;
         
         Priority priority = Priority.valueOf( taskParts[1] );
         if( priority == null ) priority = Priority.NORMAL;
         
         list.add( new TaskTag( taskParts[0], priority ) );
      }
      
      return list;
   }
   
   /**
    * Like getTaskTags, but only the tag part (not the priority)
    * @param tagString The string of tags to process. Must be in the form:
    * TAG:PRIORITY,...
    * @return The tags as a List of Strings.
    */
   public static List<String> getTaskTagStrings( String tagString )
   {
      LinkedList<String> list = new LinkedList<String>();
      
      String[] tasks = tagString.split( "[\\s,]+" ); 
      
      for( String task : tasks ) {
         String[] taskParts = task.split( ":" ); 
         if( taskParts.length != 2 ) continue;
                 
         list.add( taskParts[0] );
      }
      
      return list;
   }
   
   /**
    * Converts a collection of TaskTags and coverts them into a single string,
    * suitable for storage as a preference value;
    * @param tags The tags to put into a single string.
    * @return A string representation of the passed TaskTag objects.
    */
   public static String createTagString( Collection<TaskTag> tags )
   {          
      StringBuffer buffer = new StringBuffer( 300 );
      
      for( TaskTag tag : tags ) {
         if( buffer.length() > 0 ) buffer.append( "," ); 
         buffer.append( tag.getTag() );
         buffer.append( ":" ); 
         buffer.append( tag.getPriority().name() );
      }
      ZDebug.print( 5, "TaskString: ", buffer.toString() ); 
      return buffer.toString();
   }
}


