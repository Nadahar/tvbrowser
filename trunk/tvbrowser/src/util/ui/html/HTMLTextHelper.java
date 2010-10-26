package util.ui.html;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.util.Translate;

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

    text = IOUtilities.replace(text.trim(), "<", "&lt;");
    text = IOUtilities.replace(text.trim(), ">", "&gt;");

    // Translate line breaks to HTML breaks
    text = IOUtilities.replace(text.trim(), "\n", "<br>");

    // Create links for URLs
    if (createLinks) {

      Matcher matcher = Pattern.compile("(http[s]?://|www\\.)[^\\s<\"']*").matcher(text);

      StringBuilder result = new StringBuilder();

      int end = 0;

      while (matcher.find()) {
        result.append(text.substring(end, matcher.start()));
        end = matcher.end();

        String linkText = text.substring(matcher.start(), matcher.end());

        // remove brackets
        if (linkText.endsWith(")") && result.length() > 0 && result.charAt(result.length() - 1) == '(') {
          linkText = linkText.substring(0, linkText.length() - 1);
          end--;
        }

        // remove trailing characters which are not part of the URL
        while (linkText.endsWith(".")) {
          linkText = linkText.substring(0, linkText.length() - 1);
          end--;
        }

        result.append("<a href=\"");
        if (!linkText.toLowerCase().startsWith("http://") && !linkText.toLowerCase().startsWith("https://")) {
          result.append("http://");
        }

        result.append(linkText);
        result.append("\">");
        if (linkText.startsWith("http://")) {
          linkText = linkText.substring("http://".length()); // remove http:// from shown link text
        }
        if (linkText.startsWith("https://")) {
          linkText = linkText.substring("https://".length()); // remove https:// from shown link text
        }
        if (linkText.endsWith("/")) { // remove trailing slash from display
          linkText = linkText.substring(0, linkText.length() - "/".length());
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
    if (html == null) {
      return null;
    }
    String temp = Translate.decode(html);

    temp = temp.replace("<br>","\n");

    return temp;
  }

}
