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
package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.plugin.programformating.GlobalPluginProgramFormating;
import tvbrowser.core.plugin.programformating.GlobalPluginProgramFormatingManager;
import tvbrowser.ui.mainframe.MainFrame;
import util.program.AbstractPluginProgramFormating;
import util.ui.LocalPluginProgramFormatingSettingsDialog;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.customizableitems.SortableItemList;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The settings for the global program configurations.
 *
 * @author René Mach
 */
public class GlobalPluginProgramFormatingSettings implements SettingsTab, ActionListener {
  /** The localizer for this class */
  public static final Localizer mLocalizer = Localizer.getLocalizerFor(GlobalPluginProgramFormatingSettings.class);

  private SortableItemList mConfigurations;
  private JButton mAdd, mEdit, mDelete;

  public JPanel createSettingsPanel() {
    try {
      CellConstraints cc = new CellConstraints();
      PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default:grow,5dlu","pref,5dlu,fill:default:grow,5dlu,pref,10dlu,pref"));
      pb.setDefaultDialogBorder();

      mConfigurations = new SortableItemList("",GlobalPluginProgramFormatingManager.getInstance().getAvailableGlobalPluginProgramFormatings());
      mConfigurations.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      mConfigurations.getList().addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
            LocalPluginProgramFormatingSettingsDialog.createInstance(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), (AbstractPluginProgramFormating)mConfigurations.getList().getSelectedValue(), GlobalPluginProgramFormatingManager.getDefaultConfiguration(), true, true);
            mConfigurations.getList().repaint();
          }
        }
      });

      pb.addSeparator(mLocalizer.msg("title","Plugin program formating"), cc.xyw(1,1,3));
      pb.add(mConfigurations, cc.xy(2,3));

      FormLayout layout = new FormLayout("default,5dlu,default,5dlu,default","pref");
      layout.setColumnGroups(new int[][] {{1,3,5}});

      JPanel buttonPanel = new JPanel(layout);

      mAdd = new JButton(Localizer.getLocalization(Localizer.I18N_ADD));
      mAdd.setIcon(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
      mAdd.addActionListener(this);

      mEdit = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT));
      mEdit.setIcon(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
      mEdit.setEnabled(false);
      mEdit.addActionListener(this);

      mDelete = new JButton(Localizer.getLocalization(Localizer.I18N_DELETE));
      mDelete.setIcon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
      mDelete.setEnabled(false);
      mDelete.addActionListener(this);

      buttonPanel.add(mAdd, cc.xy(1,1));
      buttonPanel.add(mEdit, cc.xy(3,1));
      buttonPanel.add(mDelete, cc.xy(5,1));

      pb.add(buttonPanel, cc.xy(2,5));
      pb.addLabel(mLocalizer.msg("help","<html>This list of formating can be used by several plugins. So a formating don't have to be entered in every plugin that should use the formating. The selection of the formating can be done in the settings of the plugin.</html>"), cc.xy(2,7));

      mConfigurations.getList().addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if(!e.getValueIsAdjusting()) {
            mEdit.setEnabled(mConfigurations.getList().getSelectedIndex() != -1);
            mDelete.setEnabled(mConfigurations.getList().getSelectedIndex() != -1);
          }
        }
      });

      return pb.getPanel();

    }catch(Exception e) {e.printStackTrace();}
    return null;
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("title","Plugin program formating");
  }

  public void saveSettings() {
    Object[] o = mConfigurations.getItems();

    if(o != null && o.length > 0) {
      GlobalPluginProgramFormating[] p = new GlobalPluginProgramFormating[o.length];

      for(int i = 0; i < o.length; i++) {
        p[i] = (GlobalPluginProgramFormating)o[i];
      }

      GlobalPluginProgramFormatingManager.getInstance().setAvailableProgramConfigurations(p);
    }
    else {
      final GlobalPluginProgramFormating[] formating = new GlobalPluginProgramFormating[2];
      formating[0] = GlobalPluginProgramFormatingManager.getDefaultConfiguration();

      Thread t = new Thread("Plugin formating setting") {
        public void run() {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {}

          formating[1] = GlobalPluginProgramFormatingManager.getTvPearlFormating();
        }
      };

      t.start();

      try {
        t.join();
      } catch (InterruptedException e) {}

      GlobalPluginProgramFormatingManager.getInstance().setAvailableProgramConfigurations(formating);
    }

    GlobalPluginProgramFormatingManager.getInstance().store();
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == mAdd) {
      mConfigurations.addElement(GlobalPluginProgramFormatingManager.getDefaultConfiguration());
    } else if(e.getSource() == mDelete) {
      mConfigurations.removeElementAt(mConfigurations.getList().getSelectedIndex());
    } else if(e.getSource() == mEdit) {
      LocalPluginProgramFormatingSettingsDialog.createInstance(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), (AbstractPluginProgramFormating)mConfigurations.getList().getSelectedValue(), GlobalPluginProgramFormatingManager.getDefaultConfiguration(), true, true);
      mConfigurations.getList().repaint();
    }
  }
}

