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

package com.zeus.eclipsePlugin.codedata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.zeus.eclipsePlugin.ZDebug;

/**
 * Used to load our code definition XML files. Extends the TrafficScriptVersion
 * class storing all the XMLs data.
 */
public class CodeDefinitionXML extends VersionCodeData
{
   private InputStream xmlFileStream;
   private Document doc;
      
   // Consts for all the tag names / parameters
   private static final String CODE_DATA = "codedata";
   
   private static final String KEYWORDS = "keywords";
   private static final String KEYWORD = "keyword";
   private static final String KEYWORD_NAME = "name";
   
   private static final String GROUPS = "groups";
   private static final String GROUP = "group";
   private static final String GROUP_NAME = "name";
   private static final String GROUP_DESC = "description";
   
   private static final String FUNCTIONS = "functions";
   private static final String FUNC = "func";
   private static final String FUNC_NAME = "name";
   private static final String FUNC_DESC = "description";
   private static final String FUNC_PARAM = "param";
   private static final String FUNC_PARAMS = "parameters";
   private static final String FUNC_PARAM_NAME = "name";
   private static final String FUNC_RESTRICTIONS = "restrictions";
   
   private static final String RESTRICTION = "restriction";
   private static final String RESTRICTION_TYPE = "type";
   
   private static final String PARAM_MIN = "min";
   private static final String PARAM_MAX = "max";
   private static final String PARAM_MAX_INF = "INF";
   
   /**
    * Loads the passed input stream into this version class.
    * @param xmlStream An input stream from a code data XML file.
    */
   public CodeDefinitionXML( InputStream xmlStream )
   {
      super( 5, 0 );
      this.xmlFileStream = xmlStream;
      loadFile();
   }
   
   /**
    * Read the xmlFileStream and load all the data out of it.
    */
   private void loadFile()
   {
      try {
         // Load the document into memory
         DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = fact.newDocumentBuilder();         
         doc = builder.parse( xmlFileStream );
         
         Element codeData  = getSingleElementFromNode( doc, CODE_DATA );
         
         String[] ver = codeData.getAttribute( "version" ).split( "\\." );
         setVersion( Integer.parseInt( ver[0] ), Integer.parseInt( ver[1] ) );
         
         // Keywords
         Element keywords = getSingleElementFromNode( codeData, KEYWORDS );
         setKeywords( getElementsPropertyAsArray( keywords, KEYWORD, KEYWORD_NAME ) );
         
         // Read the function groups - <groups> .. </groups>
         Node groups = getSingleElementFromNode( codeData, GROUPS );
         
         // Iterate through each group - <group>
         for( Element group : iter( groups, GROUP ) ) {
            
            // Read group info
            String groupName = group.getAttribute( GROUP_NAME );
            ZDebug.print( 3, "Group: ", groupName );
            
            String groupDesc = getTextSurroundedByNode( group, GROUP_DESC );
            ZDebug.print( 4, "Group Desc: ", groupDesc );
            
            // Create a code group object to store it
            FunctionGroup codeGroup = new FunctionGroup( groupName, groupDesc );
            addGroup( codeGroup );
            
            Element functions  = getSingleElementFromNode( group, FUNCTIONS );
            
            // Iterate through the functions in a group - <func> ... </func>
            for( Element func : iter( functions, FUNC ) ) {
               
               // Get function info
               String funcName = func.getAttribute( FUNC_NAME );
               String funcNameShort = funcName.substring( funcName.lastIndexOf( '.' ) + 1 );
               ZDebug.print( 6, "Func: ", funcNameShort );
               
               String funcDesc = getTextSurroundedByNode( func, FUNC_DESC );
               
               // Get parameter info
               Element paramsElement = getSingleElementFromNode( func, FUNC_PARAMS );
               int min = Integer.parseInt( paramsElement.getAttribute( PARAM_MIN ) );
               String maxString = paramsElement.getAttribute( PARAM_MAX );
               int max = Function.INFINITE;
               if( !maxString.equals( PARAM_MAX_INF ) ) {
                  max = Integer.parseInt( maxString );
               }
               
               // Read the names of the parameters
               String[] params = getElementsPropertyAsArray( paramsElement, FUNC_PARAM, FUNC_PARAM_NAME );
               
               // Get Restrictions
               Element restrictElement = getSingleElementFromNode( func, FUNC_RESTRICTIONS );
               String[] restrictions = getElementsPropertyAsArray( restrictElement, RESTRICTION, RESTRICTION_TYPE );
               
               // Add the function to our function reference classes
               Function codeFunc = new Function( codeGroup,
                  funcNameShort, funcDesc, min, max, restrictions, params 
               );
               codeGroup.addFunction( codeFunc );
            }
         }
         
      } catch( IOException e ) {
         ZDebug.printStackTrace( e, "Reading code data XML file failed." );
      } catch( SAXException e ) {
         ZDebug.printStackTrace( e, "Loading code data XML failed." );
      } catch( ParserConfigurationException e ) {
         ZDebug.printStackTrace( e, "Invalid XML parser configuration." );
      }
   }
   
