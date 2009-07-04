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

package tvbrowser.extras.searchplugin;


import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import util.settings.PluginPictureSettings;
import util.ui.Localizer;
import util.ui.SearchForm;
import util.ui.SearchFormSettings;
import util.ui.SearchHelper;
import util.ui.TabLayout;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;

import devplugin.PluginManager;

/**
 * A dialog for searching programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SearchDialog extends JDialog implements WindowClosingIf {

  /** The localizer of this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SearchDialog.class);

  /** The search form to use for specifying the search criteria. */
  private SearchForm mSearchForm;
  /** The button that starts the search. */
  private JButton mSearchBt;
  /** The button that closes the dialog. */
  private JButton mCloseBt;

  /**
   * Creates a new instance of SearchDialog.
   *
   * @param parent The dialog's parent.
   */
  public SearchDialog(Window parent) {
    super(parent);
    setModal(true);
    init();
  }

  private void init() {
    UiUtilities.registerForClosing(this);

    String msg;

    msg = mLocalizer.msg("dlgTitle", "Search programs");
    setTitle(msg);

    JPanel main = new JPanel(new TabLayout(1));
    main.setBorder(Borders.DLU4_BORDER);
    setContentPane(main);

    // pattern
    mSearchForm = new SearchForm(true, true);
    mSearchForm.setHistory(SearchPlugin.getSearchHistory());
    mSearchForm.addPatternChangeListener(new DocumentListener() {

      private void updateButton(DocumentEvent e) {
        mSearchBt.setEnabled(e.getDocument().getLength() > 0);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updateButton(e);
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        updateButton(e);
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updateButton(e);
      }
    });
    mSearchForm.addPatternActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        search();
      }
    });
    main.add(mSearchForm);

    msg = mLocalizer.msg("search", "Search");
    mSearchBt = new JButton(msg);
    mSearchBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
    	if(!mSearchForm.hasFocus()) {
        search();
      }
      }
    });
    getRootPane().setDefaultButton(mSearchBt);
    
    mCloseBt = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    mCloseBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    });

    ButtonBarBuilder2 buttons = new ButtonBarBuilder2();
    buttons.addButton(new JButton[]{mSearchBt, mCloseBt});

    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(buttons.getPanel(), BorderLayout.EAST);

    main.add(buttonPanel);

    Settings.layoutWindow("extras.searchDialog", this);
  }



  /**
   * Sets the text of the pattern text field.
   *
   * @param text The new pattern text.
   */
  public void setPatternText(String text) {
    SearchFormSettings settings = new SearchFormSettings(text);
    settings.setSearchIn(SearchFormSettings.SEARCH_IN_TITLE);
    settings.setSearcherType(PluginManager.SEARCHER_TYPE_EXACTLY);
    settings.setCaseSensitive(true);

    setSearchSettings(settings);
  }
  
  public void setSearchSettings(SearchFormSettings settings) {
    mSearchForm.setSearchFormSettings(settings);
  }

  /**
   * Starts the search.
   */
  private void search() {
    final SearchFormSettings settings = mSearchForm.getSearchFormSettings();
    if (settings.getFieldTypes().length == 0) {
      String msg = mLocalizer.msg("noFields.message",
          "No search fields selected!");
      String title = mLocalizer.msg("noFields.title", "Error");
      JOptionPane.showMessageDialog(MainFrame.getInstance(), msg, title,
          JOptionPane.ERROR_MESSAGE);
      mSearchForm.focusSearchFieldButton();
    } else {
      SearchPlugin.setSearchHistory(mSearchForm.getHistory());
      SearchHelper.search(this, new PluginPictureSettings(
          PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), settings, true);
    }
  }

  public void close() {
    dispose();
  }

  protected void setSearchText(String text) {
    mSearchForm.setPattern(text);
  }

}