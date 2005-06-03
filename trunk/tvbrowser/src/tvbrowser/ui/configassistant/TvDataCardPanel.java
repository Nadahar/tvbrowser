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
import tvbrowser.core.TvDataServiceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;


class TvDataCardPanel extends AbstractCardPanel {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(TvDataCardPanel.class);


  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(TvDataCardPanel.class.getName());

  private JPanel mContent;
  private ImportHandler mImportHandler;
  private JTextField mDirectoryTF;

  public TvDataCardPanel(PrevNextButtons btns) {
    super(btns);
    mContent=new JPanel(new BorderLayout());

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));


    JLabel area=new JLabel();
    area.setFont(new Font("SansSerif", Font.PLAIN, 12));
    area.setText(mLocalizer.msg("infotext.1","<HTML><H2>TV-Daten-Verzeichnis</H2><BR>Hier haben Sie die M&ouml;glichkeit, den Ort festzulegen, wo TV-Browser die TV-Daten speichern soll.<BR>Auf Mehrbenutzersystemen wird empfohlen, ein Verzeichnis festzulegen, auf das alle Benutzer lesend und schreibend zugreifen d&uuml;rfen.</HTML>"));
    JPanel areaPn = new JPanel(new BorderLayout());
    areaPn.add(area, BorderLayout.CENTER);


    JPanel directoryChooserPn = createDirectoryChooser();
    directoryChooserPn.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));

    JPanel importPn = createImportPanel();

    content.add(areaPn);
    content.add(directoryChooserPn);
    content.add(importPn);


    mContent.add(content,BorderLayout.NORTH);
  }


  private JPanel createDirectoryChooser() {
    JPanel result = new JPanel(new BorderLayout());
    result.add(new JLabel(mLocalizer.msg("tvlistingsDir","TV-Daten-Verzeichnis")+":"), BorderLayout.WEST);

    mDirectoryTF = new JTextField(Settings.propTVDataDirectory.getString());
    result.add(mDirectoryTF, BorderLayout.CENTER);

    JButton chooseBtn = new JButton(mLocalizer.msg("browse","Browse...")+"...");
    result.add(chooseBtn, BorderLayout.EAST);


    return result;
  }

  private JPanel createImportPanel() {
    JPanel importPn = new JPanel(new BorderLayout());
    JPanel gridPn = new JPanel(new GridLayout(2,1));
    JButton importBtn = new JButton(mLocalizer.msg("selectImportDir","Zu importierende TV-Daten auswaehlen"));
    final JLabel statusLb = new JLabel();
    importBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int retVal = fileChooser.showOpenDialog(util.ui.UiUtilities.getBestDialogParent(mContent));
        if (retVal == JFileChooser.APPROVE_OPTION) {
          File f=fileChooser.getSelectedFile();
          if (f!=null) {
            mImportHandler = new ImportHandler(f);
            if (mImportHandler.getChannelCount()==0) {
              JOptionPane.showMessageDialog(mContent, mLocalizer.msg("error.msg.1","The selected directory doesn't contain valid tv listings."), mLocalizer.msg("error.title.1","Invalid directory"), JOptionPane.WARNING_MESSAGE);
              statusLb.setText("");
            }
            else {
              statusLb.setText(mLocalizer.msg("importStatus","{0} channels ready for import",mImportHandler.getChannelCount()+""));
            }
          }
        }
      }
    });

    gridPn.add(importBtn);
    gridPn.add(statusLb);

    JPanel pn = new JPanel(new BorderLayout());
    pn.add(gridPn, BorderLayout.SOUTH);

    importPn.add(pn, BorderLayout.EAST);
    importPn.add(new JLabel(mLocalizer.msg("infotext.2","<HTML>Bitte nutzen Sie die M&ouml;glichkeit, die vorhandenen TV-Daten zu importieren, um unn&ouml;tigen Netzwerkverkehr zu vermeiden.</HTML>")));
    return importPn;
  }


  public boolean onNext() {
    if (mDirectoryTF.getText().trim().length()==0) {
      JOptionPane.showMessageDialog(mContent, mLocalizer.msg("error.msg.2","Please enter a directory name!"));
      return false;
    }
    /* Create the new tv listings folder */
    File tvDataDir = new File(mDirectoryTF.getText().trim());
    if (!(tvDataDir.exists() && tvDataDir.isDirectory())) {
      if (JOptionPane.showConfirmDialog(mContent,mLocalizer.msg("error.msg.3", "Directory '{0}' does not exist. Create?",tvDataDir.toString()),mLocalizer.msg("error.title.3","Create directory"), JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
        if (!tvDataDir.mkdirs()) {
          JOptionPane.showMessageDialog(mContent, mLocalizer.msg("error.msg.4","Could not create directory '{0}'.",tvDataDir.toString()));
          return false;
        }
      }
    }

    if (!(tvDataDir.exists() && tvDataDir.isDirectory())) {
      // this should not happen
      mLog.severe("creation of directory '"+tvDataDir.toString()+"' failed");
      return false;
    }

    if (mImportHandler != null) {
      try {
        mImportHandler.importTo(tvDataDir);
      }catch(IOException e) {
        util.exc.ErrorHandler.handle(mLocalizer.msg("error.msg.5","Could not import TV listings"), e);
      }
    }

    Settings.propTVDataDirectory.setString(tvDataDir.getAbsolutePath());
    TvDataServiceManager.getInstance().setTvDataDir(tvDataDir);
    return true;
  }


  public JPanel getPanel() {
    return mContent;
  }

}
