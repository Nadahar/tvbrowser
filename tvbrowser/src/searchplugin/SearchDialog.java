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


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.ProgramList;
import util.ui.SearchForm;
import util.ui.SearchFormSettings;
import util.ui.TabLayout;
import util.ui.UiUtilities;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * A dialog for searching programs.
 *
 * @author Til Schneider, www.murfman.de
 */
public class SearchDialog extends JDialog {

  /** The localizer of this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SearchDialog.class);
  
  private SearchForm mSearchForm;
  private JButton mSearchBt, mCloseBt;



  /**
   * Creates a new instance of SearchDialog.
   *
   * @param parent The dialog's parent.
   */
  public SearchDialog(Frame parent) {
    super(parent,true);
    
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
    settings.setMatch(SearchFormSettings.MATCH_EXACTLY);
    settings.setCaseSensitive(true);
    
    mSearchForm.setSearchFormSettings(settings);
  }

  
  
  /**
   * Starts the search.
   */  
  private void search() {
    SearchPlugin.setSearchHistory(mSearchForm.getHistory());
    
    SearchFormSettings settings = mSearchForm.getSearchFormSettings();

    String regex = settings.getSearchTextAsRegex();
    boolean caseSensitive = settings.getCaseSensitive();
    ProgramFieldType[] fieldArr = settings.getFieldTypes();
    devplugin.Date startDate = new devplugin.Date();
    int nrDays = mSearchForm.getNrDays();
    
    try {
      Program[] programArr = Plugin.getPluginManager().search(regex,
        caseSensitive, fieldArr, startDate, nrDays, null, true);
	  
      if (programArr.length == 0) {
        String msg = mLocalizer.msg("nothingFound",
          "No programs found with {0}!", settings.getSearchText());
        JOptionPane.showMessageDialog(this, msg);
      } else {
        String title = mLocalizer.msg("hitsTitle",
          "Sendungen mit {0}", settings.getSearchText());
        showHitsDialog(programArr, title);
      }
    }
    catch (TvBrowserException exc) {
      ErrorHandler.handle(exc);
    }
  }
  
  
  
  /**
   * Shows a dialog containing the hits of the search.
   *
   * @param programArr The hits.
   * @param title The dialog's title.
   */  
  private void showHitsDialog(final Program[] programArr, String title) {
    final JDialog dlg = new JDialog(this, title, true);
    
    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(UiUtilities.DIALOG_BORDER);
    dlg.setContentPane(main);
    
    final ProgramList list = new ProgramList(programArr);
    main.add(new JScrollPane(list), BorderLayout.CENTER);
    
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    main.add(buttonPn, BorderLayout.SOUTH);
    
    JButton closeBt = new JButton(mLocalizer.msg("close", "Close"));
    closeBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dlg.dispose();
      }
    });
    dlg.getRootPane().setDefaultButton(closeBt);
    buttonPn.add(closeBt);
    
   	dlg.setSize(400, 400);
	UiUtilities.centerAndShow(dlg);
    
  }
  
}
