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
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import util.ui.LinkButton;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * This Dialog shows an overview of all Ratings in this Database
 * 
 * @author bodo tasche
 */
public class DialogOverview extends JDialog implements WindowClosingIf {

  /** Localizer */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(DialogOverview.class);

  /** The TVRaterPlugin with the Database */
  private TVRaterPlugin mPlugin;

  /** The tabbed Pane */
  private JTabbedPane mTabbedPane;

  /** List of personal Ratings */
  private JList mPersonalRatings;

  /** List of overall Ratings */
  private JList mOverallRatings;

  /** Der Update-Button */
  private JButton mBtnUpdate;

  /**
   * Creates the Overview Dialog
   * 
   * @param parent Parent-Frame
   * @param tvraterPlugin Database to use
   */
  public DialogOverview(Frame parent, TVRaterPlugin tvraterPlugin) {
    super(parent, true);
    setTitle(mLocalizer.msg("title", "Rating-Overview"));

    mPlugin = tvraterPlugin;

    createGUI();
  }

  /**
   * Creates the GUI
   */
  private void createGUI() {
    UiUtilities.registerForClosing(this);
    
    JPanel panel = (JPanel) getContentPane();
    panel.setLayout(new BorderLayout());

    RatingComparator comperator = new RatingComparator();

    Vector<Rating> overallData = new Vector<Rating>(mPlugin.getDatabase().getServerRatings());
    Collections.sort(overallData, comperator);

    mTabbedPane = new JTabbedPane();

    mOverallRatings = new JList(overallData);
    mOverallRatings.setCellRenderer(new RatingCellRenderer());
    mOverallRatings.addMouseListener(new MouseAdapter() {

      public void mousePressed(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          showPopUpMenu(evt);
        }
      }

      public void mouseReleased(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          showPopUpMenu(evt);
        }
      }

      public void mouseClicked(MouseEvent e) {
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
          view();
        }
        super.mouseClicked(e);
      }
    });

    mTabbedPane.addTab(mLocalizer.msg("overall", "Overall Ratings"), new JScrollPane(mOverallRatings));

    Vector<Rating> personalData = new Vector<Rating>(mPlugin.getDatabase().getPersonalRatings());
    Collections.sort(personalData, comperator);

    mPersonalRatings = new JList(personalData);
    mPersonalRatings.setCellRenderer(new RatingCellRenderer());
    mPersonalRatings.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          showPopUpMenu(evt);
        }
      }

      public void mouseReleased(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          showPopUpMenu(evt);
        }
      }
      
      public void mouseClicked(MouseEvent e) {
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
          view();
        }
        super.mouseClicked(e);
      }
    });

    mTabbedPane.addTab(mLocalizer.msg("personal", "Your Ratings"), new JScrollPane(mPersonalRatings));

    panel.add(mTabbedPane, BorderLayout.CENTER);

    JPanel buttonpanel = new JPanel(new GridBagLayout());

    GridBagConstraints c4 = new GridBagConstraints();
    c4.gridwidth = GridBagConstraints.REMAINDER;
    c4.fill = GridBagConstraints.HORIZONTAL;
    c4.weightx = 1;
    c4.anchor = GridBagConstraints.CENTER;

    LinkButton linkButton = new LinkButton("http://tvaddicted.de");
    buttonpanel.add(linkButton, c4);

    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(5, 5, 5, 5);

    mBtnUpdate = new JButton(mLocalizer.msg("update", "Update"));
    buttonpanel.add(mBtnUpdate, c);
    mBtnUpdate.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        update();
      }
    });

    GridBagConstraints c2 = new GridBagConstraints();
    c2.weightx = 1;
    c2.fill = GridBagConstraints.HORIZONTAL;

    buttonpanel.add(new JPanel(), c2);

    JButton view = new JButton(mLocalizer.msg("view", "View"));
    buttonpanel.add(view, c);
    view.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        view();
      }
    });

    JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    buttonpanel.add(close, c);
    close.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });

    getRootPane().setDefaultButton(close);

    panel.add(buttonpanel, BorderLayout.SOUTH);
  }

  /**
   * Shows the PopUp
   * 
   * @param e MouseEvent
   */
  protected void showPopUpMenu(MouseEvent e) {
    if (!(e.getSource() instanceof JList)) {
      return;
    }

    JList list = (JList) e.getSource();

    int i = list.locationToIndex(e.getPoint());
    list.setSelectedIndex(i);

    JPopupMenu menu = new JPopupMenu();

    Rating selRating = (Rating) list.getSelectedValue();

    JMenuItem item = new JMenuItem(mLocalizer.msg("showDetails", "Show Details"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        view();
      }
    });
    item.setFont(item.getFont().deriveFont(Font.BOLD));

    menu.add(item);
    menu.add(new ListAction(this, selRating.getTitle()));
    menu.add(new ShowDetailsAction(selRating.getRatingId()));

    menu.show(list, e.getX(), e.getY());

  }

  /**
   * Creates a DialogRating with the selected Rating
   */
  protected void view() {
    if ((mTabbedPane.getSelectedIndex() == 0) && (mOverallRatings.getSelectedValue() != null)) {

      DialogRating dlg = new DialogRating(mPlugin.getParentFrameForTVRater(), mPlugin, ((Rating) mOverallRatings
          .getSelectedValue()).getTitle());

      UiUtilities.centerAndShow(dlg);

      updateLists();
    } else if ((mTabbedPane.getSelectedIndex() == 1) && (mPersonalRatings.getSelectedValue() != null)) {
      DialogRating dlg = new DialogRating(mPlugin.getParentFrameForTVRater(), mPlugin,
          ((Rating) mPersonalRatings.getSelectedValue()).getTitle());
      UiUtilities.centerAndShow(dlg);
      updateLists();
    }
  }

  /**
   * Updates the Rating-Lists
   */
  private void updateLists() {
    RatingComparator comperator = new RatingComparator();

    Vector<Rating> personalVector = new Vector<Rating>(mPlugin.getDatabase().getPersonalRatings());
    Collections.sort(personalVector, comperator);
    int index = mPersonalRatings.getSelectedIndex();
    mPersonalRatings.setListData(personalVector);
    mPersonalRatings.setSelectedIndex(index);

    Vector<Rating> overallVector = new Vector<Rating>(mPlugin.getDatabase().getServerRatings());
    Collections.sort(overallVector, comperator);
    index = mOverallRatings.getSelectedIndex();
    mOverallRatings.setListData(overallVector);
    mOverallRatings.setSelectedIndex(index);

  }

  /**
   * Updates the Database from the Server
   */
  protected void update() {
    mBtnUpdate.setEnabled(false);
    mPlugin.runUpdate(true, new Runnable() {

      @Override
      public void run() {
        mBtnUpdate.setEnabled(true);
      }});
  }

  public void close() {
    setVisible(false);
  }
}