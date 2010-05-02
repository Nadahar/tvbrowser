/*
 *******************************************************************
 *              TVBConsole plugin for TVBrowser                    *
 *                                                                 *
 * Copyright (C) 2010 Tomas Schackert.                             *
 * Contact koumori@web.de                                          *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 3 of the License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program, in a file called LICENSE in the top
 directory of the distribution; if not, write to
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA

 *******************************************************************/

package aconsole.config;

import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.ColorButton;
import aconsole.AConsole;
import aconsole.properties.ColorProperty;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;

/**
 * JPanel with the settings of this plugin
 *
 * TODO: Font-property TODO: buffersize-property
 *
 * @author Tomas Schackert
 *
 */
public class PropertyPanel implements SettingsTab {
  private static final long serialVersionUID = -501346123606891605L;
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(PropertyPanel.class);

  interface Link {
    public void reset();

    public void save();
  }

  static class ColorLink implements Link {
    ColorProperty prop;
    ColorButton btn;

    public void reset() {
      btn.setColor(prop.get());
    };

    public void save() {
      prop.set(btn.getColor());
    };
  }

  Vector<Link> linklist = new Vector<Link>();

  public int addColorButton(ColorProperty src, String label, String tooltip, FormLayout layout,
      PanelBuilder panelBuilder, CellConstraints cc, int row) {
    ColorLink cl = new ColorLink();
    cl.prop = src;
    cl.btn = new ColorButton();
    cl.btn.setToolTipText(tooltip);
    cl.reset();
    layout.insertRow(row, RowSpec.decode("pref"));
    layout.insertRow(row + 1, RowSpec.decode("5dlu"));
    panelBuilder.add(new JLabel(label), cc.xyw(2, row, 1));
    panelBuilder.add(cl.btn, cc.xyw(3, row, 1));
    linklist.add(cl);
    return 2;
  }

  JPanel panel;

  public PropertyPanel() {
    CellConstraints cc = new CellConstraints();
    FormLayout layout = new FormLayout("5dlu, pref, pref,default:grow", "pref, 5dlu");
    PanelBuilder panelBuilder = new PanelBuilder(layout);
    panelBuilder.add(new JLabel(mLocalizer.msg("title", "setup appearance of TVBConsole")), cc.xyw(2, 1, 2));
    int row = 2;
    row += addColorButton(AConsole.getBg(), mLocalizer.msg("Bg.label", "background:"), mLocalizer.msg("Bg.tooltip",
        "select the color for the console background"), layout, panelBuilder, cc, row);

    row += addColorButton(AConsole.getSelection(), mLocalizer.msg("Selection.label", "background for selected rows:"),
        mLocalizer.msg("Selection.tooltip", ""), layout, panelBuilder, cc, row);
    row += addColorButton(AConsole.getSelectionText(), mLocalizer
        .msg("SelectionText.label", "color for selected text:"), mLocalizer.msg("SelectionText.tooltip", ""), layout,
        panelBuilder, cc, row);
    row += addColorButton(AConsole.getSystemOutText(), mLocalizer.msg("SystemOutText.label",
        "color for text send to System.out:"), mLocalizer.msg("SystemOutText.tooltip", ""), layout, panelBuilder, cc,
        row);
    row += addColorButton(AConsole.getSystemErrText(), mLocalizer.msg("SystemErrText.label",
        "color for text send to System.err:"), mLocalizer.msg("SystemErrText.tooltip", ""), layout, panelBuilder, cc,
        row);
    row += addColorButton(AConsole.getLevelSevereText(), mLocalizer.msg("LevelSevereText.label",
        "text with logger-level of severe or higher:"), mLocalizer.msg("LevelSevereText.tooltip", ""), layout,
        panelBuilder, cc, row);
    row += addColorButton(AConsole.getLevelWarningText(), mLocalizer.msg("LevelWarningText.label",
        "text with logger-level between warning and severe:"), mLocalizer.msg("LevelWarningText.tooltip", ""), layout,
        panelBuilder, cc, row);
    row += addColorButton(AConsole.getLevelInfoText(), mLocalizer.msg("LevelInfoText.label",
        "text with logger-level between info and warning:"), mLocalizer.msg("LevelInfoText.tooltip", ""), layout,
        panelBuilder, cc, row);
    row += addColorButton(AConsole.getLevelOtherText(), mLocalizer.msg("LevelOtherText.label",
        "text with logger-level less than warning:"), mLocalizer.msg("LevelOtherText.tooltip", ""), layout,
        panelBuilder, cc, row);

    panel = panelBuilder.getPanel();

  }

  public JPanel createSettingsPanel() {
    return panel;
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return "TVBConsole";
  }

  public void saveSettings() {
    for (Link l : this.linklist) {
      l.save();
    }
  }

}
