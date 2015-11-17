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

package com.zeus.eclipsePlugin.zxtmview;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.actions.CopyRuleAction;
import com.zeus.eclipsePlugin.actions.CutRuleAction;
import com.zeus.eclipsePlugin.actions.DeleteProjectAction;
import com.zeus.eclipsePlugin.actions.DeleteRuleAction;
import com.zeus.eclipsePlugin.actions.DisconnectZXTMAction;
import com.zeus.eclipsePlugin.actions.GetErrorAction;
import com.zeus.eclipsePlugin.actions.NewRuleAction;
import com.zeus.eclipsePlugin.actions.NewZXTMAction;
import com.zeus.eclipsePlugin.actions.PasteRuleAction;
import com.zeus.eclipsePlugin.actions.RenameRuleAction;
import com.zeus.eclipsePlugin.actions.ZXTMSettingsAction;
import com.zeus.eclipsePlugin.consts.ImageFile;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.ModelListener;
import com.zeus.eclipsePlugin.model.ModelSelection;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.model.ModelElement.Event;
import com.zeus.eclipsePlugin.model.ModelElement.State;
import com.zeus.eclipsePlugin.project.operations.PasteRulesOp;
import com.zeus.eclipsePlugin.swt.AnimationTimer;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.AnimationTimer.Mode;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;

/**
 * A Eclipse view that displays the current state of the model. Allows 
 * alteration of the model using a right mouse menu.
 */
public class ZXTMViewer extends ViewPart
{   
   private static ZXTMViewer singleton = null;
   
   private ModelController controller;
   private TreeViewer view;
   private ElementListener listener = new ElementListener();
   
   private AnimationTimer timer = new AnimationTimer( 100 );
   
   private static RuleCopy[] ruleCopies;
   
   /**
    * Method that sets up the view.
    */
   /* Override */
   public void createPartControl( Composite parent )
   {
      // Create tool-bars and menus
      createToolBar();
      
      // Create hierarchical view and set its input.
      view = new TreeViewer( parent );
      view.setContentProvider( new ZXTMViewContentProvider() );
      view.setLabelProvider( new ZXTMViewLabelProvider( timer ) );
      view.addDoubleClickListener( new ZXTMViewInputManager() );
      view.setComparator( new ViewerSorter() );

      // Drag and Drop
      ZXTMViewerDragDrop dragDrop = new ZXTMViewerDragDrop( this );      
      Transfer[] transfer =  new Transfer[] {TextTransfer.getInstance()};
      int operations = DND.DROP_COPY | DND.DROP_MOVE;
      
      DragSource dragSource = new DragSource( view.getTree(), operations );
      dragSource.setTransfer( transfer );
      dragSource.addDragListener( dragDrop );      
      
      DropTarget target = new DropTarget( view.getTree(), operations );
      target.setTransfer( transfer );
      target.addDropListener( dragDrop );

      // Add listener for the model so that the view can be refreshed when 
      // something changes
      controller = ZXTMPlugin.getDefault().getModelController();      
      view.setInput( controller );

      controller.addListener( listener );
      for( ZXTM zxtm : controller.getSortedZXTMs() ) {
         zxtm.addListener( listener );
         
         for( Rule rule : zxtm.getRules() ) {
            rule.addListener( listener );
         }
      }
      
      view.expandAll();     
      
      // Menu
      MenuManager menuManager = new MenuManager();
      menuManager.setRemoveAllWhenShown( true );
      menuManager.addMenuListener( new IMenuListener() {
         public void menuAboutToShow( IMenuManager manager )
         {
            ZXTMViewer.this.fillContextMenu( manager );
         }
      } );
      
      Menu contextMenu = menuManager.createContextMenu( view.getTree() );
      view.getTree().setMenu( contextMenu );
      this.getSite().registerContextMenu( menuManager, view );

      // Setup animation timer
      timer.setCallback( new UpdateAnimations() ); 
      timer.setLimit( ImageFile.SPINNY.getFrames() * 2 );
      timer.setMode( Mode.LOOP );
      timer.start();
      
      singleton = this;
   }
   
