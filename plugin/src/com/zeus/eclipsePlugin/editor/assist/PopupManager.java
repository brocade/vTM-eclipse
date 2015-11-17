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

package com.zeus.eclipsePlugin.editor.assist;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.zeus.eclipsePlugin.ColourManager;
import com.zeus.eclipsePlugin.PreferenceManager;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.codedata.Function;
import com.zeus.eclipsePlugin.codedata.VersionCodeData;
import com.zeus.eclipsePlugin.consts.Colour;
import com.zeus.eclipsePlugin.consts.ExternalPreference;
import com.zeus.eclipsePlugin.consts.Partition;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.editor.CodeLine;
import com.zeus.eclipsePlugin.editor.CodeUtil;
import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * This class updates and displays the parameter pop-up, which shows you the 
 * parameters for the function you are currently editing.
 */
public class PopupManager implements FocusListener, ControlListener
{
   private TrafficScriptEditor editor;
   private Shell popupShell = null;
   private StyledText popupText;
   private boolean hasFocus = true;
   private Update updateThread = null;
   private Point lastCursorPos = null;
   
   /**
    * Create a pop-up manager for a particular editor.
    * @param editor The editor this class manages pop-ups for.
    */
   public PopupManager( TrafficScriptEditor editor )
   {
      this.editor = editor;
      
      // Add listeners to events which indicate the pop-up needs to be redrawn.
      editor.getViewer().getTextWidget().addFocusListener( this );
      editor.getViewer().getTextWidget().addControlListener( this );
      
      // Spawn the update thread, which monitors the text editor for changes.
      // Tried doing it using just listeners but there aren't enough events and 
      // we run the risk of missing some obscure event.
      updateThread = new Update( editor.getViewer().getTextWidget() );
      updateThread.start();
   }

   /**
    * Display the pop-up, creating it if there isn't already one on screen.
    * @param x The x position of the pop-up, relative to the editor.
    * @param y The y position of the pop-up, relative to the editor.
    * @param data The context data object which will determine the pop-ups 
    * contents.
    */
   private synchronized void showPopup( int x, int y, ContextData data )
   {
      ZDebug.print( 3, "showPopup( ", x, ", ", y, ", ", data );
      StyledText textEditor = editor.getViewer().getTextWidget();
      
      // If the position is outside of the visible bounds of the editor, don't
      // show it.
      if( y < 0 || y > textEditor.getBounds().height || 
          x < 0 || x > textEditor.getBounds().width ) 
      {
         hidePopup();
         return;
      }
      
      // If there is no shell on screen create a new one.
      if( popupShell == null ) {
         popupShell = new Shell( 
            textEditor.getShell(),
            SWT.NO_TRIM | SWT.ON_TOP
         );
         
         popupShell.setLayout( SWTUtil.makeLayoutTight( SWTUtil.createGridLayout( 1 ) ) );
         popupText = new StyledText( popupShell, SWT.READ_ONLY | SWT.FLAT | SWT.BORDER);
         SWTUtil.fontPreference( popupText, ExternalPreference.FONT_EDITOR_TEXT );
      }
      
      ColourManager colours = ZXTMPlugin.getDefault().getColourManager();
      
      // Update text and style from the context data
      popupText.setText( data.text );
      popupText.setForeground( colours.getColour( Colour.TS_CONTEXT_NORM ) );
      popupText.setBackground( colours.getColour( Colour.TS_CONTEXT_BG ) );
      
      if( data.bold != null ) {
         popupText.setStyleRange( new StyleRange(
            data.bold.getOffset(), data.bold.getLength(), 
            colours.getColour( Colour.TS_CONTEXT_BOLD ),
            null, SWT.BOLD
         ) );
      }
      
      popupShell.pack();
      
      // Set the position of the pop-up to above the specified position.
      if( lastCursorPos == null || lastCursorPos.x != x || lastCursorPos.y != y ) {
         
         Control control = editor.getViewer().getTextWidget();
         Point absolute = control.toDisplay( x, y );
      
         popupShell.setLocation( absolute.x, absolute.y - ( popupShell.getSize().y + 1) );
      }
      
      popupShell.layout( true, true );
      popupShell.pack();

      popupShell.setVisible( true );
      
      lastCursorPos = new Point( x, y );
   }
   
