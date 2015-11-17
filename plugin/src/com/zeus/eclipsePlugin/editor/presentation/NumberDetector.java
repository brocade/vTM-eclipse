/*******************************************************************************
 * Copyright (C) 2015 Brocade Communications Systems, Inc.and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://github.com/brocade/vTM-eclipse/LICENSE
 * This software is distributed "AS IS".
 *
 * Contributors:
 *     Brocade Communications Systems - Main Implementation
 *     IBM Corporation - Code snippet     
 ******************************************************************************/

package com.zeus.eclipsePlugin.editor.presentation;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * A detector that matches numbers.
 */
public class NumberDetector implements IWordDetector {

   /* Override */
   public boolean isWordPart( char c )
   {
      return Character.isDigit( c ) || c == '.' || c == 'e' || c == '-';
   }

   /* Override */
   public boolean isWordStart( char c )
   {
      return Character.isDigit( c ) || c == '-';
   }
}
