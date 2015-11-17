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

package com.zeus.eclipsePlugin.filesystem;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Ids;
import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.ModelException;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.model.ModelElement.State;

/**
 * This represents a rule as a editable text file in a ZXTM. The rule does not
 * need to exist, and it check with the model to ensure it's state is correct.
 */
public class RuleFileStore extends FileStore
{
   protected String ruleName;
   protected ZXTM zxtm;
   protected Rule rule;
   protected RuleDirectoryFileStore parent;
   protected ZXTMFileSystem system;
   protected URI uri;
   
   protected String hostname;
   protected int port;

   /**
    * Create a RuleFileStore for a rule that exists.
    * @param system The parent file-system.
    * @param rule The rule this FileStore represents.
    * @param zxtm The parent ZXTM of this rule, used to update the rules state.
    * @param parent The parent folder FileStore
    * @param uri The URI of the RuleFileStore
    */
   public RuleFileStore( ZXTMFileSystem system, Rule rule, ZXTM zxtm, RuleDirectoryFileStore parent, URI uri ) 
   {
      ZDebug.print( 3, "RuleFileStore( system, ", rule, ", ", parent, ", ", uri );
      this.system = system;
      this.rule = rule;
      if( rule == null ) {
         throw new NullPointerException( "Rule cannot be null!" );
      }
      this.ruleName = rule.getName();
      this.parent = parent;
      this.uri = uri;
      this.zxtm = zxtm;
      this.hostname = zxtm.getHostname();
      this.port = zxtm.getAdminPort();
   }
   /**
    * Create a RuleFileStore for a rule that does not exists.
    * @param system The parent file-system.
    * @param ruleName The name of the rule this FileStore represents.
    * @param zxtm The parent ZXTM of this rule, used to update the rules state.
    * @param parent The parent folder FileStore
    * @param uri The URI of the RuleFileStore
    */
   public RuleFileStore( ZXTMFileSystem system, String ruleName, ZXTM zxtm, RuleDirectoryFileStore parent, URI uri ) 
   {
      ZDebug.print( 3, "RuleFileStore( system, ", ruleName, ", ", parent, ", ", uri, " EMPTY" );
      this.ruleName = ruleName;
      this.system = system;
      this.rule = null;
      this.parent = parent;
      this.uri = uri;
      this.zxtm = zxtm;
      this.hostname = zxtm.getHostname();
      this.port = zxtm.getAdminPort();
   }
   
   /**
    * Update the rule field. Sets to null if non-existent.
    */
   private void updateRule()
   {
      if( !ZXTMPlugin.isEclipseLoaded() ) return;
      ModelController model = ZXTMPlugin.getDefault().getModelController();
      zxtm = model.getZXTM( hostname, port );
      
      if( zxtm != null ) {
         rule = zxtm.getRule( ruleName );
      } else {
         rule = null;
      }
   }
   
   /** Always returns an empty array */
   /* Override */
   public String[] childNames( int options, IProgressMonitor monitor )
      throws CoreException
   {      
      return new String[] {};
   }

   /** Return info about this rule, including if it currently exists or not */
   /* Override */
   public IFileInfo fetchInfo( int options, IProgressMonitor monitor )
      throws CoreException
   {   
      FileInfo info = new FileInfo( ZXTMFileSystem.toOSName( ruleName ) + ZXTMFileSystem.TS_FILE_EXTENSION );
      
      updateRule();
      
      // If this rule does not exist return empty info
      if( rule == null || rule.getModelState() == State.DELETED ) {
         info.setExists( false );
         info.setDirectory( false );
         info.setLength( EFS.NONE );
         info.setLastModified( EFS.NONE );
         info.setAttribute( EFS.ATTRIBUTE_READ_ONLY, false );
      
      // Otherwise return the rules info
      } else {      
         info.setExists( true );
         info.setDirectory( false );
         if( rule.getTrafficScriptCode() != null ) {
            info.setLength( rule.getTrafficScriptCode().length() );
         } else {
            info.setLength( 0 );
         }
         info.setLastModified( EFS.NONE );
         info.setAttribute( EFS.ATTRIBUTE_READ_ONLY, false );
      }
      
      return info;
   }
   
   /** Put info, does nothing as there's nothing to change about a rule */
   /* Override */
   public void putInfo( IFileInfo info, int options, IProgressMonitor monitor )
      throws CoreException
   {
      ZDebug.print( 4, "putInfo( ", info, ", ", options,", ", monitor, " ) - ", this );
      
      updateRule();
   }

