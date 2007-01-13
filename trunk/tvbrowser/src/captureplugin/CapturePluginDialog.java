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

package captureplugin;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import javax.swing.JButton;
import javax.swing.JDialog;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The Dialog for the Settings. Uses the PluginPanel
 */
public class CapturePluginDialog extends JDialog implements WindowClosingIf {
    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CapturePluginDialog.class);
  
    /** Data */
    private CapturePluginData mData;

    /** PluginPanel for entering Data */
    private CapturePluginPanel mPanel;

    /**
     * creates a new Dialog
     * @param parent Paren-Frame
     * @param data Config of the Plugin
     */
    public CapturePluginDialog(Frame parent, CapturePluginData data) {
        super(parent);

        mData = data;
        setModal(true);
        createGui(parent);
    }

    /**
     * creates a new Dialog
     * @param parent Paren-Frame
     * @param data Config of the Plugin
     */
    public CapturePluginDialog(Dialog parent, CapturePluginData data) {
        super(parent);

        mData = data;
        setModal(true);
        createGui(parent);
    }    
    
    /**
     * Creates the GUI
     * @param parent Parent
     */
    public void createGui(Window parent) {
        UiUtilities.registerForClosing(this);
      
        this.getContentPane().setLayout(new BorderLayout());
        if (parent != null) {
            this.setLocation(parent.getLocation().x + (parent.getWidth() / 2) - 200, parent.getLocation().y + (parent.getHeight() / 2)
                    - 280);
        }
        this.setTitle(mLocalizer.msg("Title", "Capture Plugin - Settings"));

        
        mPanel = new CapturePluginPanel(parent, mData);
        this.getContentPane().add(mPanel, BorderLayout.CENTER);
        this.setSize(500, 450);
        

        JButton okButton = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                okButtonPressed();
            }
        });

        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.addGlue();
        builder.addGridded(okButton);
        builder.setBorder(Borders.DLU4_BORDER);
        
        this.getContentPane().add(builder.getPanel(), BorderLayout.SOUTH);
    }

    /**
     * Updates the mPanel with the new Data and shows the Dialog
     * 
     * @param tab Tab to select
     */
    public void show(int tab) {
        mPanel.setSelectedTab(tab);
        super.setVisible(true);
    }

    /**
     * invoked when the user clicks the OK - Button, this will hide the Dialog.
     */
    public void okButtonPressed() {
        this.setVisible(false);
        mPanel.savePictureSettings();
    }

    public void close() {
      this.setVisible(false);
    }

}