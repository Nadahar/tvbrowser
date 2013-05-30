package switchplugin;

import devplugin.Program;
import util.paramhandler.ParamLibrary;

/**
 * The parameter library for the SwitchPlugin.
 * 
 * @author René Mach
 * 
 */
public class SwitchParamLibrary extends ParamLibrary {

  public String getDescriptionForKey(String key) {
    if (key.compareToIgnoreCase("channel_name_external") == 0)
      return SwitchPlugin.mLocalizer.msg("externDesc", "External channel name");
    
    return super.getDescriptionForKey(key);
  }

  public String[] getPossibleKeys() {
    String[] additionalKeys = { "channel_name_external" };

    return concat(super.getPossibleKeys(), additionalKeys);
  }

  public String getStringForKey(Program p, String key) {

    if (key.compareToIgnoreCase("channel_name_external") == 0)
      return SwitchPlugin.getInstance().getExternalNameFor(
          p.getChannel().getName());

    return super.getStringForKey(p, key);
  }

  /**
   * Methode from CaptureParamLibrary by bodum.
   * 
   * Concats two String-Arrays
   * 
   * @param ar1
   *          Array One
   * @param ar2
   *          Array Two
   * @return concated Version of the two Arrays
   */
  private String[] concat(String[] ar1, String[] ar2) {
    String[] ar3 = new String[ar1.length + ar2.length];
    System.arraycopy(ar1, 0, ar3, 0, ar1.length);
    System.arraycopy(ar2, 0, ar3, ar1.length, ar2.length);
    return ar3;
  }
}
