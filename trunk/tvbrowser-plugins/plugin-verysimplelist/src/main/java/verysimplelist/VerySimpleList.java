package verysimplelist;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.PluginInfo;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

public class VerySimpleList extends devplugin.Plugin
{
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(VerySimpleList.class);

  private Properties mSettings;

  /* tablerow(Program prog, String chn, char c)
  private String tablerow(Program prog, String chn, char c)
  {
    String s = "<tr>" +
               "<td id=\"dt" + c + "\">&nbsp;" + VerySimpleListUtilities.starttimestring(prog) + "&nbsp;</td>" +
               "<td id=\"dc" + c + "\">&nbsp;" + chn + "&nbsp;</td>" +
               "<td id=\"dp" + c + "\">&nbsp;" + VerySimpleListUtilities.programtitlestring(prog) + "&nbsp;</td>" +
               "<td id=\"de" + c + "\">&nbsp;" + VerySimpleListUtilities.programepisodestring(prog) + "&nbsp;</td></tr>";
    return s;
  }
  */

  /* listToString(ArrayList list)
  private String listToString(ArrayList list)
  {
    String s = "";
    if (list.size() > 0)
    {
      s = "<html><head><style type=\"text/css\">" +
          "<!--" +
          mSettings.getProperty("css") +
          "-->" +
          "</style></head>" +
          "<body>" +
          "<table border=0 cellspacing=0 cellpadding=0 width=\"100%\"><tr>" +
          "<th id=\"ht\">&nbsp;" + mLocalizer.msg("time", "Time") + "&nbsp;</th>" +
          "<th id=\"hc\">&nbsp;" + mLocalizer.msg("channel", "Channel") + "&nbsp;</th>" +
          "<th id=\"hp\">&nbsp;" + mLocalizer.msg("program", "Program") + "&nbsp;</th>" +
          "<th id=\"he\">&nbsp;" + mLocalizer.msg("episode", "Episode") + "&nbsp;</th>" +
          "</tr>";
      Iterator it = list.iterator();
      while ( it.hasNext() )
      {
        Program prog = (Program) it.next();
        s = s + tablerow(prog, prog.getChannel().getName(), '1');
      }
      s = s + "</table></body></html>";
    }
    return s;
  }
  */

