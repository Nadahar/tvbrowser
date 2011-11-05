package imdbplugin;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import util.ui.LinkButton;
import util.ui.Localizer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ImdbRatingPanel extends JPanel {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbRatingPanel.class);

  private ImdbRating rating;
  private ImdbMovie movie;

  public ImdbRatingPanel(final ImdbMovie movie, final ImdbRating rating) {
    this.rating = rating;
    this.movie = movie;
    createGui();
  }

  private void createGui() {
    setBackground(Color.WHITE);

    FormLayout layout = new FormLayout("fill:min:grow");

    setLayout(layout);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    CellConstraints cc = new CellConstraints();

    JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    titlePanel.setBackground(Color.WHITE);

    JLabel title = new JLabel(movie.getTitle());
    title.setFont(title.getFont().deriveFont(20f).deriveFont(Font.BOLD));
    title.setForeground(Color.BLACK);
    titlePanel.add(title);

    JLabel year = new JLabel("(" + Integer.toString(movie.getYear()) + ")");
    year.setFont(year.getFont().deriveFont(20f).deriveFont(Font.BOLD));
    year.setForeground(new Color(166, 166, 166));
    titlePanel.add(year);

    layout.appendRow(RowSpec.decode("pref"));
    add(titlePanel, cc.xy(1,layout.getRowCount()));

    if (movie.getEpisode() != null && movie.getEpisode().length() > 0) {
      layout.appendRow(RowSpec.decode("pref"));

      JLabel episode = new JLabel(movie.getEpisode());
      episode.setFont(year.getFont().deriveFont(18f).deriveFont(Font.PLAIN));
      episode.setForeground(new Color(166, 166, 166));

      JPanel episodePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      episodePanel.setBackground(Color.WHITE);
      episodePanel.add(episode);

      add(episodePanel, cc.xy(1,layout.getRowCount()));
    }
    layout.appendRow(RowSpec.decode("3dlu"));

    layout.appendRow(RowSpec.decode("pref"));
    JPanel diagramPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    diagramPanel.setBackground(Color.WHITE);
    diagramPanel.add(new RatingDiagram(rating));
    add(diagramPanel, cc.xy(1,layout.getRowCount()));
    layout.appendRow(RowSpec.decode("3dlu"));

    ImdbHistogram histogram = ImdbPlugin.getInstance().getHistogram();
    if (histogram != null && histogram.isValid()) {
      layout.appendRow(RowSpec.decode("pref"));
      JLabel percentile = new JLabel(mLocalizer.msg("percentile", "Better than {0} percent of all rated movies.", histogram.getPercentile(rating)));
      add(percentile, cc.xy(1,layout.getRowCount()));
      layout.appendRow(RowSpec.decode("10dlu"));
    }

    ImdbAka[] akas = movie.getAkas();

    if (akas.length > 0) {
      layout.appendRow(RowSpec.decode("pref"));

      JLabel alternativeHead = new JLabel(mLocalizer.msg("alternativeTitle","Alternative Titles") + ":");
      alternativeHead.setForeground(Color.black);
      alternativeHead.setFont(alternativeHead.getFont().deriveFont(Font.BOLD));

      add(alternativeHead, cc.xy(1,layout.getRowCount()));

      layout.appendRow(RowSpec.decode("3dlu"));
      layout.appendRow(RowSpec.decode("pref"));

      StringBuilder akaString = new StringBuilder();

      for (ImdbAka aka:akas) {
        if (akaString.length() > 0) {
          akaString.append(",\n");
        }
        akaString.append(aka.getTitle());
        akaString.append(" (");
        if (aka.getEpisode() != null && aka.getEpisode().length() > 0) {
          akaString.append(aka.getEpisode()).append(", ");
        }
        akaString.append(aka.getYear()).append(")");
      }

      JTextArea akaLabel = new JTextArea(akaString.toString());
      akaLabel.setEditable(false);
      akaLabel.setBackground(Color.WHITE);
      akaLabel.setForeground(Color.BLACK);
      akaLabel.setFont(alternativeHead.getFont().deriveFont(12f).deriveFont(Font.PLAIN));

      add(akaLabel, cc.xy(1,layout.getRowCount()));
    }
    try {
      layout.appendRow(RowSpec.decode("pref"));
      String link = "http://akas.imdb.com/find?s=tt&q="
        +URLEncoder.encode(movie.getTitle() + " ("+movie.getYear()+")","ISO-8859-1");
      if (movie.getEpisode() != null && movie.getEpisode().length() > 0) {
        link = "http://akas.imdb.com/find?s=ep&q="
          +URLEncoder.encode(movie.getEpisode(),"ISO-8859-1");
      }
      LinkButton imdbLink = new LinkButton(mLocalizer.msg("imdbEntry","IMDb entry"), link);
      add(imdbLink, cc.xy(1,layout.getRowCount()));
    } catch (UnsupportedEncodingException e) {
      //ignore
    }

  }

}