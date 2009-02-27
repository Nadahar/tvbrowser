package movieawardplugin;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

public class MovieAwardSettingsTab implements SettingsTab {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(MovieAwardSettingsTab.class);

  private MovieAwardPlugin mPlugin;

  public MovieAwardSettingsTab(final MovieAwardPlugin movieAwardPlugin) {
    mPlugin = movieAwardPlugin;
  }

  public JPanel createSettingsPanel() {
    final JPanel panel = new JPanel(new FormLayout("fill:min:grow", "fill:min:grow"));
    panel.setBorder(Borders.DLU4_BORDER);
    final CellConstraints cc = new CellConstraints();

    final StringBuilder builder = new StringBuilder();

    builder.append("<html><body>");

    builder.append(
        mLocalizer.msg("description",
            "This plugin contains a list of winners for these awards:"))
        .append("<br>");

    builder.append("<ul>");

    final List<MovieAward> awards = mPlugin.getMovieAwards();
    Collections.sort(awards);

    for (MovieAward award : awards) {
      builder.append("<li>");

      if (award.getUrl() != null) {
        builder.append("<a href=\"").append(award.getUrl()).append("\">");
      }
      builder.append(award.getName());
      if (award.getUrl() != null) {
        builder.append("</a>");
      }

      if (award.getProviderName() != null) {
        builder.append(' ').append(mLocalizer.msg("provided", "provided by"))
            .append(' ');
        if (award.getProviderUrl() != null) {
          builder.append("<a href=\"").append(award.getProviderUrl()).append("\">");
        }
        builder.append(award.getProviderName());
        if (award.getProviderUrl() != null) {
          builder.append("</a>");
        }
      }

      builder.append("</li>");
    }

    builder.append("</ul>");

    builder.append(mLocalizer.msg("footer", "footer"));
    builder.append("</body></html>");

    final JEditorPane editor = UiUtilities.createHtmlHelpTextArea(builder
        .toString(), Color.WHITE);
    editor.setBackground(Color.WHITE);
    editor.setOpaque(true);

    
    panel.add(new JScrollPane(editor), cc.xy(1,1));

    return panel;
  }

  public void saveSettings() {
    // nothing to save
  }

  public Icon getIcon() {
    return mPlugin.getPluginIcon();
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Movie Awards");
  }

}
