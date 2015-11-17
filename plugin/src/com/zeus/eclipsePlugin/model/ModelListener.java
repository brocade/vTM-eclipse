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

/**
 * Interface that should be implemented by anything wanting to be informed of
 * model changes.
 * 
 * IMPORTANT: Implementing classes should not perform any long term operations,
 * or anything that has to wait for user interaction in the implemented 
 * functions.
 */
public interface ModelListener
{
   /**
    * An element this listener is listening to has been changed in some way.
    * @param element The element that has been changed.
    * @param event The event that has happened
    */
   public void modelUpdated( ModelElement element, ModelElement.Event event );
   
   /**
    * The listened to element has had a child added to it.
    * @param parent The parent of the new child
    * @param child The new child object
    */
   public void childAdded( ModelElement parent, ModelElement child );
   
   /**
    * The elements current state has been altered in some way.
    * @param element The element that has changed
    * @param state The new state
    */
   public void stateChanged( ModelElement element, ModelElement.State state );
   
}
