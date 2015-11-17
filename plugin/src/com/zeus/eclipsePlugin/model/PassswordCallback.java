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
 * Used by the ZXTM model element when it needs to query the user for a 
 * password.
 */
public interface PassswordCallback
{
   /**
    * Called when a ZXTM has no password and needs the user to input it. 
    * 
    * IMPORTANT: This method should not block while waiting for user input.
    * 
    * @param zxtm The ZXTM we need the password for.
    * @param isError True if this is an incorrect password query.
    */
   public void passwordRequired( ZXTM zxtm, boolean isError );
}
