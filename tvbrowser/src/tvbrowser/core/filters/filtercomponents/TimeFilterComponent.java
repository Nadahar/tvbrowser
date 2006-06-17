/*
 * TV-Browser Copyright (C) 04-2003 Martin Oberhauser
 * (darras@users.sourceforge.net)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * CVS information: $RCSfile$ $Source:
 * /cvsroot/tvbrowser/tvbrowser/src/tvbrowser/core/filters/filtercomponents/TimeFilterComponent.java,v $
 * $Date$ $Author$ $Revision$
 */

package tvbrowser.core.filters.filtercomponents;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import tvbrowser.core.Settings;
import tvbrowser.core.filters.FilterComponent;
import util.ui.UiUtilities;
import devplugin.Program;

public class TimeFilterComponent implements FilterComponent {
    
    private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(TimeFilterComponent.class);
    
    private JSpinner mFromTimeSp, mToTimeSp;
    
    private int mFromTime, mToTime;

    private String mName, mDescription;

    public TimeFilterComponent() {
        this("", "");
    }

    public TimeFilterComponent(String name, String description) {

        mFromTime = 960; //16 * 60
        mToTime = 1380;  //23*60

        mName = name;
        mDescription = description;
    }

    public void read(ObjectInputStream in, int version) throws IOException {

        if (version == 1) {
            mFromTime = in.readInt()*60;
            mToTime = (in.readInt() % 24)*60;
        } else {
            mFromTime = in.readInt();
            mToTime = in.readInt();
        }
    }

    public String toString() {
        return mLocalizer.msg("Time","Time");
    }

    public void ok() {
        mFromTime = getTimeFromDate((Date)mFromTimeSp.getValue());
        mToTime = getTimeFromDate((Date)mToTimeSp.getValue());
    }

    public void write(ObjectOutputStream out) throws IOException {
        out.writeInt(mFromTime);
        out.writeInt(mToTime);
    }

    public JPanel getPanel() {
        JPanel content = new JPanel(new BorderLayout());

        mFromTimeSp = new JSpinner(new SpinnerDateModel());
        mFromTimeSp.setEditor(new JSpinner.DateEditor(mFromTimeSp, Settings.getTimePattern()));
        mFromTimeSp.setValue(setTimeToDate(mFromTime));
        
        mToTimeSp = new JSpinner(new SpinnerDateModel());
        mToTimeSp.setEditor(new JSpinner.DateEditor(mToTimeSp, Settings.getTimePattern()));
        mToTimeSp.setValue(setTimeToDate(mToTime));
        
        JPanel timePn = new JPanel(new GridLayout(2, 2));
        timePn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("TimeOfDay","Uhrzeit")));
        timePn.add(new JLabel(mLocalizer.msg("from", "from")));
        timePn.add(mFromTimeSp);
        timePn.add(new JLabel(mLocalizer.msg("till", "till")));
        timePn.add(mToTimeSp);

        content.add(UiUtilities.createHelpTextArea(mLocalizer.msg("desc", "")),
                BorderLayout.NORTH);
        content.add(timePn, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(content, BorderLayout.NORTH);
        return centerPanel;
    }
    
    public Date setTimeToDate(int minutes) {
        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
        cal.set(Calendar.MINUTE, minutes % 60);
        return cal.getTime();
    }

    public int getTimeFromDate(Date time) {
        Calendar cal=Calendar.getInstance();
        cal.setTime(time);
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    }
    
    private Integer[] createIntegerArray(int from, int to, int step) {
        Integer[] result = new Integer[(to - from) / step + 1];
        int cur = from;
        for (int i = 0; i < result.length; i++) {
            result[i] = new Integer(cur);
            cur += step;
        }
        return result;
    }

    public boolean accept(Program program) {
        int start = program.getStartTime();

        // From-To spans over 2 Days
        if (mToTime  < mFromTime) {
            if (start >= mFromTime) { return true; }

            if (start < mToTime ) { return true; }

        }

        // Normal-Mode
        return (start < mToTime && start >= mFromTime);
    }


    public int getVersion() {
        return 2;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setDescription(String desc) {
        mDescription = desc;
    }

}