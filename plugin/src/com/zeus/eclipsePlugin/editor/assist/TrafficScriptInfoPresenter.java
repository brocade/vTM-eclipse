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

package com.zeus.eclipsePlugin.editor.assist;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.widgets.Display;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.consts.ExternalPreference;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * This is used to format the HTML descriptions for functions. It takes the HTML
 * and strips the tags, and applies styles to a StyledText.
 */
public class TrafficScriptInfoPresenter implements DefaultInformationControl.IInformationPresenter, DefaultInformationControl.IInformationPresenterExtension
{
   /* Override */
   public String updatePresentation( Display display, String hoverInfo,
      TextPresentation presentation, int maxWidth, int maxHeight )
   {      
      return updatePresentation( display, hoverInfo, presentation, maxWidth, maxHeight );
   }
   
   /* Override */
   public String updatePresentation( Drawable drawable, String hoverInfo,
      TextPresentation presentation, int maxWidth, int maxHeight )
   {
      ZDebug.print( 5, "updatePresentation( ", drawable, ", ", hoverInfo, ", ", presentation, ", ", maxWidth, ", ", maxHeight, " )" );
     
      HTMLFormat html = new HTMLFormat();
      html.format( hoverInfo );
      
      for( StyleRange style : html.getStyleList() ) {
         presentation.addStyleRange( style );
      }
      
      if( drawable instanceof StyledText ) {
         StyledText styled = (StyledText) drawable;
         styled.setWordWrap( html.isWordWrap() );
         if( !html.isWordWrap() ) {
            SWTUtil.fontPreference( styled, ExternalPreference.FONT_EDITOR_TEXT );
         }
      }
      
      return html.getBuffer();
   }


}
