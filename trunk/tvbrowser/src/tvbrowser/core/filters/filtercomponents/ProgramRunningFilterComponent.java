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
 * Filtert ob eine Sendung schon gelaufen ist, gerade läuft oder noch laufen
 * wird
 * 
 * @author bodo
 */
public class ProgramRunningFilterComponent extends AbstractFilterComponent {

  /**
   * Erzeugt einen leeren Filter
   */
  public ProgramRunningFilterComponent() {
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
  public ProgramRunningFilterComponent(String name, String description) {
    super(name, description);
  }

  /**
   * Gibt die Version zurück
   * 
   * @see tvbrowser.core.filters.FilterComponent#getVersion()
   */
  public int getVersion() {
    return 1;
  }

  /**
   * Wird dieses Programm akzeptiert von diesem Filter ?
   * 
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
   * Liest die Einstellungen für dieses Plugin aus dem Stream
   * 
   * @see tvbrowser.core.filters.FilterComponent#read(java.io.ObjectInputStream,
   *      int)
   */
  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    mSelectedBits = in.readInt();
  }

  /**
   * Schreibt die Einstellungen dieses Plugins in den Stream
   * 
   * @see tvbrowser.core.filters.FilterComponent#write(java.io.ObjectOutputStream)
   */
  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mSelectedBits);
  }

  /**
   * Gibt einen Panel zurück, der es ermöglicht, den Filter einzustellen
   * 
   * @see tvbrowser.core.filters.FilterComponent#getSettingsPanel()
   */
  public JPanel getSettingsPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = GridBagConstraints.REMAINDER;

    _checkBox = new JCheckBox[3];

    _checkBox[0] = new JCheckBox(mLocalizer.msg("finished", "Already finished"));
    panel.add(_checkBox[0], c);

    if (bitSet(mSelectedBits, ALREADYFINISHED)) {
      _checkBox[0].setSelected(true);
    }

    _checkBox[1] = new JCheckBox(mLocalizer.msg("nowrunning", "Now running"));
    panel.add(_checkBox[1], c);

    if (bitSet(mSelectedBits, NOWRUNNING)) {
      _checkBox[1].setSelected(true);
    }

    _checkBox[2] = new JCheckBox(mLocalizer.msg("notyetstarted",
        "Not yet started"));
    panel.add(_checkBox[2], c);

    if (bitSet(mSelectedBits, NOTYETSTARTED)) {
      _checkBox[2].setSelected(true);
    }

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(panel, BorderLayout.NORTH);
    return centerPanel;
  }

  /**
   * Im Dialog wurde OK gedrückt, alle Einstellungen können nun übernommen
   * werden
   * 
   * @see tvbrowser.core.filters.FilterComponent#saveSettings()
   */
  public void saveSettings() {
    int selectedBits = 0;

    if (_checkBox[0].isSelected()) {
      selectedBits = selectedBits | ALREADYFINISHED;
    }
    if (_checkBox[1].isSelected()) {
      selectedBits = selectedBits | NOWRUNNING;
    }
    if (_checkBox[2].isSelected()) {
      selectedBits = selectedBits | NOTYETSTARTED;
    }

    mSelectedBits = selectedBits;
  }

  /**
   * Gibt den Namen des Filters zurück
   */
  @Override
  public String toString() {
    return mLocalizer.msg("ProgramState", "Programstate");
  }

  /**
   * Überprüft, ob Bits gesetzt sind
   * 
   * @param num
   *          hier prüfen
   * @param pattern
   *          diese pattern prüfen
   * @return Pattern gesetzt?
   */
  private boolean bitSet(int num, int pattern) {
    return (num & pattern) == pattern;
  }

  /**
   * Die gesetzten Bits
   */
  private int mSelectedBits = 0;

  /**
   * Die CheckBoxen für den Panel
   */
  private JCheckBox[] _checkBox;

  /**
   * Der Lokalizer
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