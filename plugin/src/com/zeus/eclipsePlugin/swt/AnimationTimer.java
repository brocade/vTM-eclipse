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

import com.zeus.eclipsePlugin.BackgroundThread;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;

/**
 * Class that counts continuously at a specified rate. Used to 
 * update an animation's frames or perform something at regular intervals.
 */
public class AnimationTimer extends BackgroundThread
{
   /** When the timer hits it's limit, it can do a number of different
    * things, defined by this enumeration. */
   public enum Mode {
      /** Stop the timer when the limit is hit. */
      STOP,
      
      /** When the limit is met, invert the increment so that it 'winds' 
       * backwards. The timer will never stop on its own in this
       * mode */
      REWIND,
      
      /** Reset to the start when the limit is reached. The timer will never 
       * stop in this mode. */
      LOOP,
   }
   
   private int time, increment, limit, rewindLimit;
   private Mode mode ;
   private Runnable callback;
   
   /**
    * Create a timer with default settings.
    * @param speed The speed of the timer (will update every 'speed' 
    * milliseconds).
    */
   public AnimationTimer( int speed )
   {
      super( "Animation Timer" );
      this.setPause( speed );
      this.time = 0;
      this.limit = 0;
      this.increment = 1;
      this.mode = Mode.STOP;
      this.rewindLimit = 0;
   }
   
   /**
    * Internal method called by the counting thread every 'speed' milliseconds.
    */
   /* Override */
   protected void run()
   {
      boolean wasAbove = time > limit;
      time += increment;
      boolean isAbove = time > limit;
      
      ZDebug.print( 7, "Increment: ", time );
     
      // Have we hit the time limit or just crossed the limit?
      if( time == limit || wasAbove != isAbove ) 
      {
         switch( mode ) {
            case STOP: {
               stopNoWait();
               time = limit;
               break;
            }
            
            case REWIND: {
               time = limit;
               increment = -increment;
               int temp = rewindLimit;
               rewindLimit = limit;
               limit = temp;
               break;
            }
            
            case LOOP: {
               time = 0;
               break;
            }
         }
      }
      
      // Run the animation callback
      if( callback != null && ZXTMPlugin.isEclipseLoaded() ) {
         SWTUtil.asyncExec( callback );
      }
   }
       
   /**
    * Stops the timer and sets the timer to 0.
    */
   public synchronized void reset()
   {
      stop();
      this.time = 0;
   }
   
   /**
    * @return Returns the increment this timer is increased by every tick.
    */
   public int getIncrement()
   {
      return increment;
   }

   /**
    * Sets the increment of this timer (the amount it increases the timer's 
    * value per tick)
    * @param increment The new increment value.
    */
   public void setIncrement( int increment )
   {
      this.increment = increment;
   }

   /**
    * @return Returns the limit of this timer
    */
   public int getLimit()
   {
      return limit;
   }

   /**
    * Sets the limit of this timer.
    * @param limit The maximum value this timer can get to before the timer 
    * stops/rewinds/resets (depending on the mode)
    */
   public void setLimit( int limit )
   {
      this.limit = limit;
   }

   /**
    * @return Returns the number of milliseconds per tick.
    */
   public int getSpeed()
   {
      return getPause();
   }

   /**
    * Sets the speed of the timer. This is the number of milliseconds per tick.
    * @param speed The number of milliseconds per tick.
    */
   public void setSpeed( int speed )
   {
      this.setPause( speed );
   }
   
   /**
    * Get the current time of the timer.
    * @return The current time of this timer.
    */
   public int getTime()
   {
      return time;
   }

   /**
    * Set the time of the timer. This will not change the state of the timer
    * (if it's running etc)
    * @param time The time value of the timer.
    */
   public void setTime( int time )
   {
      this.time = time;
   }

   /** 
    * Returns the mode this timer is in. The mode determines what happens when
    * the timer reaches its limit.
    * @return The current mode of this timer.
    */
   public Mode getMode()
   {
      return mode;
   }

   /**
    * Sets the mode of the timer. The mode determines what happens when the
    * timer reaches its limit.
    * @param mode
    */
   public void setMode( Mode mode )
   {
      this.mode = mode;
   }
   

   /**
    * Get the callback function this timer is using.
    * @return the callback
    */
   public Runnable getCallback()
   {
      return callback;
   }

   /**
    * Set this timer's callback. It is called using a SWT asyncExec. 
    * @param callback The callback to call every increment;
    */
   public void setCallback( Runnable callback )
   {
      this.callback = callback;
   }


}
