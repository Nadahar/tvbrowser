package verysimplelist;

import devplugin.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import javax.swing.*;
import util.ui.*;

public class VerySimpleListSettingsTab implements SettingsTab
{
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(VerySimpleListSettingsTab.class);

  private Properties mSettings;
  private JPanel mSettingsPn;
  private JCheckBox mNextCheckBox;
  private JTextPane mStyleSheetText;
  private ArrayList lines1;
  private ArrayList lines2;
  private Color line1Color;
  private Color line2Color;
  private Color text1Color;
  private Color text2Color;

  private MouseAdapter lines1click = new MouseAdapter()
  {
    public void mouseClicked(MouseEvent evt)
    {
      if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 2))
      {
        Color c = JColorChooser.showDialog(null, mLocalizer.msg("settingscolor1", "Color for line 1"), line1Color);
        if (c != null)
        {
          line1Color = c;
          paintLine(lines1, line1Color, text1Color);
        }
      } else
      if (SwingUtilities.isRightMouseButton(evt) && (evt.getClickCount() == 2))
      {
        Color c = JColorChooser.showDialog(null, mLocalizer.msg("settingscolor1a", "Textcolor for line 1"), text1Color);
        if (c != null)
        {
          text1Color = c;
          paintLine(lines1, line1Color, text1Color);
        }
      }
    }
  };

  private MouseAdapter lines2click = new MouseAdapter()
  {
    public void mouseClicked(MouseEvent evt)
    {
      if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 2))
      {
        Color c = JColorChooser.showDialog(null, mLocalizer.msg("settingscolor2", "Color for header and line 2"), line2Color);
        if (c != null)
        {
          line2Color = c;
          paintLine(lines2, line2Color, text2Color);
        }
      } else
      if (SwingUtilities.isRightMouseButton(evt) && (evt.getClickCount() == 2))
      {
        Color c = JColorChooser.showDialog(null, mLocalizer.msg("settingscolor2a", "Textcolor for header and line 2"), text2Color);
        if (c != null)
        {
          text2Color = c;
          paintLine(lines2, line2Color, text2Color);
        }
      }
    }
  };

  private void paintLine(ArrayList list, Color col, Color txt)
  {
    Iterator it = list.iterator();
    while ( it.hasNext() )
    {
      paintLabel((JLabel) it.next(), col, txt);
    }
  }

  private void paintLabel(JLabel label, Color col, Color txt)
  {
    label.setOpaque(true);
    label.setBackground(col);
    label.setForeground(txt);
  }

  public VerySimpleListSettingsTab(Properties settings)
  {
    this.mSettings = settings;
  }

  public JPanel createSettingsPanel()
  {
    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("settingscaption", "Simple List Options")));

    JPanel main = new JPanel(new TabLayout(1));
    mSettingsPn.add(main, BorderLayout.NORTH);

    line1Color = new Color(
      Integer.parseInt(mSettings.getProperty("color1r", "235")),
      Integer.parseInt(mSettings.getProperty("color1g", "235")),
      Integer.parseInt(mSettings.getProperty("color1b", "255")));

    line2Color = new Color(
      Integer.parseInt(mSettings.getProperty("color2r", "235")),
      Integer.parseInt(mSettings.getProperty("color2g", "255")),
      Integer.parseInt(mSettings.getProperty("color2b", "235")));

    text1Color = new Color(
      Integer.parseInt(mSettings.getProperty("tcolor1r", "0")),
      Integer.parseInt(mSettings.getProperty("tcolor1g", "0")),
      Integer.parseInt(mSettings.getProperty("tcolor1b", "0")));

    text2Color = new Color(
      Integer.parseInt(mSettings.getProperty("tcolor2r", "0")),
      Integer.parseInt(mSettings.getProperty("tcolor2g", "0")),
      Integer.parseInt(mSettings.getProperty("tcolor2b", "0")));

    mNextCheckBox = new JCheckBox(mLocalizer.msg("settingsshownext", "Show next Program"));
    mNextCheckBox.setSelected(mSettings.getProperty("shownext", "false").equals("true"));
    main.add(mNextCheckBox);

    JPanel liste = new JPanel();
    // liste.setLayout(new BoxLayout(liste, BoxLayout.Y_AXIS));
    liste.setLayout(new GridLayout(9,1));
    liste.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("settingslist", "Colors for list")));

    lines1 = new ArrayList();
    lines2 = new ArrayList();

    JLabel lbl = new JLabel(mLocalizer.msg("settingshead", "Header"));
    lbl.addMouseListener(lines2click);
    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
    lbl.setForeground(text2Color);
    liste.add(lbl);
    lines2.add(lbl);

    for (int i = 0; i < 4; i++)
    {
      lbl = new JLabel(mLocalizer.msg("settingsline1", "Line 1"));
      lbl.addMouseListener(lines1click);
      lbl.setForeground(text1Color);
      liste.add(lbl);
      lines1.add(lbl);

      lbl = new JLabel(mLocalizer.msg("settingsline2", "Line 2"));
      lbl.addMouseListener(lines2click);
      lbl.setForeground(text2Color);
      liste.add(lbl);
      lines2.add(lbl);
    }

    paintLine(lines1, line1Color, text1Color);
    paintLine(lines2, line2Color, text2Color);

    main.add(liste);

    JButton defaultBtn = new JButton(mLocalizer.msg("settingsdef", "Default"));
    defaultBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {

        line1Color = new Color(235,235,255);
        line2Color = new Color(235,255,235);
        text1Color = new Color(0,0,0);
        text2Color = new Color(0,0,0);
        paintLine(lines1, line1Color, text1Color);
        paintLine(lines2, line2Color, text2Color);

        /*
        mStyleSheetText.setText(
          "body { background-color:#EBFFEB; }\n\n" +
          "th {\n  font-size:9px; font-weight:bold;\n  font-family:Dialog; text-align:left;\n}\n\n" +
          "td { font-size:9px; font-family:Dialog; }\n\n" +

          "#ht { background-color:#EBFFEB; }\n" +
          "#hc { background-color:#D7FFD7; }\n" +
          "#hp { background-color:#EBFFEB; }\n" +
          "#he { background-color:#D7FFD7; }\n\n" +

          "#dt1 { background-color:#EBEBFF; }\n" +
          "#dc1 { background-color:#D7D7FF; }\n" +
          "#dp1 { background-color:#EBEBFF; }\n" +
          "#de1 { background-color:#D7D7FF; }\n\n" +

          "#dt2 { background-color:#EBFFEB; }\n" +
          "#dc2 { background-color:#D7FFD7; }\n" +
          "#dp2 { background-color:#EBFFEB; }\n" +
          "#de2 { background-color:#D7FFD7; }\n");
        */
      }
    });

    JPanel btnPanel = new JPanel(new BorderLayout());
    btnPanel.add(defaultBtn, BorderLayout.WEST);
    mSettingsPn.add(btnPanel, BorderLayout.SOUTH);

    return mSettingsPn;
  }

  public void saveSettings()
  {
    // mSettings.setProperty("css", new String(mStyleSheetText.getText()));
    mSettings.setProperty("shownext", new Boolean(mNextCheckBox.isSelected()).toString());

    mSettings.setProperty("color1r", new Integer(line1Color.getRed()).toString());
    mSettings.setProperty("color1g", new Integer(line1Color.getGreen()).toString());
    mSettings.setProperty("color1b", new Integer(line1Color.getBlue()).toString());

    mSettings.setProperty("color2r", new Integer(line2Color.getRed()).toString());
    mSettings.setProperty("color2g", new Integer(line2Color.getGreen()).toString());
    mSettings.setProperty("color2b", new Integer(line2Color.getBlue()).toString());

    mSettings.setProperty("tcolor1r", new Integer(text1Color.getRed()).toString());
    mSettings.setProperty("tcolor1g", new Integer(text1Color.getGreen()).toString());
    mSettings.setProperty("tcolor1b", new Integer(text1Color.getBlue()).toString());

    mSettings.setProperty("tcolor2r", new Integer(text2Color.getRed()).toString());
    mSettings.setProperty("tcolor2g", new Integer(text2Color.getGreen()).toString());
    mSettings.setProperty("tcolor2b", new Integer(text2Color.getBlue()).toString());
 }

  public Icon getIcon()
  {
    return ImageUtilities.createImageIconFromJar("verysimplelist/greenlist.png", getClass());
  }

  public String getTitle()
  {
    return mLocalizer.msg("settingsname", "Simple List");
  }
}
