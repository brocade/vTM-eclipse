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

package com.zeus.eclipsePlugin.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.zeus.eclipsePlugin.consts.Ids;

/**
 * The ZXTM perspective class. This sets up the default layout for the ZXTM 
 * perspective, and adds wizard and view links to Eclipse's menus.
 */
public class ZXTMPerspective implements IPerspectiveFactory
{
   /**
    * Setup the default perspective's view positions.
    */
   /* Override */
   public void createInitialLayout( IPageLayout layout )
   {
      layout.addShowViewShortcut( Ids.ZXTM_VIEW );
      layout.addShowViewShortcut( IPageLayout.ID_PROBLEM_VIEW );
      layout.addShowViewShortcut( IPageLayout.ID_TASK_LIST );
      layout.addShowViewShortcut( IPageLayout.ID_RES_NAV );
      
      layout.addNewWizardShortcut( Ids.NEW_ZXTM_WIZARD );
      layout.addNewWizardShortcut( Ids.NEW_RULE_WIZARD );
      
      layout.addPerspectiveShortcut( Ids.PERSPECTIVE );
            
      String editorArea = layout.getEditorArea();
      
      IFolderLayout left = layout.createFolder( "left", IPageLayout.LEFT, 0.26f, editorArea );
      IFolderLayout bottom = layout.createFolder( "bottom", IPageLayout.BOTTOM, 0.74f, editorArea );
      
      left.addView( Ids.ZXTM_VIEW ); 
      left.addView( IPageLayout.ID_RES_NAV ); 
      
      layout.addPlaceholder( IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.8f, editorArea );
      
      bottom.addView( IPageLayout.ID_PROBLEM_VIEW );
      bottom.addView( IPageLayout.ID_TASK_LIST );
      bottom.addPlaceholder( IPageLayout.ID_PROGRESS_VIEW );
      bottom.addPlaceholder( IPageLayout.ID_BOOKMARKS );
      bottom.addPlaceholder( IPageLayout.ID_PROP_SHEET );
   }

}
