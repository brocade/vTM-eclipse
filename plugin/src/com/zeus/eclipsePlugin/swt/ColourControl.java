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

import java.util.ArrayList;
import java.util.EnumMap;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Preference;

/**
 * Control used to edit colour/style settings of colour preferences.
 */
public class ColourControl
{
   private Composite composite;
   private List elementList;   
   private Button checkDefault, checkBold, checkItalic, checkStrikethrough, 
                  checkUnderline;   
   private ColorSelector buttonColour;
   
   // Colour settings wrap around a preference and manage updating the 
   // preference store.
   private ArrayList<ColourSetting> colourSettings = new ArrayList<ColourSetting>();   
   
   // A table that maps preferences to colour settings
   private EnumMap<Preference,ColourSetting> prefMap = 
      new EnumMap<Preference, ColourSetting>( Preference.class );
   
   private SWTSet settingSet;
   
   private ColourListener colourListener = new ColourListener();
   private TrafficScriptPreview preview;

   /**
    * Creates the control in the specified composite. Adds the preferences to
    * the list of styles to edit.
    * @param parent The parent composite to put this control in
    * @param preferences The preferences this control will edit.
    */
   public ColourControl( Composite parent, Preference ... preferences )
   {
      composite = SWTUtil.createGridLayoutComposite( parent, 2 );
      SWTUtil.removeLayoutMargins( (GridLayout) composite.getLayout() );
      
      SWTUtil.gridDataFillHorizontal( composite );
      
      // List of colour preferences
      SWTUtil.addLabel( composite, ZLang.ZL_ElementLabel );
      SWTUtil.createBlankGrid( composite );
      
      elementList = SWTUtil.addList( composite, SWT.V_SCROLL );
      SWTUtil.gridDataPreferredHeight( elementList, 180 );
      elementList.addSelectionListener( colourListener );
      
      for( Preference pref : preferences ) {
         elementList.add( pref.getText() );
         ColourSetting setting = new ColourSetting( pref );
         colourSettings.add( setting );
         prefMap.put( pref, setting );
      }
      elementList.setSelection( 0 );
      SWTUtil.gridDataFillHorizontal( elementList );
      
      // Colour options
      Composite colourOptions = SWTUtil.createGridLayoutComposite( composite, 2 );
      
      checkDefault = SWTUtil.addCheckButton( colourOptions, ZLang.ZL_UseDefault, true );
      SWTUtil.gridDataColSpan( checkDefault, 2 );
      checkDefault.addSelectionListener( colourListener );
      
      SWTUtil.createBlankGrid( colourOptions, 10, 1 );
      Composite colourComposite = SWTUtil.createGridLayoutComposite( colourOptions, 2 );
      SWTUtil.removeLayoutMargins( (GridLayout) colourComposite.getLayout() );
      Label colourButtonLabel = SWTUtil.addLabel( colourComposite, ZLang.ZL_ColourLabel );
      buttonColour = new ColorSelector( colourComposite );
      buttonColour.addListener( colourListener );
      
      SWTUtil.createBlankGrid( colourOptions, 10, 1 );
      checkBold = SWTUtil.addCheckButton( colourOptions, ZLang.ZL_Bold, false );
      checkBold.addSelectionListener( colourListener );
      
      SWTUtil.createBlankGrid( colourOptions, 10, 1 );
      checkItalic = SWTUtil.addCheckButton( colourOptions, ZLang.ZL_Italic, false );
      checkItalic.addSelectionListener( colourListener );
      
      SWTUtil.createBlankGrid( colourOptions, 10, 1 );
      checkStrikethrough = SWTUtil.addCheckButton( colourOptions, ZLang.ZL_Strikethrough, false );
      checkStrikethrough.addSelectionListener( colourListener );
      
      SWTUtil.createBlankGrid( colourOptions, 10, 1 );
      checkUnderline = SWTUtil.addCheckButton( colourOptions, ZLang.ZL_Underline, false );
      checkUnderline.addSelectionListener( colourListener );
      
      settingSet = new SWTSet( 
         buttonColour.getButton(), checkBold, checkItalic, checkStrikethrough, 
         checkUnderline, colourButtonLabel
      );
      
      // Preview
      Label previewLabel = SWTUtil.addLabel( composite, ZLang.ZL_PreviewLabel );
      SWTUtil.gridDataColSpan( previewLabel, 2 );
      
      preview = new TrafficScriptPreview( composite );
           
      colourListener.showSelectedProperty();
      
      composite.pack();
      composite.layout();      
   }
   
