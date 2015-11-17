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

/**
 * An exception that is thrown by the model. It indicates what problem occurred 
 * when trying to alter or update the model.
 */
public class ModelException extends Exception
{
   private static final long serialVersionUID = 1L;
   
   private ModelError error;
   private String additional;
   private ModelElement source;
   
   /**
    * Create a ModelException with additional information and a source exception.
    * @param source The ModelElement that is this problem happened to, can be 
    * null if this is not known or the element no longer exists.

    * @param additional Additional information on what happened.
    * @param e The exception that caused this problem
    */
   public ModelException( ModelElement source, ModelError error, String additional, Exception e )
   {
      super( createFullMessage( error, additional ), e ); 
      this.source = source;
      this.error = error;
      this.additional = additional;
   }
   
   /**
    * Create a ModelException with additional information.
    * @param source The ModelElement that is this problem happened to, can be 
    * null if this is not known or the element no longer exists.
    * @param error The type of error that occurred.
    * @param additional Additional information on what happened.
    */
   public ModelException( ModelElement source, ModelError error, String additional )
   {
      this( source, error, additional, null );
   }
   
   /**
    * Create a ModelException with a source exception.
    * @param source The ModelElement that is this problem happened to, can be 
    * null if this is not known or the element no longer exists.
    * @param error The type of error that occurred.
    * @param e The exception that caused this problem
    */
   public ModelException( ModelElement source, ModelError error, Exception e )
   {
      this( source, error, null, e );
   }
   
   /**
    * Create a ModelException with no additional info.
    * @param source The ModelElement that is this problem happened to, can be 
    * null if this is not known or the element no longer exists.
    * @param error The type of error that occurred.
    */
   public ModelException( ModelElement source, ModelError error )
   {
      this( source, error, null, null );
   }
   
   /**
    * Generates the exceptions message from the passed constructor parameters.
    * @param error The type of problem
    * @param additional Additional information about the exception.
    * @return The full message for the exception.
    */
   private static String createFullMessage( ModelError error, String additional )
   {
      if( additional == null ) {
         additional = "";
      } else {
         additional = " " + additional;
      }
      
      return error.getMessage() + additional;
   }

   /**
    * Get the type of error.
    * @return The ModelError object
    */
   public ModelError getError()
   {
      return error;
   }

   /**
    * Return any additional information to do with this problem.
    * @return the additional
    */
   public String getAdditional()
   {
      return additional;
   }

   /**
    * Return the Model Element that threw this exception
    * @return The source of this exception.
    */
   public ModelElement getSource()
   {
      return source;
   }
}
