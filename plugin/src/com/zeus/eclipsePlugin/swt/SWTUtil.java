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

package com.zeus.eclipsePlugin.swt;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.themes.ITheme;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.ExternalPreference;
import com.zeus.eclipsePlugin.consts.Ids;
import com.zeus.eclipsePlugin.consts.ExternalPreference.Source;

/**
 * Set of static functions for creating and modifying SWT components.
 */
public class SWTUtil
{   
   public static final int FILL = -1;
   
   /** Font id for the standard text editor */
   public static final String FONT_TEXT_EDITOR = "org.eclipse.jface.textfont";
   
   /** If set to true all controls created with this class will have borders. 
    *  Useful for debugging layout problems. */
   public static boolean debugBorders = false;
   
   /** Returns an SWT border style depending on the debugBorders variable.*/
   public static int getDebugBorder() 
   {
      return debugBorders ? SWT.BORDER : SWT.NONE;
   }

   /**
    * Creates a row layout with default values
    * @param isHorziontal If true the layout will be horizontal, otherwise 
    * vertical.
    * @return A new RowLayout object.
    */
   public static RowLayout createRowLayout( boolean isHorziontal )
   {
      RowLayout layout = new RowLayout();
      layout.wrap = false;
      layout.pack = true;
      layout.justify = false;
      layout.spacing = 0;
      layout.fill = true;
      
      if( isHorziontal ) {
         layout.type = SWT.HORIZONTAL;
      } else {
         layout.type = SWT.VERTICAL;
      }
         
      return layout;
   }
   
   /**
    * Creates a composite (used to store more than one control) with a row 
    * layout already added to it.
    * @param parent The parent control of this composite (what it will be added
    * to)
    * @param isHorziontal If true the layout will be horizontal, otherwise 
    * vertical.
    * @return The new Composite control.
    */
   public static Composite createRowLayoutComposite( Composite parent, boolean isHorziontal )
   {
      Composite comp = new Composite( parent, getDebugBorder() );
      comp.setLayout( createRowLayout( isHorziontal ) );      
      return comp;
   }
   
   /**
    * Add a group to the specified composite. A group is a composite with a 
    * labelled frame round it.
    * @param parent The parent composite to add the group to.
    * @param title
    * @return
    */
   public static Group createGroup( Composite parent, String title )
   {
      Group group = new Group( parent, getDebugBorder() );
      group.setText( title );
      return group;
   }
   
   /**
    * Alter a grid layout so that it has no spacing between controls, and no
    * margins.
    * @param layout The layout to alter.
    * @return The layout passed in.
    */
   public static Layout makeLayoutTight( Layout layout )
   {
      if( layout instanceof GridLayout ) {  
         GridLayout gridLayout = (GridLayout) layout;
         gridLayout.marginBottom = 0;
         gridLayout.marginHeight = 0;
         gridLayout.marginLeft = 0;
         gridLayout.marginRight = 0;
         gridLayout.marginTop = 0;
         gridLayout.marginWidth = 0;
         gridLayout.horizontalSpacing = 0;
         gridLayout.verticalSpacing = 0;
      }
      return layout;
   }
   
   /**
    * Removes the margins of the passed in GridLayout.
    * @param layout The layout to remove the margins of.
    * @return The passed in GridLayout
    */
   public static Layout removeLayoutMargins( Layout layout ) 
   {
      if( layout instanceof GridLayout ) {    
         GridLayout gridLayout = (GridLayout) layout;
         gridLayout.marginBottom = 0;
         gridLayout.marginHeight = 0;
         gridLayout.marginLeft = 0;
         gridLayout.marginRight = 0;
         gridLayout.marginTop = 0;
         gridLayout.marginWidth = 0;
      }
      return layout;
   }
   
