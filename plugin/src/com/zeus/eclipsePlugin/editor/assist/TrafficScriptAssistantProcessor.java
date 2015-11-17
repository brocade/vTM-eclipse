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

package com.zeus.eclipsePlugin.editor.assist;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.zeus.eclipsePlugin.ImageManager;
import com.zeus.eclipsePlugin.PreferenceManager;
import com.zeus.eclipsePlugin.ZDebug;
import com.zeus.eclipsePlugin.ZLang;
import com.zeus.eclipsePlugin.ZXTMPlugin;
import com.zeus.eclipsePlugin.codedata.CodePossibility;
import com.zeus.eclipsePlugin.codedata.Function;
import com.zeus.eclipsePlugin.codedata.FunctionGroup;
import com.zeus.eclipsePlugin.codedata.VersionCodeData;
import com.zeus.eclipsePlugin.codedata.CodePossibility.Type;
import com.zeus.eclipsePlugin.consts.ImageFile;
import com.zeus.eclipsePlugin.consts.Partition;
import com.zeus.eclipsePlugin.consts.Preference;
import com.zeus.eclipsePlugin.editor.CodeLine;
import com.zeus.eclipsePlugin.editor.CodeUtil;
import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;
import com.zeus.eclipsePlugin.swt.SWTUtil;

/**
 * Controls the display of auto-complete possibilities. Is an interface used by
 * the editor's TrafficScriptAssistant.
 */
public class TrafficScriptAssistantProcessor implements IContentAssistProcessor
{
   private TrafficScriptEditor editor;  
   
   private static Type[] types = null;
   
   /**
    * Update the types of possibilities that are suggested.
    */
   public static void updateTypes()
   {
      LinkedList<Type> typeList = new LinkedList<Type>();
      if( PreferenceManager.getPreferenceBool( Preference.ASSIST_GROUP ) ) {
         typeList.add( Type.GROUP );
      } 
      if( PreferenceManager.getPreferenceBool( Preference.ASSIST_FUNC ) ) {
         typeList.add( Type.FUNCTION );
      } 
      if( PreferenceManager.getPreferenceBool( Preference.ASSIST_KEYWORDS ) ) {
         typeList.add( Type.KEYWORD );
      } 
      
      types = typeList.toArray( new Type[typeList.size()] );
   }
   
   /**
    * Get the types of possibilities we are to suggest.
    * @return An array of possibilities types that we care about.
    */
   public static Type[] getTypes()
   {
      if( types == null ) updateTypes();
      return types;
   }
      
   /**
    * Create an assistant processor for a particular editor.
    * @param editor The editor this assistant processor is calculating the 
    * possibilities for.
    */
   public TrafficScriptAssistantProcessor( TrafficScriptEditor editor )
   {
      this.editor = editor;
   }

   /**
    * This works out what word (if any) is before the current offset and 
    * returns suggestions to complete the word into functions and groups.
    */
   /* Override */
   public ICompletionProposal[] computeCompletionProposals( ITextViewer viewer,
      int offset )
   {
      ZDebug.print( 3, "computeCompletionProposals( ", viewer, ", ", offset, " )" );
      
      try {
         IDocument doc = viewer.getDocument();
         ImageManager images = ZXTMPlugin.getDefault().getImageManager();
         
         // Should we show function proposals after selecting a group?
         boolean groupShowFuncs = PreferenceManager.getPreferenceBool( 
            Preference.ASSIST_GROUP_FUNC 
         );
         
         CodeLine lineInfo = CodeUtil.getLineAreas( doc, offset );
         ZDebug.print( 7, lineInfo );
         Partition partition = lineInfo.getRegionType( offset );
         ZDebug.print( 4, "Offset Partition: ", partition );
         if( partition != Partition.CODE ) {
            return null;
         }
         
         int lastN = Math.max( 0, offset - 50 );
         String before = doc.get( lastN, offset - lastN );
                 
         int wordStart = before.length() - 1;
         while( wordStart >= 0 ) {
            char c = before.charAt( wordStart );
            if( !Character.isLetterOrDigit( c ) && c != '.' && c != '_' ) 
            {
               break;
            }
            wordStart--;
         }
         
         if( wordStart < -1 ) return null;
         
         // The word before the dot, e.g. string. == 'string'
         String lastWord = before.substring( wordStart + 1 ).trim();
         ZDebug.print( 5, "Last Word: ", lastWord );
         
         VersionCodeData codeVer = editor.getCodeDataVersion();
         
         Collection<CodePossibility> suggestions = 
            codeVer.getPossiblilities( lastWord, getTypes() );
         
         LinkedList<ICompletionProposal> list = new LinkedList<ICompletionProposal>();  
    
         for( CodePossibility suggestion : suggestions ) {
            switch( suggestion.getType() ) {
               case KEYWORD: {
                  String keyword = suggestion.getName();
                  
                  list.add( new ZCompletionProposal(
                     keyword,
                     offset - suggestion.getCurrentText().length(), 
                     suggestion.getCurrentText().length(), 
                     keyword.length(),
                     null,
                     keyword,
                     null,
                     Type.KEYWORD
                   ) );  
                  break;          
               }
               
               case GROUP: {
                  FunctionGroup group = suggestion.getFunctionGroup();
                  
                  String toInsert = group.getName();
                  if( groupShowFuncs ) {
                     toInsert += ".";
                  }
                  
                  list.add( new ZCompletionProposal(
                     toInsert,
                     offset - suggestion.getCurrentText().length(), 
                     suggestion.getCurrentText().length(), 
                     toInsert.length(),
                     images.getImage( ImageFile.GROUP ),
                     group.getName(),
                     group.getDescription(),
                     Type.GROUP
                   ) );  
                  break;
               }
               
               case FUNCTION: {
                  Function func = suggestion.getFunction();
                  String name = func.getName();
                  
                  StringBuffer replace = new StringBuffer( name );
                  StringBuffer option = new StringBuffer( name );
                  int cursor = 0;
                  
                  if( func.getParams() == null ||  func.getParams().length == 0 ) {
                     replace.append( "()" );
                     option.append( "()" );
                     cursor = name.length() + 2;
                  } else {
                     replace.append( "(  )" );
                     option.append( "( " );
                     
                     boolean comma = false;
                     for( String param : func.getParams() ) {
                        if( comma ) {
                           option.append( ", " );
                        }
                        option.append( param );                      
                        
                        comma = true;
                     }

                     option.append( " )" );
                     cursor = name.length() + 2;
                  }
                  
                  list.add( new ZCompletionProposal( 
                     replace.toString(),
                     offset - suggestion.getCurrentText().length(), 
                     suggestion.getCurrentText().length(), 
                     cursor,
                     images.getImage( ImageFile.FUNC ),
                     option.toString(),
                     func.getFullDescription(),
                     Type.FUNCTION
                  ) );   
                  
                  break;
               }
            }
         }
            
         return list.toArray( new ICompletionProposal[list.size()] );
         
      } catch( BadLocationException e ) {
         ZDebug.printStackTrace( e, "Error whilst working out code completions" );
      }
      return null;
   }

