/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import devplugin.Program;

/**
 * Filtered if a show is currently running
 * 
 * @author bodo
 */
public class ProgramRunningFilterComponent extends AbstractFilterComponent {

  /**
   * Creates an unnamed filter
   */
  public ProgramRunningFilterComponent() {
    this("", "");
  }

  /**
   * Creates the filter
   * 
   * @param name
   *          name of the filter
   * @param description
   *          description of the filter
   */
  public ProgramRunningFilterComponent(String name, String description) {
    super(name, description);
  }

  /**
   * @return version of the filter
   * @see tvbrowser.core.filters.FilterComponent#getVersion()
   */
  public int getVersion() {
    return 1;
  }

  /**
   * @return true if the program is now running
   * @see tvbrowser.core.filters.FilterComponent#accept(devplugin.Program)
   */
  public boolean accept(Program program) {
    if (bitSet(mSelectedBits, NOWRUNNING) && (program.isOnAir())) {
      return true;
    }

    if (bitSet(mSelectedBits, ALREADYFINISHED) && program.isExpired()) {
      return true;
    }

    if (bitSet(mSelectedBits, NOTYETSTARTED) && !program.isExpired()
        && !program.isOnAir()) {
      return true;
    }

    return false;
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

  /**
   * @return settings panel for this filter component
   *
   * @see tvbrowser.core.filters.FilterComponent#getSettingsPanel()
   */
  public JPanel getSettingsPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = GridBagConstraints.REMAINDER;

    mCheckBox = new JCheckBox[3];

    mCheckBox[0] = new JCheckBox(mLocalizer.msg("finished", "Already finished"));
    panel.add(mCheckBox[0], c);

    if (bitSet(mSelectedBits, ALREADYFINISHED)) {
      mCheckBox[0].setSelected(true);
    }

    mCheckBox[1] = new JCheckBox(mLocalizer.msg("nowrunning", "Now running"));
    panel.add(mCheckBox[1], c);

    if (bitSet(mSelectedBits, NOWRUNNING)) {
      mCheckBox[1].setSelected(true);
    }

    mCheckBox[2] = new JCheckBox(mLocalizer.msg("notyetstarted",
        "Not yet started"));
    panel.add(mCheckBox[2], c);

    if (bitSet(mSelectedBits, NOTYETSTARTED)) {
      mCheckBox[2].setSelected(true);
    }

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(panel, BorderLayout.NORTH);
    return centerPanel;
  }

  /**
   * save the settings
   * 
   * @see tvbrowser.core.filters.FilterComponent#saveSettings()
   */
  public void saveSettings() {
    int selectedBits = 0;

    if (mCheckBox[0].isSelected()) {
      selectedBits = selectedBits | ALREADYFINISHED;
    }
    if (mCheckBox[1].isSelected()) {
      selectedBits = selectedBits | NOWRUNNING;
    }
    if (mCheckBox[2].isSelected()) {
      selectedBits = selectedBits | NOTYETSTARTED;
    }

    mSelectedBits = selectedBits;
  }

  /**
   * @return name of the filter
   */
  @Override
  public String toString() {
    return mLocalizer.msg("ProgramState", "Programstate");
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
   * localization
   */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ProgramRunningFilterComponent.class);

  /** On Air */
  private static int NOWRUNNING = 1;
  /** Expired */
  private static int ALREADYFINISHED = 2;
  /** Not expired */
  private static int NOTYETSTARTED = 4;
}