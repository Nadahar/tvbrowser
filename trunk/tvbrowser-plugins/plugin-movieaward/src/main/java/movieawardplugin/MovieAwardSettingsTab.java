package movieawardplugin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.customizableitems.SelectableItemList;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

public class MovieAwardSettingsTab implements SettingsTab {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(MovieAwardSettingsTab.class);

  private MovieAwardPlugin mPlugin;

	private SelectableItemList mAwardSelection;

	private MovieAwardSettings mSettings;

  public MovieAwardSettingsTab(final MovieAwardPlugin movieAwardPlugin, final MovieAwardSettings movieAwardSettings) {
    mPlugin = movieAwardPlugin;
    mSettings = movieAwardSettings;
  }

  public JPanel createSettingsPanel() {
    final JPanel panel = new JPanel(new FormLayout("fill:min:grow", "150," + FormFactory.RELATED_GAP_COLSPEC.encode() + ",fill:min:grow"));
    panel.setBorder(Borders.DLU4_BORDER);
    final CellConstraints cc = new CellConstraints();

    ArrayList<String> allNames = new ArrayList<String>();
    ArrayList<String> selectedNames = new ArrayList<String>();
    for (String award : MovieAwardPlugin.getAvailableAwards()) {
			String localizedName = MovieAwardPlugin.getNameOfAward(award);
			allNames.add(localizedName);
			if (mSettings.isAwardEnabled(award)) {
				selectedNames.add(localizedName);
			}
		}
    Collections.sort(allNames);
    mAwardSelection = new SelectableItemList(selectedNames.toArray(new String[selectedNames.size()]), allNames.toArray(new String[allNames.size()]));
    panel.add(mAwardSelection, cc.xy(1, 1));
    
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

    panel.add(new JScrollPane(editor), cc.xy(1,3));
    SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
		    editor.setCaretPosition(0);
			}
		});
    return panel;
  }

  public void saveSettings() {
    Object[] selection = mAwardSelection.getSelection();
		ArrayList<String> selected = new ArrayList<String>();
    for (Object object : selection) {
    	selected.add(object.toString());
		}
    for (String award : MovieAwardPlugin.getAvailableAwards()) {
			mSettings.enableAward(award, selected.contains(MovieAwardPlugin.getNameOfAward(award)));
		}
    MovieAwardPlugin.getInstance().reloadAwards();
  }

  public Icon getIcon() {
    return mPlugin.getPluginIcon();
  }

  public String getTitle() {
    return mLocalizer.msg("title", "Movie Awards");
  }

}
