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

import java.io.File;
import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.filesystem.IFileTree;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import com.zeus.eclipsePlugin.ZDebug;

/**
 * This is an evil, evil hack for Eclipse Bug #192631 
 * (https://bugs.eclipse.org/bugs/show_bug.cgi?id=192631).<p/>
 * 
 * Eclipse thinks 2 files are the same (e.g. one is an alias of another) if 
 * their URIs are the same NOT TAKING INTO ACCOUNT HOSTNAME OR PORT. Therefore 
 * 'zxtm://foo:9090/Rules' and 'zxtm://bar:9090/Rules' are the same. The 
 * Comparator class that makes this mistake (AliasManager line 472) also checks
 * the FileSystem's scheme (i.e. 'zxtm') of the FileStores. This class wraps 
 * around our ZXTMFileSystem class, altering get scheme to include hostname and
 * port if the Comparator method calls it.
 */
public class FileSystemWrapperHack implements IFileSystem
{
   private ZXTMFileSystem system;
   private String node;
   
   public FileSystemWrapperHack( ZXTMFileSystem system, String node )
   {
      this.system = system;
      this.node = node;
   }

   /* Override */
   public String getScheme()
   {
      StackTraceElement[] elements = Thread.currentThread().getStackTrace();
      ZDebug.print( 2, "getScheme()" );
      
      // Cycle through the last 5ish elements and see if its the Alias Manager
      // calling us with its buggy code. We do this in a loop as different 
      // versions of java pad the top of the stack trace with different amounts 
      // of stuff!
      for( int i = 0; i < elements.length; i++ ) {
         ZDebug.print( 4, "Checking element[", i, "] - ", elements[i] );
         
         if( elements[i].getClassName().contains( "AliasManager" )
            && elements[i].getMethodName().equals( "compare" ) ) 
         {
            ZDebug.print( 3, "Altering scheme for ", elements[i] );
            return system.getScheme() + ":" + node;
         }

         if( i >= 4 ) break;
      }
      
      return system.getScheme();
   }
   
   /* Override */
   public int attributes()
   {
      return system.attributes();
   }

   /* Override */
   public boolean canDelete()
   {
      return system.canDelete();
   }

   /* Override */
   public boolean canWrite()
   {
      return system.canWrite();
   }

   /* Override */
   public IFileTree fetchFileTree( IFileStore root, IProgressMonitor monitor )
      throws CoreException
   {
      return system.fetchFileTree( root, monitor );
   }

   /* Override */
   public IFileStore fromLocalFile( File file )
   {
      return system.fromLocalFile( file );
   }

   /* Override */
   public IFileStore getStore( IPath path )
   {
      return system.getStore( path );
   }

   /* Override */
   public IFileStore getStore( URI uri )
   {
      return system.getStore( uri );
   }

   /* Override */
   public boolean isCaseSensitive()
   {
      return system.isCaseSensitive();
   }

   @SuppressWarnings("unchecked")
   /* Override */
   public Object getAdapter( Class adapter )
   {
      return system.getAdapter( adapter );
   }

}
