/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.ui.html;

import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * A HTML editor kit that supports the integration von {@link Component}s.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ExtendedHTMLEditorKit extends HTMLEditorKit {
  
  /**
   * Create an uninitialized text storage model that is appropriate for this
   * type of editor.
   * <p>
   * Copied from superclass and changed to create ExtendedHTMLDocuments.
   *
   * @return the model
   */
  public Document createDefaultDocument() {
    StyleSheet styles = getStyleSheet();
    StyleSheet ss = new StyleSheet();
    
    ss.addStyleSheet(styles);
    
    ExtendedHTMLDocument doc = new ExtendedHTMLDocument(ss);
    doc.setParser(getParser());
    doc.setAsynchronousLoadPriority(4);
    doc.setTokenThreshold(100);
    return doc;
  }


}
