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

/**
 * Class that only runs a single instance at once. If the instance is currently 
 * running a request to run another is ignored. Sub-classes should implement 
 * the runAsync method.
 */
public abstract class AsyncExec
{
   private boolean running = false;
   
   /**
    * This method should be called by sub-classes to run the runAsync method in 
    * the SWT thread. Only starts if one isn't all ready running. Always 
    * returns immediately.
    * @param userData Data to pass to the runAsync method.
    */
   protected void startAsync( Object ... userData )
   {
      synchronized( this ) {
         if( running ) return;
         running = true;         
      }
     
      SWTUtil.asyncExec( new AyncRunnable( userData ) );
   }
   
   /**
    * Check if the thread is currently running.
    * @return True if the thread is running, false otherwise.
    */
   public synchronized boolean isRunning()
   {
      return running;
   }
   
   /**
    * Method to be implemented by sub-classes. Is called by the thread when it 
    * is run. This method should not be run directly, only through startAsync().
    * This method is called from the SWT display thread (so it's OK to do UI
    * stuff in this method).
    * @param userData User-data passed to the startAsync method.
    */
   protected abstract void runAsync( Object[] userData );
  
   /**
    * Runnable that is passed to the SWT thread to be run asynchronously.
    */
   private class AyncRunnable implements Runnable
   {
      private Object[] userData;

      /**
       * Setup with user data to pass on to the runAsync method.
       * @param userData The user data to pass on to the runAsync method.
       */
      public AyncRunnable( Object[] userData )
      {
         this.userData = userData;
      }

      /**
       * Just runs the runAsync method
       */
      /* Override */
      public void run()
      {
         runAsync( userData );
         synchronized( AsyncExec.this ) {
            running = false;         
         }
      }
      
   }

}
