package util.i18n;

/**
 * @author Jo
 *
 */
public class WritingConversion {

  /**
   * Remove any diacritical marks (accents like ç, ñ, é, etc) from the given string.
   * @param string The string to remove diacritical marks from.
   * @return The string with removed diacritical marks, if any.
   */
  // from http://balusc.blogspot.com/2006/10/stringutil.html
  // no copyright since it's too simple ;-)
  public static String removeDiacriticalMarks(String string) {
    return java.text.Normalizer.normalize(string, java.text.Normalizer.Form.NFD)
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
      case 'ß': retval += "ss"; break;
      case 'Ø': retval += "O"; break;
      case 'ø': retval += "o"; break;
      case 'Æ': retval += "Ae"; break;
      case 'æ': retval += "ae"; break;
      case 'Ð': retval += "D"; break;
      case 'ð': retval += "d"; break;
      case 'Þ': retval += "Th"; break;
      case 'þ': retval += "th"; break;     
      case 'Œ': retval += "Oe"; break;     
      case 'œ': retval += "oe"; break;     
      case 'Đ': retval += "D"; break;     
      case 'đ': retval += "d"; break;     
      case 'Ł': retval += "L"; break;     
      case 'ł': retval += "l"; break;     
      case 'ı': retval += "i"; break;     
      case 'İ': retval += "I"; break;     
      case 'ħ': retval += "h"; break;     
      case 'Ħ': retval += "H"; break;     
      case 'Ŋ': retval += "N"; break;     
      case 'ŋ': retval += "n"; break;     
      case 'Ŧ': retval += "T"; break;     
      case 'ŧ': retval += "t"; break;     
      case 'ĸ': retval += "k"; break;     
      case 'Ʒ': retval += "Z"; break;     
      case 'ʒ': retval += "z"; break;     
      case 'Ǯ': retval += "Z"; break;     
      case 'ǯ': retval += "z"; break;     
      case 'Ə': retval += "E"; break;     
      case 'ə': retval += "e"; break;     
      case 'Ǥ': retval += "G"; break;     
      case 'ǥ': retval += "g"; break;     
      case 'Ĳ': retval += "IJ"; break;     
      case 'ĳ': retval += "ij"; break;     
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
    return string.replaceAll("(\\p{Punct}|¿|¡|\u2012|\u2013|\u2014|\u2015|\u2018|\u2019|\u201A|\u201B|\u201C|\u201D|\u201E|\u201F|\u00AB|\u00BB)+", replaceString);
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