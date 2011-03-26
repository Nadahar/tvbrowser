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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import devplugin.Program;

/**
 * This filter accepts programs where the duration is in a given time range.
 * 
 * @author bodo
 */
public class ProgramLengthFilterComponent extends AbstractFilterComponent {

  /**
   * Erzeugt einen leeren Filter
   */
  public ProgramLengthFilterComponent() {
    this("", "");
  }

  /**
   * Erzeugt einen Filter
   * 
   * @param name
   *          Name
   * @param description
   *          Beschreibung
   */
  public ProgramLengthFilterComponent(final String name,
      final String description) {
    super(name, description);
  }

  /**
   * returns the version of the filter component
   * 
   * @see tvbrowser.core.filters.FilterComponent#getVersion()
   */
  public int getVersion() {
    return 1;
  }

  /**
   * accepts only programs with a certain program length
   * 
   * @see tvbrowser.core.filters.FilterComponent#accept(devplugin.Program)
   */
  public boolean accept(final Program program) {

    final int length = program.getLength();
    if (length < 0) {
      return true;
    }
    if (mUseMin && (length < mMin)) {
      return false;
    }

    if (mUseMax && (length > mMax)) {
      return false;
    }

    return true;
  }

  /**
   * Liest die Einstellungen
   * 
   * @see tvbrowser.core.filters.FilterComponent#read(java.io.ObjectInputStream,
   *      int)
   */
  public void read(final ObjectInputStream in, final int version)
      throws IOException,
      ClassNotFoundException {
    mUseMin = in.readBoolean();
    mUseMax = in.readBoolean();
    mMin = in.readInt();
    mMax = in.readInt();
  }

  /**
   * Schreibt die Einstellungen
   * 
   * @see tvbrowser.core.filters.FilterComponent#write(java.io.ObjectOutputStream)
   */
  public void write(final ObjectOutputStream out) throws IOException {
    out.writeBoolean(mUseMin);
    out.writeBoolean(mUseMax);
    out.writeInt(mMin);
    out.writeInt(mMax);
  }

  /**
   * Erzeugt das Settings-Panel
   * 
   * @see tvbrowser.core.filters.FilterComponent#getSettingsPanel()
   */
  public JPanel getSettingsPanel() {
    final JPanel panel = new JPanel();
    
    mMin = Math.min(1000,Math.max(0,mMin));
    mMax = Math.min(1000,Math.max(1,mMax));

    mMinSpinner = new JSpinner(new SpinnerNumberModel(mMin, 0, 1000, 1));
    mMaxSpinner = new JSpinner(new SpinnerNumberModel(mMax, 1, 1000, 1));
    mMinCheck = new JCheckBox(mLocalizer.msg("minimum", "minimum in Minutes")
        + ":", mUseMin);
    mMaxCheck = new JCheckBox(mLocalizer.msg("maximum", "maximum in Minutes")
        + ":", mUseMax);

    if (!mUseMin) {
      mMinSpinner.setEnabled(false);
    }

    if (!mUseMax) {
      mMaxSpinner.setEnabled(false);
    }

    mMinCheck.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        mMinSpinner.setEnabled(mMinCheck.isSelected());
      }
    });
    mMaxCheck.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        mMaxSpinner.setEnabled(mMaxCheck.isSelected());
      }
    });

    panel.setLayout(new GridBagLayout());

    final GridBagConstraints a = new GridBagConstraints();
    a.gridwidth = GridBagConstraints.REMAINDER;
    a.fill = GridBagConstraints.NONE;

    final GridBagConstraints b = new GridBagConstraints();
    b.fill = GridBagConstraints.NONE;

    mMinSpinner.setEditor(new JSpinner.NumberEditor(mMinSpinner, "###0"));
    mMaxSpinner.setEditor(new JSpinner.NumberEditor(mMaxSpinner, "###0"));

    panel.add(mMinCheck, b);
    panel.add(mMinSpinner, a);

    panel.add(mMaxCheck, b);
    panel.add(mMaxSpinner, a);

    final JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(panel, BorderLayout.NORTH);
    return centerPanel;
  }

  /**
   * Schreibt die GUI-Daten in die Variablen
   * 
   * @see tvbrowser.core.filters.FilterComponent#saveSettings()
   */
  public void saveSettings() {
    mMin = ((Integer) mMinSpinner.getValue()).intValue();
    mMax = ((Integer) mMaxSpinner.getValue()).intValue();
    mUseMin = mMinCheck.isSelected();
    mUseMax = mMaxCheck.isSelected();
  }

  /**
   * return the filter name
   */
  @Override
  public String toString() {
    return mLocalizer.msg("ProgrammLength", "Program length");
  }

  /**
   * Der Lokalizer
   */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ProgramLengthFilterComponent.class);

  /** minimum length in minutes */
  private int mMin = 5;
  /** maximum length in minutes */
  private int mMax = 90;
  /** use minimum */
  private boolean mUseMin = true;
  /** use maximum */
  private boolean mUseMax = true;

  /** GUI components */
  private JSpinner mMinSpinner;
  private JSpinner mMaxSpinner;
  private JCheckBox mMinCheck;
  private JCheckBox mMaxCheck;
}