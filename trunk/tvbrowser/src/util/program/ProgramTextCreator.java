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

import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
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
    return createInfoText(prog,doc,fieldArr,tFont,bFont,new ProgramPanelSettings(showImage ? ProgramPanelSettings.SHOW_PICTURES_EVER : ProgramPanelSettings.SHOW_PICTURES_NEVER, -1, -1, false, true, 10),showHelpLinks, 100);
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
   *          Settings of the ProgramPanel 
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
   *          Settings of the ProgramPanel 
   * @param showHelpLinks
   *          Show the Help-Links (Quality of Data, ShowView)
   * @param zoom The zoom value for the picture.
   * @return The html String.
   * @since 2.6
   */
  public static String createInfoText(Program prog, ExtendedHTMLDocument doc, 
      Object[] fieldArr, Font tFont, Font bFont, PluginPictureSettings settings, 
      boolean showHelpLinks, int zoom) {
    return createInfoText(prog,doc,fieldArr,tFont,bFont,new ProgramPanelSettings(settings,false),showHelpLinks, zoom, true);
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
   *          Settings of the ProgramPanel
   * @param showHelpLinks
   *          Show the Help-Links (Quality of Data, ShowView)
   * @param zoom The zoom value for the picture.
   * @param showPluginIcons If the plugin icons should be shown.
   * @return The html String.
   * @since 2.5.3
   */
  public static String createInfoText(Program prog, ExtendedHTMLDocument doc, 
      Object[] fieldArr, Font tFont, Font bFont, ProgramPanelSettings settings, 
      boolean showHelpLinks, int zoom, boolean showPluginIcons) {try {
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
    } else {
      return null;
    }

    if (fieldArr == null) {
      return null;
    }

    buffer.append("<html>");
    buffer.append("<table width=\"100%\" style=\"font-family:");

    buffer.append(bodyFont);

    buffer.append(";\"><tr>");
    buffer.append("<td width=\"60\">");
    buffer.append("<p \"align=center\">");
    
    JLabel channelLogo = new JLabel(prog.getChannel().getIcon());
    channelLogo.setToolTipText(prog.getChannel().getName());
    buffer.append(doc.createCompTag(channelLogo));
    
    buffer.append("</p></td><td>");
    buffer.append("<div style=\"color:#ff0000; font-size:");

    buffer.append(mBodyFontSize);

    buffer.append(";\"><b>");
    buffer.append(prog.getDateString());
    buffer.append(" · ");
    buffer.append(prog.getTimeString());
    buffer.append("-");
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

    /*String episode = prog.getTextField(ProgramFieldType.EPISODE_TYPE);

    if (episode != null && episode.trim().length() > 0) {
      buffer.append("<div style=\"color:#808080; font-size:");

      buffer.append(mBodyFontSize);

      buffer.append("\">");
      buffer.append(episode);
      buffer.append("</div>");
    }*/

    buffer.append("</td></tr>");

    boolean show = false;
    
    if(settings.isShowingPictureForPlugins()) {
      String[] pluginIds = settings.getPluginIds();
      Marker[] markers = prog.getMarkerArr();
      
      if(markers != null && pluginIds != null) {
        for (Marker marker : markers) {
          for (String pluginId : pluginIds) {
            if(marker.getId().compareTo(pluginId) == 0) {
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
          
          if(zoom != 100) {
            imageIcon = (ImageIcon)UiUtilities.scaleIcon(imageIcon, imageIcon.getIconWidth() * zoom/100);
          }
          
          StringBuffer value = new StringBuffer();
            
          if(prog.getTextField(ProgramFieldType.PICTURE_COPYRIGHT_TYPE) != null) {
            value.append(prog.getTextField(ProgramFieldType.PICTURE_COPYRIGHT_TYPE));
          }
          
          if(settings.isShowingPictureDescription() &&  prog.getTextField(ProgramFieldType.PICTURE_DESCRIPTION_TYPE) != null) {
            value.append("<br>").append(prog.getTextField(ProgramFieldType.PICTURE_DESCRIPTION_TYPE));
          }
                    
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
      addSeparator(doc, buffer);

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

        if (icons != null) {
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
      }
      closePara(buffer);
      buffer.append("</td></tr>");
    }

    PluginAccess[] plugins = Plugin.getPluginManager().getActivatedPlugins();
    ArrayList<JLabel> iconLabels = new ArrayList<JLabel>();
    for (PluginAccess plugin : plugins) {
      Icon[] icons = plugin.getProgramTableIcons(prog);

      if (icons != null) {
        for (Icon icon : icons) {
          JLabel iconLabel = new JLabel(icon);
          iconLabel.setToolTipText(plugin.getInfo().getName());
          iconLabels.add(iconLabel);
        }
      }
    }

    if (showPluginIcons && iconLabels.size() > 0) {
      addSeparator(doc, buffer);

      buffer
          .append("<tr><td valign=\"middle\" style=\"color:#808080; font-size:");

      buffer.append(mBodyFontSize);

      buffer.append("\"><b>");
      buffer.append("Plugin-Icons");
      buffer.append("</b></td><td valign=\"top\" style=\"font-size:4\">");

      openPara(buffer, "info");
      // Workaround: Without the &nbsp; the component are not put in one line.
      buffer.append("&nbsp;");

      for (int i = 0; i < iconLabels.size(); i++) {
        buffer.append(doc.createCompTag(iconLabels.get(i)));
        buffer.append("&nbsp;&nbsp;");
      }

      closePara(buffer);
      buffer.append("</td></tr>");
    }

    addSeparator(doc, buffer);

    Object[] ids = fieldArr;

    for (Object id : ids) {
      ProgramFieldType type = null;

      if (id instanceof String) {
        if (((String) id).matches("\\d+")) {
          try {
            type = ProgramFieldType
                .getTypeForId(Integer.parseInt((String) id, 10));
          } catch (Exception e) {
            
          }
        }
        
        if (type == null) {
          int length = prog.getLength();
          if (length > 0 && ((String) id).trim().length() > 0) {

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
            addSeparator(doc, buffer);
          }
        }
      } else if(id instanceof CompoundedProgramFieldType) {
        CompoundedProgramFieldType value = (CompoundedProgramFieldType) id;
        String entry = value.getFormatedValueForProgram(prog);
        
        if(entry != null) {
          startInfoSection(buffer, value.getName());
          buffer.append(HTMLTextHelper.convertTextToHtml(entry, false));
          
          addSeparator(doc,buffer);
        }
      }
      else {
        type = (ProgramFieldType) id;      

        if (type == ProgramFieldType.DESCRIPTION_TYPE) {
          if (prog.getDescription() != null
              && prog.getDescription().trim().length() > 0) {
            addEntry(doc, buffer, prog, ProgramFieldType.DESCRIPTION_TYPE, true,
                showHelpLinks);
          } else {
            addEntry(doc, buffer, prog, ProgramFieldType.SHORT_DESCRIPTION_TYPE,
                true, showHelpLinks);
          }
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
            addSeparator(doc, buffer);
          }
        } else if (type == ProgramFieldType.URL_TYPE) {
          addEntry(doc, buffer, prog, ProgramFieldType.URL_TYPE, true,
              showHelpLinks);
        } else if (type == ProgramFieldType.ACTOR_LIST_TYPE) {
          ArrayList<String> actorsList = new ArrayList<String>();
          String[] recognizedActors = ProgramUtilities.getActorsFromActorsField(prog);
          if (recognizedActors != null) {
            for (String actorName : recognizedActors) {
              actorsList.add(actorName);
            }
          }
          String actorField = prog.getTextField(type);
          if (actorField != null) {
            actorField = actorField.trim();
            String[] actors = new String[0];
            if (actorField.contains("\n")) {
              actors = actorField.split("\n");
            }
            else if (actorField.contains(",")) {
              actors = actorField.split(",");
            }
            else if (actorField.contains("\t")) {
              actors = new String[1];
              actors[0] = actorField;
            }
            if (actors.length > 0) {
              startInfoSection(buffer, type.getLocalizedName());
              buffer.append("<table border=\"0\" cellpadding=\"0\" style=\"font-family:");
              buffer.append(bodyFont);
              buffer.append(";\">");
              for (String actor : actors) {
                actor = actor.trim();
                if (actor != "") {
                  String part1 = actor;
                  String part2 = "";
                  if (actor.contains("\t")) {
                    part1 = actor.substring(0, actor.indexOf("\t")).trim();
                    part2 = actor.substring(actor.indexOf("\t")).trim();
                  }
                  else if (actor.contains("(") && actor.contains(")")) {
                    part1 = actor.substring(0, actor.indexOf("(")).trim();
                    part2 = actor.substring(actor.indexOf("(")+1, actor.lastIndexOf(")")).trim();
                  }
                  if (actorsList.contains(part1)) {
                    part1 = addWikiLink(part1);
                  }
                  if (actorsList.contains(part2)) {
                    part2 = addWikiLink(part2);
                  }
                  buffer.append("<tr><td>");
                  buffer.append(part1);
                  buffer.append("</td><td width=\"10\">&nbsp;</td><td>");
                  buffer.append(part2);
                  buffer.append("</td></tr>");
                }
              }
              buffer.append("</table>");
              buffer.append("</td></tr>");
              addSeparator(doc, buffer);
            }
            else {
              addEntry(doc, buffer, prog, type, showHelpLinks);
            }
          }
        }
        else {
          addEntry(doc, buffer, prog, type, showHelpLinks);
        }
      }
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

    return buffer.toString();}catch(Exception e) {e.printStackTrace();}
    return "";
  }

  private static String addWikiLink(String topic, String displayText) {
    String url = topic;
    String style = " style=\"color:black; text-decoration:none; border-bottom: 1px dashed;\"";
    return mLocalizer.msg("wikipediaLink", "<a href=\"http://en.wikipedia.org/wiki/{0}\"{1}>{2}</a>", url, style, displayText);
  }

  private static String addWikiLink(String topic) {
    return addWikiLink(topic, topic);
  }
  
  private static String[] splitList(String field) {
    String[] items;
    if (field.contains("\n")) {
      items = field.split("\n");
    }
    else if (field.contains(",")) {
      items = field.split(",");
    }
    else {
      items = new String[1];
      items[0] = field;
    }
    for (int i = 0; i < items.length; i++) {
      items[i] = items[i].trim();
    }
    return items;
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

          while (shortInfo.toString().endsWith(".")) {
            shortInfo.deleteCharAt(shortInfo.length() - 1);
          }

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

    if (text == null || text.trim().length() < 1) {
      if (ProgramFieldType.SHOWVIEW_NR_TYPE == fieldType) {
        text = mLocalizer.msg("noShowview", "No Showview data ");
      } else {
        return;
      }
    }
    
    startInfoSection(buffer, name);

    // add wikipedia links
    if (ProgramFieldType.DIRECTOR_TYPE == fieldType || ProgramFieldType.SCRIPT_TYPE == fieldType) {
      String[] directors = splitList(text);
      for (int i = 0; i < directors.length; i++) {
        String topic = directors[i];
        if (directors[i].contains("(")) {
          topic = directors[i].substring(0, directors[i].indexOf("(")-1);
        }
        directors[i] = addWikiLink(topic, directors[i]);
      }
      buffer.append(concatList(directors));
    }
    else {
      buffer.append(HTMLTextHelper.convertTextToHtml(text, createLinks));
    }
    
    if ((ProgramFieldType.SHOWVIEW_NR_TYPE == fieldType) && (showHelpLinks)) {
      buffer.append(" (<a href=\"").append(
          mLocalizer.msg("showviewInfo",
              "http://wiki.tvbrowser.org/index.php/Showviewnummern")).append(
          "\">?</a>)");
    }

    buffer.append("</td></tr>");

    addSeparator(doc, buffer);
  }

  private static String concatList(String[] strings) {
    String result = "";
    for (int i = 0; i < strings.length; i++) {
      if (i > 0) {
        result = result + ", ";
      }
      result = result + strings[i];
    }
    return result;
  }

  private static void startInfoSection(StringBuffer buffer, String section) {
    buffer.append("<tr><td valign=\"top\" style=\"color:#808080; font-size:");
    buffer.append(mBodyFontSize);
    buffer.append("\"><b>");
    buffer.append(section);
    buffer.append("</b></td><td style=\"font-size:");
    buffer.append(mBodyFontSize);
    buffer.append("\">");
  }

  private static void addSeparator(ExtendedHTMLDocument doc, StringBuffer buffer) {
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
        ProgramFieldType.PRODUCER_TYPE,
        ProgramFieldType.CAMERA_TYPE,
        ProgramFieldType.CUTTER_TYPE,
        ProgramFieldType.URL_TYPE,
        ProgramFieldType.ORIGINAL_TITLE_TYPE,
        ProgramFieldType.ORIGINAL_EPISODE_TYPE,
        CompoundedProgramFieldType.EPISODE_COMPOSITION,
        ProgramFieldType.REPETITION_OF_TYPE,
        ProgramFieldType.REPETITION_ON_TYPE, ProgramFieldType.AGE_LIMIT_TYPE,
        ProgramFieldType.INFO_TYPE, ProgramFieldType.VPS_TYPE,
        ProgramFieldType.SHOWVIEW_NR_TYPE, 
        getDurationTypeString() };
  }

  /**
   * @return The String for the duration/end of a program.
   */
  public static String getDurationTypeString() {
    return mLocalizer.msg("duration", "Program duration/<br>-end").replaceAll(
        "<br>", "");
  }
}
