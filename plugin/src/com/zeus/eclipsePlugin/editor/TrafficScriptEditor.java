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

package com.zeus.eclipsePlugin.editor;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import com.zeus.eclipsePlugin.ImageManager;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.codedata.VersionCodeData;
import com.zeus.eclipsePlugin.consts.Ids;
import com.zeus.eclipsePlugin.consts.ImageFile;
import com.zeus.eclipsePlugin.editor.presentation.ZXTMScanner;
import com.zeus.eclipsePlugin.filesystem.ZXTMFileSystem;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.ModelListener;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.RuleProblem;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.model.ModelElement.Event;
import com.zeus.eclipsePlugin.model.ModelElement.State;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.swt.SWTUtil;
import com.zeus.eclipsePlugin.swt.dialogs.ZDialog;


/**
 * The central class for the TrafficScript editor.
 * 
 * Brings in all management classes that deal with the various parts of the 
 * editor.
 */
public class TrafficScriptEditor extends TextEditor
{
   private ZXTM zxtm;
   private Rule rule;
   private String ruleName;
   private RuleListener listener = new RuleListener();
   private IFile file;
   private EditorNotification notify;
   private TrafficScriptEditor thisEditor;
   protected boolean disposed = false;
   private boolean lastSaveFailed = false;
   private TextViewerUndoManager undoManager = null;
   private ContentAssistant assistant;
   private ZXTMScanner[] scanners;
   
   private static HashMap<String,TrafficScriptEditor> openEditors = 
      new HashMap<String,TrafficScriptEditor>();
   
   /**
    * Set the Configuration class which controls such things as partitioning,
    * annotations (when hovering over stuff), what happens when text gets 
    * clicked on, code colouring etc.
    */
   /* Override */
   protected void initializeEditor()
   {
      ZDebug.print( 2, "initializeEditor()" );
      
      super.initializeEditor();
      setSourceViewerConfiguration( new TrafficScriptConf( this ) );     
      notify = new EditorNotification( this );
      thisEditor = this;
   }
      
   /**
    * Publicly expose the source viewer.
    * @return The source viewer for this editor.
    */
   public ISourceViewer getViewer() 
   {
      return this.getSourceViewer();
   }

   /**
    * Returns false if the editor is editing a RuleBuilder rule, or the rule 
    * cannot be edited.
    * @return If this file can be edited.
    */
   /* Override */
   public boolean isEditable()
   {
      if( rule != null && (rule.isRulebuilder() || rule.getModelState() == State.CANNOT_SYNC)  ) {         
         return false;
      }
      return super.isEditable();
   }
   
   /**
    * Uses this rules ZXTM to check the code before saving. If the code has 
    * errors, stop the rule being saved.
    */
   /* Override */
   public void doSave( IProgressMonitor progressMonitor )
   {
      ZDebug.print( 4, "doSave()" );
      try {
         String text = this.getSourceViewer().getTextWidget().getText();
         
         ZXTM closestZXTM = this.getClosestZXTM();
                 
         // No ZXTMs loaded
         if( closestZXTM == null ) {
            super.doSave( progressMonitor );
            lastSaveFailed = false;
            return;
         }
         
         // Check the code before we save it.     
         RuleProblem[] errors = closestZXTM.checkTrafficScriptCode( text );      
         
         // If code contains errors, add markers to the file
         MarkerManager.updateErrorMarkers( file, errors );    
         MarkerManager.updateTaskMarkers( file, text );
         
         boolean hasErrors = false;
         for( RuleProblem error : errors ) {
            if( error.isError() ) {
               hasErrors = true;
               break;
            }
         }
         
         // If this file is on a ZXTM and there are errors, refuse to save.
         if( rule != null && hasErrors ) {    
            if( progressMonitor != null ) {
               progressMonitor.setCanceled( true );
            }
            
            lastSaveFailed = true;
            updateNotifyBar();
            return;
         }
         
         lastSaveFailed = false;
         updateNotifyBar();
      } catch( RuntimeException e ) {
         ZDebug.printStackTrace( e, "Failed to save file: ", file, " rule: ", ruleName );
      }
      
      // If no errors or file on disk, save.
      super.doSave( progressMonitor );
   }
   
