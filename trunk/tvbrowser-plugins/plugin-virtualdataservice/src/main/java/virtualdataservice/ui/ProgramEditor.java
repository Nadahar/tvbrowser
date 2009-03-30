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
package virtualdataservice.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.Settings;
import util.ui.CaretPositionCorrector;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import virtualdataservice.virtual.VirtualProgram;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.michaelbaranov.microba.calendar.DatePicker;

public class ProgramEditor extends JDialog implements WindowClosingIf, ActionListener
{
	private static final long serialVersionUID = 1L;
	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(virtualdataservice.VirtualDataService.class);

	private JButton mOk;
	private JButton mCancel;
	private JTextField mProgramName;
	private DatePicker mDate;
	private JSpinner mTime;
	private JFormattedTextField mProgramLength;
	private JCheckBox mEnableRepeater;
	private RepeaterComponent mRepeater;

	private VirtualProgram mProgram = null;

	public ProgramEditor(final Frame frame)
	{
		super(frame, true);

		setTitle(mLocalizer.msg("ProgramEditor.name", "Program Editor"));
		createGUI();
		UiUtilities.registerForClosing(this);
	}

	private void createGUI()
	{
		setPreferredSize(new Dimension(400, 400));

		//setLayout(new BorderLayout());

		final FormLayout layout = new FormLayout(
        "5dlu, pref, 3dlu, pref:grow, 5dlu",
        "5dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 10dlu, pref, 2dlu, pref, 2dlu, top:pref:grow, 2dlu, pref, 3dlu");

		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		final CellConstraints cc = new CellConstraints();

		final NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setGroupingUsed(false);

		mProgramName = new JTextField();

		mDate = new DatePicker(Calendar.getInstance().getTime());
		mDate.setKeepTime(false);
		mDate.setStripTime(true);

		mTime = new JSpinner(new SpinnerDateModel());
		final JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(mTime,
        Settings.getTimePattern());
		mTime.setEditor(dateEditor);
		CaretPositionCorrector.createCorrector(dateEditor.getTextField(), new char[] { ':' }, -1);

		mProgramLength = new JFormattedTextField(nf);

		mEnableRepeater = new JCheckBox(mLocalizer.msg("ProgramEditor.repeat", "Enable repeat"));
		mEnableRepeater.addChangeListener(new ChangeListener()
		{
			public void stateChanged(final ChangeEvent evt)
			{
				mRepeater.setEnabled(mEnableRepeater.isSelected());
			}
		});

		mRepeater = new RepeaterComponent();
		mRepeater.setEnabled(false);

		final ButtonBarBuilder buttonBuilder = new ButtonBarBuilder();
		mOk = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
		mOk.addActionListener(this);
		mCancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
		mCancel.addActionListener(this);
		buttonBuilder.addGlue();
		buttonBuilder.addGriddedButtons(new JButton[] { mOk, mCancel });

		int row = 2;
		builder.addLabel(mLocalizer.msg("ProgramEditor.programName", "Program name"), cc.xy(2, row));
		builder.add(mProgramName, cc.xy(4, row));
		row += 2;
		builder.addLabel(mLocalizer.msg("ProgramEditor.programDate", "Date"), cc.xy(2, row));
		builder.add(mDate, cc.xy(4, row));
		row += 2;
		builder.addLabel(mLocalizer.msg("ProgramEditor.programStart", "Start"), cc.xy(2, row));
		builder.add(mTime, cc.xy(4, row));
		row += 2;
		builder.addLabel(mLocalizer.msg("ProgramEditor.programLength", "Length"), cc.xy(2, row));
		builder.add(mProgramLength, cc.xy(4, row));
		row += 2;
		builder.addSeparator(mLocalizer.msg("ProgramEditor.repeater", "Repeater"), cc.xyw(2, row, 3));
		row += 2;
		builder.add(mEnableRepeater, cc.xyw(2, row, 3));
		row += 2;
		builder.add(mRepeater, cc.xyw(2, row, 3));
		row += 2;
		builder.add(buttonBuilder.getPanel(), cc.xyw(2, row, 3));

		setContentPane(builder.getPanel());
		getRootPane().setDefaultButton(mOk);
		pack();
	}

	public void close()
	{
		mProgram = null;
		this.setVisible(false);
		//this.dispose();
	}

	public void setProgram(final VirtualProgram program)
	{
		mProgram = program;
		mEnableRepeater.setSelected(mProgram.getRepeat() != null);
		if (mEnableRepeater.isSelected())
		{
			mRepeater.setRepeater(mProgram.getRepeat());
		}
		loadFields();
	}

	public VirtualProgram getProgram()
	{
		return mProgram;
	}

	public void actionPerformed(final ActionEvent evt)
	{
		if (evt.getSource() == mOk)
		{
			if (isValidProgram())
			{
				if (mProgram == null)
				{
					mProgram = new VirtualProgram();
				}
				mProgram.setTitle(mProgramName.getText().trim());
				final Calendar cal = Calendar.getInstance();
				cal.setTime(mDate.getDate());
				final Calendar time = Calendar.getInstance();
				time.setTime((Date) mTime.getValue());
				cal.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
				cal.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				mProgram.setStart(cal);
				mProgram.setLength(Integer.parseInt(mProgramLength.getText()));
				if (mEnableRepeater.isSelected())
				{
					mProgram.setRepeat(mRepeater.getRepeater());
				}
				else
				{
					mProgram.setRepeat(null);
				}
				setVisible(false);

				System.out.println(mProgram);
			}
		}
		else if (evt.getSource() == mCancel)
		{
			mProgram = null;
			setVisible(false);
		}
	}

	private boolean isValidProgram()
	{
		if (mProgramName.getText().trim().length() == 0)
		{
			mProgramName.requestFocus();
			return false;
		}
		if (mDate.getDate() == null)
		{
			mDate.requestFocus();
			return false;
		}
		if (mProgramLength.getText().trim().length() == 0)
		{
			mProgramLength.requestFocus();
			return false;
		}
		return true;
	}

	private void loadFields()
	{
		if (mProgram != null)
		{
			mProgramName.setText(mProgram.getTitle());
			try
			{
				mDate.setDate(mProgram.getStart().getTime());
			}
			catch (Exception ex)
			{}
			mTime.setValue(mProgram.getStart().getTime());
			mProgramLength.setText(Integer.toString(mProgram.getLength()));
		}
	}
}
