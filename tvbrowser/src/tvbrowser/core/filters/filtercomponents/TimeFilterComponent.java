/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.core.filters.filtercomponents;

import java.io.*;

import javax.swing.*;

import tvbrowser.core.filters.FilterComponent;

import java.awt.*;

import devplugin.Program;

public class TimeFilterComponent implements FilterComponent {

  private JComboBox mFromTimeCb, mToTimeCb;
  private int mFromTime, mToTime;
  private String mName, mDescription;

  public TimeFilterComponent() {
    this("","");
  }

  public TimeFilterComponent(String name, String description) {
   
    mFromTime = 16;
    mToTime = 23;
    
    mName = name;
    mDescription = description;
  }

  public void read(ObjectInputStream in, int version) throws IOException {   
      
      mFromTime = in.readInt();
      mToTime = in.readInt();   
    
  }

	public String toString() {
		return "Zeit";
	}

	
	public void ok() {
		mFromTime = ((Integer)mFromTimeCb.getSelectedItem()).intValue();
    mToTime = ((Integer)mToTimeCb.getSelectedItem()).intValue();
    
	}


	public void write(ObjectOutputStream out) throws IOException {
   
    out.writeInt(mFromTime);
    out.writeInt(mToTime); 

  }

	
	public JPanel getPanel() {
    JPanel content = new JPanel(new BorderLayout());
    
    JPanel timePn = new JPanel(new GridLayout(2,2));
    timePn.setBorder(BorderFactory.createTitledBorder("Uhrzeit"));
    timePn.add(new JLabel("Von"));
    timePn.add(mFromTimeCb=new JComboBox(createIntegerArray(0,23,1)));
    timePn.add(new JLabel("Bis:"));
    timePn.add(mToTimeCb=new JComboBox(createIntegerArray(12,36,1)));
    
    mFromTimeCb.setSelectedItem(new Integer(mFromTime));
    mToTimeCb.setSelectedItem(new Integer(mToTime));
    
    mFromTimeCb.setRenderer(new TimeListCellRenderer());
    mToTimeCb.setRenderer(new TimeListCellRenderer());
    
    
    content.add(new JLabel("<html>Dieser Filter akzeptiert ausschlieszlich Sendungen, die<br>im angegebenen Zeitraum beginnen</html>"),BorderLayout.NORTH);
    content.add(timePn, BorderLayout.CENTER);
    
		return content;
	}

 
  private Integer[] createIntegerArray(int from, int to, int step) {
    Integer[] result = new Integer[(to-from)/step+1];
    int cur=from;
    for (int i=0;i<result.length;i++) {
      result[i]=new Integer(cur);
      cur+=step;
    }
    return result;
  }	
  
  
	public boolean accept(Program program) {    
    int h = program.getHours();
    return (h<mToTime%24 && h>=mFromTime);     
	}
  
  
  class TimeListCellRenderer extends DefaultListCellRenderer {

    public TimeListCellRenderer() {
      
    }
  
  
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    
      JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                  index, isSelected, cellHasFocus);
      
      if (value instanceof Integer) {
        int val = ((Integer)value).intValue();
        if (val<24) {
          label.setText(val+":00");            
        }
        else {
          label.setText((val-24)+":00 (naechster Tag)");
        }
      }
                
      return label;
    }
  
  }
	
	public int getVersion() {
		return 1;
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