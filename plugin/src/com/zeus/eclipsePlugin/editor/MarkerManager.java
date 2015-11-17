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

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;

import com.zeus.eclipsePlugin.PreferenceManager;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.editor.MarkerUtil.MarkerType;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.ModelListener;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.RuleProblem;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.model.ModelElement.Event;
import com.zeus.eclipsePlugin.model.ModelElement.State;
import com.zeus.eclipsePlugin.model.ModelElement.Type;
import com.zeus.eclipsePlugin.project.ZXTMProject;

/**
 * Listens to model changes and updates markers when rule contents change. Also 
 * should be used to update markers for files independently.
 * 
 * Markers are used to tell Eclipse the location of problems and tasks in a 
 * file.
 */
public class MarkerManager implements ModelListener
{
   private static String taskTagPref = null;
   private static Pattern pattern = null;
   private static HashMap<String, TaskTag> taskMap = new HashMap<String,TaskTag>();
   
   /**
    * This is called to update the task tags from the systems preferences.
    * This builds a regex that can match all the task markers. It should be 
    * called every time you use the pattern field.
    */
   private static void updateTaskTags()
   {
      ZDebug.print( 5, "updateTaskTags()" );
      String newTaskPref = PreferenceManager.getPreference( Preference.TASK_TAGS );
      if( taskTagPref != null && newTaskPref.equals( taskTagPref ) ) {
         return;
      }
      
      taskTagPref = newTaskPref;
      
      List<TaskTag> tags = TaskTag.getTaskTags( taskTagPref );
      
      StringBuffer regex = new StringBuffer( 100 );
      taskMap.clear();
      
      regex.append( '(' );
      for( TaskTag tag : tags ) {
         if( regex.length() > 1 ) regex.append( '|' );
         regex.append( tag.getTag() );
         taskMap.put( tag.getTag(), tag );
      }
      regex.append( ")(\\s+|$)" );
            
      pattern = Pattern.compile( regex.toString() );
      ZDebug.print( 6, "New Task Regex: ", pattern.pattern() );
   }
   
   /**
    * Update the markers for the specified rule.
    * @param rule The rule object to update markers for.
    */
   public static void update( Rule rule ) 
   {
      ZDebug.print( 4, "update( ", rule, " )" );
      RuleProblem[] errors = rule.getCodeErrors();
      
      updateErrorMarkers( rule, errors );      
      updateTaskMarkers( rule );
   }
   
   /**
    * Update the problem markers (errors and warnings) for a rule.
    * @param rule The rule to update.
    * @param errors The problems the rule currently has.
    */
   public static void updateErrorMarkers( Rule rule, RuleProblem[] errors ) {
      IFile file = ZXTMProject.getFileForRule( rule );
      if( file != null && file.exists() ) {
         updateErrorMarkers( file, errors );
      }
   }
   
   /**
    * Update the error markers for a particular file.
    * @param file The file to update.
    * @param errors The problems the file currently has.
    */
   public static void updateErrorMarkers( IFile file, RuleProblem[] errors )
   {
      if( file != null ) {
         MarkerUtil.removeMarkersOfType( file, 
            MarkerType.ERROR, MarkerType.WARNING 
         );
         
         for( RuleProblem error : errors ){
            MarkerUtil.createMarker(
               error.isError() ? MarkerType.ERROR : MarkerType.WARNING,
               file, 
               error.getLine(), 
               error.getStart(),
               error.getEnd(),
               error.getDescription()
            );
         }
      }
   }
   
   /**
    * Update the task markers for a rule. 
    * @param rule The rule to update.
    */
   public static void updateTaskMarkers( Rule rule ) 
   {
      IFile file = ZXTMProject.getFileForRule( rule );
      if( file != null && file.exists() ) {
         updateTaskMarkers( file, rule.getTrafficScriptCode() );
      }
   }
      
