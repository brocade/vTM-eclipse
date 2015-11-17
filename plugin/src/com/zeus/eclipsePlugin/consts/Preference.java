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

package com.zeus.eclipsePlugin.consts;

import java.util.regex.Pattern;

import com.zeus.eclipsePlugin.ZLang;

/**
 * This class stores each of the global configurable preferences for this 
 * plug-in.
 * 
 * IMPORTANT: Do not alter the keys of existing preferences as it will bork
 * peoples upgrades.
 */
public enum Preference
{   
   SOAP_RATE         ( "soap.rate", Format.MILISECONDS, 1000, ZLang.ZL_PREF_soap_rate ),
   
   DEBUG_ENABLED     ( "debug.enabled", Format.BOOLEAN, false, ZLang.ZL_PREF_debug_enabled ),
   DEBUG_UI          ( "debug.ui", Format.BOOLEAN, false, ZLang.ZL_PREF_debug_ui ),
   DEBUG_FILTER      ( "debug.filter", Format.STRING_LIST, "", 
                       "^(\\s*\\w+(\\.java)?:\\d+\\s*)$", ZLang.ZL_PREF_debug_filter_validate,
                       ZLang.ZL_PREF_debug_filter ),
                       
   COLOUR_DEFAULT    ( "editor.colour.default", Format.COLOUR,  "0,0,0,       0,0,0,0", ZLang.ZL_PREF_editor_colour_default ),
   COLOUR_KEYWORD    ( "editor.colour.keyword", Format.COLOUR,  "125,0,0,     1,0,0,0", ZLang.ZL_PREF_editor_colour_keyword ),
   COLOUR_FUNCTION   ( "editor.colour.function", Format.COLOUR, "0,0,0,       0,1,0,0", ZLang.ZL_PREF_editor_colour_function ),
   COLOUR_DEPRECATED ( "editor.colour.deprecated", Format.COLOUR, "0,0,0,     0,1,1,0", ZLang.ZL_PREF_editor_colour_deprecated ),
   COLOUR_VARIABLE   ( "editor.colour.variable", Format.COLOUR, "0,0,100,     0,0,0,0", ZLang.ZL_PREF_editor_colour_variable ),
   COLOUR_COMMENT    ( "editor.colour.comment", Format.COLOUR,  "63,127,95,   0,0,0,0", ZLang.ZL_PREF_editor_colour_comment ),
   COLOUR_TASK       ( "editor.colour.task", Format.COLOUR,     "127,159,191, 1,0,0,0", ZLang.ZL_PREF_editor_colour_task ),
   COLOUR_NUMBER     ( "editor.colour.number ", Format.COLOUR,  "0,0,150,     0,0,0,0", ZLang.ZL_PREF_editor_colour_number ),
   COLOUR_STRING     ( "editor.colour.string", Format.COLOUR,   "0,0,220,     0,0,0,0", ZLang.ZL_PREF_editor_colour_string ),
   COLOUR_ESCAPE     ( "editor.colour.escape", Format.COLOUR,   "90,90,220,   0,0,0,0", ZLang.ZL_PREF_editor_colour_escape ),
   
   HOVER_PROBLEMS    ( "editor.hover.problems", Format.BOOLEAN, true, ZLang.ZL_PREF_editor_hover_problems ),
   HOVER_TASKS       ( "editor.hover.tasks", Format.BOOLEAN, false, ZLang.ZL_PREF_editor_hover_tasks ),
   HOVER_DOCS        ( "editor.hover.docs", Format.BOOLEAN, true, ZLang.ZL_PREF_editor_hover_docs ),
   
   CONTEXT_ENABLE    ( "editor.context.enable", Format.BOOLEAN, true, ZLang.ZL_PREF_editor_context_enable ),
   CONTEXT_WAIT      ( "editor.context.wait", Format.MILISECONDS, 300, ZLang.ZL_PREF_editor_context_wait ),
   
   ASSIST_GROUP      ( "editor.assist.group", Format.BOOLEAN, true, ZLang.ZL_PREF_editor_assist_group ),
   ASSIST_GROUP_FUNC ( "editor.assist.groupFuncs", Format.BOOLEAN, true, 
                       ZLang.ZL_PREF_editor_assist_groupFuncs ),
   ASSIST_FUNC       ( "editor.assist.func", Format.BOOLEAN, true, ZLang.ZL_PREF_editor_assist_func ),
   ASSIST_KEYWORDS   ( "editor.assist.keywords", Format.BOOLEAN, false, ZLang.ZL_PREF_editor_assist_keywords ),
   
   TASK_TAGS         ( "editor.task.tags", Format.STRING_LIST, 
                       ZLang.ZL_FIXME + ":HIGH," + ZLang.ZL_TODO + ":NORMAL", 
                       ZLang.ZL_PREF_editor_task_tags ),
   ;
   
   /**
    * The format of this preference. Effects how this preference is stored and
    * displayed in the UI.
    */
   public enum Format {
      STRING,
      MILISECONDS,
      POSITIVE_INT,
      INT,
      BOOLEAN,
      STRING_LIST,
      COLOUR         // Format: R,G,B,Bold,Italic,Strike-through,Underline
   }
   
   /** Standard prefix put in from of all our preferences */
   private static final String PREFIX = "com.zeus.eclipsePlugin.";
   
   private String key, text, errorText;
   private Object def;
   private Format format;   
   private Pattern pattern = null;
   
   /**
    * Creates a standard preference with no explicit validation.
    * @param key The unique identifier of this preference.
    * @param format The format of this preference.
    * @param def The default value for this plugin.
    * @param text A description of this preference.
    */
   private Preference( String key, Format format, Object def, String text )
   {
      this.def = def;
      this.format = format;
      this.text = text;
      
      this.key = PREFIX + key;
   }
   
   /**
    * Creates preference with a regex validation.
    * @param key The unique identifier of this preference.
    * @param format The format of this preference.
    * @param def The default value for this plug-in. Must be in the correct 
    * format.
    * @param regex A regular expression which is used to validate this preference
    * in the UI.
    * @param error The error to print if validation fails
    * @param text The description of this preference.
    */
   private Preference( String key, Format format, Object def, String regex, String error, String text )
   {
      this.def = def;
      this.format = format;
      this.text = text;
      
      this.key = PREFIX + key;
      this.pattern = Pattern.compile( regex );
      this.errorText = error;
   }

   /**
    * Gets the unique identifier of this preference.
    * @return The unique identifier of this preference.
    */
   public String getKey()
   {
      return key;
   }

   /**
    * Get the default value of this preference.
    * @return The default value of this preference.
    */
   public Object getDefault()
   {
      return def;
   }
   
   /**
    * Get the format of this preference.
    * @return The format of this preference.
    */
   public Format getFormat()
   {
      return format;
   }

   /**
    * Get the description of this preference.
    * @return The description.
    */
   public String getText()
   {
      return text;
   }

   /**
    * Get the regex pattern of this plug-in, or null if there is no regex 
    * validation.
    * @return The regex Pattern to validate this preference.
    */
   public Pattern getPattern()
   {
      return pattern;
   }

   /**
    * Returns the error text of this preference.
    * @return The error text if validation fails, or null if there's no error 
    * text for this preference.
    */
   public String getErrorText()
   {
      return errorText;
   }
   
   
}
