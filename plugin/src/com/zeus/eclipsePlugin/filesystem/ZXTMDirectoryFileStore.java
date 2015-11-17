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
import com.zeus.eclipsePlugin.consts.Ids;

/**
 * This is the root directory in the ZXTM file-system. It only contains the
 * virtual directories that store the ZXTMs various objects (like the Rules 
 * directory)
 */
public class ZXTMDirectoryFileStore extends FileStore
{
   private URI uri;
   private ZXTMFileSystem system;
   
   /**
    * Create a file system for the specified URI
    * @param filesystem The parent file-system
    * @param uri This directories URI
    */
   public ZXTMDirectoryFileStore( ZXTMFileSystem filesystem, URI uri )
   {
      ZDebug.print( 1, "ZXTMDirectoryFileStore( ", filesystem, ", ", uri, " )" );
      this.system = filesystem;
      this.uri = uri;
   }

   /** Returns all the virtual directories that store ZXTMs objects. Currently
    *  only returns the Rules directory. */
   /* Override */
   public String[] childNames( int options, IProgressMonitor monitor )
      throws CoreException
   {     
      return new String[] { ZXTMFileSystem.RULE_PATH };
   }

   /** Returns info indicating this is a directory that exists. */
   /* Override */
   public IFileInfo fetchInfo( int options, IProgressMonitor monitor )
      throws CoreException
   {
      FileInfo info = new FileInfo( getName() );
      info.setExists( true );
      info.setDirectory( true );
      info.setLength( EFS.NONE );
      info.setLastModified( EFS.NONE );
      
      return info;
   }

   /** Get a child file store */
   /* Override */
   public IFileStore getChild( String name )
   {
      ZDebug.print( 4, "getChild( ", name, " )"  );
      return system.getStore( URI.create( toURI() + "/" + name ) );
   }
   
   /** Gets this folders name, as its root it's just slash. */
   /* Override */
   public String getName()
   {     
      return "/";
   }

   /** This is the root of the file-system, so allwys returns null. */
   /* Override */
   public IFileStore getParent()
   {
      return null;
   }
   
   /** Get the parent file-system, responsible for managing this file store */
   /* Override */
   public IFileSystem getFileSystem()
   {
      return system;
   }
   
   /** Returns this file store as it is not read only. */
   /* Override */
   public IFileStore mkdir( int options, IProgressMonitor monitor )
      throws CoreException
   {      
      return this;
   }

   /** Throws an exception as this is a directory. */
   /* Override */
   public InputStream openInputStream( int options, IProgressMonitor monitor )
      throws CoreException
   {
      throw new CoreException( new Status( IStatus.ERROR, Ids.PLUGIN, 
         ZLang.bind( ZLang.ZL_DirectoryCannotBeOpenedForInput, toURI().toString() )
      ));
   }

   /** The URI of this root directory. */
   /* Override */
   public URI toURI()
   {
      return uri;
   }

}
