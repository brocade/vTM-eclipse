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

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * Encrypt and decrypt strings for storage on disk. This is currently only used
 * for passwords. Uses Triple DES (EDE) to encrypt the string, and stored as
 * numeric bytes separated with colons.
 */
public class Encrypter
{     
   private static Cipher encryptCipher = null;
   private static Cipher decryptCipher = null;
   
   private static final String ALGORITHM = "DESede";
   private static final String SCHEME = ALGORITHM + "/CBC/PKCS5Padding";
   private static final String STR_CHARSET = "UTF8";
   
   /**
    * Initialise the ciphers object that do the encrypting and decrypting
    */
   private static void initCiphers()
   {
      if( encryptCipher != null && decryptCipher != null ) return;
      
      try {
         byte[] keyBase = 
            "AAAABAAAABAAAAB--KEY-AAAABAAAABAAAAB".getBytes( STR_CHARSET );
         
         encryptCipher = Cipher.getInstance( SCHEME );
         decryptCipher = Cipher.getInstance( SCHEME );
         
         KeySpec keySpec = new DESedeKeySpec( keyBase );
         SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);         
         SecretKey key = keyFactory.generateSecret( keySpec );   
         
         IvParameterSpec initalVector = new IvParameterSpec( new byte[] {
            1, 2, 3, 4, 5, 6, 7, 8
         });
         
         encryptCipher.init( Cipher.ENCRYPT_MODE, key, initalVector );
         decryptCipher.init( Cipher.DECRYPT_MODE, key, initalVector );

      } catch( Exception e ) {
         encryptCipher = null;
         decryptCipher = null;
         throw new RuntimeException( "Could not init encrypter", e );
      }

   }
   
   /**
    * Encrypt a plain text string to a cipher string.
    * @param toEncrypt The string to encrypt.
    * @return The encrypted string.
    */
   public static synchronized String encrypt( String toEncrypt )
   {
      initCiphers();       
      
      try {
         byte[] bytes = encryptCipher.doFinal( toEncrypt.getBytes( STR_CHARSET ) );
                  
         return encodeBytes( bytes );
         
      } catch( Exception e ) {
         throw new RuntimeException( "Could not encrypt string", e );
      }

   }
   
   /**
    * Decrypt a encrypted string back into plain text.
    * @param toDecrypt The encrypted string
    * @return The decrypted string, plain text
    */
   public static synchronized String decrypt( String toDecrypt )
   {
      initCiphers();
      
      try {
         byte[] bytes = decodeBytes( toDecrypt );
         
         String result = new String( 
            decryptCipher.doFinal( bytes ), STR_CHARSET
         );
         
         return result;
         
      } catch( Exception e ) {
         throw new RuntimeException( "Could not decrypt string", e );
      }
   }
   
   /**
    * Encode bytes to something that can be put on disk
    * @param bytes The bytes to turn into a string
    * @return A string representation of the bytes
    */
   private static String encodeBytes( byte[] bytes )
   {
      StringBuffer buffer = new StringBuffer( 100 );
      for( byte b : bytes ) {
         if( buffer.length() > 0 ) buffer.append( ":" );
         String out = "" + ((int) (b + 256));         
         buffer.append( out );
      }
      return buffer.toString();
   }
   
   /**
    * Decode our string representation for a byte array back into bytes.
    * @param data The string representation
    * @return The bytes that were in the string.
    */
   private static byte[] decodeBytes( String data )
   {
      String[] parts = data.split( ":" );
      byte[] bytes = new byte[parts.length];
      for( int i = 0; i < parts.length; i++ ) {
         bytes[i] = (byte) (Integer.parseInt( parts[i] ) - 256);
      }
      
      return bytes;
   }
   
}