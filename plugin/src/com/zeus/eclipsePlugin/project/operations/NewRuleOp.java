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

package com.zeus.eclipsePlugin.project.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.model.ModelException;
import com.zeus.eclipsePlugin.model.ZXTM;
import com.zeus.eclipsePlugin.swt.EmptyMonitor;

/**
 * Operation that creates a new empty rule.
 */
public class NewRuleOp extends WorkspaceModifyOperation
{
   private ZXTM zxtm;
   private String name;
   
   /**
    * Setup the new rule operation with the specified destination ZXTM and rule
    * name. 
    * @param zxtm The ZXTM to add the rule to.
    * @param name The name of the new rule.
    */
   public NewRuleOp( ZXTM zxtm, String name )
   {
      this.zxtm = zxtm;
      this.name = name;
   }

   /**
    * Creates the new rule on the ZXTM.
    */
   /* Override */
   protected void execute( IProgressMonitor monitor ) throws CoreException,
      InvocationTargetException, InterruptedException
   {
      if( monitor == null ) monitor = new EmptyMonitor();
      monitor.beginTask( ZLang.bind( ZLang.ZL_AddingNewRuleTo, zxtm ), 1 );
      monitor.subTask( "" );
      
      try {
         zxtm.addRule( name );         
      } catch( ModelException e ) {
         throw new WorkbenchException( e.getLocalizedMessage(), e );
      }     
      
      monitor.worked( 1 );
      monitor.done();
   }

}
