/*
 * Timeline by Reinhard Lehrbaum
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
package timelineplugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import timelineplugin.format.TextFormatter;
import util.paramhandler.ParamLibrary;
import util.ui.FontChooserPanel;
import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.SettingsTab;

public class TimelinePluginSettingsTab implements SettingsTab
{
	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TimelinePluginSettingsTab.class);

	private JComboBox mProgressBar;
	private JLabel mFocusDeltaLabel;
	private JSlider mFocusDelta;
	private JCheckBox mAutoStart;
	private JCheckBox mStartWithNow;
	private JCheckBox mResizeWithMouse;
	private JFormattedTextField mHourWidth;
	private JFormattedTextField mChannelHeight;
	private JTextArea mFormat;
	private FontChooserPanel mFontPanel;
	private JComboBox mKeyList;
	private JComboBox mFunctionList;
	private ParamLibrary mParamLibrary;
	private JPanel mPreview;
	private ProgramLabel mPreviewProgram;
	private JLabel mPreviewError;
	private JLabel mDurationLabel;
	private JSlider mDuration;
	private JRadioButton mShowIconAndName;
	private JRadioButton mShowName;
	private JRadioButton mShowIcon;

	public JPanel createSettingsPanel()
	{
		JTabbedPane tab = new JTabbedPane();
		tab.setBorder(null);
		JScrollPane scrollCommonSettings = new JScrollPane(getCommonSettings());
		scrollCommonSettings.setBorder(null);
		tab.addTab(mLocalizer.msg("common", "Common"), scrollCommonSettings);
		JScrollPane scrollFormatSettings = new JScrollPane(getFormatSettings());
		scrollFormatSettings.setBorder(null);
		tab.addTab(mLocalizer.msg("label", "Label"), scrollFormatSettings);

		CellConstraints cc = new CellConstraints();

		JPanel p = new JPanel(new FormLayout("default:grow", "5dlu,fill:default:grow"));
		p.add(tab, cc.xy(1, 2));

		return p;
	}

	private JPanel getCommonSettings()
	{
		FormLayout layout = new FormLayout("5dlu, 5dlu, pref, 3dlu, fill:pref:grow, 3dlu, pref, 5dlu", "5dlu, pref, 2dlu, pref, 10dlu, pref, 5dlu, pref, 2dlu, pref, 10dlu, pref, 5dlu, pref, 2dlu, pref, 2dlu, pref, 10dlu, pref, 5dlu, pref, 2dlu, pref, 2dlu, pref");

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		CellConstraints cc = new CellConstraints();

		mProgressBar = new JComboBox(getProgressBarOption());
		if (TimelinePlugin.getInstance().showBar())
		{
			mProgressBar.setSelectedIndex(1);
		}
		mFocusDelta = new JSlider(JSlider.HORIZONTAL, 0, 100, TimelinePlugin.getInstance().getFocusDelta());
		mFocusDelta.setToolTipText("xxx");
		mFocusDelta.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JSlider source = (JSlider) e.getSource();
				mFocusDeltaLabel.setText(Integer.toString(source.getValue() - 50));
			}
		});
		mFocusDeltaLabel = new JLabel(Integer.toString(mFocusDelta.getValue() - 50));

		mAutoStart = new JCheckBox(mLocalizer.msg("showAtStart", "Show at startup"));
		mAutoStart.setSelected(TimelinePlugin.getInstance().showAtStartUp());
		mStartWithNow = new JCheckBox(mLocalizer.msg("startWithNow", "Select Now at startup"));
		mStartWithNow.setSelected(TimelinePlugin.getInstance().startWithNow());

		mResizeWithMouse = new JCheckBox(mLocalizer.msg("resizeWithMouse", "Resize with the mouse"));
		mResizeWithMouse.setSelected(TimelinePlugin.getInstance().resizeWithMouse());

		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setGroupingUsed(false);

		mHourWidth = new JFormattedTextField(nf);
		mHourWidth.setText(Integer.toString(TimelinePlugin.getInstance().getHourWidth()));
		mHourWidth.setColumns(10);
		mChannelHeight = new JFormattedTextField(nf);
		mChannelHeight.setText(Integer.toString(TimelinePlugin.getInstance().getChannelHeight()));
		mChannelHeight.setColumns(10);

		JButton resetHour = new JButton(Localizer
        .getLocalization(Localizer.I18N_DEFAULT));
		resetHour.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				mHourWidth.setText("120");
			}
		});
		JButton resetChannel = new JButton(Localizer
        .getLocalization(Localizer.I18N_DEFAULT));
		resetChannel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				mChannelHeight.setText("20");
			}
		});

		mShowIconAndName = new JRadioButton(mLocalizer.msg("showIconName", "Show channel icon and channel name"), TimelinePlugin.getInstance().showChannelName() && TimelinePlugin.getInstance().showChannelIcon());
		mShowName = new JRadioButton(mLocalizer.msg("showName", "Show channel name"), TimelinePlugin.getInstance().showChannelName() && !TimelinePlugin.getInstance().showChannelIcon());
		mShowIcon = new JRadioButton(mLocalizer.msg("showIcon", "Show channel icon"), !TimelinePlugin.getInstance().showChannelName() && TimelinePlugin.getInstance().showChannelIcon());

		ButtonGroup bg1 = new ButtonGroup();
		bg1.add(mShowIconAndName);
		bg1.add(mShowIcon);
		bg1.add(mShowName);

		int row = 2;
		builder.addLabel(mLocalizer.msg("progressView", "Progress view"), cc.xy(3, row));
		builder.add(mProgressBar, cc.xyw(5, row, 3));
		row += 2;
//		builder.addLabel("Focus", cc.xy(3, row));
//		builder.add(mFocusDelta, cc.xy(5, row));
//		builder.add(mFocusDeltaLabel, cc.xy(7, row));
		row += 2;
		builder.addSeparator(mLocalizer.msg("start", "Start"), cc.xyw(2, row, 6));
		row += 2;
		builder.add(mAutoStart, cc.xyw(3, row, 5));
		row += 2;
		builder.add(mStartWithNow, cc.xyw(3, row, 5));
		row += 2;
		builder.addSeparator(mLocalizer.msg("format", "Format"), cc.xyw(2, row, 6));
		row += 2;
		builder.add(mResizeWithMouse, cc.xyw(3, row, 5));
		row += 2;
		builder.addLabel(mLocalizer.msg("hourWidth", "Hour width"), cc.xy(3, row));
		builder.add(mHourWidth, cc.xy(5, row));
		builder.add(resetHour, cc.xy(7, row));
		row += 2;
		builder.addLabel(mLocalizer.msg("channelHeight", "Channel height"), cc.xy(3, row));
		builder.add(mChannelHeight, cc.xy(5, row));
		builder.add(resetChannel, cc.xy(7, row));
		row += 2;
		builder.addSeparator(mLocalizer.msg("iconNameSeparator", "Channel icons/channel name"), cc.xyw(2, row, 6));
		row += 2;
		builder.add(mShowIconAndName, cc.xyw(3, row, 5));
		row += 2;
		builder.add(mShowIcon, cc.xyw(3, row, 5));
		row += 2;
		builder.add(mShowName, cc.xyw(3, row, 5));

		return builder.getPanel();
	}

	private JPanel getFormatSettings()
	{
		FormLayout layout = new FormLayout("5dlu, 5dlu, pref, 3dlu, fill:pref:grow, 3dlu, pref, 5dlu", "5dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 5dlu, pref, 10dlu, pref, 5dlu, pref, 2dlu, pref, 2dlu, pref");

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		CellConstraints cc = new CellConstraints();

		mFormat = new JTextArea(5, 20);
		mFormat.setText(TimelinePlugin.getInstance().getFormat());
		JScrollPane scrollFormat = new JScrollPane(mFormat);
		mFontPanel = new FontChooserPanel(TimelinePlugin.getInstance().getFont());
		JButton addFontBtn = new JButton(Localizer
        .getLocalization(Localizer.I18N_ADD));
		addFontBtn.setToolTipText(mLocalizer.msg("addHint", "Add font"));
		addFontBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String format = mFormat.getText();
				String pre = format.substring(0, mFormat.getSelectionStart());
				String post = format.substring(mFormat.getSelectionStart());
				Font cf = mFontPanel.getChosenFont();
				String choosenFont = String.format("{setFont(%1$s,%2$s,%3$s)}", cf.getName(), cf.getStyle(), cf.getSize());

				mFormat.setText(pre + choosenFont + post);
			}
		});
		JButton resetBtn = new JButton(Localizer
        .getLocalization(Localizer.I18N_DEFAULT));
    resetBtn.setToolTipText(mLocalizer.msg("resetHint", "Reset formatting"));
		resetBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				mFormat.setText(TimelinePlugin.getInstance().getDefaultFormat());
				mFontPanel.selectFont(TimelinePlugin.getInstance().getFont());
			}
		});

		mParamLibrary = new ParamLibrary();

		mKeyList = new JComboBox();
		for (String key : mParamLibrary.getPossibleKeys())
		{
			mKeyList.addItem(mParamLibrary.getDescriptionForKey(key));
		}
		JButton addKeyBtn = new JButton(Localizer
        .getLocalization(Localizer.I18N_ADD));
		addKeyBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String format = mFormat.getText();
				String pre = format.substring(0, mFormat.getSelectionStart());
				String post = format.substring(mFormat.getSelectionStart());
				String key = String.format("{%1$s}", mParamLibrary.getPossibleKeys()[mKeyList.getSelectedIndex()]);

				mFormat.setText(pre + key + post);
			}
		});

		mFunctionList = new JComboBox();
		mFunctionList.addItem("multiline");
		for (String function : mParamLibrary.getPossibleFunctions())
		{
			mFunctionList.addItem(function);
		}
		mFunctionList.setRenderer(new BasicComboBoxRenderer()
		{
			private static final long serialVersionUID = 1L;

			public JComponent getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				if (isSelected)
				{
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
					if (index == 0)
					{
						list.setToolTipText("<html>" + mLocalizer.msg("function_multiline", "Multiline"));
					}
					else if (0 < index)
					{
						list.setToolTipText("<html>" + mParamLibrary.getDescriptionForFunctions(mParamLibrary.getPossibleFunctions()[index - 1]));
					}
				}
				else
				{
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}
				setFont(list.getFont());
				setText((value == null) ? "" : value.toString());
				return this;
			}
		});
		JButton addFunctionBtn = new JButton(Localizer
        .getLocalization(Localizer.I18N_ADD));
		addFunctionBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String format = mFormat.getText();
				String pre = format.substring(0, mFormat.getSelectionStart());
				String post = format.substring(mFormat.getSelectionStart());
				//String function = String.format("{%1$s()}", mParamLibrary.getPossibleFunctions()[mFunctionList.getSelectedIndex()]);
				String function = String.format("{%1$s()}", mFunctionList.getSelectedItem());

				mFormat.setText(pre + function + post);
			}
		});

		int pw = Integer.parseInt(mHourWidth.getText());
		int ph = Integer.parseInt(mChannelHeight.getText());
		mPreview = new JPanel();
		mPreview.setBackground(Color.WHITE);
		mPreview.setPreferredSize(new Dimension(pw + 5, ph + 10));
		mPreviewError = new JLabel(mLocalizer.msg("previewError", "Error"));
		mPreviewProgram = new ProgramLabel();
		mPreviewProgram.setProgram(Plugin.getPluginManager().getExampleProgram());
		mPreviewProgram.setPreferredSize(new Dimension(pw, ph));
		mPreview.add(mPreviewProgram);
		JButton takeChangesBtn = new JButton(mLocalizer.msg("preview", "Preview"));
		takeChangesBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				UpdatePreview();
			}
		});
		mDurationLabel = new JLabel("60");
		mDuration = new JSlider(JSlider.HORIZONTAL, 5, 120, 60);
		mDuration.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				JSlider source = (JSlider) e.getSource();
				mDurationLabel.setText(Integer.toString(source.getValue()));
				UpdatePreview();
			}
		});

		int row = 2;
		builder
        .addLabel(mLocalizer.msg("title", "Formattings:"), cc.xyw(3, row, 5));
		row += 2;
		builder.add(scrollFormat, cc.xyw(3, row, 5));
		row += 2;
		builder.addLabel(mLocalizer.msg("font", "Font"), cc.xy(3, row));
		builder.add(mFontPanel, cc.xyw(5, row, 3));
		row += 2;
		builder.add(addFontBtn, cc.xy(7, row));
		row += 2;
		builder.addLabel(mLocalizer.msg("key", "Key"), cc.xy(3, row));
		builder.add(mKeyList, cc.xy(5, row));
		builder.add(addKeyBtn, cc.xy(7, row));
		row += 2;
		builder.addLabel(mLocalizer.msg("function", "Function"), cc.xy(3, row));
		builder.add(mFunctionList, cc.xy(5, row));
		builder.add(addFunctionBtn, cc.xy(7, row));
		row += 2;
		builder.add(resetBtn, cc.xy(7, row));
		row += 2;
		builder.addSeparator(mLocalizer.msg("preview", "Preview"), cc.xyw(2, row, 6));
		row += 2;
		builder.add(takeChangesBtn, cc.xy(7, row));
		row += 2;
		builder.add(mPreview, cc.xyw(3, row, 5));
		row += 2;
		builder.addLabel(mLocalizer.msg("duration", "Duration"), cc.xy(3, row));
		builder.add(mDuration, cc.xy(5, row));
		builder.add(mDurationLabel, cc.xy(7, row));

		UpdatePreview();

		return builder.getPanel();
	}

	private void UpdatePreview()
	{
		int pw = Math.round(Integer.parseInt(mHourWidth.getText()) / 60.0f * mDuration.getValue());
		int ph = Integer.parseInt(mChannelHeight.getText());
		mPreview.setPreferredSize(new Dimension(pw + 5, ph + 10));
		mPreviewProgram.setPreferredSize(new Dimension(pw, ph));
		TextFormatter formatter = new TextFormatter();
		formatter.setFont(TimelinePlugin.getInstance().getFont());
		formatter.setInitialiseMaxLine(true);
		formatter.setFormat(mFormat.getText());
		if (!formatter.testFormat())
		{
			mPreview.removeAll();
			mPreview.add(mPreviewError);
		}
		else
		{
			mPreview.removeAll();
			mPreview.add(mPreviewProgram);
			mPreviewProgram.setFormatter(formatter);
		}
		mPreview.updateUI();
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
		if ((mShowIconAndName.isSelected() || mShowName.isSelected() != TimelinePlugin.getInstance().showChannelName()) || (mShowIconAndName.isSelected() || mShowIcon.isSelected() != TimelinePlugin.getInstance().showChannelIcon()))
		{
			TimelinePlugin.getInstance().resetChannelWidth();
		}

		TimelinePlugin.getInstance().setProperty("ProgressView", String.valueOf(mProgressBar.getSelectedIndex() + 1));
		TimelinePlugin.getInstance().setProperty("FocusDelta", String.valueOf(mFocusDelta.getValue()));
		TimelinePlugin.getInstance().setProperty("ShowAtStartup", mAutoStart.isSelected());
		TimelinePlugin.getInstance().setProperty("StartWithNow", mStartWithNow.isSelected());
		TimelinePlugin.getInstance().setProperty("ResizeWithMouse", mResizeWithMouse.isSelected());
		TimelinePlugin.getInstance().setProperty("HourWidth", mHourWidth.getText());
		TimelinePlugin.getInstance().setProperty("ChannelHeight", mChannelHeight.getText());
		TimelinePlugin.getInstance().setProperty("ShowChannelName", mShowIconAndName.isSelected() || mShowName.isSelected());
		TimelinePlugin.getInstance().setProperty("ShowChannelIcon", mShowIconAndName.isSelected() || mShowIcon.isSelected());
		TimelinePlugin.getInstance().setFormat(mFormat.getText());
	}

	private Vector<String> getProgressBarOption()
	{
		Vector<String> result = new Vector<String>();

		result.add(mLocalizer.msg("progress", "Progress"));
		result.add(mLocalizer.msg("bar", "Bar"));

		return result;
	}
}
