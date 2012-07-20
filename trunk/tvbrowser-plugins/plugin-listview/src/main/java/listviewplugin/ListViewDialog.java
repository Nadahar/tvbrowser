/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package listviewplugin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;

/**
 * Creates a Dialog with a List of Programs
 *
 * @author bodo
 */
public class ListViewDialog extends JDialog implements WindowClosingIf {

  /** The localizer used by this class. */
  static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ListViewDialog.class);

  /** Settings for this Plugin */
  private Properties mSettings;
  
  private ListViewPanel mListViewPanel;
  
  /**
   * Creates the Dialog
   *
   * @param frame Frame for modal
   * @param plugin Plugin for reference
   * @param settings The settings of the ListViewPlugin
   */
  public ListViewDialog(Frame frame, Plugin plugin, Properties settings) {
    super(frame, true);
    setTitle(mLocalizer.msg("viewList1", "View List:"));
    mSettings = settings;
    mListViewPanel = new ListViewPanel(plugin);
    createGUI();
    mListViewPanel.addChangeTimer();
    UiUtilities.registerForClosing(this);
  }
  
  public void createGUI() {
    CellConstraints cc = new CellConstraints();
    
    JPanel content = (JPanel) this.getContentPane();
    content.setLayout(new BorderLayout());
    
    JPanel buttonPn = new JPanel(new BorderLayout());
    buttonPn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    JButton closeButton = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    closeButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    });

    JPanel p = new JPanel(new FormLayout("pref,5dlu,pref,5dlu,pref", "pref"));
    JButton settings = new JButton(TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
    settings.setToolTipText(mLocalizer.msg("settings","Open settings"));

    settings.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
        Plugin.getPluginManager().showSettings(ListViewPlugin.getInstance());
      }
    });

    final JCheckBox showAtStartup = new JCheckBox(mLocalizer.msg("showAtStart", "Show at start"));
    showAtStartup.setSelected(mSettings.getProperty("showAtStartup", "false").equals("true"));

    showAtStartup.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (showAtStartup.isSelected()) {
          mSettings.setProperty("showAtStartup", "true");
        } else {
          mSettings.setProperty("showAtStartup", "false");
        }
      }
    });

    p.add(settings, cc.xy(1, 1));
    p.add(showAtStartup, cc.xy(3, 1));

    buttonPn.add(p, BorderLayout.WEST);

    buttonPn.add(closeButton, BorderLayout.EAST);
    
    add(mListViewPanel, BorderLayout.CENTER);
    add(buttonPn, BorderLayout.SOUTH);
  }

  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);
    mListViewPanel.cancelTimer();
  }

  public void close() {
    dispose();
  }
}