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

package programinfo;


import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.logging.Level;

import util.io.IOUtilities;
import util.ui.*;

import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ProgramInfoDialog extends JDialog implements SwingConstants {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ProgramInfoDialog.class);

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(ProgramInfoDialog.class.getName());


  public ProgramInfoDialog(Frame parent, Program program) {
    super(parent, true);

    setTitle(mLocalizer.msg("title", "Program information"));
    
    JPanel main = new JPanel(new BorderLayout());
    main.setPreferredSize(new Dimension(500, 350));
    main.setBorder(UiUtilities.DIALOG_BORDER);
    setContentPane(main);
    
    JEditorPane infoEP = new JEditorPane();
    infoEP.setEditorKit(new ExtendedHTMLEditorKit());
    ExtendedHTMLDocument doc = (ExtendedHTMLDocument) infoEP.getDocument();
    String text = createInfoText(program, doc);
    // System.out.println(text);
    infoEP.setText(text);
    infoEP.setEditable(false);
    infoEP.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          String url = evt.getURL().toString();
          try {
            BrowserLauncher.openURL(url);
          }
          catch (IOException exc) {
            mLog.log(Level.WARNING, "Could not open URL: " + url, exc);
          }
        }
      }
    });
    
    final JScrollPane scrollPane = new JScrollPane(infoEP);
    main.add(scrollPane, BorderLayout.CENTER);
    
    // buttons
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    main.add(buttonPn, BorderLayout.SOUTH);

    JButton closeBtn = new JButton(mLocalizer.msg("close", "Close"));
    closeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    });
    buttonPn.add(closeBtn);
    getRootPane().setDefaultButton(closeBtn);
    
    // Scroll to the beginning
    Runnable runnable = new Runnable() {
      public void run() {
        scrollPane.getVerticalScrollBar().setValue(0);
      }
    };
    SwingUtilities.invokeLater(runnable);
  }
  
  
  private String createInfoText(Program prog, ExtendedHTMLDocument doc) {
    String msg;
    String value;
    
    StringBuffer buffer = new StringBuffer();
    
    buffer.append("<html><head>" +
      "<style type=\"text/css\" media=\"screen\">" +
      "<!--" +
      // "body" +
      // "{ background-image:url(back.jpg); padding:10px; }" +
      "#title" +
      "{ font-size:15px; font-family:Dialog; " +
      "text-align:center; font-weight:bold; }" +
      "#time" +
      "{ font-size:9px; font-family:Dialog; text-align:center; }" +
      "#info" +
      "{ font-size:9px; font-family:Dialog; }" +
      "#text" +
      "{ font-size:11px; font-family:Dialog; }" +
      "#small" +
      "{ font-size:9px; font-family:Dialog; }" +
      "-->" +
      "</style>" +
      "</head>" +
      "<body>");
    
    openPara(buffer, "title");
    buffer.append(prog.getChannel().getName() + ": " + prog.getTitle());
    closePara(buffer);
    
    openPara(buffer, "time");
    buffer.append(prog.getDateString() + " - ");
    int minutes = prog.getMinutes();
    int hours = prog.getHours();
    buffer.append(hours + ":" + (minutes < 10 ? "0" : "") + minutes);
    int length = prog.getLength();
    if (length > 0) {
      msg = mLocalizer.msg("minutes", "{0} min", new Integer(length));
      buffer.append(" - " + msg + " (");
      
      int endTime = (hours * 60 + minutes + length) % (24 * 60);
      minutes = endTime % 60;
      hours = endTime / 60;
      String until = hours + ":" + (minutes < 10 ? "0" : "") + minutes;
      buffer.append(mLocalizer.msg("until", "until {0}", until));
      
      int netLength = prog.getIntField(ProgramFieldType.NET_PLAYING_TIME_TYPE);
      if (netLength != -1) {
        msg = mLocalizer.msg("netMinuted", "{0} min net", new Integer(netLength));
        buffer.append(" - " + msg);
      }
      buffer.append(")");
    }

    closePara(buffer);

    buffer.append("<br>\n");
    int offset = buffer.length();

    String info = programInfoToString(prog.getInfo());
    newPara(buffer, "info", info);

    msg = mLocalizer.msg("episode", "Episode");
    value = prog.getTextField(ProgramFieldType.EPISODE_TYPE);
    newPara(buffer, "info", msg, value);

    msg = mLocalizer.msg("genre", "Genre");
    value = prog.getTextField(ProgramFieldType.GENRE_TYPE);
    newPara(buffer, "info", msg, value);

    msg = mLocalizer.msg("repetitionOf", "Repetition of");
    value = prog.getTextField(ProgramFieldType.REPETITION_OF_TYPE);
    newPara(buffer, "info", msg, value);
    
    int ageLimit = prog.getIntField(ProgramFieldType.AGE_LIMIT_TYPE);
    if (ageLimit != -1) {
      msg = mLocalizer.msg("ageLimit", "age limit");
      newPara(buffer, "info", msg, Integer.toString(ageLimit));
    }

    msg = mLocalizer.msg("vps", "VPS");
    value = prog.getTimeFieldAsString(ProgramFieldType.VPS_TYPE);
    newPara(buffer, "info", msg, value);

    msg = mLocalizer.msg("showview", "Showview");
    value = prog.getTextField(ProgramFieldType.SHOWVIEW_NR_TYPE);
    newPara(buffer, "info", msg, value);

    Plugin[] pluginArr = prog.getMarkedByPlugins();
    if ((pluginArr != null) && (pluginArr.length != 0)) {
      openPara(buffer, "info");
      // Workaround: Without the &nbsp; the component are not put in one line.
      buffer.append("&nbsp;");
      for (int i = 0; i < pluginArr.length; i++) {
        Icon icon = pluginArr[i].getMarkIcon();
        JLabel iconLabel = new JLabel(icon);
        buffer.append(doc.createCompTag(iconLabel));
      }
      closePara(buffer);
    }

    if (offset != buffer.length()) {
      buffer.append("<br>\n");
      offset = buffer.length();
    }

    byte[] image = prog.getBinaryField(ProgramFieldType.IMAGE_TYPE);
    if (image != null) {
      Icon icon = new ImageIcon(image);
      JLabel iconLabel = new JLabel(icon);
      buffer.append(doc.createCompTag(iconLabel));
    }

    // Add short info, if short info isn't only the short version of the
    // description
    String description = prog.getDescription();
    String shortInfo = prog.getShortInfo();
    if (shortInfo != null) {
      String shortInfoSubString = shortInfo;
      if (shortInfo.endsWith("...")) {
        shortInfoSubString = shortInfo.substring(0, shortInfo.length() - 3);
      }
      
      if ((description != null) && description.startsWith(shortInfoSubString)) {
        // The short info is only a short version of the description
        // -> Don't add it
      } else {
        newPara(buffer, "text", shortInfo, true);
      }
    }
    
    newPara(buffer, "text", description, true);

    if (offset != buffer.length()) {
      buffer.append("<br>\n");
      offset = buffer.length();
    }

    msg = mLocalizer.msg("origin", "Origin");
    value = prog.getTextField(ProgramFieldType.ORIGIN_TYPE);
    newPara(buffer, "small", msg, value);

    int productionYear = prog.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE);
    if (productionYear != -1) {
      msg = mLocalizer.msg("productionYear", "Production year");
      newPara(buffer, "small", msg, Integer.toString(productionYear));
    }

    msg = mLocalizer.msg("website", "Website");
    value = prog.getTextField(ProgramFieldType.URL_TYPE);
    newPara(buffer, "small", msg, value, true);

    msg = mLocalizer.msg("originalTitle", "Original title");
    value = prog.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE);
    newPara(buffer, "small", msg, value);

    msg = mLocalizer.msg("originalEpisode", "Original episode");
    value = prog.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE);
    newPara(buffer, "small", msg, value);

    msg = mLocalizer.msg("moderation", "Moderation");
    value = prog.getTextField(ProgramFieldType.MODERATION_TYPE);
    newPara(buffer, "small", msg, value);

    msg = mLocalizer.msg("director", "Director");
    value = prog.getTextField(ProgramFieldType.DIRECTOR_TYPE);
    newPara(buffer, "small", msg, value);

    msg = mLocalizer.msg("actors", "Actors");
    value = prog.getTextField(ProgramFieldType.ACTOR_LIST_TYPE);
    newPara(buffer, "small", msg, value);

    msg = mLocalizer.msg("script", "Script");
    value = prog.getTextField(ProgramFieldType.SCRIPT_TYPE);
    newPara(buffer, "small", msg, value);

    msg = mLocalizer.msg("music", "Music");
    value = prog.getTextField(ProgramFieldType.MUSIC_TYPE);
    newPara(buffer, "small", msg, value);
 
    buffer.append("</body></html>");

    return buffer.toString();
  }


  private void openPara(StringBuffer buffer, String style) {
    buffer.append("<div id=\"" + style + "\">");
  }


  private void closePara(StringBuffer buffer) {
    buffer.append("</div>\n");
  }


  private void newPara(StringBuffer buffer, String style, String text) {
    newPara(buffer, style, null, text);
  }


  private void newPara(StringBuffer buffer, String style, String text,
    boolean createLinks)
  {
    newPara(buffer, style, null, text, createLinks);
  }


  private void newPara(StringBuffer buffer, String style, String label,
    String text)
  {
    newPara(buffer, style, label, text, false);
  }


  private void newPara(StringBuffer buffer, String style, String label,
    String text, boolean createLinks)
  {
    if ((text == null) || (text.length() == 0)) {
      return;
    }

    text = IOUtilities.replace(text.trim(), "\n", "<br>");
    if (createLinks) {
      System.out.println("creating links");
      text = text.replaceAll("(http://|www.)[^\\s<]*", "<a href=\"$0\">$0</a>");
    }
    
    openPara(buffer, style);
    if (label != null) {
      buffer.append("<b>" + label + ":</b> ");
    }
    buffer.append(text);
    closePara(buffer);
  }
  
  
  private String programInfoToString(int info) {
    if ((info == -1) || (info == 0)) {
      return null;
    }
    
    StringBuffer buf = new StringBuffer();

    if (bitSet(info, Program.INFO_VISION_BLACK_AND_WHITE)) {
      buf.append(mLocalizer.msg("blackAndWhite", "Black and white") + "  ");
    }
    if (bitSet(info, Program.INFO_VISION_4_TO_3)) {
      buf.append(mLocalizer.msg("4to3", "4:3") + "  ");
    }
    if (bitSet(info, Program.INFO_VISION_16_TO_9)) {
      buf.append(mLocalizer.msg("16to9", "16:9") + "  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_MONO)) {
      buf.append(mLocalizer.msg("mono", "Mono") + "  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_STEREO)) {
      buf.append(mLocalizer.msg("stereo", "Stereo") + "  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_DOLBY_SURROUND)) {
      buf.append(mLocalizer.msg("dolbySurround", "Dolby surround") + "  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_DOLBY_DIGITAL_5_1)) {
      buf.append(mLocalizer.msg("dolbyDigital5.1", "Dolby digital 5.1") + "  ");
    }
    if (bitSet(info, Program.INFO_AUDIO_TWO_CHANNEL_TONE)) {
      buf.append(mLocalizer.msg("twoChannelTone", "Two channel tone") + "  ");
    }
    if (bitSet(info, Program.INFO_SUBTITLE)) {
      buf.append(mLocalizer.msg("subtitle", "Subtitle") + "  ");
    }
    if (bitSet(info, Program.INFO_LIVE)) {
      buf.append(mLocalizer.msg("live", "Live") + "  ");
    }
    
    return buf.toString();
  }
  
  
  
  /**
   * Returns whether a bit (or combination of bits) is set in the specified
   * number.
   */
  private boolean bitSet(int num, int pattern) {
    return (num & pattern) == pattern;
  }

}