   /**
    * Hides and destroys the pop-up.
    */
   private synchronized void hidePopup()
   {
      if( popupShell != null ) {
         popupShell.dispose();
         popupShell = null;
      }
      
      lastCursorPos = null;
   }
   
   /**
    * Calculate the context data (what function we are in, its open bracket 
    * position etc) for the current offset.
    * @param doc The document the offset is in.
    * @param offset The offset we want to work out context data for.
    * @return The context data for this position, or null if there is none (if
    * we are not in the parameter part of a function call).
    */
   public ContextData getContextData( IDocument doc, int offset )
   {
      ZDebug.print( 6, "computeContextData( ", offset, " )" );
      
      try {         
         // Read in the last 500 characters, hopefully we wont need any more 
         // than this (if so this will break)
         int lastN = Math.max( 0, offset - 2000 );
         String before = doc.get( lastN, (offset - (lastN)));
         
         CodeLine currentLine = CodeUtil.getLineAreas( doc, Math.max( 0, offset ) );
         ZDebug.print( 7, "CURRENT LINE: ", currentLine );
         
         int start = before.length() - 1;
         int commas = 0;
         int neededOpenBrackets = 1;
         int bracketPos = -1;
         StringBuffer functionBuffer = new StringBuffer( 20 );
         
         Partition partition = currentLine.getRegionType( offset );
         if( partition == Partition.COMMENT ) {
            ZDebug.print( 6, "In a comment, return null" );
            return null;
         }
         
         // Search through the characters before the offset in reverse.
         while( start >= 0 ) {
            char c = before.charAt( start );
            
            if( c == '\n' || c == '\r' ) {
               currentLine = CodeUtil.getLineAreas( doc, Math.max( 0, start - 1 ) );
               ZDebug.print( 7, "CURRENT LINE: ", currentLine );
               start--;
               continue;
            }
            
            partition = currentLine.getRegionType( start );
            
            if( partition == Partition.CODE ) {

               // We have hit the end of a statement, we can't be in a parameter 
               // block any more
               if( c == ';' ) {
                  ZDebug.print( 8, "Breaking: semicolon" );
                  break;
               
               // We've started to read a function name and we've hit a 
               // non-function character
               } else if( 
                  functionBuffer.length() > 0 &&
                  !(Character.isLetterOrDigit( c ) || c == '.' ) )
               {
                  ZDebug.print( 8, "Breaking: function chars and an invalid char" );
               
                  // This wasn't a function just some brackets 
                  // e.g. foo( 1 + (2-3 <CURSOR>) )
                  if( functionBuffer.length() == 0 || 
                     functionBuffer.toString().startsWith( "." ) ) // Starts with == ends with
                  {
                     ZDebug.print( 8, "False Match [", functionBuffer, "]" );
                     neededOpenBrackets++;
                     functionBuffer = new StringBuffer( 20 );
                  
                  // Its a function, lets break
                  } else {
                     break;
                  }
               
            
               // We just hit a close bracket, looks like we just entered another
               // function call or set of brackets.
               } else if( c == ')' ) {
                  neededOpenBrackets++; 
            
               // We just left a function call parameter block. We might be able to
               // look for the function name now!
               } else if( c == '(' ) {
                  neededOpenBrackets--;
                  if( neededOpenBrackets < 0 ) {
                     neededOpenBrackets = 0;
                  }
                  
                  if( neededOpenBrackets == 0 ) {
                     bracketPos = offset - (before.length() - start);
                  }
            
               // If this is a function character and we've seen the end of the 
               // parameter block this must be the function name
               } else if( 
                  neededOpenBrackets == 0 && 
                  (Character.isLetterOrDigit( c ) || c == '.' ) ) 
               {
                  functionBuffer.append( c );
                              
               // If we are expecting a function name and we hit non-whitespace 
               // and non-function, must be a brackets set.
               } else if( neededOpenBrackets == 0 && !Character.isWhitespace( c ) ) {
                  ZDebug.print( 8, "Not a function bracket set, increasing needed brackets." );
                  
                  neededOpenBrackets++;
                                 
               // Count the commas, this tells us which parameter we are in
               } else if( neededOpenBrackets == 1 && c == ',' ) {
                  commas++;
               }
            }
            
            ZDebug.print( 6, "Char: ", c, " - ", partition );
            start--;
         }
         start++;
         
         functionBuffer.reverse();
            
         ZDebug.print( 7, "Function: [", functionBuffer, "] Commas: ", commas );
                     
         // Is this a function with parameters?
         VersionCodeData version = editor.getCodeDataVersion();
         Function function = version.getFunctionMatching( functionBuffer.toString() );
         if( function == null || function.getParams().length == 0 ) return null;
         
         // Work out which parameter we are in
         int paramNum = commas + 1;
         
         String[] params = function.getParams();
         int currentParam = -1;
         if( function.getMaxParams() == Function.INFINITE || 
            paramNum <= function.getMaxParams() )
         {
            currentParam = paramNum;
         }
         
         // Generate the text to go in the pop-up
         int boldStart = 0, boldLength = 0;
         
         StringBuffer buffer = new StringBuffer();
         for( int i = 0; i < params.length; i++ ) {            
            if( i > 0 ) buffer.append( ", " );
            
            String param = params[i];
            if( (i + 1) > function.getMinParams() ) {
               param = "[" + param + "]";
            }
            
            if( (i + 1) == currentParam ) {
               boldStart = buffer.length();
               boldLength = param.length();
            } 
            
            buffer.append( param );                      
         }  
         
         if( function.getMaxParams() == Function.INFINITE ) 
         {
            if( buffer.length() > 0 ) buffer.append( " " );
            ZDebug.print( 4, "INF Params: ", currentParam, " vs ", params.length );
            if( currentParam > params.length ) {
               boldStart = buffer.length();
               boldLength = 3;
            } 
            buffer.append( "..." );   
         }
         
         Region boldRegion = null;
         if( boldLength != 0 ) {
            boldRegion = new Region( boldStart, boldLength );
         }
         
         // Return the context data
         return new ContextData( 
            buffer.toString(), 
            bracketPos, 
            boldRegion
         );
         
        
      } catch( BadLocationException e ) {
         ZDebug.printStackTrace( e, "While working out context data" );
      }
      return null;
   }

