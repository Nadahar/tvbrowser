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
   * Convert Text to HTML. > and < will be converted to &gt; and &lt;
   * \n will be <code>&lt;br&gt;</code>
   * 
   * Links in the text will be made clickable.
   * 
   * @param text text to display
   * @return Result
   * @since 2.7
   */
  public static String convertTextToHtml(String text) {
    return convertTextToHtml(text, true);
  }

  /**
   * Convert Text to HTML. > and < will be converted to &gt; and &lt;
   * \n will be <code>&lt;br&gt;</code>
   * 
   * If createLinks is true, it will try to find links and make them clickable
   * 
   * @param text text to display
   * @param createLinks if true, it will create links
   * @return Result
   */
  public static String convertTextToHtml(String text, boolean createLinks) {
    // remove Javascript

    text = text.replaceAll("(?i)<script.*?(>.*?</script>|/>)", "");

    // Disarm HTML entities
    text = IOUtilities.replace(text.trim(), "<", "&lt;");
    text = IOUtilities.replace(text.trim(), ">", "&gt;");

    // Translate line breaks to HTML breaks
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
        if (linkText.endsWith("/")) { // remove trailing slash from display
          linkText = linkText.substring(0, linkText.length() - 1);
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
   * Replaces HTML German umlauts and the HTML formatting tags with a Java String.
   * 
   * @param html The HTML text to replace.
   * @return The text with the replaced Strings.
   * @since 2.7
   */
  public static String convertHtmlToText(String html) {
    html = html.trim();
    html = IOUtilities.replace(html, "&auml;", "\u00e4");
    html = IOUtilities.replace(html, "&Auml;", "\u00c4");

    html = IOUtilities.replace(html, "&ouml;", "\u00f6");
    html = IOUtilities.replace(html, "&Ouml;", "\u00d6");
    
    html = IOUtilities.replace(html, "&uuml;", "\u00fc");
    html = IOUtilities.replace(html, "&Uuml;", "\u00dc");
    
    html = IOUtilities.replace(html, "&szlig;", "\u00df");
    
    html = IOUtilities.replace(html, "&eacute;", "\u00E9");

    html = IOUtilities.replace(html, "<br>", "\n");
    
    html = IOUtilities.replace(html.trim(), "<i>", "");
    html = IOUtilities.replace(html.trim(), "</i>", "");
    
    html = IOUtilities.replace(html.trim(), "<b>", "");
    html = IOUtilities.replace(html.trim(), "</b>", "");
    
    html = IOUtilities.replace(html.trim(), "<u>", "");
    html = IOUtilities.replace(html.trim(), "</u>", "");
    
    html = html.trim();
    
    html = IOUtilities.replace(html, "&quot;", "\"");
    html = IOUtilities.replace(html, "\\'", "'");
    
    // &amp; must be last, otherwise new wrong encodings may be created
    html = IOUtilities.replace(html, "&amp;", "&");

    return html;
  }
  
}
