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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.Localizer;
import devplugin.Program;

public class SingleTitleFilterComponent extends AbstractFilterComponent {

  private static final Localizer mLocalizer = Localizer
  .getLocalizerFor(SingleTitleFilterComponent.class);
  private long mLastTime;
  private HashSet<String> mTitles;

  public SingleTitleFilterComponent(String name, String desc) {
    super(name, desc);
    mLastTime = System.currentTimeMillis();
    reset();
  }

  private void reset() {
    mTitles = new HashSet<String>();
  }

  public SingleTitleFilterComponent() {
    this("", "");
  }

  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  public String toString() {
    return mLocalizer.msg("name", "First occurrences");
  }

  @Override
  public boolean accept(final Program program) {
    long now = System.currentTimeMillis();
    if (now - mLastTime > 1000) {
      reset();
    }
    boolean newElement = mTitles.add(program.getTitle());
    mLastTime = now;
    return newElement;
  }

  @Override
  public void read(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {
    // no settings
  }

  @Override
  public void write(ObjectOutputStream out) throws IOException {
    // no settings
  }

  @Override
  public JPanel getSettingsPanel() {
    JPanel p1 = new JPanel();
    p1.add(new JLabel(mLocalizer.msg("desc",
        "Accepts only the first occurrence of several programs with similar titles.")));
    return p1;
  }

  @Override
  public void saveSettings() {
    // TODO Auto-generated method stub

  }

}
