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
 * This is used to reference preferences part of other plug-ins (mainly the core
 * Eclipse plug-ins).
 */
public enum ExternalPreference
{
   EDITOR_TAB_WIDTH     ( "tabWidth", Source.CORE_EDITOR ),
   EDITOR_UNDO_LEVEL    ( "undoHistorySize", Source.CORE_EDITOR ),   
   
   FONT_EDITOR_TEXT          ( "org.eclipse.jface.textfont", Source.FONT_REGISTRY ),
   ;
   
   /**
    * The source plug-in of this preference. 
    */
   public enum Source {
      CORE_EDITOR,
      FONT_REGISTRY,
   }
   
   private String key;
   private Source source;
   
   /**
    * Creates an external preference.
    * @param key The Id of the preference. Should match what's in the external 
    * plug-in's plugin.xml file.
    * @param source The source plug-in of this preference.
    */
   private ExternalPreference( String key, Source source )
   {
      this.key = key;
      this.source = source;
   }

   /**
    * Get the key (ID) of this preference.
    * @return The ID of the preference, should match the external plugin.xml 
    * files ID.
    */
   public String getKey()
   {
      return key;
   }

   /**
    * Get the source plugin of this preference.
    * @return The enum representing the plug-in this preference comes from. 
    */
   public Source getSource()
   {
      return source;
   }
   
}
