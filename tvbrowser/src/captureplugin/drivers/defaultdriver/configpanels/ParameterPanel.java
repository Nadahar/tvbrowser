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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import util.paramhandler.ParamDescriptionPanel;
import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.drivers.defaultdriver.AdditionalParams;
import captureplugin.drivers.defaultdriver.CaptureParamLibrary;
import captureplugin.drivers.defaultdriver.DefaultKonfigurator;
import captureplugin.drivers.defaultdriver.DeviceConfig;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Enter the Parameters
 */
public class ParameterPanel extends JPanel {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ParameterPanel.class);

    /** GUI */
    private JTextArea mAddFormatTextField = new JTextArea();

    private JTextArea mRemFormatTextField = new JTextArea();

    /** Data for the Panel */
    private DeviceConfig mData;

    /** Konfigurator-Dialog */
    private DefaultKonfigurator mKonfigurator;
    
    /**
     * Creates the Panel
     * @param dialog Dialog for this Panel
     * @param data Settings
     */
    public ParameterPanel(DefaultKonfigurator dialog, DeviceConfig data) {
        mKonfigurator = dialog;
        mData = data;
        
        createPanel();
    }

    /**
     * creates a JPanel for getting the parameterformat
     */
    private void createPanel() {try {
      CellConstraints cc = new CellConstraints();
      PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,pref,5dlu,pref:grow,pref,5dlu",
          "pref,5dlu,pref,55dlu,5dlu,pref,55dlu,5dlu,pref,5dlu,default"),this);
      pb.setDefaultDialogBorder();

      pb.addSeparator(mLocalizer.msg("Parameters", "Parameters"), cc.xyw(1,1,6));
      pb.addLabel(mLocalizer.msg("Record", "record"), cc.xy(2,3));
      

      mAddFormatTextField.setLineWrap(true);
        // Consume Enter-Key
      mAddFormatTextField.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent ke) {
          if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            ke.consume();
          }
        }
      });
      
      mAddFormatTextField.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          addFormatChanged();
        }
      });

      mAddFormatTextField.setText(mData.getParameterFormatAdd());

      JScrollPane scroll = new JScrollPane(mAddFormatTextField, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      
      pb.add(scroll, cc.xywh(4,3,2,2));
      
      pb.addLabel(Localizer.getLocalization(Localizer.I18N_DELETE), cc.xy(2,6));

      mRemFormatTextField.setLineWrap(true);
      
      // Consume Enter-Key
      mRemFormatTextField.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent ke) {
          if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            ke.consume();
          }
        }
      });
      
      mRemFormatTextField.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          remFormatChanged();
        }
      });

      mRemFormatTextField.setText(mData.getParameterFormatRem());
            
      scroll = new JScrollPane(mRemFormatTextField, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      pb.add(scroll, cc.xywh(4,6,2,2));
        
      
      JButton additional = new JButton(mLocalizer.msg("Additional", "Additional Parameters"));
      
      additional.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          additionalPressed();
        }
      });
        
      pb.add(additional, cc.xy(5,9));
      pb.add(new ParamDescriptionPanel(new CaptureParamLibrary(mData)), cc.xyw(2,11,4));}catch(Exception e){e.printStackTrace();}
    }

    /**
     * invoked when the addFormat - TextField losts the Focus, the value of the
     * TextField will then be stored.
     */
    public void addFormatChanged() {
        mData.setParameterFormatAdd(mAddFormatTextField.getText());
    }

    /**
     * invoked when the remFormat - TextField losts the Focus, the value of the
     * TextField will then be stored.
     */
    public void remFormatChanged() {
        mData.setParameterFormatRem(mRemFormatTextField.getText());
    }

    /**
     * Additional Parameters was pressed
     */
    private void additionalPressed() {
        AdditionalParams params = new AdditionalParams(mKonfigurator, mData);
        UiUtilities.centerAndShow(params);
    }

}