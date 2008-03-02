package util.ui.html;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.io.IOUtilities;

/**
 * This is a Helper-Class for converting Text to HMTL
 * 
 * @author bodum
 * @since 2.1
 */
public class HTMLTextHelper {

  /**
   * Convert Text to HTML. > and < will be convertet to &lt; and &gt;
   * \n will be &gt;br&lt;
   * 
   * If createLinks is true, it will try to find Links and create a href-Elements
   * 
   * @param text Text-Files
   * @param createLinks if true, it will create Links
   * @return Result
   */
  public static String convertTextToHtml(String text, boolean createLinks) {
    // Disarm html entities
    text = IOUtilities.replace(text.trim(), "<", "&lt;");
    text = IOUtilities.replace(text.trim(), ">", "&gt;");

    // Translate line breaks to html breaks
    text = IOUtilities.replace(text.trim(), "\n", "<br>");

    // Create links for URLs
    if (createLinks) {
      
      Matcher matcher = Pattern.compile("(http://|www\\.)[^\\s<]*").matcher(text);
      
      StringBuffer result = new StringBuffer();
      
      int end = 0;
      
      while (matcher.find()) {
        result.append(text.substring(end, matcher.start()));
        end = matcher.end();
        result.append("<a href=\"");
        
        String linkText = text.substring(matcher.start(), matcher.end());
        
        // remove trailing characters which are not part of the URL
        while (linkText.endsWith(".")) {
          linkText = linkText.substring(0, linkText.length() - 1);
          end--;
        }
      
        if (!linkText.startsWith("http://")) {
          result.append("http://");
        }
        
        result.append(linkText);
        result.append("\">");
        if (linkText.startsWith("http://")) {
          linkText = linkText.substring(7); // remove http:// from shown link text
        }
        result.append(linkText.length() > 40 ? linkText.substring(0,40) + "..." : linkText);
        result.append("</a>");
      }

      result.append(text.substring(end));
      
      text = result.toString();
    }

    return text;
  }
  
  /**
   * Replaces html german Umlaute and the html formating tags with a Java String.
   * 
   * @param html The html text to replace.
   * @return The text with the replaced Strings.
   * @since 2.7
   */
  public static String convertHtmlToText(String html) {
    html = IOUtilities.replace(html.trim(), "&auml;", "\u00e4");
    html = IOUtilities.replace(html.trim(), "&Auml;", "\u00c4");

    html = IOUtilities.replace(html.trim(), "&ouml;", "\u00f6");
    html = IOUtilities.replace(html.trim(), "&Ouml;", "\u00d6");
    
    html = IOUtilities.replace(html.trim(), "&uuml;", "\u00fc");
    html = IOUtilities.replace(html.trim(), "&Uuml;", "\u00dc");
    
    html = IOUtilities.replace(html.trim(), "&szlig;", "\u00df");
    
    html = IOUtilities.replace(html.trim(), "<br>", "\n");
    
    html = IOUtilities.replace(html.trim(), "<i>", "");
    html = IOUtilities.replace(html.trim(), "</i>", "");
    
    html = IOUtilities.replace(html.trim(), "<b>", "");
    html = IOUtilities.replace(html.trim(), "</b>", "");
    
    html = IOUtilities.replace(html.trim(), "<u>", "");
    html = IOUtilities.replace(html.trim(), "</u>", "");
    
    html = IOUtilities.replace(html.trim(), "&quot;", "\"");
    html = IOUtilities.replace(html.trim(), "\\'", "'");
    
    return html;
  }
  
}