   /** Rules can't have children, so always returns EmptyFileStore */
   /* Override */
   public IFileStore getChild( String name )
   {
      return new EmptyFileStore( system, name, this );
   }
   
   /** Get the name of this file */
   /* Override */
   public String getName()
   {
      return uri.toString();
   }

   /** Gets the parent directory of this Rule File, always a 
    *  RuleDirectoryFileStore*/
   /* Override */
   public IFileStore getParent()
   {      
      ZDebug.print( 4, "getParent() - ", ruleName, " returning: ", parent );
      return parent;
   }
   
   /** Returns the file-system that manages this file store*/
   /* Override */
   public IFileSystem getFileSystem()
   {
      return system;
   }

   /** 
    * Open an input stream to the rule. If the rule is not accessible, throws 
    * an exception. Otherwise returns an input stream that returns the contents
    * of the rule.
    */
   /* Override */
   public InputStream openInputStream( int options, IProgressMonitor monitor )
      throws CoreException
   {
      ZDebug.print( 4, "openInputStream() - Rule ", ruleName );
      
      updateRule();
      
      // If this rule does not exist throw exception
      if( rule == null || rule.getModelState() == State.DELETED ) {
         throw new CoreException( new Status( IStatus.ERROR, Ids.PLUGIN, ZLang.ZL_RuleNoLongerExists ));
      }
      
      if( rule.getModelState() != State.UP_TO_DATE )
      {
         if( zxtm != null && zxtm.getModelState() == State.WAITING_FOR_FIRST_UPDATE ) {
            throw new CoreException( new Status( IStatus.ERROR, 
               Ids.PLUGIN, 
               ZLang.ZL_WaitingForFirstUpdate
            ) );
         }
         
         switch( rule.getModelState() ) {
            case CANNOT_SYNC: {
               throw new CoreException( new Status( IStatus.ERROR, 
                  Ids.PLUGIN, 
                  ZLang.bind( ZLang.ZL_RuleCouldNotBeRetrieved, 
                     rule.getLastError().getMessage() 
                  )
               ) );
            }
            
            case DELETED: {
               throw new CoreException( new Status( IStatus.ERROR, 
                  Ids.PLUGIN, 
                  ZLang.ZL_RuleNoLongerExists
               ) );
            }
            
            case WAITING_FOR_FIRST_UPDATE:
               throw new CoreException( new Status( IStatus.ERROR, 
                  Ids.PLUGIN, 
                  ZLang.ZL_WaitingForFirstUpdate
               ) );
         }
         
      }
      
      return new ByteArrayInputStream( rule.getTrafficScriptCode().getBytes() );
   }
   
   /**
    * Open an output stream to write to the rule. This uses a magic output 
    * stream which writes to the rule when the stream is closed. If the rule
    * does not exist the stream will create the file.
    */
   /* Override */
   public OutputStream openOutputStream( int options, IProgressMonitor monitor )
      throws CoreException
   {
      ZDebug.print( 4, "openOutputStream() - Rule ", ruleName );
      
      updateRule();
      
      if( rule != null ) { 
         return new RuleOutputStream( rule, zxtm, this );
      } else {
         return new RuleOutputStream( ruleName, zxtm, this );
      }
   }
   
   /**
    * Delete the file == delete the rule.
    */
   /* Override */
   public void delete( int options, IProgressMonitor monitor )
      throws CoreException
   {
      ZDebug.print( 4, "delete() - ", this );      
           
      updateRule();
      
      if( rule != null ) {
         try {
            zxtm.deleteRule( ruleName );
            
         } catch( ModelException e ) {
            ZDebug.printStackTrace( e, "Exception trying to delete file ", ruleName );
            throw new CoreException( new Status( IStatus.ERROR, Ids.PLUGIN, 
               ZLang.bind( ZLang.ZL_CouldNotDeleteRule, ruleName, e.getMessage() ),
               e 
            ));         
         } 
      }

   }
   /** Get the file stores URI */
   /* Override */
   public URI toURI()
   {
      return uri;
   }
   
   /** Copy the file, disables the files system refresher. */
   /* Override */
   protected void copyFile( IFileInfo sourceInfo, IFileStore destination,
      int options, IProgressMonitor monitor ) throws CoreException
   {
      ZDebug.print( 4, "copyFile(", sourceInfo, ", ", destination, ") - ", ruleName );
      
      super.copyFile( sourceInfo, destination, options, monitor );
   }

   
}
