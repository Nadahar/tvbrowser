/*
 * ToolTipControlPlugin
 * Copyright (C) 12-2007 René Mach
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
 */
package tooltipcontrolplugin;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;

import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * Settings for the ToolTipControlPlugin.
 * 
 * @author René Mach
 */
public final class ToolTipControlPluginSettingsTab implements SettingsTab {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ToolTipControlPluginSettingsTab.class);
  
  private JCheckBox mEnabled;
  private JSpinner mInitialSpinner, mDismissSpinner, mReshowSpinner;
  private Properties mSettings;
  
  protected ToolTipControlPluginSettingsTab(Properties settings) {
    mSettings = settings;
  }
  
  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout("3dlu,pref,3dlu,pref","5dlu,default,5dlu,default,2dlu,default,2dlu,default"));
    
    mInitialSpinner = new JSpinner(new SpinnerNumberModel(Integer.parseInt(mSettings.getProperty("initialDelay")),500,5000,100));
    mDismissSpinner = new JSpinner(new SpinnerNumberModel(Integer.parseInt(mSettings.getProperty("dismissDelay")),500,20000,100));
    mReshowSpinner = new JSpinner(new SpinnerNumberModel(Integer.parseInt(mSettings.getProperty("reshowDelay")),500,5000,100));

    pb.add(mEnabled = new JCheckBox(mLocalizer.msg("enabled","Show tooltips"),mSettings.getProperty("isEnabled","true").equals("true")), cc.xyw(2,2,3));
    
    final JLabel label1 = pb.addLabel(mLocalizer.msg("initialDelay","Wait time until the tooltip should be shown (in milliseconds)"), cc.xy(2,4));
    pb.add(mInitialSpinner, cc.xy(4,4));
    final JLabel label2 = pb.addLabel(mLocalizer.msg("dismissDelay","Wait time until the tooltip should be hidden (in milliseconds)"), cc.xy(2,6));
    pb.add(mDismissSpinner, cc.xy(4,6));
    final JLabel label3 = pb.addLabel(mLocalizer.msg("reshowDelay","Wait time until the tooltip should be shown again (in milliseconds)"), cc.xy(2,8));
    pb.add(mReshowSpinner, cc.xy(4,8));

    mEnabled.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        label1.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        label2.setEnabled(label1.isEnabled());
        label3.setEnabled(label1.isEnabled());
        
        mInitialSpinner.setEnabled(label1.isEnabled());
        mDismissSpinner.setEnabled(label1.isEnabled());
        mReshowSpinner.setEnabled(label1.isEnabled());
      }
    });

    label1.setEnabled(mEnabled.isSelected());
    label2.setEnabled(label1.isEnabled());
    label3.setEnabled(label1.isEnabled());
    
    mInitialSpinner.setEnabled(label1.isEnabled());
    mDismissSpinner.setEnabled(label1.isEnabled());
    mReshowSpinner.setEnabled(label1.isEnabled());

    
    return pb.getPanel();
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return null;
  }

  public void saveSettings() {
    mSettings.setProperty("isEnabled",String.valueOf(mEnabled.isSelected()));
    mSettings.setProperty("initialDelay", String.valueOf(mInitialSpinner.getValue()));
    mSettings.setProperty("dismissDelay", String.valueOf(mDismissSpinner.getValue()));
    mSettings.setProperty("reshowDelay", String.valueOf(mReshowSpinner.getValue()));

    ToolTipManager.sharedInstance().setEnabled(mEnabled.isSelected());
    ToolTipManager.sharedInstance().setInitialDelay(((Integer)mInitialSpinner.getValue()).intValue());
    ToolTipManager.sharedInstance().setDismissDelay(((Integer)mDismissSpinner.getValue()).intValue());
    ToolTipManager.sharedInstance().setReshowDelay(((Integer)mReshowSpinner.getValue()).intValue());
    
    ToolTipControlPlugin.save();
  }

}
