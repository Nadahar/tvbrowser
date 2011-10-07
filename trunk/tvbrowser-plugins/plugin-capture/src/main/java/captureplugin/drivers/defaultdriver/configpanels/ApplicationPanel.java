/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *     $Date: 2009-09-04 11:15:55 +0200 (Fr, 04 Sep 2009) $
 *   $Author: bananeweizen $
 * $Revision: 5953 $
 */
package captureplugin.drivers.defaultdriver.configpanels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import util.ui.Localizer;
import captureplugin.drivers.defaultdriver.DeviceConfig;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * This Panel lets the User choose the Application / URL
 */
public class ApplicationPanel extends JPanel {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ApplicationPanel.class);

    /** Data for Panel */
    private DeviceConfig mData;
    
    /** Path */
    private JTextField mPathTextField = new JTextField();

    private JTextField mUrl = new JTextField();
    
    private JButton mFileButton = new JButton(Localizer.getLocalization(Localizer.I18N_FILE));
    
    /**
     * Creates the Panel
     * @param data Configuration
     */
    public ApplicationPanel(DeviceConfig data) {
        mData = data;
        createPanel();
    }
    
    /**
     * creates a JPanel for getting the programpath
     */
    private void createPanel() {
      CellConstraints cc = new CellConstraints();
      PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,3dlu,pref:grow,3dlu,pref,2dlu",
          "pref,5dlu,pref,3dlu,pref"),this);
      pb.setDefaultDialogBorder();

      pb.addSeparator(mLocalizer.msg("What", "What to start"), cc.xyw(1,1,7));
      
      JRadioButton application = new JRadioButton(mLocalizer.msg("Application", "Application"));
        
      pb.add(application, cc.xy(2,3));
        
      mPathTextField.setText(mData.getProgramPath());
      mPathTextField.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          mData.setProgramPath(mPathTextField.getText());
        }
      });

      pb.add(mPathTextField, cc.xy(4,3));

      mFileButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          pathButtonPressed(e);
        }
      });

      pb.add(mFileButton, cc.xy(6,3));
        
      JRadioButton url = new JRadioButton(mLocalizer.msg("URL", "URL"));
        
      pb.add(url, cc.xy(2,5));
        
      mUrl.setText(mData.getWebUrl());
        
      mUrl.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          mData.setWebUrl(mUrl.getText());
        }
      });

      pb.add(mUrl, cc.xyw(4,5,3));
        
      ButtonGroup group = new ButtonGroup();
        
      group.add(application);
      group.add(url);
        
      url.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setUrlMode(true);
        }
      });
        
      application.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setUrlMode(false);
        }
      });
        
      if (mData.getUseWebUrl()) {
        url.setSelected(true);
      } else {
        application.setSelected(true);
      }
      
      setUrlMode(mData.getUseWebUrl());
    }

    /**
     * Sets the Mode of the Application
     * @param urlmode
     */
    private void setUrlMode(boolean urlmode) {
        mData.setUseWebUrl(urlmode);
        mUrl.setEnabled(urlmode);
        mPathTextField.setEnabled(!urlmode);
        mFileButton.setEnabled(!urlmode);
    }
    
    /**
     * invoked when the user clicks the Button to open an FileChooser - Dialog
     */
    private void pathButtonPressed(ActionEvent e) {
        JFileChooser f = new JFileChooser();
        if (f.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            mData.setProgramPath(f.getSelectedFile().toString());
            mPathTextField.setText(mData.getProgramPath());
        }
    }


}