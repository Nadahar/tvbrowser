/*
 * TV-Browser Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package tvraterplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import util.ui.Localizer;
import util.ui.TabLayout;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * This Dialog shows one Rating
 *
 * @author bodo tasche
 */
public class DialogRating extends JDialog implements WindowClosingIf {

    /** Localizer */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(DialogRating.class);

    /** Rating-Boxes */
    private JComboBox[] mRatings = new JComboBox[6];

    /** Personal Rating */
    private Rating mPersonalRating;

    /** Overall Rating */
    private Rating mServerRating;

    /** Title of the Program */
    private String mTitle;

    /** Original Title */
    private String mOriginaltitle;

    /** Rater Plugin */
    private TVRaterPlugin mRater;

    /** Empty Personal Rating */
    private boolean mEmptyPersonal = false;

    /** The Genre */
    private GenreComboBox mGenre;

    /** The Program that is rated */
    private Program mProgram = null;

    /**
     * Creates the DialgoRating
     *
     * @param frame ParentFrame
     * @param rater TVRaterPlugin
     * @param programtitle the Title of the Program to rate
     */
    public DialogRating(Frame frame, TVRaterPlugin rater, String programtitle) {
        super(frame, true);
        setTitle(mLocalizer.msg("title", "View Rating"));

        mRater = rater;
        mServerRating = mRater.getDatabase().getServerRating(programtitle);
        mPersonalRating = mRater.getDatabase().getPersonalRating(programtitle);

        if (mPersonalRating == null) {
            mEmptyPersonal = true;
            mPersonalRating = new Rating(programtitle);
        }

        mTitle = programtitle;
        mOriginaltitle = null;
        createGUI();
    }

