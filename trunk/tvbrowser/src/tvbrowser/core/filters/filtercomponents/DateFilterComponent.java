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
package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Date;
import devplugin.Program;

/**
 * filter component to filter for a relative date range (e.g. from current date
 * to current date + 7 days)
 * 
 * @author Bananeweizen
 * 
 */
public class DateFilterComponent extends AbstractFilterComponent {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(DateFilterComponent.class);
  private JSpinner mFromSpinner;
  private JSpinner mToSpinner;
  private int mStartDays = 0;
  private int mEndDays = 7;

  public DateFilterComponent(String name, String description) {
    super(name, description);
  }

  public DateFilterComponent() {
    this("", "");
  }

  @Override
  public boolean accept(final Program program) {
    final Date progDate = program.getDate();
    Date currentDate = Date.getCurrentDate();
    Date mStartDate = currentDate.addDays(mStartDays);
    Date mEndDate = currentDate.addDays(mEndDays);
    return mStartDate.compareTo(progDate) <= 0
        && progDate.compareTo(mEndDate) <= 0;
  }

  @Override
  public JPanel getSettingsPanel() {

    FormLayout layout = new FormLayout(
        "pref, 3dlu, fill:pref:grow, 3dlu, pref", "");
    JPanel content = new JPanel(layout);
    content.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();
    int currentRow = 1;

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));
    content.add(UiUtilities.createHelpTextArea(mLocalizer
        .msg("description", "")), cc.xyw(1, currentRow, 5));

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));

    content.add(new JLabel(mLocalizer.msg("from.1", "From today plus")), cc.xy(
        1, currentRow += 2));
    mFromSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 28, 1));
    mFromSpinner.setValue(0);
    content.add(mFromSpinner, cc.xy(3, currentRow));
    content.add(new JLabel(mLocalizer.msg("from.2", "days")), cc.xy(5,
        currentRow));

    layout.appendRow(RowSpec.decode("pref"));

    content.add(new JLabel(mLocalizer.msg("to.1", "Until today plus")), cc.xy(
        1, currentRow += 2));
    mToSpinner = new JSpinner(new SpinnerNumberModel(7, 0, 28, 1));
    mToSpinner.setValue(7);
    content.add(mToSpinner, cc.xy(3, currentRow));
    content.add(new JLabel(mLocalizer.msg("to.2", "days")), cc
        .xy(5, currentRow));

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(content, BorderLayout.NORTH);
    return centerPanel;
  }

  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    mStartDays = in.readInt();
    mEndDays = in.readInt();
  }

  @Override
  public void saveSettings() {
    mStartDays = (Integer) mFromSpinner.getValue();
    mEndDays = (Integer) mToSpinner.getValue();
  }

  @Override
  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mStartDays);
    out.writeInt(mEndDays);
  }

  @Override
  public String toString() {
    return mLocalizer.msg("date", "Date");
  }

}
