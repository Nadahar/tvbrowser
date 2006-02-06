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

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.Border;

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
  private JRadioButton mSearcherTypeExactlyRB;
  private JRadioButton mSearcherTypeKeywordRB;
  private JRadioButton mSearcherTypeRegexRB;
  private JRadioButton mSearcherTypeBooleanRB;
  private JCheckBox mCaseSensitiveChB;
  
  private ProgramFieldType[] mUserDefinedFieldTypeArr;

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

  /**
   * Creates a new search form.
   * 
   * @param showInputfield Should there be a Input-Field? 
   * @param showHistory Should there be a history?
   * @param showTimeSelection Should the search time (number of days) be selectable?
   *        See {@link devplugin.PluginManager#search(String, boolean, ProgramFieldType[], devplugin.Date, int, devplugin.Channel[], boolean)}.
   */
  public SearchForm(boolean showInputfield, boolean showHistory, boolean showTimeSelection) {
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
    } else if (showInputfield){
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
    mSearcherTypeExactlyRB = new JRadioButton(msg);
    bg.add(mSearcherTypeExactlyRB);
    p1.add(mSearcherTypeExactlyRB);
    
    msg = mLocalizer.msg("matchSubstring", "Term is a keyword");
    mSearcherTypeKeywordRB = new JRadioButton(msg);
    mSearcherTypeKeywordRB.setSelected(true);
    bg.add(mSearcherTypeKeywordRB);
    p1.add(mSearcherTypeKeywordRB);
    
    msg = mLocalizer.msg("matchRegex", "Term is a regular expression");
    mSearcherTypeRegexRB = new JRadioButton(msg);
    bg.add(mSearcherTypeRegexRB);
    p1.add(mSearcherTypeRegexRB);
    
    LinkButton b = new LinkButton(
            "("+mLocalizer.msg("regExHelp","Help for regular expressions")+")",
            mLocalizer.msg("regExUrl","http://wiki.tvbrowser.org/index.php/Regul%C3%A4re_Ausdr%C3%BCcke"));
    b.setHorizontalAlignment(LinkButton.CENTER);
    p1.add(b);

    msg = mLocalizer.msg("matchBoolean", "Term is a boolean (with AND, OR, a.s.o.)");
    mSearcherTypeBooleanRB = new JRadioButton(msg);
    bg.add(mSearcherTypeBooleanRB);
    p1.add(mSearcherTypeBooleanRB);
    
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
  
  public boolean hasFocus() {
    if(mPatternCB != null)
      return mPatternCB.getEditor().getEditorComponent().hasFocus();
    else
      return mPatternTF.hasFocus();
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
    
    
    public void centerAndShow() {
      UiUtilities.centerAndShow(mDlg);
    }
    
    
    public ProgramFieldType[] getSelectedTypes() {
      return mSelectedTypeArr;
    }
    
  } // inner class FieldSelectionDialog

}
