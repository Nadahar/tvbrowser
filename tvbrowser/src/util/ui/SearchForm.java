/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.Border;

import devplugin.ProgramFieldType;

/**
 * A search form for searching TV data.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class SearchForm extends JPanel {

  /** The localizer of this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SearchForm.class);
  
  /** The messages for the time combo box. */  
  private static final String[] TIME_STRING_ARR = new String[] {
    mLocalizer.msg("search.14", "Naechste 14 Tagen"),
    mLocalizer.msg("search.1000", "Alle Daten"),
    mLocalizer.msg("search.0", "Heute"),
    mLocalizer.msg("search.1", "Morgen"),
    mLocalizer.msg("search.7", "Eine Woche"),
    mLocalizer.msg("search.21", "3 Wochen"),
    mLocalizer.msg("search.-7", "Letzte Woche")
  };

  /** The values for the time combo box. */  
  private static final int[] TIME_VALUE_ARR = new int[] {
    14, 1000, 0, 1, 7, 21, -7
  };

  /** The maximum length of the history */
  private static final int MAX_HISTORY_LENGTH = 15;
  
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
  private JRadioButton mMatchExactlyRB, mMatchKeywordRB, mMatchRegexRB;
  private JCheckBox mCaseSensitiveChB;
  
  private ProgramFieldType[] mUserDefinedFieldTypeArr;
  

  /**
   * Creates a new search form.
   * 
   * @param showHistory Should there be a history?
   * @param showTimeSelection Should the search time (number of days) be selectable?
   *        See {@link devplugin.PluginManager#search(String, boolean, ProgramFieldType[], Date, int, Channel[], boolean)}.
   */
  public SearchForm(boolean showHistory, boolean showTimeSelection) {
    super(new TabLayout(1));
    
    String msg;
    JPanel p1, p2;
    ButtonGroup bg;
    
    // Search term and time selection
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
    } else {
      mPatternTF = new JTextField(20);
    }
    
    if (showTimeSelection) {
      p1 = new JPanel(new BorderLayout());
      this.add(p1);
      
      p2 = new JPanel(new TabLayout(1));
      p1.add(p2, BorderLayout.CENTER);
      
      msg = mLocalizer.msg("searchTerm", "Search term");
      p2.add(new JLabel(msg));
      
      if (mPatternCB != null) {
        p2.add(mPatternCB);
      } else {
        p2.add(mPatternTF);
      }

      p2 = new JPanel(new TabLayout(1));
      p1.add(p2, BorderLayout.EAST);
      
      msg = mLocalizer.msg("period", "Period");
      p2.add(new JLabel(msg));

      mTimeCB = new JComboBox(TIME_STRING_ARR);
      p2.add(mTimeCB);
    } else {
      p1 = new JPanel(new BorderLayout());
      this.add(p1);

      msg = mLocalizer.msg("searchTerm", "Search term");
      p1.add(new JLabel(msg + "  "), BorderLayout.WEST);
      
      if (mPatternCB != null) {
        p1.add(mPatternCB, BorderLayout.CENTER);
      } else {
        p1.add(mPatternTF, BorderLayout.CENTER);
      }
    }
    
    // Search in
    bg = new ButtonGroup();
    
    p1 = new JPanel(new TabLayout(1, 2, 0));
    msg = mLocalizer.msg("searchIn", "Search in");
    p1.setBorder(BorderFactory.createTitledBorder(msg));
    this.add(p1);
    
    ActionListener updateEnabledListener = new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        updateEnabled();
      }
    };

    msg = mLocalizer.msg("onlyTitle", "Only in title");
    mSearchTitleRB = new JRadioButton(msg);
    mSearchTitleRB.setSelected(true);
    mSearchTitleRB.addActionListener(updateEnabledListener);
    p1.add(mSearchTitleRB);
    bg.add(mSearchTitleRB);

    msg = mLocalizer.msg("allFields", "All fields");
    mSearchAllRB = new JRadioButton(msg);
    mSearchAllRB.addActionListener(updateEnabledListener);
    p1.add(mSearchAllRB);
    bg.add(mSearchAllRB);
    
    p2 = new JPanel(new BorderLayout());
    p1.add(p2);
    
    msg = mLocalizer.msg("certainFields", "Certain Fields");
    mSearchUserDefinedRB = new JRadioButton(msg);
    mSearchUserDefinedRB.addActionListener(updateEnabledListener);
    p2.add(mSearchUserDefinedRB, BorderLayout.CENTER);
    bg.add(mSearchUserDefinedRB);

    msg = mLocalizer.msg("select", "Select");
    mChangeSearchFieldsBt = new JButton(msg);
    mChangeSearchFieldsBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showSelectSearchFieldsDialog();
      }
    });
    p2.add(mChangeSearchFieldsBt, BorderLayout.EAST);
    
    // options
    p1 = new JPanel(new TabLayout(1, 2, 0));
    msg = mLocalizer.msg("options", "Options");
    p1.setBorder(BorderFactory.createTitledBorder(msg));
    this.add(p1);
    
    bg = new ButtonGroup();
    msg = mLocalizer.msg("matchExactly", "Match exactly");
    mMatchExactlyRB = new JRadioButton(msg);
    bg.add(mMatchExactlyRB);
    p1.add(mMatchExactlyRB);
    
    msg = mLocalizer.msg("matchSubstring", "Term is a keyword");
    mMatchKeywordRB = new JRadioButton(msg);
    mMatchKeywordRB.setSelected(true);
    bg.add(mMatchKeywordRB);
    p1.add(mMatchKeywordRB);
    
    msg = mLocalizer.msg("matchRegex", "Term is a regular expression");
    mMatchRegexRB = new JRadioButton(msg);
    bg.add(mMatchRegexRB);
    p1.add(mMatchRegexRB);
    
    msg = mLocalizer.msg("caseSensitive", "Case sensitive");
    mCaseSensitiveChB = new JCheckBox(msg);
    p1.add(mCaseSensitiveChB);
    
    // Set the default settings
    setSearchFormSettings(new SearchFormSettings(""));
    
    updateEnabled();
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
    
    switch (settings.getMatch()) {
      case SearchFormSettings.MATCH_EXACTLY:
        mMatchExactlyRB.setSelected(true); break;
      case SearchFormSettings.MATCH_KEYWORD:
        mMatchKeywordRB.setSelected(true); break;
      case SearchFormSettings.MATCH_REGULAR_EXPRESSION:
        mMatchRegexRB.setSelected(true); break;
    }

    mCaseSensitiveChB.setSelected(settings.getCaseSensitive());
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
    
    int match;
    if (mMatchExactlyRB.isSelected()) {
      match = SearchFormSettings.MATCH_EXACTLY;
    } else if (mMatchKeywordRB.isSelected()) {
      match = SearchFormSettings.MATCH_KEYWORD;
    } else {
      match = SearchFormSettings.MATCH_REGULAR_EXPRESSION;
    }
    settings.setMatch(match);

    settings.setCaseSensitive(mCaseSensitiveChB.isSelected());
    
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
    } else {
      return mPatternTF.getText();
    }
  }
  
  
  private void setPattern(String pattern) {
    if (mPatternCB != null) {
      mPatternCB.setSelectedItem(pattern);
    } else {
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
    FieldSelectionDialog dlg = new FieldSelectionDialog(this, mUserDefinedFieldTypeArr);
    dlg.centerAndShow();
    mUserDefinedFieldTypeArr = dlg.getSelectedTypes();
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
    
    
    public void centerAndShow() {
      UiUtilities.centerAndShow(mDlg);
    }
    
    
    public ProgramFieldType[] getSelectedTypes() {
      return mSelectedTypeArr;
    }
    
  } // inner class FieldSelectionDialog

}
