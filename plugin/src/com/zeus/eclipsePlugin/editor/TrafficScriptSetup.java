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

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Partition;

/**
 * Class sets up document (just the partitioner) for .zts (TrafficScript) files.
 */
public class TrafficScriptSetup implements IDocumentSetupParticipant
{
   /**
    * Setup the TrafficScript Partitioner for .zts files
    */
   /* Override */
   public void setup( IDocument document )
   {     
      if (document instanceof IDocumentExtension3) {
         IDocumentExtension3 extension3 = (IDocumentExtension3) document;

         IDocumentPartitioner partitioner = new FastPartitioner( 
            ZXTMPlugin.getDefault().getTrafficScriptPartitioner(),
            Partition.getAllPartitionIds()
         );

         extension3.setDocumentPartitioner(
            TrafficScriptPartitioner.TS_PARTITIONER, 
            partitioner 
         );
        
         partitioner.connect( document );

      }

   }
}
