/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2007-09-20 23:45:38 +0200 (Do, 20 Sep 2007) $
 *   $Author: bananeweizen $
 * $Revision: 3894 $
 */
package schedulesdirectdataservice;

import java.util.Properties;

import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import tvdataservice.SettingsPanel;
import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class SchedulesDirectSettingsPanel extends SettingsPanel {
    /**
     * Translator
     */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(SchedulesDirectSettingsPanel.class);
    protected final static int PASSWORDSEED = 1231945;

    private Properties mProperties;
    private JTextField mUsername;
    private JPasswordField mPassword;

    private String mOldPassword;
    private String mOldUserName;


    public SchedulesDirectSettingsPanel(Properties properties,boolean showSeparator) {
        mProperties = properties;

        mOldUserName = mProperties.getProperty("username", "");
        mOldPassword = mProperties.getProperty("password", "");

        createGui(showSeparator);
    }

    private void createGui(boolean showSeparator) {
      FormLayout layout = new FormLayout("5dlu, default, 3dlu, fill:default:grow, 5dlu", "5dlu, default");
      PanelBuilder pb = new PanelBuilder(layout,this);
        
      CellConstraints cc = new CellConstraints();
      
      pb.add(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("info","<a href=\"http://www.schedulesdirect.org/\">SchedulesDirect</a> allows access to TV data of North America and some other regions with a small annual fee.<br>You need to register to <a href=\"http://www.schedulesdirect.org/\">SchedulesDirect</a> to be allowed to download the TV data.<br><br><b>The TV channels of <a href=\"http://www.schedulesdirect.org/\">SchedulesDirect</a> can only be found after you have entered the Authentication data</b>.")),cc.xyw(2,2,3));
      
      if(showSeparator) {
        layout.appendRow(RowSpec.decode("10dlu"));
        layout.appendRow(RowSpec.decode("default"));
        pb.addSeparator(mLocalizer.msg("authentication", "Authentication data"), cc.xyw(1,layout.getRowCount(),5));
      }
      
      layout.appendRow(RowSpec.decode("10dlu"));
      layout.appendRow(RowSpec.decode("default"));
      pb.addLabel(mLocalizer.msg("user", "Username") + ":",cc.xy(2,layout.getRowCount()));
        
        mUsername = new JTextField();
        mUsername.setText(mOldUserName);
        mUsername.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                updateValue();
            }

            public void removeUpdate(DocumentEvent event) {
                updateValue();
            }

            public void changedUpdate(DocumentEvent event) {
                updateValue();
            }

            public void updateValue() {
                mProperties.setProperty("username", mUsername.getText());
            }
        });
        pb.add(mUsername, cc.xy(4, layout.getRowCount()));

        layout.appendRow(RowSpec.decode("5dlu"));
        layout.appendRow(RowSpec.decode("default"));
        pb.addLabel(mLocalizer.msg("password", "Password") + ":", cc.xy(2, layout.getRowCount()));
        mPassword = new JPasswordField();
        mPassword.setText(IOUtilities.xorDecode(mOldPassword, PASSWORDSEED));
        mPassword.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                updateValue();
            }

            public void removeUpdate(DocumentEvent event) {
                updateValue();
            }

            public void changedUpdate(DocumentEvent event) {
                updateValue();
            }

            public void updateValue() {
                mProperties.setProperty("password", IOUtilities.xorEncode(new String(mPassword.getPassword()), PASSWORDSEED));
            }
        });
        pb.add(mPassword, cc.xy(4, layout.getRowCount()));
    }

    public void ok() {
        mProperties.setProperty("username", mUsername.getText());
        mProperties.setProperty("password", IOUtilities.xorEncode(new String(mPassword.getPassword()), PASSWORDSEED));
    }

    public void cancel() {
        mProperties.setProperty("username", mOldUserName);
        mProperties.setProperty("password", mOldPassword);
    }
}
