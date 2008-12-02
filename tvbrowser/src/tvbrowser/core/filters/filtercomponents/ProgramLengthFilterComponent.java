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
 * Dieser Filter akzeptiert Sendungen mit selbst definiertbaren min und max
 * L�ngen
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
  public ProgramLengthFilterComponent(String name, String description) {
    super(name, description);
  }

  /**
   * Gibt die Version zur�ck
   * 
   * @see tvbrowser.core.filters.FilterComponent#getVersion()
   */
  public int getVersion() {
    return 1;
  }

  /**
   * Aktzeptiert nur Sendungen einer bestimmten L�nge
   * 
   * @see tvbrowser.core.filters.FilterComponent#accept(devplugin.Program)
   */
  public boolean accept(Program program) {

    if (_useMin && (program.getLength() < _min)) {
      return false;
    }

    if (_useMax && (program.getLength() > _max)) {
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
  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    _useMin = in.readBoolean();
    _useMax = in.readBoolean();
    _min = in.readInt();
    _max = in.readInt();
  }

  /**
   * Schreibt die Einstellungen
   * 
   * @see tvbrowser.core.filters.FilterComponent#write(java.io.ObjectOutputStream)
   */
  public void write(ObjectOutputStream out) throws IOException {
    out.writeBoolean(_useMin);
    out.writeBoolean(_useMax);
    out.writeInt(_min);
    out.writeInt(_max);
  }

  /**
   * Erzeugt das Settings-Panel
   * 
   * @see tvbrowser.core.filters.FilterComponent#getSettingsPanel()
   */
  public JPanel getSettingsPanel() {
    JPanel panel = new JPanel();

    _minSpinner = new JSpinner(new SpinnerNumberModel(_min, 0, 1000, 1));
    ;
    _maxSpinner = new JSpinner(new SpinnerNumberModel(_max, 0, 1000, 1));
    ;
    _minBox = new JCheckBox(mLocalizer.msg("minimum", "minimum in Minutes")
        + ":", _useMin);
    _maxBox = new JCheckBox(mLocalizer.msg("maximum", "maximum in Minutes")
        + ":", _useMax);

    if (!_useMin) {
      _minSpinner.setEnabled(false);
    }

    if (!_useMax) {
      _maxSpinner.setEnabled(false);
    }

    _minBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _minSpinner.setEnabled(_minBox.isSelected());
      }
    });
    _maxBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _maxSpinner.setEnabled(_maxBox.isSelected());
      }
    });

    panel.setLayout(new GridBagLayout());

    GridBagConstraints a = new GridBagConstraints();
    a.gridwidth = GridBagConstraints.REMAINDER;
    a.fill = GridBagConstraints.NONE;

    GridBagConstraints b = new GridBagConstraints();
    b.fill = GridBagConstraints.NONE;

    _minSpinner.setEditor(new JSpinner.NumberEditor(_minSpinner, "###0"));
    _maxSpinner.setEditor(new JSpinner.NumberEditor(_maxSpinner, "###0"));

    panel.add(_minBox, b);
    panel.add(_minSpinner, a);

    panel.add(_maxBox, b);
    panel.add(_maxSpinner, a);

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(panel, BorderLayout.NORTH);
    return centerPanel;
  }

  /**
   * Schreibt die GUI-Daten in die Variablen
   * 
   * @see tvbrowser.core.filters.FilterComponent#saveSettings()
   */
  public void saveSettings() {
    _min = ((Integer) _minSpinner.getValue()).intValue();
    _max = ((Integer) _maxSpinner.getValue()).intValue();
    _useMin = _minBox.isSelected();
    _useMax = _maxBox.isSelected();
  }

  /**
   * Gibt den Namen des Filters zur�ck
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

  /** Minimal-L�nge */
  private int _min;
  /** Maximal-L�nge */
  private int _max;
  /** Minimum benutzen? */
  private boolean _useMin;
  /** Maximum benutzen */
  private boolean _useMax;

  /** GUI-Komponenten f�r das Panel */
  private JSpinner _minSpinner;
  private JSpinner _maxSpinner;
  private JCheckBox _minBox;
  private JCheckBox _maxBox;
}