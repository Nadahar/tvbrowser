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
 
//  private JCheckBox updateCheck, settingsCheck;
//  private JRadioButton textOnlyRadio, picOnlyRadio, textAndPicRadio;
//  private OrderChooser mButtonList;
 
  private TimesListPanel mTimeButtonsPn;

  public ButtonsSettingsTab()  {
  }

 
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    String msg;
    
    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    JPanel main = new JPanel(new TabLayout(1));
    mSettingsPn.add(main, BorderLayout.NORTH);
    
    JPanel toolBarPanel=new JPanel();
    toolBarPanel.setLayout(new BoxLayout(toolBarPanel,BoxLayout.Y_AXIS));
    main.add(toolBarPanel);
    
    
    /* visible buttons */
 /*   JPanel toolbarbuttonsPanel = new JPanel(new BorderLayout());
    toolbarbuttonsPanel.setBorder(BorderFactory.createTitledBorder("Toolbar buttons"));
    
    ButtonItem[] availableButtons = getAvailableButtons();
    ButtonItem[] toolbarButtons = getSelectedToolbarButtons(availableButtons);
    mButtonList = new OrderChooser(toolbarButtons, availableButtons);
    toolbarbuttonsPanel.add(mButtonList,BorderLayout.WEST);
 
*/


  /* 'text and images' buttons */
/*
    JPanel labelBtnsPanel=new JPanel(new BorderLayout());
    JPanel panel4=new JPanel(new BorderLayout());
    msg = mLocalizer.msg("label", "Label");
    panel4.setBorder(BorderFactory.createTitledBorder(msg));
    panel4.setLayout(new BoxLayout(panel4,BoxLayout.Y_AXIS));
    textOnlyRadio = new JRadioButton(mLocalizer.msg("textOnly", "Text only"));
    picOnlyRadio = new JRadioButton(mLocalizer.msg("imagesOnly", "Images only"));
    textAndPicRadio = new JRadioButton(mLocalizer.msg("textAndImages", "Text and images"));
    ButtonGroup labelBtnsGroup=new ButtonGroup();
    labelBtnsGroup.add(textOnlyRadio);
    labelBtnsGroup.add(picOnlyRadio);
    labelBtnsGroup.add(textAndPicRadio);

    if (Settings.propToolbarButtonStyle.getString().equals("text")) {
      textOnlyRadio.setSelected(true);
    } else if (Settings.propToolbarButtonStyle.getString().equals("icon")) {
      picOnlyRadio.setSelected(true);
    } else {
      textAndPicRadio.setSelected(true);
    }

    panel4.add(textOnlyRadio);
    panel4.add(picOnlyRadio);
    panel4.add(textAndPicRadio);
    labelBtnsPanel.add(panel4,BorderLayout.NORTH);
*/
    mTimeButtonsPn = new TimesListPanel(Settings.propTimeButtons.getIntArray());  //createTimeButtonsPanel();
    mTimeButtonsPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("buttons.time", "Time buttons")));
    
    
 
 //   JPanel buttonsPn = new JPanel(new GridLayout(1,2));
//    buttonsPn.add(toolbarbuttonsPanel);
//    buttonsPn.add(labelBtnsPanel);
 //   toolBarPanel.add(buttonsPn);
    
    toolBarPanel.add(mTimeButtonsPn);
    
    return mSettingsPn;
  }
  
  /*
  private ButtonItem[] getSelectedToolbarButtons(ButtonItem[] availableItems) {
    
    
    String[] toolbarButtons = Settings.propToolbarButtons.getStringArray();
    if (toolbarButtons == null) {
      ButtonItem[] result = new ButtonItem[availableItems.length];
      System.arraycopy(availableItems, 0, result, 0, availableItems.length);
      return result;
    }
    ArrayList list = new ArrayList();
    for (int i=0; i<toolbarButtons.length; i++) {
      String itemName = toolbarButtons[i];
      for (int j=0; j<availableItems.length; j++) {
         if (availableItems[j].getId().equals(itemName)) {
           list.add(availableItems[j]);
         }
        
      }
    }
        
    ButtonItem[] result = new ButtonItem[list.size()];
    list.toArray(result);
    return result;
  }
*/
/*
  private ButtonItem[] getAvailableButtons() {
    PluginProxy[] pluginArr = PluginProxyManager.getInstance().getActivatedPlugins();
    
    ArrayList buttonList = new ArrayList();
    buttonList.add(new ButtonItem("#update","Update"));
    buttonList.add(new ButtonItem("#settings","Settings"));
    
    for (int i = 0; i < pluginArr.length; i++) {
      if (pluginArr[i].getButtonAction() != null) {
        buttonList.add(new ButtonItem(pluginArr[i]));
      }
    }
    
    // Create an array from the list
    ButtonItem[] buttonArr = new ButtonItem[buttonList.size()];
    buttonList.toArray(buttonArr);
    return buttonArr;
  }
*/

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
   // Settings.propShowTimeButtons.setBoolean(mTimeCheck.isSelected());
  /*  
    Object[] oItems = mButtonList.getOrder();
    String[] items = new String[oItems.length];
    for (int i=0; i<oItems.length; i++) {
      items[i] = ((ButtonItem)oItems[i]).getId();
    }
    Settings.propToolbarButtons.setStringArray(items);
    
    if (textOnlyRadio.isSelected()) {
      Settings.propToolbarButtonStyle.setString("text");
    } else if (picOnlyRadio.isSelected()) {
      Settings.propToolbarButtonStyle.setString("icon");
    } else {
      Settings.propToolbarButtonStyle.setString("text&icon");
    }
    */
    int[] x = mTimeButtonsPn.getTimes();
    Settings.propTimeButtons.setIntArray(mTimeButtonsPn.getTimes());
    for (int i=0; i<x.length; i++) {
      System.out.println(x[i]);
    }
    
 /*   Settings.propEarlyTime.setInt(mEarlyTimePn.getTime());
    Settings.propMiddayTime.setInt(mMiddayTimePn.getTime());
    Settings.propAfternoonTime.setInt(mAfternoonTimePn.getTime());
    Settings.propEveningTime.setInt(mEveningTimePn.getTime());*/
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
      setLayout(new BorderLayout());
      
      mListPn = new JPanel();
      mListPn.setLayout(new BoxLayout(mListPn, BoxLayout.Y_AXIS));
      add(mListPn, BorderLayout.CENTER);
       
      for (int i=0; i<times.length; i++) {
        final Row row = new Row(times[i]);
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
      southPn.add(newBtn, BorderLayout.WEST);
      
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
      JPanel row = new JPanel(new TabLayout(2));
      
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



