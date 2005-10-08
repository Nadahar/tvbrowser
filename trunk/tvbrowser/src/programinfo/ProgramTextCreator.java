package programinfo;

import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.HTMLTextHelper;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramInfoHelper;

public class ProgramTextCreator {
  
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(ProgramTextCreator.class);
    
  public String createInfoText(Program prog, ExtendedHTMLDocument doc, String styleSheet) {
    // NOTE: All field types are included until type 25 (REPETITION_ON_TYPE)

    StringBuffer buffer = new StringBuffer();

    buffer.append("<html><head>" + "<style type=\"text/css\" media=\"screen\">"
        + "<!--" + styleSheet + "-->" + "</style>" + "</head>" + "<body>");

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
        msg = mLocalizer.msg("netMinuted", "{0} min net",
            new Integer(netLength));
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
    }

    PluginAccess[] plugins = Plugin.getPluginManager().getActivatedPlugins();
    ArrayList icons = new ArrayList();
    for (int i = 0; i < plugins.length; i++) {
      Icon[] ico = plugins[i].getProgramTableIcons(prog);
      if (ico != null
          && !plugins[i].getId().equals("java.programinfo.ProgramInfo")) {
        for (int t = 0; t < ico.length; t++) {
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

      for (int i = 0; i < icons.size(); i++) {
        buffer.append(doc.createCompTag((JLabel) icons.get(i)));
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
      try {
        Icon icon = new ImageIcon(image);
        JLabel iconLabel = new JLabel(icon);
        buffer.append(doc.createCompTag(iconLabel));
      } catch(Exception e) {
        // Picture was wrong;
      }
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
      ProgramFieldType fieldType) {
    newPara(buffer, style, prog, fieldType, false);
  }

  private void newPara(StringBuffer buffer, String style, Program prog,
      ProgramFieldType fieldType, boolean createLinks) {
    String label = fieldType.getLocalizedName();

    String text = null;
    if (fieldType.getFormat() == ProgramFieldType.TEXT_FORMAT) {
      text = prog.getTextField(fieldType);
    } else if (fieldType.getFormat() == ProgramFieldType.TIME_FORMAT) {
      text = prog.getTimeFieldAsString(fieldType);
    } else if (fieldType.getFormat() == ProgramFieldType.INT_FORMAT) {
      text = prog.getIntFieldAsString(fieldType);
    }

    newPara(buffer, style, label, text, createLinks);
  }

  private void newPara(StringBuffer buffer, String style, String text) {
    newPara(buffer, style, null, text, false);
  }

  private void newPara(StringBuffer buffer, String style, String text,
      boolean createLinks) {
    newPara(buffer, style, null, text, createLinks);
  }

  private void newPara(StringBuffer buffer, String style, String label,
      String text, boolean createLinks) {
    if ((text == null) || (text.length() == 0)) {
      return;
    }

    text = HTMLTextHelper.convertTextToHtml(text, createLinks);
    
    openPara(buffer, style);
    if (label != null) {
      buffer.append("<b>" + label + ":</b> ");
    }
    buffer.append(text);
    closePara(buffer);
  }

}
