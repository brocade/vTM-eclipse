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

import java.util.EnumSet;
import java.util.HashMap;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.editors.text.EditorsUI;

import com.zeus.eclipsePlugin.consts.ExternalPreference;
import com.zeus.eclipsePlugin.consts.Ids;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.consts.ExternalPreference.Source;
import com.zeus.eclipsePlugin.consts.Preference.Format;
import com.zeus.eclipsePlugin.editor.MarkerManager;
import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;
import com.zeus.eclipsePlugin.editor.assist.TrafficScriptAssistantProcessor;
import com.zeus.eclipsePlugin.editor.presentation.ZXTMScanner;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * Class that manages the preferences for this plug-in.
 */
public class PreferenceManager extends AbstractPreferenceInitializer
{
   /**
    * This function is used to link the preferences with the plug-in's internal 
    * settings.
    * @param pref The preference that has been changed
    * @param newValue The new value of this preference
    */
   protected static void update( Preference pref, Object newValue )
   {
      ZDebug.print( 5, "Pref changed: ", pref, ": ", newValue );
      
      if( pref.getFormat() == Format.COLOUR ) {
         if( !ZXTMPlugin.isEclipseLoaded() ) return;
         ZDebug.print( 5, "Rescanning all documents" );
         ZXTMScanner.invalidateAllScanners();
         for( TrafficScriptEditor editor : TrafficScriptEditor.getAllEditors() ) {
            editor.getViewer().invalidateTextPresentation();
            
         }
      }
      
      switch( pref ) {
         case DEBUG_ENABLED: {
            ZDebug.setDebug( (Boolean) newValue );
            ZXTMPlugin.getDefault().setDebugging();
            break;
         }
         
         case DEBUG_UI: {
            SWTUtil.debugBorders = (Boolean) newValue;
            break;
         }
            
         
         case DEBUG_FILTER: {
            String[] values = newValue.toString().split( "[,\\s]+" );
            
            ZDebug.resetFiles();
            ZXTMPlugin.getDefault().setDebugging();
            
            if( values.length == 1 && values[0].trim().length() == 0 ) break;
            
            for( String value : values ) {
               try {
                  ZDebug.print( 6, "Adding debug file: '", value, "'" );
                  String[] fileLevel = value.split( ":" );
                  String file = fileLevel[0];
                  int level = Integer.parseInt( fileLevel[1] );
                  
                  ZDebug.addFile( file, level );
                  
               } catch( Exception e ) {
                  ZDebug.printStackTrace( e, "Failed to set debug filter" );
               }                  
            }

            break;
         }
         
         case TASK_TAGS: {
            if( !ZXTMPlugin.isEclipseLoaded() ) break;
            
            ZXTMScanner.invalidateAllScanners();
            for( TrafficScriptEditor editor : TrafficScriptEditor.getAllEditors() ) {
               editor.getViewer().invalidateTextPresentation();               
            }
            
            ModelController model = ZXTMPlugin.getDefault().getModelController();
            for( ZXTM zxtm : model.getSortedZXTMs() ) {
               for( Rule rule : zxtm.getRules() ) {
                  TrafficScriptEditor editor = 
                     TrafficScriptEditor.getEditorForRule( rule );
                  
                  if( editor == null || !editor.isDirty() ) {
                     MarkerManager.updateTaskMarkers( rule );
                  }
               }
            }
            
            break;
         }
         
         case ASSIST_FUNC: case ASSIST_GROUP: case ASSIST_KEYWORDS: {
            TrafficScriptAssistantProcessor.updateTypes();
            break;
         }
      }      
   }
   
