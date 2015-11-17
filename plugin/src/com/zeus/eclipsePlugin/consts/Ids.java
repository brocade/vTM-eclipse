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

/**
 * This constants class defines the various ids used by this plugin. The ids 
 * in this class should match those in plugin.xml, otherwise things will break
 * in unpleasant ways.
 * 
 * IMPORTANT: This class must match plugin.xml
 */
public class Ids
{
   public static final String PREFIX = "com.zeus.eclipsePlugin.id.";
   
   // Plugin ID
   public static final String PLUGIN = "com.zeus.eclipsePlugin";

   // Views and Editors
   public static final String TRAFFIC_SCRIPT_EDITOR = PREFIX + "TrafficScriptEditor";
   public static final String ZXTM_VIEW = PREFIX + "ZXTMView";
   
   // Categories for Eclipse UI
   public static final String VIEW_CATEGORY = PREFIX + "ViewCategory";
   public static final String WIZARD_CATEGORY = PREFIX + "WizardCategory";
   
   // Wizards
   public static final String NEW_ZXTM_WIZARD = PREFIX + "wizards.NewZXTMWizard";
   public static final String NEW_RULE_WIZARD = PREFIX + "wizards.NewRuleWizard";
   
   // File System
   public static final String FILESYSTEM = PREFIX + "FileSystem";
   
   // ZXTM Project
   public static final String ZXTM_PROJECT_NATURE = PREFIX + "ZXTMProjectNature";
   
   // Perspective
   public static final String PERSPECTIVE = PREFIX + "ZXTMPerspective";   
   
   // Preferences page 
   public static final String PREFS_MAIN_PAGE = PREFIX + "prefs.MainPage";
   public static final String PREFS_EDITOR = PREFIX + "prefs.Editor";
   public static final String PREFS_HOVER = PREFIX + "prefs.Assist";
   public static final String PREFS_TASK = PREFIX + "prefs.Task";
   
   public static final String RES_PREFS_ZXTM = PREFIX + "resourcePrefs.ZXTMPage";
   
   // Conditions definitions
   public static final String CON_IN_ZXTM_VIEW = PREFIX + "def.InZXTMViewer";
   public static final String CON_IN_TS_EDITOR = PREFIX + "def.InTrafficScriptEditor";
   
}