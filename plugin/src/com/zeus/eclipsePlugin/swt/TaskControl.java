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

package com.zeus.eclipsePlugin.swt;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.editor.TaskTag;
import com.zeus.eclipsePlugin.editor.TaskTag.Priority;

/**
 * Control for changing the task labels that auto-add task markers and are 
 * specially coloured in the editor.
 */
public class TaskControl
{
   private Composite parent;
   private Composite taskComposite;
   private Button buttonAdd, buttonEdit, buttonDelete;
   private TaskListener listener = new TaskListener();
   private Table table;
   
   private HashMap<String,Priority> priorityMap = new HashMap<String,Priority>();
   private Priority[] priorities;

   /**
    * Create the task control with the specified task preference's string 
    * representation.
    * @param parent The composite to add this control to
    * @param taskPref The string representation of the all the tasks this
    * control is editing.
    */
   public TaskControl( Composite parent, String taskPref )
   {
      this.parent = parent;
      
      priorities = EnumSet.allOf( Priority.class ).toArray( new Priority[0] ); 
      for( Priority priority : priorities ) {
         priorityMap.put( priority.getText(), priority );
      }
      
      taskComposite = SWTUtil.createGridLayoutComposite( parent, 2 );
      
      createTable( taskComposite, TaskTag.getTaskTags( taskPref ) );
      createButtons( taskComposite );
   }

   /**
    * Create the table that displays the current task markers and their 
    * priorities.
    * @param parent The parent composite to add the table to.
    * @param tasks The collection of TaskTag objects that represent the tags
    * going into the table.
    */
   private void createTable( Composite parent, Collection<TaskTag> tasks )
   {
      table = new Table( parent, 
         SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION
      );
      
      SWTUtil.gridDataFillHorizontal( table );
      SWTUtil.gridDataFillVertical( table );
      SWTUtil.gridDataPreferredHeight( table, 200 );
      SWTUtil.gridDataPreferredHeight( table, 200 );
      
      table.setLinesVisible( true );
      table.setHeaderVisible( true );
      
      TableColumn tagColumn = new TableColumn( table, SWT.NONE );
      tagColumn.setText( ZLang.ZL_Tag );
      tagColumn.setWidth( 100 );
      
      TableColumn priorityColumn = new TableColumn( table, SWT.NONE );
      priorityColumn.setText( ZLang.ZL_Priority );
      priorityColumn.pack();      
      
      for( TaskTag task : tasks ) {       
         TableItem item = new TableItem( table, SWT.NONE );
         item.setText( 0, task.getTag() );
         item.setText( 1, task.getPriority().getText() );
      }
      
      table.addSelectionListener( new SelectionListener() {
         /* Override */ public void widgetDefaultSelected( SelectionEvent e ) { widgetSelected( e ); }

         /* Override */
         public void widgetSelected( SelectionEvent e )
         {
            updateButtons();
         }         
      } );
   }
   
   /**
    * Create the buttons that sit to the right of the table. These are used to 
    * alter the tables contents.
    * @param parent The parent composite to add the button's composite to.
    * @return The Composite containing all the buttons.
    */
   private Composite createButtons( Composite parent )
   {
      Composite composite = SWTUtil.createGridLayoutComposite( parent, 1 );
      SWTUtil.gridDataFillVertical( composite );
      
      buttonAdd = SWTUtil.addButton( composite, ZLang.ZL_AddButton );  
      buttonAdd.addSelectionListener( listener );
      SWTUtil.gridDataPreferredWidth( buttonAdd, 100 );
      
      buttonEdit = SWTUtil.addButton( composite, ZLang.ZL_EditButton );
      buttonEdit.addSelectionListener( listener );
      SWTUtil.gridDataPreferredWidth( buttonEdit, 100 );
      
      buttonDelete = SWTUtil.addButton( composite, ZLang.ZL_Delete );
      buttonDelete.addSelectionListener( listener );
      SWTUtil.gridDataPreferredWidth( buttonDelete, 100 );
      
      updateButtons();
      return composite;
   }
   
