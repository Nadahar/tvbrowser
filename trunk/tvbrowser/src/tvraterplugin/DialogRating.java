/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package tvraterplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import util.ui.Localizer;
import util.ui.TabLayout;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * This Dialog shows one Rating
 * 
 * @author bodo tasche
 */
public class DialogRating extends JDialog {
	/** Localizer */
	private static final Localizer _mLocalizer = Localizer.getLocalizerFor(DialogRating.class);
	/** Raiting-Boxes */
	private JComboBox[] _ratings = new JComboBox[6];

	/** Personal Rating */
	private Rating _personalrating;
	/** Overall Rating */
	private Rating _overallrating;
	
	/** Length of the Program */
	private int _length;
	/** Title of teh Program */
	private String _title;
	/** Original Title */
	private String _originaltitle;
	
	/** Database to use */
	private Database _tvraterDB;

	/**
	 * Creates the DialgoRating
	 * 
	 * @param parent ParentFrame
	 * @param programtitle the Title of the Program to rate
	 * @param tvraterDB the Database
	 */
	public DialogRating(Frame parent, String programtitle, Database tvraterDB) {
		super(parent, true);
		setTitle(_mLocalizer.msg("title", "View Rating"));

		_tvraterDB = tvraterDB;
		_overallrating = _tvraterDB.getOverallRating(programtitle);
		_personalrating = _tvraterDB.getPersonalRating(programtitle);
		
		if (_personalrating == null) {
			_personalrating = new Rating(programtitle);
		}
		
		_length = 90;
		_title = programtitle;
		_originaltitle = null;
		createGUI();
	}


