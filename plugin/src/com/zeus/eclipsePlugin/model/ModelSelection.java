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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

import com.zeus.eclipsePlugin.ZUtil;

/**
 * Used for returning a set of selected model elements.
 */
public class ModelSelection implements IStructuredSelection
{
   private LinkedList<ZXTM> zxtmList;
   private LinkedList<Rule> ruleList;
   private LinkedList<ModelElement> elementList;
   
   /**
    * This can be used to create a ModelSelection from a generic structured 
    * selection. If the passed in StructuredSelection is a model selection,
    * then it is just returned.
    * @param other The selection you want to turn into a ModelSelection
    * @return A model selection containing all the ZXTMs/rules that the
    * passed in selection did.
    */
   public static ModelSelection createFromOtherSelection( IStructuredSelection other )
   {
      if( other instanceof ModelSelection ) {
         return (ModelSelection) other;
      } else {
         if( other == null ) {
            return new ModelSelection();
         }
         
         Object[] selection = other.toArray(); 
         if( selection.length < 1 ) {
            return new ModelSelection();
         }
         
         LinkedList<ZXTM> zxtmList = new LinkedList<ZXTM>();
         LinkedList<Rule> ruleList = new LinkedList<Rule>();
         
         for( int i = 0; i < selection.length; i++ ) {
            Object data = selection[i];
            if( data instanceof ModelElement ) {
               ModelElement element = (ModelElement) data;
               
               switch( element.getModelType() ) 
               {
                  case ZXTM: zxtmList.add( (ZXTM) element ); break;
                  case RULE: ruleList.add( (Rule) element ); break;
               }
            }
         }
         
         return new ModelSelection( zxtmList, ruleList );         
      }
   }
   
   /**
    * Create an empty model selection
    */
   public ModelSelection()
   {
      zxtmList = new LinkedList<ZXTM>();
      ruleList = new LinkedList<Rule>();
      elementList = new LinkedList<ModelElement>();
   }
   
   /**
    * Create a model selection with the passed in list of rules and zxtms.
    * @param zxtmList The ZXTMs in this selection.
    * @param ruleList The rules in this selection.
    */
   public ModelSelection( LinkedList<ZXTM> zxtmList, LinkedList<Rule> ruleList )
   {
      this.zxtmList = zxtmList;
      this.ruleList = ruleList;
      this.elementList = new LinkedList<ModelElement>( zxtmList );
      elementList.addAll( ruleList );
   }
   
   /**
    * Return all selected ZXTMs.
    * @return All selected ZXTMs.
    */
   public Collection<ZXTM> getSelectedZXTMs()
   {
      return zxtmList;
   }
   
   /**
    * Return all selected Rules
    * @return All selected rules.
    */
   public Collection<Rule> getSelectedRules()
   {
      return ruleList;
   }
   /**
    * Get a combined set of all ModelElements in this selection.
    * @return All ModelElements.
    */
   public Collection<ModelElement> getElements()
   {
      return elementList;
   }
   
   /**
    * Is this selection only rules?
    * @return Returns true if this selection contains only rules. If selection
    * is empty it always returns false.
    */
   public boolean isOnlyRules() {
      return ( !isEmpty() && ruleList.size() == elementList.size() );
   }
   
   /**
    * Is this selection only ZXTMs?
    * @return Returns true if this selection contains only ZXTMs. If selection
    * is empty it always returns false.
    */
   public boolean isOnlyZXTMs() {
      return ( !isEmpty() && zxtmList.size() == elementList.size() );
   }
   
   /**
    * Does this selection contain only one ZXTM (and no other elements)
    * @return Returns true if there is only one ZXTM element and nothing else.
    */
   public boolean isOnlyOneZXTM()
   {
      return isOnlyZXTMs() && zxtmList.size() == 1;
   }
   
   /**
    * Is nothing selected at all?
    * @return Returns true if there are no elements in this selection.
    */
   public boolean isEmpty()
   {
      return elementList.isEmpty();
   }
   
   /**
    * Returns the total amount of elements in this selection.
    * @return
    */
   public int getSize()
   {
      return elementList.size();
   }
   
   /**
    * Get the first selected ZXTM. Order is that provided when the class was 
    * constructed.
    * @return The first ZXTM, or null if no ZXTMs are selected.
    */
   public ZXTM firstZXTM()
   {
      return zxtmList.getFirst();
   }
   
   /**
    * Get the first selected rule. Order is that provided when the class was 
    * constructed.
    * @return The first rule, or null if no rules are selected.
    */
   public Rule firstRule()
   {
      return ruleList.getFirst();
   }
   
   /**
    * Get the single selected ZXTM. If a single ZXTM is selected, that is 
    * returned. If a set of rules is selected, and they all share a single 
    * ZXTM parent, that parent is returned. Otherwise returns null. 
    * @return The selected ZXTM, or null if more or less than one is selected.
    */
   public ZXTM getSelectedZXTM()
   {
      ZXTM zxtm = null;
      if( isOnlyRules() ) {
         for( Rule rule : ruleList ) {
            ZXTM parentZXTM = (ZXTM) rule.getModelParent();
            
            if( zxtm != null && !parentZXTM.equals( zxtm ) ) {
               zxtm = null;
               break;
            }
            
            zxtm = parentZXTM;
         }
      } else if( isOnlyOneZXTM() ) { 
         zxtm = firstZXTM();
      }
      
      return zxtm;
   }
   

   /* Override */
   public Object getFirstElement()
   {
      return elementList.getFirst();
   }

   @SuppressWarnings("unchecked")
   /* Override */
   public Iterator iterator()
   {
      return elementList.iterator();
   }

   /* Override */
   public int size()
   {
      return elementList.size();
   }

   /* Override */
   public Object[] toArray()
   {
      return elementList.toArray();
   }

   @SuppressWarnings("unchecked")
   /* Override */
   public List toList()
   {
      return elementList;
   }

   /** Prints the contents of the selection. */
   /* Override */
   public String toString()
   {
      return "Selection - ZXTMs: " + ZUtil.join( ", ",  zxtmList ) + " " +
      	    "Rules: " + ZUtil.join( ", ", ruleList );
   }
   
   

}
