package swedbtvdataservice;

import java.net.URL;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvdataservice.SettingsPanel;
import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.html.ExtendedHTMLEditorKit;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class DataHydraSettingsPanel extends SettingsPanel {
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(DataHydraSettingsPanel.class);

  private Properties mProperties;
  private JCheckBox mShowRegisterText;

  public DataHydraSettingsPanel(Properties prop) {
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
