package tvbrowser.extras.programinfo;

import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.HTMLTextHelper;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramInfoHelper;

/**
 * Creates the String for the ProgramInfoDialog
 */
public class ProgramTextCreator {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ProgramTextCreator.class);

  /**
   * 
   * @param prog  The Program to show
   * @param doc The HTMLDocument.
   * @return The html String.
   */
  public static String createInfoText(Program prog, ExtendedHTMLDocument doc) {
    // NOTE: All field types are included until type 25 (REPETITION_ON_TYPE)
    StringBuffer buffer = new StringBuffer();

    buffer.append("<html>");
    buffer.append("<table width=\"100%\" style=\"font-family:");

    buffer.append(ProgramInfo.getInstance().getProperty("bodyfont","Verdana"));

    buffer.append(";\"><tr>");
    buffer.append("<td width=\"60\">");
    buffer.append("<p \"align=center\">");
    buffer.append(doc.createCompTag(new JLabel(prog.getChannel().getIcon())));
    buffer.append("</p></td><td>");
    buffer.append("<div style=\"color:#ff0000; font-size:");

    buffer.append(ProgramInfo.getInstance().getProperty("small","11"));

    buffer.append(";\"><b>");
    buffer.append(prog.getDateString());
    buffer.append(" | ");
    buffer.append(prog.getTimeString());
    buffer.append(" | ");
    buffer.append(prog.getChannel());

    buffer.append("</b></div><div style=\"color:#003366; font-size:");

    buffer.append(ProgramInfo.getInstance().getProperty("title","18"));
    
    buffer.append("; line-height:2.5em; font-family:");
    buffer.append(ProgramInfo.getInstance().getProperty("titlefont","Verdana"));
    buffer.append("\"><b>");
    buffer.append(prog.getTitle());
    buffer.append("</b></div>");

    String episode = prog.getTextField(ProgramFieldType.EPISODE_TYPE);

    if (episode != null && episode.trim().length() > 0) {
      buffer.append("<div style=\"color:#808080; font-size:");

      buffer.append(ProgramInfo.getInstance().getProperty("small","11"));

      buffer.append("\">");
      buffer.append(episode);
      buffer.append("</div>");
    }

    buffer.append("</td></tr>");

    Marker[] pluginArr = prog.getMarkerArr();
    if ((pluginArr != null) && (pluginArr.length != 0)) {
      buffer.append("<tr><td></td><td>");
      openPara(buffer, "info");

      // Workaround: Without the &nbsp; the component are not put in one line.
      buffer.append("&nbsp;");
      for (int i = 0; i < pluginArr.length; i++) {
        Icon icon = pluginArr[i].getMarkIcon();
        JLabel iconLabel = new JLabel(icon);
        PluginAccess plugin = Plugin.getPluginManager()
            .getActivatedPluginForId(pluginArr[i].getId());

        if (plugin != null)
          iconLabel.setToolTipText(plugin.getInfo().getName());

        buffer.append(doc.createCompTag(iconLabel));
      }
      closePara(buffer);
      buffer.append("</td></tr>");
    }

    PluginAccess[] plugins = Plugin.getPluginManager().getActivatedPlugins();
    ArrayList icons = new ArrayList();
    for (int i = 0; i < plugins.length; i++) {
      Icon[] ico = plugins[i].getProgramTableIcons(prog);
      
      if (ico != null
          && !plugins[i].getId().equals("programinfo.ProgramInfo")) {
        for (int t = 0; t < ico.length; t++) {
          JLabel iconLabel = new JLabel(ico[t]);
          iconLabel.setToolTipText(plugins[i].getInfo().getName());
          icons.add(iconLabel);
        }
      }
    }

    if (icons.size() > 0) {
      buffer.append("<tr><td></td><td>");
      openPara(buffer, "info"); 
      // Workaround: Without the &nbsp; the component are not put in one line.
      buffer.append("&nbsp;");

      for (int i = 0; i < icons.size(); i++) {
        buffer.append(doc.createCompTag((JLabel) icons.get(i)));
        buffer.append("&nbsp;");
      }

      closePara(buffer);
      buffer.append("</td></tr>");
    }

    
    byte[] image = prog.getBinaryField(ProgramFieldType.IMAGE_TYPE);
    if (image != null) {
      buffer.append("<tr><td></td><td>");
      try {
        Icon icon = new ImageIcon(image);
        JLabel iconLabel = new JLabel(icon);
        buffer.append(doc.createCompTag(iconLabel));
        buffer.append("</td></tr>");
      } catch(Exception e) {
        // Picture was wrong;
        buffer.delete(buffer.length()-18,buffer.length());
      }
    }
    
    addSeperator(buffer);
    
    addEntry(buffer, prog, ProgramFieldType.GENRE_TYPE);
    addEntry(buffer, prog, ProgramFieldType.MUSIC_TYPE);

    if (prog.getTextField(ProgramFieldType.DESCRIPTION_TYPE) != null
        && prog.getTextField(ProgramFieldType.DESCRIPTION_TYPE).trim().length() > 0)
      addEntry(buffer, prog, ProgramFieldType.DESCRIPTION_TYPE, true);
    else
      addEntry(buffer, prog, ProgramFieldType.SHORT_DESCRIPTION_TYPE, true);

    addEntry(buffer, prog, ProgramFieldType.ORIGIN_TYPE);
    addEntry(buffer, prog, ProgramFieldType.DIRECTOR_TYPE);
    addEntry(buffer, prog, ProgramFieldType.SCRIPT_TYPE);
    addEntry(buffer, prog, ProgramFieldType.ACTOR_LIST_TYPE);
    addEntry(buffer, prog, ProgramFieldType.MODERATION_TYPE);
    addEntry(buffer, prog, ProgramFieldType.URL_TYPE, true);
    addEntry(buffer, prog, ProgramFieldType.ORIGINAL_TITLE_TYPE);
    addEntry(buffer, prog, ProgramFieldType.ORIGINAL_EPISODE_TYPE);
    addEntry(buffer, prog, ProgramFieldType.REPETITION_OF_TYPE);
    addEntry(buffer, prog, ProgramFieldType.REPETITION_ON_TYPE);
    addEntry(buffer, prog, ProgramFieldType.AGE_LIMIT_TYPE);

    int info = prog.getInfo();
    if ((info != -1) && (info != 0)) {
      buffer.append("<tr><td valign=\"top\" style=\"color:gray; font-size:");

      buffer.append(ProgramInfo.getInstance().getProperty("small","11"));

      buffer.append("\"><b>");
      buffer.append(mLocalizer.msg("attributes", "Program attributes"));
      buffer.append("</b></td><td valign=\"top\">");

      openPara(buffer, "info");
      // Workaround: Without the &nbsp; the component are not put in one line.
      buffer.append("&nbsp;");

      int[] infoBitArr = ProgramInfoHelper.mInfoBitArr;
      Icon[] infoIconArr = ProgramInfoHelper.mInfoIconArr;
      String[] infoMsgArr = ProgramInfoHelper.mInfoMsgArr;

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

      buffer.append("</td></tr>");
      addSeperator(buffer);
    }

    addEntry(buffer, prog, ProgramFieldType.VPS_TYPE);
    addEntry(buffer, prog, ProgramFieldType.SHOWVIEW_NR_TYPE);

    int length = prog.getLength();
    if (length > 0) {

      buffer.append("<tr><td valign=\"top\" style=\"color:gray; font-size:");

      buffer.append(ProgramInfo.getInstance().getProperty("small","11"));

      buffer.append("\"><b>");
      buffer.append(mLocalizer.msg("duration", "Program duration/<br>-end"));
      buffer.append("</b></td><td style=\"font-size:");

      buffer.append(ProgramInfo.getInstance().getProperty("small","11"));

      buffer.append("\">");

      openPara(buffer, "time");

      String msg = mLocalizer.msg("minutes", "{0} min", new Integer(length));
      buffer.append(msg).append(" (");

      int hours = prog.getHours();
      int minutes = prog.getMinutes();
      int endTime = (hours * 60 + minutes + length) % (24 * 60);
      minutes = endTime % 60;
      hours = endTime / 60;
      String until = new StringBuffer().append(hours).append(':').append(
          minutes < 10 ? "0" : "").append(minutes).toString();
      buffer.append(mLocalizer.msg("until", "until {0}", until));

      int netLength = prog.getIntField(ProgramFieldType.NET_PLAYING_TIME_TYPE);
      if (netLength != -1) {
        msg = mLocalizer.msg("netMinuted", "{0} min net",
            new Integer(netLength));
        buffer.append(" - ").append(msg);
      }
      buffer.append(")");

      closePara(buffer);

      buffer.append("</td></tr>");
      addSeperator(buffer);
    }

    buffer.append("</table></html>");

    return buffer.toString();
  }

  private static void addEntry(StringBuffer buffer, Program prog,
      ProgramFieldType fieldType) {
    addEntry(buffer, prog, fieldType, false);
  }

  private static void addEntry(StringBuffer buffer, Program prog,
      ProgramFieldType fieldType, boolean createLinks) {

    String text = null;
    String name = fieldType.getLocalizedName();
    if (fieldType.getFormat() == ProgramFieldType.TEXT_FORMAT) {
      text = prog.getTextField(fieldType);
    } else if (fieldType.getFormat() == ProgramFieldType.TIME_FORMAT) {
      text = prog.getTimeFieldAsString(fieldType);
    } else if (fieldType.getFormat() == ProgramFieldType.INT_FORMAT) {
      text = prog.getIntFieldAsString(fieldType);
    }

    if (fieldType == ProgramFieldType.ORIGIN_TYPE) {
      String temp = prog
          .getIntFieldAsString(ProgramFieldType.PRODUCTION_YEAR_TYPE);
      if (temp != null && temp.trim().length() > 0) {
        if (text == null || text.trim().length() < 1) {
          name = ProgramFieldType.PRODUCTION_YEAR_TYPE.getLocalizedName();
          text = temp;
        } else {
          name += "/<br>"
              + ProgramFieldType.PRODUCTION_YEAR_TYPE.getLocalizedName();
          text += " " + temp;
        }
      }
    }

    if (text == null || text.trim().length() < 1)
      return;

    buffer.append("<tr><td valign=\"top\" style=\"color:#808080; font-size:");
    
    buffer.append(ProgramInfo.getInstance().getProperty("small","11"));
    
    buffer.append("\"><b>");    
    buffer.append(name);

    buffer.append("</b></td><td style=\"font-size:");
    
    buffer.append(ProgramInfo.getInstance().getProperty("small","11"));
    
    buffer.append("\">");
    buffer.append(HTMLTextHelper.convertTextToHtml(text, createLinks));
    buffer.append("</td></tr>");
    
    addSeperator(buffer);
  }

  private static void addSeperator(StringBuffer buffer) {
    buffer.append("<tr><td colspan=\"2\">");
    buffer.append("<div style=\"font-size:0;\"><hr></div></td></tr>");
  }

  private static void openPara(StringBuffer buffer, String style) {
    buffer.append("<div id=\"").append(style).append("\">");
  }

  private static void closePara(StringBuffer buffer) {
    buffer.append("</div>\n");
  }
}