  private void internalexecute() {
    Object[] dlgComponents = new Object[5];
    String theTime = (new java.sql.Time((new java.util.Date()).getTime())).toString().substring(0,5);
    devplugin.Date theDate = new devplugin.Date();
    ArrayList<Program> prgList = new ArrayList<Program>();

    // build the dialog components
    dlgComponents[0] = mLocalizer.msg("day", "Day");

    JComboBox cb = new JComboBox();
    for (int i = 0; i < 14; i++)
    {
      cb.addItem(theDate.toString());
      theDate = theDate.addDays(1);
    }
    dlgComponents[1] = cb;

    dlgComponents[2] = mLocalizer.msg("time1", "Time (hh:mm)");

    JTextField tf = new JTextField(theTime);
    dlgComponents[3] = tf;

    JCheckBox cx = new JCheckBox(mLocalizer.msg("settingsshownext", "Show next Program"));
    cx.setSelected(mSettings.getProperty("shownext", "false").equals("true"));
    dlgComponents[4] = cx;

    if (JOptionPane.showOptionDialog(
          null,
          dlgComponents,
          mLocalizer.msg("select", "Select Time"),
          JOptionPane.OK_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE,
          null, null, null) == JOptionPane.OK_OPTION)
    {
      theTime = tf.getText();
      PluginManager pm = getPluginManager();
      Channel[] ch = pm.getSubscribedChannels();

      int minutes;
      try
      {
        minutes = Integer.parseInt(theTime.substring(0,2)) * 60 + Integer.parseInt(theTime.substring(3,5));
      }
      catch (Exception e)
      {
        minutes = -1;
        JOptionPane.showMessageDialog(null,
                                      mLocalizer.msg("errormsg", "Wrong time format!\n{0}\n({1})", theTime, e.getMessage()),
                                      mLocalizer.msg("error", "Error"), JOptionPane.ERROR_MESSAGE);
      }

      if (minutes >= 0)
      {
        for (Channel element : ch) //for (i = 3; i < 4; i++)
        {
          boolean vortag = false;

          // Vortag
          theDate = new devplugin.Date().addDays(cb.getSelectedIndex() - 1);
          Iterator<Program> it = pm.getChannelDayProgram(theDate, element);
          if (it != null)
          {
            while (it.hasNext())
            {
              Program prog = it.next();
              /*
              JOptionPane.showMessageDialog(
                null,
                theDate.toString() + " xx " +
                prog.getChannel().getName() + " " +
                VerySimpleListUtilities.starttimestring(prog) +  " " +
                VerySimpleListUtilities.programtitlestring(prog));
              */

              if ((prog.getStartTime() > minutes) && ((prog.getStartTime() + prog.getLength() - 24 * 60) > minutes))
              {
                prgList.add(prog);
                vortag = true;
              }
            }
          }

          theDate = new devplugin.Date().addDays(cb.getSelectedIndex());
          it = pm.getChannelDayProgram(theDate, element);
          if (it != null)
          {
            while (it.hasNext())
            {
              Program prog = it.next();

              if (vortag)
              {
                prgList.add(prog);
                break;
              }

              if (prog.getStartTime() > minutes) {
                break;
              }

              /*
              JOptionPane.showMessageDialog(
                null,
                theDate.toString() + " yy " +
                prog.getChannel().getName() + " " +
                VerySimpleListUtilities.starttimestring(prog) +  " " +
                VerySimpleListUtilities.programtitlestring(prog));
              */

              if ((prog.getStartTime() <= minutes) && ((prog.getStartTime() + prog.getLength()) > minutes))
              {
                prgList.add(prog);

                if (cx.isSelected())
                {
                  if (it.hasNext())
                  {
                    prog = it.next();
                    prgList.add(prog);
                  }
                  else
                  {
                    it = pm.getChannelDayProgram(theDate, element);
                    if (it.hasNext())
                    {
                      prog = it.next();
                      prgList.add(prog);
                    }
                  }
                }
                break;
              }
            }
          }
        }

        VerySimpleListUtilities.channelColors[3] = new Color(
          Integer.parseInt(mSettings.getProperty("tcolor1r", "0")),
          Integer.parseInt(mSettings.getProperty("tcolor1g", "0")),
          Integer.parseInt(mSettings.getProperty("tcolor1b", "0")));

        VerySimpleListUtilities.channelColors[2] = new Color(
          Integer.parseInt(mSettings.getProperty("tcolor2r", "0")),
          Integer.parseInt(mSettings.getProperty("tcolor2g", "0")),
          Integer.parseInt(mSettings.getProperty("tcolor2b", "0")));

        VerySimpleListUtilities.channelColors[1] = new Color(
          Integer.parseInt(mSettings.getProperty("color1r", "235")),
          Integer.parseInt(mSettings.getProperty("color1g", "235")),
          Integer.parseInt(mSettings.getProperty("color1b", "255")));

        VerySimpleListUtilities.channelColors[0] = new Color(
          Integer.parseInt(mSettings.getProperty("color2r", "235")),
          Integer.parseInt(mSettings.getProperty("color2g", "255")),
          Integer.parseInt(mSettings.getProperty("color2b", "235")));

        VerySimpleListDialog dlg = new VerySimpleListDialog(getParentFrame(),
                                       mLocalizer.msg("caption", "{0} {1}", theDate.toString(), theTime),
                                       prgList);

        /*
        if (1 == 2)
        {
          dlg = new VerySimpleListDialog(getParentFrame(),
                                         mLocalizer.msg("caption", "{0} {1}", theDate.toString(), theTime),
                                         listToString(prgList));
        }
        else
        {
          dlg = new VerySimpleListDialog(getParentFrame(),
                                         mLocalizer.msg("caption", "{0} {1}", theDate.toString(), theTime),
                                         prgList);
        }
        */

        UiUtilities.centerAndShow(dlg);
        dlg.dispose();
      }
    }
  }

  protected String getMarkIconName()
  {
    return "verysimplelist/greenlist.png";
  }

  public ActionMenu getButtonAction()
  {
    AbstractAction action = new AbstractAction()
    {
      public void actionPerformed(ActionEvent evt)
      {
        internalexecute();
      }
    };
    // Der Aktion einen Namen geben. Dieser Name wird dann im Men� und in der Symbolleiste gezeigt
    action.putValue(Action.NAME, mLocalizer.msg("button", "Simple List"));

    // Der Aktion ein kleines Icon geben. Dieses Icon wird im Men� gezeigt
    // Das Icon sollte 16x16 Pixel gro� sein
    action.putValue(Action.SMALL_ICON, getMarkIcon());

    // Der Aktion ein gro�es Icon geben. Dieses Icon wird in der Symbolleiste gezeigt
    // Das Icon sollte 24x24 Pixel gro� sein
    action.putValue(BIG_ICON, createImageIcon("verysimplelist/greenlist24.png"));

    return new ActionMenu(action);
  }

  public boolean canReceivePrograms()
  {
    return false;
  }

  public void receivePrograms(Program[] programArr)
  {
    ;
  }

  public PluginInfo getInfo()
  {
    // Implement this method to provide information about your plugin.
    return new PluginInfo(VerySimpleList.class,
      mLocalizer.msg("menu", "Simple List"),
      mLocalizer.msg("info", "Simple List Info"),
      "Achim Kr�ber", "GPL3");
  }

  public static Version getVersion() {
      return new Version(2, 4);
  }

  public SettingsTab getSettingsTab()
  {
    // Returns a new SettingsTab object, which is added to the settings-window.
    return new VerySimpleListSettingsTab(mSettings);
  }

  public void loadSettings(Properties settings)
  {
    // Called by the host-application during start-up.
    if (settings == null ) {
      settings = new Properties();
    }
    mSettings = settings;
  }

  public void readData(ObjectInputStream in)
  {
    // Called by the host-application during start-up.
  }

  public Properties storeSettings()
  {
    // Called by the host-application during shut-down.
    return mSettings;
  }

  public void writeData(ObjectOutputStream out)
  {
    // Counterpart to loadData.
  }
}
