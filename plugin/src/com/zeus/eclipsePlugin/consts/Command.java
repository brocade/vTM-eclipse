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
 * Constants for the command IDs used in this plug-in. Can be external or 
 * internal IDs. 
 * 
 * To find an appropriate external ID, look through the org.eclipse.ui plug-in's
 * plugin.xml file.
 */
public enum Command
{
   EDIT_COPY         ( "org.eclipse.ui.edit.copy", true ),
   EDIT_CUT          ( "org.eclipse.ui.edit.cut", true ),
   EDIT_PASTE        ( "org.eclipse.ui.edit.paste", true ),   
   EDIT_DELETE       ( "org.eclipse.ui.edit.delete", true ),
   EDIT_RENAME       ( "org.eclipse.ui.edit.rename", true ),
   
   EDITOR_PROPOSALS  ( "org.eclipse.ui.edit.text.contentAssist.proposals", true ), 
   
   NEW_WIZARD        ( "org.eclipse.ui.newWizard", true ),
   ;
   
   private String id;
   private boolean isExternal;
   
   /**
    * Create a command constant, giving the ID of the constant (should match the
    * ID in the plugin.xml file. 
    * @param id The id of the command
    * @param isExternal Set to true if the command is external (not part of this
    * plug-in)
    */
   private Command( String id, boolean isExternal )
   {
      this.id = id;
      this.isExternal = isExternal;
   }

   /**
    * The ID of this command (matches what's in the appropriate plugin.xml)
    * @return
    */
   public String getId()
   {
      return id;
   }

   /**
    * Is this command part of another plug-in?
    * @return True if the command is not part of this plug-in's XML file.
    */
   public boolean isExternal()
   {
      return isExternal;
   }   
}
