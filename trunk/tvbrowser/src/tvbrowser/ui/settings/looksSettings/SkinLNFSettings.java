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
package tvbrowser.ui.settings.looksSettings;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import tvbrowser.core.Settings;
import util.misc.OperatingSystem;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

/**
 * Settings for the Skin LnF
 *
 * @author bodum
 */
public class SkinLNFSettings extends JDialog implements WindowClosingIf {
  /** Translation */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SkinLNFSettings.class);
  private JComboBox mThemePack;

  /**
   * Create the Dialog
   * @param parent Parent
   */
  public SkinLNFSettings(JDialog parent) {
    super(parent, true);
    setTitle(mLocalizer.msg("title", "Skin Look and Feel Settings"));
    createGui();
  }

  /**
   * Create the GUI
   */
  private void createGui() {
    JPanel content = (JPanel) getContentPane();

    content.setLayout(new FormLayout("5dlu, fill:50dlu:grow, 3dlu", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, fill:3dlu:grow, pref"));
    content.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();

    content.add(DefaultComponentFactory.getInstance().createSeparator(Localizer.getLocalization(Localizer.I18N_HELP)), cc.xyw(1,1,3));

    content.add(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("skinLFInfo", "Skin Info")), cc.xyw(2,3,2));

    String temp = Settings.propSkinLFThemepack.getString();
    temp = StringUtils.substringAfterLast(temp, File.separator);

    String[] skins = getThemePacks();

    mThemePack = new JComboBox(skins);
    mThemePack.setSelectedItem(temp);

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("chooseThemepack", "Choose Themepack")), cc.xyw(1,5,3));
    content.add(mThemePack, cc.xy(2,7));

    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed();
      }
    });

    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelPressed();
      }
    });

    ButtonBarBuilder2 bar = new ButtonBarBuilder2();
    bar.addButton(new JButton[] {ok, cancel});

    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    panel.add(bar.getPanel());
    content.add(panel, cc.xyw(1,9,3));

    UiUtilities.registerForClosing(this);

    setSize(Sizes.dialogUnitXAsPixel(270, this), Sizes.dialogUnitYAsPixel(145, this));
  }

  private String[] getThemePacks() {
    final TreeSet<String> themepacks = new TreeSet<String>();
    themepacks.addAll(Arrays.asList(getThemePacks(new File("themepacks"))));
    themepacks.addAll(Arrays.asList(getThemePacks(new File(Settings.getUserDirectoryName(), "themepacks"))));

    if (OperatingSystem.isMacOs()) {
      themepacks.addAll(Arrays.asList(getThemePacks(new File(Settings.getOSLibraryDirectoryName() + "themepacks"))));
    }

    return themepacks.toArray(new String[themepacks.size()]);
  }

  private String[] getThemePacks(File directory) {
    if (directory == null || !directory.exists()) {
      return new String[0];
    }

    return directory.list(new FilenameFilter() {
       public boolean accept(File dir, String name) {
         return name.toLowerCase().endsWith(".zip");
       }
     });
  }

  /**
   * Cancel was pressed
   */
  protected void cancelPressed() {
    setVisible(false);
  }

  /**
   * OK was pressed
   */
  protected void okPressed() {
    Settings.propSkinLFThemepack.setString("themepacks/"+mThemePack.getSelectedItem());
    setVisible(false);
  }

  /**
   * Close the Dialog
   */
  public void close() {
    cancelPressed();
  }
}