   /**
    * Context info is done by the PopupManager class, we don't use Eclipse's 
    * built in context stuff.
    */
   /* Override */
   public IContextInformation[] computeContextInformation( ITextViewer viewer,
      int offset )
   {
      return null;     
   }

   /**
    * Which characters should the suggestions auto appear for.
    */
   /* Override */
   public char[] getCompletionProposalAutoActivationCharacters()
   {
      return new char[] { '.' };
   }

   /** We don't do context stuff using Eclipse's code. */
   /* Override */
   public char[] getContextInformationAutoActivationCharacters()
   {      
      return null;
   }

   /** We don't do context stuff using Eclipse's code. */
   /* Override */
   public IContextInformationValidator getContextInformationValidator()
   {
      return null;
   }

   /**
    * The error message when we have no completions.
    */
   /* Override */
   public String getErrorMessage()
   {
      return ZLang.ZL_NoCompletionsAvailable;
   }
   
   /**
    * This is our custom Completion Proposal class. It allows us to know when 
    * the user picks a completion to insert.
    */
   private class ZCompletionProposal implements ICompletionProposal 
   {
      private CompletionProposal wrapped;
      private Type type;
      
      /**
       * Create a completion proposal with normal properties.
       * 
       * @param replacementString The string to replace
       * @param replacetmentOffset Where to replace from
       * @param replacementLength How long is the replacement
       * @param cursorPosition The cursor position after completion
       * @param image The icon to display with the proposal
       * @param displayString The string to display in the proposal
       * @param additionalProposalInfo Detailed info to be displayed in a popup.
       * @param type The type of this proposal
       */
      public ZCompletionProposal( String replacementString,
         int replacetmentOffset, int replacementLength, int cursorPosition,
         Image image, String displayString, String additionalProposalInfo, Type type )
      {
         this.wrapped = new CompletionProposal(
            replacementString,
            replacetmentOffset,
            replacementLength,
            cursorPosition,
            image,
            displayString,
            null,
            additionalProposalInfo 
         );
         
         this.type = type;
      }

      /**
       * When the completion is applied to a document, check if we want to go
       * straight into another completion.
       */
      /* Override */
      public void apply( IDocument document )
      {
         wrapped.apply( document );
         
         // If we just inserted a function group we may want to go straight into
         // showing its functions.
         if( type == Type.GROUP && 
            PreferenceManager.getPreferenceBool( Preference.ASSIST_GROUP_FUNC ) ) {

            // We need to wait a bit before showing the new completions.
            Runnable run = new Runnable() { public void run() {
               try { Thread.sleep( 100 ); } catch( Exception e ) {}
               SWTUtil.exec( new Runnable() { public void run() {
                  editor.getAssistant().showPossibleCompletions();
               } } );
            } };
        
            Thread thread = new Thread( run );
            thread.start();
         }
      }

      /* Override */
      public String getAdditionalProposalInfo()
      {
         return wrapped.getAdditionalProposalInfo();
      }

      /* Override */
      public IContextInformation getContextInformation()
      {
         return wrapped.getContextInformation();
      }

      /* Override */
      public String getDisplayString()
      {
         return wrapped.getDisplayString();
      }

      /* Override */
      public Image getImage()
      {
         return wrapped.getImage();
      }

      /* Override */
      public Point getSelection( IDocument document )
      {
         return wrapped.getSelection( document );
      }
      
   }

}
