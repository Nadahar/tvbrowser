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

package util.program;

import java.awt.Font;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import tvbrowser.core.Settings;
import util.settings.ProgramPanelSettings;
import util.ui.PictureSettingsPanel;
import util.ui.UiUtilities;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.HTMLTextHelper;
import util.ui.html.HorizontalLine;
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

  /**
   * The Localizer for this class.
   */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ProgramTextCreator.class);

  private static String mBodyFontSize;


  /**
   * 
   * @param prog
   *          The Program to show
   * @param doc
   *          The HTMLDocument.
   * @param fieldArr The object array with the field types.
   * @param tFont The title Font.
   * @param bFont The body Font.
   * @param showImage If the image should be shown if it is available.
   * @param showHelpLinks Show the Help-Links (Quality of Data, ShowView)
   * @return The html String.
   */
  public static String createInfoText(Program prog, ExtendedHTMLDocument doc, 
      Object[] fieldArr, Font tFont, Font bFont, boolean showImage, boolean showHelpLinks) {
    return createInfoText(prog,doc,fieldArr,tFont,bFont,new ProgramPanelSettings(showImage ? PictureSettingsPanel.SHOW_EVER : PictureSettingsPanel.SHOW_NEVER, -1, -1, false, true, 10),showHelpLinks, 100);
  }
  
  /**
   * 
   * @param prog
   *          The Program to show
   * @param doc
   *          The HTMLDocument.
   * @param fieldArr
   *          The object array with the field types.
   * @param tFont
   *          The title Font.
   * @param bFont
   *          The body Font.
   * @param settings 
   * @param showImage
   *          Show the image.
   * @param showHelpLinks
   *          Show the Help-Links (Quality of Data, ShowView)
   * @param zoom The zoom value for the picture.
   * @return The html String.
   * @since 2.2.2
   */
  public static String createInfoText(Program prog, ExtendedHTMLDocument doc, 
      Object[] fieldArr, Font tFont, Font bFont, ProgramPanelSettings settings, 
      boolean showHelpLinks, int zoom) {
    return createInfoText(prog,doc,fieldArr,tFont,bFont,settings,showHelpLinks, zoom, true);
  }

  /**
   * 
   * @param prog
   *          The Program to show
   * @param doc
   *          The HTMLDocument.
   * @param fieldArr
   *          The object array with the field types.
   * @param tFont
   *          The title Font.
   * @param bFont
   *          The body Font.
   * @param settings 
   * @param showImage
   *          Show the image.
   * @param showHelpLinks
   *          Show the Help-Links (Quality of Data, ShowView)
   * @param zoom The zoom value for the picture.
   * @param showPluginIcons If the plugin icons should be shown.
   * @return The html String.
   * @since 2.5.3
   */
  public static String createInfoText(Program prog, ExtendedHTMLDocument doc, 
      Object[] fieldArr, Font tFont, Font bFont, ProgramPanelSettings settings, 
      boolean showHelpLinks, int zoom, boolean showPluginIcons) {
    // NOTE: All field types are included until type 25 (REPETITION_ON_TYPE)
    StringBuffer buffer = new StringBuffer();

    String titleFont, titleSize, bodyFont;

    if (tFont == null && bFont != null) {
      titleFont = bodyFont = bFont.getFamily();
      titleSize = mBodyFontSize = String.valueOf(bFont.getSize());
    } else if (tFont != null && bFont != null) {
      titleFont = tFont.getFamily();
      bodyFont = bFont.getFamily();
      titleSize = String.valueOf(tFont.getSize());
      mBodyFontSize = String.valueOf(bFont.getSize());
    } else
      return null;

    if (fieldArr == null)
      return null;

    buffer.append("<html>");
    buffer.append("<table width=\"100%\" style=\"font-family:");

    buffer.append(bodyFont);

    buffer.append(";\"><tr>");
    buffer.append("<td width=\"60\">");
    buffer.append("<p \"align=center\">");
    if (Settings.propEnableChannelIcons.getBoolean()) {
      JLabel channelLogo = new JLabel(prog.getChannel().getIcon());
      channelLogo.setToolTipText(prog.getChannel().getName());
      buffer.append(doc.createCompTag(channelLogo));
    }
    buffer.append("</p></td><td>");
    buffer.append("<div style=\"color:#ff0000; font-size:");

    buffer.append(mBodyFontSize);

    buffer.append(";\"><b>");
    buffer.append(prog.getDateString());
    buffer.append(" · ");
    buffer.append(prog.getTimeString());
    buffer.append(" - ");
    buffer.append(prog.getEndTimeString());
    buffer.append(" · ");
    buffer.append(prog.getChannel());

    buffer.append("</b></div><div style=\"color:#003366; font-size:");

    buffer.append(titleSize);

    buffer.append("; line-height:2.5em; font-family:");
    buffer.append(titleFont);
    buffer.append("\"><b>");
    buffer.append(prog.getTitle());
    buffer.append("</b></div>");

    String episode = prog.getTextField(ProgramFieldType.EPISODE_TYPE);

    if (episode != null && episode.trim().length() > 0) {
      buffer.append("<div style=\"color:#808080; font-size:");

      buffer.append(mBodyFontSize);

      buffer.append("\">");
      buffer.append(episode);
      buffer.append("</div>");
    }

    buffer.append("</td></tr>");

    boolean show = false;
    
    if(settings.isShowingPictureForPlugins()) {
      String[] pluginIds = settings.getPluginIds();
      Marker[] marker = prog.getMarkerArr();
      
      if(marker != null && pluginIds != null) {
        for(int i = 0; i < marker.length; i++) {
          for(int j = 0; j < pluginIds.length; j++) {
            if(marker[i].getId().compareTo(pluginIds[j]) == 0) {
              show = true;
              break;
            }
          }
        }
      }
    }
    
    if(settings.isShowingPictureEver() || 
      (settings.isShowingPictureInTimeRange() && !ProgramUtilities.isNotInTimeRange(settings.getPictureTimeRangeStart(),settings.getPictureTimeRangeEnd(), prog)) ||
      show || (settings.isShowingPictureForDuration() && settings.getDuration() <= prog.getLength())) {
      byte[] image = prog.getBinaryField(ProgramFieldType.PICTURE_TYPE);
      if (image != null) {
        String line = "<tr><td></td><td valign=\"top\" style=\"color:black; font-size:0\">";
        buffer.append(line);
        try {
          ImageIcon imageIcon = new ImageIcon(image);
          
          if(zoom != 100)
            imageIcon = (ImageIcon)UiUtilities.scaleIcon(imageIcon, imageIcon.getIconWidth() * zoom/100);
          
          StringBuffer value = new StringBuffer();
            
          if(prog.getTextField(ProgramFieldType.PICTURE_COPYRIGHT_TYPE) != null)
            value.append(prog.getTextField(ProgramFieldType.PICTURE_COPYRIGHT_TYPE));
          
          if(settings.isShowingPictureDescription() &&  prog.getTextField(ProgramFieldType.PICTURE_DESCRIPTION_TYPE) != null)
            value.append("<br>").append(prog.getTextField(ProgramFieldType.PICTURE_DESCRIPTION_TYPE));
                    
          buffer.append(doc.createCompTag(new JLabel(imageIcon)));
          buffer.append("<div style=\"font-size:");

          buffer.append(mBodyFontSize);

          buffer.append("\">");
          buffer.append(value);
          buffer.append("</div>");          
          buffer.append("</td></tr>");
        } catch (Exception e) {
          // Picture was wrong;
          buffer.delete(buffer.length() - line.length(), buffer.length());
        }
      }
    }

    Marker[] pluginArr = prog.getMarkerArr();
    if (showPluginIcons && (pluginArr != null) && (pluginArr.length != 0)) {
      addSeperator(doc, buffer);

      buffer.append("<tr><td valign=\"top\" style=\"color:#808080; font-size:");

      buffer.append(mBodyFontSize);

      buffer.append("\"><b>");
      buffer.append(mLocalizer.msg("markedBy", "Marked by"));
      buffer.append("</b></td><td valign=\"middle\" style=\"font-size:4\">");
      openPara(buffer, "info");

      // Workaround: Without the &nbsp; the component are not put in one line.
      buffer.append("&nbsp;");
      for (Marker marker : pluginArr) {
        Icon[] icons = marker.getMarkIcons(prog);

        if (icons != null)
          for (Icon icon : icons) {
            JLabel iconLabel = new JLabel(icon);
            PluginAccess plugin = Plugin.getPluginManager()
                .getActivatedPluginForId(marker.getId());

            if (plugin != null) {
              iconLabel.setToolTipText(plugin.getInfo().getName());
            }
            else {
            	iconLabel.setToolTipText(marker.toString());
            }

            buffer.append(doc.createCompTag(iconLabel));
            buffer.append("&nbsp;&nbsp;");
          }
      }
      closePara(buffer);
      buffer.append("</td></tr>");
    }

    PluginAccess[] plugins = Plugin.getPluginManager().getActivatedPlugins();
    ArrayList<JLabel> icons = new ArrayList<JLabel>();
    for (int i = 0; i < plugins.length; i++) {
      Icon[] ico = plugins[i].getProgramTableIcons(prog);

      if (ico != null) {
        for (int t = 0; t < ico.length; t++) {
          JLabel iconLabel = new JLabel(ico[t]);
          iconLabel.setToolTipText(plugins[i].getInfo().getName());
          icons.add(iconLabel);
        }
      }
    }

    if (showPluginIcons && icons.size() > 0) {
      addSeperator(doc, buffer);

      buffer
          .append("<tr><td valign=\"middle\" style=\"color:#808080; font-size:");

      buffer.append(mBodyFontSize);

      buffer.append("\"><b>");
      buffer.append("Plugin-Icons");
      buffer.append("</b></td><td valign=\"top\" style=\"font-size:4\">");

      openPara(buffer, "info");
      // Workaround: Without the &nbsp; the component are not put in one line.
      buffer.append("&nbsp;");

      for (int i = 0; i < icons.size(); i++) {
        buffer.append(doc.createCompTag(icons.get(i)));
        buffer.append("&nbsp;&nbsp;");
      }

      closePara(buffer);
      buffer.append("</td></tr>");
    }

    addSeperator(doc, buffer);

    Object[] id = fieldArr;

    for (int j = 0; j < id.length; j++) {
      ProgramFieldType type = null;

      if (id[j] instanceof String)
        try {
          type = ProgramFieldType
              .getTypeForId(Integer.parseInt((String) id[j]));
        } catch (Exception e) {
          int length = prog.getLength();
          if (length > 0 && ((String) id[j]).trim().length() > 0) {

            buffer
                .append("<tr><td valign=\"top\" style=\"color:gray; font-size:");

            buffer.append(mBodyFontSize);

            buffer.append("\"><b>");
            buffer.append(mLocalizer.msg("duration",
                "Program duration/<br>-end"));
            buffer.append("</b></td><td style=\"font-size:");

            buffer.append(mBodyFontSize);

            buffer.append("\">");

            openPara(buffer, "time");

            String msg = mLocalizer.msg("minutes", "{0} min", new Integer(
                length));
            buffer.append(msg).append(" (");
            buffer.append(mLocalizer.msg("until", "until {0}", prog.getEndTimeString()));

            int netLength = prog
                .getIntField(ProgramFieldType.NET_PLAYING_TIME_TYPE);
            if (netLength != -1) {
              msg = mLocalizer.msg("netMinuted", "{0} min net", new Integer(
                  netLength));
              buffer.append(" - ").append(msg);
            }
            buffer.append(")");

            closePara(buffer);

            buffer.append("</td></tr>");
            addSeperator(doc, buffer);
          }
          continue;
        }
      else
        type = (ProgramFieldType) id[j];

      if (type == ProgramFieldType.DESCRIPTION_TYPE) {
        if (prog.getDescription() != null
            && prog.getDescription().trim().length() > 0)
          addEntry(doc, buffer, prog, ProgramFieldType.DESCRIPTION_TYPE, true,
              showHelpLinks);
        else
          addEntry(doc, buffer, prog, ProgramFieldType.SHORT_DESCRIPTION_TYPE,
              true, showHelpLinks);
      } else if (type == ProgramFieldType.INFO_TYPE) {
        int info = prog.getInfo();
        if ((info != -1) && (info != 0)) {
          buffer
              .append("<tr><td valign=\"top\" style=\"color:gray; font-size:");

          buffer.append(mBodyFontSize);

          buffer.append("\"><b>");
          buffer
              .append(type.getLocalizedName()/*
                                               * mLocalizer.msg("attributes",
                                               * "Program attributes")
                                               */);
          buffer
              .append("</b></td><td valign=\"middle\" style=\"font-size:5\">");

          openPara(buffer, "info");
          // Workaround: Without the &nbsp; the component are not put in one
          // line.
          buffer.append("&nbsp;");

          int[] infoBitArr = ProgramInfoHelper.mInfoBitArr;
          Icon[] infoIconArr = ProgramInfoHelper.mInfoIconArr;
          String[] infoMsgArr = ProgramInfoHelper.mInfoMsgArr;

          for (int i = 0; i < infoBitArr.length; i++) {
            if (ProgramInfoHelper.bitSet(info, infoBitArr[i])) {
              if (infoIconArr[i] != null) {
                JLabel iconLabel = new JLabel(infoIconArr[i]);
                iconLabel.setToolTipText(infoMsgArr[i]);
                buffer.append(doc.createCompTag(iconLabel));
              } else {
                buffer.append(infoMsgArr[i]);
              }
              buffer.append("&nbsp;&nbsp;");
            }
          }

          closePara(buffer);

          buffer.append("</td></tr>");
          addSeperator(doc, buffer);
        }
      } else if (type == ProgramFieldType.URL_TYPE)
        addEntry(doc, buffer, prog, ProgramFieldType.URL_TYPE, true,
            showHelpLinks);
      else
        addEntry(doc, buffer, prog, type, showHelpLinks);
    }

    if (showHelpLinks) {
      buffer
          .append("<tr><td colspan=\"2\" valign=\"top\" align=\"center\" style=\"color:#808080; font-size:");
      buffer.append(mBodyFontSize).append("\">");
      buffer.append("<a href=\"");
      buffer.append(
          mLocalizer.msg("dataInfo",
              "http://wiki.tvbrowser.org/index.php/Qualit%C3%A4t_der_Daten"))
          .append("\">");
      buffer.append(mLocalizer
          .msg("dataQuality", "Details of the data quality"));
      buffer.append("</a>");
      buffer.append("</td></tr>");
    }
    buffer.append("</table></html>");

    return buffer.toString();
  }

  private static void addEntry(ExtendedHTMLDocument doc, StringBuffer buffer,
      Program prog, ProgramFieldType fieldType, boolean showHelpLinks) {
    addEntry(doc, buffer, prog, fieldType, false, showHelpLinks);
  }

  private static void addEntry(ExtendedHTMLDocument doc, StringBuffer buffer,
      Program prog, ProgramFieldType fieldType, boolean createLinks,
      boolean showHelpLinks) {

    String text = null;
    String name = fieldType.getLocalizedName();
    if (fieldType.getFormat() == ProgramFieldType.TEXT_FORMAT) {
      if (fieldType == ProgramFieldType.DESCRIPTION_TYPE) {
        String description = prog.getDescription().trim();

        if (prog.getShortInfo() != null) {
          StringBuffer shortInfo = new StringBuffer(prog.getShortInfo().trim());

          while (shortInfo.toString().endsWith("."))
            shortInfo.deleteCharAt(shortInfo.length() - 1);

          if (!description.trim().startsWith(shortInfo.toString())) {
            addEntry(doc, buffer, prog,
                ProgramFieldType.SHORT_DESCRIPTION_TYPE, true, showHelpLinks);
          }
        }
      }

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
      if (ProgramFieldType.SHOWVIEW_NR_TYPE == fieldType)
        text = mLocalizer.msg("noShowview", "No Showview data ");
      else
        return;

    buffer.append("<tr><td valign=\"top\" style=\"color:#808080; font-size:");

    buffer.append(mBodyFontSize);

    buffer.append("\"><b>");
    buffer.append(name);

    buffer.append("</b></td><td style=\"font-size:");

    buffer.append(mBodyFontSize);

    buffer.append("\">");
    buffer.append(HTMLTextHelper.convertTextToHtml(text, createLinks));

    if ((ProgramFieldType.SHOWVIEW_NR_TYPE == fieldType) && (showHelpLinks))
      buffer.append(" (<a href=\"").append(
          mLocalizer.msg("showviewInfo",
              "http://wiki.tvbrowser.org/index.php/Showviewnummern")).append(
          "\">?</a>)");

    buffer.append("</td></tr>");

    addSeperator(doc, buffer);
  }

  private static void addSeperator(ExtendedHTMLDocument doc, StringBuffer buffer) {
    buffer.append("<tr><td colspan=\"2\">");
    buffer.append("<div style=\"font-size:0;\">").append(
        doc.createCompTag(new HorizontalLine())).append("</div></td></tr>");
  }

  private static void openPara(StringBuffer buffer, String style) {
    buffer.append("<div id=\"").append(style).append("\">");
  }

  private static void closePara(StringBuffer buffer) {
    buffer.append("</div>\n");
  }

  /**
   *
   * @return The default order of the entries.
   */
  public static Object[] getDefaultOrder() {
    return new Object[] {
        ProgramFieldType.GENRE_TYPE,
        ProgramFieldType.DESCRIPTION_TYPE,        
        ProgramFieldType.ORIGIN_TYPE,
        ProgramFieldType.DIRECTOR_TYPE,
        ProgramFieldType.SCRIPT_TYPE,
        ProgramFieldType.ACTOR_LIST_TYPE,
        ProgramFieldType.MODERATION_TYPE,
        ProgramFieldType.MUSIC_TYPE,
        ProgramFieldType.URL_TYPE,
        ProgramFieldType.ORIGINAL_TITLE_TYPE,
        ProgramFieldType.ORIGINAL_EPISODE_TYPE,
        ProgramFieldType.REPETITION_OF_TYPE,
        ProgramFieldType.REPETITION_ON_TYPE, ProgramFieldType.AGE_LIMIT_TYPE,
        ProgramFieldType.INFO_TYPE, ProgramFieldType.VPS_TYPE,
        ProgramFieldType.SHOWVIEW_NR_TYPE, getDurationTypeString() };
  }

  /**
   * @return The String for the duration/end of a program.
   */
  public static String getDurationTypeString() {
    return mLocalizer.msg("duration", "Program duration/<br>-end").replaceAll(
        "<br>", "");
  }
}
