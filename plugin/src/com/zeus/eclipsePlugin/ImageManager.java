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

import java.util.EnumSet;
import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.zeus.eclipsePlugin.consts.ImageFile;

/**
 * Controls loading of images in the plug-in. All image manipulation and loading
 * should be done through this class.
 */
public class ImageManager
{
   /** The directory that stores the images for this plug-in. */
   public static final String ZV_IMAGE_DIRECTORY = "img";
   
   private HashMap<String, Image> imagesTable = new HashMap<String, Image>();
   
   private HashMap<String, ImageDescriptor> descriptorsTable = 
      new HashMap<String, ImageDescriptor>();
   
   /**
    * Create the image manager, loading all images used by the plug-in into 
    * memory.
    */
   public ImageManager() 
   {
      for( ImageFile imageConst : EnumSet.allOf( ImageFile.class )  ) {  
         
         {
            String filename = imageConst.getFilename();
            
            ImageDescriptor desc = ZXTMPlugin.getImageDescriptor( 
               ImageManager.ZV_IMAGE_DIRECTORY + "/" + filename 
            );
            
            if( desc == null ) continue;         
            descriptorsTable.put( imageConst.toString(), desc );
            
            Image image = desc.createImage();         
            imagesTable.put( imageConst.toString(), image );
         }
         
         // Animation images
         String[] files = imageConst.getFilenames();
         if( files.length <= 1 ) continue;
         
         int i = -1;
         for( String animFile : files ) {
            i++;
            
            ImageDescriptor animDesc = ZXTMPlugin.getImageDescriptor( 
               ImageManager.ZV_IMAGE_DIRECTORY + "/" + animFile 
            );
            
            if( animDesc == null ) continue;         
            descriptorsTable.put( imageConst.toString() + " " + i, animDesc );
            
            Image animImage = animDesc.createImage();         
            imagesTable.put( imageConst.toString()  + " " + i, animImage );
         }
      }
   }
   
   /**
    * Get an image from the managers cache.
    * @param image ImageConst of the image you want.
    * @return An image object corresponding to the passed id.
    */
   public Image getImage( ImageFile image ) 
   {
      return imagesTable.get( image.toString() );
   }
   
   /**
    * Get an animation frame's image from the managers cache.
    * @param image ImageConst of the image you want.
    * @param frame The frame you want, starting at 0.
    * @return An image object corresponding to the passed id.
    */
   public Image getImage( ImageFile image, int frame ) 
   {
      return imagesTable.get( image.toString() + " " + (frame % image.getFrames()) );
   }
   
   /**
    * Get an image descriptor from the manager's cache.
    * @param image The ImageConst of the ImageDescriptor you want.
    * @return An ImageDescriptor corresponding to the id that was passed.
    */
   public ImageDescriptor getDescriptor( ImageFile image )
   {
      return descriptorsTable.get( image.toString() );
   }
   
   /**
    * Get an animation frame's image descriptor from the manager's cache.
    * @param image The ImageConst of the ImageDescriptor you want.
    * @param frame The frame you want, starting at 0.
    * @return An ImageDescriptor corresponding to the id that was passed.
    */
   public ImageDescriptor getDescriptor( ImageFile image, int frame )
   {
      return descriptorsTable.get( image.toString() + " " + (frame % image.getFrames()) );
   }
   
   /**
    * Free all images stored in the cache.
    */
   public void dispose() 
   {
      for( Image image : imagesTable.values() ) {
         image.dispose();
      }
   }
   
}
