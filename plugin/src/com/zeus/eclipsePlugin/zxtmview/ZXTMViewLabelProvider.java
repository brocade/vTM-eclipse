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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.zeus.eclipsePlugin.ImageManager;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.ImageFile;
import com.zeus.eclipsePlugin.model.ModelElement;
import com.zeus.eclipsePlugin.model.Rule;
import com.zeus.eclipsePlugin.model.RuleProblem;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.model.ModelElement.Type;
import com.zeus.eclipsePlugin.project.ZXTMProject;
import com.zeus.eclipsePlugin.swt.AnimationTimer;

/**
 * Interface between the Model and ZXTM Viewer for providing labels and icons
 * for each element in the viewers tree.
 */
public class ZXTMViewLabelProvider implements ILabelProvider
{
   private AnimationTimer timer;
   
   /**
    * Creates the label provider.
    * @param timer The animation timer that controls the spinny animations when
    * an element is waiting for first sync.
    */
   public ZXTMViewLabelProvider( AnimationTimer timer )
   {
      this.timer = timer;
   }

   /**
    * Get an appropriate image for the specified ModelElement.
    */
   /* Override */
   public Image getImage( Object element )
   {
   
      ImageManager imageManager = ZXTMPlugin.getDefault().getImageManager();
      ModelElement modElement = (ModelElement) element;

      switch( modElement.getModelType() ) {

         case ZXTM: {
            switch ( modElement.getModelState() ) {
               case CANNOT_SYNC: return imageManager.getImage( ImageFile.ZXTM_ERROR );
               case WAITING_FOR_FIRST_UPDATE: 
                  return imageManager.getImage( ImageFile.SPINNY, timer.getTime() );
               case DISCONNECTED: return imageManager.getImage( ImageFile.ZXTM_GREY );
               default: return imageManager.getImage( ImageFile.ZXTM );             
            }
            
         }
          
         case RULE: {        
            Rule rule = (Rule) modElement;
            
            // If this is a RuleBuilder rule show the red icons.
            if( rule.isRulebuilder() ) {
               switch ( modElement.getModelState() ) {
                  case CANNOT_SYNC:
                     return imageManager.getImage( ImageFile.RULE_RB_ERROR );
                  case WAITING_FOR_FIRST_UPDATE: 
                     return imageManager.getImage( ImageFile.SPINNY, timer.getTime() );
                  default: {
                     return imageManager.getImage( ImageFile.RULE_RB );                     
                  }
               }
            
            // Otherwise this is a normal rule. Show the black icons.
            } else {
               switch ( modElement.getModelState() ) {
                  case CANNOT_SYNC:
                     return imageManager.getImage( ImageFile.RULE_ERROR );
                  case WAITING_FOR_FIRST_UPDATE: 
                     return imageManager.getImage( ImageFile.SPINNY, timer.getTime() );
                  default: {
                     boolean hasWarnings = false, hasErrors = false;
                     for( RuleProblem error : rule.getCodeErrors() ) {
                        if( error.isWarning() ) {
                           hasWarnings = true;
                        } else {
                           hasErrors = true;
                           break;
                        }
                     }
                     
                     if( hasErrors ) {
                        return imageManager.getImage( ImageFile.RULE_ERROR ); 
                     } else if( hasWarnings ) {
                        return imageManager.getImage( ImageFile.RULE_WARN ); 
                     }
                     
                     return imageManager.getImage( ImageFile.RULE );                     
                  }
               }
            }

         }

         default: return null;
      }

   }

   /**
    * Get the label for the element. This defaults to using toString() unless 
    * its a ZXTM, which also adds its project in brackets at the end.
    */
   /* Override */
   public String getText( Object element )
   {     
      ModelElement modElement = (ModelElement) element;

      if( modElement.getModelType() == Type.ZXTM ) {
         ZXTM zxtm = (ZXTM) modElement;
         IProject project = ZXTMProject.getProjectForZXTM( zxtm );
         if( project != null ) {
            return element.toString() + " (" + project.getName() + ")";
         }
      }
      
      return element.toString();
   }

   // The rest isn't implemented as its unneeded.
   /* Override */ public void addListener( ILabelProviderListener listener ) {}
   /* Override */ public void dispose() {}
   /* Override */ public void removeListener( ILabelProviderListener listener ) {}

   /* Override */
   public boolean isLabelProperty( Object element, String property )
   {
      return false;
   }



   

}
