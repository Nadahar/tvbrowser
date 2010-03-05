/*
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

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.ui.settings.MarkingsSettingsTab;
import tvbrowser.ui.settings.SettingsDialog;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsItem;



/**
 * A class that is a panel that allows selection of the mark priority for programs.
 *
 * @author René Mach
 * @since 2.5.3
 */
public final class DefaultMarkingPrioritySelectionPanel extends JPanel {
  /**
   * default serial version uid.
   */
  private static final long serialVersionUID = 1L;

  /**
   * the localizer for this class.
   */
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(DefaultMarkingPrioritySelectionPanel.class);

  /**
   * the dropdowns for the mark priority selection.
   */
  private JComboBox[] mPrioritySelection;

  /**
   * a comment/help text shown below the drop down for the
   * mark prio selection.
   */
  private JEditorPane mHelpLabel;

  /**
   * the seperator for the panel.
   */
  private JComponent mSeparator;

  /**
   * the labels for the dropdowns.
   */
  private JComponent[] mLabel;



  /**
   * @param priority which priority is selected in the drop down
   * @param showTitle if true, show the title
   * @param withDefaultDialogBorder if true, use the default border
   */
  private DefaultMarkingPrioritySelectionPanel(final int priority, final boolean showTitle, final boolean withDefaultDialogBorder) {
    this(priority, LOCALIZER.msg("color", "Highlighting color"), showTitle, true, withDefaultDialogBorder);
  }


  /**
   * @param priority which priority is selected in the drop down
   * @param label the label for the drop down
   * @param showTitle if true, show the title
   * @param showHelpLabel if true, show the help text
   * @param withDefaultDialogBorder if true, use the default border
   * @since 3.0
   */
  private DefaultMarkingPrioritySelectionPanel(final int priority, final String label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder) {
    this(new int[] {priority}, new String[] {label}, showTitle, showHelpLabel, withDefaultDialogBorder);
  }


  /**
   * the arrays for label and priority must have the same length. the index of
   * both arrays must be in the range of an integer. both indexes must be > 0.
   *
   * @param priority which priority is selected in the dropdowns. must not be null.
   * @param label the labels for the dropdowns. must not be null.
   * @param showTitle if true, show the title
   * @param showHelpLabel if true, show the help text
   * @param withDefaultDialogBorder if true, use the default border
   * @since 3.0
   */
  private DefaultMarkingPrioritySelectionPanel(final int[] priority, final String[] label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder) {
    CellConstraints cc = new CellConstraints();
    FormLayout layout = new FormLayout("5dlu,pref,5dlu,pref,0dlu:grow");
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder(layout,this);

    //how many selectors do we have to draw?
    int choosersToDraw = Math.min(priority.length, label.length);

    if (withDefaultDialogBorder) {
      pb.setDefaultDialogBorder();
    }

    //init the components
    mLabel = new JComponent[choosersToDraw];
    mPrioritySelection = new JComboBox[choosersToDraw];

    //add all the sub components to this panel
    if (showTitle) {
      pb.addRow();
      mSeparator = pb.addSeparator(getTitle(), cc.xyw(1, pb.getRowCount(), 5));
    }

    for (int i = 0; i < choosersToDraw; i++) {
      pb.addRow();
      mLabel[i] = pb.addLabel(label[i], cc.xy(2, pb.getRowCount()));

      mPrioritySelection[i] = new JComboBox(getMarkingColorNames(true));
      mPrioritySelection[i].setSelectedIndex(priority[i] + 1);
      mPrioritySelection[i].setRenderer(new MarkPriorityComboBoxRenderer());

      pb.add(mPrioritySelection[i], cc.xy(4, pb.getRowCount()));
    }

    if (showHelpLabel) {
      mHelpLabel = UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("help", "The selected higlighting color is only shown if the program is higlighted by this plugin only or if the other higlightings have a lower or the same priority. The higlighting colors of the priorities can be changed in the <a href=\"#link\">higlighting settings</a>."), new HyperlinkListener() {
        public void hyperlinkUpdate(final HyperlinkEvent e) {
          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            SettingsDialog.getInstance().showSettingsTab(SettingsItem.PROGRAMPANELMARKING);
          }
        }
      });

      pb.addGrowingRow();
      pb.addRow();

