package widgetplugin;

import java.io.IOException;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import util.ui.Localizer;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;

final public class WidgetPlugin extends Plugin {

	private static final Version PLUGIN_VERSION = new Version(2, 70, 0, true);

	/**
	 * Localizer
	 */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(WidgetPlugin.class);

	private static ImageIcon mIcon;

  /**
   * encapsulation of the HTTP response calculation
   */
  private WidgetServer server;

  private WidgetSettings mSettings;
	private boolean mStartFinished;

  private static WidgetPlugin mInstance;

	public WidgetPlugin() {
		mInstance = this;
	}

  public static Version getVersion() {
		return PLUGIN_VERSION;
	}

	@Override
	public PluginInfo getInfo() {
		return new PluginInfo(WidgetPlugin.class, "Widgets", mLocalizer.msg(
				"description", "feeds widgets with the current TV program"),
				"Michael Keppler", "GPL 3");
	}

	@Override
	public void onActivation() {
		// only start the server if this is a manual plugin activation
		// otherwise the server will be started by the handleStartFinished callback
		if (mStartFinished) {
			startServer();
		}
	}

	@Override
	public void onDeactivation() {
		stopServer();
	}

	private void startServer() {
		try {
			server = new WidgetServer(mSettings);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void stopServer() {
		server = null;
	}

	protected void restartServer() {
		stopServer();
		startServer();
	}

	@Override
	public void loadSettings(Properties settings) {
    if (settings == null) {
      settings = new Properties();
    }
    mSettings = new WidgetSettings(settings);
	}

	@Override
	public Properties storeSettings() {
		return mSettings.storeSettings();
	}

	@Override
	public SettingsTab getSettingsTab() {
		return new WidgetSettingsTab(this, mSettings);
	}

	@Override
	public void handleTvBrowserStartFinished() {
		startServer();
		mStartFinished = true;
	}

	public static WidgetPlugin getInstance() {
		return mInstance;
	}

	public Icon getPluginIcon() {
		if (mIcon == null) {
			mIcon = new ImageIcon(getClass().getResource("widget.png"));
		}
		return mIcon;
	}

}
