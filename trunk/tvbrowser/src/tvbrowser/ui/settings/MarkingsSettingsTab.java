/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package tvbrowser.ui.settings;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.core.Settings;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.Localizer;
import util.ui.MarkPriorityComboBoxRenderer;
import util.ui.PooledLocalizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * A settings tab for the marking colors
 * 
 * @author René Mach
 * @since 2.5.1
 */
public class MarkingsSettingsTab implements SettingsTab {
  /** The localizer for this class */
  public static final Localizer mLocalizer = PooledLocalizer.getLocalizerFor(MarkingsSettingsTab.class);

  private ColorLabel mProgramItemMinMarkedColorLb, mProgramItemLowerMediumMarkedColorLb, mProgramItemMediumMarkedColorLb, mProgramItemHigherMediumMarkedColorLb, mProgramItemMaxMarkedColorLb;
  private JCheckBox mProgramItemWithMarkingsIsShowingBorder, mProgramPanelUsesExtraSpaceForMarkIcons;
  private JComboBox mDefaultColor;
  private JEditorPane mHelpLabel;
  
  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default:grow","pref,5dlu,pref,10dlu,pref,5dlu,pref,fill:10dlu:grow,default"));
    pb.setDefaultDialogBorder();
    
    JPanel defaultMarkings = new JPanel(new FormLayout("default, 5dlu, default",
    "default,2dlu,default,2dlu,default"));

    String[] colors = {mLocalizer.msg("color.noPriority","Don't highlight"),mLocalizer.msg("color.minPriority","1. Color (minimal priority)"),mLocalizer.msg("color.lowerMediumPriority","2. Color (lower medium priority)"),mLocalizer.msg("color.mediumPriority","3. Color (medium priority)"),mLocalizer.msg("color.higherMediumPriority","4. Color (higher medium priority)"),mLocalizer.msg("color.maxPriority","5. Color (maximum priority)")};

    defaultMarkings.add(mProgramPanelUsesExtraSpaceForMarkIcons = new JCheckBox(mLocalizer.msg("panel.extraSpace","Use additional space for the mark icons"), Settings.propProgramPanelUsesExtraSpaceForMarkIcons.getBoolean()), cc.xyw(1,1,3));
    defaultMarkings.add(mProgramItemWithMarkingsIsShowingBorder = new JCheckBox(mLocalizer.msg("color.showBorder","Show border for highlighted programs"), Settings.propProgramPanelWithMarkingsShowingBoder.getBoolean()), cc.xyw(1,3,3));
    defaultMarkings.add(new JLabel(mLocalizer.msg("color.showColor","Highlight with color (default color):")), cc.xy(1,5));
    defaultMarkings.add(mDefaultColor = new JComboBox(colors), cc.xy(3,5));
    mDefaultColor.setSelectedIndex(Settings.propProgramPanelUsedDefaultMarkPriority.getInt()+1);
    
    mDefaultColor.setRenderer(new MarkPriorityComboBoxRenderer());
    
    JPanel markings = new JPanel(new FormLayout("default, 5dlu, default, 5dlu, default",
        "default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default"));
        
    Color programItemMinMarkedColor = Settings.propProgramPanelMarkedMinPriorityColor.getColor();
    Color programItemMinDefaultMarkedColor = Settings.propProgramPanelMarkedMinPriorityColor.getDefaultColor();
    
    markings.add(new JLabel(colors[1]), cc.xy(1,1));
    markings.add(mProgramItemMinMarkedColorLb = new ColorLabel(programItemMinMarkedColor), cc.xy(3,1));
    mProgramItemMinMarkedColorLb.setStandardColor(programItemMinDefaultMarkedColor);
    markings.add(new ColorButton(mProgramItemMinMarkedColorLb), cc.xy(5,1));
    
    Color programItemLowerMediumMarkedColor = Settings.propProgramPanelMarkedLowerMediumPriorityColor.getColor();
    Color programItemDefaultLowerMediumMarkedColor = Settings.propProgramPanelMarkedLowerMediumPriorityColor.getDefaultColor();

    markings.add(new JLabel(colors[2]), cc.xy(1,3));
    markings.add(mProgramItemLowerMediumMarkedColorLb = new ColorLabel(programItemLowerMediumMarkedColor), cc.xy(3,3));
    mProgramItemLowerMediumMarkedColorLb.setStandardColor(programItemDefaultLowerMediumMarkedColor);
    markings.add(new ColorButton(mProgramItemLowerMediumMarkedColorLb), cc.xy(5,3));
    
