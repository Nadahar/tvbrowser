/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package calendarexportplugin.exporter;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import calendarexportplugin.CalendarExportSettings;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

/**
 * Settings for the Apple iCal Exporter
 */
public class AppleSettingsDialog extends JDialog implements WindowClosingIf {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(AppleSettingsDialog.class);

  /** Which Button was pressed ? */
  private int mReturnValue = JOptionPane.CANCEL_OPTION;

  private JButton mOkButton;
  private JTextField mCalendarChooser;

  public AppleSettingsDialog(Window owner, CalendarExportSettings settings) {
    super(owner);
    setModal(true);
    createGui(settings);
  }

  private void createGui(final CalendarExportSettings settings) {
    setTitle(mLocalizer.msg("title", "iCal Calendar Settings"));

    UiUtilities.registerForClosing(this);

    JPanel content = (JPanel) getContentPane();
    content.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();

    content
        .setLayout(new FormLayout("5dlu, 15dlu, fill:pref:grow, 3dlu, 100dlu",
            "pref, 3dlu,pref, 3dlu,pref, 3dlu,pref, 3dlu,pref, 3dlu,pref, 3dlu,pref, 3dlu,pref, fill:3dlu:grow ,pref, 3dlu,pref"));

    content.add(new JLabel(mLocalizer.msg("select", "Select Calendar")), cc.xyw(2, 3, 2));
    mCalendarChooser = new JTextField();
    content.add(mCalendarChooser, cc.xy(5, 3));

    ButtonBarBuilder2 builder = new ButtonBarBuilder2();
    builder.addGlue();

    mOkButton = new JButton(Localizer.getLocalization(Localizer.I18N_OK));

    mOkButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed(settings);
      }
    });

    getRootPane().setDefaultButton(mOkButton);

    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));

    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    builder.addButton(new JButton[] { mOkButton, cancel });
    content.add(builder.getPanel(), cc.xyw(1, 19, 5));
    loadValues(settings);

    setSize(Sizes.dialogUnitXAsPixel(200, this), Sizes.dialogUnitYAsPixel(100, this));
  }

  private void loadValues(CalendarExportSettings settings) {
    mCalendarChooser.setText(settings.getExporterProperty(AppleiCalExporter.PROPERTY_CALENDAR_NAME, "TV-Browser"));
  }

  private void okPressed(CalendarExportSettings settings) {
    mReturnValue = JOptionPane.OK_OPTION;
    setVisible(false);
    settings.setExporterProperty(AppleiCalExporter.PROPERTY_CALENDAR_NAME, mCalendarChooser.getText().trim());
  }

  /**
   * Show the Dialog
   *
   * @return Which Button was pressed ? (JOptionpane.OK_OPTION / CANCEL_OPTION)
   */
  public int showDialog() {
    UiUtilities.centerAndShow(this);
    return mReturnValue;
  }

  public void close() {
    mReturnValue = JOptionPane.CANCEL_OPTION;
    setVisible(false);
  }

}