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

import java.awt.*;
import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import virtualdataservice.virtual.*;

public class MonthlyRepeaterPanel extends RepeaterPanel
{
	private static final long serialVersionUID = 1L;
	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(virtualdataservice.VirtualDataService.class);

	private JRadioButton mMonthByDay;
	private JRadioButton mMonthByPosition;
	private JSpinner mMonthDay;
	private JComboBox mMonthPosition;
	private JComboBox mWeekday;

	public MonthlyRepeaterPanel()
	{
		setLayout(new BorderLayout());
		setBorder(null);

		FormLayout layout = new FormLayout("pref, 3dlu, pref:grow", "pref, 3dlu, pref");

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		CellConstraints cc = new CellConstraints();

		mMonthByDay = new JRadioButton();
		mMonthByDay.setSelected(true);
		mMonthByPosition = new JRadioButton();
		ButtonGroup endGroup = new ButtonGroup();
		endGroup.add(mMonthByDay);
		endGroup.add(mMonthByPosition);

		builder.add(mMonthByDay, cc.xy(1, 1));
		builder.add(getDayPanel(), cc.xy(3, 1));
		builder.add(mMonthByPosition, cc.xy(1, 3));
		builder.add(getPositionPanel(), cc.xy(3, 3));

		add(builder.getPanel());
	}

	private JPanel getDayPanel()
	{
		FormLayout layout = new FormLayout("pref, 3dlu, pref, 3dlu, pref:grow", "pref");

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		CellConstraints cc = new CellConstraints();

		SpinnerModel model = new SpinnerNumberModel(1, 1, 31, 1);
		mMonthDay = new JSpinner(model);

		builder.addLabel(mLocalizer.msg("MonthlyRepeaterPanel.day1", "Day"), cc.xy(1, 1));
		builder.add(mMonthDay, cc.xy(3, 1));
		builder.addLabel(mLocalizer.msg("MonthlyRepeaterPanel.day2", "of the month"), cc.xy(5, 1));

		return builder.getPanel();
	}

	private JPanel getPositionPanel()
	{
		FormLayout layout = new FormLayout("pref, 3dlu, pref, 3dlu, pref, 3dlu, pref:grow", "pref");

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		CellConstraints cc = new CellConstraints();

		String[] modelPosition = new String[] { mLocalizer.msg("MonthlyRepeaterPanel.first", "first"), mLocalizer.msg("MonthlyRepeaterPanel.second", "second"), mLocalizer.msg("MonthlyRepeaterPanel.third", "third"), mLocalizer.msg("MonthlyRepeaterPanel.fourth", "fourth"), mLocalizer.msg("MonthlyRepeaterPanel.fifth", "fifth"), };
		mMonthPosition = new JComboBox(modelPosition);

		String[] modelWeekday = new String[] { mLocalizer.msg("MonthlyRepeaterPanel.mon", "mon"), mLocalizer.msg("MonthlyRepeaterPanel.tue", "tue"), mLocalizer.msg("MonthlyRepeaterPanel.wed", "wed"), mLocalizer.msg("MonthlyRepeaterPanel.thu", "thu"), mLocalizer.msg("MonthlyRepeaterPanel.fri", "fri"), mLocalizer.msg("MonthlyRepeaterPanel.sat", "sat"), mLocalizer.msg("MonthlyRepeaterPanel.sun", "sun"), };
		mWeekday = new JComboBox(modelWeekday);

		builder.addLabel(mLocalizer.msg("MonthlyRepeaterPanel.pos1", "The"), cc.xy(1, 1));
		builder.add(mMonthPosition, cc.xy(3, 1));
		builder.add(mWeekday, cc.xy(5, 1));
		builder.addLabel(mLocalizer.msg("MonthlyRepeaterPanel.pos2", "of the month"), cc.xy(7, 1));

		return builder.getPanel();
	}

	public Repeat getRepeater()
	{
		if (mMonthByDay.isSelected())
		{
			MonthlyRepeater repeater = new MonthlyRepeater();
			repeater.setDay((Integer) mMonthDay.getValue());
			return repeater;
		}
		else
		{
			MonthlyExRepeater repeater = new MonthlyExRepeater();
			repeater.setDayOfWeek((mWeekday.getSelectedIndex() + 1) % 7 + 1);
			repeater.setWeekOfMonth(mMonthPosition.getSelectedIndex() + 1);
			return repeater;
		}
	}

	public void setRepeater(Repeat repeater)
	{
		switch (repeater.getID())
		{
			case 3:
				MonthlyRepeater monthlyRepeater = (MonthlyRepeater) repeater;
				mMonthByDay.setSelected(true);
				mMonthDay.setValue(monthlyRepeater.getDay());
				break;
			case 4:
				MonthlyExRepeater monthlyExRepeater = (MonthlyExRepeater) repeater;
				mMonthByPosition.setSelected(true);
				mMonthPosition.setSelectedIndex(monthlyExRepeater.getWeekOfMonth() - 1);
				mWeekday.setSelectedIndex((monthlyExRepeater.getDayOfWeek() + 5) % 7);
				break;
		}
	}
}