   /**
    * Create a new GridLayout with default settings.
    * @param columns The number of columns in the grid
    * @param horSpacing The horizontal spacing between controls.
    * @param vertSpacing The vertical spacing between controls.
    * @return The new GridLayout.
    */
   public static GridLayout createGridLayout( int columns, int horSpacing, int vertSpacing )
   {
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = columns;
      if( horSpacing >= 0 ) gridLayout.horizontalSpacing = horSpacing;
      if( vertSpacing >= 0 ) gridLayout.verticalSpacing = vertSpacing;
      return gridLayout;
   }
   
  
   /**
    * Create a GridLayout with default settings and default spacing.
    * @param columns The number of columns in this grid layout.
    * @return The new GridLayout.
    */
   public static GridLayout createGridLayout( int columns )
   {
      return createGridLayout( columns, -1, -1 );
   }
   
   /**
    * Create a composite with a GridLayout already added to it. 
    * @param parent The parent control of the composite.
    * @param columns The number of columns in the GridLayout
    * @param horSpacing The horizontal spacing between controls.
    * @param vertSpacing The vertical spacing between controls.
    * @return The new composite.
    */
   public static Composite createGridLayoutComposite( Composite parent, int columns, int horSpacing, int vertSpacing   )
   {
      Composite comp = new Composite( parent, getDebugBorder() );
      comp.setLayout( createGridLayout( columns, horSpacing, vertSpacing ) );      
      return comp;
   }
   
   /**
    * Create a composite with a GridLayout already added to it.
    * @param parent The parent control of the composite.
    * @param columns The number of columns in the GridLayout.
    * @return The new composite.
    */
   public static Composite createGridLayoutComposite( Composite parent, int columns )
   {
      return createGridLayoutComposite( parent, columns, -1, -1 );
   }
   
   /**
    * Create a group with a GridLayout already added to it.
    * @param parent The parent control of the composite.
    * @param text The title of the group
    * @param columns The number of columns in the GridLayout.
    * @return The new group.
    */
   public static Group createGridLayoutGroup( Composite parent, String text, int columns )
   {
      Group group = new Group( parent, getDebugBorder() );
      group.setLayout( createGridLayout( columns, -1, -1 ) );    
      group.setText( text );
      return group;
   }
   
   /**
    * Set a empty control, which is useful for adding spaces to grid layouts.
    * @param parent The composite to add the space to.
    */
   public static Composite createBlankGrid( Composite parent ) 
   {
      return createBlankGrid( parent, 1, 1 );
   }
   
   /**
    * Set a empty control, which is useful for adding spaces to grid layouts.
    * @param parent The composite to add the space to.
    */
   public static Composite createBlankGrid( Composite parent, int width, int height ) 
   {
      Composite comp = new Composite( parent, getDebugBorder() );
      GridData data = new GridData();
      data.heightHint = height;
      data.widthHint = width;
      comp.setLayoutData( data );
      return comp;
   }
   
   /**
    * Create a blank composite that fills a row.
    * @param parent The composite to add the space to. MUST have a GridLayout.
    */
   public static Composite createBlankHorizontalFill( Composite parent, int height ) 
   {
      Composite comp = new Composite( parent, getDebugBorder() );
      GridLayout layout = (GridLayout) parent.getLayout();
      GridData data = new GridData();
      
      data.heightHint = height;
      data.grabExcessHorizontalSpace = true;
      data.horizontalSpan = layout.numColumns;
      
      comp.setLayoutData( data );
      return comp;
   }
   
   /**
    * Gets the grid data of a control, or creates a new one if the control has 
    * none.
    * @param control The control you want to get the GridData for.
    * @return The GridData of the control.
    */
   private static GridData getGridData( Control control )
   {
      if( control.getLayoutData() instanceof GridData ) {
         return (GridData) control.getLayoutData();
      } else {
         return new GridData();
      }
   }
   
   /**
    * Makes a control fill the horizontal free space in a GridLayout.
    * @param control The control you want to make fill horizontal space. 
    * @return The control that was passed in.
    */
   public static <C extends Control> C gridDataFillHorizontal( C control )
   {
      GridData gridData = getGridData( control );
      gridData.horizontalAlignment = GridData.FILL;
      gridData.grabExcessHorizontalSpace = true;
      control.setLayoutData( gridData );
   
      return control;
   }
   
