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
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Panel builder with additional methods for typical TV-Browser UI (e.g. settings tab).<br>
 * When using this class, you should normally use {@link #addParagraph(String)} to add a section to your settings tab
 * and afterwards use {@link #addRow()} to add standard controls into the section. For lists and other large controls
 * you may also use {@link #addGrowingRow()} instead.
 * <br>Don't create rows with height settings yourself!
 *
 * @author bananeweizen
 * @since 3.0
 *
 */
public class EnhancedPanelBuilder extends PanelBuilder {
  public EnhancedPanelBuilder(final FormLayout layout, final JPanel parentPanel) {
    super(layout,parentPanel);
  }

  public EnhancedPanelBuilder(final FormLayout layout) {
    super(layout);
  }

  /**
   * Create a new panel builder with the given columns.
   * You can add rows afterwards by using {@link #addParagraph(String)}, {@link #addRow()} and {@link #addGrowingRow()}.
   * @param encodedColumnSpecs The encoded column spec.
   */
  public EnhancedPanelBuilder(final String encodedColumnSpecs) {
    super(new FormLayout(encodedColumnSpecs,""));
  }

  /**
   * Create a new panel builder with the given columns, which sits on the given panel.
   * You can add rows afterwards by using {@link #addParagraph(String)}, {@link #addRow()} and {@link #addGrowingRow()}.
   * @param encodedColumnSpecs The encoded column spec.
   * @param parentPanel the finally built panel will be a child of this parent panel
   */
  public EnhancedPanelBuilder(final String encodedColumnSpecs, final JPanel parentPanel) {
    super(new FormLayout(encodedColumnSpecs,""), parentPanel);
  }

  /**
   * create a new section in the layout, which is separated from the previous line by a PARAGRAPH_GAP
   * @param label label string
   * @return the new separator component
   */
  public JComponent addParagraph(final String label) {
    if (getRowCount() > 0) {
      appendRow(RowSpec.decode("10dlu"));
    }
    else {
      appendRow(FormSpecs.NARROW_LINE_GAP_ROWSPEC);
    }
    appendRow(FormSpecs.DEFAULT_ROWSPEC);
    incrementRowNumber(true);
    if (label != null && !label.isEmpty()) {
      return addSeparator(label);
    }
    return null;
  }

  /**
   * Add a new standard layout row to the builders layout.
   * It is separated from the preceding row with a LINE_GAP. Use {@link #getRow()} to address this line
   * afterwards.
   *
   * @return the builder
   */
  public PanelBuilder addRow() {
    return addRow(true);
  }
  
  /**
   * Add a new standard layout row to the builders layout.
   * It is separated from the preceding row with a LINE_GAP if parameter withGap is <code>true</code>. Use {@link #getRow()} to address this line
   * afterwards.
   * 
   * @param withGap If the LINE_GAP should be added
   * @return the builder
   */
  public PanelBuilder addRow(boolean withGap) {
    return addRow(FormSpecs.DEFAULT_ROWSPEC.encode(),withGap);
  }

  private void incrementRowNumber(boolean lineGap) {
    // there is no line number zero, therefore only add one row, if we are still in the first line
    if (getRow() == 1 || !lineGap) {
      nextRow();
    }
    else {
      nextRow(2);
    }
  }

  /**
   * Add a new growing layout row to the builders layout.
   * It is separated from the preceding row by LINE_GAP and will grow to take the available space.
   * @return the builder
   */
  public PanelBuilder addGrowingRow() {
    return addGrowingRow(true);
  }


  /**
   * Add a new growing layout row to the builders layout.
   * It is separated from the preceding row by LINE_GAP if parameter withGap is <code>true</code> and will grow to take the available space.
   * @param withGap If the LINE_GAP should be added
   * @return the builder
   */
  public PanelBuilder addGrowingRow(boolean withGap) {
    return addRow("fill:default:grow", withGap);
  }
  
  /**
   * Add a new layout row with the given height to the builders layout.
   * It is separated from the preceding row with a LINE_GAP.
   * Use {@link #getRow()} to address this line afterwards.<br>
   * This method should normally not be used! Use {@link #addRow()} or {@link #addGrowingRow()} instead.
   * The necessary sizes for rows will be calculated by the PanelBuilder.
   * @param rowHeightCode row height
   * @return the builder
   */
  public PanelBuilder addRow(final String rowHeightCode) {
    return addRow(rowHeightCode, true);
  }
  
  /**
   * Add a new layout row with the given height to the builders layout.
   * It is separated from the preceding row with a LINE_GAP if parameter withGap is <code>true</code>.
   * Use {@link #getRow()} to address this line afterwards.<br>
   * This method should normally not be used! Use {@link #addRow()} or {@link #addGrowingRow()} instead.
   * The necessary sizes for rows will be calculated by the PanelBuilder.
   * @param rowHeightCode row height
   * @param withGap If the LINE_GAP should be added
   * @return the builder
   */
  public PanelBuilder addRow(final String rowHeightCode, boolean withGap) {
    if(withGap) {
      appendRow(RowSpec.decode("5dlu"));
    }
    
    appendRow(rowHeightCode);
    incrementRowNumber(withGap);
    return this;
  }
}
