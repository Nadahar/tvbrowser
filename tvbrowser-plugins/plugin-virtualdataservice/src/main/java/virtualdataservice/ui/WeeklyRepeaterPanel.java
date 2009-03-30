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

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import virtualdataservice.virtual.Repeat;
import virtualdataservice.virtual.WeeklyRepeater;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class WeeklyRepeaterPanel extends RepeaterPanel
{
	private static final long serialVersionUID = 1L;
	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(virtualdataservice.VirtualDataService.class);

	private JSpinner mWeeks;
	private JCheckBox mMon;
	private JCheckBox mTue;
	private JCheckBox mWed;
	private JCheckBox mThu;
	private JCheckBox mFri;
	private JCheckBox mSat;
	private JCheckBox mSun;

	public WeeklyRepeaterPanel()
	{
		setLayout(new BorderLayout());
		setBorder(null);

		final FormLayout layout = new FormLayout(
        "pref, 3dlu, pref, 3dlu, pref:grow", "pref, 3dlu, pref");

		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		final CellConstraints cc = new CellConstraints();

		final SpinnerModel model = new SpinnerNumberModel(1, 1, 500, 1);
		mWeeks = new JSpinner(model);

		builder.addLabel(mLocalizer.msg("WeeklyRepeaterPanel.every", "Every"), cc.xy(1, 1));
		builder.add(mWeeks, cc.xy(3, 1));
		builder.addLabel(mLocalizer.msg("WeeklyRepeaterPanel.week", "week"), cc.xy(5, 1));
		builder.add(getDays(), cc.xyw(1, 3, 5));

		add(builder.getPanel());
	}

	private JPanel getDays()
	{
	  final FormLayout layout = new FormLayout(
        "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref", "pref, 1dlu, pref");
		layout.setColumnGroups(new int[][] { { 1, 3, 5, 7 } });

		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		final CellConstraints cc = new CellConstraints();

		mMon = new JCheckBox(mLocalizer.msg("WeeklyRepeaterPanel.mon", "Mon"));
		mTue = new JCheckBox(mLocalizer.msg("WeeklyRepeaterPanel.tue", "Tue"));
		mWed = new JCheckBox(mLocalizer.msg("WeeklyRepeaterPanel.wed", "Wed"));
		mThu = new JCheckBox(mLocalizer.msg("WeeklyRepeaterPanel.thu", "Thu"));
		mFri = new JCheckBox(mLocalizer.msg("WeeklyRepeaterPanel.fri", "Fri"));
		mSat = new JCheckBox(mLocalizer.msg("WeeklyRepeaterPanel.sat", "Sat"));
		mSun = new JCheckBox(mLocalizer.msg("WeeklyRepeaterPanel.sun", "Sun"));

		builder.add(mMon, cc.xy(1, 1));
		builder.add(mTue, cc.xy(1, 3));
		builder.add(mWed, cc.xy(3, 1));
		builder.add(mThu, cc.xy(3, 3));
		builder.add(mFri, cc.xy(5, 1));
		builder.add(mSat, cc.xy(5, 3));
		builder.add(mSun, cc.xy(7, 1));

		return builder.getPanel();
	}

	public Repeat getRepeater()
	{
	  final WeeklyRepeater repeater = new WeeklyRepeater();
		
		repeater.setWeeks((Integer) mWeeks.getValue());
		repeater.setMonday(mMon.isSelected());
		repeater.setTuesday(mTue.isSelected());
		repeater.setWednesday(mWed.isSelected());
		repeater.setThursday(mThu.isSelected());
		repeater.setFriday(mFri.isSelected());
		repeater.setSaturday(mSat.isSelected());
		repeater.setSunday(mSun.isSelected());
		
		return repeater;
	}

	public void setRepeater(final Repeat repeater)
	{
		if (repeater.getID() == 2)
		{
		  final WeeklyRepeater weeklyRepeater = (WeeklyRepeater) repeater;
			
			mWeeks.setValue(weeklyRepeater.getWeeks());
			mMon.setSelected(weeklyRepeater.getMonday());
			mTue.setSelected(weeklyRepeater.getTuesday());
			mWed.setSelected(weeklyRepeater.getWednesday());
			mThu.setSelected(weeklyRepeater.getThursday());
			mFri.setSelected(weeklyRepeater.getFriday());
			mSat.setSelected(weeklyRepeater.getSaturday());
			mSun.setSelected(weeklyRepeater.getSunday());
		}
		
	}

}