   /**
    * Makes a control fill vertical the free space in a GridLayout.
    * @param control The control you want to make fill vertical space. 
    * @return The control that was passed in.
    */
   public static <C extends Control> C gridDataFillVertical( C control )
   {
      GridData gridData = getGridData( control );
      gridData.verticalAlignment = GridData.FILL;
      gridData.grabExcessVerticalSpace = true;
      control.setLayoutData( gridData );
   
      return control;
   }
   
   /**
    * Set the preferred width of the specified control. Control must be in a 
    * GridLayout for this method to have an effect.
    * @param control The control to set the preferred width for.
    * @param width The new preferred width.
    * @return The control that was passed in.
    */
   public static <C extends Control> C gridDataPreferredWidth( C control, int width )
   {
      GridData gridData = getGridData( control );
      gridData.widthHint = width;
      control.setLayoutData( gridData );
   
      return control;
   }
   
   /**
    * Set the preferred height of the specified control. Control must be in a 
    * GridLayout for this method to have an effect.
    * @param control The control to set the preferred height for.
    * @param height The new preferred height.
    * @return The control that was passed in.
    */
   public static <C extends Control> C gridDataPreferredHeight( C control, int height )
   {
      GridData gridData = getGridData( control );
      gridData.heightHint = height;
      control.setLayoutData( gridData );
   
      return control;
   }
   
   /**
    * Set the minimum width of the specified control. Control must be in a 
    * GridLayout for this method to have an effect.
    * @param control The control to set the minimum width for.
    * @param width The new minimum width.
    * @return The control that was passed in.
    */
   public static <C extends Control> C gridDataMinimumWidth( C control, int width )
   {
      GridData gridData = getGridData( control );
      gridData.minimumWidth = width;
      control.setLayoutData( gridData );
   
      return control;
   }
   
   /**
    * Set the minimum height of the specified control. Control must be in a 
    * GridLayout for this method to have an effect.
    * @param control The control to set the minimum height for.
    * @param width The new minimum height.
    * @return The control that was passed in.
    */
   public static <C extends Control> C gridDataMinimumHeight( C widget, int height )
   {
      GridData gridData = getGridData( widget );
      gridData.minimumHeight = height;
      widget.setLayoutData( gridData );
   
      return widget;
   }
   
   /**
    * Set the amount of columns this control takes up in a GridLayout.
    * @param control The control to alter the column span of.
    * @param span The amount of columns the control should span.
    * @return The control that was passed in.
    */
   public static <C extends Control> C gridDataColSpan( C control, int span )
   {
      GridData gridData = getGridData( control );
      gridData.horizontalSpan = span;
      control.setLayoutData( gridData );
   
      return control;
   }
   
   /**
    * Set the amount of rows this control takes up in a GridLayout.
    * @param control The control to alter the row span of.
    * @param span The amount of rows the control should span.
    * @return The control that was passed in.
    */
   public static <C extends Control> C gridDataRowSpan( C control, int span )
   {
      GridData gridData = getGridData( control );
      gridData.verticalSpan = span;
      control.setLayoutData( gridData );
   
      return control;
   }
   
   /**
    * Makes the passed control fill up the specified columns, filling the
    * all the available space as much as possible. Equivalent to:
    *    gridDataColSpan( control, span ); 
    *    gridDataFillHorizontal( control );
    * 
    * @param control The control to make fill the specified columns
    * @param span The amount of columns to fill.
    * @return The passed in control.
    */
   public static <C extends Control> C gridDataFillCols( C control, int span )
   {
      gridDataColSpan( control, span );
      gridDataFillHorizontal( control );
   
      return control;
   }
   
   /**
    * Create a simple Fill layout.
    * @param isHorziontal If true the layout will be horizontal, otherwise it 
    * will be vertical.
    * @return The created FillLayout.
    */
   public static FillLayout createFillLayout( boolean isHorziontal )
   {
      FillLayout fillLayout = new FillLayout();
      if( isHorziontal ) {
         fillLayout.type = SWT.HORIZONTAL;
      } else {
         fillLayout.type = SWT.VERTICAL;
      }      
      return fillLayout;
   }
   