   /**
    * Check the state of the rule this editor is editing, and update the 
    * notification bar accordingly.
    */
   protected void updateNotifyBar()
   {
      ZDebug.print( 4, "updateNotifyBar() - ", rule );
      
      // Last save failed, show bar with warning and revert option
      if( lastSaveFailed ) {
         ZDebug.print( 5, "Show: Last saved failed" );
         notify.setMessage( ZLang.ZL_EditorCouldNotSaveAsCode );
         notify.setButton1( ZLang.ZL_EditorRevertChanges, new Runnable() {

            /* Override */
            public void run()
            {
               doRevertToSaved();
            }
            
         });  
         
         notify.showNotifyBar();
         
      // Rule update failed, show bar. This shouldn't ever be shown...
      } else if( rule != null && rule.getModelState() == State.CANNOT_SYNC ) {
         ZDebug.print( 5, "Show: Cannot sync" );
         notify.setMessage( 
            ZLang.bind( ZLang.ZL_EditorRuleCannotBeRetrieved, 
               rule.getLastError().getError().getMessage() 
            )
         );
         notify.setButton1( null, null );         
         notify.showNotifyBar();
         
      // Show pop-up if this is a RuleBuilder rule.
      } else if( rule != null && rule.isRulebuilder() ) {
         notify.setMessage( 
            ZLang.ZL_EditorPreviewOfRulebuilderRule 
         );
         notify.setButton1( ZLang.ZL_EditorConvert, new Runnable() {

            /* Override */
            public void run()
            {
               if( ZDialog.showConfirmDialog(
                  ZLang.ZL_EditorConvertConfirmTitle, 
                  ZLang.ZL_EditorConvertConfirmMessage
               ) ) 
               {
                  try {
                     rule.setCode( rule.getTrafficScriptCode() );     
                  } catch( Exception e ) {
                     ZDebug.printStackTrace( e, "Convert to TS failed for ", rule );
                     
                     ZDialog.showErrorDialog( ZLang.TS_TrafficScriptConvertFailedTitle,
                        ZLang.bind( ZLang.TS_TrafficScriptConvertFailedMessage,
                           e.getLocalizedMessage()
                        )
                     );
                  }
                  SWTUtil.exec( reopenAction ); 
               }
            }
            
         });        
         notify.showNotifyBar();
         
         ImageManager images = ZXTMPlugin.getDefault().getImageManager();         
         Image rulebuilderImage = images.getImage( ImageFile.RULE_RB );
         
         if( !getTitleImage().equals( rulebuilderImage ) ) {
            setTitleImage( rulebuilderImage );
         }
     
      // No problems to report, hide the notification bar
      } else {
         ZDebug.print( 5, "Hide" );
         notify.hideNotifyBar();
         
         ImageManager images = ZXTMPlugin.getDefault().getImageManager();         
         Image ruleImage = images.getImage( ImageFile.RULE );
         
         if( !getTitleImage().equals( ruleImage ) ) {
            setTitleImage( ruleImage );
         }
      }
   }

   /**
    * The file has been changed externally, update markers if change was
    * accepted.
    */
   /* Override */
   protected void handleEditorInputChanged()
   {
      super.handleEditorInputChanged();
      updateNotifyBar();
      
      ZDebug.print( 5, "Checking if user used new change..." );
      String text = getSourceViewer().getTextWidget().getText();
      if( text.equals( rule.getTrafficScriptCode() ) ) {
         ZDebug.print( 6, "They did, updating markers" );
         MarkerManager.update( rule );
      }
   }
   
