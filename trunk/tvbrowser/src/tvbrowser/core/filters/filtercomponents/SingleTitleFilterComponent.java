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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.ui.Localizer;
import devplugin.Program;
import devplugin.ProgramFieldType;

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
  public synchronized boolean accept(final Program program) {
    long now = System.currentTimeMillis();
    if (now - mLastTime > 1000) {
      reset();
    }
    String element = program.getTitle();
    if (bitSet(mSelectedBits,EPISODEONLY)) {
      String episode = program.getTextField(ProgramFieldType.EPISODE_TYPE);
      if (episode != null && episode.length() > 0) {
        element += "##" + episode;
      }
    }
    if (bitSet(mSelectedBits,CHANNELONLY)) {
      element += "##" + program.getChannel().getId();
    }

    boolean newElement = mTitles.add(element);
    mLastTime = now;
    return newElement;
  }

  /**
   * read the settings
   *
   * @see tvbrowser.core.filters.FilterComponent#read(java.io.ObjectInputStream,
   *      int)
   */
  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    mSelectedBits = in.readInt();
  }

  /**
   * write the settings
   *
   * @see tvbrowser.core.filters.FilterComponent#write(java.io.ObjectOutputStream)
   */
  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mSelectedBits);
  }

  @Override
  public JPanel getSettingsPanel() {
    JPanel p1 = new JPanel();
    p1.setLayout(new GridBagLayout());


    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = GridBagConstraints.REMAINDER;
   
    p1.add(new JLabel(mLocalizer.msg("desc",
    "Accepts only the first occurrence of several programs with similar titles.")), c); 

    mCheckBox = new JCheckBox[2];

    mCheckBox[0] = new JCheckBox(mLocalizer.msg("channelonly", "Same channel only"));
    p1.add(mCheckBox[0], c);

    if (bitSet(mSelectedBits, CHANNELONLY)) {
      mCheckBox[0].setSelected(true);
    }
    mCheckBox[1] = new JCheckBox(mLocalizer.msg("episodeonly", "Same episode only"));
    p1.add(mCheckBox[1], c);

    if (bitSet(mSelectedBits, EPISODEONLY)) {
      mCheckBox[1].setSelected(true);
    }




    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(p1, BorderLayout.NORTH);
    return centerPanel;
  
  }
  
  /**
   * Check if a bit is set
   * 
   * @param num
   *          test this int
   * @param pattern
   *          test for this pattern
   * @return true, if pattern is set
   */
  private boolean bitSet(int num, int pattern) {
    return (num & pattern) == pattern;
  }

  
  /**
   * the current selected bits
   */
  private int mSelectedBits = 0;

  /**
   * checkbox for the settingspanel
   */
  private JCheckBox[] mCheckBox;

  /**
   * save the settings
   * 
   * @see tvbrowser.core.filters.FilterComponent#saveSettings()
   */
  public void saveSettings() {
    int selectedBits = 0;

    if (mCheckBox[0].isSelected()) {
      selectedBits = selectedBits |CHANNELONLY;
    }
    if (mCheckBox[1].isSelected()) {
      selectedBits = selectedBits | EPISODEONLY;
    }


    mSelectedBits = selectedBits;
  }
  
  /** On Air */
  private static int CHANNELONLY = 1;
  /** Expired */
  private static int EPISODEONLY = 2;

}
