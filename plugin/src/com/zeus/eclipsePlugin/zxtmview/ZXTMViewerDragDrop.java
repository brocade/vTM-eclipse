/*******************************************************************************
 * Copyright (C) 2015 Brocade Communications Systems, Inc.and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://github.com/brocade/vTM-eclipse/LICENSE
 * This software is distributed "AS IS".
 *
 * Contributors:
 *     Brocade Communications Systems - Main Implementation
 *     IBM Corporation - Code snippet     
 ******************************************************************************/

package com.zeus.eclipsePlugin.zxtmview;

import java.util.LinkedList;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.model.ModelSelection;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.ZXTM;

/**
 * Class that manages drag and drop for the ZXTM viewer.
 */
public class ZXTMViewerDragDrop implements DragSourceListener, DropTargetListener
{
   private ZXTMViewer parent;
   private ModelSelection selection;
   
   /**
    * Create a drag and drop manager for the specified ZXTMViewer.
    * @param parent The ZXTM viewer this class manages drag and drop for.
    */
   public ZXTMViewerDragDrop( ZXTMViewer parent )
   {
      this.parent = parent;
   }

   /**
    * Checks the current selection is only rules, if so the drag is permitted 
    * and the selection is stored.
    */
   public void dragStart( DragSourceEvent event )
   {
      ZDebug.print( 3, "dragStart( ", event, " )" );

      selection = parent.getSelection();
      if( parent.getSelection().isOnlyRules() ) {
         event.doit = true;
      } else {
         selection = null;
         event.doit = false;
      }
   }

   /**
    * Set the data for the drag. Just sets the string to a single rule.
    */
   public void dragSetData( DragSourceEvent event )
   {
      ZDebug.print( 3, "dragSetData( ", event," )" );
      if( selection != null ) {
         event.data = selection.getFirstElement().toString();
      }
   }

   /**
    * Resets the current selection.
    */
   public void dragFinished( DragSourceEvent event )
   {
      ZDebug.print( 3, "dragFinished( ", event, " )" );
      selection = null;
   }
   
   /**
    * Display appropriate feedback on the tree when dragging.
    */
   public void dragOver( DropTargetEvent event )
   {
      event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
      Display display = parent.getSite().getShell().getDisplay();
      
      if( event.item != null ) {
         TreeItem item = (TreeItem) event.item;
         Point pt = display.map( null, parent.getTreeView().getTree(), event.x, event.y );
         Rectangle bounds = item.getBounds();
         if( pt.y < bounds.y + bounds.height / 3 ) {
            event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
         } else if( pt.y > bounds.y + 2 * bounds.height / 3 ) {
            event.feedback |= DND.FEEDBACK_INSERT_AFTER;
         } else {
            event.feedback |= DND.FEEDBACK_SELECT;
         }
      }
   }
   
   /**
    * Handle the user dropping the selection on something. If its on the viewer
    * on a ZXTM or rule in a ZXTM, try and paste it.
    */
   public void drop( DropTargetEvent event )
   {
      ZDebug.print( 3, "drop( ", event, " )" );
      
      if( selection != null && event.item != null ) {
         TreeItem item = (TreeItem) event.item;
         TreeItem parentItem = item.getParentItem();
         
         if( parentItem == null ) parentItem = item;
         
         if( parentItem != null && parentItem.getData() instanceof ZXTM ) {
            ZXTM zxtm = (ZXTM) parentItem.getData();
            
            LinkedList<RuleCopy> list = new LinkedList<RuleCopy>();
            
            // Copy the rules to memory
            for( Rule rule : selection.getSelectedRules() ) {               
               if( ((ZXTM) rule.getModelParent()).equals( zxtm ) ) {
                  continue;
               }
               
               list.add(  new RuleCopy( rule.getName(), rule.getRawCode() ) );
            }
            
            // Paste them
            ZXTMViewer.pasteRules( zxtm, list.toArray( new RuleCopy[list.size()] ) );
         }            
      }
   }

   /* Override */
   public void dragEnter( DropTargetEvent event )
   {
    
   }

   /* Override */
   public void dragLeave( DropTargetEvent event )
   {
      
   }

   /* Override */
   public void dragOperationChanged( DropTargetEvent event )
   {
      
   }

   /* Override */
   public void dropAccept( DropTargetEvent event )
   {
      
   }

   

}
