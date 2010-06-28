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
 *     $Date: 2008-02-26 20:55:40 +0100 (Di, 26 Feb 2008) $
 *   $Author: ds10 $
 * $Revision: 4312 $
 */
package util.ui;

import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.ui.settings.SettingsDialog;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;
import devplugin.SettingsItem;

/**
 * A class that is a panel that allows selection of the program importance.
 * 
 * @author René Mach
 * @since 3.0
 */
public class DefaultProgramImportanceSelectionPanel extends JPanel {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(DefaultProgramImportanceSelectionPanel.class);
  private JComboBox mProgramImportanceSelection;
  private JEditorPane mHelpLabel;
  
  private DefaultProgramImportanceSelectionPanel(byte importance, boolean showTitle, boolean withDefaultDialogBorder) {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(showTitle ? new FormLayout("5dlu,pref,5dlu,pref,0dlu:grow","pref,5dlu,pref,fill:0dlu:grow,10dlu,pref") : new FormLayout("5dlu,pref,5dlu,pref,0dlu:grow","pref,fill:0dlu:grow,10dlu,pref"),this);
    
    if(withDefaultDialogBorder) {
      pb.setDefaultDialogBorder();
    }
    
    mProgramImportanceSelection = new JComboBox(getProgramImportanceNames(true));
    mProgramImportanceSelection.setSelectedIndex(getIndexForImportance(importance));
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("help","The selected importance is used to determinate the transparency of a program. It's calculated over all plugins as mean value. Lower importance leads to higher transparency. This works only if the plugins are allowed to set the transparency at <a href=\"#link\">program panel settings</a>."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.PROGRAMPANELLOOK);
        }
      }
    });
    
    int y = 1;
    
    if(showTitle) {
      pb.addSeparator(getTitle(),cc.xyw(1,y++,5));
      y++;
    }
    
    pb.addLabel(mLocalizer.msg("color","Program importance:"), cc.xy(2,y));
    pb.add(mProgramImportanceSelection, cc.xy(4,y++));y++;
    pb.add(mHelpLabel, cc.xyw(2,++y,4));
  }
  
  /**
   * Creates an instance of this class.
   * 
   * @param importance The current selected importance.
   * @param showTitle If the title should be shown.
   * @param withDefaultDialogBorder If the panel should show the default dialog border of FormLayouts PanelBuilder.
   * @return The created instance of this class.
   */
  public static DefaultProgramImportanceSelectionPanel createPanel(byte importance, boolean showTitle, boolean withDefaultDialogBorder) {
    return new DefaultProgramImportanceSelectionPanel(importance, showTitle, withDefaultDialogBorder);
  }
  
  /**
   * Gets the selected program importance.
   * 
   * @return The selected marking priority.
   */
  public byte getSelectedImportance() {
    switch(mProgramImportanceSelection.getSelectedIndex()) {
      case 1: return Program.MIN_PROGRAM_IMPORTANCE;
      case 2: return Program.LOWER_MEDIUM_PROGRAM_IMPORTANCE;
      case 3: return Program.MEDIUM_PROGRAM_IMPORTANCE;
      case 4: return Program.HIGHER_MEDIUM_PROGRAM_IMPORTANCE;
      case 5: return Program.MAX_PROGRAM_IMPORTANCE;
      
      default: return Program.DEFAULT_PROGRAM_IMPORTANCE;
    }
  }
  
  private int getIndexForImportance(byte importance) {
    switch(importance) {
    case Program.MIN_PROGRAM_IMPORTANCE: return 1;
    case Program.LOWER_MEDIUM_PROGRAM_IMPORTANCE: return 2;
    case Program.MEDIUM_PROGRAM_IMPORTANCE: return 3;
    case Program.HIGHER_MEDIUM_PROGRAM_IMPORTANCE: return 4;
    case Program.MAX_PROGRAM_IMPORTANCE: return 5;
    
    default: return 0;
  }
  }
  
  /**
   * Gets the title of this settings panel.
   * 
   * @return The title of this settings panel.
   */
  public static String getTitle() {
    return mLocalizer.msg("title","Program transparency");
  }
  
  /**
   * Gets the names of the importance values in an array sorted from the lowest to the highest importance.
   * <p>
   * @param withDefaultImportance If the array should contain the default importance name.
   * @return The names of the importance values in an array sorted from the lowest to the highest importance.
   */
  public static String[] getProgramImportanceNames(boolean withDefaultImportance) {
    if(withDefaultImportance) {
      return new String[] {mLocalizer.msg("color.default","Default importance"),mLocalizer.msg("color.min","Mininum importance"),mLocalizer.msg("color.lowerMedium","Lower medium importance"),mLocalizer.msg("color.medium","Medium importance"),mLocalizer.msg("color.higherMedium","Higher medium importance"),mLocalizer.msg("color.max","Maximum importance")};
    }
    else {
      return new String[] {mLocalizer.msg("color.min","Mininum importance"),mLocalizer.msg("color.lowerMedium","Lower medium importance"),mLocalizer.msg("color.medium","Medium importance"),mLocalizer.msg("color.higherMedium","Higher medium importance"),mLocalizer.msg("color.max","Maximum importance")};
    }
  }
}