	/**
	 * Creates the DialgoRating
	 * 
	 * @param parent Parent-Frame
	 * @param program Program to rate
	 * @param tvraterDB the Database
	 */
	public DialogRating(Frame parent, Program program, Database tvraterDB) {
		super(parent, true);
		setTitle(_mLocalizer.msg("title", "View Rating"));

		_tvraterDB = tvraterDB;
		_overallrating = _tvraterDB.getOverallRating(program);
		_personalrating = _tvraterDB.getPersonalRating(program);

		if (_personalrating == null) {
			_personalrating = new Rating(program.getTitle());
		}

		_length = program.getLength();
		_title = program.getTitle();
		_originaltitle = program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE);
		createGUI();
	}
	
	/**
	 * Creates the GUI.
	 */
	private void createGUI() {
		JPanel panel = (JPanel) this.getContentPane();

		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		JPanel titlePanel = new JPanel(new TabLayout(1));
		titlePanel.setBackground(Color.white);

		JLabel title = new JLabel(_title);
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setFont(new Font("Helvetica", Font.BOLD, 20));
		titlePanel.add(title);

		if (_originaltitle != null) {
			JLabel original = new JLabel("(" + _originaltitle + ")");
			original.setHorizontalAlignment(JLabel.CENTER);
			titlePanel.add(original);
		}

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;

		panel.add(titlePanel, c);

		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;

		panel.add(createRatingPanel(_overallrating), c);

		panel.add(createVotingPanel(_personalrating), c);

		JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));

		JButton rate = new JButton(_mLocalizer.msg("rate", "Rate"));

		rate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] values = new int[6];

				for (int i = 0; i < 6; i++) {
					values[i] = _ratings[i].getSelectedIndex();
				}

				_personalrating.setValue(Rating.OVERALL, _ratings[0].getSelectedIndex());
				_personalrating.setValue(Rating.ACTION, _ratings[1].getSelectedIndex());
				_personalrating.setValue(Rating.FUN, _ratings[2].getSelectedIndex());
				_personalrating.setValue(Rating.EROTIC, _ratings[3].getSelectedIndex());
				_personalrating.setValue(Rating.TENSION, _ratings[4].getSelectedIndex());
				_personalrating.setValue(Rating.ENTITLEMENT, _ratings[5].getSelectedIndex());
				
				_tvraterDB.setPersonalRating(_personalrating);
				hide();
			}
		});

		buttonpanel.add(rate);

		JButton cancel = new JButton(_mLocalizer.msg("cancel", "Cancel"));

		cancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
			}
		});

		buttonpanel.add(cancel);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.weighty = 0;
		c.insets = new Insets(0, 5, 5, 5);

		panel.add(buttonpanel, c);

		pack();
	}

	/**
	 * Creates the RatingPanel
	 * 
	 * @param rating Rating to use
	 * @return RatingPanel
	 */
	private JPanel createRatingPanel(Rating rating) {
		JPanel ratingPanel = new JPanel();
		ratingPanel.setBorder(BorderFactory.createTitledBorder(_mLocalizer.msg("overallRating", "Overall Rating")));

		
		if (rating != null) {
			ratingPanel.setLayout(new GridLayout(1, 6));			
			ratingPanel.add(createRatingBox(_mLocalizer.msg("overall", "Overall") + ":", rating.getIntValue(Rating.OVERALL)));
			ratingPanel.add(createRatingBox(_mLocalizer.msg("action", "Action") + ":", rating.getIntValue(Rating.ACTION)));
			ratingPanel.add(createRatingBox(_mLocalizer.msg("fun", "Fun") + ":", rating.getIntValue(Rating.FUN)));
			ratingPanel.add(createRatingBox(_mLocalizer.msg("erotic", "Erotic") + ":", rating.getIntValue(Rating.EROTIC)));
			ratingPanel.add(createRatingBox(_mLocalizer.msg("tension", "Tension") + ":", rating.getIntValue(Rating.TENSION)));
			ratingPanel.add(createRatingBox(_mLocalizer.msg("entitlement", "Entitlement") + ":", rating.getIntValue(Rating.ENTITLEMENT)));
		} else {
			ratingPanel.setLayout(new BorderLayout());			
			JTextPane pane = new JTextPane();
			pane.setContentType("text/html");
			
			if (_length < 75) {
				pane.setText("<center style='font-family: helvetica'>"+
							_mLocalizer.msg("tooshort", "Program too short for rating. The minimum lenght is 75 min.<br>This reduces traffic on the server.")
							+"</center>");
			} else {
				pane.setText("<center style='font-family: helvetica'>"+
							_mLocalizer.msg("doesntexist", "Sorry, rating doesn't exist!")
							+"</center>");
			}
			
			pane.setEditable(false);
			ratingPanel.add(pane, BorderLayout.CENTER);
		}


		return ratingPanel;
	}

	/**
	 * Creates a Ratingbox
	 * 
	 * @param name Name of the Rating-Element
	 * @param value Value to show (1-5)
	 * @return JPanel with rating-box
	 */
	private Component createRatingBox(String name, int value) {
		JPanel box = new JPanel(new BorderLayout());

		box.add(new JLabel(name), BorderLayout.NORTH);

		if (value > -1) {
			box.add(new JLabel(Integer.toString(value) + "/5", JLabel.CENTER), BorderLayout.CENTER);
		} else {
			box.add(new JLabel("---", JLabel.CENTER), BorderLayout.CENTER);
		}

		box.setBorder(BorderFactory.createEtchedBorder());

		return box;
	}

	/**
	 * Creates a voting-panel 
	 * @param rating Rating to use
	 * @return voting-panel
	 */
	private JPanel createVotingPanel(Rating rating) {
		JPanel voting = new JPanel(new GridLayout(1, 6));
		voting.setBorder(BorderFactory.createTitledBorder(_mLocalizer.msg("yourRating", "Your Rating")));

		voting.add(createVotingBox(_mLocalizer.msg("overall", "Overall") + ":", _personalrating.getIntValue(Rating.OVERALL), 0));
		voting.add(createVotingBox(_mLocalizer.msg("action", "Action") + ":", _personalrating.getIntValue(Rating.ACTION), 1));
		voting.add(createVotingBox(_mLocalizer.msg("fun", "Fun") + ":", _personalrating.getIntValue(Rating.FUN), 2));
		voting.add(createVotingBox(_mLocalizer.msg("erotic", "Erotic") + ":", _personalrating.getIntValue(Rating.EROTIC), 3));
		voting.add(createVotingBox(_mLocalizer.msg("tension", "Tension") + ":", _personalrating.getIntValue(Rating.TENSION), 4));
		voting.add(createVotingBox(_mLocalizer.msg("entitlement", "Entitlement") + ":", _personalrating.getIntValue(Rating.ENTITLEMENT), 5));

		return voting;
	}

	/**
	 * Creates a voting Box
	 * 
	 * @param name Name of the Rating-Element
	 * @param value Value to select
	 * @param ratingbox number of the Box
	 * @return a Panel with a Voting-Box
	 */
	private Component createVotingBox(String name, int value, int ratingbox) {
		JPanel box = new JPanel(new BorderLayout());

		box.add(new JLabel(name), BorderLayout.NORTH);

		String[] text = { "0/5", "1/5", "2/5", "3/5", "4/5", "5/5" };

		JComboBox valuebox = new JComboBox(text);
		valuebox.setSelectedIndex(value);

		_ratings[ratingbox] = valuebox;
		box.add(valuebox, BorderLayout.CENTER);

		box.setBorder(BorderFactory.createEtchedBorder());

		return box;
	}

}