   /**
    * Revert to the last saved version. The save can't have failed, so update
    * the notify bar. Also reload the markers.
    */
   /* Override */
   public void doRevertToSaved()
   {
      super.doRevertToSaved();
      
      lastSaveFailed = false;
      MarkerManager.update( rule );
      updateNotifyBar();
   }

   /**
    * New input specified, check the rule is loaded. If setup listeners to wait
    * for the rule to be ready, so we can update the editor.
    * @param input
    * @throws CoreException
    */
   /* Override */
   protected void doSetInput( IEditorInput input ) throws CoreException
   {
      ZDebug.print( 3, "doSetInput( ", input, " )" );
      super.doSetInput( input );
      lastSaveFailed = false;
      
      String name = input.getName();
      if( name.endsWith( ZXTMFileSystem.TS_FILE_EXTENSION ) ) {
         ruleName = ZXTMFileSystem.toZXTMName( name.substring( 0, name.length() - 4 ) );
         ZDebug.print( 4, "Input changed: ", name, " => ", ruleName );      
         this.setPartName( ruleName );
      } else {
         ruleName = name;
      }
      
      FileEditorInput fileInput = (FileEditorInput) input;
      URI uri = fileInput.getFile().getRawLocationURI();
      
      file = fileInput.getFile();
      
      ZDebug.print( 5, "URI scheme: ", uri.getScheme() );
      
      // Is this a ZXTM hosted file? If not stop here.
      if( !uri.getScheme().equals( ZXTMFileSystem.PROTOCOL ) ) {
         updateNotifyBar();
         return;
      }
      
      // Try and get the ZXTM for this rule
      ModelController model = ZXTMPlugin.getDefault().getModelController();
      zxtm = model.getZXTM( uri.getHost(), uri.getPort() );
      
      if( zxtm == null ) {
         ZDebug.print( 3, "Could not find ZXTM for uri: ", uri );
         updateNotifyBar();
         return;
      } 
            
      // Try and get the rule
      rule = zxtm.getRule( ruleName );
      
      // Can't get the rule, setup a listener to wait for the rule to be ready
      if( rule == null ) {
         ZDebug.print( 3, "Could not find Rule for uri: ", uri );
         zxtm.addListener( listener );
         updateNotifyBar();
         return;
      } 
      
      // We have the rule, update quick lookup table
      openEditors.put( zxtm + " > " + rule, this );
      rule.addListener( listener );
      
      updateNotifyBar();
   }
   
   /**
    * Close this editor. Remove any setup listeners and ensure markers are 
    * valid.
    */
   /* Override */
   public void close( boolean save )
   {
      ZDebug.print( 4, "close( ", save, " )"  );
      openEditors.put( zxtm + " > " + rule, null );
      disposed = true;
      
      if( rule != null ) rule.removeListener( listener );
      if( zxtm != null ) zxtm.removeListener( listener );
      thisEditor = null;
      super.close( save );
      
      // If we have unsaved changes with errors in them, we need to clear them.
      if( rule != null ) {      
         MarkerManager.update( rule );
      }
   }

   /**
    * Close this editor. Remove any setup listeners and ensure markers are 
    * valid.
    */
   /* Override */
   public void dispose()
   {
      ZDebug.print( 3, "dispose()"  );
      disposed = true;
      openEditors.put( zxtm + " > " + rule, null );
      if( rule != null ) rule.removeListener( listener );
      if( zxtm != null ) zxtm.removeListener( listener );
      if( scanners != null ) {
         for( ZXTMScanner scanner : scanners ) scanner.dispose();
      }
      thisEditor = null;
      super.dispose();
      
      // If we have unsaved changes with errors in them, we need to clear them.
      if( rule != null ) {      
         MarkerManager.update( rule );
      }
   }

   // -------------------------- Actions --------------------------------
   private DeleteRuleAction deleteAction = new DeleteRuleAction();
   private ReOpenAction reopenAction = new ReOpenAction();
   
