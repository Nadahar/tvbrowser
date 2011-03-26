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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.core.filters.filtercomponents;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import tvbrowser.ui.settings.MarkingsSettingsTab;
import util.ui.Localizer;
import util.ui.MarkPriorityComboBoxRenderer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;

/**
 * A filter component for tracking programs that have a selected mark priority.
 * 
 * @author René Mach
 * @since 2.5.1
 */
public class ProgramMarkingPriorityFilterComponent extends
    AbstractFilterComponent {

  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(ProgramMarkingPriorityFilterComponent.class);

  private int mMarkPriority = Program.MIN_MARK_PRIORITY;
  private JComboBox mValueSelection;

  /**
   * Creates an new instance of this filter component.
   */
  public ProgramMarkingPriorityFilterComponent() {
    this("", "");
  }

  /**
   * Creates an instance of this filter component with given name and given
   * description.
   * 
   * @param name
   *          The name of this filter component.
   * @param desc
   *          The description for this filter component.
   */
  public ProgramMarkingPriorityFilterComponent(String name, String desc) {
    super(name, desc);
  }

  public boolean accept(Program program) {
    return program.getMarkPriority() == mMarkPriority;
  }

  public JPanel getSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    JPanel p = new JPanel(new FormLayout("default", "pref"));

    Localizer localizer = MarkingsSettingsTab.mLocalizer;
    String[] values = {
        localizer.msg("color.minPriority", "1. Color (minimum priority)"),
        localizer.msg("color.lowerMediumPriority",
            "2. Color (lower medium priority)"),
        localizer.msg("color.mediumPriority", "3. Color (Medium priority)"),
        localizer.msg("color.higherMediumPriority",
            "4. Color (higher medium priority)"),
        localizer.msg("color.maxPriority", "5. Color (maximum priority)") };

    mValueSelection = new JComboBox(values);
    mValueSelection.setSelectedIndex(mMarkPriority);
    mValueSelection.setRenderer(new MarkPriorityComboBoxRenderer());

    p.add(mValueSelection, cc.xy(1, 1));

    return p;
  }

  public int getVersion() {
    return 1;
  }

  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    mMarkPriority = in.readInt();
  }

  public void saveSettings() {
    if (mValueSelection != null) {
      mMarkPriority = mValueSelection.getSelectedIndex();
    }
  }

  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mMarkPriority);
  }

  @Override
  public String toString() {
    return mLocalizer.msg("name", "Marking priority");
  }
}
