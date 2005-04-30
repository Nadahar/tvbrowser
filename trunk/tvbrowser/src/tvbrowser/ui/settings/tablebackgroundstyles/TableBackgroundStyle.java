package tvbrowser.ui.settings.tablebackgroundstyles;

import javax.swing.*;


public interface TableBackgroundStyle {

  public boolean hasContent();

  public JPanel createSettingsContent();

  public void storeSettings();

  public String getName();

  public String getSettingsString();

}
