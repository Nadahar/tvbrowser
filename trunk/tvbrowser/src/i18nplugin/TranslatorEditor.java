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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package i18nplugin;

import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
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
  /**
   * node representing the current properties class
   */
  private PropertiesNode mPropertiesNode;
  /**
   * key of the currently displayed property
   */
  private String mCurrentPropertyKey;
  private JLabel mState;

  private PropertiesEntryNode mPropertiesEntryNode;

  private JLabel mIcon;
  
  public TranslatorEditor(Locale locale) {
    mCurrentLocale = locale;
    createGui();
  }

  private void createGui() {
    setLayout(new FormLayout("fill:min:grow", "fill:min:grow, pref"));
    CellConstraints cc = new CellConstraints();

    // Top
    PanelBuilder topPanel = new PanelBuilder(new FormLayout("fill:10dlu:grow", "pref, 5dlu, fill:pref:grow")); 
    topPanel.setBorder(Borders.DLU4_BORDER);
    topPanel.addSeparator(mLocalizer.msg("original","Original text"), cc.xy(1,1));

    mOriginal = new JTextArea();
    mOriginal.setWrapStyleWord(false);
    mOriginal.setEditable(false);
    
    topPanel.add(new JScrollPane(mOriginal), cc.xy(1,3));
    
    // Bottom
    PanelBuilder bottomPanel = new PanelBuilder(new FormLayout("fill:10dlu:grow", "pref, 5dlu, fill:pref:grow")); 
    bottomPanel.setBorder(Borders.DLU4_BORDER);
    bottomPanel.addSeparator(mLocalizer.msg("translation","Translation"), cc.xy(1,1));
    mTranslation = new JTextArea();
    mTranslation.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        updateState();
      }
      public void insertUpdate(DocumentEvent e) {
        updateState();
      }
      public void removeUpdate(DocumentEvent e) {
        updateState();
      }});
    mOriginal.setBackground(mTranslation.getBackground());
    mOriginal.setForeground(mTranslation.getForeground());
    
    bottomPanel.add(new JScrollPane(mTranslation), cc.xy(1,3));

    // Splitpane
    
    final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    split.setBorder(null);
    
    split.setTopComponent(topPanel.getPanel());
    split.setBottomComponent(bottomPanel.getPanel());
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        split.setDividerLocation(0.5);
      }
    });
    
    add(split, cc.xy(1,1));
    
    // translation state
    PanelBuilder panel = new PanelBuilder(new FormLayout("pref, 2dlu, fill:10dlu:grow", "pref, 3dlu, pref")); 
    panel.setBorder(Borders.DLU4_BORDER);
    panel.addSeparator(mLocalizer.msg("state","State"), cc.xyw(1, 1, 3));
    mIcon = new JLabel();
    panel.add(mIcon, cc.xy(1, 3));
    mState = new JLabel("-");
    panel.add(mState, cc.xy(3, 3));

    add(panel.getPanel(), cc.xy(1,2));
  }

  /**
   * Set the selected Property-Node
   * 
   * @param node selected Node
   */
  public void setSelectedProperties(PropertiesEntryNode node) {
    mPropertiesEntryNode = node;
    mPropertiesNode = (PropertiesNode) node.getParent();
    mCurrentPropertyKey = node.getPropertyName();
    updateText();
  }
  
  /**
   * Update the Text
   */
  public void updateText() {
    if (mPropertiesNode != null) {
      String original = mPropertiesNode.getPropertyValue(mCurrentPropertyKey);
      mOriginal.setText(original);
      mOriginal.setCaretPosition(0);
      
      String translated = mPropertiesNode.getPropertyValue(mCurrentLocale, mCurrentPropertyKey);
      mTranslation.setText(translated);
      mTranslation.setCaretPosition(0);
      
      updateState();
    }
  }

  private void updateState() {
    String original = mOriginal.getText();
    String translated = mTranslation.getText();
    String text = "";
    int state = mPropertiesEntryNode.getTranslationState(original, translated);
    if (state == LanguageNodeIf.STATE_MISSING_TRANSLATION) {
      text = mLocalizer.msg("state.missingTranslation", "Missing translation");
    }
    else if (state == LanguageNodeIf.STATE_NON_WELLFORMED_ARG_COUNT) {
      text = mLocalizer.msg("state.wrongArgCount", "Wrong number of arguments");
    }
    else if (state == LanguageNodeIf.STATE_NON_WELLFORMED_ARG_FORMAT) {
      text = mLocalizer.msg("state.differentArguments", "Different message arguments");
    }
    else if (state == LanguageNodeIf.STATE_NON_WELLFORMED_PUNCTUATION_END) {
      text = mLocalizer.msg("state.wrongPunctuation", "Different punctuation at the end");
    }
    else if (state == LanguageNodeIf.STATE_OK) {
      text = mLocalizer.msg("state.ok", "OK");
    }
    
    mState.setText(text);
    mIcon.setVisible(state != LanguageNodeIf.STATE_OK);
    if (state != LanguageNodeIf.STATE_OK) {
      if (state == LanguageNodeIf.STATE_MISSING_TRANSLATION) {
        mIcon.setIcon(UiUtilities.scaleIcon(UIManager.getIcon("OptionPane.errorIcon"),16));
      }
      else {
        mIcon.setIcon(UiUtilities.scaleIcon(UIManager.getIcon("OptionPane.warningIcon"),16));
      }
    }
  }

  /**
   * Set the Locale
   * @param locale new Locale
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