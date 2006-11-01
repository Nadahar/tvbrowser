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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package captureplugin.drivers.defaultdriver.configpanels;

import captureplugin.drivers.defaultdriver.DeviceConfig;
import util.ui.Localizer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;


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
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("What", "What to start")));

        
        setLayout(new GridBagLayout());
        
        GridBagConstraints rb = new GridBagConstraints();
        rb.anchor = GridBagConstraints.NORTHWEST;
        rb.insets = new Insets(0, 5, 0, 5);

        JRadioButton application = new JRadioButton(mLocalizer.msg("Application", "Application"));
        
        add(application, rb);
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(0, 5, 0, 5);
        mPathTextField.setText(mData.getProgramPath());
        
        mPathTextField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                mData.setProgramPath(mPathTextField.getText());
            }
        });

        add(mPathTextField, c);

        
        GridBagConstraints fb = new GridBagConstraints();
        fb.gridwidth = GridBagConstraints.REMAINDER;
        fb.weightx = 0;
        fb.fill = GridBagConstraints.NONE;

        mFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                pathButtonPressed(e);
            }
        });

        add(mFileButton, fb);

        
        JRadioButton url = new JRadioButton(mLocalizer.msg("URL", "URL"));
        
        add(url, rb);
        

        GridBagConstraints uf = new GridBagConstraints();
        uf.fill = GridBagConstraints.HORIZONTAL;
        uf.weightx = 1.0;
        uf.gridwidth = GridBagConstraints.REMAINDER;
        uf.insets = new Insets(5, 5, 0, 0);

        mUrl.setText(mData.getWebUrl());
        
        mUrl.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                mData.setWebUrl(mUrl.getText());
            }
        });

        add(mUrl, uf);
        
        ButtonGroup group = new ButtonGroup();
        
        group.add(application);
        group.add(url);
        
        
        GridBagConstraints filler = new GridBagConstraints();
        filler.fill = GridBagConstraints.BOTH;
        filler.weightx = 1.0;
        filler.weighty = 1.0;
        filler.gridwidth = GridBagConstraints.REMAINDER;
        
        add(new JPanel(), filler);
        
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