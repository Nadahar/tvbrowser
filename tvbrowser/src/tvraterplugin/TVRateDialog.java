/*
 * Created on 02.12.2003
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

import util.ui.Localizer;
import util.ui.TabLayout;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * @author bodo
 */
public class TVRateDialog extends JDialog {

	private static final Localizer _mLocalizer = Localizer.getLocalizerFor(TVRateDialog.class);

	private JComboBox[] _ratings = new JComboBox[6];

	private Program _program;

	/**
	 * @param parent
	 * @param program
	 */
	public TVRateDialog(Frame parent, Program program) {
		super(parent, true);
		setTitle(_mLocalizer.msg("title", "View Rating"));

		_program = program;

		JPanel panel = (JPanel) this.getContentPane();

		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		JPanel titlePanel = new JPanel(new TabLayout(1));
		titlePanel.setBackground(Color.white);

		JLabel title = new JLabel(program.getTitle());
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setFont(new Font("Helvetica", Font.BOLD, 20));
		titlePanel.add(title);

		if (program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE) != null) {
			JLabel original = new JLabel("(" + program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE) + ")");
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

		panel.add(createRatingPanel(), c);

		panel.add(createVotingPanel(), c);

		JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));

		JButton rate = new JButton(_mLocalizer.msg("rate", "Rate"));

		rate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] values = new int[6];
				
				for (int i = 0; i < 6; i++) {
					values[i] = _ratings[i].getSelectedIndex();
				}
				
				TVRaterPlugin.tvraterDB.setPersonalRating(_program, values);
				
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

	private JPanel createRatingPanel() {
		JPanel rating = new JPanel(new GridLayout(1, 6));
		rating.setBorder(BorderFactory.createTitledBorder(_mLocalizer.msg("overallRating", "Overall Rating")));

		int[] values = TVRaterPlugin.tvraterDB.getOverallRating(_program);

		rating.add(createRatingBox(_mLocalizer.msg("overall", "Overall") + ":", values[0]));
		rating.add(createRatingBox(_mLocalizer.msg("action", "Action") + ":", values[1]));
		rating.add(createRatingBox(_mLocalizer.msg("fun", "Fun") + ":", values[2]));
		rating.add(createRatingBox(_mLocalizer.msg("erotic", "Erotic") + ":", values[3]));
		rating.add(createRatingBox(_mLocalizer.msg("tension", "Tension") + ":", values[4]));
		rating.add(createRatingBox(_mLocalizer.msg("entitlement", "Entitlement") + ":", values[5]));

		return rating;
	}

	/**
	 * @param string
	 * @param i
	 * @return
	 */
	private Component createRatingBox(String string, int i) {
		JPanel box = new JPanel(new BorderLayout());
		
		box.add(new JLabel(string), BorderLayout.NORTH);
		
		if (i > -1) {
			box.add(new JLabel(Integer.toString(i) + "/5", JLabel.CENTER), BorderLayout.CENTER);
		} else {
			box.add(new JLabel("---", JLabel.CENTER), BorderLayout.CENTER);
		}
		
		box.setBorder(BorderFactory.createEtchedBorder());
		
		return box;
	}

	private JPanel createVotingPanel() {
		JPanel voting = new JPanel(new GridLayout(1, 6));
		voting.setBorder(BorderFactory.createTitledBorder(_mLocalizer.msg("yourRating", "Your Rating")));

		int[] values = TVRaterPlugin.tvraterDB.getPersonalRating(_program);

		voting.add(createVotingBox(_mLocalizer.msg("overall", "Overall") + ":", values[0], 0));
		voting.add(createVotingBox(_mLocalizer.msg("action", "Action") + ":", values[1], 1));
		voting.add(createVotingBox(_mLocalizer.msg("fun", "Fun") + ":", values[2], 2));
		voting.add(createVotingBox(_mLocalizer.msg("erotic", "Erotic") + ":", values[3], 3));
		voting.add(createVotingBox(_mLocalizer.msg("tension", "Tension") + ":", values[4], 4));
		voting.add(createVotingBox(_mLocalizer.msg("entitlement", "Entitlement") + ":", values[5], 5));

		return voting;
	}

	/**
	 * @param string
	 * @param i
	 * @return
	 */
	private Component createVotingBox(String string, int i, int ratingbox) {
		JPanel box = new JPanel(new BorderLayout());
		
		box.add(new JLabel(string), BorderLayout.NORTH);
		
		String[] text = {"0/5", "1/5", "2/5", "3/5", "4/5", "5/5"};
		
		JComboBox value = new JComboBox(text);
		value.setSelectedIndex(i);
		
		_ratings[ratingbox] = value;
		box.add(value, BorderLayout.CENTER);
		
		box.setBorder(BorderFactory.createEtchedBorder());
		
		return box;
	}

}