    Color programItemMediumMarkedColor = Settings.propProgramPanelMarkedMediumPriorityColor.getColor();
    Color programItemMediumDefaultMarkedColor = Settings.propProgramPanelMarkedMediumPriorityColor.getDefaultColor();
    
    markings.add(new JLabel(colors[3]), cc.xy(1,5));
    markings.add(mProgramItemMediumMarkedColorLb = new ColorLabel(programItemMediumMarkedColor), cc.xy(3,5));
    mProgramItemMediumMarkedColorLb.setStandardColor(programItemMediumDefaultMarkedColor);
    markings.add(new ColorButton(mProgramItemMediumMarkedColorLb), cc.xy(5,5));

    Color programItemHigherMediumMarkedColor = Settings.propProgramPanelMarkedHigherMediumPriorityColor.getColor();
    Color programItemHigherMediumDefaultMarkedColor = Settings.propProgramPanelMarkedHigherMediumPriorityColor.getDefaultColor();
    
    markings.add(new JLabel(colors[4]), cc.xy(1,7));
    markings.add(mProgramItemHigherMediumMarkedColorLb = new ColorLabel(programItemHigherMediumMarkedColor), cc.xy(3,7));
    mProgramItemHigherMediumMarkedColorLb.setStandardColor(programItemHigherMediumDefaultMarkedColor);
    markings.add(new ColorButton(mProgramItemHigherMediumMarkedColorLb), cc.xy(5,7));
    
    Color programItemMaxMarkedColor = Settings.propProgramPanelMarkedMaxPriorityColor.getColor();
    Color programItemMaxDefaultMarkedColor = Settings.propProgramPanelMarkedMaxPriorityColor.getDefaultColor();
    
    markings.add(new JLabel(colors[5]), cc.xy(1,9));
    markings.add(mProgramItemMaxMarkedColorLb = new ColorLabel(programItemMaxMarkedColor), cc.xy(3,9));
    mProgramItemMaxMarkedColorLb.setStandardColor(programItemMaxDefaultMarkedColor);
    markings.add(new ColorButton(mProgramItemMaxMarkedColorLb), cc.xy(5,9));

    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("color.help","The priority that a plugin uses for a program is used to decide which color have to be used for the marking. A higher priority color replaces a lower priority color. The setting for the default color is only for plugins that do not care about the priority. But it works like for plugins that uses the priorities, so if you select the highest priority color there, all marking of plugin which do not care about the priority will replace lower marking colors."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
      }
    });
    
    pb.addSeparator(mLocalizer.msg("color.programMarked","Highlighting by plugins"), cc.xyw(1,1,2));
    pb.add(defaultMarkings, cc.xy(2,3));
    pb.addSeparator(mLocalizer.msg("color.programMarkedAdditional","Additional colors (replacing default color)"), cc.xyw(1,5,2));
    pb.add(markings, cc.xy(2,7));
    pb.add(mHelpLabel, cc.xy(2,9));
    
    return pb.getPanel();
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("title","Highlighting");
  }

  public void saveSettings() {
    Settings.propProgramPanelUsesExtraSpaceForMarkIcons.setBoolean(mProgramPanelUsesExtraSpaceForMarkIcons.isSelected());
    Settings.propProgramPanelWithMarkingsShowingBoder.setBoolean(mProgramItemWithMarkingsIsShowingBorder.isSelected());
    Settings.propProgramPanelUsedDefaultMarkPriority.setInt(mDefaultColor.getSelectedIndex() - 1);
        
    Settings.propProgramPanelMarkedMinPriorityColor.setColor(mProgramItemMinMarkedColorLb.getColor());
    Settings.propProgramPanelMarkedLowerMediumPriorityColor.setColor(mProgramItemLowerMediumMarkedColorLb.getColor());
    Settings.propProgramPanelMarkedMediumPriorityColor.setColor(mProgramItemMediumMarkedColorLb.getColor());
    Settings.propProgramPanelMarkedHigherMediumPriorityColor.setColor(mProgramItemHigherMediumMarkedColorLb.getColor());
    Settings.propProgramPanelMarkedMaxPriorityColor.setColor(mProgramItemMaxMarkedColorLb.getColor());
  }
}
