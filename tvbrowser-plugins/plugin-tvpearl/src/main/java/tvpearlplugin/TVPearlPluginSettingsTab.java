/*
 * TV-Pearl by Reinhard Lehrbaum
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
package tvpearlplugin;

import devplugin.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import util.ui.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

public class TVPearlPluginSettingsTab implements SettingsTab
{
	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TVPearlPluginSettingsTab.class);

	private JCheckBox mUpdateAtStart;
	private JCheckBox mUpdateAfterUpdateFinished;
	private JCheckBox mUpdateManual;
	private JComboBox mViewOption;
	private JCheckBox mMarkPearl;
	private JComboBox mMarkPriority;
	private JCheckBox mShowInfoModal;
	private JCheckBox mEnableFilter;
	private JRadioButton mFilterShowOnly;
	private JRadioButton mFilterShowNot;
	private JTextArea mFilterComposer;
	private JLabel mPluginLabel;

	private ProgramReceiveTarget[] mClientPluginTargets;

	public JPanel createSettingsPanel()
	{
		FormLayout layout = new FormLayout("5dlu, pref, 3dlu, fill:pref:grow, 5dlu", "5dlu, pref, 2dlu, pref, 10dlu, pref, 2dlu, pref, 2dlu, pref, 10dlu, pref, 2dlu, pref, 10dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 10dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref");

		PanelBuilder builder = new PanelBuilder(layout, new ScrollableJPanel());
		// builder.setDefaultDialogBorder();
		builder.setBorder(null);

		CellConstraints cc = new CellConstraints();

		mUpdateAtStart = new JCheckBox(mLocalizer.msg("updateAtStart", "Update after TV-Browser start"), TVPearlPlugin.getInstance().getPropertyBoolean("UpdateAtStart"));
		mUpdateAfterUpdateFinished = new JCheckBox(mLocalizer.msg("updateAfterUpdateFinished", "Update after program data download"), TVPearlPlugin.getInstance().getPropertyBoolean("UpdateAfterUpdateFinished"));
		mUpdateManual = new JCheckBox(mLocalizer.msg("updateManual", "Update manual"), TVPearlPlugin.getInstance().getPropertyBoolean("UpdateManual"));
		mViewOption = new JComboBox(getViewOption());
		mViewOption.setSelectedIndex(TVPearlPlugin.getInstance().getPropertyInteger("ViewOption") - 1);
		if (mViewOption.getSelectedIndex() < 0)
		{
			mViewOption.setSelectedIndex(0);
		}
		mMarkPearl = new JCheckBox(mLocalizer.msg("markPearl", "Mark pearls within the TV-Browser"), TVPearlPlugin.getInstance().getPropertyBoolean("MarkPearl"));
		mMarkPriority = new JComboBox(getPriorities());
		mMarkPriority.setRenderer(new MarkPriorityComboBoxRenderer());
		mMarkPriority.setSelectedIndex(TVPearlPlugin.getInstance().getPropertyInteger("MarkPriority") + 1);
		mShowInfoModal = new JCheckBox(mLocalizer.msg("modal", "modal Info dialog"), TVPearlPlugin.getInstance().getPropertyBoolean("ShowInfoModal"));

		mClientPluginTargets = TVPearlPlugin.getInstance().getClientPluginsTargets();
		mPluginLabel = new JLabel();
		handlePluginSelection();

		JButton choose = new JButton(mLocalizer.msg("selectPlugins", "Choose Plugins"));
		choose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					PluginChooserDlg chooser = new PluginChooserDlg((JFrame) null, mClientPluginTargets, null, TVPearlPlugin.getInstance());
					chooser.setVisible(true);

					if (chooser.getReceiveTargets() != null)
					{
						mClientPluginTargets = chooser.getReceiveTargets();
					}

					handlePluginSelection();
				}
				catch (Exception ee)
				{
					ee.printStackTrace();
				}
			}
		});

		JPanel pluginPanel = new JPanel(new FormLayout("fill:pref:grow, 3dlu, pref", "default"));
		pluginPanel.add(mPluginLabel, cc.xy(1, 1));
		pluginPanel.add(choose, cc.xy(3, 1));

		mEnableFilter = new JCheckBox(mLocalizer.msg("enableFilter", "enable filter"), TVPearlPlugin.getInstance().getPropertyBoolean("ShowEnableFilter"));
		mFilterShowOnly = new JRadioButton(mLocalizer.msg("filterShowOnly", "show only"), TVPearlPlugin.getInstance().getPropertyInteger("ShowFilter") == 0);
		mFilterShowNot = new JRadioButton(mLocalizer.msg("filterShowNot", "show not"), TVPearlPlugin.getInstance().getPropertyInteger("ShowFilter") == 1);
		ButtonGroup group = new ButtonGroup();
		group.add(mFilterShowOnly);
		group.add(mFilterShowNot);
		mFilterComposer = new JTextArea(3, 10);
		mFilterComposer.setText(getComposers());
		JScrollPane scrollComposer = new JScrollPane(mFilterComposer);

		int row = 2;
		builder.addLabel(mLocalizer.msg("view", "View"), cc.xy(2, row));
		builder.add(mViewOption, cc.xy(4, row));
		row += 2;
		builder.add(mShowInfoModal, cc.xyw(2, row, 3));
		row += 2;
		builder.addSeparator(mLocalizer.msg("programTable", "Program table"), cc.xyw(1, row, 5));
		row += 2;
		builder.add(mMarkPearl, cc.xyw(2, row, 3));
		row += 2;
		builder.addLabel(mLocalizer.msg("markPriority", "Mark priority"), cc.xy(2, row));
		builder.add(mMarkPriority, cc.xy(4, row));
		row += 2;
		builder.addSeparator(mLocalizer.msg("sendToPlugin", "Send reminded program to"), cc.xyw(1, row, 5));
		row += 2;
		builder.add(pluginPanel, cc.xyw(2, row, 3));
		row += 2;
		builder.addSeparator(mLocalizer.msg("updateOption", "Update options"), cc.xyw(1, row, 5));
		row += 2;
		builder.add(mUpdateAtStart, cc.xyw(2, row, 3));
		row += 2;
		builder.add(mUpdateAfterUpdateFinished, cc.xyw(2, row, 3));
		row += 2;
		builder.add(mUpdateManual, cc.xyw(2, row, 3));
		row += 2;
		builder.addSeparator(mLocalizer.msg("filter", "Filter"), cc.xyw(1, row, 5));
		row += 2;
		builder.add(mEnableFilter, cc.xyw(2, row, 3));
		row += 2;
		builder.add(mFilterShowOnly, cc.xyw(2, row, 3));
		row += 2;
		builder.add(mFilterShowNot, cc.xyw(2, row, 3));
		row += 2;
		builder.add(scrollComposer, cc.xyw(2, row, 3));
		row += 2;

		JScrollPane scrollPane = new JScrollPane(builder.getPanel());
		scrollPane.setBorder(null);
		scrollPane.setViewportBorder(null);

		JPanel scrollPanel = new JPanel(new FormLayout("default:grow", "default"));
		scrollPanel.setBorder(null);
		scrollPanel.add(scrollPane, cc.xy(1, 1));

		return scrollPanel;
	}

	public Icon getIcon()
	{
		return null;
	}

	public String getTitle()
	{
		return null;
	}

	public void saveSettings()
	{
		TVPearlPlugin.getInstance().setProperty("UpdateAtStart", mUpdateAtStart.isSelected());
		TVPearlPlugin.getInstance().setProperty("UpdateAfterUpdateFinished", mUpdateAfterUpdateFinished.isSelected());
		TVPearlPlugin.getInstance().setProperty("UpdateManual", mUpdateManual.isSelected());
		TVPearlPlugin.getInstance().setProperty("ViewOption", mViewOption.getSelectedIndex() + 1);
		TVPearlPlugin.getInstance().setProperty("MarkPearl", mMarkPearl.isSelected());
		TVPearlPlugin.getInstance().setProperty("MarkPriority", mMarkPriority.getSelectedIndex() - 1);
		TVPearlPlugin.getInstance().setProperty("ShowInfoModal", mShowInfoModal.isSelected());
		TVPearlPlugin.getInstance().setProperty("ShowEnableFilter", mEnableFilter.isSelected());
		TVPearlPlugin.getInstance().setProperty("ShowFilter", mFilterShowOnly.isSelected() ? 0 : 1);
		
		TVPearlPlugin.getInstance().setClientPluginsTargets(mClientPluginTargets);
		
		Vector<String> composers = new Vector<String>();
		for (String item : mFilterComposer.getText().split("\n"))
		{
			item = item.trim();
			if (item.length() > 0)
			{
				composers.add(item);
			}
		}
		TVPearlPlugin.getInstance().setComposers(composers);

		TVPearlPlugin.getInstance().updateChanges();
	}

	private Vector<String> getViewOption()
	{
		Vector<String> result = new Vector<String>();

		result.add(mLocalizer.msg("viewAll", "View all pearls"));
		result.add(mLocalizer.msg("viewChannel", "View pearls from subscribed channel"));
		result.add(mLocalizer.msg("viewProgram", "View only pearls that where found within the local program data"));

		return result;
	}

	private Vector<String> getPriorities()
	{
		Vector<String> result = new Vector<String>();

		result.add(mLocalizer.msg("noPriority", "None"));
		result.add(mLocalizer.msg("min", "Minimum"));
		result.add(mLocalizer.msg("lowerMedium", "Lower Medium"));
		result.add(mLocalizer.msg("medium", "Medium"));
		result.add(mLocalizer.msg("higherMedium", "Higher Medium"));
		result.add(mLocalizer.msg("max", "Maximum"));

		return result;
	}

	private String getComposers()
	{
		String result = "";
		for (String item : TVPearlPlugin.getInstance().getComposers())
		{
			result += item + "\n";
		}
		return result.trim();
	}

	private void handlePluginSelection()
	{
		ArrayList<ProgramReceiveIf> plugins = new ArrayList<ProgramReceiveIf>();

		if (mClientPluginTargets != null)
		{
			for (ProgramReceiveTarget target : mClientPluginTargets)
			{
				if (!plugins.contains(target.getReceifeIfForIdOfTarget()))
				{
					plugins.add(target.getReceifeIfForIdOfTarget());
				}
			}

			ProgramReceiveIf[] mClientPlugins = plugins.toArray(new ProgramReceiveIf[plugins.size()]);

			if (mClientPlugins.length > 0)
			{
				mPluginLabel.setText(mClientPlugins[0].toString());
				mPluginLabel.setEnabled(true);
			}
			else
			{
				mPluginLabel.setText(mLocalizer.msg("noPlugins", "No Plugins choosen"));
				mPluginLabel.setEnabled(false);
			}

			for (int i = 1; i < (mClientPlugins.length > 4 ? 3 : mClientPlugins.length); i++)
			{
				mPluginLabel.setText(mPluginLabel.getText() + ", " + mClientPlugins[i]);
			}

			if (mClientPlugins.length > 4)
			{
				mPluginLabel.setText(mPluginLabel.getText() + " (" + (mClientPlugins.length - 3) + " " + mLocalizer.msg("otherPlugins", "others...") + ")");
			}
		}
	}
}
