/*
 * TV-Browser Copyright (C) 04-2003 Martin Oberhauser
 * (darras@users.sourceforge.net)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * CVS information: $RCSfile$ $Source:
 * /cvsroot/tvbrowser/tvbrowser/src/tvbrowser/core/filters/filtercomponents/TimeFilterComponent.java,v $
 * $Date$ $Author$ $Revision$
 */
package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import util.ui.Localizer;
import util.ui.SearchForm;
import util.ui.SearchFormSettings;

import com.jgoodies.forms.factories.Borders;

/**
 * This class provides a Settings-Dialog for the MassFilter
 * 
 * @author bodum
 */
public class MassFilterSettingsDialog extends JDialog {

  /** Translation */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(MassFilterSettingsDialog.class);
  /** Show Search-Form */
  private SearchForm mForm;
  /** Search-Settings */
  private SearchFormSettings mSearchFormSettings;

  /**
   * Create the Dialog
   * 
   * @param parent
   *          Parent-Frame
   * @param searchFormSettings
   *          Settings to use
   */
  public MassFilterSettingsDialog(Window parent,
      SearchFormSettings searchFormSettings) {
    super(parent);
    setModal(true);
    mSearchFormSettings = searchFormSettings;
    createGui();
    setLocationRelativeTo(parent);
  }

  /**
   * Create the GUI
   */
  private void createGui() {
    setTitle(mLocalizer.msg("title", "Mass-Filter Settings"));

    mForm = new SearchForm(false, false, false);
    mForm.setSearchFormSettings(mSearchFormSettings);

    JPanel content = (JPanel) getContentPane();

    content.setLayout(new BorderLayout());
    content.setBorder(Borders.DLU4_BORDER);

    content.add(mForm, BorderLayout.NORTH);

    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        okPressed();
        setVisible(false);
      }

    });

    JButton cancel = new JButton(Localizer
        .getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }

    });

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(ok);
    buttons.add(cancel);
    content.add(buttons, BorderLayout.SOUTH);

    pack();
  }

  /**
   * OK pressed, overwrite old Settings
   */
  private void okPressed() {
    mSearchFormSettings = mForm.getSearchFormSettings();
  }

  /**
   * Get Settings from Dialog
   * 
   * @return new Settings
   */
  public SearchFormSettings getSearchFormSettings() {
    return mSearchFormSettings;
  }

}
