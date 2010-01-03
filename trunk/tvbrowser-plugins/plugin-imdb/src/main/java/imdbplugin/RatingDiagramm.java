package imdbplugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JComponent;
import util.ui.Localizer;

public class RatingDiagramm extends JComponent {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(RatingDiagramm.class);

  private ImdbRating rating;

  Color ratingBackground = new Color(229, 229, 229);
  Color legendBackground = new Color(201, 201, 201);
  Color headlineColor = new Color(127, 127, 127);
  Font legendFont;
  Font headline;
  int[] values = new int[10];
  int maxValue = 0;

  public RatingDiagramm(ImdbRating rating) {
    this.rating = rating;
    String dist = rating.getDistribution();
    for (int i=0;i<10;i++){
      char character = dist.charAt(i);
      if (character == '.') {
        values[i] = 0;
      } else if (character == '*') {
        values[i] = 100;
      } else {
        values[i] = (character - '0') * 10;
      }
      if (values[i] > maxValue) {
        maxValue = values[i];
      }
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (legendFont == null) {
       legendFont = getFont().deriveFont(12f);
       headline = getFont().deriveFont(17f);
    }

    g.setColor(ratingBackground);
    g.fillRect(0,0, getWidth(), getHeight());

    g.setFont(headline);
    g.setColor(Color.black);

    g.drawString(rating.getRatingText(), 10, 25);
    g.setColor(headlineColor);
    g.drawString("/10", g.getFontMetrics().stringWidth(rating.getRatingText()) + 10, 25);

    String votes = rating.getVotes() + " " + mLocalizer.msg("votes", "votes");

    int width = g.getFontMetrics().stringWidth(votes);
    g.drawString(votes, getWidth() - width - 10, 25);

    for (int i = 0;i < 10;i++) {
      g.setColor(legendBackground);
      g.drawRect(10 + i*25, 50, 20, 88);
      g.fillRect(10 + i*25, getHeight() - 35, 20, 25);

      g.setColor(Color.black);

      int size = (int)(88 - (88f / maxValue) * values[i]);

      g.fillRect(10 + i*25, 50 + size, 20, 88 - size);

      g.setFont(legendFont);
      String value = Integer.toString(i + 1);
      width = g.getFontMetrics().stringWidth(value);
      g.drawString(value, (20-width / 2) + i*25, getHeight() - 18);
    }

  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(265, 180);
  }
}
