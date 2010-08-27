package tvbrowser.ui.settings;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.lang.StringUtils;

import tvbrowser.core.Settings;
import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;
import util.ui.html.HTMLTextHelper;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.PluginInfo;

/**
 * The Info-Dialog shows Informations about an Plugin
 *
 * @author bodum
 */
public class PluginInfoDialog extends JDialog implements WindowClosingIf {
  /** Translation */
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(PluginInfoDialog.class);

  /** Infos about the Plugin */
  private PluginInfo mPluginInfo;
  /** Icon */
  private Icon mPluginIcon;

  /**
   * Create the Dialog
   * @param dialog Parent Dialog
   * @param pluginInfo Info's about the Plugin
   */
  public PluginInfoDialog(JDialog dialog, PluginInfo pluginInfo) {
    this(dialog, null, pluginInfo);
  }

  /**
   * Create the Dialog
   * @param dialog Parent Dialog
   * @param icon Icon of the Plugin
   * @param pluginInfo Info's about the Plugin
   */
  public PluginInfoDialog(JDialog dialog, Icon icon, PluginInfo pluginInfo) {
    super(dialog, true);
    setTitle(pluginInfo.getName());
    mPluginInfo = pluginInfo;

    if (icon == null) {
      icon = new ImageIcon("imgs/Jar16.gif");
    }

    mPluginIcon = icon;
    initGui();
  }

  /**
   * Create the GUI
   */
  private void initGui() {

    JPanel panel = (JPanel) getContentPane();
    panel.setBorder(Borders.DLU4_BORDER);
    panel.setLayout(new FormLayout("fill:default:grow, default", "fill:default:grow, 3dlu, default"));

    CellConstraints cc = new CellConstraints();

    JEditorPane infoPanel = new JEditorPane();

    infoPanel.setEditorKit(new ExtendedHTMLEditorKit());

    ExtendedHTMLDocument doc = (ExtendedHTMLDocument) infoPanel.getDocument();

    infoPanel.setEditable(false);
    infoPanel.setText(generateHtml(doc));

    infoPanel.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = evt.getURL();
          if (url != null) {
              Launch.openURL(url.toString());
          }
        }
      }
    });

    panel.add(new JScrollPane(infoPanel), cc.xyw(1,1,2));

    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    panel.add(ok, cc.xy(2,3));

    Settings.layoutWindow("pluginInfoDialog",this,new Dimension(700,500));

    UiUtilities.registerForClosing(this);
  }

  /**
   * Generate the HML for the EditorPane
   * @param doc Document to use
   * @return Html-Text
   */
  private String generateHtml(ExtendedHTMLDocument doc) {
    StringBuilder html = new StringBuilder(1024);

    html.append("<html><style type=\"text/css\" media=\"screen\">"
                + "<!--" +
                    "body {font-size:12px;font-family:Dialog;}" +
                    "h1 {font-size:12px;font-family:Dialog;font-weight:bold;}" +
                "-->" +
                "</style><body>");

    html.append("<table><tr><td valign=\"top\">");

    html.append(doc.createCompTag(new JLabel(mPluginIcon)));

    html.append("</td><td valign=\"top\"><b>").append(mPluginInfo.getName()).append("</b></td></tr></table>");

    html.append("<i>").append(mLocalizer.msg("version", "Version")).append(' ')
        .append(mPluginInfo.getVersion()).append("</i><br>");

    html.append("<h1>").append(mLocalizer.msg("author", "Author")).append("</h1>");
    html.append(HTMLTextHelper.convertTextToHtml(mPluginInfo.getAuthor(), true));

    if (StringUtils.isNotEmpty(mPluginInfo.getLicense())) {
      html.append("<h1>").append(mLocalizer.msg("licence", "Licence")).append("</h1>");
      html.append(mPluginInfo.getLicense().toLowerCase().startsWith("<html>") ? mPluginInfo.getLicense() : HTMLTextHelper.convertTextToHtml(mPluginInfo.getLicense(), true));
    }

    html.append("<h1>").append(mLocalizer.msg("description", "Description")).append("</h1>");
    html.append(HTMLTextHelper.convertTextToHtml(mPluginInfo.getDescription(), true));

    if(mPluginInfo.getHelpUrl() != null) {
      html.append("<br><br><a href=\"").append(mPluginInfo.getHelpUrl()).append("</a>");
    }

    html.append("</body></html>");

    return html.toString();
  }

  public void close() {
    setVisible(false);
  }

}