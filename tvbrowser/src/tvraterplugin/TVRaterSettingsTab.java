/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package tvraterplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import util.io.IOUtilities;
import util.ui.BrowserLauncher;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.TabLayout;
import devplugin.SettingsTab;

/**
 * This class contains the Settings-Tab to configurate the plugin
 * @author bodo tasche
 */
public class TVRaterSettingsTab implements SettingsTab {
	private static final Localizer mLocalizer = Localizer.getLocalizerFor(TVRaterSettingsTab.class);
	private Properties _settings;

	private JTextField _name;
	private JPasswordField _password;
//	private JCheckBox _includeFav;
	private JCheckBox _ownRating;
	private JComboBox _updateTime;

	/**
	 * @param settings
	 */
	public TVRaterSettingsTab(Properties settings) {
		_settings = settings;
	}

	/* (non-Javadoc)
	 * @see devplugin.SettingsTab#createSettingsPanel()
	 */
	public JPanel createSettingsPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel main = new JPanel(new TabLayout(1));

		JPanel user = new JPanel(new GridBagLayout());

		// Account Settings
		user.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("accountsetting", "Account settings")));

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;

		c.weightx = 0;
		c.insets = new Insets(5, 0, 0, 5);
		c.gridwidth = GridBagConstraints.RELATIVE;
		JLabel name = new JLabel(mLocalizer.msg("name", "Name") + ":");
		user.add(name, c);

		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 0, 0, 3);
		_name = new JTextField(_settings.getProperty("name","noname"));
		user.add(_name, c);

		c.weightx = 0;
		c.insets = new Insets(5, 0, 0, 5);
		c.gridwidth = GridBagConstraints.RELATIVE;
		user.add(new JLabel(mLocalizer.msg("password", "Password") + ":"), c);

		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 0, 0, 3);
		_password = new JPasswordField(IOUtilities.xorEncode(_settings.getProperty("password",""), 21));
		user.add(_password, c);

		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0;
		c.insets = new Insets(0, 0, 0, 0);
		user.add(new JPanel(), c);

		JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));

		JButton newAccount = new JButton(mLocalizer.msg("newAccount", "Create new Account"));

		newAccount.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BrowserLauncher.openURL("http://tvaddicted.wannawork.de/index.php?Page=newuser");
			}
		});

		buttonpanel.add(newAccount);

		JButton lostPassword = new JButton(mLocalizer.msg("lostPassword", "Lost Password?"));

		lostPassword.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BrowserLauncher.openURL("http://tvaddicted.wannawork.de/index.php?Page=lostpasswd");
			}
		});

		buttonpanel.add(lostPassword);

		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		user.add(buttonpanel, c);

		main.add(user);

	/*	_includeFav =
			new JCheckBox(
				mLocalizer.msg("includeFavorites", "Include Favorites into rating"),
				_settings.getProperty("includeFavorites", "true").equals("true"));
		main.add(_includeFav);
*/
		_ownRating =
			new JCheckBox(
				mLocalizer.msg("ownRating", "Use own rating if available"),
				_settings.getProperty("ownRating", "true").equals("true"));
		main.add(_ownRating);


		String[] updateStrings =
			{
				mLocalizer.msg("update", "only when updating TV Data"),
				mLocalizer.msg("everyTime", "every Time a rating is made"),
				mLocalizer.msg("eachStart", "at each start of TV Browser"),
				mLocalizer.msg("manual", "manual Update"),};

		_updateTime = new JComboBox(updateStrings);

		_updateTime.setSelectedIndex(Integer.parseInt(_settings.getProperty("updateIntervall", "4")));

		JPanel updatePanel = new JPanel(new BorderLayout(5, 0));
		updatePanel.add(new JLabel(mLocalizer.msg("transmit", "Transmit data")), BorderLayout.WEST);
		updatePanel.add(_updateTime, BorderLayout.CENTER);

			
		main.add(updatePanel);
		
		panel.add(main, BorderLayout.CENTER);

        JLabel urlLabel = new JLabel("<html><u>http://tvaddicted.wannawork.de</u></html>", JLabel.CENTER);
        urlLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        urlLabel.setForeground(Color.BLUE);
        urlLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                BrowserLauncher.openURL("http://tvaddicted.wannawork.de");
            }
        });
    	urlLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        
        JPanel urlPanel = new JPanel(new BorderLayout());
        
        if (!Locale.getDefault().equals(Locale.GERMANY)) {
        	JTextArea help = new JTextArea("Hi!\nThis plugin is not 100% translated.\nIf you want to help, please contact me via the HP.\nThanks!");
        	help.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        	help.setEditable(false);
        	help.setForeground(name.getForeground());
        	help.setBackground(name.getBackground());
        	urlPanel.add(help, BorderLayout.CENTER);
            urlPanel.add(urlLabel, BorderLayout.SOUTH);
        } else {
            urlPanel.add(urlLabel, BorderLayout.CENTER);
        }
        
        
        panel.add(urlPanel, BorderLayout.SOUTH);

		
		return panel;
	}

	/* (non-Javadoc)
	 * @see devplugin.SettingsTab#saveSettings()
	 */
	public void saveSettings() {
		_settings.setProperty("name", _name.getText());
		_settings.setProperty("password", IOUtilities.xorEncode(new String(_password.getPassword()), 21));
/*		if (_includeFav.isSelected()) {
			_settings.setProperty("includeFavorites", "true");
		} else {
			_settings.setProperty("includeFavorites", "false");
		}
*/
		if (_ownRating.isSelected()) {
			_settings.setProperty("ownRating", "true");
		} else {
			_settings.setProperty("ownRating", "false");
		}

		_settings.setProperty("updateIntervall", Integer.toString(_updateTime.getSelectedIndex()));

	}

	/* (non-Javadoc)
	 * @see devplugin.SettingsTab#getIcon()
	 */
	public Icon getIcon() {
		String iconName = "tvraterplugin/tvrater.gif";
		return ImageUtilities.createImageIconFromJar(iconName, getClass());
	}

	/* (non-Javadoc)
	 * @see devplugin.SettingsTab#getTitle()
	 */
	public String getTitle() {
		return mLocalizer.msg("tabName", "TV Rater");
	}
}