   /**
    * Updates listener.
    */
   class RuleListener implements ModelListener
   {
      /**
       * Check for changes and deletions.
       */
      /* Override */
      public void modelUpdated( ModelElement element, Event event )
      {
         if( thisEditor == null ) return;
         
         switch( event ) {
            case DELETED: {
               ZDebug.print( 4, "Rule deleted - Using delete action" );
               SWTUtil.exec( deleteAction );
               break;
            }
            default: {
               ZDebug.print( 4, "Rule altered - Using default update action." );
               SWTUtil.exec( new UpdateAction( rule.getTrafficScriptCode() ) );
               break;
            }
         }
      }
         
      /**
       * If the rule was syncing while this editor was opened, this refreshes
       * the editor when the rule is loaded.
       */
      /* Override */
      public void childAdded( ModelElement parent, ModelElement child ) 
      {
         if( thisEditor == null ) return;
         
         if( rule == null && ruleName != null &&
             parent.getModelType() == ModelElement.Type.ZXTM ) 
         {
            ZXTM zxtm = (ZXTM) parent;
            rule = zxtm.getRule( ruleName );
            if( rule != null ) {
               openEditors.put( zxtm + " > " + rule, TrafficScriptEditor.this );
               SWTUtil.exec( reopenAction );
            }
         }
      }

      /* Override */
      public void stateChanged( ModelElement element, State state )
      {
         
      }
   }
   
   /**
    * Check contents changes. If changed (externally) a dialog will ask the
    * user what they want to do.
    */
   class UpdateAction implements Runnable
   {
      private String ruleCode;
      
      public UpdateAction( String ruleCode )
      {
         this.ruleCode = ruleCode;
      }
      
      /* Override */
      public void run()
      {
         ZDebug.print( 4, "run() - updateAction" );
         try {
            updateNotifyBar();
            String text = getSourceViewer().getTextWidget().getText();

            if( !ruleCode.equals( text ) ) {
               ZDebug.print( 5, "Rule changed outside of editor... displaying update dialog" );
               ZDebug.print( 10, "Rule:\n", ruleCode );
               ZDebug.print( 10, "Edit\n:", text );
               
               URI uri = ZXTMFileSystem.getURIForModelElement( rule );
               IFileSystem fs = EFS.getFileSystem( ZXTMFileSystem.PROTOCOL ); 
               
               IFileStore store = fs.getStore( uri );
               ZDebug.print( 5, "File Store of rule is: ", store );
                              
               // Wait for Eclipse to regain focus if it doesn't have it.
               Thread thread = new Thread() {
                  public void run() {
                     while( !SWTUtil.isWindowFocused() ) 
                     {                        
                        ZDebug.print( 8, "Tick! Waiting for window to regain focus..." );
                        try { sleep( 500 ); } catch ( InterruptedException e ) {}                      
                     }
                     ZDebug.print( 5, "Window got focus" );

                     if( disposed ) return;
                     
                     SWTUtil.exec( new Runnable() { public void run() {
                        ZDebug.print( 5, "handleEditorInputChanged due to bg change." );
                        handleEditorInputChanged();
                     } } );
                     
                  }
               };
               
               thread.start();
            } else {
               ZDebug.print( 5, "Editor change - ignoring" );
            }
         } catch( CoreException e ) {
            ZDebug.printStackTrace( e, "External change action error for rule: ", rule );
         }
      }      
   }
   
   class DeleteRuleAction implements Runnable 
   {
      /* Override */
      public void run()
      {
        ZDialog.showErrorDialog( ZLang.ZL_EditorRuleDeletedTitle,
           ZLang.ZL_EditorRuleDeletedMessage
        );        
      }      
   }
   
   /**
    * Reload the contents of this editor. No dialogs are shown.
    */
   class ReOpenAction implements Runnable 
   {
      /* Override */
      public void run()
      {
         setInput( new FileEditorInput( file ) );
      }      
   }
   
