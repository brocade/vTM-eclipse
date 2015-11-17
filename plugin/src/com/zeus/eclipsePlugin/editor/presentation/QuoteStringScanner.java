package com.zeus.eclipsePlugin.editor.presentation;

import com.zeus.eclipsePlugin.editor.TrafficScriptEditor;

/**
 * Scanner that generates tokens for single quoted strings.
 */
public class QuoteStringScanner extends StringScanner
{
   /**
    * Default constructor.
    * @param editor The editor this scanner works on.
    */
   public QuoteStringScanner( TrafficScriptEditor editor )
   {
      super( editor );
   }

   /** Overrides the parent classes declaration of this method, making this 
    *  scanner work for quoted strings. */
   /* Override */
   protected boolean isQuoteString() { return true; }  

}