    /**
     * Creates the DialgoRating
     *
     * @param parent Parent-Frame
     * @param rater TVRaterPlugin
     * @param program Program to rate
     */
    public DialogRating(Frame parent, TVRaterPlugin rater, Program program) {
        super(parent, true);
        setTitle(mLocalizer.msg("title", "View Rating"));

        mRater = rater;
        mServerRating = mRater.getDatabase().getServerRating(program);
        mPersonalRating = rater.getDatabase().getPersonalRating(program);

        if (mPersonalRating == null) {
            mEmptyPersonal = true;
            mPersonalRating = new Rating(program.getTitle());
        }

        mTitle = program.getTitle();
        mOriginaltitle = program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE);
        mProgram = program;
        createGUI();
    }

    /**
     * Creates the GUI.
     */
    private void createGUI() {
        UiUtilities.registerForClosing(this);

        JPanel panel = (JPanel) this.getContentPane();

        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        JPanel titlePanel = new JPanel(new TabLayout(1));
        titlePanel.setBackground(Color.white);

        JLabel title = new JLabel(mTitle);
        title.setPreferredSize(new Dimension(400, 40));

        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 20));
        titlePanel.add(title);

        if (mOriginaltitle != null) {
            JLabel original = new JLabel("(" + mOriginaltitle + ")");
            original.setHorizontalAlignment(SwingConstants.CENTER);
            titlePanel.add(original);
        }

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;

        panel.add(titlePanel, c);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = .5;
        c.weighty = 1.0;
        c.gridwidth = 1;

        panel.add(createRatingPanel(mServerRating), c);

        c.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(createVotingPanel(mPersonalRating), c);

        JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));

        JButton rate = new JButton(mLocalizer.msg("rate", "Rate"));

        rate.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                rateWasPressed();
            }
        });

        buttonpanel.add(rate);

        JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));

        cancel.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
              setVisible(false);
            }
        });

        buttonpanel.add(cancel);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0;
        c.insets = new Insets(0, 5, 5, 5);

        c.gridwidth = 1;

        JPanel leftbuttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton showListButton = new JButton(new ListAction(this, mTitle, true, false));

        leftbuttonPanel.add(showListButton);


        int id = 0;

        if (mServerRating != null) {
            id = mServerRating.getRatingId();
        }

        if ((mPersonalRating != null) && (id <= 0)){
            id = mPersonalRating.getRatingId();
        }

        JButton detailsButtons = new JButton(new ShowDetailsAction(id, true, false));

        if ((mServerRating == null) || ( mServerRating.getRatingId() < 0)) {
            detailsButtons.setEnabled(false);
        }

        if ((mPersonalRating != null) && ( mPersonalRating.getRatingId() > 0)) {
            detailsButtons.setEnabled(true);
        }

        leftbuttonPanel.add(detailsButtons);


        panel.add(leftbuttonPanel, c);

        panel.add(buttonpanel, c);

        getRootPane().setDefaultButton(cancel);

        pack();
    }

    /**
     * Rate was pressed, internal Database gets updated
     */
    private void rateWasPressed() {

        if (!checkAllRatings()) {
            JOptionPane.showMessageDialog(this,
                    mLocalizer.msg("allRatings", "Please set all Ratings"));
            return;
        }

        int[] values = new int[6];

        for (int i = 0; i < 6; i++) {
            values[i] = mRatings[i].getSelectedIndex();
        }

        mPersonalRating.setOverallRating(mRatings[0].getSelectedIndex());
        mPersonalRating.setActionRating(mRatings[1].getSelectedIndex());
        mPersonalRating.setFunRating(mRatings[2].getSelectedIndex());
        mPersonalRating.setEroticRating(mRatings[3].getSelectedIndex());
        mPersonalRating.setTensionRating(mRatings[4].getSelectedIndex());
        mPersonalRating.setEntitlementRating(mRatings[5].getSelectedIndex());
        mPersonalRating.setGenre(Integer.parseInt(mGenre.getSelectedItem().toString()));

        mRater.getDatabase().setPersonalRating(mPersonalRating);
        setVisible(false);

        if (mRater.getUpdateInterval() == UpdateInterval.OnRating) {
          mRater.runUpdate(true, null);
        }

        // refresh the plugin tree as we may need to remove the just rated program
        mRater.updateRootNode();
    }

    /**
     * Checks if all ratings are checked
     * @return true if all ratings are checked
     */
    private boolean checkAllRatings() {
        for (JComboBox _rating : mRatings) {
            if (_rating.getSelectedItem() == null) {
                return false;
            }
        }

        return mGenre.getSelectedItem() != null;
    }

    /**
     * Creates the RatingPanel
     *
     * @param rating Rating to use
     * @return RatingPanel
     */
    private JPanel createRatingPanel(Rating rating) {
        JPanel ratingPanel = new JPanel();

        String titel = mLocalizer.msg("overallRating", "Overall Rating");
        if ((rating != null) && (rating.getRatingCount() > 0)) {
            titel += " (" + rating.getRatingCount() + ")";
        }

        ratingPanel.setBorder(BorderFactory.createTitledBorder(titel));

        if (rating != null) {

            GridBagConstraints labc = new GridBagConstraints();
            labc.weightx = 1;
            labc.weighty = 1;
            labc.fill = GridBagConstraints.BOTH;

            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 0;
            c.weighty = 0;
            c.fill = GridBagConstraints.HORIZONTAL;

            ratingPanel.setLayout(new GridBagLayout());

            ratingPanel.add(new JLabel(mLocalizer.msg("overall", "Overall") + ":", SwingConstants.LEFT), labc);
            ratingPanel.add(createRatingBox(rating, Rating.OVERALL_RATING_KEY), c);

            ratingPanel.add(new JLabel(mLocalizer.msg("action", "Action") + ":", SwingConstants.LEFT), labc);
            ratingPanel.add(createRatingBox(rating, Rating.ACTION_RATING_KEY), c);

            ratingPanel.add(new JLabel(mLocalizer.msg("fun", "Fun") + ":", SwingConstants.LEFT), labc);
            ratingPanel.add(createRatingBox(rating, Rating.FUN_RATING_KEY), c);

            ratingPanel.add(new JLabel(mLocalizer.msg("erotic", "Erotic") + ":", SwingConstants.LEFT), labc);
            ratingPanel.add(createRatingBox(rating, Rating.EROTIC_RATING_KEY), c);

            ratingPanel.add(new JLabel(mLocalizer.msg("tension", "Tension") + ":", SwingConstants.LEFT), labc);
            ratingPanel.add(createRatingBox(rating, Rating.TENSION_RATING_KEY), c);

            ratingPanel.add(new JLabel(mLocalizer.msg("entitlement", "Entitlement") + ":", SwingConstants.LEFT), labc);
            ratingPanel.add(createRatingBox(rating, Rating.ENTITLEMENT_RATING_KEY), c);

            ratingPanel.add(new JLabel(mLocalizer.msg("genre", "Genre") + ":", SwingConstants.LEFT), labc);

            if (rating.getGenre() >= 0) {

                String str = Integer.toString(rating.getGenre());

                while (str.length() < 3) {
                    str = "0" + str;
                }

                String text;
                if (RatingIconTextFactory.getGenres().containsKey(str)) {
                    text = RatingIconTextFactory.getGenres().getProperty(str);
                } else {
                    text = RatingIconTextFactory.getGenres().getProperty("999");
                }

                ratingPanel.add(new JLabel(text), c);
            } else {
                ratingPanel.add(new JLabel("-"), c);
            }
        } else {
            ratingPanel.setLayout(new BorderLayout());
            JTextPane pane = new JTextPane();
            pane.setContentType("text/html");

            if ((mProgram != null) && !mRater.isProgramRateable(mProgram)) {
                pane.setText(MessageFormat.format("<center style=''font-family: helvetica''>{0}", mLocalizer.msg("tooshort", "Program too short for rating. <br>The minimum length is {0} min.<br>This reduces traffic on the server.", TVRaterPlugin.MINLENGTH)));
            } else {
                pane.setText("<center style='font-family: helvetica'>"
                        + mLocalizer.msg("doesntexist", "Sorry, rating doesn't exist!") + "</center>");
            }

            pane.setEditable(false);
            ratingPanel.add(pane, BorderLayout.CENTER);
        }

        return ratingPanel;
    }

    /**
     * Creates a Ratingbox
     *
     * @param rating Rating that should be shown
     * @param type Element in rating that should be shown
     * @return JPanel with rating-box
     */
    private static Component createRatingBox(Rating rating, int type) {
      int value = rating.getIntValue(type);

      return new JLabel(RatingIconTextFactory.getStringForRating(type, value), RatingIconTextFactory
              .getImageIconForRating(value), SwingConstants.LEFT);
    }

    /**
     * Creates a voting-panel
     *
     * @param rating Rating to use
     * @return voting-panel
     */
    private JPanel createVotingPanel(Rating rating) {
        JPanel voting = new JPanel(new GridBagLayout());

        String text = mLocalizer.msg("yourRating", "Your Rating");

        if (mEmptyPersonal) {
            text += "*";
        }

        voting.setBorder(BorderFactory.createTitledBorder(text));

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 0.5;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        GridBagConstraints labc = new GridBagConstraints();
        labc.weightx = 0.5;
        labc.weighty = 1;
        labc.fill = GridBagConstraints.BOTH;

        voting.add(new JLabel(mLocalizer.msg("overall", "Overall") + ":"), labc);
        voting.add(createVotingBox(mPersonalRating, Rating.OVERALL_RATING_KEY, 0), c);

        voting.add(new JLabel(mLocalizer.msg("action", "Action") + ":"), labc);
        voting.add(createVotingBox(mPersonalRating, Rating.ACTION_RATING_KEY, 1), c);

        voting.add(new JLabel(mLocalizer.msg("fun", "Fun") + ":"), labc);
        voting.add(createVotingBox(mPersonalRating, Rating.FUN_RATING_KEY, 2), c);

        voting.add(new JLabel(mLocalizer.msg("erotic", "Erotic") + ":"), labc);
        voting.add(createVotingBox(mPersonalRating, Rating.EROTIC_RATING_KEY, 3), c);

        voting.add(new JLabel(mLocalizer.msg("tension", "Tension") + ":"), labc);
        voting.add(createVotingBox(mPersonalRating, Rating.TENSION_RATING_KEY, 4), c);

        voting.add(new JLabel(mLocalizer.msg("entitlement", "Entitlement") + ":"), labc);
        voting.add(createVotingBox(mPersonalRating, Rating.ENTITLEMENT_RATING_KEY, 5), c);

        voting.add(new JLabel(mLocalizer.msg("genre", "Genre") + ":"), labc);

        mGenre = new GenreComboBox(mPersonalRating.getGenre());
        voting.add(mGenre, c);

        return voting;
    }

    /**
     * Creates a voting Box
     *
     * @param rating Rating to use
     * @param type what to rate
     * @param ratingbox number of the Box
     * @return a Panel with a Voting-Box
     */
    private Component createVotingBox(Rating rating, int type, int ratingbox) {
        RatingComboBox valuebox = new RatingComboBox(rating, type);

        mRatings[ratingbox] = valuebox;

        return valuebox;
    }

    public void close() {
      setVisible(false);
    }

}