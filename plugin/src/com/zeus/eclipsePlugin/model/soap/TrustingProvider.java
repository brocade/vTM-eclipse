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

import java.security.KeyStore;
import java.security.Provider;
import java.security.cert.X509Certificate;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

/**
 * Used to make java accept unsigned certificates
 */
public class TrustingProvider extends Provider
{

   private static final long serialVersionUID = 1L;

   public TrustingProvider()
   {
      super( "MyProvider", 1.0, "Trust certificates" );
      put(
         "TrustManagerFactory.TrustAllCertificates",
         MyTrustManagerFactory.class.getName() 
      );
   }

   public static class MyTrustManagerFactory extends TrustManagerFactorySpi
   {
      public MyTrustManagerFactory() {}
      protected void engineInit( KeyStore keystore ) {}
      protected void engineInit( ManagerFactoryParameters mgrparams ) {}
      
      protected TrustManager[] engineGetTrustManagers()
      {
         return new TrustManager[] {
            new MyX509TrustManager()
         };
      }
   }

   protected static class MyX509TrustManager implements X509TrustManager
   {
      public void checkClientTrusted( X509Certificate[] chain, String authType ) {}
      public void checkServerTrusted( X509Certificate[] chain, String authType ) {}
      
      public X509Certificate[] getAcceptedIssuers()
      {
         return null;
      }
   }

}