   /**
    * Like the update method, but for preference changes by other plug-ins we are
    * listening to.
    * @param pref The external preference that has been changed.
    * @param newValue The new value it has been changed to.
    */
   protected static void updateExternal( ExternalPreference pref, Object newValue ) 
   {
      ZDebug.print( 5, "External pref changed: ", pref, ": ", newValue );
      
      switch( pref ) {
         case EDITOR_TAB_WIDTH: {
            if( !ZXTMPlugin.isEclipseLoaded() ) return;
            for( TrafficScriptEditor editor : TrafficScriptEditor.getAllEditors() ) {
               ZDebug.print( 6, "Updating editor tabs for: ", editor.getRuleName() );
               StyledText text = editor.getViewer().getTextWidget();
               text.setTabs( (Integer) newValue );
            }
            break;
         }
         
         case EDITOR_UNDO_LEVEL: {
            if( !ZXTMPlugin.isEclipseLoaded() ) return;
            for( TrafficScriptEditor editor : TrafficScriptEditor.getAllEditors() ) {
               ZDebug.print( 6, "Updating undo level for: ", editor.getRuleName() );
               
               TextViewerUndoManager undo = editor.getUndoManager();
               if( undo == null ) break;
               undo.setMaximalUndoLevel( (Integer) newValue );               
            }
            break;
         }
            
      }
   }
   
   /**
    * Add listeners to all the preference stores we are interested in. Should be
    * called once on plug-in start.
    */
   public static void initialiseListeners()
   {
      ZDebug.print( 3, "initialiseListeners()" );
      IPreferenceStore preferences = ZXTMPlugin.getDefault().getPreferenceStore();

      // Perform updates on all the preferences this plugin uses to ensure all
      // the settings are pushed to the relevant classes.
      ZXTMPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( 
         new PreferenceListener()
      );
      
      for( Preference pref : EnumSet.allOf( Preference.class )  ) {
         switch( pref.getFormat() ) {
            case BOOLEAN: {
               update( pref, preferences.getBoolean( pref.getKey() ) );
               break;
            }
            
            case INT: case MILISECONDS: case POSITIVE_INT: {
               update( pref, preferences.getInt( pref.getKey() ) );
               break;
            }
            
            case STRING: case STRING_LIST: case COLOUR: {
               update( pref, preferences.getString( pref.getKey() ) );
               break;
            }
            
            default: {
               System.err.println( "Preference " + pref + " format " +  pref.getFormat() + " not updated." );
            }
         }
      }
            
      // Listen to the editors property changes
      IPreferenceStore editorPrefs = EditorsUI.getPreferenceStore();
      editorPrefs.addPropertyChangeListener( 
         new ExternalPreferenceListener( Source.CORE_EDITOR )
      );
      
   }

   /**
    * Sets the default values for all the preferences the plug-in uses.
    */
   /* Override */
   public void initializeDefaultPreferences()
   {
      ZDebug.print( 4, "initializeDefaultPreferences()" );
      IEclipsePreferences node = new DefaultScope().getNode( Ids.PLUGIN );
      
      for( Preference pref : EnumSet.allOf( Preference.class )  ) { 
         ZDebug.print( 6, "Setting default for: ", pref, " = ", pref.getDefault() );
         
         switch( pref.getFormat() ) {
            case INT: case POSITIVE_INT: case MILISECONDS: {
               node.putInt( pref.getKey(), (Integer) pref.getDefault() );
               break;
            }
   
            case STRING: default: {
               node.put( pref.getKey(), pref.getDefault().toString() );
            }
         }
      }
      
   }
   
   /**
    * Convenience method to get a preference value for this plug-in.
    * @param pref The preference you want the value of.
    * @return The value of the preference as an int.
    */
   public static int getPreferenceInt( Preference pref )
   {      
      if( ZXTMPlugin.getDefault() == null ) {
         return (Integer) pref.getDefault();
      }
      
      IPreferenceStore preferences = ZXTMPlugin.getDefault().getPreferenceStore();
      return preferences.getInt( pref.getKey() );
   }
   
   /**
    * Convenience method to get a preference value for this plug-in.
    * @param pref The preference you want the value of.
    * @return The value of the preference as a string.
    */
   public static String getPreference( Preference pref )
   {      
      if( ZXTMPlugin.getDefault() == null ) {
         return pref.getDefault().toString();
      }
      
      IPreferenceStore preferences = ZXTMPlugin.getDefault().getPreferenceStore();
      return preferences.getString( pref.getKey() );
   }
   