   /**
    * Get the context data for the current offset and update the pop-up 
    * accordingly.
    */
   private synchronized void updatePopup()
   {
      try {   
         StyledText textEditor = editor.getViewer().getTextWidget();
         
         if( textEditor.isDisposed() || !hasFocus ||             
            !PreferenceManager.getPreferenceBool( Preference.CONTEXT_ENABLE ) ) 
         {
            hidePopup();
            return;
         }

         int offset = textEditor.getSelection().x;
         IDocument doc = editor.getViewer().getDocument();

         ContextData data = getContextData( doc, offset );
         ZDebug.print( 3, "Popup text is: ", data );
         
         if( data != null ) {
            Rectangle startPos = textEditor.getTextBounds(
               data.bracketPosition, data.bracketPosition
            );

            showPopup( startPos.x, startPos.y, data );
         } else {
            hidePopup();
         }
    
      } catch( Exception e ) {
         ZDebug.printStackTrace( e, "Updating popup failed" );
      }      
   }
   
   /* Override */
   public synchronized void focusGained( FocusEvent e )
   {
      ZDebug.print( 2, "focusGained( ", e, " )" );
      hasFocus = true;
      updatePopup();
   }

   /* Override */
   public synchronized void focusLost( FocusEvent e )
   {
      ZDebug.print( 2, "focusLost( ", e, " )" );
      hasFocus = false;
      hidePopup();
   }
   
   /* Override */
   public void controlMoved( ControlEvent e )
   {
      lastCursorPos = null; // Force position recalculation
      hidePopup();
      updatePopup();
   }

   /* Override */
   public void controlResized( ControlEvent e )
   {
      lastCursorPos = null; // Force position recalculation
      hidePopup();
      updatePopup();
   }

   /**
    * Information on function parameters the user is currently editing.
    */
   class ContextData
   {
      public String text; 
      public int bracketPosition;
      public Region bold;
      
      /**
       * Sets up a context data
       * @param text The text to put in the pop-up
       * @param bracketPosition The offset to the bracket position
       * @param bold The region in the pop-up that should be bold (to highlight
       * the current parameter)
       */
      private ContextData( String text, int bracketPosition, Region bold )
      {
         this.text = text;
         this.bracketPosition = bracketPosition;
         this.bold = bold;
      }

      /* Override */
      public String toString()
      {
         return text + " @ " + bracketPosition;
      }      
   }
   