   /**
    * Removes the update listener from all the elements in the model.
    */
   /* Override */
   public void dispose()
   {
      singleton = null;
      
      timer.stop();
      
      controller.removeListener( listener );
      for( ZXTM zxtm : controller.getSortedZXTMs() ) {
         zxtm.removeListener( listener );
         
         for( Rule rule : zxtm.getRules() ) {
            rule.removeListener( listener );
         }
      }
      
      super.dispose();
   }

   /**
    * Create the contents of the right mouse button menu. This is called every 
    * time it is displayed. 
    * @param menuManager The menuManager object which we add the options to.
    */
   private void fillContextMenu( IMenuManager menuManager )
   {
      ZDebug.print( 4, "fillContextMenu()" );
      ModelSelection selected = getSelection();
      ModelController controller = ZXTMPlugin.getDefault().getModelController();
      
      // Create the groups we will be adding options to.
      menuManager.add( new Separator( "Add" ));
      GroupMarker marker = new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS);
      menuManager.add( marker );
      
      menuManager.add( new Separator( "Info" ) );
      menuManager.add( marker );
      
      menuManager.add( new Separator( "Edit" ) );
      menuManager.add( marker );
      
      NewZXTMAction newZXTMAction = new NewZXTMAction();
      menuManager.appendToGroup( "Add", newZXTMAction );

      // If there's at least 1 ZXTM, allow people to add rules
      if( controller.getSortedZXTMs().length > 0 ) { 
         NewRuleAction newRuleAction;
         newRuleAction = new NewRuleAction( selected );        
         menuManager.appendToGroup( "Add", newRuleAction );
      }
      
      // Get error info for problem elements
      if( selected.getSize() == 1
         && ((ModelElement) selected.getFirstElement()).getModelState() == State.CANNOT_SYNC ) 
      {
         GetErrorAction errorAction = new GetErrorAction( selected );
         menuManager.appendToGroup( "Info", errorAction );
      }
      
            
      // If we've got only rules selected, enable the copy/cut rule action.
      if( selected.isOnlyRules() ) {         
         menuManager.appendToGroup( "Edit", new CutRuleAction( selected ) );
         menuManager.appendToGroup( "Edit", new CopyRuleAction( selected ) );
      } 
      
      // If we have selected a ZXTM, paste is allowed.
      if( selected.getSelectedZXTM() != null && ruleCopies != null && ruleCopies.length > 0 ) {
         menuManager.appendToGroup( "Edit", new PasteRuleAction( selected ) );
      }
      
      // Delete if only rules, rename if only 1 rule
      if( selected.isOnlyRules() ) {
         menuManager.appendToGroup( "Edit", new DeleteRuleAction( selected ) );
         if( selected.getSize() == 1 ) {
            menuManager.appendToGroup( "Edit", new RenameRuleAction( selected ) );
         }
      }
      
      if( selected.isOnlyOneZXTM() && 
          selected.firstZXTM().getModelState() != State.DISCONNECTED ) 
      {
         menuManager.appendToGroup( "Info", new ZXTMSettingsAction( selected ) );
      }
      
