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

package com.zeus.eclipsePlugin.editor.presentation;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

/**
 * Detector for whitespace
 */
public class WhiteSpaceDetector implements IWhitespaceDetector
{
   /* Override */
   public boolean isWhitespace( char c )
   {
      return Character.isWhitespace( c );
   }

}
