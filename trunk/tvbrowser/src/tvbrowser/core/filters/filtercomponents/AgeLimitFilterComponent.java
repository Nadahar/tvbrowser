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

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import util.ui.UiUtilities;
import devplugin.Program;
import devplugin.ProgramFieldType;

public class AgeLimitFilterComponent extends AbstractFilterComponent {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(AgeLimitFilterComponent.class);

  public AgeLimitFilterComponent(String name, String description) {
    super(name, description);
  }

  public AgeLimitFilterComponent() {
    this("", "");
  }

  private int mRequiredAge;
  private JSpinner mAgeSpinner;

  public boolean accept(final Program program) {
    final int ageLimit = program.getIntField(ProgramFieldType.AGE_LIMIT_TYPE);
    return ageLimit > 0 && ageLimit <= mRequiredAge;
  }

  public JPanel getSettingsPanel() {
    JPanel content = new JPanel(new BorderLayout());

    mAgeSpinner = new JSpinner(new SpinnerNumberModel(16, 6, 21, 1));
    mAgeSpinner.setValue(14);
    content.add(UiUtilities.createHelpTextArea(mLocalizer
        .msg("description", "")), BorderLayout.NORTH);
    content.add(mAgeSpinner, BorderLayout.SOUTH);

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(content, BorderLayout.NORTH);
    return centerPanel;
  }

  public int getVersion() {
    return 1;
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    mRequiredAge = in.readInt();
  }

  public void saveSettings() {
    mRequiredAge = (Integer) mAgeSpinner.getValue();
  }

  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mRequiredAge);
  }

  @Override
  public String toString() {
    return mLocalizer.msg("label", "Age limit");
  }


}
