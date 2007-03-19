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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.Localizer;

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
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(MarkingsSettingsTab.class);

  private ColorLabel mProgramItemDefaultMarkedColorLb, mProgramItemMinMarkedColorLb, mProgramItemMediumMarkedColorLb, mProgramItemMaxMarkedColorLb;
  private JCheckBox mProgramItemDefaultMarkedColorShown;
  
  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default:grow","pref,5dlu,pref,10dlu,pref,5dlu,pref"));
    pb.setDefaultDialogBorder();
    
    JPanel defaultMarkings = new JPanel(new FormLayout("default, 5dlu, default, 5dlu, default",
    "default"));
    
    Color programItemDefaultMarkedColor = Settings.propProgramTableMarkedDefaultPriorityColor.getColor();
    Color programItemDefaultDefaultMarkedColor = Settings.propProgramTableMarkedDefaultPriorityColor.getDefaultColor();
    
    defaultMarkings.add(mProgramItemDefaultMarkedColorShown = new JCheckBox(mLocalizer.msg("color.showColor","Highlight with default color"), Settings.propProgramTableMarkedDefaultPriorityShowsColor.getBoolean()), cc.xy(1,1));
    
    defaultMarkings.add(mProgramItemDefaultMarkedColorLb = new ColorLabel(programItemDefaultMarkedColor), cc.xy(3,1));
    mProgramItemDefaultMarkedColorLb.setStandardColor(programItemDefaultDefaultMarkedColor);
    
    final ColorButton defaultColorBtn = new ColorButton(mProgramItemDefaultMarkedColorLb);
    defaultMarkings.add(defaultColorBtn, cc.xy(5,1));

    mProgramItemDefaultMarkedColorLb.setEnabled(mProgramItemDefaultMarkedColorShown.isSelected());
    defaultColorBtn.setEnabled(mProgramItemDefaultMarkedColorShown.isSelected());
    
    mProgramItemDefaultMarkedColorShown.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mProgramItemDefaultMarkedColorLb.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        defaultColorBtn.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      }
    });
        
    JPanel markings = new JPanel(new FormLayout("default, 5dlu, default, 5dlu, default",
        "default, 3dlu, default, 3dlu, default, 3dlu, default"));
        
    Color programItemMinMarkedColor = Settings.propProgramTableMarkedMinPriorityColor.getColor();
    Color programItemMinDefaultMarkedColor = Settings.propProgramTableMarkedMinPriorityColor.getDefaultColor();
    
    markings.add(new JLabel(mLocalizer.msg("color.minPriority","Minimum priority")), cc.xy(1,1));
    markings.add(mProgramItemMinMarkedColorLb = new ColorLabel(programItemMinMarkedColor), cc.xy(3,1));
    mProgramItemMinMarkedColorLb.setStandardColor(programItemMinDefaultMarkedColor);
    markings.add(new ColorButton(mProgramItemMinMarkedColorLb), cc.xy(5,1));
    
    Color programItemMediumMarkedColor = Settings.propProgramTableMarkedMediumPriorityColor.getColor();
    Color programItemMediumDefaultMarkedColor = Settings.propProgramTableMarkedMediumPriorityColor.getDefaultColor();
    
    markings.add(new JLabel(mLocalizer.msg("color.mediumPriority","Medium priority")), cc.xy(1,3));
    markings.add(mProgramItemMediumMarkedColorLb = new ColorLabel(programItemMediumMarkedColor), cc.xy(3,3));
    mProgramItemMediumMarkedColorLb.setStandardColor(programItemMediumDefaultMarkedColor);
    markings.add(new ColorButton(mProgramItemMediumMarkedColorLb), cc.xy(5,3));

    Color programItemMaxMarkedColor = Settings.propProgramTableMarkedMaxPriorityColor.getColor();
    Color programItemMaxDefaultMarkedColor = Settings.propProgramTableMarkedMaxPriorityColor.getDefaultColor();
    
    markings.add(new JLabel(mLocalizer.msg("color.maxPriority","Maximum priority")), cc.xy(1,5));
    markings.add(mProgramItemMaxMarkedColorLb = new ColorLabel(programItemMaxMarkedColor), cc.xy(3,5));
    mProgramItemMaxMarkedColorLb.setStandardColor(programItemMaxDefaultMarkedColor);
    markings.add(new ColorButton(mProgramItemMaxMarkedColorLb), cc.xy(5,5));

    pb.addSeparator(mLocalizer.msg("color.programMarked","Markierung durch Plugins"), cc.xyw(1,1,2));
    pb.add(defaultMarkings, cc.xy(2,3));    
    pb.addSeparator(mLocalizer.msg("color.programMarkedAdditional","Additional colors (replacing default color)"), cc.xyw(1,5,2));    
    pb.add(markings, cc.xy(2,7));
    
    return pb.getPanel();
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("title","Highlighting");
  }

  public void saveSettings() {
    Settings.propProgramTableMarkedDefaultPriorityShowsColor.setBoolean(mProgramItemDefaultMarkedColorShown.isSelected());
    
    Settings.propProgramTableMarkedDefaultPriorityColor.setColor(mProgramItemDefaultMarkedColorLb.getColor());
    Settings.propProgramTableMarkedMinPriorityColor.setColor(mProgramItemMinMarkedColorLb.getColor());
    Settings.propProgramTableMarkedMediumPriorityColor.setColor(mProgramItemMediumMarkedColorLb.getColor());
    Settings.propProgramTableMarkedMaxPriorityColor.setColor(mProgramItemMaxMarkedColorLb.getColor());
  }
}
