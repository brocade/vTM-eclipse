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

package com.zeus.eclipsePlugin.model.soap;

import java.util.LinkedList;
import java.util.PriorityQueue;

import com.zeus.eclipsePlugin.BackgroundThread;
import com.zeus.eclipsePlugin.PreferenceManager;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;

import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;

/**
 * This class continually updates the SOAP model in the background (separate 
 * thread). 
 * 
 * It picks the next thing to update based on its type, how long ago
 * it was last updated, and if it is being viewed (e.g. a TrafficScript rule
 * being edited).
 */
public class SOAPUpdater extends BackgroundThread
{
   private PriorityQueue<SOAPObject> queue = new PriorityQueue<SOAPObject>();
 
   /**
    * Create a SOAP updater. The name should be the parent object of this 
    * updater.
    * @param name The name of the class that manages this updater.
    */
   public SOAPUpdater( String name ) 
   {
      super( "SOAP Updater " + name );
   }
   
   /**
    * Add a SOAP object to update constantly.
    * @param object The soap object you want to update.
    */
   public synchronized void add( SOAPUpdatable object )
   {
      ZDebug.print( 6, "Adding object to queue: ", object );
      queue.add( new SOAPObject( object ) );
   }
   
   /**
    * Remove all objects that equal() the passed in object.
    * @param object The object to stop updating.
    */
   public synchronized void remove( SOAPUpdatable object )
   {
      ZDebug.print( 6, "Removing object to queue: ", object );
      LinkedList<SOAPObject> toRemove = new LinkedList<SOAPObject>();
      
      for( SOAPObject obj : queue ) {
         if( obj.getObject().equals( object ) ) {
            toRemove.add( obj );
         }
      }
      
      for( SOAPObject obj : toRemove ) {
         queue.remove( obj );
      }
   }
   
   /**
    * Gets the next thing to update.
    * @return The next soap object to update, based on priority.
    */
   protected synchronized SOAPUpdatable poll()
   {
      SOAPObject obj =  queue.poll();
      if( obj != null ) {
         return obj.getObject();
      } else {
         return null;
      }
   }
   
   /**
    * Main thread method. Waits for ZXTM to finish loading, then continuously
    * updates 
    */
   /* Override */
   public void run()
   {
      // Wait for the workbench to be ready
      while( !ZXTMPlugin.isEclipseLoaded()  ) {
         setPause( 1000 );
         return;
      }
      
      SOAPUpdatable obj = null;
      try {
         // Get a object from the queue
         obj = poll();
         if( obj == null ) {
            setPause( 500 );
            return;
         }
         
         // Update the object
         ZDebug.print( 5, "Updating object: ", obj );
         boolean keepMe = obj.updateFromZXTM();     
         
         // If the update function returns true, re-add the object to the
         // queue
         if( keepMe ) {
            add( obj ); // Re-add to the queue
         }
         
       // We are probably stopping
      } catch( RuntimeException e ) {
         ZDebug.printStackTrace( e, "SOAPUpdater error whilst updating", obj );
         if( obj != null ) add( obj );
      }
      
      setPause( PreferenceManager.getPreferenceInt( Preference.SOAP_RATE ) );
   }    
      
   /**
    * Wraps around a SOAP updateable.
    */
   private class SOAPObject implements Comparable<SOAPObject>
   {
      private long timeAdded;
      private SOAPUpdatable obj;
                 
      /**
       * Create the object, stores the time it was created
       * @param obj The SOAPUpdatable this SOAPObject wraps 
       */
      public SOAPObject( SOAPUpdatable obj )
      {
         this.timeAdded = System.currentTimeMillis() / 100;
         this.obj = obj;
      }

      /**
       * Uses the priority of the object to compare.
       */
      /* Override */
      public int compareTo( SOAPObject o )
      {          
         return o.getAdjustedPriority() - this.getAdjustedPriority();
      }
      
      /**
       * Calculates a objects priority based on the time it was added and the 
       * base priority of the object type. Rules also get a big bonus if they
       * are open in an editor.
       * @return The priority as an integer.
       */
      public int getAdjustedPriority()
      {
         long timeNow = System.currentTimeMillis() / 100;
         
         int bonusForBeingOpen = 0;
         
         if( obj instanceof SOAPRule ) {
            SOAPRule rule = (SOAPRule) obj;
            if( TrafficScriptEditor.getEditorForRuleQuick( rule ) != null ) {
               ZDebug.print( 9, "Bonus for being open: ", rule );
               bonusForBeingOpen = 40;
            }
         }
         
         return (int) (( timeNow - timeAdded ) + obj.getPriority() + bonusForBeingOpen);
      }

      /** Get the wrapped SOAPUpdatable */
      public SOAPUpdatable getObject()
      {
         return obj;
      }      
   }
   
}
