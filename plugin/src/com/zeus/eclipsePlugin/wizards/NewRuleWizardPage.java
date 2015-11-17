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

import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZUtil;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.ModelListener;
import com.zeus.eclipsePlugin.model.ModelSelection;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.model.ModelElement.Event;
import com.zeus.eclipsePlugin.model.ModelElement.State;
import com.zeus.eclipsePlugin.model.ModelElement.Type;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * The first and only page in the NewRuleWizard. Lets users pick a name and ZXTM
 * to add the rule to.
 */
public class NewRuleWizardPage extends WizardPage
{
   private StatusListener status =  new StatusListener();
   private ZXTMStatusListener zxtmListener = new ZXTMStatusListener();
   
   private LinkedList<ModelElement> listenedElements = new LinkedList<ModelElement>();
         
   private Text textName;
   private List listZXTM;

   private ZXTM[] zxtms;
   
   private String name;
   private ZXTM zxtm;
   
   private ModelSelection selection;
   private ZXTM selectedZXTM = null;
   private Label waitingLabel;

   /**
    * Setup the page with a selection.
    * @param selection The selection, or null to not initally select anything.
    */
   protected NewRuleWizardPage( IStructuredSelection selection )
   {
      super( ZLang.ZL_NewTSRule );
      this.selection = ModelSelection.createFromOtherSelection( selection );
      if( this.selection != null ) {
         this.selectedZXTM = this.selection.getSelectedZXTM();
      }
   }

   /**
    * Setup the UI components for this control.
    */
   /* Override */
   public void createControl( Composite parent )
   {      
      Composite composite = SWTUtil.createGridLayoutComposite( parent, 2, 10, 10 );
      
      setDescription( ZLang.ZL_SpecifyNameAndLocation );
      setTitle( ZLang.ZL_AddANewTSRuleTitle );
      
      textName = SWTUtil.addLabeledText( composite, ZLang.ZL_RuleNameLabel, SWTUtil.FILL ).text();
      textName.addModifyListener( status );
      
      // ZXTM List and its label
      Label listLabel = SWTUtil.addLabel( composite, ZLang.ZL_ZXTMToAddRuleTo );
      SWTUtil.gridDataColSpan( listLabel, 2 );
      
      listZXTM = SWTUtil.addList( composite );
      listZXTM.addSelectionListener( status );
      SWTUtil.gridDataFillCols( listZXTM, 2 );
      SWTUtil.gridDataPreferredHeight( listZXTM, 150 );
      
      // Add listeners to the model, so that the ZXTM list gets updated correctly.
      ModelController controller = ZXTMPlugin.getDefault().getModelController();
      
      for( ZXTM zxtm : controller.getSortedZXTMs() ) {
         zxtm.addListener( zxtmListener );
         listenedElements.add( zxtm );                  
      }          
      controller.addListener( zxtmListener );
      listenedElements.add( controller );
      
      waitingLabel = SWTUtil.addLabel( composite, "" );
      SWTUtil.gridDataFillCols( waitingLabel, 2 );
      
      zxtmListener.updateZXTMList(); // Contains implicit status.update()
      
      this.setControl( composite );      
   }
   
   /**
    * Returns the selected name for the new rule.
    * @return The selected name for the new rule.
    */
   public String getName()
   {
      return name.trim();
   }

   /**
    * Returns the ZXTM to add the new rule to.
    * @return The ZXTM to add the new rule to.
    */
   public ZXTM getZXTM()
   {
      return zxtm;
   }
   
   /**
    * Checks that the name entered is valid and not already in existence on the
    * ZXTM.
    */
   class StatusListener extends SelectionAdapter implements ModifyListener
   {
      
      /**
       * Update messages telling user what needs to be typed in. If everything 
       * is entered correctly, allow wizard page to finish.
       */
      public void update()
      {         
         name = textName.getText();
         // Stage 1 - Name
         if( name.equals( "" ) ) {
            setMessage( ZLang.ZL_EnterNameForTSRule );
            setPageComplete( false );
            setErrorMessage( null );
            return;
         } 
         
         String nameError = ZUtil.validateRuleName( name );
         if( nameError != null ) {
            setErrorMessage( nameError );
            setPageComplete( false );
            return;
         }
         
         int zxtmIndex = listZXTM.getSelectionIndex();
         zxtm = (zxtmIndex >= 0) ? zxtms[zxtmIndex] : null;
         
         if( zxtm != null && zxtm.getRule( name.trim() ) != null ) {
            setErrorMessage( ZLang.ZL_ValidationRuleAllreadyExists );
            setPageComplete( false );
            return;
         }
         
         // Stage 2 - Location
         if( zxtm == null ) {
            setMessage( ZLang.ZL_SpecifyTheZXTMToAddRuleTo );
            setPageComplete( false );
            setErrorMessage( null );
            return;
         }
                           
         setErrorMessage( null );
         setMessage( ZLang.ZL_VerifyAndFinishToAddRule );
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
      
   }
   
