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

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;

import com.zeus.eclipsePlugin.ColourManager;
import com.zeus.eclipsePlugin.PreferenceManager;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Colour;
import com.zeus.eclipsePlugin.consts.ExternalPreference;
import com.zeus.eclipsePlugin.consts.Partition;
import com.zeus.eclipsePlugin.editor.assist.PopupManager;
import com.zeus.eclipsePlugin.editor.assist.TrafficScriptAssistantProcessor;
import com.zeus.eclipsePlugin.editor.assist.TrafficScriptInfoPresenter;
import com.zeus.eclipsePlugin.editor.presentation.CommentScanner;
import com.zeus.eclipsePlugin.editor.presentation.QuoteStringScanner;
import com.zeus.eclipsePlugin.editor.presentation.StringScanner;
import com.zeus.eclipsePlugin.editor.presentation.TrafficScriptCodeScanner;
import com.zeus.eclipsePlugin.editor.presentation.TrafficScriptTextHover;
import com.zeus.eclipsePlugin.editor.presentation.ZXTMScanner;

/**
 * Configuration class that defines the behaviour of the TrafficScript 
 * Editor.
 */
public class TrafficScriptConf extends SourceViewerConfiguration
{
   private TrafficScriptEditor editor;
      
   /** Scanner class that only returns one token */
   static class SingleTokenRepairer extends DefaultDamagerRepairer
   {
      public SingleTokenRepairer( TextAttribute attribute )
      {
         super( new SingleTokenScanner( attribute ) );
      }

      static class SingleTokenScanner extends BufferedRuleBasedScanner
      {
         public SingleTokenScanner( TextAttribute attribute )
         {
            setDefaultReturnToken( new Token( attribute ) );
         }
      }
   }

   /**
    * Setup this class for the specified editor
    * @param editor The editor this class is setting up.
    */
   public TrafficScriptConf( TrafficScriptEditor editor )
   {
      this.editor = editor;
   }

   /**
    * Specify the Partitioner for the TrafficScriptEditor
    */
   /* Override */
   public String getConfiguredDocumentPartitioning( ISourceViewer sourceViewer )
   {
      return TrafficScriptPartitioner.TS_PARTITIONER;
   }

   /**
    * The presentation reconciler is used to change the appearance of text in
    * the editor (such as colour and font). 
    * 
    * 'Damager' classes are used to identify which areas of text have been
    * changed by the user.
    * 'Repairer' classes re-apply colouring/font style to the 'damaged' 
    * areas.
    */
   /* Override */
   public IPresentationReconciler getPresentationReconciler(
      ISourceViewer sourceViewer )
   {
      PresentationReconciler reconciler = new PresentationReconciler();
      reconciler.setDocumentPartitioning( 
         getConfiguredDocumentPartitioning( sourceViewer )
      );      
      
      ZXTMScanner[] scanners = new ZXTMScanner[4];
      
      // Damager & Repairer for TrafficScript code
      scanners[0] = new TrafficScriptCodeScanner( editor );
      {         
         DefaultDamagerRepairer repairer = new DefaultDamagerRepairer( 
            scanners[0]
         );
         reconciler.setDamager( repairer, Partition.CODE.getId() );
         reconciler.setRepairer( repairer, Partition.CODE.getId() );
      }
      
      // Damager & Repairer for comments
      scanners[1] = new CommentScanner( editor );
      {
         DefaultDamagerRepairer repairer = new DefaultDamagerRepairer( 
            scanners[1]
         );
         
         reconciler.setDamager( repairer, Partition.COMMENT.getId() );
         reconciler.setRepairer( repairer, Partition.COMMENT.getId() );
      }

      // Damager & Repairer for string definitions
      scanners[2] = new StringScanner( editor );
      {
         DefaultDamagerRepairer repairer = new DefaultDamagerRepairer( 
            scanners[2]
         ); 
           
         reconciler.setDamager( repairer, Partition.STRING.getId() );
         reconciler.setRepairer( repairer, Partition.STRING.getId() );
      }

      // Damager & Repairer for quote string definitions
      scanners[3] = new QuoteStringScanner( editor );
      {
         DefaultDamagerRepairer repairer = new DefaultDamagerRepairer( 
            scanners[3]
         ); 

         reconciler.setDamager( repairer, Partition.QUOTE_STRING.getId() );
         reconciler.setRepairer( repairer, Partition.QUOTE_STRING.getId() );
      }

      editor.setCodeScanners( scanners );
      
      return reconciler;
   }

