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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import tvbrowser.core.Settings;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.extras.favoritesplugin.FavoritesPluginProxy;
import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTreeModel;
import tvbrowser.extras.programinfo.ProgramInfo;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.HTMLTextHelper;
import util.ui.html.HorizontalLine;
import devplugin.Date;
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
  
  /** The used link protocol for actor links */
  public static final String TVBROWSER_URL_PROTOCOL = "tvbrowser://";


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
   * @return The HTML String.
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
   * @return The HTML String.
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
   * @return The HTML String.
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
   * @return The HTML String.
   * @since 2.5.3
   */
  public static String createInfoText(Program prog, ExtendedHTMLDocument doc, 
      Object[] fieldArr, Font tFont, Font bFont, ProgramPanelSettings settings, 
      boolean showHelpLinks, int zoom, boolean showPluginIcons) {
    return createInfoText(prog, doc, fieldArr, tFont, bFont, settings,
        showHelpLinks, zoom, showPluginIcons, true);
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
   * @param zoom
   *          The zoom value for the picture.
   * @param showPluginIcons
   *          If the plugin icons should be shown.
   * @return The HTML String.
   * @since 3.0
   */
  public static String createInfoText(Program prog, ExtendedHTMLDocument doc,
      Object[] fieldArr, Font tFont, Font bFont, ProgramPanelSettings settings,
      boolean showHelpLinks, int zoom, boolean showPluginIcons,
      boolean showPersonLinks) {
    String debugTables = "0"; //set to "1" for debugging, to "0" for no debugging
    try {
    // NOTE: All field types are included until type 25 (REPETITION_ON_TYPE)
      StringBuilder buffer = new StringBuilder(1024);

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
    buffer.append("<table width=\"100%\" border=\"" + debugTables + "\" style=\"font-family:");

    buffer.append(bodyFont);

    buffer.append(";\"><tr>");
    buffer.append("<td width=\"60\">");
    buffer.append("<p \"align=center\">");
    
    JLabel channelLogo = new JLabel(prog.getChannel().getIcon());
    channelLogo.setToolTipText(prog.getChannel().getName());
    buffer.append(doc.createCompTag(channelLogo));
    
    buffer.append("</p></td><td><table width=\"100%\" border=\""+ debugTables +"\"><tr><td>");
    buffer.append("<div style=\"color:#ff0000; font-size:");

    buffer.append(mBodyFontSize);

    buffer.append(";\"><b>");
    
    Date currentDate = Date.getCurrentDate();
    Date programDate = prog.getDate();
    if(programDate.equals(currentDate.addDays(-1))) {
      buffer.append(Localizer.getLocalization(Localizer.I18N_YESTERDAY));
      buffer.append(" · ");
    }
    else if(programDate.equals(currentDate)){
      buffer.append(Localizer.getLocalization(Localizer.I18N_TODAY));
      buffer.append(" · ");
    }
    else if(programDate.equals(currentDate.addDays(1))){
      buffer.append(Localizer.getLocalization(Localizer.I18N_TOMORROW));
      buffer.append(" · ");
    }
    buffer.append(prog.getDateString());
    
    buffer.append(" · ");
    buffer.append(prog.getTimeString());
    if (prog.getLength() > 0) {
      buffer.append('-');
      buffer.append(prog.getEndTimeString());
    }
    buffer.append(" · ");
    buffer.append(prog.getChannel());

    buffer.append("</b></div><div style=\"color:#003366; font-size:");

    buffer.append(titleSize);

    buffer.append("; line-height:2.5em; font-family:");
    buffer.append(titleFont);
    buffer.append("\"><b>");
    buffer.append(prog.getTitle());
    buffer.append("</b></div>");

    String episode = CompoundedProgramFieldType.EPISODE_COMPOSITION.getFormatedValueForProgram(prog);
    
    if (episode != null && episode.trim().length() > 0) {
      buffer.append("<div style=\"color:#808080; font-size:");

      buffer.append(mBodyFontSize);

      buffer.append("\">");
      buffer.append(episode);
      buffer.append("</div>");
    }

    buffer.append("</td><td align=\"right\"><table border=\"" + debugTables +"\"><tr><td>");
    JButton btn = new JButton(TVBrowserIcons.left(TVBrowserIcons.SIZE_LARGE));
    buffer.append(doc.createCompTag(btn));
    btn.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        ProgramInfo.getInstance().historyBack();
      }
    });
    btn.setEnabled(ProgramInfo.getInstance().canNavigateBack());
    btn.setToolTipText(ProgramInfo.getInstance().navigationBackwardText());

    buffer.append("</td><td>");
    btn = new JButton(TVBrowserIcons.right(TVBrowserIcons.SIZE_LARGE));
    buffer.append(doc.createCompTag(btn));
    btn.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        ProgramInfo.getInstance().historyForward();
      }
    });
    btn.setEnabled(ProgramInfo.getInstance().canNavigateForward());
    btn.setToolTipText(ProgramInfo.getInstance().navigationForwardText());
    
    buffer.append("</td></tr></table></td></tr></table></td></tr>");

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
    
    Color foreground = Settings.propProgramPanelForegroundColor.getColor();
    
    if(settings.isShowingPictureEver() || 
      (settings.isShowingPictureInTimeRange() && !ProgramUtilities.isNotInTimeRange(settings.getPictureTimeRangeStart(),settings.getPictureTimeRangeEnd(), prog)) ||
      show || (settings.isShowingPictureForDuration() && settings.getDuration() <= prog.getLength())) {
      byte[] image = prog.getBinaryField(ProgramFieldType.PICTURE_TYPE);
      if (image != null) {
        String line = "<tr><td></td><td valign=\"top\" style=\"color:rgb("+ foreground.getRed() + "," + foreground.getGreen() + "," + foreground.getBlue() + "); font-size:0\">";
        buffer.append(line);
        try {
          ImageIcon imageIcon = new ImageIcon(image);
          
          if(zoom != 100) {
            imageIcon = (ImageIcon)UiUtilities.scaleIcon(imageIcon, imageIcon.getIconWidth() * zoom/100);
          }
          
          StringBuilder value = new StringBuilder();
            
          String textField = prog.getTextField(ProgramFieldType.PICTURE_COPYRIGHT_TYPE);
          if (textField != null) {
            value.append(textField);
          }
          
          if (settings.isShowingPictureDescription()) {
              textField = prog
                  .getTextField(ProgramFieldType.PICTURE_DESCRIPTION_TYPE);
              if (textField != null) {
                value.append("<br>").append(textField);
              }
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
      for (int markerCount = pluginArr.length-1; markerCount >= 0; markerCount--) {
        Icon[] icons = pluginArr[markerCount].getMarkIcons(prog);

        if (icons != null) {
          for(int i = icons.length - 1; i >= 0 ; i--) {
            JLabel iconLabel = new JLabel(icons[i]);
            PluginAccess plugin = Plugin.getPluginManager()
                .getActivatedPluginForId(pluginArr[markerCount].getId());
            if (plugin != null) {
              iconLabel.setToolTipText(plugin.getInfo().getName());
            }
            else {
              InternalPluginProxyIf internalPlugin = InternalPluginProxyList.getInstance().getProxyForId(pluginArr[markerCount].getId());
              if (internalPlugin != null) {
                iconLabel.setToolTipText(internalPlugin.getName());
                if (internalPlugin.equals(FavoritesPluginProxy.getInstance())) {
                  // if this is a favorite, add the names of the favorite
                  String favTitles = "";
                  for (Favorite favorite : FavoriteTreeModel.getInstance().getFavoritesContainingProgram(prog)) {
                    if (favTitles.length() > 0) {
                      favTitles = favTitles + ", ";
                    }
                    favTitles = favTitles + favorite.getName();
                  }
                  if (favTitles.length() > 0) {
                    iconLabel.setToolTipText(iconLabel.getToolTipText() + " (" + favTitles + ")");
                  }
                }
              }
              else {
                iconLabel.setToolTipText(pluginArr[markerCount].toString());
              }
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

      for (JLabel iconLabel : iconLabels) {
        buffer.append(doc.createCompTag(iconLabel));
        buffer.append("&nbsp;&nbsp;");
      }

      closePara(buffer);
      buffer.append("</td></tr>");
    }

    addSeparator(doc, buffer);

    for (Object id : fieldArr) {
      ProgramFieldType type = null;

      if (id instanceof String) {
        if (((String) id).matches("\\d+")) {
          try {
            type = ProgramFieldType
                .getTypeForId(Integer.parseInt((String) id, 10));
          } catch (Exception e) {
            // Empty Catch
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
            buffer.append("</b></td><td style=\"color:rgb("+ foreground.getRed() + "," + foreground.getGreen() + "," + foreground.getBlue() + "); font-size:");

            buffer.append(mBodyFontSize);

            buffer.append("\">");

            openPara(buffer, "time");

            String msg = mLocalizer.msg("minutes", "{0} min", length);
            buffer.append(msg).append(" (");
            buffer.append(mLocalizer.msg("until", "until {0}", prog.getEndTimeString()));

            int netLength = prog
                .getIntField(ProgramFieldType.NET_PLAYING_TIME_TYPE);
            if (netLength != -1) {
              msg = mLocalizer.msg("netMinuted", "{0} min net", netLength);
              buffer.append(" - ").append(msg);
            }
            buffer.append(')');

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
                showHelpLinks, showPersonLinks);
          } else {
            addEntry(doc, buffer, prog, ProgramFieldType.SHORT_DESCRIPTION_TYPE,
                true, showHelpLinks,
                  showPersonLinks);
          }
        } else if (type == ProgramFieldType.INFO_TYPE) {
          int info = prog.getInfo();
          if ((info != -1) && (info != 0)) {
            buffer
                .append("<tr><td valign=\"top\" style=\"color:gray; font-size:");
  
            buffer.append(mBodyFontSize);
  
            buffer.append("\"><b>");
            buffer
                .append(type.getLocalizedName());
            buffer
                .append("</b></td><td valign=\"middle\" style=\"font-size:5\">");
  
            openPara(buffer, "info");
            // Workaround: Without the &nbsp; the component are not put in one
            // line.
            buffer.append("&nbsp;");
  
            int[] infoBitArr = ProgramInfoHelper.getInfoBits();
            Icon[] infoIconArr = ProgramInfoHelper.getInfoIcons();
            String[] infoMsgArr = ProgramInfoHelper.getInfoIconMessages();
  
            for (int i = 0; i < infoBitArr.length; i++) {
              if (ProgramInfoHelper.bitSet(info, infoBitArr[i])) {
                JLabel iconLabel;
                
                if (infoIconArr[i] != null) {  
                  iconLabel = new JLabel(infoIconArr[i]);
                }
                else {
                  iconLabel = new JLabel(infoMsgArr[i]);
                }
                
                iconLabel.setToolTipText(infoMsgArr[i]);
                buffer.append(doc.createCompTag(iconLabel));

                buffer.append("&nbsp;&nbsp;");
              }
            }
  
            closePara(buffer);
  
            buffer.append("</td></tr>");
            addSeparator(doc, buffer);
          }
        } else if (type == ProgramFieldType.URL_TYPE) {
          addEntry(doc, buffer, prog, ProgramFieldType.URL_TYPE, true,
              showHelpLinks, showPersonLinks);
        } else if (type == ProgramFieldType.ACTOR_LIST_TYPE) {
          ArrayList<String> knownNames = new ArrayList<String>();
          String[] recognizedActors = ProgramUtilities.getActorNames(prog);
          if (recognizedActors != null) {
            knownNames.addAll(Arrays.asList(recognizedActors));
          }
          String actorField = prog.getTextField(type);
          if (actorField != null) {
            ArrayList<String>[] lists = ProgramUtilities.splitActors(prog);
            if (lists == null) {
              lists = splitActorsSimple(prog);
            }
            if (lists != null && lists[0].size() > 0) {
              startInfoSection(buffer, type.getLocalizedName());
              buffer.append("<table border=\"0\" cellpadding=\"0\" style=\"font-family:");
              buffer.append(bodyFont);
              buffer.append(";\">");
              for (int i=0; i < lists[0].size(); i++) {
                String[] parts = new String[2];
                parts[0] = lists[0].get(i);
                parts[1] = "";
                if (i < lists[1].size()) {
                  parts[1] = lists[1].get(i);
                }
                int actorIndex = 0;
                if (showPersonLinks) {
                    if (knownNames.contains(parts[0])) {
                      parts[0] = addPersonLink(parts[0]);
                    } else if (knownNames.contains(parts[1])) {
                      parts[1] = addPersonLink(parts[1]);
                      actorIndex = 1;
                    }
                }
                buffer.append("<tr><td valign=\"top\">&#8226;&nbsp;</td><td valign=\"top\">");
                buffer.append(parts[actorIndex]);
                buffer.append("</td><td width=\"10\">&nbsp;</td>");

                if (parts[1-actorIndex].length() > 0) {
                  buffer.append("<td valign=\"top\">");
                  buffer.append(parts[1-actorIndex]);
                  buffer.append("</td>");
                } else {
                  // if roles are missing add next actor in the same line
                   if (i+1 < lists[0].size() && lists[1].size() == 0) {
                    i++;
                    buffer.append("<td valign=\"top\">&#8226;&nbsp;</td><td valign=\"top\">");
                    if (showPersonLinks) {
                        buffer.append(addSearchLink(lists[0].get(i)));
                      } else {
                        buffer.append(lists[0].get(i));
                      }
                    buffer.append("</td>");
                  }
                }
                buffer.append("</td></tr>");
              }
              buffer.append("</table>");
              buffer.append("</td></tr>");
              addSeparator(doc, buffer);
            }
            else {
              addEntry(doc, buffer, prog, type, showHelpLinks,
                    showPersonLinks);
            }
          }
        }
        else {
          addEntry(doc, buffer, prog, type, showHelpLinks, showPersonLinks);
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

  private static String addPersonLink(final String name) {
    if (name == null || name.isEmpty()) {
      return mLocalizer.msg("unknown", "(unknown)");
    }
    return addSearchLink(name);
  }

  private static ArrayList<String>[] splitActorsSimple(Program prog) {
    @SuppressWarnings("unchecked")
    ArrayList<String> list1 = new ArrayList();
    @SuppressWarnings("unchecked")
    ArrayList<String> list2 = new ArrayList();
    String actorField = prog.getTextField(ProgramFieldType.ACTOR_LIST_TYPE).trim();
    String[] actors;
    // don't try any parsing if newlines and commas are available
    // this must be recognized by the more advanced actors parsing
    if (actorField.contains("\n")) {
      if (actorField.contains(",")) {
        return null;
      }
      actors = actorField.split("\n");
    }
    else if (actorField.contains(",")) {
      actors = actorField.split(",");
    }
    else if (actorField.contains("\t")) {
      actors = new String[1];
      actors[0] = actorField;
    }
    else {
      return null;
    }
    for (String actor : actors) {
      actor = actor.trim();
      if (actor.length() > 0) {
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
        list1.add(part1);
        list2.add(part2);
      }
    }
    @SuppressWarnings("unchecked")
    ArrayList<String>[] result = new ArrayList[2];
    result[0] = list1;
    result[1] = list2;
    return result;
  }

  private static String addSearchLink(String topic, String displayText) {
    Color foreground = Settings.propProgramPanelForegroundColor.getColor();
    
    String style = " style=\"color:rgb("+ foreground.getRed() + "," + foreground.getGreen() + "," + foreground.getBlue() + "); border-bottom: 1px dashed;\"";
      StringBuilder buffer = new StringBuilder(32);
      buffer.append("<a href=\"");
      buffer.append(TVBROWSER_URL_PROTOCOL);
      buffer.append(topic.replaceAll("\"", "").replaceAll("'", ""));
      
      buffer.append("\" ");
      buffer.append(style);
      buffer.append('>');
      buffer.append(displayText);
      buffer.append("</a>");
      return buffer.toString();
  }

  private static String addSearchLink(String topic) {
    if (topic == null || topic.isEmpty()) {
      return "";
    }
    return addSearchLink(topic, topic);
  }
  
  private static void addEntry(ExtendedHTMLDocument doc, StringBuilder buffer,
      Program prog, ProgramFieldType fieldType, boolean showHelpLinks,
      boolean showPersonLinks) {
    addEntry(doc, buffer, prog, fieldType, false, showHelpLinks,
        showPersonLinks);
  }

  private static void addEntry(ExtendedHTMLDocument doc, StringBuilder buffer,
      Program prog, ProgramFieldType fieldType, boolean createLinks,
      boolean showHelpLinks, boolean showPersonLinks) {

    String text = null;
    String name = fieldType.getLocalizedName();
    int blank = name.indexOf(' ', 16);
    if (blank > 0) {
      name = name.substring(0, blank) + "<br>" + name.substring(blank +1);
    }
    if (fieldType.getFormat() == ProgramFieldType.TEXT_FORMAT) {
      text = prog.getTextField(fieldType);

      // Lazily add short description, but only if it differs from description
      if (fieldType == ProgramFieldType.DESCRIPTION_TYPE) {
        String description = prog.getDescription().trim();

        if (prog.getShortInfo() != null) {
          StringBuilder shortInfo = new StringBuilder(prog.getShortInfo()
              .trim());

          while (shortInfo.toString().endsWith(".")) {
            shortInfo.deleteCharAt(shortInfo.length() - 1);
          }

          if (!description.trim().startsWith(shortInfo.toString())) {
            addEntry(doc, buffer, prog,
                ProgramFieldType.SHORT_DESCRIPTION_TYPE, true, showHelpLinks);
          }
        }
        text = removeArtificialLineBreaks(text);
        text = HTMLTextHelper.convertTextToHtml(text, createLinks);
        // scan for moderation in beginning of description
        String[] lines = text.split("<br>");
        String[] tags = { "von und mit", "präsentiert von", "mit", "film von",
            "moderation", "zu gast" };
        for (int i = 0; i < 2; i++) {
          if (lines.length > i && lines[i].length() < 60) {
            String line = lines[i];
            for (String tag : tags) {
              if (line.toLowerCase().startsWith(tag)
                  || line.toLowerCase().startsWith(tag + ":")) {
                String persons = line.substring(tag.length(), line.length())
                    .trim();
                if (persons.startsWith(":")) {
                  persons = persons.substring(1).trim();
                }
                if (persons.endsWith(".")) {
                  persons = persons.substring(0, persons.length() - 1).trim();
                }
                for (String person : persons.split(" und ")) {
                  int partCount = person.split(" ").length;
                  if (partCount >= 2 && partCount < 4) {
                    text = text.replaceFirst(person, addSearchLink(person));
                  }
                }
              }
            }
          }
        }
      }
      
    } else if (fieldType.getFormat() == ProgramFieldType.TIME_FORMAT) {
      text = prog.getTimeFieldAsString(fieldType);
    } else if (fieldType.getFormat() == ProgramFieldType.INT_FORMAT) {
      if (fieldType == ProgramFieldType.RATING_TYPE) {
        int value = prog.getIntField(fieldType);
        if (value > -1) {
          text = new DecimalFormat("##.#").format((double)prog.getIntField(fieldType) / 10) + "/10";
        }
      } else {
        text = prog.getIntFieldAsString(fieldType);
      }
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

    // add person links
    if (ProgramFieldType.DIRECTOR_TYPE == fieldType
        || ProgramFieldType.SCRIPT_TYPE == fieldType
        || ProgramFieldType.CAMERA_TYPE == fieldType
        || ProgramFieldType.CUTTER_TYPE == fieldType
        || ProgramFieldType.MUSIC_TYPE == fieldType
        || ProgramFieldType.MODERATION_TYPE == fieldType
        || ProgramFieldType.ADDITIONAL_PERSONS_TYPE == fieldType
        || ProgramFieldType.PRODUCER_TYPE == fieldType) {
      if (showPersonLinks && text.length() < 200) {
        // if field is longer, this is probably not a list of names
        if (text.endsWith(".")) {
          text = text.substring(0, text.length() - 1);
        }
        String[] persons = splitPersons(text);
        for (int i = 0; i < persons.length; i++) {
          // remove duplicate entries
          boolean duplicate = false;
          if (i < persons.length - 1) {
            for (int j = i + 1; j < persons.length; j++) {
              if (persons[i].equalsIgnoreCase(persons[j])) {
                duplicate = true;
                break;
              }
            }
          }
          if (duplicate) {
            text = text.replaceFirst(Pattern.quote(persons[i]), "").trim();
            if (text.startsWith(",")) {
              text = text.substring(1).trim();
            }
            text = text.replaceAll(",\\s*,", ",");
            continue;
          }
          // a name shall not have more name parts
          if (persons[i].trim().split(" ").length <= 3) {
            String link;
            if (persons[i].contains("(")) {
              int index = persons[i].indexOf('(');
              String topic = persons[i].substring(0, index).trim();
              link = addSearchLink(topic) + " " + persons[i].substring(index).trim();
            } else {
              link = addSearchLink(persons[i]);
            }
            text = text.replace(persons[i], link);
          }
        }
      }
      buffer.append(text);
    }
    else {
      if (ProgramFieldType.DESCRIPTION_TYPE == fieldType) {
        buffer.append(text);
      } else {
        buffer.append(HTMLTextHelper.convertTextToHtml(text, createLinks));
      }
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

  private static String[] splitPersons(final String textField) {
    return ProgramUtilities.splitPersons(textField);
  }

  /**
   * remove line breaks from description texts which are formatted as block text
   * with lines up to around 80 characters
   * 
   * @param text
   * @return floating text
   */
  private static String removeArtificialLineBreaks(String text) {
    String[] lines = text.split("\n");
    if (lines.length > 5) {
      int avg = (text.length() - lines.length + 1) / lines.length;
      if (avg < 100 && avg > 50) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
          result.append(lines[i]);
          if (lines[i].length() < avg - 10) {
            result.append('\n');
          } else {
            result.append(' ');
          }
        }
        return result.toString();
      }
    }
    return text;
  }

  private static void startInfoSection(StringBuilder buffer, String section) {
    Color foreground = Settings.propProgramPanelForegroundColor.getColor();
    
    buffer.append("<tr><td valign=\"top\" style=\"color:#808080; font-size:");
    buffer.append(mBodyFontSize);
    buffer.append("\"><b>");
    buffer.append(section);
    buffer.append("</b></td><td style=\"color:rgb("+ foreground.getRed() + "," + foreground.getGreen() + "," + foreground.getBlue() + "); font-size:");
    buffer.append(mBodyFontSize);
    buffer.append("\">");
  }

  private static void addSeparator(ExtendedHTMLDocument doc,
      StringBuilder buffer) {
    buffer.append("<tr><td colspan=\"2\">");
    buffer.append("<div style=\"font-size:0;\">").append(
        doc.createCompTag(new HorizontalLine())).append("</div></td></tr>");
  }

  private static void openPara(StringBuilder buffer, String style) {
    buffer.append("<div id=\"").append(style).append("\">");
  }

  private static void closePara(StringBuilder buffer) {
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
        ProgramFieldType.ADDITIONAL_INFORMATION_TYPE,
        ProgramFieldType.RATING_TYPE,
        ProgramFieldType.ORIGIN_TYPE,
        ProgramFieldType.DIRECTOR_TYPE,
        ProgramFieldType.SCRIPT_TYPE,
        ProgramFieldType.ACTOR_LIST_TYPE,
        ProgramFieldType.MODERATION_TYPE,
        ProgramFieldType.MUSIC_TYPE,
        ProgramFieldType.PRODUCER_TYPE,
        ProgramFieldType.CAMERA_TYPE,
        ProgramFieldType.CUTTER_TYPE,
        ProgramFieldType.ADDITIONAL_PERSONS_TYPE,
        ProgramFieldType.URL_TYPE,
        ProgramFieldType.ORIGINAL_TITLE_TYPE,
        ProgramFieldType.ORIGINAL_EPISODE_TYPE,
        ProgramFieldType.PRODUCTION_COMPANY_TYPE,
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