   /**
    * Listener used to monitor the model for any new ZXTMs (or ZXTMs that have
    * become properly synced). The wizard displays the number of ZXTMs yet to be
    * synchronised properly.
    */
   class ZXTMStatusListener implements ModelListener
   {      
      /**
       * Monitors ZXTMs that are added/synchronised. If something changes the 
       * list of ZXTMs is updated to reflect that.
       */
      public void updateZXTMList()
      { 
         ZDebug.print( 3, "updateZXTMList()" );
         
         // This wizard is no more it seems, stop listening.
         if( listZXTM.isDisposed() || !ZXTMPlugin.isEclipseLoaded() ) {
            ZDebug.print( 4, "Page disposed, destroying listener..." );
            for( ModelElement element : listenedElements ) {
               element.removeListener( this );
            }
            return;
         }
         
         // We are editing some SWT stuff outside of the display thread, so
         // use SWTUtil to run our code in that thread.
         SWTUtil.exec( new Runnable() { public void run()
         {             
            int zxtmIndex = listZXTM.getSelectionIndex();
            ZXTM oldZXTM = (zxtmIndex >= 0) ? zxtms[zxtmIndex] : null;
            
            int newIndex = -1;
            
            // Add ZXTMs to the list
            ModelController controller = ZXTMPlugin.getDefault().getModelController();
            LinkedList<ZXTM> filteredZXTMs = new LinkedList<ZXTM>();
            
            listZXTM.removeAll();
            
            int i = -1, waitingCount = 0;
            for( ZXTM zxtm : controller.getSortedZXTMs() ) {
               ZDebug.print( 4, "Processing ", zxtm, " ", zxtm.getModelState() );
               ZDebug.print( 3, i, " - ", zxtm );
   
               if( zxtm.getModelState() == State.WAITING_FOR_FIRST_UPDATE ) {
                  waitingCount++;
               }
               if( zxtm.getModelState() != State.UP_TO_DATE ) continue;
               i++;
               
               IProject project = ZXTMProject.getProjectForZXTM( zxtm );
               String postfix = "";
               if( project != null ) postfix = " (" + project.getName() + ")";
               
               listZXTM.add( zxtm.toString() + postfix );
               filteredZXTMs.add( zxtm );
               
               // If there was a selection before, use that
               if( oldZXTM != null && zxtm.equals( oldZXTM ) ) {
                  newIndex = i;
                  
               // Otherwise if the Wizard was created with a ZXTM selected in
               // the ZXTM Viewer, use that.
               } else if( oldZXTM == null && selectedZXTM != null &&
                  zxtm.equals( selectedZXTM ) ) 
               {
                  newIndex = i;
               }
            }
            
            // If there is only one ZXTM available, select that.
            if( newIndex == -1 && selectedZXTM == null && 
                filteredZXTMs.size() == 1 )
            {
               newIndex = 0;
            }
            
            // Apply the selection
            if( newIndex != -1 ) {
               listZXTM.setSelection( newIndex );
            }
            
            zxtms = filteredZXTMs.toArray( new ZXTM[filteredZXTMs.size()] );
            
            if( waitingCount > 0 ) {
               if( waitingCount == 1 ) {
                  waitingLabel.setText( 
                     ZLang.ZL_WaitingFor1ZXTMToUpdate 
                  );
               } else {
                  waitingLabel.setText(
                     ZLang.bind( ZLang.ZL_WaitingForManyZXTMsToUpdate, waitingCount )
                  );
               }
               waitingLabel.setVisible( true );
            } else {
               waitingLabel.setText( "" );
               waitingLabel.setVisible( false );
            }
            
            status.update();// Update the status messages at the top of the page
         } } );
         // End of SWT thread stuff.
      }

      /* Override */
      public void childAdded( ModelElement parent, ModelElement child )
      {
         if( child.getModelType() == Type.ZXTM ) {
            child.addListener( this );
            listenedElements.add( child );
            updateZXTMList();
         }         
      }

      /* Override */
      public void modelUpdated( ModelElement element, Event event )
      {
         updateZXTMList();
      }

      /* Override */
      public void stateChanged( ModelElement element, State state )
      {
         updateZXTMList();
      }
      
   }

}
