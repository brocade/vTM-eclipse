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

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * Class that stores a set of SWT controls. 
 */
public class SWTSet
{
   protected Control[] controls;
   
   private Label label = null;
   private Button button = null;
   private Text text = null;
   private Combo combo = null;
   private List list = null;
   
   /**
    * Create the set with the specified controls.
    * @param controls The controls which make up this set.
    */
   public SWTSet( Control ... controls )
   {
      this.controls = controls;
      for( Control control : controls ) {
         if( this.label == null && control instanceof Label ) {
            this.label = (Label) control;
         }
         if( this.button == null && control instanceof Button ) {
            this.button = (Button) control;
         }
         if( this.text == null && control instanceof Text ) {
            this.text = (Text) control;
         }
         if( this.combo == null && control instanceof Combo ) {
            this.combo = (Combo) control;
         }
         if( this.list == null && control instanceof List ) {
            this.list = (List) control;
         }
      }
   }
   
   /**
    * Get the specified item in the set.
    * @param i The index of the item. Starts at 0, ends at length - 1.
    * @return The control at the specified index.
    */
   public Control item( int i ) 
   {
      return controls[i];
   }
   
   /**
    * The number of controls in this set.
    * @return The length of this set.
    */
   public int length() 
   {
      return controls.length;
   }
   
   /**
    * Make all of the controls in this set enabled or disabled.
    * @param value If true all controls are enabled, otherwise they are all 
    * disabled.
    */
   public void setEnabled( boolean value ) 
   {
      for( Control control : controls ) {
         control.setEnabled( value );
      }
   }
   
   /**
    * Get the first label in this set.
    * @return The first label in the set or null if there is none.
    */
   public Label label()
   {
      return this.label;
   }
   
   /**
    * Get the first button in this set.
    * @return The first button in the set or null if there is none.
    */
   public Button button()
   {
      return this.button;
   }
   
   /**
    * Get the first text box in this set.
    * @return The first text box in the set or null if there is none.
    */
   public Text text()
   {
      return this.text;
   }
   
   /**
    * Get the first UI list in this set.
    * @return The first UI list in the set or null if there is none.
    */
   public List list()
   {
      return this.list;
   }
   
   /**
    * Get the first drop down box in this set.
    * @return The first drop down box in the set or null if there is none.
    */
   public Combo combo()
   {
      return this.combo;
   }
   
}
