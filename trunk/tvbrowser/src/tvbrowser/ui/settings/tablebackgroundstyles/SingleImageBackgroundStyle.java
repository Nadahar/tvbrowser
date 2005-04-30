package tvbrowser.ui.settings.tablebackgroundstyles;

import util.ui.TabLayout;

import javax.swing.*;

import tvbrowser.core.Settings;
import tvbrowser.ui.settings.ProgramTableSettingsTab;


/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 30.04.2005
 * Time: 17:47:57
 */
public class SingleImageBackgroundStyle implements TableBackgroundStyle {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SingleImageBackgroundStyle.class);

  private JTextField mOneImageBackgroundTF;

  private JPanel mContent;


  public SingleImageBackgroundStyle() {

  }

  public boolean hasContent() {
    return true;
  }

  public JPanel createSettingsContent() {
    mContent = new JPanel(new TabLayout(3));

    mContent.add(new JLabel(mLocalizer.msg("oneImage.image", "Image")));
    mOneImageBackgroundTF = new JTextField(Settings.propOneImageBackground.getString(), 25);
    mContent.add(mOneImageBackgroundTF);
    mContent.add(ProgramTableSettingsTab.createBrowseButton(mContent, mOneImageBackgroundTF));


    return mContent;
  }

  public void storeSettings() {
    if (mContent != null) {
      Settings.propOneImageBackground.setString(mOneImageBackgroundTF.getText());
    }
  }

  public String getName() {
    return mLocalizer.msg("style","One image");
  }


  public String toString() {
    return getName();
  }

  public String getSettingsString() {
    return "oneImage";
  }

}
