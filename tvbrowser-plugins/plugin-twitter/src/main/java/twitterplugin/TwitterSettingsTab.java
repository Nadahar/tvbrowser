package twitterplugin;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.paramhandler.ParamInputField;
import util.ui.Localizer;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;
import devplugin.ThemeIcon;

public final class TwitterSettingsTab implements SettingsTab {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TwitterSettingsTab.class);
  private boolean mUserStored = "true".equalsIgnoreCase(TwitterPlugin.getInstance().getSettings().getProperty(TwitterPlugin.STORE_PASSWORD, "false"));
  private ParamInputField mFormat;

  public JPanel createSettingsPanel() {
    final FormLayout layout = new FormLayout("3dlu,fill:min:grow,3dlu");
    final CellConstraints cc = new CellConstraints();

    final JPanel panel = new JPanel(layout);
    panel.setBorder(Borders.DLU4_BORDER);
    
    int line = 1;

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));

    JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    String username = TwitterPlugin.getInstance().getSettings().getProperty(TwitterPlugin.USERNAME);

    if (!mUserStored) {
      username = mLocalizer.msg("notDefined", "Not entered");
    }

    final JLabel user = new JLabel(mLocalizer.msg("userText", "<html><body><b>User:</b> {0}</body></html>", username));
    userPanel.add(user);

    final JButton delete = new JButton(TwitterPlugin.getPluginManager().getIconFromTheme(TwitterPlugin.getInstance(), new ThemeIcon("actions", "edit-delete", 16)));
    delete.setToolTipText(mLocalizer.msg("delete", "Delete"));

    delete.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mUserStored = false;
        user.setText(mLocalizer.msg("userText", "<html><body><b>User:</b> {0}</body></html>", mLocalizer.msg("notDefined", "Not entered")));
        delete.setVisible(false);
      }
    });

    delete.setVisible(mUserStored);

    userPanel.add(delete);

    panel.add(userPanel, cc.xy(2, line));

    layout.appendRow(RowSpec.decode("pref"));
    layout.appendRow(RowSpec.decode("3dlu"));

    panel.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("format", "Twitter Format")), cc.xyw(1, line += 2, 3));

    layout.appendRow(RowSpec.decode("fill:min:grow"));
    layout.appendRow(RowSpec.decode("3dlu"));

    mFormat = new ParamInputField(TwitterPlugin.getInstance().getSettings().getProperty(TwitterPlugin.FORMAT, TwitterPlugin.DEFAULT_FORMAT));

    panel.add(mFormat, cc.xy(2, line += 2));

    return panel;
  }

  public void saveSettings() {
    if (!mUserStored) {
      TwitterPlugin.getInstance().getSettings().setProperty(TwitterPlugin.STORE_PASSWORD, "false");
    }
    TwitterPlugin.getInstance().getSettings().setProperty(TwitterPlugin.FORMAT, mFormat.getText());
  }

  public Icon getIcon() {
    return TwitterPlugin.getInstance().getPluginIcon();
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Title");
  }
}
