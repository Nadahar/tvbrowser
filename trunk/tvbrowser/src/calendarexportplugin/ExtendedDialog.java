/*
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
 *     $Date: 2005-12-26 21:46:18 +0100 (Mo, 26 Dez 2005) $
 *   $Author: troggan $
 * $Revision: 1764 $
 */
package calendarexportplugin;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import util.ui.Localizer;
import util.ui.PluginProgramConfigurationPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The Dialog for the Extended Settings.
 *
 * This Settings should not be visible in the Settings-Tab. They are too
 * complicated
 *
 * @author bodum
 */
public class ExtendedDialog extends JDialog {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ExtendedDialog.class);

  private PluginProgramConfigurationPanel mConfigPanel;

  /**
   * Creates the Dialog
   *
   * @param parent
   *          Parent-Frame
   */
  public ExtendedDialog(Window parent) {
    super(parent, mLocalizer.msg("title", "Formattings selection"));
    setModal(true);
    createGui();
  }

  /**
   * Create the GUI
   */
  private void createGui() {
    try {
      CellConstraints cc = new CellConstraints();
      PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default:grow,5dlu",
          "pref,5dlu,fill:default:grow,5dlu,pref"), (JPanel) this.getContentPane());
      pb.setDefaultDialogBorder();

      mConfigPanel = new PluginProgramConfigurationPanel(CalendarExportPlugin.getInstance()
          .getSelectedPluginProgramFormattings(), CalendarExportPlugin.getInstance()
          .getAvailableLocalPluginProgramFormatings(), CalendarExportPlugin.getDefaultFormatting(), true, false);

      pb.addSeparator(mLocalizer.msg("title", "Formatings selection"), cc.xyw(1, 1, 3));
      pb.add(mConfigPanel, cc.xy(2, 3));

      FormLayout layout = new FormLayout("0dlu:grow,pref,5dlu,pref", "pref");
      layout.setColumnGroups(new int[][] { { 2, 4 } });

      JPanel buttonPanel = new JPanel(layout);

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

      buttonPanel.add(ok, cc.xy(2, 1));
      buttonPanel.add(cancel, cc.xy(4, 1));

      pb.add(buttonPanel, cc.xy(2, 5));

      getRootPane().setDefaultButton(ok);

      setSize(550, 400);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Cancel was pressed
   */
  private void cancelPressed() {
    setVisible(false);
  }

  /**
   * OK was pressed, the Settings will be saved
   */
  private void okPressed() {
    CalendarExportPlugin.getInstance().setAvailableLocalPluginProgramFormatings(
        mConfigPanel.getAvailableLocalPluginProgramFormatings());
    CalendarExportPlugin.getInstance().setSelectedPluginProgramFormattings(
        mConfigPanel.getSelectedPluginProgramFormatings());

    setVisible(false);
  }

}