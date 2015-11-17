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

package com.zeus.eclipsePlugin.swt;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Monitor that does nothing. Useful for workspace operations that are not 
 * passed a monitor to use.
 */
public class EmptyMonitor implements IProgressMonitor
{

   /* Override */
   public void beginTask( String name, int totalWork )
   {
      
   }

   /* Override */
   public void done()
   {
      
   }

   /* Override */
   public void internalWorked( double work )
   {
      
   }

   /* Override */
   public boolean isCanceled()
   {     
      return false;
   }

   /* Override */
   public void setCanceled( boolean value )
   {
      
   }

   /* Override */
   public void setTaskName( String name )
   {
      
   }

   /* Override */
   public void subTask( String name )
   {
      
   }

   /* Override */
   public void worked( int work )
   {
      
   }

}
