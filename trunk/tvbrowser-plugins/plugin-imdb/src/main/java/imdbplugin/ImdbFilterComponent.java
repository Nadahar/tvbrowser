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
package imdbplugin;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.PluginsFilterComponent;
import devplugin.Program;

public class ImdbFilterComponent extends PluginsFilterComponent {

  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(ImdbFilterComponent.class);

  private double mMinRating = 0.0;
  private double mMaxRating = 10.0;

  private JSpinner mMinSpinner;

  private JSpinner mMaxSpinner;

  @Override
  public String getUserPresentableClassName() {
    return mLocalizer.msg("name", "IMDb rating");
  }

  public boolean accept(final Program program) {
    final ImdbPlugin plugin = ImdbPlugin.getInstance();
    if (plugin == null) {
      return false;
    }
    final ImdbRating rating = plugin.getRatingFor(program);
    if (rating == null) {
      return false;
    }
    final double ratingValue = 10.0 * rating.getRatingRelative();
    return ratingValue >= mMinRating && ratingValue <= mMaxRating;
  }

  @Override
  public JPanel getSettingsPanel() {

    final FormLayout layout = new FormLayout(
        "pref, 3dlu, fill:pref:grow, 3dlu, pref", "");
    final JPanel content = new JPanel(layout);
    content.setBorder(Borders.DIALOG_BORDER);

    final CellConstraints cc = new CellConstraints();
    int currentRow = 1;

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("5dlu"));
    content.add(UiUtilities.createHelpTextArea(mLocalizer
        .msg("description", "")), cc.xyw(1, currentRow, 5));

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));

    content.add(new JLabel(mLocalizer.msg("min.1", "Minimum")), cc.xy(1,
        currentRow += 2));
    mMinSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10.0, 0.1));
    mMinSpinner.setValue(mMinRating);
    content.add(mMinSpinner, cc.xy(3, currentRow));
    content.add(new JLabel(mLocalizer.msg("min.2", "(of 10.0)")), cc.xy(5,
        currentRow));

    layout.appendRow(RowSpec.decode("pref"));

    content.add(new JLabel(mLocalizer.msg("max.1", "Maximum")), cc.xy(1,
        currentRow += 2));
    mMaxSpinner = new JSpinner(new SpinnerNumberModel(10.0, 0.0, 10.0, 0.1));
    mMaxSpinner.setValue(mMaxRating);
    content.add(mMaxSpinner, cc.xy(3, currentRow));
    content.add(new JLabel(mLocalizer.msg("max.2", "(of 10.0)")), cc.xy(5,
        currentRow));

    final JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(content, BorderLayout.NORTH);
    return centerPanel;
  }

  public int getVersion() {
    return 1;
  }

  public void read(final ObjectInputStream in, final int version)
      throws IOException, ClassNotFoundException {
    mMinRating = in.readDouble();
    mMaxRating = in.readDouble();
  }

  public void write(final ObjectOutputStream out) throws IOException {
    out.writeDouble(mMinRating);
    out.writeDouble(mMaxRating);
  }

  @Override
  public void saveSettings() {
    mMinRating = (Double) mMinSpinner.getValue();
    mMaxRating = (Double) mMaxSpinner.getValue();
  }
}
