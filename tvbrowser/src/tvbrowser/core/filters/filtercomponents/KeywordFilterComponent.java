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
package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.SearchForm;
import util.ui.SearchFormSettings;
import util.ui.UiUtilities;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramSearcher;

public class KeywordFilterComponent extends AbstractFilterComponent {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(KeywordFilterComponent.class);

  private SearchForm mSearchForm;

  private ProgramSearcher mSearcher;
  private ProgramFieldType[] mSearchFieldArr;

  private SearchFormSettings mSearchFormSettings;

  public KeywordFilterComponent(String name, String desc) {
    super(name, desc);
    setSearchFormSettings(new SearchFormSettings(""));
  }

  public KeywordFilterComponent() {
    this("", "");
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    setSearchFormSettings(new SearchFormSettings(in));
  }

  public void write(ObjectOutputStream out) throws IOException {
    mSearchFormSettings.writeData(out);
  }

  private void setSearchFormSettings(SearchFormSettings settings) {
    mSearchFormSettings = settings;
    try {
      mSearcher = mSearchFormSettings.createSearcher();
    } catch (TvBrowserException exc) {
      ErrorHandler.handle(exc);
    }

    mSearchFieldArr = mSearchFormSettings.getFieldTypes();
  }

  public void saveSettings() {
    mSearchFormSettings = mSearchForm.getSearchFormSettings();
    this.setSearchFormSettings(mSearchFormSettings);
  }

  public boolean accept(Program program) {
    return mSearcher.matches(program, mSearchFieldArr);
  }

  public JPanel getSettingsPanel() {
    String msg;

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    JPanel pn = new JPanel(new BorderLayout());
    pn.setBorder(BorderFactory.createEmptyBorder(3, 0, 5, 0));
    msg = mLocalizer.msg("description",
        "Accept all programs containing the following keyword:");
    pn.add(UiUtilities.createHelpTextArea(msg));
    content.add(pn);

    mSearchForm = new SearchForm(false, false);
    mSearchForm.setSearchFormSettings(mSearchFormSettings);
    content.add(mSearchForm);

    return content;
  }

  @Override
  public String toString() {
    return mLocalizer.msg("keyword", "keyword");
  }

  public int getVersion() {
    return 1;
  }

}
