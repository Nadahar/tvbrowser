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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import util.io.IOUtilities;
import util.ui.BrowserLauncher;
import util.ui.UiUtilities;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;
import devplugin.Plugin;
import devplugin.PluginAccess;
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

  private String mStyleSheet;
  private JEditorPane mInfoEP;

  public ProgramInfoDialog(Frame parent, String styleSheet, final Program program,
   int[] infoBitArr, Icon[] infoIconArr, String[] infoMsgArr)
  {
    super(parent, true);
   
    mStyleSheet=styleSheet;

    setTitle(mLocalizer.msg("title", "Program information"));
    
    JPanel main = new JPanel(new BorderLayout());
    main.setPreferredSize(new Dimension(500, 350));
    main.setBorder(UiUtilities.DIALOG_BORDER);
    setContentPane(main);
    
    mInfoEP = new JEditorPane();
    mInfoEP.setEditorKit(new ExtendedHTMLEditorKit());
    ExtendedHTMLDocument doc = (ExtendedHTMLDocument) mInfoEP.getDocument();
    String text = createInfoText(program, doc, infoBitArr, infoIconArr, infoMsgArr);
    mInfoEP.setText(text);
    mInfoEP.setEditable(false);
    mInfoEP.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = evt.getURL();
          if (url != null) {
            BrowserLauncher.openURL(url.toString());
          }
        }
      }
    });



    mInfoEP.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent e) {
        handleMouseClicked(e, program);
      }
    });
    
    final JScrollPane scrollPane = new JScrollPane(mInfoEP);
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
  

  private void handleMouseClicked(MouseEvent evt, Program program) {
    if (SwingUtilities.isRightMouseButton(evt)) {
      if (program != null) {
        JPopupMenu menu = Plugin.getPluginManager().createPluginContextMenu(program);
        menu.show(mInfoEP, evt.getX() - 15, evt.getY() - 15);
      }
    }
    else if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 2)) {
      Plugin.getPluginManager().handleProgramDoubleClick(program);
    }
  }  
  
  private String createInfoText(Program prog, ExtendedHTMLDocument doc,
    int[] infoBitArr, Icon[] infoIconArr, String[] infoMsgArr)
  {
    // NOTE: All field types are included until type 25 (REPETITION_ON_TYPE)
    
    StringBuffer buffer = new StringBuffer();
    
    buffer.append("<html><head>" +
      "<style type=\"text/css\" media=\"screen\">" +
      "<!--" + mStyleSheet + "-->" +
      "</style>" +
      "</head>" +
      "<body>");
      
    buffer.append("<table width=\"100%\"><tr><td valign=\"top\">");
    
    newPara(buffer, "title", prog.getTitle());

    openPara(buffer, "time");
    int length = prog.getLength();
    if (length > 0) {
      String msg = mLocalizer.msg("minutes", "{0} min", new Integer(length));
      buffer.append(msg + " (");
      
      int hours = prog.getHours();
      int minutes = prog.getMinutes();
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

    buffer.append("</td><td width=\"1%\" valign=\"top\" nowrap>");

    newPara(buffer, "maininfo", prog.getDateString());
    newPara(buffer, "maininfo", prog.getTimeString());
    newPara(buffer, "maininfo", prog.getChannel().getName());

    buffer.append("</td></tr></table>");

    int info = prog.getInfo();
    if ((info != -1) && (info != 0)) {
      openPara(buffer, "info");
      // Workaround: Without the &nbsp; the component are not put in one line.
      buffer.append("&nbsp;");
      
      for (int i = 0; i < infoBitArr.length; i++) {
        if (ProgramInfo.bitSet(info, infoBitArr[i])) {
          if (infoIconArr[i] != null) {
            JLabel iconLabel = new JLabel(infoIconArr[i]);
            iconLabel.setToolTipText(infoMsgArr[i]);
            buffer.append(doc.createCompTag(iconLabel));
          } else {
            buffer.append(infoMsgArr[i]);
          } 
          buffer.append("&nbsp;");
        }
      }
      
      closePara(buffer);
    }

    PluginProxy[] plugins = PluginProxyManager.getInstance().getActivatedPlugins();
    ArrayList icons = new ArrayList();
    for (int i=0; i <plugins.length; i++) {
        Icon[] ico = plugins[i].getProgramTableIcons(prog); 
        if (ico != null && !plugins[i].getId().equals("java.programinfo.ProgramInfo")) {
            for (int t = 0; t<ico.length;t++) {
                JLabel iconLabel = new JLabel(ico[t]);
                iconLabel.setToolTipText(plugins[i].getInfo().getName());
                icons.add(iconLabel);
            }
        }
    }
    
    if (icons.size() > 0) {
        openPara(buffer, "info");
        // Workaround: Without the &nbsp; the component are not put in one line.
        buffer.append("&nbsp;");
        
        for (int i=0; i < icons.size(); i++) {
            buffer.append(doc.createCompTag((JLabel)icons.get(i)));
            buffer.append("&nbsp;");
        }
        
        closePara(buffer);
    }
    
    
    buffer.append("<br>\n");
    int offset = buffer.length();

    newPara(buffer, "info", prog, ProgramFieldType.EPISODE_TYPE);
    newPara(buffer, "info", prog, ProgramFieldType.GENRE_TYPE);
    newPara(buffer, "info", prog, ProgramFieldType.REPETITION_OF_TYPE);
    newPara(buffer, "info", prog, ProgramFieldType.REPETITION_ON_TYPE);
    newPara(buffer, "info", prog, ProgramFieldType.AGE_LIMIT_TYPE);
    newPara(buffer, "info", prog, ProgramFieldType.VPS_TYPE);
    newPara(buffer, "info", prog, ProgramFieldType.SHOWVIEW_NR_TYPE);

    PluginAccess[] pluginArr = prog.getMarkedByPlugins();
    if ((pluginArr != null) && (pluginArr.length != 0)) {
      openPara(buffer, "info");
      // Workaround: Without the &nbsp; the component are not put in one line.
      buffer.append("&nbsp;");
      for (int i = 0; i < pluginArr.length; i++) {
        Icon icon = pluginArr[i].getMarkIcon();
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setToolTipText(pluginArr[i].getInfo().getName());
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

    newPara(buffer, "small", prog, ProgramFieldType.ORIGIN_TYPE);
    newPara(buffer, "small", prog, ProgramFieldType.PRODUCTION_YEAR_TYPE);
    newPara(buffer, "small", prog, ProgramFieldType.URL_TYPE, true);
    newPara(buffer, "small", prog, ProgramFieldType.ORIGINAL_TITLE_TYPE);
    newPara(buffer, "small", prog, ProgramFieldType.ORIGINAL_EPISODE_TYPE);
    newPara(buffer, "small", prog, ProgramFieldType.MODERATION_TYPE);
    newPara(buffer, "small", prog, ProgramFieldType.DIRECTOR_TYPE);
    newPara(buffer, "small", prog, ProgramFieldType.ACTOR_LIST_TYPE);
    newPara(buffer, "small", prog, ProgramFieldType.SCRIPT_TYPE);
    newPara(buffer, "small", prog, ProgramFieldType.MUSIC_TYPE);
 
    buffer.append("</body></html>");

    return buffer.toString();
  }


  private void openPara(StringBuffer buffer, String style) {
    buffer.append("<div id=\"" + style + "\">");
  }


  private void closePara(StringBuffer buffer) {
    buffer.append("</div>\n");
  }


  private void newPara(StringBuffer buffer, String style, Program prog,
    ProgramFieldType fieldType)
  {
    newPara(buffer, style, prog, fieldType, false);
  }


  private void newPara(StringBuffer buffer, String style, Program prog,
    ProgramFieldType fieldType, boolean createLinks)
  {
    String label = fieldType.getLocalizedName();

    String text = null;
    if (fieldType.getFormat() == ProgramFieldType.TEXT_FORMAT) {
      text = prog.getTextField(fieldType);
    }
    else if (fieldType.getFormat() == ProgramFieldType.TIME_FORMAT) {
      text = prog.getTimeFieldAsString(fieldType);
    }
    else if (fieldType.getFormat() == ProgramFieldType.INT_FORMAT) {
      text = prog.getIntFieldAsString(fieldType);
    }
    
    newPara(buffer, style, label, text, createLinks);
  }


  private void newPara(StringBuffer buffer, String style, String text) {
    newPara(buffer, style, null, text, false);
  }


  private void newPara(StringBuffer buffer, String style, String text,
    boolean createLinks)
  {
    newPara(buffer, style, null, text, createLinks);
  }


  private void newPara(StringBuffer buffer, String style, String label,
    String text, boolean createLinks)
  {
    if ((text == null) || (text.length() == 0)) {
      return;
    }

    // Disarm html entities
    text = IOUtilities.replace(text.trim(), "<", "&lt;");
    text = IOUtilities.replace(text.trim(), ">", "&gt;");
    
    // Translate line breaks to html breaks
    text = IOUtilities.replace(text.trim(), "\n", "<br>");
    
    // Create links for URLs
    if (createLinks) {
      text = text.replaceAll("(http://|www.)[^\\s<]*", "<a href=\"$0\">$0</a>");
    }
    
    openPara(buffer, style);
    if (label != null) {
      buffer.append("<b>" + label + ":</b> ");
    }
    buffer.append(text);
    closePara(buffer);
  }

}