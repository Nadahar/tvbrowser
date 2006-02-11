/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.ui.configassistant;

import tvbrowser.core.Settings;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class ConfigAssistant extends JDialog implements ActionListener,
    PrevNextButtons, WindowClosingIf {

  private JButton mNextBt, mBackBt, mCancelBt;

  private CardPanel mCurCardPanel, mFinishedPanel;

  private JPanel mCardPn;

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ConfigAssistant.class);

  public ConfigAssistant(JFrame parent) {
    super(parent, true);

    UiUtilities.registerForClosing(this);
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        close();
      }
    });

    File tvDataDir = new File(Settings.propTVDataDirectory.getString().trim());
    Settings.propTVDataDirectory.setString(tvDataDir.toString());
    TvDataServiceProxyManager.getInstance().setTvDataDir(tvDataDir);

    setTitle(mLocalizer.msg("title", "Setup assistant"));
    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(new BorderLayout());

    JPanel centerPanel = new JPanel(new BorderLayout());

    mCardPn = new JPanel(new CardLayout());
    mCardPn.setBorder(BorderFactory.createEmptyBorder(3, 100, 10, 20));

    JPanel btnPanel = new JPanel(new BorderLayout());
    JPanel panel1 = new JPanel();

    mBackBt = new JButton("<< " + mLocalizer.msg("back", "back"));
    mNextBt = new JButton(mLocalizer.msg("next", "next") + " >>");
    mCancelBt = new JButton(mLocalizer.msg("cancel", "cancel"));

    mBackBt.setEnabled(false);

    mBackBt.addActionListener(this);
    mNextBt.addActionListener(this);
    mCancelBt.addActionListener(this);

    CardPanel welcomePanel = new WelcomeCardPanel(this);
    CardPanel proxyPanel = new ProxyCardPanel(this);
    CardPanel proxyQuestionPanel = new ProxyQuestionCardPanel(this, proxyPanel);

    CardPanel subscribeChannelPanel = new SubscribeChannelCardPanel(this);
    CardPanel downloadChannelListPanel = new DownloadChannelListCardPanel(this);
    mFinishedPanel = new FinishCardPanel(this);

    mCardPn.add(welcomePanel.getPanel(), welcomePanel.toString());
    mCardPn.add(proxyQuestionPanel.getPanel(), proxyQuestionPanel.toString());
    mCardPn.add(proxyPanel.getPanel(), proxyPanel.toString());
    mCardPn.add(mFinishedPanel.getPanel(), mFinishedPanel.toString());
    mCardPn.add(subscribeChannelPanel.getPanel(), subscribeChannelPanel
        .toString());

    boolean dynamicChannelList = isDynamicChannelListSupported();

    welcomePanel.setNext(proxyQuestionPanel);
    if (dynamicChannelList) {
      mCardPn.add(downloadChannelListPanel.getPanel(), downloadChannelListPanel
          .toString());

      proxyQuestionPanel.setNext(downloadChannelListPanel);
      proxyPanel.setNext(downloadChannelListPanel);
      downloadChannelListPanel.setNext(subscribeChannelPanel);
      downloadChannelListPanel.setPrev(proxyQuestionPanel);
      subscribeChannelPanel.setNext(mFinishedPanel);
    } else {
      proxyQuestionPanel.setNext(subscribeChannelPanel);
      proxyPanel.setNext(subscribeChannelPanel);
      subscribeChannelPanel.setNext(mFinishedPanel);
      subscribeChannelPanel.setPrev(proxyQuestionPanel);
    }

    mCurCardPanel = welcomePanel;

    panel1.add(mBackBt);
    panel1.add(mNextBt);

    JPanel panel2 = new JPanel();
    panel2.add(mCancelBt);

    centerPanel.add(mCardPn, BorderLayout.CENTER);

    btnPanel.add(panel1, BorderLayout.CENTER);
    btnPanel.add(panel2, BorderLayout.EAST);
    contentPane.add(btnPanel, BorderLayout.SOUTH);
    contentPane.add(centerPanel, BorderLayout.CENTER);

    setSize(530, 420);

  }

  private boolean isDynamicChannelListSupported() {
    TvDataServiceProxy services[] = TvDataServiceProxyManager.getInstance()
        .getDataServices();
    for (int i = 0; i < services.length; i++) {
      if (services[i].supportsDynamicChannelList())
        return true;
    }
    return false;
  }

  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o == mBackBt) {
      if (!mCurCardPanel.onPrev())
        return;
      mCurCardPanel = mCurCardPanel.getPrev();
      CardLayout cl = (CardLayout) mCardPn.getLayout();
      mCurCardPanel.onShow();
      cl.show(mCardPn, mCurCardPanel.toString());
    } else if (o == mNextBt) {
      if (!mCurCardPanel.onNext())
        return;
      mCurCardPanel = mCurCardPanel.getNext();
      CardLayout cl = (CardLayout) mCardPn.getLayout();
      mCurCardPanel.onShow();

      cl.show(mCardPn, mCurCardPanel.toString());

      if (mCurCardPanel == mFinishedPanel) {
        mCancelBt.setText(mLocalizer.msg("finish", "Finish"));
        mNextBt.setEnabled(false);
        mBackBt.setEnabled(false);
      }

    } else if (o == mCancelBt) {
      close();
    }

  }

  private void cancel() {
    JCheckBox notShowAgain = new JCheckBox(mLocalizer.msg("notShowAgain",
        "Don't show assistant again."));
    notShowAgain.setSelected(!tvbrowser.core.Settings.propShowAssistant.getBoolean());
    
    Object[] values = { mLocalizer.msg("cancelDlg", "message"), notShowAgain };
    Object[] buttons = { mLocalizer.msg("button.1", "Close assistant"),
        mLocalizer.msg("button.2", "Continue configuration") };
    
    int selectedValue = JOptionPane.showOptionDialog(this, values, mLocalizer
        .msg("cancelDlg.title", "title"), JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.WARNING_MESSAGE, null, buttons, buttons[1]);

    if (selectedValue == 0) {
      tvbrowser.core.Settings.propShowAssistant.setBoolean(!notShowAgain
          .isSelected());
      hide();
    }
  }

  public void enablePrevButton() {
    mBackBt.setEnabled(true);
  }

  public void enableNextButton() {
    mNextBt.setEnabled(true);
  }

  public void disablePrevButton() {
    mBackBt.setEnabled(false);
  }

  public void disableNextButton() {
    mNextBt.setEnabled(false);
  }

  public void close() {
    if (mCurCardPanel == mFinishedPanel) {
      tvbrowser.core.Settings.propShowAssistant.setBoolean(false);
      hide();
    } else
      cancel();
  }

}