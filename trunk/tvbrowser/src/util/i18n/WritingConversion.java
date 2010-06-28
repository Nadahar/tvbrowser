package util.i18n;

import java.text.Normalizer;

/**
 * @author Jo
 *
 */
public class WritingConversion {

  /**
   * Remove any diacritical marks (accents like ç, ñ, é, etc) from the given string.
   *
   * from http://balusc.blogspot.com/2006/10/stringutil.html
   * no copyright since it's too simple ;-)
   *
   * @param string The string to remove diacritical marks from.
   * @return The string with removed diacritical marks, if any.
   */
  public static String removeDiacriticalMarks(String string) {
    return Normalizer.normalize(string, Normalizer.Form.NFD)
    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }

  /**
   * Replaces non-ASCII latin derived letters with similar ASCII letters if available.<br />
   * (currently supported: ISO-8859-1 to -4, ISO-8859-9, -10, ISO 8859-13 to -16, Windows-1252, MacRoman)
   * @param string The string to replace non-ASCII letters.
   * @return The string with all supported non-ASCII letters replaced.
   */
  public static String replaceLatinDerivedLetters(String string) {
    String tmpval = removeDiacriticalMarks(string);
    String retval = "";
    for (char c: tmpval.toCharArray()) {
      switch (c) {
      case '\u00df': retval += "ss"; break;
      case '\u00d8': retval += "O"; break;
      case '\u00f8': retval += "o"; break;
      case '\u00c6': retval += "Ae"; break;
      case '\u00e6': retval += "ae"; break;
      case '\u00d0': retval += "D"; break;
      case '\u00f0': retval += "d"; break;
      case '\u00de': retval += "Th"; break;
      case '\u00fe': retval += "th"; break;
      case '\u0152': retval += "Oe"; break;
      case '\u0153': retval += "oe"; break;
      case '\u0110': retval += "D"; break;
      case '\u0111': retval += "d"; break;
      case '\u0141': retval += "L"; break;
      case '\u0142': retval += "l"; break;
      case '\u0131': retval += "i"; break;
      case '\u0130': retval += "I"; break;
      case '\u0127': retval += "h"; break;
      case '\u0126': retval += "H"; break;
      case '\u014a': retval += "N"; break;
      case '\u014b': retval += "n"; break;
      case '\u0166': retval += "T"; break;
      case '\u0167': retval += "t"; break;
      case '\u0138': retval += "k"; break;
      case '\u01b7': retval += "Z"; break;
      case '\u0292': retval += "z"; break;
      case '\u01ee': retval += "Z"; break;
      case '\u01ef': retval += "z"; break;
      case '\u018f': retval += "E"; break;
      case '\u0259': retval += "e"; break;
      case '\u01e4': retval += "G"; break;
      case '\u01e5': retval += "g"; break;
      case '\u0132': retval += "IJ"; break;
      case '\u0133': retval += "ij"; break;
      default: retval += c;
      }
    }
    return retval;
  }

  /**
   * Replace punctuation (!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~¿¡
   * dashes and quotation marks) with a given string
   * @param string The string to replace punctuation from.
   * @param replaceString The string to replace the punctuation with
   * @return The string with removed punctuation, if any.
   */
  public static String replacePunctuation(String string, String replaceString) {
    return string.replaceAll("(\\p{Punct}|\u00bf|\u00a1|\u2012|\u2013|\u2014|\u2015|\u2018|\u2019|\u201A|\u201B|\u201C|\u201D|\u201E|\u201F|\u00AB|\u00BB)+", replaceString);
  }

  /**
   * Replace whitespaces with a single space
   * @param string The string to replace whitespaces from.
   * @return The string with replaced whitespaces, if any.
   */
  public static String removeRedundantWhitespaces(String string) {
    return string.replaceAll("\\s+", " ").trim();
  }

  /**
   * Reduces String to ASCII letters by removing punctuation and redundant whitespaces
   * and replacing non-ASCII letters with similar ASCII letters if available.<br />
   * (currently supported: ISO-8859-1 to -4, ISO-8859-9, -10, ISO 8859-13 to -16, Windows-1252, MacRoman)
   * @param string The string to reduce.
   * @param replaceUnsupportedChars If true, all characters that can't be replaced with
   * ASCII letters will be replaced with an underscore.
   * @return The reduced string.
   */
  public static String reduceToASCIILetters(String string, boolean replaceUnsupportedChars) {
    String retval = string;
    retval = replacePunctuation(retval, " ");
    retval = removeRedundantWhitespaces(retval);
    retval = replaceLatinDerivedLetters(retval);

    if (replaceUnsupportedChars) {
      retval = retval.replaceAll("[^a-zA-Z0-9 ]", "_");
    }
    return retval;
  }

}