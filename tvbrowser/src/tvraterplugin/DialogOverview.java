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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import util.ui.Localizer;
import util.ui.UiUtilities;

/**
 * This Dialog shows an overview of all Ratings in this Database
 * 
 * @author bodo tasche
 */
public class DialogOverview extends JDialog {
	/** Localizer */
	private static final Localizer _mLocalizer = Localizer.getLocalizerFor(DialogOverview.class);

	/** The Database to use */
	private Database _tvraterDB;
	/** The tabbed Pane */
	private JTabbedPane _tabbed;
	/** List of personal Ratings */
	private JList _personal;
	/** List of overall Ratings */
	private JList _overall;

	/**
	 * Creates the Overview Dialog
	 * @param parent Parent-Frame
	 * @param tvraterDB Database to use
	 */
	public DialogOverview(Frame parent, Database tvraterDB) {
		super(parent, true);
		setTitle(_mLocalizer.msg("title", "Rating-Overview"));

		_tvraterDB = tvraterDB;

		createGUI();
	}

	/**
	 * Creates the GUI
	 */
	private void createGUI() {
		JPanel panel = (JPanel) getContentPane();

		panel.setLayout(new BorderLayout());

		RatingComperator comperator = new RatingComperator();

		Vector overallVector = new Vector(_tvraterDB.getOverallRating());
		Collections.sort(overallVector, comperator);

		_tabbed = new JTabbedPane();

		_overall = new JList(overallVector);
		_overall.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
					view();
				}
				super.mouseClicked(e);
			}
		});

		_tabbed.addTab(_mLocalizer.msg("overall", "Overall Ratings"), new JScrollPane(_overall));

		Vector personalVector = new Vector(_tvraterDB.getPersonalRating());
		Collections.sort(personalVector, comperator);

		_personal = new JList(personalVector);
		_personal.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
					view();
				}
				super.mouseClicked(e);
			}
		});


		_tabbed.addTab(_mLocalizer.msg("personal", "Your Ratings"), new JScrollPane(_personal));

		panel.add(_tabbed, BorderLayout.CENTER);

		JPanel buttonpanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(5, 5, 5, 5);

		JButton update = new JButton(_mLocalizer.msg("update", "Update"));
		buttonpanel.add(update, c);
		update.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update();
			}
		});

		GridBagConstraints c2 = new GridBagConstraints();
		c2.weightx = 1;
		c2.fill = GridBagConstraints.HORIZONTAL;

		buttonpanel.add(new JPanel(), c2);

		JButton view = new JButton(_mLocalizer.msg("view", "View"));
		buttonpanel.add(view, c);
		view.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				view();
			}
		});

		JButton close = new JButton(_mLocalizer.msg("close", "Close"));
		buttonpanel.add(close, c);
		close.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
			}
		});

		panel.add(buttonpanel, BorderLayout.SOUTH);
	}

	/**
	 * Creates a DialogRating with the selected Rating
	 */
	protected void view() {
		if ((_tabbed.getSelectedIndex() == 0) && (_overall.getSelectedValue() != null)) {
			DialogRating dlg = new DialogRating((Frame)this.getParent(), ((Rating)_overall.getSelectedValue()).getTitle(), _tvraterDB);
			UiUtilities.centerAndShow(dlg);
		} else if ((_tabbed.getSelectedIndex() == 1) && (_personal.getSelectedValue() != null)) {
			DialogRating dlg = new DialogRating((Frame)this.getParent(), ((Rating)_personal.getSelectedValue()).getTitle(), _tvraterDB);
			UiUtilities.centerAndShow(dlg);
		}
	}

	/**
	 * Updates the Database from the Server
	 */
	protected void update() {
		Updater up = new Updater((Frame) this.getParent(), _tvraterDB);
		up.doUpdate();
	}
}