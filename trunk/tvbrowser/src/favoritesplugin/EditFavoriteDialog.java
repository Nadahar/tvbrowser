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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package favoritesplugin;

import java.util.Calendar;
import java.util.Date;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import util.exc.*;
import util.ui.*;

import devplugin.*;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class EditFavoriteDialog {

  /** The localizer for this class. */  
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(EditFavoriteDialog.class);

  private Favorite mFavorite;
  private Channel[] mSubscribedChannelArr;

  private JDialog mDialog;
  private JTextField mTermTF;
  private JCheckBox mSearchTitleChB, mSearchInTextChB;
  private JRadioButton mMatchExactlyRB, mMatchSubstringRB, mRegexRB;
  private JCheckBox mCertainChannelChB, mCertainTimeOfDayChB;
  private JComboBox mCertainChannelCB;
  private JSpinner mCertainFromTimeSp, mCertainToTimeSp;
  private JLabel mCertainToTimeLabel;
  private JButton mOkBt, mCancelBt;
  
  private boolean mOkWasPressed = false;
  
  
  
  /** 
   * Creates a new instance of EditFavoriteDialog.
   */
  public EditFavoriteDialog(Component parent, Favorite favorite) {
    mFavorite = favorite;
    mSubscribedChannelArr = Plugin.getPluginManager().getSubscribedChannels();

    mDialog = UiUtilities.createDialog(parent, true);
    mDialog.setTitle(mLocalizer.msg("dlgTitle", "Edit favorite program"));
    
    JPanel main = new JPanel(new TabLayout(1));
    main.setBorder(UiUtilities.DIALOG_BORDER);
    mDialog.setContentPane(main);
    
    JPanel p1, p2;
    String msg;
    
    // term
    p1 = new JPanel(new TabLayout(2));
    main.add(p1);
    
    p1.add(new JLabel(mLocalizer.msg("term", "Term")));
    mTermTF = new JTextField(15);
    p1.add(mTermTF);
    
    // search in
    p1 = new JPanel(new TabLayout(1));
    msg = mLocalizer.msg("searchIn", "Search in");
    p1.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(p1);

    msg = mLocalizer.msg("title", "Title");
    mSearchTitleChB = new JCheckBox(msg);
    p1.add(mSearchTitleChB);

    msg = mLocalizer.msg("infoText", "Information text");
    mSearchInTextChB = new JCheckBox(msg);
    p1.add(mSearchInTextChB);
    
    // options
    p1 = new JPanel(new TabLayout(1));
    msg = mLocalizer.msg("options", "Options");
    p1.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(p1);
    
    ButtonGroup bg = new ButtonGroup();
    msg = mLocalizer.msg("matchExactly", "Match exactly");
    mMatchExactlyRB = new JRadioButton(msg);
    bg.add(mMatchExactlyRB);
    p1.add(mMatchExactlyRB);
    
    msg = mLocalizer.msg("matchSubstring", "Term is a keyword");
    mMatchSubstringRB = new JRadioButton(msg);
    bg.add(mMatchSubstringRB);
    p1.add(mMatchSubstringRB);
    
    msg = mLocalizer.msg("matchRegex", "Term is a regular expression");
    mRegexRB = new JRadioButton(msg);
    bg.add(mRegexRB);
    p1.add(mRegexRB);
    
    // limitations
    p1 = new JPanel(new TabLayout(2));
    msg = mLocalizer.msg("limitations", "Limitations");
    p1.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(p1);
    
    msg = mLocalizer.msg("certainChannel", "Certain channel");
    mCertainChannelChB = new JCheckBox(msg);
    mCertainChannelChB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        updateEnabled();
      }
    });
    p1.add(mCertainChannelChB);
    
    mCertainChannelCB = new JComboBox(mSubscribedChannelArr);
    mCertainChannelCB.setRenderer(new ChannelListCellRenderer());
    p1.add(mCertainChannelCB);

    msg = mLocalizer.msg("certainTimeOfDay", "Certain time of day");
    mCertainTimeOfDayChB = new JCheckBox(msg);
    mCertainTimeOfDayChB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        updateEnabled();
      }
    });
    p1.add(mCertainTimeOfDayChB);
    
    p2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    p1.add(p2);
    String timePattern = mLocalizer.msg("timePattern", "hh:mm a");
    
    mCertainFromTimeSp = new JSpinner(new SpinnerDateModel());
    mCertainFromTimeSp.setEditor(new JSpinner.DateEditor(mCertainFromTimeSp, timePattern));
    mCertainFromTimeSp.setBorder(null);
    p2.add(mCertainFromTimeSp);
    
    msg = mLocalizer.msg("timeTo", "to");
    mCertainToTimeLabel = new JLabel("  " + msg + "  ");
    p2.add(mCertainToTimeLabel);

    mCertainToTimeSp = new JSpinner(new SpinnerDateModel());
    mCertainToTimeSp.setEditor(new JSpinner.DateEditor(mCertainToTimeSp, timePattern));
    mCertainToTimeSp.setBorder(null);
    p2.add(mCertainToTimeSp);

    // buttons
    JPanel buttonPn = new JPanel();
    main.add(buttonPn);
    
    mOkBt = new JButton(mLocalizer.msg("ok", "OK"));
    mOkBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (saveValues()) {
          mOkWasPressed = true;
          mDialog.dispose();
        }
      }
    });
    buttonPn.add(mOkBt);
    mDialog.getRootPane().setDefaultButton(mOkBt);

    mCancelBt = new JButton(mLocalizer.msg("cancel", "Cancel"));
    mCancelBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        mDialog.dispose();
      }
    });
    buttonPn.add(mCancelBt);
    
    loadValues();
    mDialog.pack();
  }
  
  
  
  public void centerAndShow() {
    UiUtilities.centerAndShow(mDialog);
  }

  
  
  private void updateEnabled() {
    boolean enabled = mCertainChannelChB.isSelected();
    mCertainChannelCB.setEnabled(enabled);

    enabled = mCertainTimeOfDayChB.isSelected();
    mCertainFromTimeSp.setEnabled(enabled);
    mCertainToTimeLabel.setEnabled(enabled);
    mCertainToTimeSp.setEnabled(enabled);
  }

  
  
  private void loadValues() {
    mTermTF.setText(mFavorite.getTerm());
    mSearchTitleChB.setSelected(mFavorite.getSearchInTitle());
    mSearchInTextChB.setSelected(mFavorite.getSearchInText());
    
    int searchMode = mFavorite.getSearchMode();
    mMatchExactlyRB.setSelected(searchMode == Favorite.MODE_MATCH_EXACTLY);
    mMatchSubstringRB.setSelected(searchMode == Favorite.MODE_TERM_IS_KEYWORD);
    mRegexRB.setSelected(searchMode == Favorite.MODE_TERM_IS_REGEX);

    mCertainChannelChB.setSelected(mFavorite.getUseCertainChannel());
    Channel certainChannel = mFavorite.getCertainChannel();
    mCertainChannelCB.setSelectedItem(certainChannel);
    
    mCertainTimeOfDayChB.setSelected(mFavorite.getUseCertainTimeOfDay());
    Calendar cal = Calendar.getInstance();
    
    int minutes = mFavorite.getCertainFromTime();
    cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
    cal.set(Calendar.MINUTE, minutes % 60);
    mCertainFromTimeSp.setValue(cal.getTime());
    
    minutes = mFavorite.getCertainToTime();
    cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
    cal.set(Calendar.MINUTE, minutes % 60);
    mCertainToTimeSp.setValue(cal.getTime());

    updateEnabled();
  }
  
  
  
  private boolean saveValues() {
    mFavorite.setTerm(mTermTF.getText());
    mFavorite.setSearchInTitle(mSearchTitleChB.isSelected());
    mFavorite.setSearchInText(mSearchInTextChB.isSelected());
    
    if (mMatchExactlyRB.isSelected()) {
      mFavorite.setSearchMode(Favorite.MODE_MATCH_EXACTLY);
    }
    else if (mMatchSubstringRB.isSelected()) {
      mFavorite.setSearchMode(Favorite.MODE_TERM_IS_KEYWORD);
    }
    else if (mRegexRB.isSelected()) {
      mFavorite.setSearchMode(Favorite.MODE_TERM_IS_REGEX);
    }

    mFavorite.setUseCertainChannel(mCertainChannelChB.isSelected());
    Channel certainChannel = (Channel)mCertainChannelCB.getSelectedItem();
    mFavorite.setCertainChannel(certainChannel);
    
    mFavorite.setUseCertainTimeOfDay(mCertainTimeOfDayChB.isSelected());
    Calendar cal = Calendar.getInstance();
    
    Date fromTime = (Date) mCertainFromTimeSp.getValue();
    cal.setTime(fromTime);
    int minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    mFavorite.setCertainFromTime(minutes);
    
    Date toTime = (Date) mCertainToTimeSp.getValue();
    cal.setTime(toTime);
    minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    mFavorite.setCertainToTime(minutes);
    
    try {
      mFavorite.updatePrograms();
      return true;
    }
    catch (TvBrowserException exc) {
      ErrorHandler.handle(exc);
      return false;
    }
  }
  
  
  
  public boolean getOkWasPressed() {
    return mOkWasPressed;
  }

  
  // inner class ChannelListCellRenderer
  
  
  class ChannelListCellRenderer extends DefaultListCellRenderer {
    
    public Component getListCellRendererComponent(JList list, Object value,
      int index, boolean isSelected, boolean cellHasFocus)
    {
      if (value instanceof Channel) {
        Channel channel = (Channel)value;
        value = channel.getName();
      }
      
      return super.getListCellRendererComponent(list, value, index, isSelected,
        cellHasFocus);
    }
    
  }
  
}
