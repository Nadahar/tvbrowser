package widgetplugin;

import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import util.ui.Localizer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;

public class WidgetSettingsTab implements SettingsTab, IWidgetSettings {

	private static final Localizer mLocalizer = Localizer
			.getLocalizerFor(WidgetSettingsTab.class);

	private WidgetPlugin mPlugin;

	private JSpinner mSpinner;

	private Properties mSettings;

	private JCheckBox mRefresh;

	protected WidgetSettingsTab(WidgetPlugin plugin, Properties settings) {
		super();
		mPlugin = plugin;
		mSettings = settings;
	}

	public JPanel createSettingsPanel() {
		int currentRow = 1;
		final FormLayout layout = new FormLayout(
				"5dlu, pref, 3dlu, pref, 3dlu, pref, fill:default:grow",
				"");
		PanelBuilder panelBuilder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();

		// settings
		layout.appendRow(RowSpec.decode("pref"));
		layout.appendRow(RowSpec.decode("5dlu"));
		layout.appendRow(RowSpec.decode("pref"));
		layout.appendRow(RowSpec.decode("5dlu"));

		// port
		JLabel label = new JLabel(mLocalizer.msg("portNumber", "Port number"));
		panelBuilder.add(label, cc.xy(2, currentRow));

		SpinnerNumberModel model = new SpinnerNumberModel(34567, 1, 65535, 1);
		mSpinner = new JSpinner(model);
		mSpinner.setValue(Integer
				.valueOf(mSettings.getProperty(SETTING_PORT_NUMBER, SETTING_PORT_NUMBER_DEFAULT)));
		panelBuilder.add(mSpinner, cc.xy(4, currentRow));

		// refresh
		mRefresh = new JCheckBox(mLocalizer.msg("refresh", "Automatic refresh"));
		panelBuilder.add(mRefresh, cc.xy(2, (currentRow += 2)));
		
		// layout settings
		layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("2dlu"));
		layout.appendRow(RowSpec.decode("fill:default:grow"));

		// layout
		panelBuilder.addSeparator(mLocalizer.msg("layout",
				"Layout"), cc.xyw(1,
				(currentRow += 2), 5));

		return panelBuilder.getPanel();
	}

	public Icon getIcon() {
		return WidgetPlugin.getInstance().getPluginIcon();
	}

	public String getTitle() {
		return mLocalizer.msg("title", "Widgets");
	}

	public void saveSettings() {
		mSettings.setProperty(SETTING_PORT_NUMBER, mSpinner.getValue().toString());
		mPlugin.storeSettings();
		mPlugin.restartServer();
	}

}