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

import com.zeus.eclipsePlugin.ZLang;

/**
 * The different things that can go wrong with a Model Element.
 */
public enum ModelError
{
   CONNECTION_REFUSED   ( ZLang.ZL_ModelConnectionRefusesd  ),
   CANNOT_RESOLVE       ( ZLang.ZL_ModelCannotResolve ),
   BAD_SOAP_RESPONSE    ( ZLang.ZL_ModelBadSOAPResponse ), 
   SSL_ERROR            ( ZLang.ZL_ModelSSLError ),
   AUTH_FAILED          ( ZLang.ZL_ModelAuthorisationFailed ), 
   INTERNAL   		      ( ZLang.ZL_ModelInternalError ),
   ELEMENT_EXISTS       ( ZLang.ZL_ModelElementAlreadyExists ),
   NO_TRAFFIC_SCRIPT    ( ZLang.ZL_ModelNoTrafficScript ),
   UNKNOWN              ( ZLang.ZL_ModelUnknownError ),               
   ;
   
   private String message;
   
   /**
    * Create a ModelError with pretty text.
    * @param message The message that gets shown to users.
    */
   private ModelError( String message )
   {
      this.message = message;
   }
   
   /**
    * Get the message that should be shown to users.
    * @return The message that should be shown to users.
    */
   public String getMessage()
   {
      return message;
   }
}
