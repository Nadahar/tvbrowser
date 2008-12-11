package recommendationplugin;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;
import devplugin.ActionMenu;
import devplugin.Program;
import util.ui.Localizer;
import util.ui.UiUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JDialog;
import java.util.Properties;
import java.awt.event.ActionEvent;
import java.awt.Frame;
import java.awt.Window;

public class RecommendationPlugin extends Plugin {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(RecommendationPlugin.class);
  protected static RecommendationPlugin mInstance;
  private Icon mIcon;

  public static Version getVersion() {
    return new Version(0, 1, false);
  }

  /**
   * Creates an instance of this plugin.
   */
  public RecommendationPlugin() {
    mInstance = this;
  }

  public PluginInfo getInfo() {
    return new PluginInfo(RecommendationPlugin.class, mLocalizer.msg("pluginName", "Recommendation Plugin"),
        mLocalizer.msg("description", "Shows recommendation based on data from different sources"),
        "Bodo Tasche", "GPL");
  }

  public Icon getPluginIcon() {
    if (mIcon == null) {
      mIcon = new ImageIcon(getClass().getResource("recommendation.png"));
    }
    return mIcon;
  }

  public static RecommendationPlugin getInstance() {
    return mInstance;
  }

}