   /**
    * This class monitors a text editor, waiting for it to change. When it does
    * it updates the pop-up.
    */
   class Update extends Thread
   {
      private StyledText textEditor;
      private HidePopup hidePopup = new HidePopup();
      private UpdatePopup updatePopup = new UpdatePopup();
      
      public Update( StyledText textEditor )
      {
         super( "Parameter Pop-up Updater" );
         this.textEditor = textEditor;
      }
      
      /** Runnable that hides the pop-up */
      private class HidePopup implements Runnable
      {
         /* Override */
         public void run()
         {
            hidePopup();
         }
      }
      
      /** Runnable that update the pop-up */
      private class UpdatePopup implements Runnable
      {
         /* Override */
         public void run()
         {
            updatePopup();
         }
      }
      /**
       * This checks for changes in the text editor. It checks the x/y position
       * of the cursor relative to the editor, and the cursors offset.
       */
      private class GetOffset implements Runnable
      {
         /** The last offset that was measured */
         public int offset = -1; 
         
         /** The X/Y location of the cursor realtive to the editor */
         public Point cursorPos = null;
         
         // These tell us what has changed.
         public boolean disposed = false, offsetChanged = false, 
                        cursorChanged = false;
         
         /**
          * This updates the offset and cursor pos. Needs to be run in the SWT
          * thread.
          */
         /* Override */
         public void run()
         {
            if( textEditor.isDisposed() ) {
               disposed = true;
               return;
            }
            
            if( textEditor.getText().length() == 0 ) return;
            
            offset = textEditor.getSelection().x;
            
            Rectangle cursorRect = textEditor.getTextBounds(
               Math.max( 0, offset - 1 ), Math.max( 0, offset - 1 )
            );
            cursorPos = new Point( cursorRect.x, cursorRect.y );
         }

         /**
          * This method checks if the editor has changed. It also updates 
          * the public variables of this class.
          * @return True if the editor has changed, false otherwise.
          */
         public boolean updateOffset()
         {
            int oldOffset = offset;
            Point oldCursorPos = cursorPos;
            
            SWTUtil.exec( this );
            if( disposed || cursorPos == null ) return false;
            
            ZDebug.print( 10, "Update - offset: ", offset, " Cursor: ", cursorPos.x, ",", cursorPos.y );
            
            if( oldOffset != offset ) { 
               offsetChanged = true;
               cursorChanged = false;
               return true;
            } else if ( oldCursorPos == null || 
               oldCursorPos.x != cursorPos.x || oldCursorPos.y != cursorPos.y ) 
            {
               offsetChanged = false;
               cursorChanged = true;
               return true;
            } else {
               offsetChanged = false;
               cursorChanged = false;
               return false;
            }
         }
      }
      
      /**
       * This is the thread update code. It continually checks the editor for 
       * changes and updates the pop-up.
       */
      /* Override */
      public void run()
      {
         GetOffset getOffset = new GetOffset();
         getOffset.updateOffset();
         
         while( true ) {
            
            if( !PreferenceManager.getPreferenceBool( Preference.CONTEXT_ENABLE ) ) {
               if( popupShell != null ) {
                  SWTUtil.exec( hidePopup );
               }
               if( textEditor.isDisposed() ) return;
               try { sleep( 2000 ); } catch( InterruptedException e ) { }
               if( textEditor.isDisposed() ) return;
               continue;
            }
            
            // Wait for a change          
            do {
               if( textEditor.isDisposed() ) return;
               try { sleep( 50 ); } catch( InterruptedException e ) { }
               if( textEditor.isDisposed() ) return;
   
               ZDebug.print( 10, "Tick!" );
   
            } while( !getOffset.updateOffset() );
            
            // Wait for changes to stabilise    
            if( getOffset.cursorChanged || popupShell == null ) {
               if( popupShell != null ) {
                  SWTUtil.exec( hidePopup );
               }
               
               do {
                  if( textEditor.isDisposed() ) return;
                  try { sleep( PreferenceManager.getPreferenceInt( Preference.CONTEXT_WAIT ) ); } catch( InterruptedException e ) { }
                  if( textEditor.isDisposed() ) return;
      
                  ZDebug.print( 8, "Tick!" );
      
               } while( getOffset.updateOffset() );
            }
            
            ZDebug.print( 4, "Finished sleep..." );
   
            // Update the pop-up
            SWTUtil.asyncExec( updatePopup );
            
            if( textEditor.isDisposed() ) return;
         }
         
      }        
   }

  
}
