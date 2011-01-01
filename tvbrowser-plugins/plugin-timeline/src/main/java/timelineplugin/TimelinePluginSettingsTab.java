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
import javax.swing.SwingConstants;
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

public final class TimelinePluginSettingsTab implements SettingsTab {
	static final util.ui.Localizer mLocalizer = util.ui.Localizer
			.getLocalizerFor(TimelinePluginSettingsTab.class);

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

	public JPanel createSettingsPanel() {
		final JTabbedPane tab = new JTabbedPane();
		tab.setBorder(null);
		final JScrollPane scrollCommonSettings = new JScrollPane(
				getCommonSettings());
		scrollCommonSettings.setBorder(null);
		tab.addTab(mLocalizer.msg("common", "Common"), scrollCommonSettings);
		final JScrollPane scrollFormatSettings = new JScrollPane(
				getFormatSettings());
		scrollFormatSettings.setBorder(null);
		tab.addTab(mLocalizer.msg("label", "Label"), scrollFormatSettings);

		final CellConstraints cc = new CellConstraints();

		final JPanel p = new JPanel(new FormLayout("default:grow",
				"5dlu,fill:default:grow"));
		p.add(tab, cc.xy(1, 2));

		return p;
	}

	private JPanel getCommonSettings() {
		final FormLayout layout = new FormLayout(
				"5dlu, 5dlu, pref, 3dlu, fill:pref:grow, 3dlu, pref, 5dlu",
				"5dlu, pref, 2dlu, pref, 10dlu, pref, 5dlu, pref, 2dlu, pref, 10dlu, pref, 5dlu, pref, 2dlu, pref, 2dlu, pref, 10dlu, pref, 5dlu, pref, 2dlu, pref, 2dlu, pref");

		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		final CellConstraints cc = new CellConstraints();

		mProgressBar = new JComboBox(getProgressBarOption());
		if (TimelinePlugin.getSettings().showBar()) {
			mProgressBar.setSelectedIndex(1);
		}
		mFocusDelta = new JSlider(SwingConstants.HORIZONTAL, 0, 100, TimelinePlugin
				.getSettings().getFocusDelta());
		mFocusDelta.setToolTipText("xxx");
		mFocusDelta.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();
				mFocusDeltaLabel.setText(Integer.toString(source.getValue() - 50));
			}
		});
		mFocusDeltaLabel = new JLabel(Integer.toString(mFocusDelta.getValue() - 50));

		mAutoStart = new JCheckBox(mLocalizer.msg("showAtStart", "Show at startup"));
		mAutoStart.setSelected(TimelinePlugin.getSettings().showAtStartUp());
		mStartWithNow = new JCheckBox(mLocalizer.msg("startWithNow",
				"Select Now at startup"));
		mStartWithNow.setSelected(TimelinePlugin.getSettings().startWithNow());

		mResizeWithMouse = new JCheckBox(mLocalizer.msg("resizeWithMouse",
				"Resize with the mouse"));
		mResizeWithMouse
				.setSelected(TimelinePlugin.getSettings().resizeWithMouse());

		final NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setGroupingUsed(false);

		mHourWidth = new JFormattedTextField(nf);
		mHourWidth.setText(Integer.toString(TimelinePlugin.getSettings()
				.getHourWidth()));
		mHourWidth.setColumns(10);
		mChannelHeight = new JFormattedTextField(nf);
		mChannelHeight.setText(Integer.toString(TimelinePlugin.getSettings()
				.getChannelHeight()));
		mChannelHeight.setColumns(10);

		final JButton resetHour = new JButton(
				Localizer.getLocalization(Localizer.I18N_DEFAULT));
		resetHour.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				mHourWidth.setText("120");
			}
		});
		final JButton resetChannel = new JButton(
				Localizer.getLocalization(Localizer.I18N_DEFAULT));
		resetChannel.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				mChannelHeight.setText("20");
			}
		});

		mShowIconAndName = new JRadioButton(mLocalizer.msg("showIconName",
				"Show channel icon and channel name"), TimelinePlugin.getSettings()
				.showChannelName() && TimelinePlugin.getSettings().showChannelIcon());
		mShowName = new JRadioButton(
				mLocalizer.msg("showName", "Show channel name"), TimelinePlugin
						.getSettings().showChannelName()
						&& !TimelinePlugin.getSettings().showChannelIcon());
		mShowIcon = new JRadioButton(
				mLocalizer.msg("showIcon", "Show channel icon"), !TimelinePlugin
						.getSettings().showChannelName()
						&& TimelinePlugin.getSettings().showChannelIcon());

		final ButtonGroup bg1 = new ButtonGroup();
		bg1.add(mShowIconAndName);
		bg1.add(mShowIcon);
		bg1.add(mShowName);

		int row = 2;
		builder.addLabel(mLocalizer.msg("progressView", "Progress view"),
				cc.xy(3, row));
		builder.add(mProgressBar, cc.xyw(5, row, 3));
		row += 2;
		// builder.addLabel("Focus", cc.xy(3, row));
		// builder.add(mFocusDelta, cc.xy(5, row));
		// builder.add(mFocusDeltaLabel, cc.xy(7, row));
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
		builder.addLabel(mLocalizer.msg("channelHeight", "Channel height"),
				cc.xy(3, row));
		builder.add(mChannelHeight, cc.xy(5, row));
		builder.add(resetChannel, cc.xy(7, row));
		row += 2;
		builder.addSeparator(
				mLocalizer.msg("iconNameSeparator", "Channel icons/channel name"),
				cc.xyw(2, row, 6));
		row += 2;
		builder.add(mShowIconAndName, cc.xyw(3, row, 5));
		row += 2;
		builder.add(mShowIcon, cc.xyw(3, row, 5));
		row += 2;
		builder.add(mShowName, cc.xyw(3, row, 5));

		return builder.getPanel();
	}

	private JPanel getFormatSettings() {
		final FormLayout layout = new FormLayout(
				"5dlu, 5dlu, pref, 3dlu, fill:pref:grow, 3dlu, pref, 5dlu",
				"5dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 5dlu, pref, 10dlu, pref, 5dlu, pref, 2dlu, pref, 2dlu, pref");

		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		final CellConstraints cc = new CellConstraints();

		mFormat = new JTextArea(5, 20);
		mFormat.setText(TimelinePlugin.getSettings().getTitleFormat());
		final JScrollPane scrollFormat = new JScrollPane(mFormat);
		TimelinePlugin.getInstance();
		mFontPanel = new FontChooserPanel(TimelinePlugin.getFont());
		final JButton addFontBtn = new JButton(
				Localizer.getLocalization(Localizer.I18N_ADD));
		addFontBtn.setToolTipText(mLocalizer.msg("addHint", "Add font"));
		addFontBtn.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final String format = mFormat.getText();
				final String pre = format.substring(0, mFormat.getSelectionStart());
				final String post = format.substring(mFormat.getSelectionStart());
				final Font cf = mFontPanel.getChosenFont();
				final String choosenFont = String.format("{setFont(%1$s,%2$s,%3$s)}",
						cf.getName(), cf.getStyle(), cf.getSize());

				mFormat.setText(pre + choosenFont + post);
			}
		});
		final JButton resetBtn = new JButton(
				Localizer.getLocalization(Localizer.I18N_DEFAULT));
		resetBtn.setToolTipText(mLocalizer.msg("resetHint", "Reset formatting"));
		resetBtn.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				mFormat.setText(TimelinePlugin.getSettings().getDefaultTitleFormat());
				mFontPanel.selectFont(TimelinePlugin.getFont());
			}
		});

		mParamLibrary = new ParamLibrary();

		mKeyList = new JComboBox();
		for (String key : mParamLibrary.getPossibleKeys()) {
			mKeyList.addItem(mParamLibrary.getDescriptionForKey(key));
		}
		final JButton addKeyBtn = new JButton(
				Localizer.getLocalization(Localizer.I18N_ADD));
		addKeyBtn.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final String format = mFormat.getText();
				final String pre = format.substring(0, mFormat.getSelectionStart());
				final String post = format.substring(mFormat.getSelectionStart());
				final String key = String.format("{%1$s}",
						mParamLibrary.getPossibleKeys()[mKeyList.getSelectedIndex()]);

				mFormat.setText(pre + key + post);
			}
		});

		mFunctionList = new JComboBox();
		mFunctionList.addItem("multiline");
		for (String function : mParamLibrary.getPossibleFunctions()) {
			mFunctionList.addItem(function);
		}
		mFunctionList.setRenderer(new BasicComboBoxRenderer() {
			public JComponent getListCellRendererComponent(final JList list,
					final Object value, final int index, final boolean isSelected,
					final boolean cellHasFocus) {
				if (isSelected) {
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
					if (index == 0) {
						list.setToolTipText("<html>"
								+ mLocalizer.msg("function_multiline", "Multiline"));
					} else if (0 < index) {
						list.setToolTipText("<html>"
								+ mParamLibrary.getDescriptionForFunctions(mParamLibrary
										.getPossibleFunctions()[index - 1]));
					}
				} else {
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}
				setFont(list.getFont());
				setText((value == null) ? "" : value.toString());
				return this;
			}
		});
		final JButton addFunctionBtn = new JButton(
				Localizer.getLocalization(Localizer.I18N_ADD));
		addFunctionBtn.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final String format = mFormat.getText();
				final String pre = format.substring(0, mFormat.getSelectionStart());
				final String post = format.substring(mFormat.getSelectionStart());
				// String function = String.format("{%1$s()}",
				// mParamLibrary.getPossibleFunctions()[mFunctionList.getSelectedIndex()]);
				final String function = String.format("{%1$s()}",
						mFunctionList.getSelectedItem());

				mFormat.setText(pre + function + post);
			}
		});

		final int pw = Integer.parseInt(mHourWidth.getText());
		final int ph = Integer.parseInt(mChannelHeight.getText());
		mPreview = new JPanel();
		mPreview.setBackground(Color.WHITE);
		mPreview.setPreferredSize(new Dimension(pw + 5, ph + 10));
		mPreviewError = new JLabel(mLocalizer.msg("previewError", "Error"));
		mPreviewProgram = new ProgramLabel(Plugin.getPluginManager().getExampleProgram());
		mPreviewProgram.setPreferredSize(new Dimension(pw, ph));
		mPreview.add(mPreviewProgram);
		final JButton takeChangesBtn = new JButton(mLocalizer.msg("preview",
				"Preview"));
		takeChangesBtn.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				updatePreview();
			}
		});
		mDurationLabel = new JLabel("60");
		mDuration = new JSlider(SwingConstants.HORIZONTAL, 5, 120, 60);
		mDuration.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();
				mDurationLabel.setText(Integer.toString(source.getValue()));
				updatePreview();
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
		builder.addSeparator(mLocalizer.msg("preview", "Preview"),
				cc.xyw(2, row, 6));
		row += 2;
		builder.add(takeChangesBtn, cc.xy(7, row));
		row += 2;
		builder.add(mPreview, cc.xyw(3, row, 5));
		row += 2;
		builder.addLabel(mLocalizer.msg("duration", "Duration"), cc.xy(3, row));
		builder.add(mDuration, cc.xy(5, row));
		builder.add(mDurationLabel, cc.xy(7, row));

		updatePreview();

		return builder.getPanel();
	}

	private void updatePreview() {
		final int pw = Math.round(Integer.parseInt(mHourWidth.getText()) / 60.0f
				* mDuration.getValue());
		final int ph = Integer.parseInt(mChannelHeight.getText());
		mPreview.setPreferredSize(new Dimension(pw + 5, ph + 10));
		mPreviewProgram.setPreferredSize(new Dimension(pw, ph));
		final TextFormatter formatter = new TextFormatter();
		formatter.setFont(TimelinePlugin.getFont());
		formatter.setInitialiseMaxLine(true);
		formatter.setFormat(mFormat.getText());
		if (!formatter.testFormat()) {
			mPreview.removeAll();
			mPreview.add(mPreviewError);
		} else {
			mPreview.removeAll();
			mPreview.add(mPreviewProgram);
			mPreviewProgram.setFormatter(formatter);
		}
		mPreview.updateUI();
	}

	public Icon getIcon() {
		return null;
	}

	public String getTitle() {
		return null;
	}

	public void saveSettings() {
		if ((mShowIconAndName.isSelected() || mShowName.isSelected() != TimelinePlugin
				.getSettings().showChannelName())
				|| (mShowIconAndName.isSelected() || mShowIcon.isSelected() != TimelinePlugin
						.getSettings().showChannelIcon())) {
			TimelinePlugin.getInstance().resetChannelWidth();
		}

		TimelinePlugin.getSettings().setProgressView(
				mProgressBar.getSelectedIndex() + 1);
		TimelinePlugin.getSettings().setFocusDelta(mFocusDelta.getValue());
		TimelinePlugin.getSettings().setShowAtStartup(mAutoStart.isSelected());
		TimelinePlugin.getSettings().setStartWithNow(mStartWithNow.isSelected());
		TimelinePlugin.getSettings().setResizeWithMouse(
				mResizeWithMouse.isSelected());
		TimelinePlugin.getSettings().setHourWidth(
				Integer.valueOf(mHourWidth.getText()));
		TimelinePlugin.getSettings().setChannelHeight(
				Integer.valueOf(mChannelHeight.getText()));
		TimelinePlugin.getSettings().setShowChannelName(
				mShowIconAndName.isSelected() || mShowName.isSelected());
		TimelinePlugin.getSettings().setShowChannelIcon(
				mShowIconAndName.isSelected() || mShowIcon.isSelected());
		TimelinePlugin.getSettings().setTitleFormat(mFormat.getText());
	}

	private String[] getProgressBarOption() {
		return new String[] { mLocalizer.msg("progress", "Progress"),
				mLocalizer.msg("bar", "Bar") };
	}
}