      // If we've got only ZXTM selected, enable the disconnect/settings ZXTM 
      // actions.
      if( selected.isOnlyZXTMs() ) {         
         menuManager.appendToGroup( "Info", new DisconnectZXTMAction( selected ) );
         menuManager.appendToGroup( "Edit", new DeleteProjectAction( selected ) );
      }
   }
   
   /**
    * Get the selected elements of this view.
    * @return A ModelSelection object that stores all this views currently 
    * selected objects.
    */
   public ModelSelection getSelection()
   {
      TreeItem[] selection = view.getTree().getSelection(); 
      if( selection.length < 1 ) {
         return new ModelSelection();
      }
      
      LinkedList<ZXTM> zxtmList = new LinkedList<ZXTM>();
      LinkedList<Rule> ruleList = new LinkedList<Rule>();
      
      for( int i = 0; i < selection.length; i++ ) {
         Object data = selection[i].getData();
         if( data instanceof ModelElement ) {
            ModelElement element = (ModelElement) data;
            
            switch( element.getModelType() ) 
            {
               case ZXTM: zxtmList.add( (ZXTM) element ); break;
               case RULE: ruleList.add( (Rule) element ); break;
            }
         }
      }
      
      return new ModelSelection( zxtmList, ruleList );
   }
   
   /**
    * Class which returns the selection for the currently open viewer if there 
    * is one.
    * @return The ModelSelection for the currently open viewer, or null if there
    * is none open.
    */
   public static ModelSelection getSelectionForOpenViewer()
   {
      if( singleton != null ) {
         return singleton.getSelection();
      } else {
         return null;
      }
   }

   /**
    * Create a tool bar for this view, which allows you to add new rules and 
    * ZXTMs.
    */
   private void createToolBar()
   {
      IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();

      // Add actions to the toolbar
      toolbar.add( new NewZXTMAction() );
      toolbar.add( new NewRuleAction() );
   }

   /**
    * This class listens to the model controller and refreshes the view when 
    * something changes.
    */
   private class ElementListener implements ModelListener, Runnable
   {
      /* Override */
      public void modelUpdated( ModelElement element, Event event )
      {
         SWTUtil.exec( this );
      }
      
      /* Override */
      public void childAdded( ModelElement parent, ModelElement child )
      {
         child.addListener( this );
         SWTUtil.exec( this );         
      }
      
      /* Override */
      public void stateChanged( ModelElement element, State state )
      {
         SWTUtil.exec( this );         
      }
         
      /* Override */
      public void run() {  
         ZDebug.print( 5, "Refreshing ZXTM View due to update" );
         if( view.getTree().isDisposed() ) return;
         view.refresh( true );
      }
   }
   
   /**
    * This is run by an animation timer to update the spinner animations. Also
    * completely refreshes the view.
    */
   private class UpdateAnimations implements Runnable
   {
      private ModelController model;

      public UpdateAnimations()
      {
         model = ZXTMPlugin.getDefault().getModelController();
      }

      /* Override */
      public void run()
      {
         if( view.getTree().isDisposed() ) return;
         
         if( timer.getTime() == 1 ) {
            view.refresh( true );
            return;
         }
         
         for( ZXTM zxtm : model.getSortedZXTMs() ) {
            if( zxtm.getModelState() == State.WAITING_FOR_FIRST_UPDATE ) {
               view.refresh( zxtm, true );
            }
            for( Rule rule : zxtm.getRules() ) {
               if( rule.getModelState() == State.WAITING_FOR_FIRST_UPDATE ) {
                  view.refresh( rule, true );
               }
            }
         }
      }
      
   }

   /* Override */
   public void setFocus()
   {
      
   }
   
   /**
    * Add the supplied rules to the specified ZXTM. Uses the PasteRulesOp with
    * a progress dialog. Users will be queried if the rules already exist.
    * @param zxtm The ZXTM to add the rules to.
    * @param copies The rules to add.
    */
   public static void pasteRules( ZXTM zxtm, RuleCopy[] copies )
   {
      ZDebug.print( 3, "pasteRules( ", zxtm, " )" );
      if( copies == null || copies.length == 0 ) return;
      
      PasteRulesOp op = new PasteRulesOp( copies, zxtm );
      try {
         SWTUtil.progressDialog( op );
      } catch( InvocationTargetException e ) {
         ZDialog.showErrorDialog( "Rule insert failed", 
            "Could not insert rules: " + ZUtil.getRootCauseMessage( e )
         );
      }
   }
   
   /**
    * Paste the rules stored in memory. Note this does not use the clip-board.
    * @param zxtm The ZXTM to paste the rules to.
    */
   public static void pasteRulesFromMemory( ZXTM zxtm )
   {
      ZDebug.print( 3, "pasteRulesFromMemory( ", zxtm, " )" );
      pasteRules( zxtm, ruleCopies );
   }

   /**
    * Copy the rules to local memory. Does not use the clip-board however.
    * @param selection The selection to copy to memory.
    */
   public synchronized static void copyRules( ModelSelection selection )
   {
      int i = 0;
      ruleCopies = new RuleCopy[ selection.size() ];
      
      for( Rule rule : selection.getSelectedRules() ) {
         if( rule.getModelState() != State.UP_TO_DATE ) continue;   
         ruleCopies[i++] = new RuleCopy( rule.getName(), rule.getRawCode() );
      }
   }

   /**
    * Returns the TreeViewer that displays the model.
    * @return The TreeViewer that displays the model. 
    */
   public TreeViewer getTreeView()
   {
      return view;
   }
   
}
