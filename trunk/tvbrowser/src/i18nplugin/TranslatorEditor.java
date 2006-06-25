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
 *     $Date: 2006-06-05 21:02:43 +0200 (Mo, 05 Jun 2006) $
 *   $Author: darras $
 * $Revision: 2466 $
 */
package i18nplugin;

import java.util.Locale;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import util.ui.Localizer;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the Editor-Component. It shows the original text and a input field
 * for the translation.
 * 
 * @author bodum
 */
public class TranslatorEditor extends JPanel {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TranslatorEditor.class);

  private JTextArea mOriginal;
  private JTextArea mTranslation;
  private Locale mCurrentLocale;
  private PropertiesNode mPropertiesNode;
  private String mCurrentPropertyKey;
  
  public TranslatorEditor(Locale locale) {
    mCurrentLocale = locale;
    createGui();
  }

  private void createGui() {
    setLayout(new FormLayout("fill:min:grow", "fill:min:grow"));
    CellConstraints cc = new CellConstraints();
    
    // Top
    JPanel topPanel = new JPanel(new FormLayout("fill:10dlu:grow", "pref, 5dlu, fill:pref:grow")); 
    topPanel.setBorder(Borders.DLU4_BORDER);
    topPanel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("original","Original text")), cc.xy(1,1));

    mOriginal = new JTextArea();
    mOriginal.setWrapStyleWord(false);
    mOriginal.setEditable(false);
    
    topPanel.add(new JScrollPane(mOriginal), cc.xy(1,3));
    
    // Bottom
    JPanel bottomPanel = new JPanel(new FormLayout("fill:10dlu:grow", "pref, 5dlu, fill:pref:grow")); 
    bottomPanel.setBorder(Borders.DLU4_BORDER);
    bottomPanel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("translation","Translation")), cc.xy(1,1));

    mTranslation = new JTextArea();
    mOriginal.setBackground(mTranslation.getBackground());
    mOriginal.setForeground(mTranslation.getForeground());
    bottomPanel.add(new JScrollPane(mTranslation), cc.xy(1,3));

    // Splitpane :
    
    final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    
    split.setTopComponent(topPanel);
    split.setBottomComponent(bottomPanel);
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        split.setDividerLocation(0.5);
      };
    });
    
    add(split, cc.xy(1,1));
  }

  /**
   * Set the selected Property-Node
   * 
   * @param node selected Node
   */
  public void setSelectedProperties(PropertiesEntryNode node) {
    mPropertiesNode = (PropertiesNode) node.getParent();
    mCurrentPropertyKey = node.getPropertyName();
    updateText();
  }
  
  /**
   * Update the Text
   */
  public void updateText() {
    if (mPropertiesNode != null) {
      mOriginal.setText(mPropertiesNode.getPropertyValue(mCurrentPropertyKey));
      mOriginal.setCaretPosition(0);
      
      mTranslation.setText(mPropertiesNode.getPropertyValue(mCurrentLocale, mCurrentPropertyKey));
      mTranslation.setCaretPosition(0);
    }
  }

  /**
   * Set the Locale
   */
  public void setCurrentLocale(Locale locale) {
    mCurrentLocale = locale;
    updateText();
  }
  
  /**
   * Save the current state in the Property 
   */
  public void save() {
    if (mCurrentPropertyKey != null) {
      String text =  mTranslation.getText();
      if (text.length() == 0) {
        mPropertiesNode.setPropertyValue(mCurrentLocale, mCurrentPropertyKey,null);
      } else {
        mPropertiesNode.setPropertyValue(mCurrentLocale, mCurrentPropertyKey,text);
      }
    }
  }
}