   /**
    * Change the tasks displayed by this control to the tasks within the passed
    * task string representation.
    * @param newTaskString The task string that this control should now display.
    */
   public void setTaskString( String newTaskString )
   {
      table.removeAll();
      
      for( TaskTag task : TaskTag.getTaskTags( newTaskString ) ) {
         TableItem item = new TableItem( table, SWT.NONE );
         item.setText( 0, task.getTag() );
         item.setText( 1, task.getPriority().getText() );
      }
   }
   
   /**
    * Get the string representation for the current state of this control.
    * @return The string representation of all the tasks in the table.
    */
   public String getTaskString()
   {
      ZDebug.print( 4, "getTaskString()" );
      
      LinkedList<TaskTag> list = new LinkedList<TaskTag>();
      for( TableItem item : table.getItems() ) {
         list.add( new TaskTag( 
            item.getText( 0 ), 
            priorityMap.get( item.getText(1) ) 
         ) );       
      }
    
      return TaskTag.createTagString( list );
   }
   
   /**
    * Enable and disable the buttons in the control based on what is selected in 
    * the table.
    */
   protected void updateButtons()
   {
      if( table.getSelectionIndex() < 0 ) {
         if( buttonEdit != null ) buttonEdit.setEnabled( false );
         if( buttonDelete != null ) buttonDelete.setEnabled( false );
      } else {
         if( buttonEdit != null ) buttonEdit.setEnabled( true );
         if( buttonDelete != null ) buttonDelete.setEnabled( true );
      }
   }

   /**
    * Get the main composite for this control.
    * @return The main composite for this control.
    */
   public Composite getComposite()
   {
      return taskComposite;
   }
   
   /**
    * Get the parent composite for this control.
    * @return The parent composite for this control.
    */
   public Composite getParent()
   {
      return parent;
   }

   /**
    * Listener that is called when one of the buttons is pressed.
    */
   private class TaskListener implements SelectionListener
   {

      /* Override */
      public void widgetDefaultSelected( SelectionEvent e )
      {
         widgetSelected( e );
      }

      /**
       * Perform the appropriate action when a button is pressed.
       */
      /* Override */
      public void widgetSelected( SelectionEvent e )
      {
         ZDebug.print( 4, "widgetSelected( ", e, " ) - ", table.getSelectionIndex() );
         
         // Delete Button - delete currently selected item from the table.
         if( e.widget == buttonDelete && table.getSelectionIndex() >= 0 ) {
            ZDebug.print( 5, "Deleting item" );
            int toDelete = table.getSelectionIndex();
            table.remove( toDelete );
            
            if( (toDelete - 1) > 0 ) {
               table.setSelection( toDelete - 1 );
            } else if( table.getItemCount() > 0 ) {
               table.setSelection( 0 );
            }
            
         // Add a new task tag - Show our task dialog
         } else if( e.widget == buttonAdd ) {
            ZDebug.print( 5, "Adding new item" );
            TaskDialog dialog = new TaskDialog( 
               ZLang.ZL_AddTaskTitle, "", Priority.NORMAL 
            );
            dialog.setBlockOnOpen( true );
            dialog.open();
            
            if( dialog.isOkPressed() ) {
               String newTag = dialog.getTag();
               
               // Insert the new tag in the appropriate place (for alphabetical
               // ordering.
               int i = -1;
               int insertIndex = table.getItemCount();
               for( TableItem item : table.getItems() ) {
                  i++;
                  
                  String tag = item.getText( 0 );
                  int diff = newTag.compareTo( tag );
                  if( diff < 0 ) {
                     insertIndex = i;
                     break;
                  }
               }
               
               TableItem item = new TableItem( table, SWT.NONE, insertIndex );
               item.setText( 0, newTag );
               item.setText( 1, dialog.getPriority().toString() );
               table.setSelection( insertIndex );
            }
            
         // Edit the selected task tag = Show our task dialog
         } else if( e.widget == buttonEdit && table.getSelectionIndex() >= 0 ) {
            ZDebug.print( 5, "Editing item" );
            TableItem oldItem = table.getSelection()[0];
            
            TaskDialog dialog = new TaskDialog( 
               ZLang.ZL_EditTaskTitle, 
               oldItem.getText( 0 ), 
               priorityMap.get( oldItem.getText( 1 ) )
            );
            dialog.setBlockOnOpen( true );
            dialog.open();
            
            if( dialog.isOkPressed() ) {
               // Delete the old tag
               table.remove( table.getSelectionIndex() );
               
               String newTag = dialog.getTag();
               
               // Insert the new tag in the appropriate place (for alphabetical
               // ordering.
               int i = -1;
               int insertIndex = table.getItemCount();
               for( TableItem item : table.getItems() ) {
                  i++;
                  
                  String tag = item.getText( 0 );
                  int diff = newTag.compareTo( tag );
                  if( diff < 0 ) {
                     insertIndex = i;
                     break;
                  }
               }
               
               TableItem item = new TableItem( table, SWT.NONE, insertIndex );
               item.setText( 0, newTag );
               item.setText( 1, dialog.getPriority().toString() );
               table.setSelection( insertIndex );
            }
         }
         
         updateButtons();
      }

   }
   
