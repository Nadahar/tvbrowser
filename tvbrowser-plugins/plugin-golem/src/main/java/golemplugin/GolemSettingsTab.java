/*
 * Golem.de guckt - Plugin for TV-Browser
 * Copyright (C) 2010 Bodo Tasche
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
 * SVN information:
 *     $Date: 2010-02-20 13:09:24 +0100 (Sa, 20. Feb 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6530 $
 */
package golemplugin;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.DefaultMarkingPrioritySelectionPanel;
import util.ui.Localizer;
import util.ui.PluginChooserDlg;
import util.ui.UiUtilities;

public class GolemSettingsTab implements SettingsTab {
  static final Localizer mLocalizer = Localizer.getLocalizerFor(GolemSettingsTab.class);

  private JCheckBox markPrograms;
  private DefaultMarkingPrioritySelectionPanel markPriority;
  private ProgramReceiveTarget[] clientPluginTargets;
  private JLabel pluginLabel;

  public JPanel createSettingsPanel() {
    final FormLayout layout = new FormLayout("3dlu, pref, 3dlu, fill:min:grow, 3dlu, pref, 3dlu");
    final CellConstraints cc = new CellConstraints();

    final JPanel panel = new JPanel(layout);

    int line = 2;

    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));

    markPrograms = new JCheckBox(mLocalizer.msg("markItems", "Mark programs that are mentioned by Golem.de"));
    markPrograms.setSelected(GolemPlugin.getInstance().getSettings().isMarkEnabled());
    markPrograms.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        markPriority.setEnabled(markPrograms.isSelected());
      }
    });
    panel.add(markPrograms, cc.xyw(2, line, 4));

    line += 2;
    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));

    markPriority = DefaultMarkingPrioritySelectionPanel.createPanel(GolemPlugin.getInstance().getSettings().getMarkPriority(), mLocalizer.msg("markPriority", "Mark programs with:"), false, false, false);
    panel.add(markPriority, cc.xyw(2, line, 4));

    markPriority.setEnabled(GolemPlugin.getInstance().getSettings().isMarkEnabled());

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("6dlu"));
    line += 2;

    clientPluginTargets = GolemPlugin.getInstance().getSettings().getReceiveTargets();

    final JButton choose = new JButton(mLocalizer.msg("selectPlugins", "Choose Plugins"));
    choose.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        try {
          final Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
          PluginChooserDlg chooser = null;
          chooser = new PluginChooserDlg(w, clientPluginTargets, null, GolemPlugin.getInstance());
          chooser.setVisible(true);

          if (chooser.getReceiveTargets() != null) {
            clientPluginTargets = chooser.getReceiveTargets();
          }

          handlePluginSelection();
        }
        catch (Exception ee) {
          ee.printStackTrace();
        }
      }
    });

    pluginLabel = new JLabel();
    handlePluginSelection();

    panel.add(new JLabel(mLocalizer.msg("exportTitle", "Export to Plugin(s):")), cc.xy(2, line));
    panel.add(pluginLabel, cc.xy(4, line));
    panel.add(choose, cc.xy(6, line));


    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("6dlu"));
    line += 2;

    JButton button = new JButton(mLocalizer.msg("update", "Update now"));
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            GolemUpdater.getInstance().update();
          }
        });
      }
    });

    panel.add(button, cc.xy(2, line));

    return panel;
  }

  private void handlePluginSelection() {
    final ArrayList<ProgramReceiveIf> plugins = new ArrayList<ProgramReceiveIf>();

    if (clientPluginTargets != null) {
      for (ProgramReceiveTarget target : clientPluginTargets) {
        if (!plugins.contains(target.getReceifeIfForIdOfTarget())) {
          plugins.add(target.getReceifeIfForIdOfTarget());
        }
      }

      final ProgramReceiveIf[] mClientPlugins = plugins
          .toArray(new ProgramReceiveIf[plugins.size()]);

      if (mClientPlugins.length > 0) {
        pluginLabel.setText(mClientPlugins[0].toString());
        pluginLabel.setEnabled(true);
      } else {
        pluginLabel.setText(mLocalizer.msg("noPlugins", "No Plugins choosen"));
        pluginLabel.setEnabled(false);
      }

      for (int i = 1; i < (mClientPlugins.length > 4 ? 3 : mClientPlugins.length); i++) {
        pluginLabel.setText(pluginLabel.getText() + ", " + mClientPlugins[i]);
      }

      if (mClientPlugins.length > 4) {
        pluginLabel.setText(pluginLabel.getText() + " (" + (mClientPlugins.length - 3) + " " + mLocalizer.msg("otherPlugins", "others...") + ")");
      }
    }
  }

  public void saveSettings() {
    GolemPlugin.getInstance().getSettings().setMarkEnabled(markPrograms.isSelected());
    GolemPlugin.getInstance().getSettings().setMarkPriority(markPriority.getSelectedPriority());
    GolemPlugin.getInstance().getSettings().setReceiveTargets(clientPluginTargets);
  }

  public Icon getIcon() {
    return GolemPlugin.getInstance().getPluginIcon();
  }

  public String getTitle() {
    return null;
  }
}