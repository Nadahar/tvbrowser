/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import util.ui.TabLayout;
import devplugin.SettingsTab;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ButtonsSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ButtonsSettingsTab.class);
  
  private JPanel mSettingsPn;

 
  private TimesListPanel mTimeButtonsPn;

  public ButtonsSettingsTab()  {
  }

 
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {

    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    JPanel main = new JPanel(new TabLayout(1));
    mSettingsPn.add(main, BorderLayout.NORTH);
    
    JPanel toolBarPanel=new JPanel();
    toolBarPanel.setLayout(new BoxLayout(toolBarPanel,BoxLayout.Y_AXIS));
    main.add(toolBarPanel);
    


    mTimeButtonsPn = new TimesListPanel(Settings.propTimeButtons.getIntArray());
    mTimeButtonsPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("buttons.time", "Time buttons")));
    

    
    toolBarPanel.add(mTimeButtonsPn);
    
    return mSettingsPn;
  }
  


  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {

    int[] x = mTimeButtonsPn.getTimes();
    Settings.propTimeButtons.setIntArray(mTimeButtonsPn.getTimes());
    for (int i=0; i<x.length; i++) {
      System.out.println(x[i]);
    }
  }

  
  
  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    
    return null;
    
  }
  
  
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("buttons", "Buttons");
  }



class TimePanel extends JPanel {
  
  
  private JSpinner mTimeSp;
  public TimePanel(int minutes) {
    setLayout(new BorderLayout());
    
    String timePattern = mLocalizer.msg("timePattern", "hh:mm a");
   
    mTimeSp = new JSpinner(new SpinnerDateModel());
    mTimeSp.setEditor(new JSpinner.DateEditor(mTimeSp, timePattern));
   
    add(mTimeSp,BorderLayout.EAST);
    setTime(minutes);
  }
  
  public void setTime(int minutes) {
    Calendar cal=Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
    cal.set(Calendar.MINUTE, minutes % 60);
    mTimeSp.setValue(cal.getTime());   
  }
  
  public int getTime() {
    
    Date time= (Date) mTimeSp.getValue();
    Calendar cal=Calendar.getInstance();
    cal.setTime(time);
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }
  
  public void setEnabled(boolean val) {
    mTimeSp.setEnabled(val);
  }
}

  class TimesListPanel extends JPanel {
    private ArrayList mRows;
    private JPanel mListPn;
      
    public TimesListPanel(int[] times) {
        
      mRows = new ArrayList();        
      setLayout(new BorderLayout(3,8));
      
      mListPn = new JPanel();
      mListPn.setLayout(new BoxLayout(mListPn, BoxLayout.Y_AXIS));
      add(mListPn, BorderLayout.CENTER);
       
      for (int i=0; i<times.length; i++) {
        final Row row = new Row(times[i]);
        row.setBorder(BorderFactory.createEmptyBorder(2,0,2,0));
        mRows.add(row);
        row.getRemoveButton().addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent arg) {
            mRows.remove(row);
            updateContent();
          }
        });
      }
      JButton newBtn = new JButton(new ImageIcon("imgs/New16.gif"));
      newBtn.setToolTipText(mLocalizer.msg("new","New"));
      JPanel southPn = new JPanel(new BorderLayout());
      southPn.add(newBtn, BorderLayout.EAST);
      
      add(southPn, BorderLayout.SOUTH);
      newBtn.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent event) {
            
          final Row row = new Row(0);
          mRows.add(row);
          
          row.getRemoveButton().addActionListener(new ActionListener(){
              public void actionPerformed(ActionEvent arg) {
                mRows.remove(row);
                updateContent();
              }
            });

          updateContent();            
        }        
      });
      
      updateContent();
    }
    
    private void updateContent() {
      mListPn.removeAll();
      for (int i=0; i<mRows.size(); i++) {
        Row row = (Row)mRows.get(i);
        mListPn.add(row);
      }
      mListPn.updateUI();
    }
    
    
    public int[] getTimes() {
      int[] result = new int[mRows.size()];
      for (int i=0; i<result.length; i++) {
        result[i] = ((Row)mRows.get(i)).getTime();
      }
      return result;
    }
  }

  class Row extends JPanel {
    
    private JButton mRemoveBtn;
    private TimePanel mTimePn;  
    
    public Row(int time) {
      setLayout(new BorderLayout());
      mRemoveBtn = new JButton(new ImageIcon("imgs/Delete16.gif"));
      mRemoveBtn.setToolTipText(mLocalizer.msg("delete","Delete"));
      JPanel row = new JPanel(new TabLayout(2,14,0));
      
      row.add(mTimePn = new TimePanel(time));
      row.add(mRemoveBtn);
      
      add(row, BorderLayout.WEST);
      
    }
    
    public JButton getRemoveButton() {
      return mRemoveBtn;
    }
    
    public int getTime() {
      return mTimePn.getTime();
    }
    
  }

  class ButtonItem {
    
    private String mText, mId;
    
    public ButtonItem(String id, String text) {
      mText = text;
      mId = id;
    }
        
    public ButtonItem(PluginProxy p) {
      this(p.getId(), (String) p.getButtonAction().getAction().getValue(Action.NAME));
    }
    
    public String getId() {
      return mId;
    }
    
    public String toString() {
      return mText;
    }
  }
  
}



