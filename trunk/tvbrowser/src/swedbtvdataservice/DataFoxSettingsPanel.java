package swedbtvdataservice;

import tvdataservice.SettingsPanel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.factories.DefaultComponentFactory;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

import util.ui.html.ExtendedHTMLEditorKit;
import util.ui.Localizer;
import util.browserlauncher.Launch;

import java.net.URL;
import java.util.Properties;

public class DataFoxSettingsPanel extends SettingsPanel {
  public static final Localizer mLocalizer = Localizer.getLocalizerFor(DataFoxSettingsPanel.class);

  private Properties mProperties;
  private JCheckBox mShowRegisterText;

  public DataFoxSettingsPanel(Properties prop) {
    mProperties = prop;
    createGui();
  }

  private void createGui() {
    setLayout(new FormLayout("5dlu, fill:10dlu:grow", "3dlu, pref, 3dlu, pref, 3dlu, fill:pref:grow"));

    CellConstraints cc = new CellConstraints();

    mShowRegisterText = new JCheckBox(mLocalizer.msg("showRegister", "Show 'Please Register' Text at bottom of description"));

    if ("true".equals(mProperties.getProperty(SweDBTvDataService.SHOW_REGISTER_TEXT, "true"))) {
      mShowRegisterText.setSelected(true);
    }

    add(mShowRegisterText, cc.xy(2,2));

    add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("about","About")), cc.xyw(1,4,2));

    JEditorPane htmlPane = new JEditorPane();

    htmlPane.setEditorKit(new ExtendedHTMLEditorKit());
    htmlPane.setEditable(false);

    htmlPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = evt.getURL();
          if (url != null) {
            Launch.openURL(url.toString());
          }
        }
      }
    });

    htmlPane.setText("<html><body>"+mLocalizer.msg("help", "Help Text")+"</body></html>");

    add(new JScrollPane(htmlPane), cc.xy(2,6));
  }

  public void ok() {
    
    if (mShowRegisterText.isSelected()) {
      mProperties.setProperty(SweDBTvDataService.SHOW_REGISTER_TEXT, "true");
    } else {
      mProperties.setProperty(SweDBTvDataService.SHOW_REGISTER_TEXT, "false");
    }

  }

}
