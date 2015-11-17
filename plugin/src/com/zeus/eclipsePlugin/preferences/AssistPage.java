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

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.swt.SWTEnableListener;
import com.zeus.eclipsePlugin.swt.SWTSet;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * The editor assistant preference for the ZXTM plug-in. Alters how the 
 * TrafficScript editor tries to help the user whilst typing.
 */
public class AssistPage extends ZXTMPreferencePage
{
   /**
    * Create the contents of the preference page.
    */
   /* Override */
   protected Control createContents( Composite parent )
   {
      ZDebug.print( 4, "createControl( ", parent, " )" );
      Composite mainComposite = SWTUtil.createGridLayoutComposite( parent, 1 );
      SWTUtil.gridDataFillHorizontal( mainComposite );
      SWTUtil.removeLayoutMargins( (GridLayout) mainComposite.getLayout() );
      
      // Mouse hover options
      Group hoverGroup = SWTUtil.createGridLayoutGroup( mainComposite, ZLang.ZL_AssistMouseHover, 3 );
      SWTUtil.gridDataFillHorizontal( hoverGroup );
      {
         SWTUtil.addLabel( hoverGroup, ZLang.ZL_AssistShowInfoWhenHovering );
         
         addControlForPreference( Preference.HOVER_PROBLEMS, hoverGroup );
         addControlForPreference( Preference.HOVER_TASKS, hoverGroup );
         addControlForPreference( Preference.HOVER_DOCS, hoverGroup );
      }
      
      SWTUtil.createBlankHorizontalFill( mainComposite, 5 );
      
      // Code auto-complete options
      Group assistGroup = SWTUtil.createGridLayoutGroup( mainComposite, ZLang.ZL_AssistAutoComplete, 3 );
      SWTUtil.gridDataFillHorizontal( assistGroup );
      {
         SWTUtil.addLabel( assistGroup, ZLang.ZL_AssistAutoCompletePossibilitiesFor );
         
         Button buttonGroup = addControlForPreference(
            Preference.ASSIST_GROUP, assistGroup 
         ).button();
         
         Composite indent = SWTUtil.createGridLayoutComposite( assistGroup, 4 );
         SWTUtil.removeLayoutMargins( indent.getLayout() );
         SWTUtil.gridDataFillCols( indent, 3 );
         SWTUtil.createBlankGrid( indent, 10, 1 );
         Button buttonGroupFunc = addControlForPreference( 
            Preference.ASSIST_GROUP_FUNC, indent 
         ).button();
         buttonGroup.addSelectionListener( new SWTEnableListener( 
            buttonGroupFunc, buttonGroup.getSelection(), buttonGroup 
         ) );
         
         addControlForPreference( Preference.ASSIST_FUNC, assistGroup );
         
         // SFT: Keywords don't work correctly without a code parser
         //addControlForPreference( Preference.ASSIST_KEYWORDS, assistGroup );
      }
      
      SWTUtil.createBlankHorizontalFill( mainComposite, 5 );
      
      // Parameter pop-up options
      Group contextGroup = SWTUtil.createGridLayoutGroup( mainComposite, ZLang.ZL_AssistParameterPopup, 3 );
      SWTUtil.gridDataFillHorizontal( contextGroup );
      {         
         Button button = (Button) addControlForPreference( Preference.CONTEXT_ENABLE, contextGroup ).button();
         SWTSet waitSet = addControlForPreference( Preference.CONTEXT_WAIT, contextGroup );
         waitSet.setEnabled( button.getSelection() );
         
         button.addSelectionListener( new SWTEnableListener( waitSet, button.getSelection(), button ) );         
      }
            
      mainComposite.pack();
      mainComposite.layout();
      
      return mainComposite;
   }
   
}
