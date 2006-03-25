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

package searchplugin;


import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import util.ui.SearchForm;
import util.ui.SearchFormSettings;
import util.ui.SearchHelper;
import util.ui.TabLayout;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import devplugin.Plugin;
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

  /** The Caller-Plugin */
  private Plugin mPlugin;
  
  /**
   * Creates a new instance of SearchDialog.
   *
   * @param plugin The Plugin
   * @param parent The dialog's parent.
   */
  public SearchDialog(Plugin plugin, Frame parent) {
    super(parent, true);
    
    UiUtilities.registerForClosing(this);
    
    mPlugin = plugin;
    
    String msg;
    
    msg = mLocalizer.msg("dlgTitle", "Search programs");
    setTitle(msg);
    
    JPanel main = new JPanel(new TabLayout(1));
    main.setBorder(UiUtilities.DIALOG_BORDER);
    setContentPane(main);
    
    // pattern
    mSearchForm = new SearchForm(true, true);
    mSearchForm.setHistory(SearchPlugin.getSearchHistory());
    mSearchForm.addPatternActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        search();
      }
    });
    main.add(mSearchForm);

    // the buttons
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    main.add(buttonPn);
    
    msg = mLocalizer.msg("search", "Search");
    mSearchBt = new JButton(msg);
    mSearchBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        search();
      }
    });
    buttonPn.add(mSearchBt);
    getRootPane().setDefaultButton(mSearchBt);

    msg = mLocalizer.msg("cancel", "Cancel");
    mCloseBt = new JButton(msg);
    mCloseBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    });
    buttonPn.add(mCloseBt);

    pack();
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
    
    mSearchForm.setSearchFormSettings(settings);
  }

  /**
   * Starts the search.
   */  
  private void search() {
    SearchHelper.search(this, mSearchForm.getSearchFormSettings());
  }

  public void close() {
    dispose();
  }
  
}