   /**
    * Set a string preference's value.
    * @param pref The preference to set.
    * @param value The new value of the preference.
    */
   public static void setPreference( Preference pref, String value )
   {
      if( ZXTMPlugin.getDefault() == null ) {
         return;
      }
      
      IPreferenceStore preferences = ZXTMPlugin.getDefault().getPreferenceStore();
      preferences.setValue( pref.getKey(), value );
   }
   
   /**
    * Convenience method to get a preference value for this plug-in.
    * @param pref The preference you want the value of.
    * @return The value of the preference as a boolean.
    */
   public static Boolean getPreferenceBool( Preference pref )
   {      
      if( ZXTMPlugin.getDefault() == null ) {
         return (Boolean) pref.getDefault();
      }
      
      IPreferenceStore preferences = ZXTMPlugin.getDefault().getPreferenceStore();
      return preferences.getBoolean( pref.getKey() );
   }
   
   /**
    * Get if the preference is currently set to use the default value.
    * @param pref The preference you are checking
    * @return True if the preference is it's default value, false otherwise.
    */
   public static boolean isPreferenceDefault( Preference pref )
   {
      if( ZXTMPlugin.getDefault() == null ) {
         return false;
      }
      IPreferenceStore preferences = ZXTMPlugin.getDefault().getPreferenceStore();
      
      return preferences.isDefault( pref.getKey() );
   }
   
   /**
    * Get the value of an external preference (as an int).
    * @param pref The external you want the value of. 
    * @return The value of the specified external preference.
    */
   public static int getExternalPreferenceInt( ExternalPreference pref )
   {
      IPreferenceStore preferences = null;
      switch( pref.getSource() ) {
         case CORE_EDITOR: {
            preferences = EditorsUI.getPreferenceStore();
            break;
         }
      }
      
      if( preferences == null ) return 0;
      
      return preferences.getInt( pref.getKey() );
   }
   
   /**
    * Class that listens for preference changes to this plug-ins properties..
    */
   private static class PreferenceListener implements IPropertyChangeListener
   {
      private HashMap<String,Preference> prefMap;
      
      /**
       * Setup the listener. Creates a map between the preferences key and the
       * it's Preference enum object.
       */
      public PreferenceListener()
      {
         prefMap = new HashMap<String,Preference>(); 
         
         for( Preference pref : EnumSet.allOf( Preference.class )  ) {
            prefMap.put( pref.getKey(), pref );
         }
      }

      /**
       * The listeners callback method. Runs the update method which is used to
       * push changes into the classes that need it.
       */
      /* Override */
      public void propertyChange( PropertyChangeEvent event )
      {
         ZDebug.print( 4, "preferenceChange( ", event, " )" );
         
         Preference pref = prefMap.get( event.getProperty() );
         
         if( pref != null ) {       
            update( pref, event.getNewValue() );
         }
      }
   }
   
   /**
    * Class that listens to preference changes for other plug-ins.
    */
   private static class ExternalPreferenceListener implements IPropertyChangeListener
   {
      private HashMap<String,ExternalPreference> prefMap;
      
      /**
       * Setup the listener. Creates a map between the external preferences key
       * and the it's ExternalPreference enum object.
       */
      public ExternalPreferenceListener( Source prefSource )
      {
         prefMap = new HashMap<String,ExternalPreference>(); 
         
         for( ExternalPreference pref : EnumSet.allOf( ExternalPreference.class )  ) {
            if( pref.getSource() == prefSource ) {
               prefMap.put( pref.getKey(), pref );
            }
         }
      }

      /**
       * The listeners callback method. Runs the updateExternal method which is 
       * used to push changes into the classes that need it.
       */
      /* Override */
      public void propertyChange( PropertyChangeEvent event )
      {
         ZDebug.print( 4, "External property changed: ", event.getProperty(), " = ", event.getNewValue() );
         ExternalPreference pref = prefMap.get( event.getProperty() );
                  
         if( pref != null ) {
            updateExternal( pref, event.getNewValue() );
         }
      }
   }

   

}
