/*
* TV-Browser
* Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */


package reminderplugin;

import java.util.Properties;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import devplugin.*;


public class ReminderSettingsTab extends devplugin.SettingsTab {

  private JCheckBox reminderwindowCheckBox;
  private FileCheckBox soundFileCheckBox;
  private FileCheckBox execFileCheckBox;
  private Properties settings;

  public String getName() {
    return "reminder";
  }

  public void ok() {

    settings.setProperty("soundfile",soundFileCheckBox.getTextField().getText());
    settings.setProperty("execfile",execFileCheckBox.getTextField().getText());

    settings.setProperty("usemsgbox",new Boolean(reminderwindowCheckBox.isSelected()).toString());
    settings.setProperty("usesound",new Boolean(soundFileCheckBox.isSelected()).toString());
    settings.setProperty("useexec",new Boolean(execFileCheckBox.isSelected()).toString());
  }


  public ReminderSettingsTab(Properties settings) {
    super();

    setLayout(new BorderLayout());
    this.settings=settings;

    JPanel reminderPanel=new JPanel();

    reminderPanel.setBorder(BorderFactory.createTitledBorder("Remind me by:"));

    reminderPanel.setLayout(new BoxLayout(reminderPanel,BoxLayout.Y_AXIS));
    JPanel panel1=new JPanel(new BorderLayout());
    reminderwindowCheckBox=new JCheckBox("Erinnerungsfenster");
    panel1.add(reminderwindowCheckBox,BorderLayout.WEST);
    reminderPanel.add(panel1);


    String soundFName=settings.getProperty("soundfile","/");
    String execFName=settings.getProperty("execfile","/");

    File soundFile=new File(soundFName);
    File execFile=new File(execFName);

    soundFileCheckBox=new FileCheckBox("playing sound:",soundFile,200);
    execFileCheckBox=new FileCheckBox("executing program:",execFile,200);

    JFileChooser soundChooser=new JFileChooser("sound/");
    JFileChooser execChooser=new JFileChooser("/");

    soundChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
      public boolean accept(File f) {
        String name=f.getName().toLowerCase();
        return f.isFile() && (name.endsWith(".wav") || name.endsWith(".aif") || name.endsWith("rmf") || name.endsWith(".au") || name.endsWith(".mid"));
      }
      public String getDescription() {
        return "Sound files";
      }
    }
    );

    reminderwindowCheckBox.setSelected(settings.getProperty("usemsgbox","true").equals("true"));
    soundFileCheckBox.setSelected(settings.getProperty("usesound","false").equals("true"));
    execFileCheckBox.setSelected(settings.getProperty("useexec","false").equals("true"));


    soundFileCheckBox.setFileChooser(soundChooser);
    execFileCheckBox.setFileChooser(execChooser);

    reminderPanel.add(soundFileCheckBox);
    reminderPanel.add(execFileCheckBox);


    add(reminderPanel,BorderLayout.NORTH);
    updateUI();
  }




}