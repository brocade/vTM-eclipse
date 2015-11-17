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

package com.zeus.eclipsePlugin.swt.dialogs;

import com.zeus.eclipsePlugin.swt.dialogs.ZDialog.DialogOption;

/**
 * Class that stores the results from a password dialog.
 */
public class PasswordResult
{
   private String user, password;
   private boolean store;
   private DialogOption option;
   
   /**
    * Create password result object.  
    * @param user The user-name entered.
    * @param password The password entered.
    * @param store Should the password be stored on disk?
    * @param option The option selected in the dialog.
    */
   public PasswordResult( String user, String password, boolean store, DialogOption option )
   {
      super();
      this.user = user;
      this.password = password;
      this.store = store;
      this.option = option;
   }
   
   public String getUserName()
   {
      return user;
   }

   public String getPassword()
   {
      return password;
   }

   public boolean getStorePassword()
   {
      return store;
   }

   public DialogOption getOption()
   {
      return option;
   }
   
}
