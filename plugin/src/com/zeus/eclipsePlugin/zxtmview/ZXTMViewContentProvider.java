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

package com.zeus.eclipsePlugin.zxtmview;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.zeus.eclipsePlugin.ZDebug;

import com.zeus.eclipsePlugin.model.ModelController;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.ZXTM;

/**
 * Used as an interface between the model and the tree that displays the model
 * in the UI.
 */
public class ZXTMViewContentProvider implements ITreeContentProvider
{
   /** Just calls getElements() */
   /* Override */
   public Object[] getChildren( Object parentElement )
   {
      return getElements( parentElement );
   }

   /** 
    * Uses the ModelElement method getModelParent. Assumes everything is
    * a ModelElement. 
    */
   /* Override */
   public Object getParent( Object element )
   {
      return ((ModelElement) element).getModelParent();
   }

   /**
    * Uses getElements() to get the array, and then checks if it is empty.
    */
   /* Override */
   public boolean hasChildren( Object element )
   {      
      return getElements( element ).length > 0;
   }

   /**
    * Gets the child elements of the specified element.
    */
   /* Override */
   public Object[] getElements( Object inputElement )
   {
      ZDebug.print( 4, "getElements( ", inputElement, " )" );
      
      switch( ((ModelElement) inputElement).getModelType() ) {
         
         case CONTROLLER: 
            return ((ModelController) inputElement).getSortedZXTMs();                    
        
         case ZXTM: 
            return ((ZXTM) inputElement).getRules();                    

         default: return new Object[0];
      }
      
   }

   /** Does nothing. */
   /* Override */ public void dispose() {}

   /** Not implemented. */
   /* Override */
   public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
   {
      ZDebug.print( 6, "inputChanged() -  ZXTMViewContentProvider" );
   }

}
