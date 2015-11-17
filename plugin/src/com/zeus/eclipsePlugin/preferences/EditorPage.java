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

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.zeus.eclipsePlugin.PreferenceManager;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.consts.ExternalPreference;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.swt.ColourControl;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * The main TrafficScript editor page. Contains a link to the general editor 
 * page and options for code colouring.
 */
public class EditorPage extends ZXTMPreferencePage
{
   /**
    * Create the contents of this page. Uses the ColourControl to edit 
    * colour preferences.
    */
   /* Override */
   protected Control createContents( Composite parent )
   {
      ZDebug.print( 4, "createControl( ", parent, " )" );
      Composite mainComposite = SWTUtil.createGridLayoutComposite( parent, 1 );
      SWTUtil.gridDataFillHorizontal( mainComposite );
      SWTUtil.removeLayoutMargins( (GridLayout) mainComposite.getLayout() );
      
      SWTUtil.addLinkLabel( mainComposite, 
         ZLang.ZL_EditorLinkToGeneralTextEditor, 
         new OpenPageRunnable( parent.getShell(), 
            "org.eclipse.ui.preferencePages.GeneralTextEditor"
         ) 
      );
      
      Group group = SWTUtil.createGridLayoutGroup( mainComposite, ZLang.ZL_EditorSyntaxColouring, 1 );
      SWTUtil.gridDataFillHorizontal( group );
      
      ColourControl colourControl = createColourControl( group,
         Preference.COLOUR_DEFAULT,
         Preference.COLOUR_KEYWORD, 
         Preference.COLOUR_FUNCTION,
         Preference.COLOUR_DEPRECATED,
         Preference.COLOUR_VARIABLE,
         Preference.COLOUR_NUMBER,
         Preference.COLOUR_STRING,
         Preference.COLOUR_ESCAPE,
         Preference.COLOUR_COMMENT,
         Preference.COLOUR_TASK
      );
      
      int tabSize = PreferenceManager.getExternalPreferenceInt( ExternalPreference.EDITOR_TAB_WIDTH );
      String tab = "";
      for( int i = 0; i < tabSize; i++ ) tab += " ";
      
      // Create the preview section.
      colourControl.addPreviewSection( "# " + ZLang.ZL_EditorPreviewComment, Preference.COLOUR_COMMENT );
      colourControl.addPreviewSection( "\n", Preference.COLOUR_DEFAULT );
      colourControl.addPreviewSection( "# ", Preference.COLOUR_COMMENT );
      colourControl.addPreviewSection( ZLang.ZL_TODO, Preference.COLOUR_TASK );
      colourControl.addPreviewSection( " " + ZLang.ZL_EditorPreviewTODOComment, Preference.COLOUR_COMMENT );
      colourControl.addPreviewSection( "\n", Preference.COLOUR_DEFAULT );
      colourControl.addPreviewSection( "$page", Preference.COLOUR_VARIABLE );
      colourControl.addPreviewSection( " = ", Preference.COLOUR_DEFAULT );
      colourControl.addPreviewSection( "http.getBody", Preference.COLOUR_FUNCTION );
      colourControl.addPreviewSection( "();\n", Preference.COLOUR_DEFAULT );
      colourControl.addPreviewSection( "if", Preference.COLOUR_KEYWORD );
      colourControl.addPreviewSection( "( ", Preference.COLOUR_DEFAULT );
      colourControl.addPreviewSection( "string.count", Preference.COLOUR_FUNCTION );
      colourControl.addPreviewSection( "( ", Preference.COLOUR_DEFAULT );      
      colourControl.addPreviewSection( "$page", Preference.COLOUR_VARIABLE );
      colourControl.addPreviewSection( ", ", Preference.COLOUR_DEFAULT );      
      colourControl.addPreviewSection( "\"NEEDLE ", Preference.COLOUR_STRING );
      colourControl.addPreviewSection( "\\r\\n", Preference.COLOUR_ESCAPE );
      colourControl.addPreviewSection( "\"", Preference.COLOUR_STRING );
      colourControl.addPreviewSection( ", ", Preference.COLOUR_DEFAULT );
      colourControl.addPreviewSection( "10", Preference.COLOUR_NUMBER );
      colourControl.addPreviewSection( " ) > ", Preference.COLOUR_DEFAULT );
      colourControl.addPreviewSection( "0", Preference.COLOUR_NUMBER );
      colourControl.addPreviewSection( " ) {\n" + tab, Preference.COLOUR_DEFAULT );      
      colourControl.addPreviewSection( "string.IreplaceAll", Preference.COLOUR_DEPRECATED );
      colourControl.addPreviewSection( "( ", Preference.COLOUR_DEFAULT );
      colourControl.addPreviewSection( "$page", Preference.COLOUR_VARIABLE );
      colourControl.addPreviewSection( ", ", Preference.COLOUR_DEFAULT );    
      colourControl.addPreviewSection( "\"NEEDLE\"", Preference.COLOUR_STRING );
      colourControl.addPreviewSection( ", ", Preference.COLOUR_DEFAULT );
      colourControl.addPreviewSection( "\"HELLO WORLD\"", Preference.COLOUR_STRING );
      colourControl.addPreviewSection( " );\n}\n", Preference.COLOUR_DEFAULT );
            
      colourControl.updatePreview();
      
      mainComposite.pack();
      mainComposite.layout();
      
      return mainComposite;
   }
   
   /**
    * Runnable that opens a preference page.
    */
   private class OpenPageRunnable implements Runnable
   {
      private Shell shell;
      private String target;
      
      /**
       * Create an open page runnable that opens the passed in target.
       * @param shell The shell to open the preference dialog in.
       * @param target The target to open.
       */
      private OpenPageRunnable( Shell shell, String target )
      {
         this.shell = shell;
         this.target = target;
      }

      /**
       * Opens a preference page in the current preference dialog.
       */
      /* Override */
      public void run()
      {
         PreferencesUtil.createPreferenceDialogOn( shell, target, null, null );         
      }
      
   }

}
