/*
 * VirtualDataService by Reinhard Lehrbaum
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
package virtualdataservice;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import tvdataservice.SettingsPanel;
import util.ui.Localizer;
import util.ui.UiUtilities;
import virtualdataservice.ui.ProgramEditor;
import virtualdataservice.virtual.VirtualChannel;
import virtualdataservice.virtual.VirtualChannelManager;
import virtualdataservice.virtual.VirtualProgram;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Plugin;

public class VirtualDataServiceSettingsPanel extends SettingsPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private static final Localizer mLocalizer = Localizer.getLocalizerFor(VirtualDataServiceSettingsPanel.class);

	private static final String mDateFormat = "yyyy-MM-dd HH:mm";

	private VirtualChannelManager mChannelManager;

	private DefaultListModel mChannels;
	private DefaultTableModel mPrograms;
	private JList mChannelList;
	private JTable mProgramList;
	private JButton mChannelAdd;
	private JButton mChannelDel;
	private JButton mChannelEdit;
	private JButton mProgramAdd;
	private JButton mProgramDel;
	private JButton mProgramEdit;

	public VirtualDataServiceSettingsPanel(final String workingDirectory)
	{
		mChannelManager = new VirtualChannelManager(workingDirectory);

		setLayout(new BorderLayout());
		setBorder(Borders.createEmptyBorder(Sizes.DLUY5, Sizes.DLUX5, Sizes.DLUY5, Sizes.DLUX5));

		final FormLayout layout = new FormLayout(
        "5dlu, default, 3dlu, pref, 5dlu",
        "5dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, pref:grow, 10dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, pref:grow");

		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		final CellConstraints cc = new CellConstraints();

		mChannels = new DefaultListModel();
		mChannelList = new JList(mChannels);
		mChannelList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(final ListSelectionEvent e)
			{
				setChannelButtons(true);
				loadProgram(getChannel());
			}
		});
		mChannelList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		final JScrollPane scrollChannel = new JScrollPane(mChannelList);

		mPrograms = new ProgramTableModel();
		mPrograms.addColumn(" ");
		mPrograms.addColumn(mLocalizer.msg("start", "Start"));
		mPrograms.addColumn(mLocalizer.msg("title", "Title"));
		mProgramList = new JTable(mPrograms);
		mProgramList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(final ListSelectionEvent evt)
			{
				setProgramButtons(true, true);
			}
		});
		mProgramList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mProgramList.getColumnModel().getColumn(0).setMaxWidth(Sizes.dialogUnitXAsPixel(10, mProgramList));
		final JScrollPane scrollProgram = new JScrollPane(mProgramList);

		mChannelAdd = new JButton(Localizer.getLocalization(Localizer.I18N_ADD),
        Plugin.getPluginManager().getIconFromTheme(null, "actions",
            "document-new", 16));
		mChannelAdd.setToolTipText(mLocalizer.msg("addChannel", "Add channel"));
		mChannelAdd.setHorizontalAlignment(SwingConstants.LEFT);
		mChannelAdd.addActionListener(this);
		mChannelDel = new JButton(Localizer.getLocalization(Localizer.I18N_DELETE), Plugin.getPluginManager().getIconFromTheme(null, "actions", "edit-delete", 16));
		mChannelDel.setToolTipText(mLocalizer.msg("delChannel", "Delete channel"));
		mChannelDel.setHorizontalAlignment(SwingConstants.LEFT);
		mChannelDel.setEnabled(false);
		mChannelDel.addActionListener(this);
		mChannelEdit = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT), Plugin.getPluginManager().getIconFromTheme(null, "actions", "document-edit", 16));
		mChannelEdit.setToolTipText(mLocalizer.msg("editChannel", "Edit channel"));
		mChannelEdit.setHorizontalAlignment(SwingConstants.LEFT);
		mChannelEdit.setEnabled(false);
		mChannelEdit.addActionListener(this);
		mProgramAdd = new JButton(Localizer.getLocalization(Localizer.I18N_ADD), Plugin.getPluginManager().getIconFromTheme(null, "actions", "document-new", 16));
		mProgramAdd.setToolTipText(mLocalizer.msg("addProgram", "Add program"));
		mProgramAdd.setHorizontalAlignment(SwingConstants.LEFT);
		mProgramAdd.setEnabled(false);
		mProgramAdd.addActionListener(this);
		mProgramDel = new JButton(Localizer.getLocalization(Localizer.I18N_DELETE), Plugin.getPluginManager().getIconFromTheme(null, "actions", "edit-delete", 16));
		mProgramDel.setToolTipText(mLocalizer.msg("delProgram", "Delete program"));
		mProgramDel.setHorizontalAlignment(SwingConstants.LEFT);
		mProgramDel.setEnabled(false);
		mProgramDel.addActionListener(this);
		mProgramEdit = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT), Plugin.getPluginManager().getIconFromTheme(null, "actions", "document-edit", 16));
		mProgramEdit.setToolTipText(mLocalizer.msg("editProgram", "Edit program"));
		mProgramEdit.setHorizontalAlignment(SwingConstants.LEFT);
		mProgramEdit.setEnabled(false);
		mProgramEdit.addActionListener(this);

		int row = 2;
		builder.addLabel(Localizer.getLocalization(Localizer.I18N_CHANNEL), cc.xyw(2, row, 3));
		row += 2;
		builder.add(scrollChannel, cc.xywh(2, row, 1, 6));
		builder.add(mChannelAdd, cc.xy(4, row));
		row += 2;
		builder.add(mChannelDel, cc.xy(4, row));
		row += 2;
		builder.add(mChannelEdit, cc.xy(4, row));
		row += 3;
		builder.addLabel(Localizer.getLocalization(Localizer.I18N_PROGRAM), cc.xyw(2, row, 3));
		row += 2;
		builder.add(scrollProgram, cc.xywh(2, row, 1, 6));
		builder.add(mProgramAdd, cc.xy(4, row));
		row += 2;
		builder.add(mProgramDel, cc.xy(4, row));
		row += 2;
		builder.add(mProgramEdit, cc.xy(4, row));

		add(builder.getPanel(), BorderLayout.CENTER);

		loadChannel();
	}

	private void loadChannel()
	{
		mChannels.clear();
		final List<VirtualChannel> channels = mChannelManager.getChannels();
		Collections.sort(channels);
		for (VirtualChannel channel : channels)
		{
			mChannels.addElement(channel);
		}
		setChannelButtons(false);
	}

	private void loadProgram(final VirtualChannel channel)
	{
		while (mPrograms.getRowCount() > 0)
		{
			mPrograms.removeRow(0);
		}
		final List<VirtualProgram> programs = channel.getPrograms();
		Collections.sort(programs);
		for (VirtualProgram program : programs)
		{
			mPrograms.addRow(getRowData(program));
		}
		setProgramButtons(true, false);
	}

	private Vector<String> getRowData(final VirtualProgram program)
	{
	  final Vector<String> row = new Vector<String>();

		String repeater = " ";
		if (program.getRepeat() != null)
		{
			switch (program.getRepeat().getID())
			{
				case 1:
					repeater = "T";
					break;
				case 2:
					repeater = "W";
					break;
				case 3:
				case 4:
					repeater = "M";
					break;
				case 5:
					repeater = "J";
					break;
			}
		}
		row.add(repeater);
		final SimpleDateFormat format = new SimpleDateFormat(mDateFormat);
		row.add(format.format(program.getStart().getTime()));
		row.add(program.getTitle());

		return row;
	}

	public void ok()
	{
		mChannelManager.save();
		VirtualDataService.getInstance().clearChannelList();
		setChannelButtons(false);
	}

	private void setChannelButtons(final boolean enable)
	{
		mChannelDel.setEnabled(enable);
		mChannelEdit.setEnabled(enable);
		setProgramButtons(enable, false);
	}

	private void setProgramButtons(final boolean enable, final boolean all)
	{
		mProgramAdd.setEnabled(enable);
		mProgramDel.setEnabled(enable && all);
    mProgramEdit.setEnabled(enable && all);
	}

	public void actionPerformed(final ActionEvent evt)
	{
		if (evt.getSource() == mChannelAdd)
		{
		  final String channelName = (String) JOptionPane.showInputDialog(this,
          mLocalizer.msg("enterChannelName",
              "Please enter the name of the channel"), mLocalizer.msg(
              "enterChannelNameTitle", "Add channel"),
          JOptionPane.PLAIN_MESSAGE, null, null, "");

			if (channelName != null && channelName.length() > 0)
			{
				addChannel(channelName);
			}
		}
		else if (evt.getSource() == mChannelDel)
		{
		  final VirtualChannel channel = getChannel();
			if (channel != null)
			{
				mChannelManager.removeChannel(channel);
				mChannels.removeElement(channel);
				if (mChannels.size() == 0)
				{
					setChannelButtons(false);
				}
				setChannelButtons(false);
			}
		}
		else if (evt.getSource() == mChannelEdit)
		{
		  final VirtualChannel channel = getChannel();
			if (channel != null)
			{
			  final String channelName = (String) JOptionPane.showInputDialog(this,
            mLocalizer.msg("enterChannelName",
                "Please enter the name of the channel"), mLocalizer.msg(
                "enterChannelNameTitle", "Add channel"),
            JOptionPane.PLAIN_MESSAGE, null, null, channel.getName());

				if (channelName != null && channelName.length() > 0)
				{
					channel.setName(channelName);
					mChannelList.updateUI();
					setChannelButtons(true);
				}
			}
		}
		else if (evt.getSource() == mProgramAdd)
		{
			addProgram(getChannel());
		}
		else if (evt.getSource() == mProgramDel)
		{
		  final int index = mProgramList.getSelectedRow();
			if (index >= 0)
			{
			  final VirtualProgram program = getProgram(index);
				if (program != null)
				{
					getChannel().removeProgram(program);
					mPrograms.removeRow(index);
				}
			}
		}
		else if (evt.getSource() == mProgramEdit)
		{
			editProgram(getChannel(), getProgram(mProgramList.getSelectedRow()));
		}
	}

	private void addChannel(final String name)
	{
		if (mChannels.indexOf(name) < 0)
		{
			mChannelManager.addChannel(name);
			//mChannels.addElement(channel);
			loadChannel();
		}
	}

	private void addProgram(final VirtualChannel channel)
	{
		ProgramEditor editor = new ProgramEditor(JOptionPane.getFrameForComponent(this));
		try
		{
			editor.setModal(true);
			UiUtilities.centerAndShow(editor);
			final VirtualProgram program = editor.getProgram();

			if (program != null)
			{
				channel.addProgram(program);
				loadProgram(channel);
			}
		}
		finally
		{
			editor.dispose();
			editor = null;
		}
	}

	private void editProgram(final VirtualChannel channel,
      final VirtualProgram program)
	{
		ProgramEditor editor = new ProgramEditor(JOptionPane.getFrameForComponent(this));
		try
		{
			editor.setModal(true);
			editor.setProgram(program);
			UiUtilities.centerAndShow(editor);
			//editor.getProgram();

			if (editor.getProgram() != null)
			{
				loadProgram(channel);
			}
		}
		finally
		{
			editor.dispose();
			editor = null;
		}
	}

	private VirtualChannel getChannel()
	{
		return (VirtualChannel) mChannelList.getSelectedValue();
	}

	private VirtualProgram getProgram(final int index)
	{
	  final String date = (String) mPrograms.getValueAt(index, 1);
    final String title = (String) mPrograms.getValueAt(index, 2);
		Calendar startDate = null;

		final DateFormat formatter = new SimpleDateFormat(mDateFormat);
		try
		{
			startDate = Calendar.getInstance();
			startDate.setTime(formatter.parse(date));
		}
		catch (Exception ex)
		{}
		final VirtualChannel channel = getChannel();
		for (VirtualProgram program : channel.getPrograms())
		{
// modified by jb:
		  if (program.getTitle().equals(title) && program.getStart().get(Calendar.HOUR_OF_DAY)== startDate.get(Calendar.HOUR_OF_DAY) && program.getStart().get(Calendar.MINUTE)== startDate.get(Calendar.MINUTE))
// modified by jb //
		    {
				return program;
			}
		}
		return null;
	}

	private static class ProgramTableModel extends DefaultTableModel
	{
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(final int arg0, final int arg1)
		{
			return false;
		}
	}
}