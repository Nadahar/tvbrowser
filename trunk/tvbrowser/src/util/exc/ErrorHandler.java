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

package util.exc;

import java.util.logging.Level;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import util.io.IOUtilities;

import util.ui.UiUtilities;
import util.ui.TextAreaIcon;

/**
 * This error handler should be used to show exceptions (like IOExceptions)
 * to the user. It provides showing a localized message, that says something
 * to the user, having the option to get extra information like the stack trace
 * and nested exceptions. The error is logged, too.
 *
 * @author  Til Schneider, www.murfman.de
 */
public class ErrorHandler {
  
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(ErrorHandler.class.getName());

  private static final Icon ERROR_ICON = UIManager.getIcon("OptionPane.errorIcon");
  
  private static JFrame mParent;
  
  

  public static void setFrame(JFrame parentFrame) {
    mParent = parentFrame;
  }
  
  
  
  public static void handle(TvBrowserException tvExc) {
    handle(tvExc.getLocalizedMessage(), tvExc);
  }
  
  
  
  public static void handle(String msg, Throwable thr) {
    mLog.log(Level.SEVERE, msg, thr);
    
    new ErrorDialog(mParent, msg, thr).centerAndShow();
  }
  
  
  // inner class ErrorDialog
  
  
  static class ErrorDialog {
    
    private JDialog mDialog;
    private JPanel mMainPn, mMessagePn, mDetailPn;
    private JButton mOkBt, mDetailBt;
    
    private boolean mDetailsVisible;
    
    private Throwable mThrowable;
    
    
    
    public ErrorDialog(Component parent, String msg, Throwable thr) {
      mThrowable = thr;
      
      mDialog = UiUtilities.createDialog(mParent, true);
      mDialog.setTitle("Fehler");
      
      mMainPn = new JPanel(new BorderLayout());
      mMainPn.setBorder(UiUtilities.DIALOG_BORDER);
      mDialog.setContentPane(mMainPn);
      
      // message panel
      mMessagePn = new JPanel(new BorderLayout(10, 10));
      mMainPn.add(mMessagePn, BorderLayout.CENTER);

      JLabel errorLb = new JLabel(ERROR_ICON);
      mMessagePn.add(errorLb, BorderLayout.WEST);
      int width = UiUtilities.getStringWidth(errorLb.getFont(), msg);
      width = Math.min(width, 400);
      Icon messageIcon = new TextAreaIcon(msg.toCharArray(), errorLb.getFont(), width);
      mMessagePn.add(new JLabel(messageIcon));
      
      // The buttons
      JPanel buttonPn = new JPanel();
      mMessagePn.add(buttonPn, BorderLayout.SOUTH);
      
      mOkBt = new JButton("OK");
      mOkBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          mDialog.dispose();
        }
      });
      buttonPn.add(mOkBt);
      mDialog.getRootPane().setDefaultButton(mOkBt);
      
      mDetailBt = new JButton("Details");
      mDetailBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          setDetailsVisible(! mDetailsVisible);
        }
      });
      buttonPn.add(mDetailBt);

      mDetailsVisible = true;
      setDetailsVisible(false);
      
      mDialog.pack();
    }


    
    public void centerAndShow() {
      UiUtilities.centerAndShow(mDialog);
    }
    
    
    
    private JPanel createDetailPanel() {
      JPanel detailPn = new JPanel(new BorderLayout(0, 10));

      detailPn.add(new JSeparator(), BorderLayout.NORTH);
      detailPn.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
      
      if (mThrowable.getCause() == null) {
        JComponent detailComp = createDetailComponent(mThrowable);
        detailPn.add(detailComp, BorderLayout.CENTER);
      } else {
        JTabbedPane tabbedPane = new JTabbedPane();
        detailPn.add(tabbedPane, BorderLayout.CENTER);
        
        Throwable thr = mThrowable;
        for (int i = 0; thr != null; i++) {
          JComponent detailComp = createDetailComponent(thr);
          
          tabbedPane.addTab("" + (i + 1), detailComp);
          
          thr = thr.getCause();
        }
      }
      
      return detailPn;
    }

    
    
    private JComponent createDetailComponent(Throwable thr) {
      String detailText = "";
      try {
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        
        thr.printStackTrace(printer);
        
        printer.close();
        writer.close();
        
        IOUtilities.replace(writer.getBuffer(), "\t", "  ");

        detailText = writer.toString();
      }
      catch (Exception exc) {}
      
      JTextArea textArea = new JTextArea(detailText);
      JScrollPane scrollPane = new JScrollPane(textArea);
      scrollPane.setPreferredSize(new Dimension(200, 200));
      
      return scrollPane;
    }
    
    
    
    private void setDetailsVisible(boolean detailsVisible) {
      if (detailsVisible != mDetailsVisible) {
        mDetailsVisible = detailsVisible;

        if (mDetailsVisible) {
          if (mDetailPn == null) {
            mDetailPn = createDetailPanel();
            mMainPn.add(mDetailPn, BorderLayout.SOUTH);
          }
          mDetailPn.setVisible(true);
          
          mDetailBt.setIcon(new ImageIcon("imgs/up16.gif"));
        } else {
          if (mDetailPn != null) {
            mDetailPn.setVisible(false);
          }
          
          mDetailBt.setIcon(new ImageIcon("imgs/down16.gif"));
        }

        mDialog.pack();
      }
    }
    
  }
  
}
