/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.settings;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import tvbrowser.core.Settings;
import tvbrowser.core.contextmenu.ConfigMenuItem;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.contextmenu.DoNothingContextMenuItem;
import tvbrowser.core.contextmenu.LeaveFullScreenMenuItem;
import tvbrowser.core.contextmenu.SelectProgramContextMenuItem;
import tvbrowser.core.contextmenu.SeparatorMenuItem;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import util.settings.StringProperty;
import util.ui.CustomComboBoxRenderer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Plugin;
import devplugin.Program;

public class MouseSettingsTab implements devplugin.SettingsTab {

	private static final util.ui.Localizer mLocalizer = util.ui.Localizer
			.getLocalizerFor(MouseSettingsTab.class);

	private ArrayList<MouseClickSetting> mSettings = new ArrayList<MouseSettingsTab.MouseClickSetting>();

	public JPanel createSettingsPanel() {
		PanelBuilder contentPanel = new PanelBuilder(
				new FormLayout("5dlu, pref, 3dlu, pref, fill:pref:grow, 3dlu",
						"pref, 5dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
		contentPanel.setBorder(Borders.DIALOG_BORDER);

		CellConstraints cc = new CellConstraints();
		contentPanel
				.addSeparator(mLocalizer.msg("title", "Title"), cc.xyw(1, 1, 6));

		contentPanel.add(
				new JLabel(mLocalizer.msg("MouseButtons", "Mouse Buttons:")),
				cc.xyw(2, 3, 4));

		int row = 5;
		for (MouseClickSetting clickSetting : mSettings) {
			contentPanel.add(new JLabel(clickSetting.mLabel), cc.xy(2, row));
			contentPanel.add(clickSetting.createComboxBox(), cc.xy(4, row));
			row += 2;
		}
		return contentPanel.getPanel();
	}

	public void saveSettings() {
		for (MouseClickSetting clickSetting : mSettings) {
			clickSetting.saveSetting();
		}
		ContextMenuManager.getInstance().init();
	}

	public Icon getIcon() {
		return IconLoader.getInstance().getIconFromTheme("devices", "input-mouse",
				16);
	}

	public String getTitle() {
		return mLocalizer.msg("title", "context menu");
	}

	private static class ContextMenuCellRenderer extends CustomComboBoxRenderer {
		public ContextMenuCellRenderer(ListCellRenderer backendRenderer) {
		  super(backendRenderer);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			JLabel label = (JLabel) getBackendRenderer().getListCellRendererComponent(list, value,
					index, isSelected, cellHasFocus);

			if (value instanceof ContextMenuIf) {
				ContextMenuIf menuIf = (ContextMenuIf) value;
				Program exampleProgram = Plugin.getPluginManager().getExampleProgram();

				// Get the context menu item text
				StringBuilder text = new StringBuilder();
				Icon icon = null;
				ActionMenu actionMenu = menuIf.getContextMenuActions(exampleProgram);
				if (actionMenu != null) {
					Action action = actionMenu.getAction();
					if (action != null) {
						text.append((String) action.getValue(Action.NAME));
						icon = (Icon) action.getValue(Action.SMALL_ICON);
					} else if (menuIf instanceof PluginProxy) {
						text.append(((PluginProxy) menuIf).getInfo().getName());
						icon = ((PluginProxy) menuIf).getMarkIcon();
					} else {
						text.append("unknown");
						icon = null;
					}
				}
				label.setIcon(icon);
        label.setText(text.toString());
			}

			return label;
		}

	}

	public MouseSettingsTab() {
		mSettings.add(new MouseClickSetting(ContextMenuManager.getInstance()
				.getLeftSingleClickIf(), Settings.propLeftSingleClickIf, mLocalizer
				.msg("leftSingleClickLabel", "Left single click")));
		mSettings.add(new MouseClickSetting(ContextMenuManager.getInstance()
				.getDefaultContextMenuIf(), Settings.propDoubleClickIf, mLocalizer.msg(
				"doubleClickLabel", "Double click")));
		mSettings.add(new MouseClickSetting(ContextMenuManager.getInstance()
				.getMiddleClickIf(), Settings.propMiddleClickIf, mLocalizer.msg(
				"middleClickLabel", "Middle single click")));
		mSettings.add(new MouseClickSetting(ContextMenuManager.getInstance()
				.getMiddleDoubleClickIf(), Settings.propMiddleDoubleClickIf, mLocalizer
				.msg("middleDoubleClickLabel", "Middle double click")));
		mSettings.add(new MouseClickSetting(ContextMenuManager.getInstance()
				.getLeftSingleCtrlClickIf(), Settings.propLeftSingleCtrlClickIf,
				mLocalizer.msg("leftCtrlClickLabel", "Ctrl left click")));
	}

	private static class MouseClickSetting {
		private String mLabel;
		private ContextMenuIf mClickInterface;
		private StringProperty mSettingsProperty;
		private JComboBox mComboBox;

		public MouseClickSetting(ContextMenuIf clickIf,
				StringProperty settingsProperty, final String label) {
			mClickInterface = clickIf;
			mLabel = label;
			mSettingsProperty = settingsProperty;
		}

		public void saveSetting() {
			ContextMenuIf selectedIf = (ContextMenuIf) mComboBox.getSelectedItem();
			if (selectedIf != null) {
				mSettingsProperty.setString(selectedIf.getId());
			} else {
				mSettingsProperty.setString(null);
			}
			mClickInterface = selectedIf;
		}

		public JComboBox createComboxBox() {
			mComboBox = new JComboBox();
			mComboBox.setSelectedItem(mClickInterface);
			mComboBox.setMaximumRowCount(15);
			mComboBox.setRenderer(new ContextMenuCellRenderer(mComboBox.getRenderer()));
			mComboBox.removeAllItems();
			DoNothingContextMenuItem doNothing = DoNothingContextMenuItem
					.getInstance();
			mComboBox.addItem(doNothing);
			mComboBox.addItem(SelectProgramContextMenuItem.getInstance());
			fillListBox();
			if (mClickInterface != null) {
				mComboBox.setSelectedItem(mClickInterface);
			} else {
				mComboBox.setSelectedItem(doNothing);
			}
			return mComboBox;
		}

		private void fillListBox() {
			ContextMenuIf[] menuIfList = ContextMenuManager.getInstance()
					.getAvailableContextMenuIfs(true, false);
			Program exampleProgram = Plugin.getPluginManager().getExampleProgram();
			for (ContextMenuIf element : menuIfList) {
				if (element instanceof SeparatorMenuItem) {
				} else if (element instanceof ConfigMenuItem
						|| element instanceof LeaveFullScreenMenuItem) {
				} else {
					ActionMenu actionMenu = element.getContextMenuActions(exampleProgram);
					if (actionMenu != null) {
						mComboBox.addItem(element);
					}
				}
			}
		}
	}
}