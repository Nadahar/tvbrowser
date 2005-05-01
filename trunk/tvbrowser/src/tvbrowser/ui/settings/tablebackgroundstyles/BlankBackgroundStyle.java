package tvbrowser.ui.settings.tablebackgroundstyles;

import javax.swing.*;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 30.04.2005
 * Time: 17:47:39
 */
public class BlankBackgroundStyle implements TableBackgroundStyle {

   private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(BlankBackgroundStyle.class);

  public BlankBackgroundStyle() {

  }

  public boolean hasContent() {
    return false;
  }

  public JPanel createSettingsContent() {
    return null;
  }

  public void storeSettings() {

  }

  public String getName() {
    return mLocalizer.msg("style","White");
  }


  public String toString() {
    return getName();
  }

  public String getSettingsString() {
    return "white";
  }

}
