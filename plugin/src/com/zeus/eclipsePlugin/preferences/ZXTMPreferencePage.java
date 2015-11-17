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

package com.zeus.eclipsePlugin.preferences;

import java.util.LinkedList;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.consts.Preference.Format;
import com.zeus.eclipsePlugin.swt.ColourControl;
import com.zeus.eclipsePlugin.swt.SWTSet;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * This is the super class for all global preference pages in this plug-in.
 * Controls can easily be added for a particular preference using
 * addControlForPreference(), and this class will take care of the rest.
 */
public abstract class ZXTMPreferencePage extends PreferencePage implements
   IWorkbenchPreferencePage
{
   private LinkedList<PreferenceController> controllers = 
      new LinkedList<PreferenceController>();
   private ColourControl colourControl;
   
   /**
    * Add a control to modify the values of a preference key. This must be 
    * inserted into a composite with a GridLayout with 3 columns.
    * @param pref The preference this control will edit.
    * @param parent The parent composite this preference will be inserted into.
    */
   protected SWTSet addControlForPreference( Preference pref, Composite parent ) 
   {
      Text text = null;
      Button button = null;
      SWTSet set = null;
      
      switch( pref.getFormat() ) {
         case INT: case POSITIVE_INT: case STRING: case MILISECONDS: {
            set = SWTUtil.addLabeledText( 
               parent, pref.getText() + ": ", SWTUtil.FILL 
            );
            text = set.text();
            SWTUtil.gridDataPreferredWidth( text, 150 );
            
            text.setText( getPreferenceStore().getString( pref.getKey() ) );
            break;
         }
         
         case STRING_LIST: {
            Label label = SWTUtil.addLabel( parent, pref.getText() + ": " );
            SWTUtil.gridDataColSpan( label, 3 );
            
            text = SWTUtil.addMultiText( parent, 
               getPreferenceStore().getString( pref.getKey() ), SWTUtil.FILL 
            );
            SWTUtil.gridDataPreferredWidth( text, 150 );
            SWTUtil.gridDataPreferredHeight( text, 100 );
            
            set = new SWTSet( label, text );
            break;
         }
         
         case BOOLEAN: {
            button = SWTUtil.addCheckButton( 
               parent, pref.getText(), 
               getPreferenceStore().getBoolean( pref.getKey() )
            );
            
            set = new SWTSet( button );
            break;
         }
         
         default: {
            throw new RuntimeException( 
               "No code for preference format: " + pref.getFormat() 
            );
         }
      }
           
      switch( pref.getFormat() ) {
         case INT: case POSITIVE_INT: case STRING:  {
            SWTUtil.gridDataColSpan( text, 2 );
            break;
         }
         
         case MILISECONDS: {
            SWTUtil.gridDataMinimumWidth( text, 20 );
            Label label = SWTUtil.addLabel( parent, ZLang.ZL_Miliseconds );
            set = new SWTSet( set.item( 0 ), set.item( 1 ), label );
            break;
         }
         
         case BOOLEAN: {
            SWTUtil.gridDataColSpan( button, 3 );
            break;
         }
         
         case STRING_LIST: {
            SWTUtil.gridDataColSpan( text, 3 );
            break;
         }
            
         default: {
            throw new RuntimeException( 
               "No code for preference format: " + pref.getFormat() 
            );
         }
      }
      
      PreferenceController controller = null;
      if( text != null ) {
         controller = new PreferenceController( pref, text );      
         text.addModifyListener( controller );
         
      } else if( button != null ) {
         controller = new PreferenceController( pref, button );      
         button.addSelectionListener( controller );
      }
      
      controllers.add( controller );
      
      return set;
   }
   
   /**
    * Create a colour control for the supplied preferences.
    * @param parent The composite to put the control in.
    * @param preferences The preferences this control will alter. Must be colour
    * preferences.
    * @return The created colour control.
    */
   public ColourControl createColourControl( Composite parent, Preference ... preferences )
   {
      colourControl = new ColourControl( parent, preferences );
      return colourControl;
   }
   
   /**
    * Returns the main plug-in preference store.
    */
   /* Override */
   public IPreferenceStore getPreferenceStore()
   {
      return ZXTMPlugin.getDefault().getPreferenceStore();
   }
   
   /**
    * Cycle through all the preference controllers and check that they have 
    * errors. This should be updated if a control on the page changes.
    */
   private void updateErrors()
   {
      for( PreferenceController controller : controllers ) {
         if( !controller.isValid() ) {
            setErrorMessage( controller.getError() );
            setValid( false );
            return;
         }
      }
      
      setErrorMessage( null );
      setValid( true );
   }
   
   /**
    * Check if any of the values on the page are valid.
    * @return True if the page has invalid data on it.
    */
   protected boolean isInvalid()
   {
      for( PreferenceController controller : controllers ) {
         if( !controller.isValid() ) return true;
      }
      
      return false;
   }
   
   /**
    * Reset all the preferences to their defaults.
    */
   /* Override */
   protected void performDefaults()
   {
      for( PreferenceController controller : controllers ) {
         controller.restoreDefault();
      }
      
      if( colourControl != null ) {
         colourControl.resetToDefault();
      }
      
      super.performDefaults();
   }

   /**
    * Update the preference store with the values currently in the UI.
    */
   /* Override */
   public boolean performOk()
   {
      if( isInvalid() || !super.performOk() ) return false;
      
      for( PreferenceController controller : controllers ) {
         try {
            controller.updatePreference();
         } catch( Exception e ) {
            ZDebug.printStackTrace( e, "Update preference from UI failed" );
         }
      }
      
      if( colourControl != null ) {
         colourControl.updatePreferences();
      }
      
      return true;
   }
   
   /** Not implemented. Children should override if they need to. */
   /* Override */ public void init( IWorkbench workbench ) {}
  
   /**
    * Class that monitors a control and updates error messages and values 
    * accordingly.
    */
   protected class PreferenceController implements ModifyListener, SelectionListener 
   {
      private Preference pref;
      private Text controlText = null;
      private Button controlButton = null;
      
      private String error = null;
      private int valueInt;
      private String valueString;
      private boolean valueBoolean;

      /**
       * Create a preference controller for the a text box.
       * @param preference The preference the text box edits.
       * @param controlText The text box that edits the preference.
       */
      private PreferenceController( Preference preference, Text controlText )
      {
         ZDebug.print( 3, "PreferenceController( ", preference, ", ", controlText, " )" );
         
         this.pref = preference;
         this.controlText = controlText;
         
         switch( pref.getFormat() ) {
            case INT: case POSITIVE_INT: case MILISECONDS: {
               valueInt = getPreferenceStore().getInt( pref.getKey() );
               valueString = "" + valueInt;
               break;
            }
            case STRING: case STRING_LIST: {
               valueString = getPreferenceStore().getString( pref.getKey() );
               break;
            }
            
            default: {
               throw new RuntimeException( 
                  "No code for preference format: " + pref.getFormat() 
               );    
            }
         }
      }

      /**
       * Add a preference controller for a button (most likely a check box)
       * @param pref The preference this controller manages.
       * @param button The button that alters the preference.
       */
      public PreferenceController( Preference pref, Button button )
      {
         this.pref = pref;
         this.controlButton = button;
         
         switch( pref.getFormat() ) {
            case BOOLEAN: {
               valueBoolean = getPreferenceStore().getBoolean( pref.getKey() );
               valueString = "" + valueBoolean;
               break;
            }
            
            default: {
               throw new RuntimeException( 
                  "No code for preference format: " + pref.getFormat() 
               );    
            }
         }
      }

      /**
       * Listener event that is called when a text box is changed.
       */
      /* Override */
      public void modifyText( ModifyEvent event )
      {
         ZDebug.print( 4, "modifyText( ", event, " ) - ", pref );
         
         valueString = controlText.getText();
         ZDebug.print( 5, "New text is: ", valueString );
         
         error = null;
         
         switch( pref.getFormat() ) {
            case INT: case POSITIVE_INT: case MILISECONDS: {
               try {
                  valueInt = Integer.parseInt( valueString );
               
                  if( pref.getFormat() == Format.INT ) break;
                  
                  if( valueInt < 0 ) {
                     setError( ZLang.ZL_ValidationMustBePositive );
                     valueInt = 0;
                     break;
                  }
               } catch( NumberFormatException e ) {
                  setError( ZLang.ZL_ValidationNotAnInt );
               }
            }
            
            case STRING: {
               Pattern pattern = pref.getPattern();
               if( pattern != null && !pattern.matcher( valueString ).matches() ) {
                  setError( pref.getErrorText() );
                  valueString = "";                 
               }
               break;
            }
            
            case STRING_LIST: {
               String[] values = valueString.split( "[,\\s]+" );
               Pattern pattern = pref.getPattern();
               
               if( values.length == 1 && values[0].trim().length() == 0 ) break;
               
               for( String value : values ) {
                  ZDebug.print( 6, "Checking value: '", value, "'" );
                  if( pattern != null && !pattern.matcher( value ).matches() ) {
                     setError( pref.getErrorText() );
                     valueString = "";
                     break;
                  }
               }
               break;
            }
            
         }         
         
         ZDebug.print( 5, "New error is: ", error );
         updateErrors();
      }
      
      /**
       * Listener event for the button/check boxes.
       */
      /* Override */
      public void widgetSelected( SelectionEvent event )
      {
         ZDebug.print( 4, "widgetSelected( ", event, " ) - ", pref );
         valueString = "" + controlButton.getSelection();;
         
         switch( pref.getFormat() ) {
            case BOOLEAN: {
               valueBoolean = controlButton.getSelection();
            }               
         }
         
         updateErrors();
      }
      
      /**
       * Listener event for the button/check boxes.
       */
      /* Override */
      public void widgetDefaultSelected( SelectionEvent e )
      {
         widgetSelected( e );
      }
      
      /**
       * Set a validation error for this preference.
       * @param errorText The error text, should come from the preference.
       */
      private void setError( String errorText )
      {
         error = ZLang.bind( errorText, pref.getText() );
      }

      /**
       * Is the value of the preference control valid?
       * @return True if the preference is ready to be submitted, false 
       * otherwise.
       */
      public boolean isValid()
      {
         return getError() == null;
      }
      
      /**
       * Get the pretty description of the current error.
       * @return The current error in user readable format, or null if there is 
       * no problem.
       */
      public String getError()
      {
         return error;
      }
      
      /**
       * Changes the controls value to the preferences default.
       */
      public void restoreDefault()
      {
         if( controlText != null ) {
            controlText.setText( 
               getPreferenceStore().getDefaultString( pref.getKey() ) 
            );
         }
         
         if( controlButton != null ) {
            controlButton.setSelection( 
               getPreferenceStore().getDefaultBoolean( pref.getKey() ) 
            );
         }
      }
      
      /**
       * Update the preference using the controls current value.
       */
      public void updatePreference()
      {
         ZDebug.print( 4, "Setting preference for ", pref, " = ", valueString );
         
         switch( pref.getFormat() ) {
            case INT: case POSITIVE_INT: case MILISECONDS: {
               getPreferenceStore().setValue( pref.getKey(), valueInt );
               break;
            }
            
            case STRING: case STRING_LIST: {
               getPreferenceStore().setValue( pref.getKey(), valueString );
               break;
            }
            
            case BOOLEAN: {
               getPreferenceStore().setValue( pref.getKey(), valueBoolean );
               break;
            }
            
            default: {
               throw new RuntimeException( 
                  "No code for preference format: " + pref.getFormat() 
               );    
            }
         }
         
      }
   }
   
}
