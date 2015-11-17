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

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

import com.zeus.eclipsePlugin.consts.Partition;

/**
 * Splits out string declarations and comments from the code, and puts them in a
 * separate 'partition'.
 * 
 * Partitions are used to treat separate parts of the document differently, so
 * we can apply different syntax highlighting rules and processing to them.
 */
public class TrafficScriptPartitioner extends RuleBasedPartitionScanner
{
   /** The ID for this partitioner. */
   public static final String TS_PARTITIONER = "com.zeus.eclipsePlugin.TrafficScriptEditor.Partitioner";
   
   private static final IPredicateRule[] partitionRules = {
      new EndOfLineRule( "#", new Token( Partition.COMMENT.getId() ) ),        
      new SingleLineRule( "\"", "\"", new Token( Partition.STRING.getId() ), '\\', true ),
      new MultiLineRule( "'", "'", new Token( Partition.QUOTE_STRING.getId() ), '\\', true )
   };


   public TrafficScriptPartitioner()
   {
      super();
      setPredicateRules( partitionRules );
   }
}
