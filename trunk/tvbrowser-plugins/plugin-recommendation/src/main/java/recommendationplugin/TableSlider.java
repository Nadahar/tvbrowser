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
package recommendationplugin;

import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSlider;

import util.ui.Localizer;

/**
 * @author bananeweizen
 * 
 */
public class TableSlider extends JSlider {
  private static final int MAX_WEIGHTING = 100;
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TableSlider.class);
  private static Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>(3);
  static {
    labelTable.put(Integer.valueOf(0), new JLabel(mLocalizer.msg("neutral", "Neutral")));
    labelTable.put(Integer.valueOf(MAX_WEIGHTING), new JLabel(mLocalizer.msg("plus", "Recommend")));
    labelTable.put(Integer.valueOf(-MAX_WEIGHTING), new JLabel(mLocalizer.msg("minus", "Do not recommend")));
  }

  public TableSlider() {
    super(JSlider.HORIZONTAL, -MAX_WEIGHTING, MAX_WEIGHTING, 0);
//    setOpaque(true);
    setMajorTickSpacing(MAX_WEIGHTING);
    setPaintTicks(true);
    setPaintLabels(true);
    setLabelTable(labelTable);
    setBounds(0, 0, 100, getPreferredSize().height);
    setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
  }
}
