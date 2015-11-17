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

package com.zeus.eclipsePlugin.consts;

/**
 * This lists all the images included with the plug-in.
 * 
 * IMPORTANT: This should match the contents of the 'img' directory.
 */
public enum ImageFile
{   
   ZXTM             ( "zxtm.png" ),
   ZXTM_ERROR       ( "zxtm-error.png" ),
   ZXTM_GREY        ( "zxtm-grey.png" ),
   ZXTM_ADD         ( "zxtm-add.png" ),
   RULE             ( "rule.png" ),
   RULE_ADD         ( "rule-add.png" ),
   RULE_WARN        ( "rule-warn.png" ),
   RULE_ERROR       ( "rule-error.png" ),
   RULE_RB          ( "rule-rb.png" ),
   RULE_RB_ERROR    ( "rule-rb-error.png" ),
   RULE_RB_WARN     ( "rule-rb-warn.png" ),
   COPY             ( "copy.png" ),
   CUT              ( "cut.png" ),
   PASTE            ( "paste.png" ),
   DELETE           ( "delete.png" ),
   FUNC             ( "function.png" ),   
   GROUP            ( "group.png" ),   
   
   SPINNY           ( "spinny-1.png", "spinny-2.png", "spinny-3.png", 
                      "spinny-4.png", "spinny-5.png", "spinny-6.png", 
                      "spinny-7.png", "spinny-8.png" )
   ;
   
   private String[] files;
   
   /**
    * Create a ImageFile constant, with a path to the image. If this is an 
    * animation, give all the frames (each a separate image) in order.
    * @param files The path to this image file. If this is an animation give all 
    * the frames in order.
    */
   private ImageFile( String ... files )
   {
      this.files = files;
   }

   /**
    * The image file. Should be an image in the 'img' directory of this plug-in.
    * @return The filename of this image. If this is an animation you should 
    * probably use getFilename( frame ).
    */
   public String getFilename()
   {
      return files[0];
   }
   
   /**
    * Get a image for a particular frame if this is an animation. 
    * @param frame The frame you want the file for.
    * @return The filename corresponding to the passed frame.
    */
   public String getFilename( int frame ) 
   {
      return files[ frame % files.length ];
   }
   
   /**
    * This gets all image files associated with this image. If this is not an 
    * animation, just returns a single file, otherwise all frames in the 
    * animation.
    * @return The files associated with this image.
    */
   public String[] getFilenames()
   {
      return files;
   }
   
   /**
    * Returns the number of frames in this image.
    * @return The number of frames in this image. 
    */
   public int getFrames()
   {
      return files.length;
   }
}
