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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * A HTML document that supports the integration von {@link Component}s.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ExtendedHTMLDocument extends HTMLDocument {

  protected static final HTML.Tag COMP_TAG = new HTML.Tag("comp") {};
  
  protected ArrayList<Component> mKnownCompList;
  
  
  public ExtendedHTMLDocument(StyleSheet ss) {
    super(ss);
  }
  
  
  public String createCompTag(Component comp) {
    if (mKnownCompList == null) {
      mKnownCompList = new ArrayList<Component>();
    }
    
    int index = mKnownCompList.indexOf(comp);
    if (index == -1) {
      mKnownCompList.add(comp);
      index = mKnownCompList.size() - 1;
    }
    
    return "<"+COMP_TAG.toString()+" index=\"" + index + "\">";
  }
  
  
  public HTMLEditorKit.ParserCallback getReader(int pos) {
    return getReader(pos, 0, 0, null);
  }


  public HTMLEditorKit.ParserCallback getReader(int pos, int popDepth,
    int pushDepth, HTML.Tag insertTag)
  {
    return new ExtendedHTMLReader(pos, popDepth, pushDepth, insertTag);
  }


  protected void handleCompTag(HTML.Tag tag, MutableAttributeSet attributeSet,
    Vector<ElementSpec> parseBuffer)
  {
    if (mKnownCompList == null) {
      return;
    }
    
    String indexAsString = (String) attributeSet.getAttribute("index");
    if (indexAsString != null) {
      try {
        int index = Integer.parseInt(indexAsString);
        Component comp = mKnownCompList.get(index);
        
        addComponent(parseBuffer, comp);
      }
      catch (Exception exc) {
        exc.printStackTrace();
      }
    }
  }


  protected void addComponent(Vector<ElementSpec> parseBuffer, Component comp) {
    SimpleAttributeSet sas = new SimpleAttributeSet();
    sas.addAttribute(StyleConstants.CharacterConstants.ComponentAttribute, comp);
    sas.addAttribute(StyleConstants.NameAttribute, StyleConstants.ComponentElementName);

    ElementSpec es = new ElementSpec(sas.copyAttributes(),
                                     ElementSpec.ContentType, new char[] { ' ' },
                                     0, 1);
    parseBuffer.addElement(es);
  }
  
  
  // inner class ExtendedHTMLReader
  
  
  protected class ExtendedHTMLReader extends HTMLDocument.HTMLReader {
    
    private TagAction mCompAction;
    
    
    public ExtendedHTMLReader(int offset, int popDepth, int pushDepth,
      HTML.Tag insertTag)
    {
      super(offset, popDepth, pushDepth, insertTag);
      
      mCompAction = new TagAction() {
        public void start(HTML.Tag tag, MutableAttributeSet attributeSet) {
          Vector<ElementSpec> pBuffer = ExtendedHTMLReader.super.parseBuffer;
          handleCompTag(tag, attributeSet, pBuffer);
        }
      };
      registerTag(COMP_TAG, mCompAction);
    }


    /**
     * Callback from the parser.  Route to the appropriate
     * handler for the tag.
     */
    public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributeSet, int pos) {
      if (tag.toString().equals(COMP_TAG.toString())) {
        mCompAction.start(tag, attributeSet);
      } else {
        super.handleSimpleTag(tag, attributeSet, pos);
      }
    }

  }

}
