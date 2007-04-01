/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
package util.ui;

import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.ui.settings.MarkingsSettingsTab;
import tvbrowser.ui.settings.SettingsDialog;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsItem;

/**
 * A class that is a panel that allows selection of the mark priority for programs.
 * 
 * @author René Mach
 * @since 2.5.3
 */
public class DefaultMarkingPrioritySelectionPanel extends JPanel {
  private static Localizer mLocalizer = Localizer.getLocalizerFor(DefaultMarkingPrioritySelectionPanel.class);
  private JComboBox mPrioritySelection;
  private JEditorPane mHelpLabel;
  
  private DefaultMarkingPrioritySelectionPanel(int priority, boolean showTitle, boolean withDefaultDialogBorder) {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(showTitle ? new FormLayout("5dlu,pref,5dlu,pref,0dlu:grow","pref,5dlu,pref,fill:0dlu:grow,10dlu,pref") : new FormLayout("5dlu,pref,5dlu,pref,0dlu:grow","pref,fill:0dlu:grow,10dlu,pref"),this);
    
    if(withDefaultDialogBorder)
      pb.setDefaultDialogBorder();
    
    Localizer localizer = MarkingsSettingsTab.mLocalizer;
    
    String[] colors = {localizer.msg("color.noPriority","Don't highlight"),localizer.msg("color.minPriority","1. Color (minimum priority)"),localizer.msg("color.lowerMediumPriority","2. Color (lower medium priority)"),localizer.msg("color.mediumPriority","3. Color (Medium priority)"),localizer.msg("color.higherMediumPriority","4. Color (higher medium priority)"),localizer.msg("color.maxPriority","5. Color (maximum priority)")};
    
    mPrioritySelection = new JComboBox(colors);
    mPrioritySelection.setSelectedIndex(priority+1);
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("help","The selected higlighting color is only shown if the program is only marked by this plugin or if the other markings have a lower or the same priority. The marking colors of the priorities can be change in the <a href=\"#link\">program panel settings</a>."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.PROGRAMPANELMARKING);
        }
      }
    });
    
    int y = 1;
    
    if(showTitle) {
      pb.addSeparator(getTitle(),cc.xyw(1,y++,5));
      y++;
    }
    
    pb.addLabel(mLocalizer.msg("color","Highlighting color"), cc.xy(2,y));
    pb.add(mPrioritySelection, cc.xy(4,y++));y++;
    pb.add(mHelpLabel, cc.xyw(2,++y,4));
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param priority The current selected priority.
   * @param showTitle If the title should be shown.
   * @param withDefaultDialogBorder If the panel should show the default dialog border of FormLayouts PanelBuilder.
   * @return The created instance of this class.
   */
  public static DefaultMarkingPrioritySelectionPanel createPanel(int priority, boolean showTitle, boolean withDefaultDialogBorder) {
    return new DefaultMarkingPrioritySelectionPanel(priority, showTitle, withDefaultDialogBorder);
  }
  
  /**
   * Gets the selected marking priority.
   * 
   * @return The selected marking priority.
   */
  public int getSelectedPriority() {
    return mPrioritySelection.getSelectedIndex()-1;
  }
  
  /**
   * Gets the title of this settings panel.
   * 
   * @return The title of this settings panel.
   */
  public static String getTitle() {
    return mLocalizer.msg("title","Highlighting");
  }
}
