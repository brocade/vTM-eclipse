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

/**
 * Used to return the result of a Confirm dialog with a check box.
 */
public class ToggleConfirmResult
{
   private boolean toggle;
   private boolean selectedOk;
   
   /**
    * Create the result object.
    * @param toggle True if the check box (toggle) was checked.
    * @param selectedOk If the user selected OK returns true, otherwise returns
    * false.
    */
   ToggleConfirmResult( boolean toggle, boolean selectedOk )
   {
      this.toggle = toggle;
      this.selectedOk = selectedOk;
   }
   
   /**
    * Returns true if the user checked the dialogs toggle.
    * @return True if the user checked the dialogs toggle.
    */
   public boolean getToggleValue()
   {
      return toggle;
   }
   
   /**
    * Returns true if the user clicked OK on the dialog.
    * @return True if the user clicked OK on the dialog.
    */
   public boolean okSelected()
   {
      return selectedOk;
   }
   
   /**
    * Returns true if the user clicked Cancel on the dialog.
    * @return True if the user clicked Cancel on the dialog.
    */
   public boolean cancelSelected()
   {
      return !selectedOk;
   }
}
