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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import tvbrowser.core.ChannelList;
import tvbrowser.ui.customizableitems.SortableItemList;
import util.ui.ChannelListCellRenderer;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;

/**
 * TV-Browser
 * 
 * @author Bodo Tasche
 */
public class ChannelsSettingsTabNew3 implements devplugin.SettingsTab {

	private static final util.ui.Localizer mLocalizer = util.ui.Localizer
			.getLocalizerFor(ChannelsSettingsTabNew3.class);

	private boolean mShowAllButtons;

	private JList mAllChannels;

	private JList mSubscribedChannels;

	private JButton mLeftBtn, mRightBtn;

	public ChannelsSettingsTabNew3() {
		this(true);
	}

	public ChannelsSettingsTabNew3(boolean showAllButtons) {
		mShowAllButtons = showAllButtons;
	}


	public JPanel createSettingsPanel() {
		JPanel panel = new JPanel();
//		FormDebugPanel panel = new FormDebugPanel();
		
		FormLayout layout = new FormLayout(
				"default:grow(0.5), 3dlu, default, 3dlu, default:grow(0.5)", 
				"default, 3dlu, default:grow, default, 3dlu, default, default:grow, 3dlu, top:default, 3dlu, default, 3dlu, default");
		
		panel.setLayout(layout);
		panel.setBorder(Borders.DLU4_BORDER);
		CellConstraints c = new CellConstraints();
		
		// Left Box
		panel.add(new JLabel("Verfügbare Kanäle:"), c.xy(1,1));
		
		mAllChannels = new JList(new DefaultListModel());
		mAllChannels.setCellRenderer(new ChannelListCellRenderer());

		panel.add(new JScrollPane(mAllChannels), c.xywh(1,3,1,5));
		
		panel.add(createFilterPanel(), c.xy(1,9));
		
		// Buttons in the Middle
		
		panel.add(new JButton(">"), c.xy(3,4));
		panel.add(new JButton("<"), c.xy(3,6));

		// Right Box
		
		panel.add(new JLabel("Ausgewählte Kanäle:"), c.xy(5,1));

		SortableItemList channelList = new SortableItemList();

		mSubscribedChannels = channelList.getList();
		mSubscribedChannels.setCellRenderer(new ChannelListCellRenderer());
		
		panel.add(channelList, c.xywh(5,3,1,5));

		panel.add(createDetailsPanel(), c.xy(5,9));
		
		// Bottom Buttons
		
		if (mShowAllButtons) {
			panel.add(new JButton("Senderliste aktualisieren"), c.xy(1,13));
			panel.add(new JButton("Ausgewählte Kanäle konfig."), c.xy(5,13));
		}
		
		fillChannelListBox();
		
		return panel;
	}

	/**
	 * @return
	 */
	private Component createFilterPanel() {
		
//		FormDebugPanel panel = new FormDebugPanel();
		JPanel panel = new JPanel();

		panel.setLayout(new FormLayout("default, 3dlu, default:grow", 
				"default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default"));
		
		CellConstraints c = new CellConstraints();
		
		panel.add(new JLabel("Filter:"), c.xyw(1,1, 3));
		panel.add(new JLabel("Land"), c.xy(1, 3));
		panel.add(new JComboBox(), c.xy(3,3));
		panel.add(new JLabel("Zeitzone"), c.xy(1, 5));
		panel.add(new JComboBox(), c.xy(3,5));
		panel.add(new JLabel("Anbieter"), c.xy(1,7));
		panel.add(new JComboBox(), c.xy(3,7));
		panel.add(new JLabel("Name"), c.xy(1,9));
		panel.add(new JTextField(), c.xy(3,9));
		
		return panel;
	}

	
	/**
	 * @return
	 */
	private Component createDetailsPanel() {

//		FormDebugPanel panel = new FormDebugPanel();
		JPanel panel = new JPanel();
		
		panel.setLayout(new FormLayout("default, 3dlu, default:grow", 
				"default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default"));
		
		CellConstraints c = new CellConstraints();
		
		panel.add(new JLabel("Details:"), c.xyw(1,1,3));
		
		panel.add(new JLabel("Kanal:"), c.xy(1,3));
		panel.add(new JLabel("Eurosport"), c.xy(3,3));

		panel.add(new JLabel("Kategorie:"), c.xy(1,5));
		panel.add(new JLabel("Eurosport"), c.xy(3, 5));

		panel.add(new JLabel("Land:"), c.xy(1,7));
		panel.add(new JLabel("Eurosport"), c.xy(3,7));

		panel.add(new JLabel("Zeitzone:"), c.xy(1,9));
		panel.add(new JLabel("Eurosport"), c.xy(3,9));

		panel.add(new JLabel("Betreiber:"), c.xy(1,11));
		panel.add(new JLabel("Eurosport"), c.xy(3,11));

		return panel;
	}

	
	/**
	 * Called by the host-application, if the user wants to save the settings.
	 */
	public void saveSettings() {
	}

	/**
	 * Returns the name of the tab-sheet.
	 */
	public Icon getIcon() {
		return null;
	}

	/**
	 * Returns the title of the tab-sheet.
	 */
	public String getTitle() {
		return "Kanäle";
	}

	private void fillChannelListBox() {

		((DefaultListModel) mSubscribedChannels.getModel()).removeAllElements();
		((DefaultListModel) mAllChannels.getModel()).removeAllElements();

		// Split the channels in subscribed and available
		Channel[] channels= ChannelList.getAvailableChannels();
		int subscribedChannelCount = ChannelList
				.getNumberOfSubscribedChannels();
		Channel[] subscribedChannelArr = new Channel[subscribedChannelCount];
		ArrayList availableChannelList = new ArrayList();
		
		for (int i = 0; i < channels.length; i++) {
			Channel channel = channels[i];

			if (ChannelList.isSubscribedChannel(channel)) {
				int pos = ChannelList.getPos(channel);
				ChannelList.getSubscribedChannels()[pos]
						.copySettingsToChannel(channel);
				subscribedChannelArr[pos] = channel;
			} else {
				availableChannelList.add(channel);
			}
		}

		// Sort the available channels
		Channel[] availableChannelArr = new Channel[availableChannelList.size()];
		availableChannelList.toArray(availableChannelArr);
		Arrays.sort(availableChannelArr, createChannelComparator());

		// Add the available channels
		for (int i = 0; i < availableChannelArr.length; i++) {
			((DefaultListModel) mAllChannels.getModel())
					.addElement(availableChannelArr[i]);
		}

		// Add the subscribed channels
		for (int i = 0; i < subscribedChannelArr.length; i++) {
			((DefaultListModel) mSubscribedChannels.getModel())
					.addElement(subscribedChannelArr[i]);
		}
	}	
	
	private Comparator createChannelComparator() {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				return o1.toString().compareTo(o2.toString());
			}
		};
	}	
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("TestCase");
		
		ChannelsSettingsTabNew3 tab = new ChannelsSettingsTabNew3();
		
		JPanel panel = (JPanel)frame.getContentPane();
		panel.setLayout(new BorderLayout());
		panel.add(tab.createSettingsPanel(), BorderLayout.CENTER);
		frame.pack();
		frame.setSize(400, 500);
		frame.show();
	
	}
}