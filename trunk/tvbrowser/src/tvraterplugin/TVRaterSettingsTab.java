/*
 * Created on 02.12.2003
 */
package tvraterplugin;

import javax.swing.Icon;
import javax.swing.JPanel;

import devplugin.SettingsTab;

/**
 * @author bodo
 */
public class TVRaterSettingsTab implements SettingsTab {

	/* (non-Javadoc)
	 * @see devplugin.SettingsTab#createSettingsPanel()
	 */
	public JPanel createSettingsPanel() {
		// TODO Auto-generated method stub
		return new JPanel();
	}

	/* (non-Javadoc)
	 * @see devplugin.SettingsTab#saveSettings()
	 */
	public void saveSettings() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see devplugin.SettingsTab#getIcon()
	 */
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see devplugin.SettingsTab#getTitle()
	 */
	public String getTitle() {
		return "TV Rate";
	}

}
