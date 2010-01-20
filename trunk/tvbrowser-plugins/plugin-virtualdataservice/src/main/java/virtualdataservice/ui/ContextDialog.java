/*
 * added by jb
 * 
 * 
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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyVetoException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

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

public class ContextDialog extends JDialog implements WindowClosingIf, ActionListener, ChangeListener, FocusListener
{
	private static final long serialVersionUID = 1L;
	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(virtualdataservice.VirtualDataService.class);
  private static final Logger mLog = java.util.logging.Logger.getLogger(ContextDialog.class.getName());

  private static ContextDialog mInstance;

  private JButton mOk;
	private JButton mCancel;
	private JTextField mProgramName;
  private DatePicker mDate;
  private JSpinner mTime;
  private DatePicker mFinalDate;
  private JSpinner mFinalTime;
	private JFormattedTextField mProgramLength;
	private JCheckBox mEnableRepeater;
	private RepeaterComponent mRepeater;

	private VirtualProgram mProgram = null;

	public ContextDialog(final Frame frame)
	{
	  super(frame, true);

    mInstance = this;
    
		setTitle(mLocalizer.msg("ProgramEditor.name", "Program Editor"));
		createGUI();
		UiUtilities.registerForClosing(this);
	}

  public static ContextDialog getInstance(final Frame frame) {
    if (mInstance == null) {
      mInstance = new ContextDialog(frame);
    }
    return mInstance;
  }
	private void createGUI()
	{
		setPreferredSize(new Dimension(400, 400));

		//setLayout(new BorderLayout());

		final FormLayout layout = new FormLayout(
        "5dlu, pref, 3dlu, pref, pref:grow, 5dlu",
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
    mDate.addActionListener(this);

    mFinalDate = new DatePicker(Calendar.getInstance().getTime());
    mFinalDate.setKeepTime(false);
    mFinalDate.setStripTime(true);
    mFinalDate.addActionListener(this);

    mTime = new JSpinner(new SpinnerDateModel());
    final JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(mTime,
        Settings.getTimePattern());
		mTime.setEditor(dateEditor);
		CaretPositionCorrector.createCorrector(dateEditor.getTextField(), new char[] { ':' }, -1);
    mTime.addChangeListener(this);

    mFinalTime = new JSpinner(new SpinnerDateModel());
    final JSpinner.DateEditor finalDateEditor = new JSpinner.DateEditor(
        mFinalTime, Settings.getTimePattern());
    mFinalTime.setEditor(finalDateEditor);
    CaretPositionCorrector.createCorrector(finalDateEditor.getTextField(), new char[] { ':' }, -1);
    mFinalTime.addChangeListener(this);

    mProgramLength = new JFormattedTextField(nf);
    mProgramLength.addFocusListener(this);

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
		builder.add(mProgramName, cc.xyw(4, row, 2));
		row += 2;
		builder.addLabel(mLocalizer.msg("ProgramEditor.programStart", "Start"), cc.xy(2, row));
		builder.add(mTime, cc.xy(4, row));
    builder.add(mDate, cc.xy(5, row));
    row += 2;
    builder.addLabel(mLocalizer.msg("ProgramEditor.programEnd", "End"), cc.xy(2, row));
    builder.add(mFinalTime, cc.xy(4, row));
    builder.add(mFinalDate, cc.xy(5, row));
    row += 2;
		builder.addLabel(mLocalizer.msg("ProgramEditor.programLength", "Length"), cc.xy(2, row));
		builder.add(mProgramLength, cc.xy(4, row));
		row += 2;
		builder.addSeparator(mLocalizer.msg("ProgramEditor.repeater", "Repeater"), cc.xyw(2, row, 4));
		row += 2;
		builder.add(mEnableRepeater, cc.xyw(2, row, 4));
		row += 2;
		builder.add(mRepeater, cc.xyw(2, row, 4));
		row += 2;
		builder.add(buttonBuilder.getPanel(), cc.xyw(2, row, 4));

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


	public VirtualProgram getProgram()
	{
		return mProgram;
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
    if (mFinalDate.getDate() == null)
    {
      mFinalDate.requestFocus();
      return false;
    }
		if (mProgramLength.getText().trim().length() == 0)
		{
			mProgramLength.requestFocus();
			return false;
		}
		return true;
	}

	public void setFields(final String title, final Calendar start,
      final Calendar end, final int length)
	{
    mProgramName.setText(title);
    try
    {
      mDate.setDate(start.getTime());
      mFinalDate.setDate(end.getTime());
    }
    catch (Exception ex)
    {}
    mTime.setValue(start.getTime());
    mFinalTime.setValue(end.getTime());
    mProgramLength.setText(Integer.toString(length));
	}

	private void setEnd (){
	  mLog.info("foo: entered");
	  final Calendar cal = Calendar.getInstance();
    cal.setTime(mDate.getDate());
    final Calendar time = Calendar.getInstance();
    time.setTime((Date) mTime.getValue());
    cal.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
    cal.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    final long millis = cal.getTimeInMillis()
        + (Integer.parseInt(mProgramLength.getText()) * 60000L);
	  cal.setTimeInMillis(millis);
    try {
      mFinalDate.setDate(cal.getTime());
    } catch (PropertyVetoException e) {
      mLog.warning("Error while setting date in 'setEnd': " + e);
    }
    mFinalTime.setValue(cal.getTime());
	}
	
	private void setLength (){
	  int length = 0;
	  final Calendar calStart = Calendar.getInstance();
    final Calendar timeStart = Calendar.getInstance();
    final Calendar calEnd = Calendar.getInstance();
    final Calendar timeEnd = Calendar.getInstance();

    calStart.setTime(mDate.getDate());
    calEnd.setTime(mFinalDate.getDate());

    timeStart.setTime((Date) mTime.getValue());
    timeEnd.setTime((Date) mFinalTime.getValue());

    calStart.set(Calendar.HOUR_OF_DAY, timeStart.get(Calendar.HOUR_OF_DAY));
    calEnd.set(Calendar.HOUR_OF_DAY, timeEnd.get(Calendar.HOUR_OF_DAY));

    calStart.set(Calendar.MINUTE, timeStart.get(Calendar.MINUTE));
    calEnd.set(Calendar.MINUTE, timeEnd.get(Calendar.MINUTE));

    calStart.set(Calendar.SECOND, 0);
    calEnd.set(Calendar.SECOND, 0);

    calStart.set(Calendar.MILLISECOND, 0);
    calEnd.set(Calendar.MILLISECOND, 0);
    
    length = (int) ((calEnd.getTimeInMillis() - calStart.getTimeInMillis()) / 60000);
    mProgramLength.setText(Integer.toString(length));
	}
	
  public void actionPerformed(final ActionEvent evt)
  { if (evt.getSource()== mDate || evt.getSource()== mFinalDate) {
    setLength();
  }
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
      }
    }
    else if (evt.getSource() == mCancel)
    {
      mProgram = null;
      setVisible(false);
    }
  }

  public void stateChanged(final ChangeEvent arg) {
    if (arg.getSource()== mTime || arg.getSource()== mFinalTime){
      setLength();
    }
  }

  public void focusGained(final FocusEvent e) {
 }

  public void focusLost(final FocusEvent e) {
    if (e.getSource()== mProgramLength) {
      setEnd();
    }
  }
    

}


