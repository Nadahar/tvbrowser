 /* RelativeTimeFilterComponent
 * Copyright (C) 2014 René Mach (rene@tvbrowser.org)
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
package relativetimefiltercomponent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import util.ui.EnhancedPanelBuilder;

import com.jgoodies.forms.factories.CC;

import devplugin.Date;
import devplugin.PluginsFilterComponent;
import devplugin.Program;

/**
 * A class that provides a filter component that accepts programs around the current time.
 * 
 * @author René Mach
 */
public class RelativeTimeFilterComp extends PluginsFilterComponent {
  private int mAcceptablePreTime = 60;
  private int mAcceptablePostTime = 120;
  
  private JSpinner mPreTimeSetup;
  private JSpinner mPostTimeSetup;
  
  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  public boolean accept(Program program) {
    Calendar start = Calendar.getInstance();
    Date date = program.getDate();
    start.set(date.getYear(), date.getMonth()-1, date.getDayOfMonth(), program.getHours(), program.getMinutes());
   
    long startMinute = start.getTimeInMillis() / 60000;
    long endMinute = startMinute + program.getLength();
    long nowMinute = System.currentTimeMillis() / 60000;
   
    return ((nowMinute - mAcceptablePreTime <= startMinute) && (nowMinute + mAcceptablePostTime >= endMinute));
  }

  @Override
  public void read(ObjectInputStream in, int version) throws IOException,
      ClassNotFoundException {
    mAcceptablePreTime = in.readInt();
    mAcceptablePostTime = in.readInt();
  }

  @Override
  public void write(ObjectOutputStream out) throws IOException {
    out.writeInt(mAcceptablePreTime);
    out.writeInt(mAcceptablePostTime);
  }

  @Override
  public String getUserPresentableClassName() {
    return RelativeTimeFilterComponent.LOCALIZER.msg("name", "Provides a filter component that accepts programs around the current time.");
  }

  @Override
  public JPanel getSettingsPanel() {
    EnhancedPanelBuilder pb = new EnhancedPanelBuilder("5dlu,default,3dlu,default:grow");
    
    mPreTimeSetup = new JSpinner(new SpinnerNumberModel(mAcceptablePreTime, 5, 720, 5));
    mPostTimeSetup = new JSpinner(new SpinnerNumberModel(mAcceptablePostTime, 5, 720, 5));
    
    pb.addRow();
    pb.addLabel(RelativeTimeFilterComponent.LOCALIZER.msg("settings.firstLabel","Accepts programs that start at least"), CC.xyw(2, pb.getRow(), 3));
    
    pb.addRow();
    pb.add(mPreTimeSetup, CC.xy(2, pb.getRow()));
    pb.addLabel(RelativeTimeFilterComponent.LOCALIZER.msg("settings.secondLabel","minutes before the current time and end at most"), CC.xy(4, pb.getRow()));
    
    pb.addRow();
    pb.add(mPostTimeSetup, CC.xy(2, pb.getRow()));
    pb.addLabel(RelativeTimeFilterComponent.LOCALIZER.msg("settings.thirdLabel","minutes after the current time."), CC.xy(4, pb.getRow()));
    
    return pb.getPanel();
  }
  
  @Override
  public void saveSettings() {
    if(mPreTimeSetup != null && mPostTimeSetup != null) {
      mAcceptablePreTime = (Integer)mPreTimeSetup.getValue();
      mAcceptablePostTime = (Integer)mPostTimeSetup.getValue();
      
      mPreTimeSetup = null;
      mPostTimeSetup = null;
    }
  }
}
