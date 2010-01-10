package imdbplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import util.ui.Localizer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

final class RatingDiagram extends JPanel {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(RatingDiagram.class);

  private ImdbRating mRating;

  private Color mRatingBackground = new Color(229, 229, 229);
  private Color mLegendBackground = new Color(201, 201, 201);
  private int[] mValues = new int[10];
  private int mMaxValue = 0;

  private JLabel mLbRating;

  private JLabel mLbVotes;

  public RatingDiagram(final ImdbRating rating) {
    mRating = rating;
    decodeDistribution();
    createUI();
  }

  private void createUI() {
    setBorder(BorderFactory.createLineBorder(mRatingBackground, 10));
    FormLayout layout = new FormLayout("pref,pref,fill:min:grow,pref");
    setLayout(layout);
    setOpaque(true);
    setBackground(mRatingBackground);
    CellConstraints cc = new CellConstraints();
    layout.appendRow(RowSpec.decode("pref"));
    mLbRating = new JLabel(mRating.getRatingText());
    Font bigFont = mLbRating.getFont().deriveFont(18f);
    mLbRating.setFont(bigFont);
    add(mLbRating, cc.xy(1, layout.getRowCount()));
    JLabel label = new JLabel(" " + mLocalizer.msg("of10", "of 10"));
    label.setFont(bigFont);
    label.setForeground(mLegendBackground);
    add(label, cc.xy(2, layout.getRowCount()));
    mLbVotes = new JLabel(mLocalizer.msg("votes", "votes", mRating.getVotes()));
    mLbVotes.setFont(bigFont);
    mLbVotes.setForeground(mLegendBackground);
    add(mLbVotes, cc.xy(layout.getColumnCount(), layout.getRowCount()));
    layout.appendRow(RowSpec.decode("10dlu"));
    layout.appendRow(RowSpec.decode("fill:min:grow"));
    JPanel barPanel = new JPanel();
    barPanel.setBackground(mRatingBackground);
    GridLayout gridLayout = new GridLayout(1, 10, 4, 0);
    barPanel.setLayout(gridLayout);
    for (int i = 0; i < 10; i++) {
      barPanel.add(new SlotPanel(i + 1, mValues[i] *100 / mMaxValue));
    }
    add(barPanel, cc.xyw(1, layout.getRowCount(), layout.getColumnCount()));
  }

  private void decodeDistribution() {
    String dist = mRating.getDistribution();
    for (int i=0;i<10;i++){
      char character = dist.charAt(i);
/*
      excerpt from IMDb FAQ
      "." no votes cast        "3" 30-39% of the votes  "7" 70-79% of the votes
      "0"  1-9%  of the votes  "4" 40-49% of the votes  "8" 80-89% of the votes
      "1" 10-19% of the votes  "5" 50-59% of the votes  "9" 90-99% of the votes
      "2" 20-29% of the votes  "6" 60-69% of the votes  "*" 100%   of the votes
*/
      if (character == '.') {
        mValues[i] = 0;
      } else if (character == '*') {
        mValues[i] = 100;
      } else {
        mValues[i] = (character - '0') * 10 + 5; // adding 5 display this as average of the given range and avoids most cells being empty
      }
      if (mValues[i] > mMaxValue) {
        mMaxValue = mValues[i];
      }
    }
  }
  
  private class SlotPanel extends JPanel {
    private int mVote;
    private int mPercent;

    public SlotPanel(final int vote, final int percent) {
      mVote = vote;
      mPercent = percent;
      createUI();
    }

    private void createUI() {
      FormLayout layout = new FormLayout("fill:pref:grow");
      setLayout(layout);
      CellConstraints cc = new CellConstraints();
      layout.appendRow(RowSpec.decode("fill:pref:grow"));
      JPanel outerPanel = new JPanel(new BorderLayout());
      outerPanel.setBorder(BorderFactory.createLineBorder(mLegendBackground));
      JPanel invisiblePanel = new JPanel();
      invisiblePanel.setBackground(mRatingBackground);
      invisiblePanel.setPreferredSize(new Dimension(22, 100 - mPercent));
      outerPanel.add(invisiblePanel, BorderLayout.NORTH);
      JPanel fillPanel = new JPanel();
      fillPanel.setPreferredSize(new Dimension(22, mPercent));
      fillPanel.setOpaque(true);
      fillPanel.setBackground(Color.BLACK);
      outerPanel.add(fillPanel, BorderLayout.SOUTH);
      add(outerPanel, cc.xy(1, layout.getRowCount(), "center, bottom"));
      layout.appendRow(RowSpec.decode("1dlu"));
      layout.appendRow(RowSpec.decode("pref"));
      JLabel voteLabel = new JLabel(String.valueOf(mVote));
      voteLabel.setBackground(mLegendBackground);
      voteLabel.setOpaque(true);
      voteLabel.setHorizontalAlignment(SwingConstants.CENTER);
      add(voteLabel, cc.xy(1, layout.getRowCount(), "fill, fill"));
    }
  }
}
