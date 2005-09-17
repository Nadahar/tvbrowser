package util.ui.html;

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
      text = text.replaceAll("(http://|www.)[^\\s<]*", "<a href=\"$0\">$0</a>");
    }

    return text;
  }
  
}
