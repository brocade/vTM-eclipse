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

package com.zeus.eclipsePlugin;

/**
 * Class that runs continuously in the background. Can be started and stopped
 * as many time as you wish. Pauses for an interval.
 */
public abstract class BackgroundThread
{
   private RunThread runningThread = null;
   private String name;
   
   private boolean stopped = true;
   private boolean started = false;
   
   public static final int DEFAULT_PRIORITY = -1;
   
   private int pause = 0; 
   private int priority = DEFAULT_PRIORITY;
   
   /**
    * Constructor that should be called by implementing classes.
    * @param name The name of the thread, used to identify it whilst debugging.
    */
   protected BackgroundThread( String name )
   {
      this.name = name;
   }

   /**
    * All the work that is done in the background should be in this thread.
    */
   protected abstract void run() throws InterruptedException;
   
   /**
    * Set the amount of time the thread should wait between running the run()
    * method.
    * @param The amount of milliseconds to wait between doing work.
    */
   protected void setPause( int pause )
   {
      this.pause = pause;
   }
   
   /**
    * Get the amount of time the thread pauses (waits) between calls of run(). 
    * @return The pause in milliseconds.
    */
   protected int getPause()
   {
      return pause;
   }
   
   /**
    * Set the thread priority. Only used when starting a thread.
    * @param priority The priority thread priority, or DEFAULT_PRIORITY to use
    * a threads default priority (average).
    */
   public void setPriority( int priority )
   {
      this.priority = priority;
   }
   
   /**
    * Start the thread running if it was not already.
    */
   public synchronized void start()
   {
      if( started ) return;
      
      runningThread = new RunThread();
      if( priority != DEFAULT_PRIORITY ) {
         runningThread.setPriority( priority );
      }
      
      started = true;
      runningThread.start();            
   }
   
   /**
    * Returns true if the thread is currently running.
    * @return True if the thread is currently running.
    */
   public synchronized boolean isRunning()
   {
      return started;
   }
   
   /**
    * Stops the thread and waits for it to finish (up to a second).
    */
   public synchronized void stop()
   {
      if( !started ) return;
      
      started = false;
      runningThread.interrupt();
      if( !stopped ) {
         try { this.wait( 1000 ); } catch( InterruptedException e ) {}
      }
      
      runningThread = null;
   }
   
   /**
    * Stop the timer but don't wait for it to finish.
    */
   protected void stopNoWait()
   {
      synchronized( this ) {
         if( !started ) return;
         started = false;
         runningThread = null;
      }
   }
   
   /**
    * The thread that does all the work.
    */
   private class RunThread extends Thread
   {
      public RunThread()
      {
         super( name );
      }
      
      /* Override */
      public void run()
      {
         stopped = false;
         while( started )
         {
            try {
               if( pause > 0 ) sleep( pause );
               
               BackgroundThread.this.run();       
               
            } catch( InterruptedException e ) {}
         }
         stopped = true;
         started = false;
         
         synchronized( BackgroundThread.this ) {
            BackgroundThread.this.notifyAll();
         }
      }
      
   }
}
