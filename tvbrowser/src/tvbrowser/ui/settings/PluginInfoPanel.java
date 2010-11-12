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

package tvbrowser.ui.settings;

import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.Localizer;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Creates the info panel for a plugin.
 */
class PluginInfoPanel extends JPanel {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(PluginInfoPanel.class);

  private JLabel mNameLabel;

  private JLabel mVersionLabel;

  private JLabel mAuthorLabel;

  private JLabel mDescriptionLabel;

  private boolean mShowSettingsSeparator;

  private int mYCount = 3;

  /**
   * Creates the default instance of this panel.
   * <p>
   *
   * @param showSettingsSeparator
   */
  public PluginInfoPanel(boolean showSettingsSeparator) {
    this(null, showSettingsSeparator);
  }

  /**
   * Creates an instance of this panel.
   * <p>
   *
   * @param info
   *          The info of the plugin.
   * @param showSettingsSeparator
   *          Show the settings separator.
   */
  public PluginInfoPanel(devplugin.PluginInfo info,
      boolean showSettingsSeparator) {
    mShowSettingsSeparator = showSettingsSeparator;
    setLayout(new FormLayout("5dlu,pref,10dlu,default:grow,5dlu",
        "pref,5dlu,top:pref,top:pref,top:pref,top:pref,10dlu,pref"));
    CellConstraints cc = new CellConstraints();

    add(new JLabel(mLocalizer.msg("name", "Name")), cc.xy(2, mYCount));
    add(mNameLabel = new JLabel("-"), cc.xy(4, mYCount++));

    add(new JLabel(mLocalizer.msg("version", "Version")), cc
        .xy(2, mYCount));
    add(mVersionLabel = new JLabel("-"), cc.xy(4, mYCount++));

    add(new JLabel(mLocalizer.msg("author", "Author")), cc.xy(2, mYCount));
    add(mAuthorLabel = new JLabel("-"), cc.xy(4, mYCount++));

    add(new JLabel(mLocalizer.msg("description", "Description")), cc.xy(2,
        mYCount));
    add(mDescriptionLabel = new JLabel(), cc.xy(4, mYCount++));

    if (info != null) {
      setPluginInfo(info);
    }
  }

  public void setDefaultBorder(boolean plugin) {
    CellConstraints cc = new CellConstraints();
    String message;

    if (plugin) {
      message = mLocalizer.msg("about", "About this Plugin");
    } else {
      message = mLocalizer.msg("aboutDataService", "About this DataService");
    }
    add(DefaultComponentFactory.getInstance().createSeparator(message), cc.xyw(1, 1, 5));

    if (mShowSettingsSeparator) {
      add(DefaultComponentFactory.getInstance().createSeparator(
          Localizer.getLocalization(Localizer.I18N_SETTINGS)), cc.xyw(1, ++mYCount, 5));
    }
  }

  public void setPluginInfo(final devplugin.PluginInfo info) {
    mNameLabel.setText(info.getName());

    if (info.getVersion() == null) {
      mVersionLabel.setText("");
    } else {
      mVersionLabel
          .setText("<html>" + info.getVersion().toString() + "</html>");
    }

    mAuthorLabel.setText("<html>" + info.getAuthor() + "</html>");
    mDescriptionLabel.setText("<html>" + info.getDescription() + "</html>");
  }

}
