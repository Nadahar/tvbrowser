package compat;

public final class StringUtils {
  public static boolean isEmpty(final String value) {
    return value != null && !value.equals("");
  }
  
  public static boolean isBlank(final String value) {
    return value != null && !value.trim().equals("");
  }
  
  public static String removeEnd(String haystack, final String needle) {
    if(haystack != null && needle != null && needle.length() > 0 && haystack.endsWith(needle)) {
      haystack = haystack.substring(0, haystack.length()-needle.length());
    }
    
    return haystack;
  }
}
