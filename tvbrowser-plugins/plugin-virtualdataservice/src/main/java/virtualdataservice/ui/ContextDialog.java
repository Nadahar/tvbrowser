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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import com.michaelbaranov.microba.calendar.DatePicker;

import tvbrowser.core.Settings;
import util.ui.*;
import virtualdataservice.virtual.VirtualProgram;

public class ContextDialog extends JDialog implements WindowClosingIf, ActionListener, ChangeListener, FocusListener
{
	private static final long serialVersionUID = 1L;
	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(virtualdataservice.VirtualDataService.class);
  private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ContextDialog.class.getName());

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

	public ContextDialog(Frame frame)
	{
	  super(frame, true);

    mInstance = this;
    
		setTitle(mLocalizer.msg("ProgramEditor.name", "Program Editor"));
		createGUI();
		UiUtilities.registerForClosing(this);
	}

  public static ContextDialog getInstance(Frame frame) {
    if (mInstance == null) {
      mInstance = new ContextDialog(frame);
    }
    return mInstance;
  }
	private void createGUI()
	{
		setPreferredSize(new Dimension(400, 400));

		//setLayout(new BorderLayout());

		FormLayout layout = new FormLayout("5dlu, pref, 3dlu, pref, pref:grow, 5dlu", "5dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 10dlu, pref, 2dlu, pref, 2dlu, top:pref:grow, 2dlu, pref, 3dlu");

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		CellConstraints cc = new CellConstraints();

		NumberFormat nf = NumberFormat.getIntegerInstance();
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
		JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(mTime, Settings.getTimePattern());
		mTime.setEditor(dateEditor);
		CaretPositionCorrector.createCorrector(dateEditor.getTextField(), new char[] { ':' }, -1);
    mTime.addChangeListener(this);

    mFinalTime = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor finalDateEditor = new JSpinner.DateEditor(mFinalTime, Settings.getTimePattern());
    mFinalTime.setEditor(finalDateEditor);
    CaretPositionCorrector.createCorrector(finalDateEditor.getTextField(), new char[] { ':' }, -1);
    mFinalTime.addChangeListener(this);

    mProgramLength = new JFormattedTextField(nf);
    mProgramLength.addFocusListener(this);

		mEnableRepeater = new JCheckBox(mLocalizer.msg("ProgramEditor.repeat", "Enable repeat"));
		mEnableRepeater.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent evt)
			{
				mRepeater.setEnabled(mEnableRepeater.isSelected());
			}
		});

		mRepeater = new RepeaterComponent();
		mRepeater.setEnabled(false);

		ButtonBarBuilder buttonBuilder = new ButtonBarBuilder();
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

	private Boolean isValidProgram()
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

	public void setFields(String title, Calendar start, Calendar end, int length)
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
    Calendar cal = Calendar.getInstance();
    cal.setTime(mDate.getDate());
    Calendar time = Calendar.getInstance();
    time.setTime((Date) mTime.getValue());
    cal.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
    cal.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
	  long millis = cal.getTimeInMillis() + (Integer.parseInt(mProgramLength.getText())*60000);
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
    Calendar calStart = Calendar.getInstance();
    Calendar timeStart = Calendar.getInstance();
    Calendar calEnd = Calendar.getInstance();
    Calendar timeEnd = Calendar.getInstance();

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
	
  public void actionPerformed(ActionEvent evt)
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
        Calendar cal = Calendar.getInstance();
        cal.setTime(mDate.getDate());
        Calendar time = Calendar.getInstance();
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

  public void stateChanged(ChangeEvent arg) {
    if (arg.getSource()== mTime || arg.getSource()== mFinalTime){
      setLength();
    }
    
  }

  public void focusGained(FocusEvent e) {
 }

  public void focusLost(FocusEvent e) {
    if (e.getSource()== mProgramLength) {
      setEnd();
    }

    
  }
    

}