   // Helper functions
   
   /**
    * Get the first instance of a child element in the passed in XML element.
    * @param node The XML element you're searching
    * @param name The name of the tag you're looking for.
    * @return The first XML element within the passed in element, or null if 
    * none was found.
    */
   private Element getSingleElementFromNode( Node node, String name ) 
   {
      NodeList nodes;
      if( node instanceof Document ){
         nodes = ((Document) node).getElementsByTagName( name );    
      } else {
         nodes = ((Element) node).getElementsByTagName( name );    
      }
      return (Element) nodes.item( 0 );      
   }
   
   /**
    * Get the text content of a child element of the passed element.
    * @param node The element you want to search
    * @param name The name of the element who's text content you want
    * @return The text content of the child node as a string.
    */
   private String getTextSurroundedByNode( Element node, String name )
   {
      Element e = (Element) getSingleElementFromNode( node, name );
      return e.getTextContent();
   }
   
   /**
    * Get all child nodes of a parent element, and get the values of a certain 
    * property as an array.
    * 
    * e.g. 
    * <foo>
    *   <bar param1="A" param2="Test"/>
    *   <bar param1="B" param2="Cheese"/>
    * </foo>
    * 
    * getElementsPropertyAsArray( fooElement, "bar", "param2" ) returns:
    *    { "Test", "Cheese" };
    * 
    * @param node The parent node surrounding the elements you want.
    * @param name The name of the node who's properties you want
    * @param param The parameter you want to look at
    * @return An array of the values of each parameter.
    */
   private String[] getElementsPropertyAsArray( Element node, String name, String param ) {
      LinkedList<String> list = new LinkedList<String>();
      for( Element func : iter( node, name ) ) {
         list.add( func.getAttribute( param ) );
      }
      
      return list.toArray( new String[list.size()] );
   }
   
   /**
    * Return an iteratable for each child element called 'name' in the passed 
    * node.
    * @param node The node you want to iterate over.
    * @param name The name of elements you want.
    * @return The XMLIteratable for this node.
    */
   private XMLIteratable iter( Node node, String name )
   {
      return new XMLIteratable( (Element) node, name );
   }
   
   /**
    * Magic class that allows you to iterate over elements.
    */
   private class XMLIteratable implements Iterable<Element> {
      
      private Element parent;
      private String nameMatch;
      
      public XMLIteratable( Element parent, String nameMatch )
      {
         this.parent = parent;
         this.nameMatch = nameMatch;
      }
      
      public XMLIteratable( Element parent )
      {
         this( parent, null );
      }

      /* Override */
      public Iterator<Element> iterator()
      {
         NodeList list;
         if( nameMatch != null ) {
            list = parent.getElementsByTagName( nameMatch );
         } else {
            list = parent.getChildNodes();
         }
         return new IteratorXML( list );
      }
      
      private class IteratorXML implements Iterator<Element>
      {
         private int i;
         private NodeList list;
         
         public IteratorXML( NodeList list )
         {
            this.list = list;
            i = 0;
         }

         /* Override */
         public boolean hasNext()
         {
            return list.item( i ) != null;
         }

         /* Override */
         public Element next()
         {
            return (Element) list.item( i++ );
         }

         /* Override */
         public void remove()
         {
            throw new UnsupportedOperationException();
         }
         
      }
      
   }
   
}
