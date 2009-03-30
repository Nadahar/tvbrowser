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

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import virtualdataservice.virtual.Repeat;
import virtualdataservice.virtual.YearlyRepeater;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class YearlyRepeaterPanel extends RepeaterPanel
{
	private static final long serialVersionUID = 1L;
	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(virtualdataservice.VirtualDataService.class);

	private JSpinner mYears;

	public YearlyRepeaterPanel()
	{
		setLayout(new BorderLayout());
		setBorder(null);

		final FormLayout layout = new FormLayout(
        "pref, 3dlu, pref, 3dlu, pref:grow", "pref");

		final PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);

		final CellConstraints cc = new CellConstraints();

		final SpinnerModel model = new SpinnerNumberModel(1, 1, 50, 1);
		mYears = new JSpinner(model);

		builder.addLabel(mLocalizer.msg("YearlyRepeaterPanel.every", "Every"), cc.xy(1, 1));
		builder.add(mYears, cc.xy(3, 1));
		builder.addLabel(mLocalizer.msg("YearlyRepeaterPanel.year", "year"), cc.xy(5, 1));

		add(builder.getPanel());
	}

	public Repeat getRepeater()
	{
	  final YearlyRepeater repeater = new YearlyRepeater();
		repeater.setYears((Integer) mYears.getValue());
		return repeater;
	}

	public void setRepeater(final Repeat repeater)
	{
		if (repeater.getID() == 5)
		{
		  final YearlyRepeater yearlyRepeater = (YearlyRepeater) repeater;
			mYears.setValue(yearlyRepeater.getYears());
		}
	}
}
