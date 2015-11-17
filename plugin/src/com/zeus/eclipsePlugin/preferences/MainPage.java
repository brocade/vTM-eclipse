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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.swt.SWTEnableListener;
import com.zeus.eclipsePlugin.swt.SWTSet;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * The primary preference page for the ZXTM plug-in. All other pages are 
 * children of this page. It contains settings for the SOAP updater, and plug-in
 * debugging. 
 */
public class MainPage extends ZXTMPreferencePage
{
   /**
    * Create the preference controls. 
    */
   /* Override */
   protected Control createContents( Composite parent )
   {
      ZDebug.print( 4, "createControl( ", parent, " )" );
      Composite mainComposite = SWTUtil.createGridLayoutComposite( parent, 1 );
      SWTUtil.gridDataFillHorizontal( mainComposite );
      SWTUtil.removeLayoutMargins( (GridLayout) mainComposite.getLayout() );
      
      // SOAP Preferences
      {
         Group group = SWTUtil.createGroup( mainComposite, ZLang.ZL_MainSOAPSettings );
         group.setLayout( SWTUtil.createGridLayout( 3 ) );
         SWTUtil.gridDataFillHorizontal( group );
         
         addControlForPreference( Preference.SOAP_RATE, group );
         
         Label text = SWTUtil.addLabel( group, 
            ZLang.ZL_MainSOAPIntervalDescription 
         );
         SWTUtil.gridDataColSpan( text, 3 );
         SWTUtil.gridDataFillHorizontal( text );
         
      }
      
      SWTUtil.createBlankHorizontalFill( mainComposite, 5 );
      
      // Debugging Preferences
      {
         Group group = SWTUtil.createGroup( mainComposite, ZLang.ZL_MainPluginDebugging );
         group.setLayout( SWTUtil.createGridLayout( 3 ) );
         SWTUtil.gridDataFillHorizontal( group );
         
         Button enabled = addControlForPreference( 
            Preference.DEBUG_ENABLED, group 
         ).button();
         
         SWTSet filterSet = addControlForPreference( Preference.DEBUG_FILTER, group );
         
         enabled.addSelectionListener( new SWTEnableListener( 
            filterSet, enabled.getSelection(), enabled
         ) );
         
         addControlForPreference( Preference.DEBUG_UI, group );
      }
      
      mainComposite.pack();
      mainComposite.layout();
      
      return mainComposite;
   }
   

}
