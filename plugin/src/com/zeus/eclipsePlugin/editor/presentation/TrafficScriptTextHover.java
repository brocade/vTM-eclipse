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

package com.zeus.eclipsePlugin.editor.presentation;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;

import com.zeus.eclipsePlugin.PreferenceManager;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.codedata.Function;
import com.zeus.eclipsePlugin.consts.Partition;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.editor.CodeLine;
import com.zeus.eclipsePlugin.editor.CodeRegion;
import com.zeus.eclipsePlugin.editor.CodeUtil;
import com.zeus.eclipsePlugin.editor.MarkerUtil;
import com.zeus.eclipsePlugin.editor.TrafficScriptConf;
import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;
import com.zeus.eclipsePlugin.editor.MarkerUtil.MarkerType;

/**
 * This class manages the mouse hover pop-up for the TrafficScript editor.
 */
public class TrafficScriptTextHover implements ITextHover, IAnnotationHover, ITextHoverExtension
{
   private TrafficScriptEditor parent;
   
   /**
    * Create a hover pop-up interface class for the passed TrafficScript editor.
    * @param parent The editor this class manages the hover for.
    */
   public TrafficScriptTextHover( TrafficScriptEditor parent )
   {
      this.parent = parent;
   }
   
   /**
    * Calculates the region and pop-up text for the specified offset.
    * @param textViewer The textViewer which the mouse is hovering over.
    * @param offset The offset into the document that the mouse is hovering 
    * over.
    * @return The CodeRegion object for this offset, or null if there is no
    * hover info.
    */
   public CodeRegion calculateCodeRegion( ITextViewer textViewer, int offset )
   {
      ZDebug.print( 3, "calculateCodeRegion( ", offset, " )" );
      try {
         IDocument doc = textViewer.getDocument();
         IRegion region = doc.getLineInformationOfOffset( offset );
         
         // Check for markers at the offset (code errors, tasks etc)
         IMarker marker = MarkerUtil.findMarkerForPos( parent.getFile(), offset );
         ZDebug.print( 4, "Marker: ", marker );
         
         // Tasks
         if( marker != null &&
            !PreferenceManager.getPreferenceBool( Preference.HOVER_TASKS ) && 
            MarkerUtil.markerIsOfType( marker, MarkerType.TASK_LOW, 
               MarkerType.TASK_NORMAL, MarkerType.TASK_HIGH ) ) 
         {
            ZDebug.print( 4, "Marker is task, not returning" );
            marker = null;
            
         // Errors and warnings.
         } else if( marker != null &&
            !PreferenceManager.getPreferenceBool( Preference.HOVER_PROBLEMS ) && 
            MarkerUtil.markerIsOfType( marker, MarkerType.ERROR, 
               MarkerType.WARNING, MarkerType.INFO ) ) 
         {
            ZDebug.print( 4, "Marker is a problem, not returning" );
            marker = null;
         } 
         
         // We found a marker, so return info on that
         if( marker != null ) {
            ZDebug.print( 4, "Returning marker info" );
            int start = (Integer) marker.getAttribute( IMarker.CHAR_START );            
            int end = (Integer) marker.getAttribute( IMarker.CHAR_END );
            int len = end - start;            
            
            return new CodeRegion( 
               (String) marker.getAttribute( IMarker.MESSAGE ), start, len 
             );               
         }
         
         // If there's no markers see if we are over a function...
         if( !PreferenceManager.getPreferenceBool( Preference.HOVER_DOCS ) ) {
            return null;
         }
         
         ZDebug.print( 4, "Looking for code doc info" );         
         
         CodeLine lineData = CodeUtil.getLineAreas( doc, offset );
                
         if( lineData.getRegionType( offset ) == Partition.CODE ) {
            String line = doc.get( region.getOffset(), region.getLength() );
            String code = line.substring( lineData.codeStart - region.getOffset(), lineData.codeEnd - region.getOffset() );
            CodeRegion codeRegion = CodeUtil.getFunctionAtOffset( code, offset - lineData.codeStart );
            
            if( codeRegion != null ) {  
               ZDebug.print( 4, "Got Function: ", codeRegion.getText() );
               
               Function function =  parent.getCodeDataVersion().getFunctionMatching( 
                  codeRegion.getText()
               );
               
               if( function != null ) {                  
                  return new CodeRegion( 
                     function.getFullDescription(),
                     codeRegion.getOffset() + lineData.codeStart, 
                     codeRegion.getLength() 
                  );
               }               
            }
         }         
         
      } catch( CoreException e ) {
         ZDebug.printStackTrace( e, "Error calcluating code region for hover" );
      } catch( BadLocationException e ) {
         ZDebug.printStackTrace( e, "Error calcluating code region for hover" );
      }
      return null;
   }
   
   /**
    * This calculates the hover region. After a pop-up is displayed the user can
    * move their mouse anywhere in this region and the pop-up will not disappear.    
    */
   /* Override */
   public IRegion getHoverRegion( ITextViewer textViewer, int offset )
   {
      ZDebug.print( 2, "getHoverRegion( ", offset, " )" );
      return calculateCodeRegion( textViewer, offset );
   }

   /**
    * Retrieve the text for this particular region.
    */
   /* Override */
   public String getHoverInfo( ITextViewer textViewer, IRegion region )
   {
      ZDebug.print( 2, "getHoverInfo( ", region, " )" );
      CodeRegion codeRegion = 
         calculateCodeRegion( textViewer, region.getOffset() + region.getLength() / 2 );
      
      if( codeRegion != null ) {
         return codeRegion.getText();      
      } else {
         return null;
      }
   }

   /**
    * This get the hover text for a line (when you hover over the margins).
    */
   /* Override */
   public String getHoverInfo( ISourceViewer sourceViewer, int lineNumber )
   {
      try {         
         ZDebug.print( 4, "Line Hovering at: ", lineNumber  );
         
         // Find all the markers on this line
         IMarker[] markers = MarkerUtil.findMarkersForLine( parent.getFile(), lineNumber + 1 );
         
         // Generate the text
         StringBuffer buffer = new StringBuffer( 200 );
         if( markers.length > 1 ) {
            buffer.append( ZLang.ZL_MultipleMarkersOnLine );
         }
         
         for( IMarker marker : markers ) {
            if( markers.length > 1 ) buffer.append( " - " );
            buffer.append( marker.getAttribute( IMarker.MESSAGE ) );
            buffer.append( "\n" );            
         }
         
         if( buffer.length() == 0 ) return null;
         
         return buffer.toString().trim();
      } catch( CoreException e ) {         
         ZDebug.printStackTrace( e, "getHoverInfo" );
      }
      return null;
   }

   /**
    * Get the presenter for the pop-up. This formats the pop-ups HTML text.  
    */
   /* Override */
   public IInformationControlCreator getHoverControlCreator()
   {      
      return TrafficScriptConf.createInfoControlPresenter();
   }

}