   /**
    * Create a composite with a FillLayout already added to it.
    * @param parent The composite the new composite will be added to.
    * @param isHorziontal If true the layout will be horizontal, otherwise it 
    * will be vertical.
    * @return The new composite.
    */
   public static Composite createFillLayoutComposite( Composite parent, boolean isHorziontal )
   {
      Composite comp = new Composite( parent, getDebugBorder() );
      comp.setLayout( createFillLayout( isHorziontal ) );      
      return comp;
   }
   
   /**
    * Add a label to the specified composite.
    * @param parent The parent composite to add the label to.
    * @param text The text to put in the label
    * @param style The style of the label.
    * @return The new label.
    */
   public static Label addLabel( Composite parent, String text, int style ) 
   {
      Label label = new Label( parent, getDebugBorder() | SWT.WRAP | style );
      label.setText( text );
      return label;
   }
   
   /**
    * Add a label to the specified composite. This label will have a transparent 
    * background and no border (unless debugBorders is set).
    * @param parent The parent composite to add the label to.
    * @param text The text to put in the label
    * @return The new label.
    */
   public static Label addLabel( Composite parent, String text ) 
   {
      return addLabel( parent, text, 0 );
   }
   
   /**
    * Add a text box to the specified composite.
    * @param parent The parent composite to add the text box to.
    * @param content The initial text
    * @param textWidth The preferred width of the text box. If set to 
    * SWTUtil.FILL the box will fill available horizontal space.
    * @param style The style of the text box.
    * @return The new text control.
    */
   public static Text addText( Composite parent, String content, int textWidth, int style )
   {      
      Text text = new Text( parent, SWT.BORDER | style );
      text.setText( content );
      if( textWidth == FILL ) { 
         gridDataFillHorizontal( text ); 
      } else {
         gridDataPreferredWidth( text, textWidth );
      }
      
      return text;
   }
   
   /**
    * Add a single line text box to the specified composite.
    * @param parent The parent composite to add the text box to.
    * @param content The initial text.
    * @param textWidth The preferred width of the text box. If set to 
    * SWTUtil.FILL the box will fill available horizontal space.
    * @return The new single line text control.
    */
   public static Text addText( Composite parent, String content, int textWidth )
   {      
      return addText( parent, content, textWidth, SWT.SINGLE );
   }
   
