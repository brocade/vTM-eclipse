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

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Colour;
import com.zeus.eclipsePlugin.swt.AnimationTimer;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * This class creates the notification bar at the bottom of the TrafficScript
 * editor when there is a problem.
 */
public class EditorNotification extends Thread
{
   private TrafficScriptEditor editor;
   
   private static final int NOTIFY_HEIGHT = 38; 
   
   /**
    * Create the notification bar, and start the setup thread.
    * @param editor
    */
   public EditorNotification( TrafficScriptEditor editor )
   {
      super( "Editor Notification Bar" );
      this.editor = editor;
      
      timer = new AnimationTimer( 8 );
      timer.setLimit( NOTIFY_HEIGHT );
      timer.setMode( AnimationTimer.Mode.STOP );
      last = timer.getTime();
      
      buttonListener1 = new ButtonListener();
      
      // Starts this thread. This waits for the editor to be setup and then
      // creates the bars components.
      this.start();
   }

   private ISourceViewer viewer;
   private Composite superParent, group;
   private GridLayout layout;
   private AnimationTimer timer;   
   private Label labelText;
   private Button button1;
   
   private String msg = "";
   private String buttonText1 = "";
   private ButtonListener buttonListener1;
      
   private int last;
   private Color bgColour;
   
   private boolean ready = false;

   private boolean setupComplete = false;
   
   /**
    * Check if the editor is ready to be setup.
    * @return True if the editor is ready to be setup.
    */
   private boolean editorReady()
   {
      if( checkStopped() || !ZXTMPlugin.isEclipseLoaded() ) {
         ready = false;
         return false;
      }      
      
      SWTUtil.exec( new Runnable() { public void run()
      {
         ready = (
            editor.getViewer() != null &&
            editor.getViewer().getTextWidget() != null &&
            editor.getViewer().getTextWidget().getParent() != null
         );
         
      } } );
      
      return ready;
   }

   /** 
    * This is the thread code. It sets up the components of the notification bar
    * when the editor is ready.
    */
   /* Override */
   public void run()
   {
      while( !editorReady() ) 
      {
         try { sleep( 500 ); } catch( InterruptedException e ) { }
         if( checkStopped() ) return;
      }     
      
      viewer = editor.getViewer();
      
      try { sleep( 1000 ); } catch( InterruptedException e ) { }
      if( !editorReady() ) return;
      
      bgColour = ZXTMPlugin.getDefault().getColourManager().getColour( Colour.TS_NOTIFY_BG );
      
      SWTUtil.exec( new Runnable() { public void run()
      {
         if( !editorReady() ) return;
         try {
            String text;
            ZDebug.print( 5, "viewer: [", viewer, "]"  );
            StyledText widget = viewer.getTextWidget();
            ZDebug.print( 5, "widget: [", widget, "]"  );
            Canvas parent = (Canvas) widget.getParent();
            ZDebug.print( 5, "parent: [", parent, "]"  );
            
            superParent = parent.getParent(); 
            layout = SWTUtil.createGridLayout( 1, 0, 0 );
            SWTUtil.makeLayoutTight( layout );
            superParent.setLayout( layout );
            
            ZDebug.print( 5, "super parent: [", superParent, "]"  );
            ZDebug.print( 5, "  layout: [", superParent.getLayout().getClass(), "]"  );
            
            for( Control child : superParent.getChildren() ) {
               ZDebug.print( 6, "  child: [", child, "]"  );
            }
           
            SWTUtil.gridDataFillVertical( parent );
            SWTUtil.gridDataFillHorizontal( parent );
            
            group = new Composite( superParent, SWT.NONE );     
            group.setSize( 10, 10 );
            group.setLayout( SWTUtil.createGridLayout( 4, 2, 2 ) );
            group.setBackground( bgColour );
            SWTUtil.gridDataFillHorizontal( group );
            
            // This makes it visible if not 0
            SWTUtil.gridDataPreferredHeight( group, 0 ); 
            
            text = msg != null ? msg : ""; 
            labelText = SWTUtil.addLabel( group, "" );
            labelText.setBackground( bgColour );
            SWTUtil.gridDataFillHorizontal( labelText );
            
            text = buttonText1 != null ? buttonText1 : ""; 
            button1 = SWTUtil.addButton( group, text );
            button1.addSelectionListener( buttonListener1 );
            if( buttonText1 == null || buttonText1.trim().equals( "" ) ) {
               button1.setVisible( false );
            }
            
            superParent.layout( true, true );
            superParent.redraw();
            superParent.update();
            
         } catch( Exception e ) {
            ZDebug.printStackTrace( e, "Failed to create notification bar" );
         }
      } } );  
      
      Runnable run = new Runnable() { public void run()
      {
         if( !editorReady() ) return;  
         if( !msg.equals( labelText.getText() ) ) {
            labelText.setText( msg );
         }
         
         if( buttonText1 != null && !buttonText1.trim().equals( "" ) ) {   
            if( !button1.getText().equals( buttonText1 ) ) {
               button1.setVisible( true );
               button1.setText( buttonText1 );
            }
         } else if( button1.getVisible() ) {
            button1.setVisible( false );
         }
         
         if( timer.getTime() == last ) {
            return;
         }
         
         last = timer.getTime();
         
         if( last < 0 ) {
            timer.stop();
            timer.setTime( 0 );
            last = 0;           
         }
         
         if( !editorReady() ) return;
         if( superParent.getLayout() != layout ) {
            ZDebug.print( 4, "CHANGED BACK" );
            superParent.setLayout( layout );
         }            
         
         ZDebug.print( 5, "", timer.getTime() ); 
         SWTUtil.gridDataPreferredHeight( group, timer.getTime() );
         
         superParent.layout( true, true );
         superParent.redraw();
         superParent.update();
         
      } };
      
      timer.setCallback( run );
      setupComplete = true;
      
      timer.start();      
   }
   
   /**
    * Set the message of the bar.
    * @param msg The message to display in the notify bar
    */
   public void setMessage( String msg )
   {
      this.msg = msg;      
   }
   
   /**
    * Add a button to the bar, which will call the passed action when clicked.
    * @param text The text on the button.
    * @param action The action to perform when the button is clicked.
    */
   public void setButton1( String text, Runnable action )
   {
      this.buttonText1 = text;
      this.buttonListener1.action = action;
   }
   
   /**
    * Show the notify bar.
    */
   public void showNotifyBar()
   {
      timer.setIncrement( 1 );      
      timer.setLimit( NOTIFY_HEIGHT );
      if( setupComplete ) timer.start(); 
   }
   
   /**
    * Hide the notify bar.
    */
   public void hideNotifyBar()
   {
      timer.setIncrement( -1 );
      timer.setLimit( 0 );
      if( setupComplete ) timer.start();     
   }
   
   /**
    * Check if the parent components have been disposed.
    * @return
    */
   private boolean checkStopped()
   {
      if( superParent != null && (
            superParent.isDisposed() ||
            labelText.isDisposed() ||
            button1.isDisposed() ) 
        ) 
      {
         ZDebug.print( 3, "Parent was disposed" );
         return true;
      }
      
      return false;
   }
   
   /**
    * Listener for buttons being clicked. Runs the passed Runnable.
    */
   class ButtonListener implements SelectionListener
   {
      public Runnable action = null;
      
      /* Override */
      public void widgetDefaultSelected( SelectionEvent arg0 )
      {
         widgetSelected( arg0 );
      }

      /* Override */
      public void widgetSelected( SelectionEvent arg0 )
      {
         if( action != null ) {
            action.run();
         }
      }
      
   }

   
}
