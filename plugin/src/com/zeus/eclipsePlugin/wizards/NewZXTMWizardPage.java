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

package com.zeus.eclipsePlugin.wizards;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.swt.SWTEnableListener;
import com.zeus.eclipsePlugin.swt.SWTSet;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.ZXTMControl;

public class NewZXTMWizardPage extends WizardPage
{
   private Text textLocation, textName;
   private Button radioDefault, radioCustom;
   
   private StatusListener status =  new StatusListener();
   private ZXTMControl zxtmControl;

   public NewZXTMWizardPage()
   {
      super( ZLang.ZL_CreateAZXTMProjectTitle );
   }

   /* Override */
   public void createControl( Composite parent )
   {      
      Composite composite = SWTUtil.createGridLayoutComposite( parent, 2, 10, 10 );
      
      setDescription( ZLang.ZL_EnterDetailsAboutZXTM );
      setTitle( ZLang.ZL_CreateAZXTMProjectTitle );
      setMessage( ZLang.ZL_EnterNameForProject );
      setPageComplete( false );
      
      textName = SWTUtil.addLabeledText( composite, ZLang.ZL_ProjectNameLabel, SWTUtil.FILL ).text();
      textName.addModifyListener( status );
  
      Composite localSection = createProjectLocation( composite );
      SWTUtil.gridDataFillHorizontal( localSection );
      SWTUtil.gridDataColSpan( localSection, 2 );
      
      Composite infoSection = createZXTMInfoSection( composite );
      SWTUtil.gridDataFillHorizontal( infoSection );
      SWTUtil.gridDataColSpan( infoSection, 2 );

      status.update();
      this.setControl( composite );
   }

   private Composite createZXTMInfoSection( Composite parent )
   {
      Group group = SWTUtil.createGroup( parent, ZLang.ZL_ZXTMSettings );
      group.setLayout( SWTUtil.createGridLayout( 2, 10, 6 ) );
      
      zxtmControl = new ZXTMControl( group, status, null );
      
      return group;
   }
   
   private Composite createProjectLocation( Composite parent )
   {
      Group group = SWTUtil.createGroup( parent, ZLang.ZL_ProjectLocation );
      group.setLayout( SWTUtil.createGridLayout( 3, 10, 6 ) );
      
      radioDefault = SWTUtil.gridDataColSpan( SWTUtil.addRadioButton( group, ZLang.ZL_CreateProjectInWorkspace, true ), 3 );
      radioCustom = SWTUtil.gridDataColSpan( SWTUtil.addRadioButton( group, ZLang.ZL_CreateProjectExternally, false ), 3 );
      
      SWTSet setDirectory =
         SWTUtil.addLabeledBrowseWidget( group, ZLang.ZL_DirectoryLabel, 
            ZLang.ZL_BrowseButton, ZLang.ZL_SelectAProjectDirectoryTitle, 
            SWTUtil.FILL 
         );
      
      textLocation = setDirectory.text();
      textLocation.addModifyListener( status );
      
      SWTEnableListener listener = new SWTEnableListener( setDirectory, false, radioCustom );
      radioDefault.addSelectionListener( listener );
      radioCustom.addSelectionListener( listener );
      
      radioCustom.addSelectionListener( status );
      radioDefault.addSelectionListener( status );
      
      return group;
   }

   /* Override */
   public void dispose()
   {
      super.dispose();
   }
   
   
   class StatusListener extends SelectionAdapter implements ModifyListener, VerifyListener
   {
      public void update()
      {
         ZDebug.print( 6, "update()" );
         
         // Stage 1 - Name
         if( textName.getText().equals( "" ) ) {
            setMessage( ZLang.ZL_EnterNameForProject );
            setPageComplete( false );
            setErrorMessage( null );
            return;
         } 
         
         if( !textName.getText().matches( "^[\\w\\d -]+$" ) ) {
            setErrorMessage( ZLang.ZL_ValidateProjectNameContainsInvalidChars );
            setPageComplete( false );
            return;
         }
         
         IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( textName.getText().trim() );
         if( project != null && project.exists() ) {
            setErrorMessage( ZLang.ZL_ValidateProjectAlreadyExists );
            setPageComplete( false );
            return;
         }
         
         // Stage 2 - Location
         if( radioCustom.getSelection() && textLocation.getText().equals( "" ) ) {
            setMessage( ZLang.ZL_SpecifyDirectoryForTheProject );
            setPageComplete( false );
            setErrorMessage( null );
            return;
         }
         
         if( radioCustom.getSelection() ) {
            File dir = new File( textLocation.getText() );
            if( !dir.exists() ) {
               setErrorMessage( ZLang.ZL_ValidateProjectPathDoesNotExist );
               setPageComplete( false );
               return;
            } else if( !dir.isDirectory() ) {
               setErrorMessage( ZLang.ZL_ValidateProjectPathNotDir );
               setPageComplete( false );
               return;
            }
         }
         
         // Stage 3 - ZXTM
         if( ! zxtmControl.isFinished() ) {
            String error = zxtmControl.getError();
            String msg = zxtmControl.getMessage();
            if( error != null ) {
               setErrorMessage( error );
               setPageComplete( false );
               return;
            } 
            if( msg != null ) {
               setMessage( msg );
               setPageComplete( false );
               setErrorMessage( null );
               return;
            }
            
            setPageComplete( false );
         }
                  
         setErrorMessage( null );
         setMessage( ZLang.ZL_VerifyAndFinishToCreateProject );
         setPageComplete( true );
      }

      /* Override */
      public void widgetSelected( SelectionEvent e )
      {
         update();  
      }
      
      /* Override */
      public void modifyText( ModifyEvent e )
      {
         update();         
      }

      /* Override */
      public void verifyText( VerifyEvent e )
      {
         update(); 
      }
      
   }

   public String getProjectName() 
   {
      return textName.getText();
   }
   
   public String getPath()
   {
      if( radioCustom.getSelection() ) {
         return textLocation.getText();
      } else {                 
         return ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + Path.SEPARATOR +
                getProjectName() ;
      }
   }
   
   public boolean useCustomLocation() 
   {
      return radioCustom.getSelection();
   }
   
   public String getHostName()
   {
      return zxtmControl.getHostname();
   }
   
   public int getPort()
   {
      return zxtmControl.getPort();
   }

   public String getPassword()
   {
      return zxtmControl.getPassword();
   }
   
   public String getUserName()
   {
      return zxtmControl.getUserName();
   }

   public boolean storePassword()
   {
      return zxtmControl.getStorePassword();
   }
   
   
}
