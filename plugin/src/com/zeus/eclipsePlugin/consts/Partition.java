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

import java.util.EnumSet;

import org.eclipse.jface.text.IDocument;

/**
 * The different partition types for the TrafficScript editor.
 */
public enum Partition
{
   CODE           ( IDocument.DEFAULT_CONTENT_TYPE ),
   COMMENT        ( "com.zeus.partition.Comment" ),
   STRING         ( "com.zeus.partition.String" ),
   QUOTE_STRING   ( "com.zeus.partition.QuoteString" )
   ;
   
   private String id;
   
   /**
    * Creates a new partition constant, represented by a unique identifier.
    * @param id The ID of this partition type. MUST be unique.
    */
   private Partition( String id )
   {
      this.id = id;
   }

   /**
    * Returns the unique identifier for this partition.
    * @return
    */
   public String getId()
   {
      return id;
   }
   
   private static String[] allPartitionsIds = null;

   /**
    * Get an array of all the partition IDs used by the TrafficScript editor.
    * @return A String array containing all the partition IDs.
    */
   public synchronized static String[] getAllPartitionIds()
   {
      if( allPartitionsIds == null ) {
         EnumSet<Partition> partitions = EnumSet.allOf( Partition.class );
         
         allPartitionsIds = new String[partitions.size()];
         
         int i = 0;
         for( Partition partiton : partitions ) {
            allPartitionsIds[i++] = partiton.getId();
         }
      }
      
      return allPartitionsIds;
   }
   
}
