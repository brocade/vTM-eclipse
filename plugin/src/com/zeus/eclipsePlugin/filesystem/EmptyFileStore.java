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
 * File store for non-existent files.
 */
public class EmptyFileStore extends FileStore
{
   private String name;
   private IFileStore parent;
   private ZXTMFileSystem system;
   private URI uri;
      
   /**
    * Create a file store for a non-existent file in the passed in directory.
    * @param system The file-system this files store is managed by.
    * @param name The name of the files
    * @param parent The parent directory of the file
    */
   public EmptyFileStore( ZXTMFileSystem system, String name, IFileStore parent )
   {
      ZDebug.print( 5, "EmptyFileStore( ", name, " )" );
      this.system = system;
      this.name = name;
      this.parent = parent;
      
      String uriString = parent.toURI().toString();
      if( !uriString.endsWith( "/" ) ) {
         uriString += "/";
      }
      uriString += ZXTMFileSystem.enc( name );

      try {
         this.uri = URI.create( uriString );
      } catch( IllegalArgumentException e ) {
         ZDebug.printStackTrace( e, "Creating uri failed: ", name, " - ", parent );
      }
   }
   
   /**
    * Create an non-existent file from a URI.
    * @param system The file-system that manages this file.
    * @param uri The URI of the file
    */
   public EmptyFileStore( ZXTMFileSystem system, URI uri )
   {
      this.system = system;
      this.uri = uri;
      String[] paths = uri.getPath().split( "[\\/]" );
      if( paths.length > 0 ) {
         this.name = paths[paths.length - 1];
      } else {
         this.name = "";
      }
      this.parent = null;
   }

   /** Get the children of this file, always returns empty array */
   /* Override */
   public String[] childNames( int options, IProgressMonitor monitor )
      throws CoreException
   {
      return new String[] {};
   }

   /** Returns the files info for a non-existent file */
   /* Override */
   public IFileInfo fetchInfo( int options, IProgressMonitor monitor )
      throws CoreException
   {
      FileInfo info = new FileInfo( name );
      info.setExists( false );
      info.setDirectory( false );
      info.setLength( EFS.NONE );
      info.setLastModified( EFS.NONE );

      return info;
   }

   /** Returns another EmptyFileStore for this child */
   /* Override */
   public IFileStore getChild( String name )
   {
      return new EmptyFileStore( system, name, this );
   }

   /** Get this files name */
   /* Override */
   public String getName()
   {
       return name;
   }

   /** Get this parent (directory) */
   /* Override */
   public IFileStore getParent()
   {
      return parent;
   }

   /** Get the file-system managing this file store */
   /* Override */
   public IFileSystem getFileSystem()
   {
      return system;
   }

   /** Always throws an exception as this file does not exist. */
   /* Override */
   public InputStream openInputStream( int options, IProgressMonitor monitor )
      throws CoreException
   {
      ZDebug.print( 4, "openInputStream() - ", toURI() );
      
      throw new CoreException( new Status( IStatus.ERROR, Ids.PLUGIN, 
         ZLang.bind( ZLang.ZL_FileDoesNotExist, toURI().toString() ) 
      ) );     
   }
   
   /** Returns this file stores URI */
   /* Override */
   public URI toURI()
   {     
      return uri;
   }
   
}
