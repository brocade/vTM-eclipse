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

package com.zeus.eclipsePlugin.swt.dialogs;

import org.apache.zeusaxis.AxisFault;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.ModelException;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.DialogOption;

/**
 * A dialog that displays error information from an exception.
 */
public class ExceptionDialog extends Dialog
{
   private String text, title;
   private Exception e;
   private Text details;
   private Button button;
   private Composite composite;
   private int smallSize = -1;
   
   private static final int DIALOG_WIDTH = 400;
   
   /**
    * Setup the dialog, with the message and exception to display.
    * @param parentShell The parent shell of this dialog
    * @param title The dialog's title. 
    * @param text The message for the dialog, shown in bold.
    * @param e The exception to display.
    */
   protected ExceptionDialog( Shell parentShell, String title, String text, Exception e )
   {
      super( parentShell );
      this.title = title;
      this.text = text;
      this.e = e;
   }
   
   /**
    * Set the title of the dialog
    */
   /* Override */
   protected void configureShell( Shell newShell )
   {
      super.configureShell( newShell );
      newShell.setText( title );
   }
   
   /**
    * Setup the contents of the dialog.
    */
   /* Override */
   protected Control createDialogArea( Composite parent )
   {
      composite = (Composite) super.createDialogArea( parent );      
      composite.setLayout( SWTUtil.createGridLayout( 1 ) );
      
      Label label = SWTUtil.addLabel( composite, text );
      SWTUtil.gridDataFillHorizontal( label );
      SWTUtil.gridDataPreferredWidth( label, DIALOG_WIDTH );
      SWTUtil.fontStyle( label, SWT.BOLD );
      
      SWTUtil.createBlankHorizontalFill( composite, 1 );
      
      Label error = SWTUtil.addLabel( composite, e.getMessage() );
      SWTUtil.gridDataFillHorizontal( error );
      SWTUtil.gridDataPreferredWidth( error, DIALOG_WIDTH );
      
      SWTUtil.createBlankHorizontalFill( composite, 1 );
      
      button = SWTUtil.addButton( composite, ZLang.ZL_ShowDetails );
      button.addSelectionListener( new ButtonListener() );
      
      // Generate stack trace text
      StringBuffer buffer = new StringBuffer( 1000 );
      Throwable currentException = e;
      while( currentException != null ) {
         if( buffer.length() > 0 ) {
            buffer.append( "\n" + ZLang.ZL_ExceptionCausedBy + "\n" );
         }
         
         buffer.append( " " );
         buffer.append( currentException.getClass().getSimpleName() );
         buffer.append( ": " );
         
         if( currentException instanceof AxisFault ) {
            buffer.append( currentException.getMessage().replaceAll( "\\s+", " " ) );
         } else if( currentException instanceof ModelException ) {                        
            buffer.append( currentException.getMessage() ).append( "\n" );
            
            ModelException modelExeption = (ModelException) currentException;
            ModelElement element = modelExeption.getSource();
            buffer.append( " " + ZLang.ZL_ModelSource ).append( element.getModelType() );
            buffer.append( " (" + element.toString() + ")" );
            
         } else {
            buffer.append( currentException.getMessage() );
         }
         buffer.append( "\n" );
         
         for( StackTraceElement element : currentException.getStackTrace() ) {
            buffer.append( "  " );
            buffer.append( element.toString() ).append( '\n' );
         }
         
         if( currentException != currentException.getCause() ) { 
            currentException = currentException.getCause();
         } else {
            currentException = null;
         }
      }
         
      details = SWTUtil.addMultiText( composite, buffer.toString(), DIALOG_WIDTH );
      SWTUtil.gridDataPreferredHeight( details, 1 );
      details.setEditable( false );
      details.setVisible( false );
     
      composite.layout( true, true );
      composite.pack();
      
      return composite;
   }
   
   /**
    * This dialog only has a single OK button. 
    */
   protected void createButtonsForButtonBar( Composite parent )
   {
      createButton( 
         parent, IDialogConstants.OK_ID, 
         DialogOption.OK.getText(), true
      );
   }
   
   /**
    * Listens to the show/hide details button, and toggles the visibility of the
    * exception stack trace.
    */
   private class ButtonListener implements SelectionListener
   {
      public void update()
      {
         if( smallSize == -1 ) smallSize = getShell().getSize().y; 
         
         details.setVisible( !details.getVisible() );
         SWTUtil.gridDataFillVertical( details );
         
         if( details.getVisible() ) {
            button.setText( ZLang.ZL_HideDetails );
            getShell().setSize( getShell().getSize().x, 400 );
         } else {
            button.setText( ZLang.ZL_ShowDetails );
            getShell().setSize( getShell().getSize().x, smallSize );  
         }
              
      }

      /* Override */
      public void widgetDefaultSelected( SelectionEvent arg0 )
      {
         update();
      }

      /* Override */
      public void widgetSelected( SelectionEvent arg0 )
      {
         update();
      }
      
   }
}
