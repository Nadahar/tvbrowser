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
    private static final Localizer _mLocalizer = Localizer.getLocalizerFor(DialogRating.class);

    /** Rating-Boxes */
    private JComboBox[] _ratings = new JComboBox[6];

    /** Personal Rating */
    private Rating mPersonalRating;

    /** Overall Rating */
    private Rating mServerRating;

    /** Title of teh Program */
    private String _title;

    /** Original Title */
    private String _originaltitle;

    /** Rater Plugin */
    private TVRaterPlugin _rater;

    /** Empty Personal Rating */
    private boolean _emptyPersonal = false;

    /** ParentFrame */
    private Frame _parent;

    /** The Genre */
    private GenreComboBox _genre;  
    
    /** The Program that is rated */
    private Program _program = null;
    
    /**
     * Creates the DialgoRating
     * 
     * @param frame ParentFrame
     * @param rater TVRaterPlugin
     * @param programtitle the Title of the Program to rate
     */
    public DialogRating(Frame frame, TVRaterPlugin rater, String programtitle) {
        super(frame, true);
        _parent = frame;
        setTitle(_mLocalizer.msg("title", "View Rating"));

        _rater = rater;
        mServerRating = _rater.getDatabase().getServerRating(programtitle);
        mPersonalRating = _rater.getDatabase().getPersonalRating(programtitle);

        if (mPersonalRating == null) {
            _emptyPersonal = true;
            mPersonalRating = new Rating(programtitle);
        }

        _title = programtitle;
        _originaltitle = null;
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
        _parent = parent;
        setTitle(_mLocalizer.msg("title", "View Rating"));

        _rater = rater;
        mServerRating = _rater.getDatabase().getServerRating(program);
        mPersonalRating = rater.getDatabase().getPersonalRating(program);

        if (mPersonalRating == null) {
            _emptyPersonal = true;
            mPersonalRating = new Rating(program.getTitle());
        }

        _title = program.getTitle();
        _originaltitle = program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE);
        _program = program;
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

        JLabel title = new JLabel(_title);
        title.setPreferredSize(new Dimension(400, 40));

        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 20));
        titlePanel.add(title);

        if (_originaltitle != null) {
            JLabel original = new JLabel("(" + _originaltitle + ")");
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

        JButton rate = new JButton(_mLocalizer.msg("rate", "Rate"));

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
        
        JButton showListButton = new JButton(new ListAction(this, _title, true, false));

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
                    _mLocalizer.msg("allRatings", "Please set all Ratings"));
            return;
        }
        
        int[] values = new int[6];

        for (int i = 0; i < 6; i++) {
            values[i] = _ratings[i].getSelectedIndex();
        }

        mPersonalRating.setOverallRating(_ratings[0].getSelectedIndex());
        mPersonalRating.setActionRating(_ratings[1].getSelectedIndex());
        mPersonalRating.setFunRating(_ratings[2].getSelectedIndex());
        mPersonalRating.setEroticRating(_ratings[3].getSelectedIndex());
        mPersonalRating.setTensionRating(_ratings[4].getSelectedIndex());
        mPersonalRating.setEntitlementRating(_ratings[5].getSelectedIndex());
        mPersonalRating.setGenre(Integer.parseInt(_genre.getSelectedItem().toString()));
        
        _rater.getDatabase().setPersonalRating(mPersonalRating);
        setVisible(false);

        if (_rater.getUpdateInterval() == UpdateInterval.OnRating) {
          _rater.runUpdate(true, null);
        }
        
        // refresh the plugin tree as we may need to remove the just rated program
        _rater.updateRootNode();
    }

    /**
     * Checks if all ratings are checked 
     * @return true if all ratings are checked
     */
    private boolean checkAllRatings() {
        for (JComboBox _rating : _ratings) {
            if (_rating.getSelectedItem() == null) {
                return false;
            }
        }

        return _genre.getSelectedItem() != null;
    }

    /**
     * Creates the RatingPanel
     * 
     * @param rating Rating to use
     * @return RatingPanel
     */
    private JPanel createRatingPanel(Rating rating) {
        JPanel ratingPanel = new JPanel();

        String titel = _mLocalizer.msg("overallRating", "Overall Rating");
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

            ratingPanel.add(new JLabel(_mLocalizer.msg("overall", "Overall") + ":", SwingConstants.LEFT), labc);
            ratingPanel.add(createRatingBox(rating, Rating.OVERALL_RATING_KEY), c);

            ratingPanel.add(new JLabel(_mLocalizer.msg("action", "Action") + ":", SwingConstants.LEFT), labc);
            ratingPanel.add(createRatingBox(rating, Rating.ACTION_RATING_KEY), c);

            ratingPanel.add(new JLabel(_mLocalizer.msg("fun", "Fun") + ":", SwingConstants.LEFT), labc);
            ratingPanel.add(createRatingBox(rating, Rating.FUN_RATING_KEY), c);

            ratingPanel.add(new JLabel(_mLocalizer.msg("erotic", "Erotic") + ":", SwingConstants.LEFT), labc);
            ratingPanel.add(createRatingBox(rating, Rating.EROTIC_RATING_KEY), c);

            ratingPanel.add(new JLabel(_mLocalizer.msg("tension", "Tension") + ":", SwingConstants.LEFT), labc);
            ratingPanel.add(createRatingBox(rating, Rating.TENSION_RATING_KEY), c);

            ratingPanel.add(new JLabel(_mLocalizer.msg("entitlement", "Entitlement") + ":", SwingConstants.LEFT), labc);
            ratingPanel.add(createRatingBox(rating, Rating.ENTITLEMENT_RATING_KEY), c);
            
            ratingPanel.add(new JLabel(_mLocalizer.msg("genre", "Genre") + ":", SwingConstants.LEFT), labc);
            
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
            
            if ((_program != null) && !_rater.isProgramRateable(_program)) {
                pane.setText(MessageFormat.format("<center style=''font-family: helvetica''>{0}", _mLocalizer.msg("tooshort", "Program too short for rating. <br>The minimum length is {0} min.<br>This reduces traffic on the server.", TVRaterPlugin.MINLENGTH)));
            } else {
                pane.setText("<center style='font-family: helvetica'>"
                        + _mLocalizer.msg("doesntexist", "Sorry, rating doesn't exist!") + "</center>");
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
    private Component createRatingBox(Rating rating, int type) {
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

        String text = _mLocalizer.msg("yourRating", "Your Rating");

        if (_emptyPersonal) {
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

        voting.add(new JLabel(_mLocalizer.msg("overall", "Overall") + ":"), labc);
        voting.add(createVotingBox(mPersonalRating, Rating.OVERALL_RATING_KEY, 0), c);

        voting.add(new JLabel(_mLocalizer.msg("action", "Action") + ":"), labc);
        voting.add(createVotingBox(mPersonalRating, Rating.ACTION_RATING_KEY, 1), c);

        voting.add(new JLabel(_mLocalizer.msg("fun", "Fun") + ":"), labc);
        voting.add(createVotingBox(mPersonalRating, Rating.FUN_RATING_KEY, 2), c);

        voting.add(new JLabel(_mLocalizer.msg("erotic", "Erotic") + ":"), labc);
        voting.add(createVotingBox(mPersonalRating, Rating.EROTIC_RATING_KEY, 3), c);

        voting.add(new JLabel(_mLocalizer.msg("tension", "Tension") + ":"), labc);
        voting.add(createVotingBox(mPersonalRating, Rating.TENSION_RATING_KEY, 4), c);

        voting.add(new JLabel(_mLocalizer.msg("entitlement", "Entitlement") + ":"), labc);
        voting.add(createVotingBox(mPersonalRating, Rating.ENTITLEMENT_RATING_KEY, 5), c);

        voting.add(new JLabel(_mLocalizer.msg("genre", "Genre") + ":"), labc);
        
        _genre = new GenreComboBox(mPersonalRating.getGenre());
        voting.add(_genre, c);

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

        _ratings[ratingbox] = valuebox;

        return valuebox;
    }

    public void close() {
      setVisible(false);
    }

}