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

package com.zeus.eclipsePlugin.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.zeus.eclipsePlugin.ZDebug;

/**
 * Interface that all classes in the model should implement.
 */
public abstract class ModelElement
{  
   /**
    * Element types definition.
    */
   public enum Type
   {
      CONTROLLER,
      ZXTM,
      RULE,
      JAVA_EXTENSION,
      VIRTUAL_SERVER,
      POOL,
      TRAFFIC_IP_GROUP,
      RATE_CLASS,
      BANDWIDTH_CLASS,
      SLM_CLASS
   }
   
   /**
    * Events that can occur to a ModelElement
    */
   public enum Event
   {
      /** If the internal state changes */
      CHANGED,  
      
      /** If this object has been deleted */
      DELETED,
      
      /** If this object is about to be deleted */
      PRE_DELETE,

      /** If this object has been renamed*/
      RENAMED,
   }
   
   /**
    * The different states a model element can be in.
    */
   public enum State
   {
      /** An object is in this state if it has received no information yet, or 
       * it has just recovered from a sync error. */
      WAITING_FOR_FIRST_UPDATE,
      
      /** If the object has updated successfully, it should be in this state */
      UP_TO_DATE,
      
      /** If there is a problem updating this object, it will be in this state */
      CANNOT_SYNC,
      
      /** If the object has been deleted it will be in this state. */
      DELETED,
      
      /** If this object has been disconnected */
      DISCONNECTED,
   }
   
   private LinkedList<ModelListener> listeners = new LinkedList<ModelListener>();
   private State state = State.WAITING_FOR_FIRST_UPDATE;
   private ModelException lastError;
   
   /**
    * Returns the type of this element.
    * @return Returns this element's type. Should never be null.
    */
   public abstract Type getModelType();
   
   /**
    * Returns the parent of this element.
    * @return This element's parent, or null if this is a root element.
    */
   public abstract ModelElement getModelParent();
   
   /**
    * Get a list of the current model listeners to this class. This is a 
    * 'snapshot' of the data structure used to store the listeners, so should not
    * cause ConcurentModificationExceptions if used.
    * 
    * @return A List of the current listeners.
    */
   public List<ModelListener> getListeners() 
   {
      return new ArrayList<ModelListener>( listeners );
   }
 
   /**
    * Returns the sync state of this ZXTM.
    */
   public State getModelState()
   {    
      return state;
   }  
      
   /**
    * Add a listener that gets informed about changes to this element.
    * @param listener The listener that wants to be informed of changes about
    * this element.
    */
   public void addListener( ModelListener listener )
   {
      listeners.add( listener );
   }
   
   /**
    * Remove a listener that was listening to changes on this element.
    * @param listener The listener that wants to be removed.
    */
   public void removeListener( ModelListener listener )
   {
      listeners.remove( listener );
   }
   
   /**
    * Inform all listeners of an event. All state updates should be made before 
    * this method is called.
    * @param event The event that has occurred.
    */
   protected void updateListeners( Event event ) 
   {
      ZDebug.print( 4, "updateListeners( ", event, " )" );
      for( ModelListener listener : getListeners() ) {
         ZDebug.print( 6, "Updating listener: ", listener );
         try {            
            listener.modelUpdated( this, event );
         } catch( Exception e ) {
            ZDebug.printStackTrace( e, "Listener update threw exception - ", listener );
         }
      }
   }
   
   /**
    * Inform all listeners of a new child. The child should be added to this 
    * Model's internal structures before this event is called.
    * @param child The new child.
    */
   protected void updateListenersChild( ModelElement child ) 
   {
      ZDebug.print( 4, "updateListenersChild( ", child, " )" );
      
      for( ModelListener listener : getListeners() ) {
         ZDebug.print( 6, "Updating listener: ", listener );
         try {
            listener.childAdded( this, child );
         } catch( Exception e ) {
            ZDebug.printStackTrace( e, "Listener update threw exception - ", listener );
         }
      }
   }

   /**
    * Set this element's state. This will inform listeners if there has been a 
    * state change.
    * @param state The new state.
    * @param cause The cause of the state change, only used if state is 
    * CANNOT_SYNC.
    */
   protected void setModelState( State state, ModelException cause ) 
   {
      if( state != this.state ) {
         this.state = state;
         
         if( state != State.CANNOT_SYNC ) {
            lastError = null;
         } else if( cause == null ) {
            throw new IllegalArgumentException( "If state is CANNOT_SYNC, a " +
            	"cause is required." );
         } else {
            setErrorCause( cause );
         }
         
         for( ModelListener listener : getListeners() ) {
            listener.stateChanged( this, state );
         }
      }
   }
   
   
   
   /**
    * Get the cause of the last sync failure
    * @return The cause of the last sync failure.
    */
   public ModelException getLastError()
   {
      return lastError;
   }

   /**
    * Set the state of the Model, listeners will be informed if the state has
    * changed.
    * IMPORTANT: If the state is CANNOT_SYNC a cause must be given; use the 
    * setModelState( state, cause ) method.
    * @param state The new state of the model.
    */
   protected void setModelState( State state ) 
   {
      this.setModelState( state, null );
   }
   
   /**
    * Set the cause of the last sync failure.
    * @param error The problem that caused the sync failure.
    */
   protected void setErrorCause( ModelException error )
   {
      this.lastError = error;
   }
      
}
