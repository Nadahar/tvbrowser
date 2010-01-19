package verysimplelist;

import devplugin.Program;
import util.io.IOUtilities;
import devplugin.ProgramFieldType;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Font;
import javax.swing.SwingUtilities;
import devplugin.Plugin;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Insets;
import devplugin.PluginAccess;
import javax.swing.JOptionPane;
import devplugin.ActionMenu;

public class VerySimpleListUtilities {
  private static String lastChannel = "";
  private static int channelColorIdx = 0;
  public static Color[] channelColors = {new Color(235,235,255), new Color(235,255,235), new Color(0,0,0), new Color(0,0,0)};

  public static String removelinefeeds(String s)
  {
    char c;
    String t = "";
    for (int j = 0; j < s.length(); j++)
    {
      c = s.charAt(j);
      if ( (c == (char) 13) || (c == (char) 10) ) t = t + ' '; else t = t + c;
    }
    return t;
  }

  public static String filltime(String s)
  {
    String t = s;
    while (t.length() < 5) t = '0' + t;
    return t;
  }

  public static String starttimestring(Program prg)
  {
    return filltime(prg.getTimeString()) + " - " +
           filltime(IOUtilities.timeToString((prg.getStartTime() + prg.getLength()) % 1440));
  }

  public static String programtitlestring(Program prg)
  {
    return removelinefeeds(prg.getTitle());
  }

  public static String programepisodestring(Program prg)
  {
    String t = prg.getTextField(ProgramFieldType.EPISODE_TYPE);
    if (t == null) t = " ";
    return removelinefeeds(t);
  }

  public static void makeHeader(String s, JPanel p, GridBagLayout g, GridBagConstraints c)
  {
    channelColorIdx = 0;
    JLabel lbl = new JLabel(s);
    //lbl.setBorder(BorderFactory.createLineBorder(Color.RED));
    lbl.setOpaque(true);
    lbl.setBackground(channelColors[channelColorIdx]);
    //lbl.setForeground(new Color(0,0,128));
    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
    lbl.setForeground(channelColors[channelColorIdx + 2]);

    c.ipadx = 10;//3;
    c.ipady = 0;
    g.setConstraints(lbl, c);
    p.add(lbl);
  }

  public static void make4Headers(JPanel pnl, GridBagLayout g, String s1, String s2, String s3, String s4)
  {
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = 4;
    c.weightx = 1.0;

    makeHeader(s1, pnl, g, c);
    makeHeader(s2, pnl, g, c);
    c.weightx = 4.0;
    makeHeader(s3, pnl, g, c);
    c.gridwidth = GridBagConstraints.REMAINDER;
    makeHeader(s4, pnl, g, c);
  }

  public static void make4Labels(JPanel pnl, Program prg, GridBagLayout g)
  {
    GridBagConstraints c = new GridBagConstraints();
    // c.insets = new Insets(0, 6, 0, 6);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = 4;
    c.weightx = 1.0;

    String chn = prg.getChannel().getName();
    if (chn != lastChannel)
    {
      lastChannel = chn;
      channelColorIdx = (channelColorIdx == 0) ? 1 : 0;
    }

    makeLabel(pnl, VerySimpleListUtilities.starttimestring(prg), g, c, prg);
    makeLabel(pnl, chn, g, c, prg);
    c.weightx = 4.0;
    makeLabel(pnl, VerySimpleListUtilities.programtitlestring(prg), g, c, prg);
    c.gridwidth = GridBagConstraints.REMAINDER;
    makeLabel(pnl, VerySimpleListUtilities.programepisodestring(prg), g, c, prg);
  }

  public static void makeLabel(JPanel pnl, String name, GridBagLayout gridbag, GridBagConstraints c, final Program prg)
  {
    final JLabel lbl = new JLabel(name);
    //lbl.setBorder(BorderFactory.createLineBorder(Color.RED));
    lbl.setOpaque(true);
    lbl.setBackground(channelColors[channelColorIdx]);
    lbl.setForeground(channelColors[channelColorIdx + 2]);

    lbl.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt)
      {
        if (SwingUtilities.isRightMouseButton(evt)) {
          JPopupMenu menu = devplugin.Plugin.getPluginManager().createPluginContextMenu(prg, null);
          menu.show(lbl, evt.getX() - 15, evt.getY() - 15);
        } else
        {
          if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 2))
          {
            devplugin.Plugin.getPluginManager().handleProgramDoubleClick(prg, null);
          }
        }
      }
    });

    c.ipadx = 10;//3;
    c.ipady = 0;
    gridbag.setConstraints(lbl, c);
    pnl.add(lbl);
  }
}
