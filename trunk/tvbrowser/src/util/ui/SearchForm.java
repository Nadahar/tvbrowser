/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
package util.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.Border;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.factories.DefaultComponentFactory;

import devplugin.PluginManager;
import devplugin.ProgramFieldType;

/**
 * A search form for searching TV listings.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class SearchForm extends JPanel {

  /** The localizer of this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SearchForm.class);
  
  /** The messages for the time combo box. */  
  private static final String[] TIME_STRING_ARR = new String[] {
    mLocalizer.msg("search.-7", "Letzte Woche"),
    mLocalizer.msg("search.0", "Heute"),
    mLocalizer.msg("search.1", "Morgen"),
    mLocalizer.msg("search.7", "Eine Woche"),
    mLocalizer.msg("search.14", "Naechste 14 Tagen"),
    mLocalizer.msg("search.21", "3 Wochen"),
    mLocalizer.msg("search.1000", "Alle Daten")
  };

  /** The values for the time combo box. */  
  private static final int[] TIME_VALUE_ARR = new int[] {
    -7, 0, 1, 7, 14, 21, 1000
  };

  /** The maximum length of the history */
  private static final int MAX_HISTORY_LENGTH = 15;

  public static final int LAYOUT_HORIZONTAL = 1;
  public static final int LAYOUT_VERTICAL = 2;

  
  /**
   * The fields that can be used for searching. Is null until the first call of
   * {@link #getSearchableFieldTypes()}.
   */
  private static ProgramFieldType[] mSearchableFieldTypes;
  
  private JTextField mPatternTF;
  private JComboBox mPatternCB;
  private DefaultComboBoxModel mPatternCBModel;
  private JComboBox mTimeCB;
  private JRadioButton mSearchTitleRB, mSearchAllRB, mSearchUserDefinedRB;
  private JButton mChangeSearchFieldsBt;
  private JRadioButton mSearcherTypeExactlyRB;
  private JRadioButton mSearcherTypeKeywordRB;
  private JRadioButton mSearcherTypeRegexRB;
  private JRadioButton mSearcherTypeBooleanRB;
  private JCheckBox mCaseSensitiveChB;
  
  private ProgramFieldType[] mUserDefinedFieldTypeArr;
  
  private FieldSelectionDialog mFieldSelectionDlg;
  private JDialog mParent;

  /**
   * Creates a new search form.
   * 
   * @param showHistory Should there be a history?
   * @param showTimeSelection Should the search time (number of days) be selectable?
   *        See {@link devplugin.PluginManager#search(String, boolean, ProgramFieldType[], devplugin.Date, int, devplugin.Channel[], boolean)}.
   */
  public SearchForm(boolean showHistory, boolean showTimeSelection) {
    this(true, showHistory, showTimeSelection);
  }

  public SearchForm(boolean showInputfield, boolean showHistory, boolean showTimeSelection) {
    this(showInputfield, showHistory, showTimeSelection, LAYOUT_VERTICAL);
  }

  /**
   * Creates a new search form.
   * 
   * @param showInputfield Should there be a Input-Field? 
   * @param showHistory Should there be a history?
   * @param showTimeSelection Should the search time (number of days) be selectable?
   *        See {@link devplugin.PluginManager#search(String, boolean, ProgramFieldType[], devplugin.Date, int, devplugin.Channel[], boolean)}.
   */
  public SearchForm(boolean showInputfield, boolean showHistory, boolean showTimeSelection, int layout) {
    super();

    FormLayout layoutTop = new FormLayout("pref, 3dlu, fill:pref:grow", "");
    FormLayout layoutSearchIn = new FormLayout("3dlu, pref, fill:pref:grow","pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
    FormLayout layoutOptions = new FormLayout("3dlu, pref, fill:pref:grow","pref, 3dlu, pref, 3dlu, pref,3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");

    JPanel topPanel = new JPanel(layoutTop);
    JPanel searchInPanel = new JPanel(layoutSearchIn);
    JPanel optionsPanel = new JPanel(layoutOptions);

    DefaultFormBuilder topBuilder = new DefaultFormBuilder(layoutTop, topPanel);

    CellConstraints cc = new CellConstraints();

    
    ButtonGroup bg;
    
    if (showInputfield) {
      if (showHistory) {
        mPatternCBModel = new DefaultComboBoxModel();
        mPatternCB = new JComboBox(mPatternCBModel);
        mPatternCB.setEditable(true);
        mPatternCB.addItemListener(new ItemListener() {
          public void itemStateChanged (ItemEvent evt) {
            if (evt.getStateChange() == ItemEvent.SELECTED) {
              Object selection = mPatternCB.getSelectedItem();
              if (selection instanceof SearchFormSettings) {
                setSearchFormSettings((SearchFormSettings) selection);
              }
            }
          }
        });
        topBuilder.append(mLocalizer.msg("searchTerm", "Search term"), mPatternCB);

      } else {
        mPatternTF = new JTextField(20);
        topBuilder.append(mLocalizer.msg("searchTerm", "Search term"), mPatternTF);
      }
    }

    if (showTimeSelection) {
      mTimeCB = new JComboBox(TIME_STRING_ARR);
      topBuilder.append(mLocalizer.msg("period", "Period"), mTimeCB);
    }
    
    // Search in
    bg = new ButtonGroup();
    String msg;
    
    searchInPanel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("searchIn", "Search in")), cc.xyw(1,1,3));

    ActionListener updateEnabledListener = new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        updateEnabled();
      }
    };

    mSearchTitleRB = new JRadioButton(mLocalizer.msg("onlyTitle", "Only in title"));
    mSearchTitleRB.setSelected(true);
    mSearchTitleRB.addActionListener(updateEnabledListener);
    bg.add(mSearchTitleRB);
    searchInPanel.add(mSearchTitleRB, cc.xy(2,3));
    
    msg = mLocalizer.msg("allFields", "All fields");
    mSearchAllRB = new JRadioButton(msg);
    mSearchAllRB.addActionListener(updateEnabledListener);
    bg.add(mSearchAllRB);
    searchInPanel.add(mSearchAllRB, cc.xy(2,5));

    mSearchUserDefinedRB = new JRadioButton(mLocalizer.msg("certainFields", "Certain Fields"));
    mSearchUserDefinedRB.addActionListener(updateEnabledListener);
    bg.add(mSearchUserDefinedRB);

    mChangeSearchFieldsBt = new JButton(mLocalizer.msg("select", "Select"));
    mChangeSearchFieldsBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showSelectSearchFieldsDialog();
      }
    });

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(mSearchUserDefinedRB, BorderLayout.CENTER);
    panel.add(mChangeSearchFieldsBt, BorderLayout.EAST);
    searchInPanel.add(panel, cc.xy(2,7));

    optionsPanel.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("options", "Options")), cc.xyw(1,1,3));

    mCaseSensitiveChB = new JCheckBox(mLocalizer.msg("caseSensitive", "Case sensitive"));
    optionsPanel.add(mCaseSensitiveChB, cc.xy(2,3));

    bg = new ButtonGroup();
    mSearcherTypeExactlyRB = new JRadioButton(mLocalizer.msg("matchExactly", "Match exactly"));
    bg.add(mSearcherTypeExactlyRB);
    optionsPanel.add(mSearcherTypeExactlyRB, cc.xy(2,5));
    
    mSearcherTypeKeywordRB = new JRadioButton(mLocalizer.msg("matchSubstring", "Term is a keyword"));
    mSearcherTypeKeywordRB.setSelected(true);
    bg.add(mSearcherTypeKeywordRB);
    optionsPanel.add(mSearcherTypeKeywordRB, cc.xy(2,7));
    
    mSearcherTypeBooleanRB = new JRadioButton(mLocalizer.msg("matchBoolean", "Term is a boolean (with AND, OR, a.s.o.)"));
    bg.add(mSearcherTypeBooleanRB);
    optionsPanel.add(mSearcherTypeBooleanRB, cc.xy(2,9));

    mSearcherTypeRegexRB = new JRadioButton(mLocalizer.msg("matchRegex", "Term is a regular expression"));
    bg.add(mSearcherTypeRegexRB);
    optionsPanel.add(mSearcherTypeRegexRB, cc.xy(2,11));
    
    LinkButton b = new LinkButton(
            "("+mLocalizer.msg("regExHelp","Help for regular expressions")+")",
            mLocalizer.msg("regExUrl","http://wiki.tvbrowser.org/index.php/Regul%C3%A4re_Ausdr%C3%BCcke"));
    b.setHorizontalAlignment(LinkButton.CENTER);
    optionsPanel.add(b, cc.xy(2,13));

    
    // Set the default settings
    setSearchFormSettings(new SearchFormSettings(""));
    
    updateEnabled();

    if (layout == LAYOUT_HORIZONTAL) {
      setLayout(new FormLayout("pref, 3dlu, pref","pref, 3dlu, top:pref"));
      add(topPanel, cc.xyw(1,1, 3));
      add(searchInPanel, cc.xy(1,3));
      add(optionsPanel, cc.xy(3,3));
    }
    else if (layout == LAYOUT_VERTICAL) {
      setLayout(new FormLayout("pref:grow", "pref, 3dlu, pref, 3dlu, pref"));
      add(topPanel, cc.xy(1,1));
      add(searchInPanel, cc.xy(1,3));
      add(optionsPanel, cc.xy(1,5));
    }
    else {
      throw new IllegalArgumentException("invalid layout type: "+layout);
    }

  }
  
  public void setParentDialog(JDialog parent) {
    mParent = parent;
  }
  
  /**
   * Adds an ActionListener that will be called, when the user presses Enter
   * in the pattern text field.
   * 
   * @param listener The ActionListener to add
   */
  public void addPatternActionListener(final ActionListener listener) {
    if (mPatternCB != null) {
      // Workaround: mPatternCB.getSelectedItem() will return the last selection
      //             until the editing is finished. The editor finishes editing
      //             after he has called all the listeners.
      //             To ensure that editing is finished we call our listener,
      //             after the editor is done  
      ActionListener invokeLaterListener = new ActionListener() {
        public void actionPerformed(final ActionEvent evt) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              listener.actionPerformed(evt);
            }
          });
        }
      };
      
      mPatternCB.getEditor().addActionListener(invokeLaterListener);
    } else {
      mPatternTF.addActionListener(listener);
    }
  }
  
  public boolean hasFocus() {
    if(mPatternCB != null) {
      return mPatternCB.getEditor().getEditorComponent().hasFocus();
    }
    else if (mPatternTF != null) {
      return mPatternTF.hasFocus();
    }
    else {
      return false;
    }
  }

  /**
   * Sets the settings. These settings will be assigned to the corresponding
   * UI components.
   * 
   * @param settings The settings to set.
   */
  public void setSearchFormSettings(SearchFormSettings settings) {
    setPattern(settings.getSearchText());
    
    switch (settings.getSearchIn()) {
      case SearchFormSettings.SEARCH_IN_TITLE:
        mSearchTitleRB.setSelected(true); break;
      case SearchFormSettings.SEARCH_IN_ALL:
        mSearchAllRB.setSelected(true); break;
      case SearchFormSettings.SEARCH_IN_USER_DEFINED:
        mSearchUserDefinedRB.setSelected(true);
        updateEnabled();
        break;
    }

    mUserDefinedFieldTypeArr = settings.getUserDefinedFieldTypes();
    
    switch (settings.getSearcherType()) {
      case PluginManager.SEARCHER_TYPE_EXACTLY:
        mSearcherTypeExactlyRB.setSelected(true); break;
      case PluginManager.SEARCHER_TYPE_KEYWORD:
        mSearcherTypeKeywordRB.setSelected(true); break;
      case PluginManager.SEARCHER_TYPE_REGULAR_EXPRESSION:
        mSearcherTypeRegexRB.setSelected(true); break;
      case PluginManager.SEARCHER_TYPE_BOOLEAN:
        mSearcherTypeBooleanRB.setSelected(true); break;
    }

    mCaseSensitiveChB.setSelected(settings.getCaseSensitive());
    
    setNrDays(settings.getNrDays());
  }
  
  
  /**
   * Gets the settings from the corresponding UI components.
   * 
   * @return The settings the user made.
   */
  public SearchFormSettings getSearchFormSettings() {
    SearchFormSettings settings = new SearchFormSettings(getPattern());
    
    int searchIn;
    if (mSearchTitleRB.isSelected()) {
      searchIn = SearchFormSettings.SEARCH_IN_TITLE;
    } else if (mSearchAllRB.isSelected()) {
      searchIn = SearchFormSettings.SEARCH_IN_ALL;
    } else {
      searchIn = SearchFormSettings.SEARCH_IN_USER_DEFINED;
      settings.setUserDefinedFieldTypes(mUserDefinedFieldTypeArr);
    }
    settings.setSearchIn(searchIn);
    
    int searcherType;
    if (mSearcherTypeExactlyRB.isSelected()) {
      searcherType = PluginManager.SEARCHER_TYPE_EXACTLY;
    } else if (mSearcherTypeKeywordRB.isSelected()) {
      searcherType = PluginManager.SEARCHER_TYPE_KEYWORD;
    } else if (mSearcherTypeRegexRB.isSelected()) {
      searcherType = PluginManager.SEARCHER_TYPE_REGULAR_EXPRESSION;
    } else {
      searcherType = PluginManager.SEARCHER_TYPE_BOOLEAN;
    }
    settings.setSearcherType(searcherType);

    settings.setCaseSensitive(mCaseSensitiveChB.isSelected());
    
    settings.setNrDays(getNrDays());
    
    return settings;
  }
  
  
  /**
   * Sets the history. The first item of the history will automatically be
   * assigned
   * 
   * @param history
   */
  public void setHistory(SearchFormSettings[] history) {
    if (mPatternCB != null) {
      mPatternCBModel.removeAllElements();
      if (history != null) {
        for (int i = 0; i < history.length; i++) {
          mPatternCBModel.addElement(history[i]);
        }
      }
      
      mPatternCB.getEditor().selectAll();
      
      updateEnabled();
    }
  }
  
  
  /**
   * Gets the history. The returned history will already contain the current
   * settings.
   * 
   * @return The history.
   */
  public SearchFormSettings[] getHistory() {
    ArrayList list = new ArrayList(mPatternCBModel.getSize());
    
    // Get the old history
    for (int i = 0; i < mPatternCBModel.getSize(); i++) {
      Object item = mPatternCBModel.getElementAt(i);
      if (item instanceof SearchFormSettings) {
        list.add(item);
      }
    }

    // Get the current settings
    SearchFormSettings settings = getSearchFormSettings();
    
    // Remove the current pattern from history if it already exists
    Iterator iter = list.iterator();
    while (iter.hasNext()) {
      SearchFormSettings hist = (SearchFormSettings) iter.next();
      if (settings.getSearchText().equals(hist.getSearchText())) {
        iter.remove();
      }
    }
    
    // Add the current settings to the history
    list.add(0, settings);
    
    // Ensure that the history is not longer that MAX_HISTORY_LENGTH
    while (list.size() > MAX_HISTORY_LENGTH) {
      list.remove(list.size() - 1);
    }
    
    // Convert the list into an array
    SearchFormSettings[] history = new SearchFormSettings[list.size()];
    list.toArray(history);
    
    // Set this history for the case the form reused after a search
    setHistory(history);
    
    return history;
  }
  
  
  /**
   * Gets the selected number of days to use for searching.
   * 
   * @return The selected number of days
   */
  public int getNrDays() {
    if (mTimeCB != null) {
      return TIME_VALUE_ARR[mTimeCB.getSelectedIndex()];
    } else {
      return -1;
    }
  }


  /**
   * Sets the number of days to use for searching.
   * 
   * @param nrDays The number of days
   */
  public void setNrDays(int nrDays) {
    if (mTimeCB != null) {
      for (int i = 0; i < TIME_VALUE_ARR.length; i++) {
        if (nrDays == TIME_VALUE_ARR[i]) {
          mTimeCB.setSelectedIndex(i);
          break;
        }
      }
    }
  }


  private void updateEnabled() {
    mChangeSearchFieldsBt.setEnabled(mSearchUserDefinedRB.isSelected());
  }
  
  
  private String getPattern() {
    if (mPatternCB != null) {
      return mPatternCB.getSelectedItem().toString();
    } else if (mPatternTF != null){
      return mPatternTF.getText();
    }
    return "";
  }
  
  
  private void setPattern(String pattern) {
    if (mPatternCB != null) {
      mPatternCB.setSelectedItem(pattern);
    } else if (mPatternTF != null){
      mPatternTF.setText(pattern);
    }
  }

  
  /**
   * Gets all the fields that can be used for searching. This are all fields,
   * exept binaries. 
   * 
   * @return All searchable fields.
   */
  public static ProgramFieldType[] getSearchableFieldTypes() {
    if (mSearchableFieldTypes == null) {
      ArrayList list = new ArrayList();
      Iterator iter = ProgramFieldType.getTypeIterator();
      while (iter.hasNext()) {
        ProgramFieldType type = (ProgramFieldType) iter.next();
        if (type.getFormat() != ProgramFieldType.BINARY_FORMAT) {
          // We can search all fields but binaries
          list.add(type);
        }
      }
      
      // convert to an array
      mSearchableFieldTypes = new ProgramFieldType[list.size()];
      list.toArray(mSearchableFieldTypes);
    }
    
    return mSearchableFieldTypes;
  }


  private void showSelectSearchFieldsDialog() {
    mFieldSelectionDlg = new FieldSelectionDialog(this, mUserDefinedFieldTypeArr);
    mFieldSelectionDlg.centerAndShow(mParent);
    
    if(mParent != null)
      mParent.requestFocus();
    
    mUserDefinedFieldTypeArr = mFieldSelectionDlg.getSelectedTypes();
  }
  
  /**
   * 
   * @return If the SearchFields selection dialog is visible
   */
  public boolean isSearchFieldsSelectionDialogVisible() {
    return mFieldSelectionDlg != null && mFieldSelectionDlg.isVisible();
  }
  
  private class FieldSelectionDialog {
    
    private JDialog mDlg;
    
    private ProgramFieldType[] mSelectedTypeArr;

    private ProgramFieldType[] mTypeArr;

    private JCheckBox[] mTypeChBArr;
    
    
    public FieldSelectionDialog(Component parent,
      ProgramFieldType[] selectedTypeArr)
    {
      mSelectedTypeArr = selectedTypeArr;
      
      String msg;
      
      mDlg = UiUtilities.createDialog(parent, true);
      msg = mLocalizer.msg("chooseSearchFields", "Choose search fields");
      mDlg.setTitle(msg);
      
      UiUtilities.registerForClosing(new WindowClosingIf() {

        public void close() {
          mDlg.dispose();
        }

        public JRootPane getRootPane() {
          return mDlg.getRootPane();
        }
        
      });

      JPanel main = new JPanel(new BorderLayout());
      main.setBorder(UiUtilities.DIALOG_BORDER);
      mDlg.setContentPane(main);

      msg = mLocalizer.msg("chooseSearchFieldHelp",
        "Please select the fields to search for");
      main.add(UiUtilities.createHelpTextArea(msg + "\n"), BorderLayout.NORTH);
      
      JPanel checkBoxPn = new JPanel(new TabLayout(1));
      checkBoxPn.setBackground(Color.WHITE); // For the JList-feeling
      
      mTypeArr = getSearchableFieldTypes();
      mTypeChBArr = new JCheckBox[mTypeArr.length];
      Border border = BorderFactory.createEmptyBorder(0, 2, 0, 2);
      for (int i = 0; i < mTypeArr.length; i++) {
        mTypeChBArr[i] = new JCheckBox(mTypeArr[i].getLocalizedName());
        mTypeChBArr[i].setOpaque(false);
        mTypeChBArr[i].setFocusPainted(false);
        mTypeChBArr[i].setBorder(border);
        mTypeChBArr[i].setSelected(contains(selectedTypeArr, mTypeArr[i]));
        
        checkBoxPn.add(mTypeChBArr[i]);
      }
      
      JScrollPane scrollPane = new JScrollPane(checkBoxPn);
      scrollPane.getVerticalScrollBar().setUnitIncrement(30);
      main.add(scrollPane, BorderLayout.CENTER);
      
      JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
      main.add(buttonPn, BorderLayout.SOUTH);
      
      msg = mLocalizer.msg("ok", "OK");
      JButton okBt = new JButton(msg);
      okBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          handleOk();
        }
      });
      mDlg.getRootPane().setDefaultButton(okBt);
      buttonPn.add(okBt);

      msg = mLocalizer.msg("cancel", "Cancel");
      JButton cancelBt = new JButton(msg);
      cancelBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          mDlg.dispose();
        }
      });
      buttonPn.add(cancelBt);
      
      mDlg.setSize(250, 300);
    }
    
    
    private boolean contains(ProgramFieldType[] typeArr, ProgramFieldType type) {
      if (typeArr == null) {
        // null means: Select none
        return false;
      } else {
        for (int i = 0; i < typeArr.length; i++) {
          if (typeArr[i] == type) {
            return true;
          }
        }

        return false;
      }
    }


    private void handleOk() {
      ArrayList list = new ArrayList();
      for (int i = 0; i < mTypeChBArr.length; i++) {
        if (mTypeChBArr[i].isSelected()) {
          list.add(mTypeArr[i]);
        }
      }
      
      mSelectedTypeArr = new ProgramFieldType[list.size()];
      list.toArray(mSelectedTypeArr);
      
      mDlg.dispose();
    }
    
    public boolean isVisible() {
      return mDlg.isVisible();
    }
    
    public void centerAndShow(JDialog parent) {
      if(parent != null) {
        mDlg.setLocationRelativeTo(parent);
        mDlg.setVisible(true);
      }
      else
        centerAndShow();
    }
    
    public void centerAndShow() {
      UiUtilities.centerAndShow(mDlg);
    }
    
    public ProgramFieldType[] getSelectedTypes() {
      return mSelectedTypeArr;
    }
    
  } // inner class FieldSelectionDialog

}