   /**
    * Add a multi-line text box to the specified composite. Has scroll bars.
    * @param parent The parent composite to add the text box to.
    * @param content The initial text.
    * @param textWidth The preferred width of the text box. If set to 
    * SWTUtil.FILL the box will fill available horizontal space.
    * @return The new multi-line text control.
    */
   public static Text addMultiText( Composite parent, String content, int textWidth )
   {   
      return addText( parent, content, textWidth, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
   }
   
   /**
    * Add a label and a single line text box to the specified composite.
    * @param parent The parent composite to add both controls to.
    * @param labelText The text for the label.
    * @param textWidth The preferred width of the text box. If set to 
    * SWTUtil.FILL the box will fill available horizontal space.
    * @return A SWT set containing the label and text controls.
    */
   public static SWTSet addLabeledText( Composite parent, String labelText, int textWidth )
   {      
      Label label = addLabel( parent, labelText );
      Text text = addText( parent, "", textWidth );
      
      return new SWTSet( label, text );
   }
   
   /**
    * Add a password text box to the specified composite.
    * @param parent The parent composite to add the text box to.
    * @param textWidth The preferred width of the text box. If set to 
    * SWTUtil.FILL the box will fill available horizontal space.
    * @return The new password text control.
    */
   public static Text addPasswordText( Composite parent, int textWidth )
   {      
      return addText( parent, "", textWidth, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD );
   }
   
   /**
    * Add a label and a password line text box to the specified composite.
    * @param parent The parent composite to add both controls to.
    * @param labelText The text for the label.
    * @param textWidth The preferred width of the text box. If set to 
    * SWTUtil.FILL the box will fill available horizontal space.
    * @return A SWT set containing the label and password text controls.
    */
   public static SWTSet addLabeledPasswordText( Composite parent, String labelText, int textWidth )
   {      
      Label label = addLabel( parent, labelText );
      label.setText( labelText );
       
      return new SWTSet( label, addPasswordText( parent, textWidth ) );
   }
   
   /**
    * Add a radio button to the specified composite.
    * @param parent The parent composite to add the radio button to.
    * @param text The text label for the radio button. Note that the label is
    * part of the radio button an will only take up one space in a layout.
    * @param selected Should the radio button start out selected.
    * @return The new radio button control.
    */
   public static Button addRadioButton( Composite parent, String text, boolean selected )
   {
      Button button = new Button( parent, SWT.RADIO | getDebugBorder() );
      button.setText( text );
      button.setSelection( selected );
      
      return button;
   }
   
   /**
    * Add a check box to the specified composite.
    * @param parent The parent composite to add the check box to.
    * @param text The text label for the check box. Note that the label is
    * part of the check box an will only take up one space in a layout.
    * @param selected Should the check box start out selected.
    * @return The new check box control.
    */
   public static Button addCheckButton( Composite parent, String text, boolean selected )
   {
      Button button = new Button( parent, SWT.CHECK | getDebugBorder() );
      button.setText( text );
      button.setSelection( selected );
      
      return button;
   }
   
   /**
    * Add a standard pushable button to the control.
    * @param parent The parent composite to add the button to.
    * @param text The text on the button.
    * @return The new button control.
    */
   public static Button addButton( Composite parent, String text )
   {
      Button button = new Button( parent, SWT.PUSH | getDebugBorder() );
      button.setText( text );
      
      return button;
   }
   
   /**
    * Add a list control to the specified composite.
    * @param parent The parent composite to add the list to.
    * @param style The SWT style of the list.
    * @return The new list control.
    */
   public static List addList( Composite parent, int style )
   {
      List list = new List( parent, SWT.SINGLE | SWT.BORDER | style );
      return list;
   }
   
   /**
    * Add a list control to the specified composite.
    * @param parent The parent composite to add the list to.
    * @return The new list control.
    */
   public static List addList( Composite parent )
   {
      List list = new List( parent, SWT.SINGLE | SWT.BORDER );
      return list;
   }
   
   /**
    * Add a drop-down list (combo box) to the specified composite.
    * @param parent The parent composite to add the combo-box to.
    * @param options The selectable options for the combo-box. Each object's
    * toString() value is used as the selection label.
    * @return The new combo-box control.
    */
   public static Combo addCombo( Composite parent, Object ... options )
   {
      Combo combo = new Combo( parent, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER );
      
      for( Object option : options ) {
         combo.add( option.toString() );
      }
      
      return combo;
   }
   
   /**
    * Add a label and a combo-box to the specified composite.
    * @param parent The parent composite to add both the controls to.
    * @param labelText The text for the label.
    * @param options The selectable options for the combo-box. Each object's
    * toString() value is used as the selection label.
    * @return A set containing the new label and combo-box controls.
    */
   public static SWTSet addLabeledCombo( Composite parent, String labelText, Object ... options )
   {
      Label label = addLabel( parent, labelText );
      Combo combo = addCombo( parent, options );
      return new SWTSet( label, combo );
   }
   
   /**
    * Add a label that can contain selectable anchor tags. When these tags are
    * clicked on the specified actions are run.
    * @param parent The parent composite to add the link label to.
    * @param text The text for the label, containing anchor tags.
    * @param actions The actions to run when the anchor tags are clicked.
    * @return The new link label control.
    */
   public static Link addLinkLabel( Composite parent, String text, 
      Runnable ... actions )
   {
      Link link = new Link( parent, getDebugBorder() );
      link.setText( text );
      
      LinkListener listener = new LinkListener( text, actions );
      link.addSelectionListener( listener );
      
      return link;
   }
   
   /**
    * Adds a browse widget to the specified composite. It lets the user pick a 
    * directory using a  standard system dialog. This widget takes up 2 
    * spaces in a layout.
    * @param parent The parent composite to add the widget to.
    * @param buttonText The button text.
    * @param dialogTitle The title of the dialog that is displayed when the 
    * browse button is pressed.
    * @param textWidth The preferred width of the text box. If set to 
    * SWTUtil.FILL the box will fill available horizontal space.
    * @return A SWT set containing the text box and button controls.
    */
   public static SWTSet addBrowseWidget( Composite parent, String buttonText, String dialogTitle, int textWidth ) 
   { 
      Text text = new Text( parent, SWT.SINGLE | SWT.BORDER );
      if( textWidth == FILL ) { 
         gridDataFillHorizontal( text ); 
      } else {
         gridDataPreferredWidth( text, textWidth );
      }
      
      Button button = new Button( parent, SWT.PUSH );  
      button.setText( buttonText );   
      button.addSelectionListener( new BrowseDirectoryDisplay( parent, dialogTitle, null, text ) );
   
      return new SWTSet( text, button );
   }
   
   /**
    * Add a labelled browse widget. It lets the user pick a directory using a 
    * standard system dialog. This widget takes up 3 spaces in a layout.
    * @param parent The parent composite for the controls in this widget.
    * @param labelText The text for the label.
    * @param buttonText The text on the browse button
    * @param dialogTitle The title of the dialog when it is displayed
    * @param textWidth The preferred width of the text box. If set to 
    * SWTUtil.FILL the box will fill available horizontal space.
    * @return A SWT set containing the label, text box and button controls.
    */
   public static SWTSet addLabeledBrowseWidget( Composite parent,
      String labelText, String buttonText, String dialogTitle, int textWidth )
   { 
      Label label = new Label( parent, getDebugBorder() );
      label.setText( labelText );
      SWTSet set = addBrowseWidget( parent, buttonText, dialogTitle, textWidth ) ;
      
      return new SWTSet( set.text(), set.button(), label );
   }
   
   /**
    * Set a controls font to the specified style.
    * @param control The control to change the font of.
    * @param style The style of font
    */
   public static void fontStyle( Control control, int style )
   {
      Font font = control.getFont();
      if( font == null ) {
         return;
      }
      FontData[] fontDataArray = font.getFontData();
      if( fontDataArray == null || fontDataArray.length == 0 ) {
         return;
      }
      
      FontData data =  fontDataArray[0];   
      data.setStyle( style );
      ZDebug.print( 5, "Font data: ", data.getName(), " ", data.getStyle(), " ", data.height );
      
      FontListener.removeListener( control );
      control.setFont( new Font( font.getDevice(), data ) );
   }
   
   /**
    * Set a controls font to the specified style.
    * @param control The control to change the font of.
    * @param style The style of font
    */
   public static void fontName( Control control, String name )
   {
      Font font = control.getFont();
      if( font == null ) {
         return;
      }
      FontData[] fontDataArray = font.getFontData();
      if( fontDataArray == null || fontDataArray.length == 0 ) {
         return;
      }
      
      FontData data =  fontDataArray[0];   
      data.setName( name );
      ZDebug.print( 5, "Font data: ", data.getName(), " ",  data.getStyle(), " ", data.height );
      
      FontListener.removeListener( control );
      control.setFont( new Font( font.getDevice(), data ) );
   }
      
   /**
    * Set a control's font to one of Eclipse's built in fonts.
    * @param control The control to set the font of.
    * @param pref The preference that stores the font.
    */
   public static void fontPreference( Control control, ExternalPreference pref )
   {
      if( pref.getSource() != Source.FONT_REGISTRY ) {
         ZDebug.dumpStackTrace( "Tried to set font using preference of: ", pref );
         return;
      }
      
      ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
      control.setFont( theme.getFontRegistry().get( pref.getKey() ) );
      new FontListener( control, pref, theme.getFontRegistry() );
   }
   
   /** 
    * SWT stuff can only be altered in the display thread. The passed runnable
    * is executed on this thread, allowing you to do SWT stuff from another
    * thread. The current thread waits for the SWT function to finish.
    * @param run The function to run in the SWT thread.
    */
   public static void exec( Runnable run )
   {
      if( !ZXTMPlugin.isEclipseLoaded() ) 
      {
         System.err.println( "Cannot SWT sync exec, workspace not ready" );
         Thread.dumpStack();
         return;
      }
      
      // Hey we're on the display thread already! Just run the command
      if( Display.getCurrent() != null ) {
         run.run();
      } else {      
         ZXTMPlugin.getDefault().getWorkbench().getDisplay().syncExec( run );
      }
   }
   
   /** 
    * SWT stuff can only be altered in the display thread. The passed runnable
    * is executed on this thread, allowing you to do SWT stuff from another
    * thread. This function returns instantly (does not wait for completion)
    * @param run The function to run in the SWT thread.
    */
   public static void asyncExec( Runnable run )
   {
      if( !ZXTMPlugin.isEclipseLoaded() ) 
      {
         System.err.println( "Cannot SWT async exec, workspace not ready" );
         Thread.dumpStack();
         return;
      }
      
      ZXTMPlugin.getDefault().getWorkbench().getDisplay().asyncExec( run );   
   }
   
   /**
    * Run a progress operation, showing a busy cursor whilst it runs.
    * @param op The operation to run.
    * @throws InvocationTargetException If the operation failed and threw an
    * exception.
    */
   public static void progressBusyCursor( IRunnableWithProgress op ) throws InvocationTargetException
   {
      if( !ZXTMPlugin.isEclipseLoaded() ) 
      {
         System.err.println( "Cannot run operation, workspace not ready" );
         Thread.dumpStack();
         return;
      }
      
      IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

      try {
         progressService.busyCursorWhile( op );
      } catch( InvocationTargetException e ) {
         throw e;
      } catch( Exception e ) {
         ZDebug.printStackTrace( e, "Busy cursor failed - ", op );
      }         
    
   }
      
   /**
    * Run a progress operation in the background.
    * @param op The operation to run.
    * @param wait Should the method block whilst the job is run?
    * @throws InvocationTargetException If the operation failed and threw an
    * exception.
    */
   public static void progressBackground( IRunnableWithProgress op, boolean wait ) throws InvocationTargetException
   {
      if( !ZXTMPlugin.isEclipseLoaded() ) 
      {
         System.err.println( "Cannot run operation, workspace not ready" );
         Thread.dumpStack();
         return;
      }
      
      BackgroundJob job = new BackgroundJob( op );
      job.schedule();
      
      if( wait ) {
        if( Job.getJobManager().currentJob() != null ) {
           ZDebug.dumpStackTrace( "Background job is trying to be run from another job and blocking" );
        } else {
           try { job.join(); } catch (Exception e) {}
        }
      }
   }
      
   /**
    * Run a progress operation, showing a progress dialog whilst it runs.
    * @param op The operation to run.
    * @throws InvocationTargetException If the operation failed and threw an
    * exception.
    */
   public static void progressDialog( WorkspaceModifyOperation op ) throws InvocationTargetException
   {
      if( !ZXTMPlugin.isEclipseLoaded() ) 
      {
         ZDebug.dumpStackTrace( "Cannot run operation, workspace not ready" );
         return;
      }
      
      IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

      try {
         progressService.runInUI( progressService, op, op.getRule() );
      } catch( InvocationTargetException e ) {
         throw e;
      } catch( Exception e ) {
         ZDebug.printStackTrace( e, "Progress Dialog failed - ", op );
      }         
   }
      
   /**
    * Return true if the Eclipse window is in focus.
    * @return
    */
   public static boolean isWindowFocused()
   {
      if( !ZXTMPlugin.isEclipseLoaded() ) return false;
      FocusChecker checker = new FocusChecker();
      SWTUtil.exec( checker );
      return checker.focused;
   }
   
   /**
    * Listener for the browse widget button. Displays the platform standard 
    * browse dialog, and updates the widgets text box when complete.
    */
   public static class BrowseDirectoryDisplay extends SelectionAdapter
   {
      private Text text;
      private DirectoryDialog directoryDialog;
      
      /**
       * Create the browse dialog listener for the specified widget.
       * @param parent The parent composite of the widget.
       * @param title The dialog title
       * @param initialPath The initial path of the dialog
       * @param text The text box to update when the dialog finishes.
       */
      public BrowseDirectoryDisplay( Composite parent, String title, String initialPath, Text text )
      {
         directoryDialog = new DirectoryDialog( parent.getShell() ); 
         directoryDialog.setText( title ); 
         directoryDialog.setFilterPath( initialPath ); 
   
         this.text = text;
      }

      /**
       * Callback when the Browse... button is selected.
       */
      /* Override */
      public void widgetSelected( SelectionEvent e )
      {
         String selectedDirectory = directoryDialog.open();
         if( selectedDirectory != null ) {
            text.setText( selectedDirectory );
         }
      }      
   }
   
   /**
    * A workbench job that can run in the background. 
    */
   private static class BackgroundJob extends Job
   {
      private IRunnableWithProgress op;
      private InvocationTargetException e;
      
      /**
       * Wrap the job round a IRunnableWithProgress class.
       * @param op The operation to run in this job
       */
      private BackgroundJob( IRunnableWithProgress op )
      {
         super( op.toString() );
         this.op = op;
         this.setSystem( true );
         if( op instanceof WorkspaceModifyOperation ) {
            this.setRule( ((WorkspaceModifyOperation) op).getRule() );
         }
      }

      /**
       * Runs the wrapped operation. 
       */
      /* Override */
      protected IStatus run( IProgressMonitor monitor )
      {
         try {
            op.run( monitor );
            return new Status( IStatus.OK, Ids.PLUGIN, op.toString() + " successful" );
         } catch( InvocationTargetException e ) {
            this.e = e;
            return new Status( IStatus.OK, Ids.PLUGIN, op.toString() + " failed" );
         } catch( Exception e ) {
            ZDebug.printStackTrace( e, "Run op in background failed - ", op );
            return new Status( IStatus.ERROR, Ids.PLUGIN, op.toString() + " failed", e );
         }
      }
      
      public InvocationTargetException getException()
      {
         return e;
      }
      
   }
   
   /**
    * Runnable that checks if the current shell is in focus.
    */
   private static class FocusChecker implements Runnable
   {
      public boolean focused;
      public void run() 
      {
         focused = ZXTMPlugin.getDefault().getWorkbench().getDisplay().getActiveShell() != null;
      }
   }
   
   /**
    * Class that listens for changes to a font in Eclipse, and updates the 
    * control's font. 
    */
   private static class FontListener implements Runnable, IPropertyChangeListener
   {
      private Control control;
      private ExternalPreference fontPref;
      private FontRegistry registry;
      private static HashMap<Control,FontListener> currentListeners = 
         new HashMap<Control,FontListener>();
      
      /**
       * Create the FontListener. Starts listening to the font registry 
       * provided. It the control already has a FontListener, it is disposed.
       * @param control The control who's font this class will update.
       * @param fontPref The preference this class will update.
       * @param registry The registry to get font preferences from.
       */
      private FontListener( Control control, ExternalPreference fontPref,
         FontRegistry registry )
      {
         this.control = control;
         this.fontPref = fontPref;
         this.registry = registry;
         
         registry.addListener( this );
         
         
         removeListener( control );
         currentListeners.put( control, this );
      }
      
      public static void removeListener( Control control )
      {
         if( currentListeners.containsKey( control ) ) {
            currentListeners.get( control ).dispose();
            currentListeners.remove( control );
         }
      }
      
      /** Remove this class as a listener */
      private void dispose()
      {
         registry.removeListener( this );
      }

      /** Property has changed, is it ours? */
      /* Override */
      public void propertyChange( PropertyChangeEvent event )
      {
         if( control.isDisposed() ) {
            dispose();
            return;
         }
         if( event.getProperty().equals( fontPref.getKey() ) );
         SWTUtil.exec( this );         
      }

      /** Run using SWTUtil.exec(), updates the controls font. */
      /* Override */
      public void run()
      {
         control.setFont( registry.get( fontPref.getKey() ) );
         control.getParent().pack();
      }
      
   }
   
   
}
