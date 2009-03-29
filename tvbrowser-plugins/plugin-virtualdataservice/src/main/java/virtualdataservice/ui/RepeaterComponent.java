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

import virtualdataservice.virtual.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.*;
import javax.swing.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import com.michaelbaranov.microba.calendar.DatePicker;

public class RepeaterComponent extends JPanel
{
	private static final long serialVersionUID = 1L;
	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(virtualdataservice.VirtualDataService.class);

	private JComboBox mSchedule;
	private JRadioButton mNoEnd;
	private JRadioButton mEndWith;
	private DatePicker mEndDate;
	private JPanel mRepeater;

	public RepeaterComponent()
	{
		setLayout(new BorderLayout());
		setBorder(null);

		FormLayout layout = new FormLayout("0dlu, pref, 3dlu, pref, 3dlu, pref:grow, 0dlu", "5dlu, pref, 2dlu, pref, 2dlu, pref, 5dlu, pref:grow, 3dlu");

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		CellConstraints cc = new CellConstraints();

		mSchedule = new JComboBox(getSchedules());
		mSchedule.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				mRepeater.removeAll();
				switch (mSchedule.getSelectedIndex())
				{
					case 0:
						mRepeater.add(new DailyRepeaterPanel());
						break;
					case 1:
						mRepeater.add(new WeeklyRepeaterPanel());
						break;
					case 2:
						mRepeater.add(new MonthlyRepeaterPanel());
						break;
					case 3:
						mRepeater.add(new YearlyRepeaterPanel());
						break;
				}
				mRepeater.updateUI();
			}
		});

		mNoEnd = new JRadioButton(mLocalizer.msg("RepeaterComponent.noEnd", "No End Date"));
		mNoEnd.setSelected(true);
		mEndWith = new JRadioButton("");
		ButtonGroup endGroup = new ButtonGroup();
		endGroup.add(mNoEnd);
		endGroup.add(mEndWith);

		mEndDate = new DatePicker();

		mRepeater = new JPanel();
		mRepeater.setLayout(new BorderLayout());
		mRepeater.setBorder(null);
		mRepeater.add(new DailyRepeaterPanel());

		int row = 2;
		builder.addLabel(mLocalizer.msg("RepeaterComponent.schedule", "Schedule"), cc.xy(2, row));
		builder.add(mSchedule, cc.xyw(4, row, 3));
		row += 2;
		builder.addLabel(mLocalizer.msg("RepeaterComponent.endOn", "End on"), cc.xy(2, row));
		builder.add(mNoEnd, cc.xyw(4, row, 3));
		row += 2;
		builder.add(mEndWith, cc.xy(4, row));
		builder.add(mEndDate, cc.xy(6, row));
		row += 2;
		builder.add(mRepeater, cc.xyw(4, row, 3));

		add(builder.getPanel());
	}

	private Vector<String> getSchedules()
	{
		Vector<String> schedules = new Vector<String>();

		schedules.add(mLocalizer.msg("RepeaterComponent.daily", "Daily"));
		schedules.add(mLocalizer.msg("RepeaterComponent.weekly", "Weekly"));
		schedules.add(mLocalizer.msg("RepeaterComponent.monthly", "Monthly"));
		schedules.add(mLocalizer.msg("RepeaterComponent.yearly", "Yearly"));

		return schedules;
	}

	private RepeaterPanel getRepeaterPanel()
	{
		return (RepeaterPanel) mRepeater.getComponent(0);
	}

	public Repeat getRepeater()
	{
		Repeat repeater = getRepeaterPanel().getRepeater();
		if (mEndWith.isSelected() && mEndDate.getDate() != null)
		{
			Calendar endDate = Calendar.getInstance();
			endDate.setTime(mEndDate.getDate());
			repeater.setEndDate(endDate);
		}
		return repeater;
	}

	public void setRepeater(Repeat repeater)
	{
		if (repeater != null)
		{
			switch (repeater.getID())
			{
				case 1:
					mSchedule.setSelectedIndex(0);
					break;
				case 2:
					mSchedule.setSelectedIndex(1);
					break;
				case 3:
				case 4:
					mSchedule.setSelectedIndex(2);
					break;
				case 5:
					mSchedule.setSelectedIndex(3);
					break;
			}
			getRepeaterPanel().setRepeater(repeater);
			try
			{
				if (repeater.getEndDate() != null)
				{
					mEndWith.setSelected(true);
					mEndDate.setDate(repeater.getEndDate().getTime());
				}
				else
				{
					mNoEnd.setSelected(true);
				}
			}
			catch (PropertyVetoException e)
			{
				e.printStackTrace();
			}
		}
	}
}