   /**
    * Update the task markers for a particular file.
    * @param file The file to update
    * @param contents The contents of the file
    */
   public static void updateTaskMarkers( IFile file, String contents ) 
   {
      ZDebug.print( 5, "updateTodoMarkers( ", file, ", contents )" );
      if( file == null ) return;
      
      MarkerUtil.removeMarkersOfType( file, 
         MarkerType.TASK_LOW, MarkerType.TASK_NORMAL, MarkerType.TASK_HIGH 
      );
      
      CodeLine[] lineStarts = CodeUtil.getAllLineAreas( contents );
      
      updateTaskTags();
      
      for( CodeLine line : lineStarts ) {
         ZDebug.print( 10, line.toString() );
         if( line.commentStart == line.commentEnd ) continue;
         String comment = contents.substring( line.commentStart, line.commentEnd );
         
         ZDebug.print( 7, "Comment on line ", line.num, ": ", comment );

         Matcher matcher = pattern.matcher( comment );
         String lastTag = null;
         int lastTextStart = -1;
         while( matcher.find() ) {
            String tag = matcher.group( 1 );
            String text = comment.substring( matcher.end( 2 ) );
            
            if( lastTag != null ) {
               String lastText = comment.substring( lastTextStart, matcher.start( 1 ) ).trim();
               ZDebug.print( 5, "Final Tag: ", lastTag, " Text: ", lastText );
               TaskTag tagData = taskMap.get( lastTag );
               
               MarkerUtil.createMarker(
                  MarkerUtil.getTaskTagMarkerType( tagData ),
                  file, 
                  line.num, 
                  line.commentStart + lastTextStart,
                  line.commentStart + lastTextStart + lastText.length(),
                  lastTag + " " + lastText
               );
            }
            
            lastTag = tag;
            lastTextStart = matcher.end( 2 );
            ZDebug.print( 6, "Temp Tag: ", tag, "  Text: ", text );
         }
         
         if( lastTag != null ) {
            String lastText = comment.substring( lastTextStart ).trim();
            ZDebug.print( 5, "Final Tag: ", lastTag, " Text: ", lastText );
            TaskTag tagData = taskMap.get( lastTag );
            
            MarkerUtil.createMarker(
               MarkerUtil.getTaskTagMarkerType( tagData ),
               file, 
               line.num, 
               line.commentStart + lastTextStart,
               line.commentStart + lastTextStart + lastText.length(),
               lastTag + " " + lastText
            );
         }
        
      }
         
   }

   /**
    * Called by the Model every time a new model object is updated. Use to 
    * listen to new rules.
    */
   /* Override */
   public void childAdded( ModelElement parent, ModelElement child )
   {
      ZDebug.print( 4, "childAdded( ", parent, ", ", child, " )" );
      
      if( child.getModelType() == Type.RULE ) {
         update( (Rule) child );
      }
      
      child.addListener( this );
   }

   /**
    * Used to watch for changes to all the rules in the model. If a rule has 
    * been changed it updates it's markers. 
    */
   /* Override */
   public void modelUpdated( ModelElement element, Event event )
   {
      ZDebug.print( 4, "modelUpdated( ", element, ", ", event, " )" );
      if( element.getModelType() == Type.RULE && event == Event.CHANGED ) {
         
         // If this is open in an editor, wait for it update the markers, 
         // otherwise the new problem markers may appear in the wrong place.
         TrafficScriptEditor editor = TrafficScriptEditor.getEditorForRule( (Rule) element );
         if( editor == null ) {
            update( (Rule) element );
         }
      
      // Update all rules if the ZXTM has changed
      } else if( element.getModelType() == Type.ZXTM ) {
         for( Rule rule : ((ZXTM) element).getRules() ) {
            TrafficScriptEditor editor = TrafficScriptEditor.getEditorForRule( rule );
            if( editor == null ) {
               update( rule );
            }
         }
      }
   }

   /* Override */
   public void stateChanged( ModelElement element, State state )
   {
      
   }

}