   /**
    * Custom dialog that lets the user edit a task tag thats stored in the 
    * table.
    */
   private class TaskDialog extends Dialog
   {
      private String title, tag;
      private Priority priority;
      private boolean okPressed = false;
      
      private Combo comboPriority;
      private Text textTag;

      /**
       * Create a new dialog.
       * @param title The title for the dialog's window.
       * @param tag The initial tag string.
       * @param priority The initial priority of the tag.
       */
      protected TaskDialog( String title, String tag, Priority priority )
      {
         super( Display.getCurrent().getActiveShell() );
         this.title = title;
         this.tag = tag;
         this.priority = priority;
      }
      
      /**
       * Sets the dialogs title.
       */
      /* Override */
      protected void configureShell( Shell newShell )
      {
         super.configureShell( newShell );
         newShell.setText( title );
      }
      
      /**
       * Setup the UI controls.
       */
      /* Override */
      protected Control createDialogArea( Composite parent )
      {
         Composite composite = (Composite) super.createDialogArea( parent );
         composite.setLayout( SWTUtil.createGridLayout( 2 ) );
         
         textTag = SWTUtil.addLabeledText( composite, ZLang.ZL_TagLabel, SWTUtil.FILL ).text();
         textTag.setText( tag );
         textTag.addModifyListener( new ModifyListener() {
            
            /* Override */
            public void modifyText( ModifyEvent e )
            {
               Button ok = getButton( IDialogConstants.OK_ID );
               ok.setEnabled( textTag.getText().matches( "^\\w+$" ) ); 
            }       
            
         } );
         
         comboPriority = SWTUtil.addLabeledCombo( composite, ZLang.ZL_PriorityLabel, 
            (Object[]) priorities
         ).combo();
         
         int i = 0;
         for( Priority currentPriority : priorities ) {
            if( currentPriority == priority ) {
               comboPriority.select( i );
               break;
            }
            i++;
         }
         composite.pack();
         
         tag = null;
         priority = null;
         
         return composite;
      }
      
      /**
       * Disable the OK button if the task tag is invalid
       */
      /* Override */
      protected Control createButtonBar( Composite parent )
      {
         Control buttonBar = super.createButtonBar( parent );
         
         Button ok = getButton( IDialogConstants.OK_ID );
         ok.setEnabled( textTag.getText().matches( "^\\w+$" ) ); 
         return buttonBar;
      }

      /**
       * Store the dialogs settings so they can be accessed after it is 
       * disposed.
       */
      /* Override */
      protected void okPressed()
      {
         okPressed = true;
         tag = textTag.getText();
         priority = priorities[ comboPriority.getSelectionIndex() ];
         
         super.okPressed();
      }

      /**
       * Get the tag text. May be null if cancel was selected
       * @return The tag string that was entered by the user.
       */
      public String getTag()
      {
         return tag;
      }

      /**
       * Get the priority that was selected by the user.
       * @return
       */
      public Priority getPriority()
      {
         return priority;
      }

      /**
       * Was OK pressed by the user?
       * @return True if the OK button was pressed, false if cancel was pressed.
       */
      public boolean isOkPressed()
      {
         return okPressed;
      }
   }
   
}
