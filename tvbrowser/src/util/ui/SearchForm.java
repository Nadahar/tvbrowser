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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.PluginManager;
import devplugin.ProgramFieldType;
import tvbrowser.core.Settings;

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
    Localizer.getLocalization(Localizer.I18N_TODAY),
    Localizer.getLocalization(Localizer.I18N_TOMORROW),
    mLocalizer.msg("search.7", "Next week"),
      mLocalizer.msg("search.14", "2 weeks"),
      mLocalizer.msg("search.21", "3 weeks"),
      mLocalizer.msg("search.1000", "All data")
  };

  /** The values for the time combo box. */
  private static final int[] TIME_VALUE_ARR = new int[] {
    0, 1, 7, 14, 21, -1
  };

  /** The maximum length of the history */
  private static final int MAX_HISTORY_LENGTH = 50;

  public static final int LAYOUT_HORIZONTAL = 1;
  public static final int LAYOUT_VERTICAL = 2;


  /**
   * The fields that can be used for searching. Is null until the first call of
   * {@link #getSearchableFieldTypes()}.
   */
  private static ProgramFieldType[] mSearchableFieldTypes;

  private JTextField mPatternTF;
  private JComboBox<SearchFormSettings> mPatternCB;
  private DefaultComboBoxModel<SearchFormSettings> mPatternCBModel;
  private JComboBox<String> mTimeCB;
  private JRadioButton mSearchTitleRB, mSearchAllRB, mSearchUserDefinedRB;
  private JButton mChangeSearchFieldsBt;
  private JRadioButton mSearcherTypeExactlyRB;
  private JRadioButton mSearcherTypeKeywordRB;
  private JRadioButton mSearcherTypeRegexRB;
  private JRadioButton mSearcherTypeBooleanRB;
  private JRadioButton mSearcherTypeWholeTermRB;
  private JCheckBox mCaseSensitiveChB;

  private ProgramFieldType[] mUserDefinedFieldTypeArr;
  private ProgramFieldType[] mUserDefaultFieldTypeArr;

  private FieldSelectionDialog mFieldSelectionDlg;
  private JDialog mParent;

  /**
   * Creates a new search form.
   *
   * @param showHistory Should there be a history?
   * @param showTimeSelection Should the search time (number of days) be selectable?
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
   * @param layout selection whether the form shall be laid out horizontally or vertically
   */
  public SearchForm(boolean showInputfield, boolean showHistory, boolean showTimeSelection, int layout) {
    this(showInputfield, showHistory, showTimeSelection, layout, false);
  }

  /**
   * Creates a new search form.
   *
   * @param showInputfield Should there be a Input-Field?
   * @param showHistory Should there be a history?
   * @param showTimeSelection Should the search time (number of days) be selectable?
   * @param layout selection whether the form shall be laid out horizontally or vertically
   * @param showDefaultSelection Should the user be able to set a selected field type array as default?
   * @since 3.2.1
   */
  public SearchForm(boolean showInputfield, boolean showHistory, boolean showTimeSelection, int layout, final boolean showDefaultSelection) {
    super();

    final FormLayout layoutTop = new FormLayout("default, 3dlu, fill:10dlu:grow", "");
    final FormLayout layoutSearchIn = new FormLayout("3dlu, default:grow","default, 3dlu, default, default, default");
    final FormLayout layoutOptions = new FormLayout("3dlu, default, fill:default:grow","default, 3dlu, default, default, default, default, default, default, 3dlu, default");

    final JPanel topPanel = new JPanel(layoutTop);
    final PanelBuilder searchInPanel = new PanelBuilder(layoutSearchIn);
    final PanelBuilder optionsPanel = new PanelBuilder(layoutOptions);

    final DefaultFormBuilder topBuilder = new DefaultFormBuilder(layoutTop, topPanel);
    
    ButtonGroup bg;

    if (showInputfield) {
      if (showHistory) {
        mPatternCBModel = new DefaultComboBoxModel<>();
        mPatternCB = new JComboBox<>(mPatternCBModel);
        mPatternCB.setEditable(true);
        mPatternCB.addItemListener(evt -> {
          if (evt.getStateChange() == ItemEvent.SELECTED) {
            Object selection = mPatternCB.getSelectedItem();
            if (selection instanceof SearchFormSettings) {
              setSearchFormSettings((SearchFormSettings) selection,false);
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
      mTimeCB = new JComboBox<>(TIME_STRING_ARR);
      topBuilder.append(mLocalizer.msg("period", "Period"), mTimeCB);
    }

    // Search in
    bg = new ButtonGroup();
    String msg;

    searchInPanel.addSeparator(mLocalizer.msg("searchIn", "Search in"), CC.xyw(1,1,2));

    final ActionListener updateEnabledListener = e -> {
      updateEnabled();
    };

    mSearchTitleRB = new JRadioButton(mLocalizer.msg("onlyTitle", "Only in title"));
    mSearchTitleRB.setSelected(true);
    mSearchTitleRB.addActionListener(updateEnabledListener);
    bg.add(mSearchTitleRB);
    searchInPanel.add(mSearchTitleRB, CC.xy(2,3));

    msg = mLocalizer.msg("allFields", "All fields");
    mSearchAllRB = new JRadioButton(msg);
    mSearchAllRB.addActionListener(updateEnabledListener);
    bg.add(mSearchAllRB);
    searchInPanel.add(mSearchAllRB, CC.xy(2,4));

    mSearchUserDefinedRB = new JRadioButton(mLocalizer.msg("certainFields", "Certain Fields"));
    mSearchUserDefinedRB.addActionListener(updateEnabledListener);
    bg.add(mSearchUserDefinedRB);

    mChangeSearchFieldsBt = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
    mChangeSearchFieldsBt.addActionListener(e -> {
      showSelectSearchFieldsDialog(showDefaultSelection);
    });

    JPanel panel = new JPanel(new FormLayout("pref,1dlu:grow,pref","pref"));
    panel.add(mSearchUserDefinedRB, CC.xy(1,1));
    panel.add(mChangeSearchFieldsBt, CC.xy(3,1));
    searchInPanel.add(panel, CC.xy(2,5));

    optionsPanel.addSeparator(Localizer.getLocalization(Localizer.I18N_OPTIONS), CC.xyw(1,1,3));

    mCaseSensitiveChB = new JCheckBox(mLocalizer.msg("caseSensitive", "Case sensitive"));
    optionsPanel.add(mCaseSensitiveChB, CC.xy(2,3));

    bg = new ButtonGroup();
    mSearcherTypeExactlyRB = new JRadioButton(mLocalizer.msg("matchExactly", "Match exactly"));
    bg.add(mSearcherTypeExactlyRB);
    optionsPanel.add(mSearcherTypeExactlyRB, CC.xy(2,4));
    
    mSearcherTypeWholeTermRB = new JRadioButton(mLocalizer.msg("wholeTerm", "Whole term"));
    bg.add(mSearcherTypeWholeTermRB);
    optionsPanel.add(mSearcherTypeWholeTermRB, CC.xy(2,5));
    
    mSearcherTypeKeywordRB = new JRadioButton(mLocalizer.msg("matchSubstring", "Term is a keyword"));
    mSearcherTypeKeywordRB.setSelected(true);
    bg.add(mSearcherTypeKeywordRB);
    optionsPanel.add(mSearcherTypeKeywordRB, CC.xy(2,6));
    
    mSearcherTypeBooleanRB = new JRadioButton(mLocalizer.msg("matchBoolean", "Term is a boolean (with AND, OR, a.s.o.)"));
    bg.add(mSearcherTypeBooleanRB);
    optionsPanel.add(mSearcherTypeBooleanRB, CC.xy(2,7));

    mSearcherTypeRegexRB = new JRadioButton(mLocalizer.msg("matchRegex", "Term is a regular expression"));
    bg.add(mSearcherTypeRegexRB);
    optionsPanel.add(mSearcherTypeRegexRB, CC.xy(2,8));

    final LinkButton b = new LinkButton(
            "("+mLocalizer.msg("regExHelp","Help for regular expressions")+")",
            mLocalizer.msg("regExUrl","http://wiki.tvbrowser.org/index.php/Regul%C3%A4re_Ausdr%C3%BCcke"));
    b.setHorizontalAlignment(SwingConstants.CENTER);
    optionsPanel.add(b, CC.xy(2,10));


    // Set the default settings
    setSearchFormSettings(new SearchFormSettings(""));

    updateEnabled();

    if (layout == LAYOUT_HORIZONTAL) {
      setLayout(new FormLayout("pref:grow, 3dlu, pref:grow","pref, 3dlu, top:pref"));
      add(topPanel, CC.xyw(1,1, 3));
      add(searchInPanel.getPanel(), CC.xy(1,3));
      add(optionsPanel.getPanel(), CC.xy(3,3));
    }
    else if (layout == LAYOUT_VERTICAL) {
      setLayout(new FormLayout("pref:grow", "pref, 3dlu, pref, 3dlu, pref"));
      add(topPanel, CC.xy(1,1));
      add(searchInPanel.getPanel(), CC.xy(1,3));
      add(optionsPanel.getPanel(), CC.xy(1,5));
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
      ActionListener invokeLaterListener = e -> {
        SwingUtilities.invokeLater(() -> {
          listener.actionPerformed(e);
        });
      };

      mPatternCB.getEditor().addActionListener(invokeLaterListener);
    } else {
      mPatternTF.addActionListener(listener);
    }
  }

  /**
   * Adds a DocumentListener that will be called, when the user types text in
   * the pattern text field.
   *
   * @param listener
   *          The DocumentListener to add
   * @since 3.0
   */
  public void addPatternChangeListener(final DocumentListener listener) {
    if (mPatternCB != null) {
      JTextField textField = (JTextField)mPatternCB.getEditor().getEditorComponent();
      textField.getDocument().addDocumentListener(listener);
    } else {
      mPatternTF.getDocument().addDocumentListener(listener);
    }
  }

  @Override
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
    setSearchFormSettings(settings,true);
  }
  
  /**
   * Sets the settings. These settings will be assigned to the corresponding
   * UI components.
   *
   * @param settings The settings to set.
   * @param updatePattern If the pattern text field/combo box should be updated.
   */
  public void setSearchFormSettings(SearchFormSettings settings, boolean updatePattern) {
    if(updatePattern) {
      setPattern(settings.getSearchText());
    }

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
    mUserDefaultFieldTypeArr = settings.getUserDefaultFieldTypes();

    switch (settings.getSearcherType()) {
      case PluginManager.TYPE_SEARCHER_EXACTLY:
        mSearcherTypeExactlyRB.setSelected(true); break;
      case PluginManager.TYPE_SEARCHER_WHOLE_TERM:
        mSearcherTypeWholeTermRB.setSelected(true); break;
      case PluginManager.TYPE_SEARCHER_KEYWORD:
        mSearcherTypeKeywordRB.setSelected(true); break;
      case PluginManager.TYPE_SEARCHER_REGULAR_EXPRESSION:
        mSearcherTypeRegexRB.setSelected(true); break;
      case PluginManager.TYPE_SEARCHER_BOOLEAN:
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
    settings.setUserDefaultFieldTypes(mUserDefaultFieldTypeArr);
    settings.setSearchIn(searchIn);

    int searcherType;
    if (mSearcherTypeExactlyRB.isSelected()) {
      searcherType = PluginManager.TYPE_SEARCHER_EXACTLY;
    } else if (mSearcherTypeWholeTermRB.isSelected()) {
      searcherType = PluginManager.TYPE_SEARCHER_WHOLE_TERM;
    } else if (mSearcherTypeKeywordRB.isSelected()) {
      searcherType = PluginManager.TYPE_SEARCHER_KEYWORD;
    } else if (mSearcherTypeRegexRB.isSelected()) {
      searcherType = PluginManager.TYPE_SEARCHER_REGULAR_EXPRESSION;
    } else {
      searcherType = PluginManager.TYPE_SEARCHER_BOOLEAN;
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
   * @param history The history to use.
   */
  public void setHistory(SearchFormSettings[] history) {
    if (mPatternCB != null) {
      mPatternCBModel.removeAllElements();
      if (history != null) {
        for (SearchFormSettings element : history) {
          mPatternCBModel.addElement(element);
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
    ArrayList<SearchFormSettings> list = new ArrayList<SearchFormSettings>(mPatternCBModel.getSize());

    // Get the old history
    for (int i = 0; i < mPatternCBModel.getSize(); i++) {
      Object item = mPatternCBModel.getElementAt(i);
      if (item instanceof SearchFormSettings) {
        list.add((SearchFormSettings)item);
      }
    }

    // Get the current settings
    SearchFormSettings settings = getSearchFormSettings();

    // Remove the current pattern from history if it already exists
    Iterator<SearchFormSettings> iter = list.iterator();
    while (iter.hasNext()) {
      SearchFormSettings hist = iter.next();
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


  public void setPattern(String pattern) {
    if (mPatternCB != null) {
      mPatternCB.setSelectedItem(pattern);
    } else if (mPatternTF != null){
      mPatternTF.setText(pattern);
    }
  }

  /**
   * Gets all the fields that can be used for searching. These are all fields,
   * except binaries.
   *
   * @return All searchable fields.
   */
  public static final ProgramFieldType[] getSearchableFieldTypes() {
    if (mSearchableFieldTypes == null) {
      ArrayList<ProgramFieldType> list = new ArrayList<ProgramFieldType>();
      Iterator<ProgramFieldType> iter = ProgramFieldType.getTypeIterator();
      while (iter.hasNext()) {
        ProgramFieldType type = iter.next();
        if (type.getFormat() != ProgramFieldType.FORMAT_BINARY
            && type != ProgramFieldType.PICTURE_COPYRIGHT_TYPE
            && type != ProgramFieldType.INFO_TYPE
            && type != ProgramFieldType.CUSTOM_TYPE) {
          // We can search all fields but binaries
          list.add(type);
        }
      }

      // convert to an array
      mSearchableFieldTypes = list.toArray(new ProgramFieldType[list.size()]);
    }

    // return a copy to not have clients manipulate the field
    return mSearchableFieldTypes.clone();
  }


  private void showSelectSearchFieldsDialog(boolean showDefaultSelection) {
    mFieldSelectionDlg = new FieldSelectionDialog(this, mUserDefinedFieldTypeArr, mUserDefaultFieldTypeArr, showDefaultSelection);
    mFieldSelectionDlg.centerAndShow(mParent);

    if(mParent != null) {
      mParent.requestFocus();
    }

    mUserDefinedFieldTypeArr = mFieldSelectionDlg.getSelectedTypes();
    mUserDefaultFieldTypeArr = mFieldSelectionDlg.getDefaultTypes();
  }

  /**
   *
   * @return If the SearchFields selection dialog is visible
   */
  public boolean isSearchFieldsSelectionDialogVisible() {
    return mFieldSelectionDlg != null && mFieldSelectionDlg.isVisible();
  }

  private static class FieldSelectionDialog {

    private JDialog mDlg;

    private ProgramFieldType[] mSelectedTypeArr;
    private ProgramFieldType[] mDefaultTypeArr;

    private OrderChooser<ProgramFieldType> mSelectableItemList;
    
    private JCheckBox mDefaultSelection;

    public FieldSelectionDialog(Component parent,
      ProgramFieldType[] selectedTypeArr, ProgramFieldType[] defaultTypeArr, 
      boolean showDefaultSelection)
    {
      if(selectedTypeArr == null) {
        if(defaultTypeArr == null) {
          selectedTypeArr = new ProgramFieldType[0];
        }
        else {
          selectedTypeArr = defaultTypeArr;
        }
      }

      mSelectedTypeArr = selectedTypeArr;
      mDefaultTypeArr = defaultTypeArr;

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

      mSelectableItemList = new OrderChooser<>(selectedTypeArr,getSearchableFieldTypes(),false);
      main.add(mSelectableItemList, BorderLayout.CENTER);

      
      ButtonBarBuilder buttons = new ButtonBarBuilder();
      buttons.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
      
      if(showDefaultSelection) {
        mDefaultSelection = new JCheckBox(mLocalizer.msg("showDefaultSelection", "Save selected as default"));
        buttons.addButton(mDefaultSelection);
      }
      
      buttons.addGlue();
      
      main.add(buttons.getPanel(), BorderLayout.SOUTH);

      JButton okBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
      okBt.addActionListener(e -> {
        handleOk();
      });
      mDlg.getRootPane().setDefaultButton(okBt);
      buttons.addButton(okBt);
      buttons.addRelatedGap();

      JButton cancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
      cancelBt.addActionListener(e -> {
        mDlg.dispose();
      });
      buttons.addButton(cancelBt);

      Settings.layoutWindow("searchFormFieldSelectionDlg", mDlg, new Dimension(380,320));
    }

    private void handleOk() {
      final List<ProgramFieldType> list = mSelectableItemList.getOrderList();
      mSelectedTypeArr = new ProgramFieldType[list.size()];
      
      for (int i = 0; i < list.size(); i++) {
        mSelectedTypeArr[i]=(ProgramFieldType)list.get(i);
      }
      
      if(mDefaultSelection != null && mDefaultSelection.isSelected()) {
        mDefaultTypeArr = mSelectedTypeArr;
      }

      mDlg.dispose();
    }

    public boolean isVisible() {
      return mDlg.isVisible();
    }

    public void centerAndShow(JDialog parent) {
      if(parent != null) {
        mDlg.setLocationRelativeTo(parent);
        mDlg.setVisible(true);
      } else {
        centerAndShow();
      }
    }

    public void centerAndShow() {
      UiUtilities.centerAndShow(mDlg);
    }

    public ProgramFieldType[] getSelectedTypes() {
      return mSelectedTypeArr;
    }
    
    public ProgramFieldType[] getDefaultTypes() {
      return mDefaultTypeArr;
    }

  } // inner class FieldSelectionDialog

  public void focusSearchFieldButton() {
    mChangeSearchFieldsBt.requestFocusInWindow();
  }

}
