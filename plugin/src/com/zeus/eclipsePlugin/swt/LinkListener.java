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

import java.util.HashMap;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import com.zeus.eclipsePlugin.ZDebug;

/**
 * Class which listens to a SWT Link widget, and runs the specfied action when a 
 * link is clicked.
 */
public class LinkListener implements SelectionListener
{
   private HashMap<String,Runnable> linkMap = new HashMap<String,Runnable>();
   
   /**
    * Setup the link listener with the text which was used to create the link
    * and the actions which should be run.
    * @param text The text containing links.
    * @param actions The actions to run. The actions are assigned to each link 
    * in order.
    */
   public LinkListener( String text, Runnable ... actions )
   {
      int start, end;
      int count = 0;
      while( (start = text.toLowerCase().indexOf( "<a>" )) >= 0 ) {
         if( count >= actions.length ) break;
         
         end = text.toLowerCase().indexOf( "</a>" );
         if( end == -1 ) end = text.length() - 1;
         
         String linkText = text.substring( start + 3, end );
         ZDebug.print( 5, "Adding link action: ", linkText );
         
         linkMap.put( linkText, actions[count++] );
                  
         text = text.substring( end + 4 );
      }
   }

   /* Override */
   public void widgetDefaultSelected( SelectionEvent e )
   {
      widgetSelected( e );
   }

   /**
    * Match the link text to the appropriate action and run it.
    */
   /* Override */
   public void widgetSelected( SelectionEvent e )
   {
      Runnable action = linkMap.get( e.text );
      if( action != null ) action.run();
   }
   
}