   // Static methods
   
   /**
    * Get the TrafficScript editor that is editing the specified rule. Or null
    * if there is no rule open.
    */
   public static TrafficScriptEditor getEditorForRule( Rule rule ) 
   {
      ZDebug.print( 3, "getEditorForRule( ", rule, " )"  );
      TrafficScriptEditor editor = getEditorForRuleQuick( rule );
      if( editor != null ) return editor;
      
      for( IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows() ) {
         
         for( IWorkbenchPage page : window.getPages() ) {
            for( IEditorReference ref : page.getEditorReferences() ) {
               try {
                  if( ref.getEditorInput() instanceof FileEditorInput ) {
                     
                     FileEditorInput input = (FileEditorInput) ref.getEditorInput();
                     if( input != null && input.getURI() != null &&
                         input.getURI().equals( ZXTMFileSystem.getURIForModelElement( rule ) ) ) 
                     {
                        IEditorPart result = ref.getEditor( true );
                        if( result instanceof TrafficScriptEditor ) {
                           return (TrafficScriptEditor) result;
                        }
                     }              
                     
                  }
               } catch( Exception e ) {
                  ZDebug.printStackTrace( e, "Exception when searching for editor for rule", rule );
               }
            }
         }
      }
      
      return null;
   }
   
   /**
    * Closes the editor for the passed rule. Does noting if there is no editor.
    * @param rule The rule who's editor you want to close
    * @param save If true the user will be queried to save the rule.
    */
   public static void closeEditorForRule( Rule rule, boolean save )
   {
      TrafficScriptEditor editor = getEditorForRule( rule );
      if( editor != null ) {
         editor.getSite().getWorkbenchWindow().getActivePage().closeEditor( editor, save ); 
      }
   }
      
   /**
    * Like getEditorForRule but wont return right answer if the rule hasn't 
    * been synced yet, or the ZXTM is uncontactable.
    * @return
    */
   public static TrafficScriptEditor getEditorForRuleQuick( Rule rule )
   {
      return openEditors.get( rule.getModelParent() + " > " + rule ); 
   }
   
   /**
    * Open an editor for a particular rule if one isn't already open. Otherwise
    * just focus on the currently open editor.
    * @param rule The rule we want an editor for.
    */
   public static void openEditorForRule( Rule rule ) 
   {
      ZDebug.print( 3, "openEditorForRule( ", rule, " )"  );
      
      TrafficScriptEditor editor = getEditorForRule( rule );
      if( editor != null ) {
         ZDebug.print( 5, "Editor allready open" );
         try {
            editor.getSite().getPage().activate( editor );
         } catch ( RuntimeException e ) {
            ZDebug.printStackTrace( e, "Exception when focusing editor for rule ", rule );
         }
      } else {   
         ZDebug.print( 5, "No editor open, creating..." );
         try {            
            IFile file = ZXTMProject.getFileForRule( rule );
            
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor( 
               new FileEditorInput( file ), Ids.TRAFFIC_SCRIPT_EDITOR 
            );
         } catch( Exception e ) {
            ZDebug.printStackTrace( e, "Exception when opening new editor for rule: ", rule );
         }
      }
   }
   
   /**
    * Get a list of all the open TrafficScript editors.
    * @return A list of all the open editors.
    */
   public static List<TrafficScriptEditor> getAllEditors()
   {
      LinkedList<TrafficScriptEditor> list = new LinkedList<TrafficScriptEditor>();
      
      for( IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows() ) {
         
         for( IWorkbenchPage page : window.getPages() ) {
            for( IEditorReference ref : page.getEditorReferences() ) {
               try {
                  if( ref.getEditorInput() instanceof FileEditorInput ) {
                     
                     IEditorPart result = ref.getEditor( true );
                     if( result instanceof TrafficScriptEditor ) {
                        list.add( (TrafficScriptEditor) result );
                     }
            
                  }
               } catch( Exception e ) {
                  ZDebug.printStackTrace( e, "Exeception whilst getting all editors" );
               }
            }
         }
      }
      
      return list;
   }

