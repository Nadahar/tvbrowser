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

public class WidgetPlugin extends Plugin implements IWidgetSettings {

	private static final Version PLUGIN_VERSION = new Version(2, 70, false);

	/**
	 * Localizer
	 */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(WidgetPlugin.class);

	private static ImageIcon mIcon;

  /**
   * encapsulation of the HTTP response calculation
   */
  private WidgetServer server;

  private Properties mSettings;
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
			server = new WidgetServer(getPortNumber());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int getPortNumber() {
		return Integer.valueOf(mSettings.getProperty(SETTING_PORT_NUMBER, IWidgetSettings.SETTING_PORT_NUMBER_DEFAULT));
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
    mSettings = settings;
    if (mSettings == null) {
      mSettings = new Properties();
    }
	}

	@Override
	public Properties storeSettings() {
		return mSettings;
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
