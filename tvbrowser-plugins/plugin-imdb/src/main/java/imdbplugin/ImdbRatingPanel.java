package imdbplugin;

import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.AbstractBorder;
import util.ui.Localizer;

public class ImdbRatingPanel extends JPanel {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbRatingPanel.class);

  private ImdbRating rating;
  private ImdbMovie movie;

  public ImdbRatingPanel(ImdbMovie movie, ImdbRating rating) {
    this.rating = rating;
    this.movie = movie;
    createGui();
  }

  private void createGui() {
    setBackground(Color.WHITE);

    FormLayout layout = new FormLayout("fill:min:grow");

    setLayout(layout);

    setBorder(new AbstractBorder(){
      @Override
      public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.setColor(new Color(240,240,240));
        g.fillRect(x,y, x + width, y + 10);
        g.fillRect(x,y + height - 10, x + width, y + height);
        g.fillRect(x, y + 10, x + 10, y+ height - 10);
        g.fillRect(x + width - 10, y + 10, x + width, y+height - 10);

        g.setColor(new Color(222, 222, 222));
        g.drawLine(x+10,y+10, x + width - 10, y + 10);

        g.setColor(new Color(218, 218, 218));
        g.drawLine(x+10,y+11, x+10, y + height - 11);
        g.drawLine(x + width - 10,y+11, x + width - 10, y + height - 11);

        g.setColor(new Color(181, 181, 181));
        g.drawLine(x+10,y + height - 10, x + width - 10, y + height - 10);
      }

      @Override
      public Insets getBorderInsets(Component c) {
        return new Insets(20, 20, 20, 20);
      }
    });
                                     
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
    diagramPanel.add(new RatingDiagramm(rating));
    add(diagramPanel, cc.xy(1,layout.getRowCount()));
    layout.appendRow(RowSpec.decode("3dlu"));

    ImdbAka[] akas = movie.getAkas();

    if (akas.length > 0) {
      layout.appendRow(RowSpec.decode("pref"));

      JLabel alternativeHead = new JLabel(mLocalizer.msg("alternativeTitle","Alternative Titel") + ":");
      alternativeHead.setForeground(Color.black);
      alternativeHead.setFont(alternativeHead.getFont().deriveFont(12f).deriveFont(Font.BOLD));

      add(alternativeHead, cc.xy(1,layout.getRowCount()));

      layout.appendRow(RowSpec.decode("3dlu"));
      layout.appendRow(RowSpec.decode("pref"));

      StringBuilder akaString = new StringBuilder();

      for (ImdbAka aka:akas) {
        if (akaString.length() > 0) {
          akaString.append(", ");
        }
        akaString.append(aka.getTitle());
        akaString.append(" (");
        if (aka.getEpisode() != null && aka.getEpisode().length() > 0) {
          akaString.append(aka.getEpisode()).append(", ");
        }
        akaString.append(aka.getYear()).append(") ");
      }

      JLabel akaLabel = new JLabel(akaString.toString());
      akaLabel.setForeground(Color.black);
      akaLabel.setFont(alternativeHead.getFont().deriveFont(12f).deriveFont(Font.PLAIN));
      
      add(akaLabel, cc.xy(1,layout.getRowCount()));
    }

  }

}