      pb.add(mHelpLabel, cc.xyw(2, pb.getRowCount(), 4));
    }
  }




  /**
   * Creates an instance of this class.
   *
   * @param priority The current selected priority.
   * @param showTitle If the title should be shown.
   * @param withDefaultDialogBorder If the panel should show the default dialog border of FormLayouts PanelBuilder.
   * @return The created instance of this class.
   */
  public static DefaultMarkingPrioritySelectionPanel createPanel(final int priority, final boolean showTitle, final boolean withDefaultDialogBorder) {
    return new DefaultMarkingPrioritySelectionPanel(priority, showTitle, withDefaultDialogBorder);
  }

  /**
   * Creates an instance of this class.
   *
   * @param priority which priority is selected in the drop down
   * @param label the label for the drop down
   * @param showTitle if true, show the title
   * @param showHelpLabel if true, show the help text
   * @param withDefaultDialogBorder if true, use the default border
   * @return The created instance of this class.
   * @since 3.0
   */
  public static DefaultMarkingPrioritySelectionPanel createPanel(final int priority, final String label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder) {
    return new DefaultMarkingPrioritySelectionPanel(priority, label, showTitle, showHelpLabel, withDefaultDialogBorder);
  }

  /**
   * the arrays for label and priority must have the same length. the index of
   * both arrays must be in the range of an integer. both indexes must be > 0.
   *
   * @param priority which priority is selected in the dropdowns. must not be null.
   * @param label the labels for the dropdowns. must not be null.
   * @param showTitle if true, show the title
   * @param showHelpLabel if true, show the help text
   * @param withDefaultDialogBorder if true, use the default border
   * @return The created instance of this class.
   * @since 3.0
   */
  public static DefaultMarkingPrioritySelectionPanel createPanel(final int[] priority, final String[] label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder) {
    return new DefaultMarkingPrioritySelectionPanel(priority, label, showTitle, showHelpLabel, withDefaultDialogBorder);
  }

  /**
   * @return The selected marking priority of the first dropdown
   */
  public int getSelectedPriority() {
    return mPrioritySelection[0].getSelectedIndex() - 1;
  }

  /**
   * @param index the index of the dropdown
   * @return The selected marking priority of the dropdown with the given index
   */
  public int getSelectedPriority(final int index) {
    return mPrioritySelection[index].getSelectedIndex() - 1;
  }

  /**
   * @return The selected marking priorities of all dropdowns
   */
  public int[] getSelectedPriorities() {
    int[] prios = new int[mPrioritySelection.length];
    for (int i = 0; i < mPrioritySelection.length; i++)
    {
      prios[i] = getSelectedPriority(i);
    }
    return prios;
  }

  /**
   * Gets the title of this settings panel.
   *
   * @return The title of this settings panel.
   */
  public static String getTitle() {
    return LOCALIZER.msg("title", "Highlighting");
  }

  /**
   * Gets the name of the marking colors in an array sorted from the lowest to the highest priority.
   * <p>
   * @param withNoMarkPriority If the array should contain the no mark priority name.
   * @return The names of the marking colors in an array sorted from the lowest to the highest priority.
   * @since 2.7
   */
  public static String[] getMarkingColorNames(final boolean withNoMarkPriority) {
    if (withNoMarkPriority) {
      return new String[] {MarkingsSettingsTab.mLocalizer.msg("color.noPriority", "Don't highlight"), MarkingsSettingsTab.mLocalizer.msg("color.minPriority", "1. Color (minimum priority)"), MarkingsSettingsTab.mLocalizer.msg("color.lowerMediumPriority", "2. Color (lower medium priority)"), MarkingsSettingsTab.mLocalizer.msg("color.mediumPriority", "3. Color (Medium priority)"), MarkingsSettingsTab.mLocalizer.msg("color.higherMediumPriority", "4. Color (higher medium priority)"), MarkingsSettingsTab.mLocalizer.msg("color.maxPriority", "5. Color (maximum priority)")};
    }
    else {
      return new String[] {MarkingsSettingsTab.mLocalizer.msg("color.minPriority", "1. Color (minimum priority)"), MarkingsSettingsTab.mLocalizer.msg("color.lowerMediumPriority", "2. Color (lower medium priority)"), MarkingsSettingsTab.mLocalizer.msg("color.mediumPriority", "3. Color (Medium priority)"), MarkingsSettingsTab.mLocalizer.msg("color.higherMediumPriority", "4. Color (higher medium priority)"), MarkingsSettingsTab.mLocalizer.msg("color.maxPriority", "5. Color (maximum priority)")};
    }
  }

  /**
   * this enables the panel and all its subcomponents.
   * @param enabled true to enable this panel, false otherwise
   * @see javax.swing.JComponent#setEnabled(boolean)
   */
  @Override
  public void setEnabled(final boolean enabled) {
    if (mSeparator != null) {
      mSeparator.setEnabled(enabled);
    }
    if (mHelpLabel != null)
    {
      mHelpLabel.setEnabled(enabled);
    }
    for (int i = 0; i < mLabel.length; i++)
    {
      mLabel[i].setEnabled(enabled);
      mPrioritySelection[i].setEnabled(enabled);
    }
  }
}