   /**
    * Setup the content assistant (provides suggestions to complete code) and 
    * the PopupManager (helps users edit parameters for function calls)
    */
   /* Override */
   public IContentAssistant getContentAssistant( ISourceViewer sourceViewer )
   {
      // Setup the content assistant
      ContentAssistant assistant = new ContentAssistant();
            
      assistant.setContentAssistProcessor(
         new TrafficScriptAssistantProcessor( editor ), 
         IDocument.DEFAULT_CONTENT_TYPE
      
      );
      
      ColourManager colourManager = ZXTMPlugin.getDefault().getColourManager();
      
      assistant.setProposalSelectorBackground( colourManager.getColour( Colour.TS_ASSIST_BG ) );
      assistant.enableAutoActivation( true );
      assistant.setAutoActivationDelay( 300 );
      assistant.setProposalPopupOrientation( IContentAssistant.PROPOSAL_STACKED );
      assistant.setContextInformationPopupOrientation( IContentAssistant.CONTEXT_INFO_ABOVE );

      // For info boxes when you select an option
      assistant.setInformationControlCreator( createInfoControlPresenter() );
      
      editor.setAssistant( assistant );
      
      // Setup the pop-up manager
      new PopupManager( editor );
      
      return assistant;
   }

   /**
    * Set-up the hover manager (for when the user hovers their mouse over 
    * the margin)
    */
   /* Override */
   public IAnnotationHover getAnnotationHover( ISourceViewer sourceViewer )
   {    
      return new TrafficScriptTextHover( editor );
   }

   /**
    * Set-up the hover manager (for when the user hovers their mouse over 
    * the code)
    */
   /* Override */
   public ITextHover getTextHover( ISourceViewer sourceViewer,
      String contentType )
   {
      ZDebug.print( 3, "getTextHover( ", sourceViewer, ", ", contentType, " )" );
      return new TrafficScriptTextHover( editor );
   }

   /**
    * Returns all the Partition types the editor deals with.
    */
   /* Override */
   public String[] getConfiguredContentTypes( ISourceViewer sourceViewer )
   {      
      return Partition.getAllPartitionIds();
   }

   /**
    * Returns the tab width for this editor (is the tab width of general text
    * editors
    */
   /* Override */
   public int getTabWidth( ISourceViewer sourceViewer )
   {
      return PreferenceManager.getExternalPreferenceInt( 
         ExternalPreference.EDITOR_TAB_WIDTH 
      );
   }

   /**
    * Creates the undo manager (uses the undo level of general text editors)
    */
   /* Override */
   public IUndoManager getUndoManager( ISourceViewer sourceViewer )
   {      
      TextViewerUndoManager undoManager = new TextViewerUndoManager( 
         PreferenceManager.getExternalPreferenceInt( 
            ExternalPreference.EDITOR_UNDO_LEVEL
         )
      );
      
      editor.setUndoManager( undoManager );
      return undoManager;
   }

   /**
    * Create an info control presenter (styles HTML text in our function 
    * descriptions)
    * @return A new InfoControlCreator class that uses the  
    * TrafficScriptInfoPresenter class.
    */
   public static InfoControlCreator createInfoControlPresenter()
   {
      return new InfoControlCreator();
   }
   
   private static class InfoControlCreator implements IInformationControlCreator
   {
      /* Override */
      public IInformationControl createInformationControl( Shell parent )
      {
         return new DefaultInformationControl( parent, new TrafficScriptInfoPresenter() );
      }      
   }
   
   
}