   /**
    * Resets the preferences to their default values in the UI (doesn't change
    * the preferences stored values)
    */
   public void resetToDefault()
   {
      ZDebug.print( 3, "resetToDefault()" );
      for( ColourSetting setting : colourSettings ) {
         ZDebug.print( 5, "Resetting: ", setting.getPreference() );
         setting.resetToDefault();
      }
      
      colourListener.showSelectedProperty();
   }
   
   /**
    * Append an area of text to the preview section. The text will be updated
    * with the passed preferences style settings when the preference is altered
    * by this control. 
    * @param text The text to style with the preference
    * @param pref The colour preference to style the text.
    */
   public void addPreviewSection( String text, Preference pref )
   {
      ColourSetting setting = prefMap.get( pref );
      preview.addSection( text, setting );
   }
   
   /**
    * Update the preview with the current preference style settings.
    */
   public void updatePreview()
   {
      preview.update();
   }
   
   /**
    * Commits the settings in the UI to the preference store.
    */
   public void updatePreferences()
   {
      for( ColourSetting setting : colourSettings ) {
         Preference pref = setting.getPreference();         
         IPreferenceStore preferences = ZXTMPlugin.getDefault().getPreferenceStore();
         
         if( setting.isDefault() ) {
            preferences.setToDefault( pref.getKey() );
         } else {
            preferences.setValue( pref.getKey(), setting.toPreferenceString() );
         }         
      }
   }
   
   /**
    * Listener that updates the various sub widgets when something is clicked by
    * the user.
    */
   class ColourListener implements SelectionListener, IPropertyChangeListener
   {
      /**
       * If the user changes the selection in the preference list, this method 
       * is called. It changes the style editing widgets to reflect the new
       * style.
       */
      public void showSelectedProperty()
      {
         if( elementList.getSelectionIndex() == -1) return;
         ColourSetting setting = colourSettings.get( elementList.getSelectionIndex() );
         if( setting == null ) return;
         
         buttonColour.setColorValue( setting.getColour() );
         checkDefault.setSelection( setting.isDefault() );
         checkBold.setSelection( setting.isBold() );
         checkItalic.setSelection( setting.isItalic() );
         checkStrikethrough.setSelection( setting.isStrikethrough() );
         checkUnderline.setSelection( setting.isUnderline() );
         
         settingSet.setEnabled( !setting.isDefault() );
         preview.update();
      }
      
      /**
       * If the style editing widgets are changed, updates the colour 
       * setting object to the new values.
       */
      public void updateSelectedProperty()
      {
         if( elementList.getSelectionIndex() == -1) return;
         ColourSetting setting = colourSettings.get( elementList.getSelectionIndex() );
         if( setting == null ) return;
         
         setting.setDefault( checkDefault.getSelection() );
         
         setting.setColour( buttonColour.getColorValue() );
         setting.setBold( checkBold.getSelection() );
         setting.setItalic( checkItalic.getSelection() );
         setting.setStrikethrough( checkStrikethrough.getSelection() );
         setting.setUnderline( checkUnderline.getSelection() );
         
         settingSet.setEnabled( !checkDefault.getSelection() );
         preview.update();
      }
      
      /* Override */
      public void widgetDefaultSelected( SelectionEvent e )
      {
         widgetSelected( e );
      }

      /* Override */
      public void widgetSelected( SelectionEvent e )
      {
         if( e.widget == elementList ) {
            showSelectedProperty();
         } else {
            updateSelectedProperty();
         }
      }

      /* Override */
      public void propertyChange( PropertyChangeEvent event )
      {
         updateSelectedProperty();
      }

   }  
}
