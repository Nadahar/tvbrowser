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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import tvbrowser.core.Settings;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the Main-Class for the Config Assistent
 */
public class ConfigAssistant extends JDialog implements ActionListener, PrevNextButtons, WindowClosingIf {

  private JButton mNextBt, mBackBt, mCancelBt;

  transient private CardPanel mCurCardPanel, mFinishedPanel;

  private JPanel mCardPn;

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ConfigAssistant.class);

  public ConfigAssistant(JFrame parent) {
    super(parent, true);

    UiUtilities.registerForClosing(this);
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        close();
      }
    });

    File tvDataDir = new File(Settings.propTVDataDirectory.getString().trim());
    Settings.propTVDataDirectory.setString(tvDataDir.toString().replaceAll("\\\\","/"));
    TvDataServiceProxyManager.getInstance().setTvDataDir(tvDataDir);

    setTitle(mLocalizer.msg("title", "Setup assistant"));
    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(new FormLayout("fill:250px:grow", "fill:pref:grow, 1px, pref"));

    JPanel centerPanel = new JPanel(new BorderLayout());

    mCardPn = new JPanel(new CardLayout());

    mBackBt = new JButton("<< " + Localizer.getLocalization(Localizer.I18N_BACK));
    mNextBt = new JButton(Localizer.getLocalization(Localizer.I18N_NEXT) + " >>");
    mCancelBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));

    mBackBt.setEnabled(false);

    mBackBt.addActionListener(this);
    mNextBt.addActionListener(this);
    mCancelBt.addActionListener(this);

    CardPanel welcomePanel = new WelcomeCardPanel(this);
    CardPanel networkPanel = new NetworkCardPanel(this);
    AuthenticationChannelCardPanel authenticationChannelCardPanel = new AuthenticationChannelCardPanel(this);
    CardPanel subscribeChannelPanel = new SubscribeChannelCardPanel(this);
    CardPanel networkSuccessPanel = new NetworkSuccessPanel(this,mCardPn,authenticationChannelCardPanel,subscribeChannelPanel);    

    mFinishedPanel = new FinishCardPanel(this);

    mCardPn.add(welcomePanel.getPanel(), welcomePanel.toString());
    mCardPn.add(networkPanel.getPanel(), networkPanel.toString());
    mCardPn.add(networkSuccessPanel.getPanel(), networkSuccessPanel.toString());
    mCardPn.add(mFinishedPanel.getPanel(), mFinishedPanel.toString());
    mCardPn.add(subscribeChannelPanel.getPanel(), subscribeChannelPanel.toString());

    boolean dynamicChannelList = isDynamicChannelListSupported();

    welcomePanel.setNext(networkPanel);
    if (dynamicChannelList) {
      networkPanel.setNext(networkSuccessPanel);
      networkSuccessPanel.setNext(subscribeChannelPanel);
      subscribeChannelPanel.setPrev(networkSuccessPanel);
      subscribeChannelPanel.setNext(mFinishedPanel);
      mFinishedPanel.setPrev(subscribeChannelPanel);
    } else {
      networkPanel.setNext(networkSuccessPanel);
      networkSuccessPanel.setNext(subscribeChannelPanel);
      subscribeChannelPanel.setNext(mFinishedPanel);
      mFinishedPanel.setPrev(subscribeChannelPanel);
    }

    mCurCardPanel = welcomePanel;

    FormLayout layout = new FormLayout("fill:pref:grow, pref, 3dlu, pref, 3dlu, pref", "pref");
    layout.setColumnGroups(new int[][] { { 2, 4, 6 } });
    JPanel buttonPanel = new JPanel(layout);

    buttonPanel.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();
    buttonPanel.add(mBackBt, cc.xy(2, 1));
    buttonPanel.add(mNextBt, cc.xy(4, 1));
    buttonPanel.add(mCancelBt, cc.xy(6, 1));

    centerPanel.add(mCardPn, BorderLayout.CENTER);

    contentPane.add(centerPanel, cc.xy(1, 1));

    JPanel black = new JPanel();
    black.setBackground(mCancelBt.getForeground());
    contentPane.add(black, cc.xy(1, 2));

    contentPane.add(buttonPanel, cc.xy(1, 3));

    setSize(700, 500);

  }

  private boolean isDynamicChannelListSupported() {
    TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance().getDataServices();
    for (TvDataServiceProxy service : services) {
      if (service.supportsDynamicChannelList()) {
        return true;
      }
    }
    return false;
  }

  public void actionPerformed(final ActionEvent e) {
    new Thread(new Runnable() {
      public void run() {
        boolean next = mNextBt.isEnabled();
        boolean back = mBackBt.isEnabled();
        boolean cancel = mCancelBt.isEnabled();
        mNextBt.setEnabled(false);
        mBackBt.setEnabled(false);
        mCancelBt.setEnabled(false);

        Object o = e.getSource();
        if (o == mBackBt) {
          if (mCurCardPanel == mFinishedPanel) {
            mCancelBt.setVisible(true);
            mNextBt.setText(Localizer.getLocalization(Localizer.I18N_NEXT) + " >>");
          }

          if (!mCurCardPanel.onPrev()) {
            mNextBt.setEnabled(next);
            mBackBt.setEnabled(back);
            mCancelBt.setEnabled(cancel);
            return;
          }
          mCurCardPanel = mCurCardPanel.getPrev();
          CardLayout cl = (CardLayout) mCardPn.getLayout();

          mCurCardPanel.onShow();
          cl.show(mCardPn, mCurCardPanel.toString());
          mCancelBt.setEnabled(true);
        } else if (o == mNextBt) {
          if (mCurCardPanel == mFinishedPanel) {
            tvbrowser.core.Settings.propShowAssistant.setBoolean(false);
            setVisible(false);
          } else {
            if (!mCurCardPanel.onNext()) {
              mNextBt.setEnabled(next);
              mBackBt.setEnabled(back);
              mCancelBt.setEnabled(cancel);
              return;
            }
            mCurCardPanel = mCurCardPanel.getNext();
            CardLayout cl = (CardLayout) mCardPn.getLayout();

            mCurCardPanel.onShow();

            cl.show(mCardPn, mCurCardPanel.toString());

            mCancelBt.setEnabled(true);
            if (mCurCardPanel == mFinishedPanel) {
              mCancelBt.setVisible(false);
              mNextBt.setText(mLocalizer.msg("finish", "Finish"));
              mNextBt.setEnabled(true);
              mBackBt.setEnabled(true);
            }
          }

        } else if (o == mCancelBt) {
          cancel();
          mNextBt.setEnabled(next);
          mBackBt.setEnabled(back);
          mCancelBt.setEnabled(cancel);
        }

      }
    }).start();

  }

  private void cancel() {
    JCheckBox notShowAgain = new JCheckBox(mLocalizer.msg("notShowAgain", "Don't show assistant again."));
    notShowAgain.setSelected(!tvbrowser.core.Settings.propShowAssistant.getBoolean());

    Object[] values = { mLocalizer.msg("cancelDlg", "message"), notShowAgain };
    Object[] buttons = { mLocalizer.msg("button.1", "Close assistant"),
        mLocalizer.msg("button.2", "Continue configuration") };

    int selectedValue = JOptionPane.showOptionDialog(this, values, mLocalizer.msg("cancelDlg.title", "title"),
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, buttons, buttons[1]);

    if (selectedValue == 0) {
      tvbrowser.core.Settings.propShowAssistant.setBoolean(!notShowAgain.isSelected());
      setVisible(false);
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
      setVisible(false);
    } else {
      cancel();
    }
  }

}