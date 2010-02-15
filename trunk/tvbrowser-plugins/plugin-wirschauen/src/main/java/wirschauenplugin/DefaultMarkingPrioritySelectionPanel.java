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
package wirschauenplugin;

import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.ui.settings.MarkingsSettingsTab;
import tvbrowser.ui.settings.SettingsDialog;
import util.ui.Localizer;
import util.ui.MarkPriorityComboBoxRenderer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsItem;



/**
 * A class that is a panel that allows selection of the mark priority for programs.
 *
 * @deprecated since 3.0. use util.ui.DefaultMarkingPrioritySelectionPanel after 3.0 tvb release.
 */
@Deprecated
public final class DefaultMarkingPrioritySelectionPanel extends JPanel {
  /**
   * default serial version uid.
   */
  private static final long serialVersionUID = 1L;

  /**
   * the localizer for this class.
   */
  private static final Localizer LOCALIZER = Localizer.getLocalizerFor(util.ui.DefaultMarkingPrioritySelectionPanel.class);

  /**
   * the drop down for the mark priority selection.
   */
  private JComboBox mPrioritySelection;

  /**
   * a comment/help text shown below the drop down for the
   * mark prio selection.
   */
  private JEditorPane mHelpLabel;



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
   */
  private DefaultMarkingPrioritySelectionPanel(final int priority, final String label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder) {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(showTitle ? new FormLayout("5dlu,pref,5dlu,pref,0dlu:grow", "pref,5dlu,pref,fill:0dlu:grow,10dlu,pref") : new FormLayout("5dlu,pref,5dlu,pref,0dlu:grow", "pref,fill:0dlu:grow,10dlu,pref"), this);

    if (withDefaultDialogBorder)
    {
      pb.setDefaultDialogBorder();
    }

    mPrioritySelection = new JComboBox(getMarkingColorNames(true));
    mPrioritySelection.setSelectedIndex(priority + 1);
    mPrioritySelection.setRenderer(new MarkPriorityComboBoxRenderer());

    if (showHelpLabel)
    {
      mHelpLabel = UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("help", "The selected higlighting color is only shown if the program is only higlighted by this plugin or if the other higlightings have a lower or the same priority. The higlighting colors of the priorities can be change in the <a href=\"#link\">higlighting settings</a>."), new HyperlinkListener() {
        public void hyperlinkUpdate(final HyperlinkEvent e) {
          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            SettingsDialog.getInstance().showSettingsTab(SettingsItem.PROGRAMPANELMARKING);
          }
        }
      });
    }

    int y = 1;

    if (showTitle) {
      pb.addSeparator(getTitle(), cc.xyw(1, y++, 5));
      y++;
    }

    pb.addLabel(label, cc.xy(2, y));
    pb.add(mPrioritySelection, cc.xy(4, y++)); y++;
    if (showHelpLabel)
    {
      pb.add(mHelpLabel, cc.xyw(2, ++y, 4));
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
   */
  public static DefaultMarkingPrioritySelectionPanel createPanel(final int priority, final String label, final boolean showTitle, final boolean showHelpLabel, final boolean withDefaultDialogBorder) {
    return new DefaultMarkingPrioritySelectionPanel(priority, label, showTitle, showHelpLabel, withDefaultDialogBorder);
  }

  /**
   * Gets the selected marking priority.
   *
   * @return The selected marking priority.
   */
  public int getSelectedPriority() {
    return mPrioritySelection.getSelectedIndex() - 1;
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
}