   /**
    * Get this editors ZXTM, or null if this isn't a rule on a ZXTM (e.g. local
    * file) 
    * @return The editors ZXTM, or null if its a local file.
    */
   public ZXTM getZXTM()
   {
      return zxtm;
   }

   /** 
    * Get the rule this editor is editing, or null if the file isn't on a ZXTM
    * or the rule hasn't been loaded yet.
    * @return The rule this editor is editing, or null if the rule is local or
    * doesn't exist.
    */
   public Rule getRule()
   {
      return rule;
   }

   /**
    * Get the name of the rule this editor is editing.
    * @return The name of the rule this editor is editing.
    */
   public String getRuleName()
   {
      return ruleName;
   }

   /**
    * Get the file this editor is currently editing.
    * @return The file this editor is editing.
    */
   public IFile getFile()
   {
      return file;
   }
   
   /**
    * Set the undo manager for this editor.
    * @param undoManager The new undo manager.
    */
   public void setUndoManager( TextViewerUndoManager undoManager )
   {
      this.undoManager = undoManager;
   }
   
   /**
    * Get this editors undo manager.
    * @return The undo manager this editor uses.
    */
   public TextViewerUndoManager getUndoManager()
   {
      return undoManager;
   }
   
   /**
    * Set the completion assistant for this editor.
    * @param assistant This editors new completion assistant
    */
   public void setAssistant( ContentAssistant assistant )
   {
      this.assistant = assistant;
   }

   /**
    * Get the completion assistant for this editor.
    * @return The assistant this editor is using.
    */
   public ContentAssistant getAssistant()
   {
      return assistant;
   }
   
   /**
    * Set the code scanners this editor is using. Will be disposed when the 
    * editor is disposed.
    * @param scanners The scanners that colour/style this editors text.
    */
   public void setCodeScanners( ZXTMScanner ... scanners )
   {
      this.scanners = scanners;
   }

   /**
    * Return the best ZXTM for checking this files code and retrieving code 
    * data. If this is a file thats stored on an actual ZXTM, it just returns 
    * that. Otherwise just pick the first valid ZXTM in the model (so local 
    * files can be coloured and checked correctly).
    * @return The best ZXTM to deal with this file.
    */
   public ZXTM getClosestZXTM()
   {
      ZXTM closestZXTM = this.getZXTM();
      
      if( closestZXTM == null ) {
         ZDebug.print( 5, "Trying to find a ZXTM to talk to get a version" );
         ModelController model = ZXTMPlugin.getDefault().getModelController();
         
         for( ZXTM currentZXTM : model.getSortedZXTMs() ) {
            if( currentZXTM.getModelState() == State.UP_TO_DATE ) {
               closestZXTM = currentZXTM;
               break;
            }
         }
         
      }
      
      return closestZXTM;
   }
   
   /**
    * Get the best code data for this editor.
    * @return The TrafficScriptVersion that stores the best code data to colour
    * this file and give function descriptions.
    */
   public VersionCodeData getCodeDataVersion()
   {      
      ZDebug.print( 4, "getCodeDataVersion()" );
      
      ZXTM closestZXTM = this.getClosestZXTM();
          
      int major, minor;
      if( closestZXTM == null || closestZXTM.getModelState() != State.UP_TO_DATE ) {
         ZDebug.print( 5, "Editor ZXTM not ready: ", closestZXTM, ", using latest" );
         major = 1000;
         minor = 1000;
      } else {      
         major = closestZXTM.getMajorVersion();
         minor = closestZXTM.getMinorVersion();
         ZDebug.print( 5, "Version of ZXTM: ", major, ".",  minor );
      }
            

      return ZXTMPlugin.getDefault().getTrafficScriptCodeData().getVersion( major, minor );
   }
   
}
