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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.swing.*;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.VerticalToolBar;
import util.ui.OrderChooser;
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
 
  private JCheckBox mTimeCheck, updateCheck, settingsCheck;
  private JRadioButton textOnlyRadio, picOnlyRadio, textAndPicRadio;
  private TimePanel mEarlyTimePn, mMiddayTimePn, mAfternoonTimePn, mEveningTimePn;
  private JLabel mEarlyLb, mAfternoonLb, mMiddayLb, mEveningLb;
  private OrderChooser mButtonList;
 


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
    JPanel toolbarbuttonsPanel = new JPanel(new BorderLayout());
    toolbarbuttonsPanel.setBorder(BorderFactory.createTitledBorder("Toolbar buttons"));
    
    ButtonItem[] availableButtons = getAvailableButtons();
    ButtonItem[] toolbarButtons = getSelectedToolbarButtons(availableButtons);
    mButtonList = new OrderChooser(toolbarButtons, availableButtons);
    toolbarbuttonsPanel.add(mButtonList,BorderLayout.WEST);
 



  /* 'text and images' buttons */

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




   /* enable time buttons */

    JPanel enableTimeButtonsPn = new JPanel(new BorderLayout());
    mTimeCheck = new JCheckBox(mLocalizer.msg("buttons.time", "Time buttons"));
    mTimeCheck.setSelected(Settings.propShowTimeButtons.getBoolean());
    enableTimeButtonsPn.add(mTimeCheck,BorderLayout.WEST);


    /* time buttons */

    JPanel timeButtonsPn=new JPanel(new GridLayout(0,4));
    timeButtonsPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("buttons.time", "Time buttons")));
    
   
    
    
    
    mEarlyTimePn     = new TimePanel(Settings.propEarlyTime.getInt());    
    mMiddayTimePn    = new TimePanel(Settings.propMiddayTime.getInt());
    mAfternoonTimePn = new TimePanel(Settings.propAfternoonTime.getInt());
    mEveningTimePn   = new TimePanel(Settings.propEveningTime.getInt());
    
    mEarlyLb=new JLabel(VerticalToolBar.mLocalizer.msg("button.early","Early")+":");
    timeButtonsPn.add(mEarlyLb);
    timeButtonsPn.add(mEarlyTimePn); 
    
    mMiddayLb=new JLabel(VerticalToolBar.mLocalizer.msg("button.midday","Midday")+":");
    timeButtonsPn.add(mMiddayLb);
    timeButtonsPn.add(mMiddayTimePn);
    
    mAfternoonLb=new JLabel(VerticalToolBar.mLocalizer.msg("button.afternoon","Afternoon")+":");
    timeButtonsPn.add(mAfternoonLb);
    timeButtonsPn.add(mAfternoonTimePn); 
    
    mEveningLb=new JLabel(VerticalToolBar.mLocalizer.msg("button.evening","Evening")+":");
    timeButtonsPn.add(mEveningLb);
    timeButtonsPn.add(mEveningTimePn);

 
    toolBarPanel.add(toolbarbuttonsPanel);
    toolBarPanel.add(labelBtnsPanel);
    toolBarPanel.add(enableTimeButtonsPn);    
    toolBarPanel.add(timeButtonsPn);


    mTimeCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableTimeButtons(mTimeCheck.isSelected());
      }
    });
    
    enableTimeButtons(mTimeCheck.isSelected());
    
    return mSettingsPn;
  }

  private void enableTimeButtons(boolean val) {
    boolean b=mTimeCheck.isSelected();
    mEarlyTimePn.setEnabled(b);
    mAfternoonTimePn.setEnabled(b);
    mMiddayTimePn.setEnabled(b);
    mEveningTimePn.setEnabled(b);
    mEarlyLb.setEnabled(b);
    mAfternoonLb.setEnabled(b);
    mMiddayLb.setEnabled(b);
    mEveningLb.setEnabled(b);
  }
  
  
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


  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Settings.propShowTimeButtons.setBoolean(mTimeCheck.isSelected());
    
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
    
    Settings.propEarlyTime.setInt(mEarlyTimePn.getTime());
    Settings.propMiddayTime.setInt(mMiddayTimePn.getTime());
    Settings.propAfternoonTime.setInt(mAfternoonTimePn.getTime());
    Settings.propEveningTime.setInt(mEveningTimePn.getTime());
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
   
    add(mTimeSp,BorderLayout.WEST);
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


  class ButtonItem {
    
    private String mText, mId;
    
    public ButtonItem(String id, String text) {
      mText = text;
      mId = id;
    }
        
    public ButtonItem(PluginProxy p) {
      this(p.getId(), (String) p.getButtonAction().getValue(Action.NAME));
    }
    
    public String getId() {
      return mId;
    }
    
    public String toString() {
      return mText;
    }
  }
  
}



