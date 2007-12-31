package dreamboxdataservice;

import tvdataservice.SettingsPanel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.factories.Borders;

import javax.swing.*;
import java.util.Properties;

import util.ui.Localizer;
import util.io.IOUtilities;

public class DreamboxSettingsPanel extends SettingsPanel {
    /**
     * Translator
     */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DreamboxSettingsPanel.class);
    public final static int PASSWORDSEED = 1231945;

    private JTextField mDreamAddress;
    private Properties mProperties;
    private JTextField mUsername;
    private JPasswordField mPassword;


    public DreamboxSettingsPanel(Properties properties) {
        mProperties = properties;
        createGui();
    }

    private void createGui() {
        setLayout(new FormLayout("pref, 3dlu, fill:pref:grow", "pref, 3dlu, pref, 3dlu, pref"));
        setBorder(Borders.DLU4_BORDER);
        CellConstraints cc = new CellConstraints();

        add(new JLabel(mLocalizer.msg("ip", "IP of the Dreambox") + ":"), cc.xy(1, 1));

        mDreamAddress = new JTextField();
        mDreamAddress.setText(mProperties.getProperty("ip", ""));
        add(mDreamAddress, cc.xy(3, 1));

        add(new JLabel(mLocalizer.msg("user", "Username") + ":"), cc.xy(1, 3));
        mUsername = new JTextField();
        mUsername.setText(mProperties.getProperty("username", ""));
        add(mUsername, cc.xy(3, 3));

        add(new JLabel(mLocalizer.msg("password", "Password") + ":"), cc.xy(1, 5));
        mPassword = new JPasswordField();
        mPassword.setText(IOUtilities.xorDecode(mProperties.getProperty("password", ""), PASSWORDSEED));
        add(mPassword, cc.xy(3, 5));
    }

    public void ok() {
        mProperties.setProperty("ip", mDreamAddress.getText());
        mProperties.setProperty("username", mUsername.getText());
        mProperties.setProperty("password", IOUtilities.xorEncode(new String(mPassword.getPassword()), PASSWORDSEED));
    }
}
