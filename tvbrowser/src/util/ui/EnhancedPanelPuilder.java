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
package util.ui;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * panel builder with additional methods for typical TV-Browser UI (e.g. settings tab)
 * @author bananeweizen
 * @since 3.0
 *
 */
public class EnhancedPanelPuilder extends PanelBuilder {

  public EnhancedPanelPuilder(FormLayout layout) {
    super(layout);
  }

  public EnhancedPanelPuilder(final String encodedColumnSpecs) {
    this(new FormLayout(encodedColumnSpecs,""));
  }

  /**
   * create a new section in the layout, which is separated from the previous line by a PARAGRAP_GAP
   * @param label label string
   * @return the new separator component
   */
  public JComponent addParagraph(final String label) {
    if (getRowCount() > 0) {
      appendRow(FormFactory.PARAGRAPH_GAP_ROWSPEC);
    }
    else {
      appendRow(FormFactory.NARROW_LINE_GAP_ROWSPEC);
    }
    appendRow(FormFactory.PREF_ROWSPEC);
    incrementRowNumber();
    return addSeparator(label);
  }

  /**
   * add a new layout row to the builders layout<br>
   * It is separated from the preceding row with a LINE_GAP. Use {@link #getRow()} to address this line
   * afterwards.
   * 
   * @return the builder
   */
  public PanelBuilder addRow() {
    appendRow(FormFactory.LINE_GAP_ROWSPEC);
    appendRow(FormFactory.PREF_ROWSPEC);
    incrementRowNumber();
    return this;
  }

  private void incrementRowNumber() {
    // there is no line number zero, therefore only add one row, if we are still in the first line
    if (getRow() == 1) {
      nextRow();
    }
    else {
      nextRow(2);
    }
  }

  /**
   * add a new growing row to the builders layout<br>
   * It is separated from the preceding row by LINE_GAP and will grow to take the available space.
   * @return the builder
   */
  public PanelBuilder addGrowingRow() {
    appendRow(FormFactory.LINE_GAP_ROWSPEC);
    appendRow("fill:default:grow");
    incrementRowNumber();
    return this;
  }
}
