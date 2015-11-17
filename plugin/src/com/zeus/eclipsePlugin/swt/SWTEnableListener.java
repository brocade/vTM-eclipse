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

package com.zeus.eclipsePlugin.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

/**
 * Listener that enables/disables a set of SWT widgets when another widget is 
 * switched on or off. E.g. an option box is deselected and a bunch of child 
 * attributes are disabled. 
 * 
 * This class must be made a listener of the master control.
 */
public class SWTEnableListener extends SelectionAdapter
{
   private SWTSet set;
   private boolean state;
   private Control control;
   
   /**
    * Setup the listener with a set of SWT controls to disable when the master
    * control is disabled. 
    * @param slaveSet The set of SWT controls to enable/disabled
    * @param initialState The controls initial state.
    * @param master The master control whose changes will enable/disable the
    * supplied SWT set.
    */
   public SWTEnableListener( SWTSet slaveSet, boolean initialState, Control master )
   {
      this.set = slaveSet;
      this.state = initialState;
      this.control = master;
      
      slaveSet.setEnabled( state );      
   }
   
   /**
    * Setup the listener with a single SWT control to disable when the master
    * control is disabled.
    * @param set The item to enable/disabled
    * @param initialState The controls initial state.
    * @param master The master control whose changes will enable/disable the
    * supplied SWT set.
    */
   public SWTEnableListener( Control slave, boolean initialState, Control master )
   {
      this( new SWTSet( slave ), initialState, master );    
   }

   /**
    * The listener callback. It updates the enabled status of the slave controls
    * when the master changes.
    */
   /* Override */
   public void widgetSelected( SelectionEvent e )
   {
      if( control instanceof Button && (control.getStyle() & ( SWT.RADIO | SWT.CHECK | SWT.TOGGLE )) != 0 ) {   
         
         if( ((Button)control).getSelection() ) {
            state = true;
         } else {
            state = false;
         }
         
      } else if( control == null || e.item  == control ) {         
         state = !state;
      }
    
      set.setEnabled( state );
   }
   
}
