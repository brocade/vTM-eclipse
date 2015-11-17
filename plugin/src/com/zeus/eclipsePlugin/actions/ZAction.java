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

package com.zeus.eclipsePlugin.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.Action;

import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.consts.Command;
import com.zeus.eclipsePlugin.consts.ImageFile;

/**
 * This is a super class for our combined action and command handler extensions.
 */
public abstract class ZAction extends Action implements IHandler
{
   /** The wrapped handler class, we delegate most the IHandler methods to this.*/
   private Handler handler = new Handler();
   
   /**
    * Constructor sets the action definition if a Command is set.
    */
   protected ZAction()
   {
      Command command = getCommand();
      if( command != null ) {
         this.setActionDefinitionId( command.getId() );
      }      
   }
   
   /**
    * This is the main run command that is shared by the action and handler
    * interfaces.
    * @param event If the handler has been called, this will be the passed 
    * execution invent. Null otherwise.
    */
   protected abstract void run( ExecutionEvent event ); 
   
   /**
    * The command that the subclass is a handler for, or null if this class does
    * not handler a command.
    * @return The command this class handles, or null if it doesn't handle a 
    * command.
    */
   protected abstract Command getCommand();
   
   /**
    * A convenience method that sets this actions image descriptor using
    * a image from our ImageManager.
    * @param image The image to associate with this action.
    */
   public void setImageFile( ImageFile image )
   {
      this.setImageDescriptor( 
         ZXTMPlugin.getDefault().getImageManager().getDescriptor( image )
      );
   }
   
   /**
    * This is the run command for the action interface. It calls the main run
    * method.
    */
   /* Override */
   public final void run()
   {
      run( null );
   }
      
   /** This is passed directly to the wrapped handler class. */
   /* Override */
   public void addHandlerListener( IHandlerListener handlerListener )
   {
      handler.addHandlerListener( handlerListener );
   }
   
   /** This is passed directly to the wrapped handler class. */
   /* Override */
   public void dispose()
   {
      handler.dispose();
   }
   
   /** This is passed directly to the wrapped handler class. */
   /* Override */
   public Object execute( ExecutionEvent event ) throws ExecutionException
   {
      return handler.execute( event );
   }

   /** This is passed directly to the wrapped handler class. */
   /* Override */
   public void removeHandlerListener( IHandlerListener handlerListener )
   {
      handler.removeHandlerListener( handlerListener );
   }

   /**
    * The wrapped handler class sub class. It's execute implementation
    * runs the run method of the parent class.
    */
   private class Handler extends AbstractHandler
   {
      /* Override */
      public Object execute( ExecutionEvent event ) throws ExecutionException
      {
         ZDebug.print( 4, "Action execute via handler" );
         ZDebug.print( 6, "Event: ", event );
         ZAction.this.run(  event );
         return null;
      }      
   }
}
