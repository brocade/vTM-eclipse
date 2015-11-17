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

import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;

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
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.ZXTM;

/**
 * File store for the directory that stores the rules in ZXTM.
 */
public class RuleDirectoryFileStore extends FileStore
{
   private String name;
   private ZXTM zxtm;
   private ZXTMDirectoryFileStore parent;
   private URI uri;
   private ZXTMFileSystem system;
   private FileSystemWrapperHack hackSystem;
   
   private String hostname;
   private int port;
   
   /**
    * Create a directory file store for a particular ZXTM.
    * @param name The name of the files store.
    * @param zxtm THe ZXTM that holds this directories rules.
    * @param parent The parent file-store of this directory.
    * @param uri The URI of this directory.
    * @param system The file-system that manages this file store.
    */
   public RuleDirectoryFileStore( String name, ZXTM zxtm,
      ZXTMDirectoryFileStore parent, URI uri, ZXTMFileSystem system )
   {
      //Debug.dumpStackTrace();
      this.name = name;
      this.parent = parent;
      this.uri = uri;
      this.system = system;
      this.zxtm = zxtm;
      this.hackSystem = new FileSystemWrapperHack( system, zxtm.getNamePort() );
      this.hostname = zxtm.getHostname();
      this.port = zxtm.getAdminPort();
      
      
      ZDebug.print( 1, "RuleDirectoryFileStore( ", name, ", ", zxtm, ", ", parent, ", ", uri, " ) - ", this.hashCode() );
   }
   
   /**
    * Check the ZXTM hasn't been deleted / changed.
    */
   private void updateZXTM()
   {
      if( !ZXTMPlugin.isEclipseLoaded() ) return;
      ModelController model = ZXTMPlugin.getDefault().getModelController();
      
      zxtm = model.getZXTM( hostname, port );
   }
   
   /**
    * Returns an array of the names of all the rules on the ZXTM.
    */
   /* Override */
   public String[] childNames( int options, IProgressMonitor monitor )
      throws CoreException
   {
      ZDebug.print( 4, "childNames() - ", this );
      
      updateZXTM();
      
      if( zxtm == null ) return new String[] {};

      Rule[] rules = zxtm.getRules();
      ZDebug.print( 6, "ZXTM: ", zxtm, " - ", Integer.toHexString( zxtm.hashCode() ) );
      
      LinkedList<String> ruleNames = new LinkedList<String>();
      
      for( Rule rule : rules ) {
         //if( rule.getModelState() != State.UP_TO_DATE ) continue;
         ruleNames.add( ZXTMFileSystem.toOSName( rule.getName() ) + ".zts" );
         ZDebug.print( 7, "Rules dir - Rule: ",  ruleNames.getLast()  ); 
      } 
      
      return ruleNames.toArray( new String[ruleNames.size()] );
   }
   
   /**
    * Get file info for this directory.
    */
   /* Override */
   public IFileInfo fetchInfo( int options, IProgressMonitor monitor )
      throws CoreException
   {
      updateZXTM();
      
      FileInfo info = new FileInfo( name );
      
      if( zxtm == null ) {         
         info.setExists( false );
         info.setDirectory( true );
         info.setLength( EFS.NONE );
         info.setLastModified( EFS.NONE );
      } else {                
         info.setExists( true );
         info.setDirectory( true );
         info.setLength( EFS.NONE );
         info.setLastModified( EFS.NONE );
         info.setAttribute( EFS.ATTRIBUTE_READ_ONLY, false );
      }
         
      return info;
   }

   /**
    * Get the file-store for the specified child (which will always be a 
    * RuleFileStore). 
    */
   /* Override */
   public IFileStore getChild( String name )
   {
      ZDebug.print( 4, "getChild( ", name, " ) - ", this );
      URI uri = URI.create( toURI() + "/" + ZXTMFileSystem.enc( name  ) );
      return system.getStore( uri );
   }

   /** Get the name of this directory (should always be Rules) */
   /* Override */
   public String getName()
   {
      return name;
   }

   /** Get this directories parent directory (always a ZXTMDirectoryFileStore */
   /* Override */
   public IFileStore getParent()
   {
      ZDebug.print( 2, "getParent() - ", this, " returning: ", parent );
      return parent;
   }
   
   /** Because of an eclipse bug returns a magic file system */
   /* Override */
   public IFileSystem getFileSystem()
   {
      // For Eclipse bug  
      return hackSystem;
   }
   
   /** This file-system is not read only, so returns itself */
   /* Override */
   public IFileStore mkdir( int options, IProgressMonitor monitor )
      throws CoreException
   {
      return this;
   }

   /** Always throws an exception as you can't open an input stream for a 
    *  directory.*/
   /* Override */
   public InputStream openInputStream( int options, IProgressMonitor monitor )
      throws CoreException
   {
      throw new CoreException( new Status( IStatus.ERROR, Ids.PLUGIN, 
         ZLang.bind( ZLang.ZL_DirectoryCannotBeOpenedForInput, toString() )
      ));
   }

   /** Get this directories URI, usually zxtm://foo:9090/Rules */
   /* Override */
   public URI toURI()
   {      
      return uri;
   }
   
   
}
