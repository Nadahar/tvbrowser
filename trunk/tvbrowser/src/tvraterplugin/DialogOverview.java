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
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import util.ui.BrowserLauncher;
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

    /** The TVRaterPlugin with the Database */
    private TVRaterPlugin _tvraterPlugin;

    /** The tabbed Pane */
    private JTabbedPane _tabbed;

    /** List of personal Ratings */
    private JList _personal;

    /** List of overall Ratings */
    private JList _overall;
    
    /** Der Update-Button */
    private JButton _update;

    /**
     * Creates the Overview Dialog
     * 
     * @param parent Parent-Frame
     * @param tvraterDB Database to use
     */
    public DialogOverview(Frame parent, TVRaterPlugin tvraterPlugin) {
        super(parent, true);
        setTitle(_mLocalizer.msg("title", "Rating-Overview"));

        _tvraterPlugin = tvraterPlugin;

        createGUI();
    }

    /**
     * Creates the GUI
     */
    private void createGUI() {
        JPanel panel = (JPanel) getContentPane();
        panel.setLayout(new BorderLayout());

        RatingComparator comperator = new RatingComparator();

        Vector overallData = new Vector(_tvraterPlugin.getDatabase().getOverallRating());
        Collections.sort(overallData, comperator);

        _tabbed = new JTabbedPane();

        _overall = new JList(overallData);
        _overall.setCellRenderer(new RatingCellRenderer());
        _overall.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
                    view();
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    showPopUpMen(e);
                }
                super.mouseClicked(e);
            }
        });

        _tabbed.addTab(_mLocalizer.msg("overall", "Overall Ratings"), new JScrollPane(_overall));

        Vector personalData = new Vector(_tvraterPlugin.getDatabase().getPersonalRating());
        Collections.sort(personalData, comperator);

        _personal = new JList(personalData);
        _personal.setCellRenderer(new RatingCellRenderer());
        _personal.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
                    view();
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    showPopUpMen(e);
                }
                super.mouseClicked(e);
            }
        });

        _tabbed.addTab(_mLocalizer.msg("personal", "Your Ratings"), new JScrollPane(_personal));

        panel.add(_tabbed, BorderLayout.CENTER);

        JPanel buttonpanel = new JPanel(new GridBagLayout());

        GridBagConstraints c4 = new GridBagConstraints();
        c4.gridwidth = GridBagConstraints.REMAINDER;
        c4.fill = GridBagConstraints.HORIZONTAL;
        c4.weightx = 1;
        c4.anchor = GridBagConstraints.CENTER;
        
        JLabel urlLabel = new JLabel("<html><u>http://tvaddicted.wannawork.de</u></html>",JLabel.CENTER);
        urlLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        urlLabel.setForeground(Color.BLUE);
        urlLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                BrowserLauncher.openURL("http://tvaddicted.wannawork.de");
            }
        });
        
        buttonpanel.add(urlLabel, c4);
        
        
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(5, 5, 5, 5);

        _update = new JButton(_mLocalizer.msg("update", "Update"));
        buttonpanel.add(_update, c);
        _update.addActionListener(new java.awt.event.ActionListener() {

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

        getRootPane().setDefaultButton(close);

        panel.add(buttonpanel, BorderLayout.SOUTH);
    }

    /**
     * Shows the PopUp
     * @param e MouseEvent
     */
    protected void showPopUpMen(MouseEvent e) {
        if (!(e.getSource() instanceof JList)) {
            return;
        }
        
        JList list = (JList) e.getSource();
        
        int i = list.locationToIndex(e.getPoint());
        list.setSelectedIndex(i);
        
        JPopupMenu menu = new JPopupMenu();
        
        Rating selRating = (Rating) list.getSelectedValue();
        
        JMenuItem item = new JMenuItem(_mLocalizer.msg("showDetails", "Show Details"));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                view();
            }
        });
        item.setFont(item.getFont().deriveFont(Font.BOLD));
        
        menu.add(item);
        menu.add(new ListAction(this, selRating.getTitle()));
        menu.add(new ShowDetailsAction(selRating.getIntValue(Rating.ID)));
        
        menu.show(list, e.getX(), e.getY());
        
    }

    /**
     * Creates a DialogRating with the selected Rating
     */
    protected void view() {
        if ((_tabbed.getSelectedIndex() == 0) && (_overall.getSelectedValue() != null)) {

            DialogRating dlg = new DialogRating(_tvraterPlugin.getParentFrameForTVRater(), _tvraterPlugin,
                    ((Rating) _overall.getSelectedValue()).getTitle());

            UiUtilities.centerAndShow(dlg);
            
            updateLists();
        } else if ((_tabbed.getSelectedIndex() == 1) && (_personal.getSelectedValue() != null)) {
            DialogRating dlg = new DialogRating(_tvraterPlugin.getParentFrameForTVRater(), _tvraterPlugin,
                    ((Rating) _personal.getSelectedValue()).getTitle());
            UiUtilities.centerAndShow(dlg);
            updateLists();
        }
    }

    /**
     * Updates the Rating-Lists
     */
    private void updateLists() {
        RatingComparator comperator = new RatingComparator();

        Vector personalVector = new Vector((Collection) _tvraterPlugin.getDatabase().getPersonalRating());
        Collections.sort(personalVector, comperator);
        _personal.setListData(personalVector);

        Vector overallVector = new Vector((Collection) _tvraterPlugin.getDatabase().getOverallRating());
        Collections.sort(overallVector, comperator);
        _overall.setListData(overallVector);

    }

    /**
     * Updates the Database from the Server
     */
    protected void update() {
        Thread updateThread = new Thread() {

            public void run() {

                _update.setEnabled(false);
                System.out.println("Updater gestartet");
                Updater up = new Updater(_tvraterPlugin);
                up.run();

                updateLists();
                
                if (up.wasSuccessfull()) {
                    JOptionPane.showMessageDialog(_tvraterPlugin.getParentFrameForTVRater(), _mLocalizer.msg("updateSuccess",
                            "Update was successfull!"));
                }
                
                _update.setEnabled(true);
            };
        };
        updateThread.start();
    }
}