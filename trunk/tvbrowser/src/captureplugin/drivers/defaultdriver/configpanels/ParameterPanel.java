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

import javax.swing.JButton;
import javax.swing.JPanel;

import util.paramhandler.ParamInputField;
import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.drivers.defaultdriver.AdditionalParams;
import captureplugin.drivers.defaultdriver.CaptureParamLibrary;
import captureplugin.drivers.defaultdriver.DefaultKonfigurator;
import captureplugin.drivers.defaultdriver.DeviceConfig;

import com.jgoodies.forms.layout.CellConstraints;

/**
 * Enter the Parameters
 */
public class ParameterPanel extends JPanel {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(ParameterPanel.class);

    /** GUI */
    private ParamInputField mAddFormatTextField;

    private ParamInputField mRemFormatTextField;

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
     * creates a JPanel for getting the parameters
     */
    private void createPanel() {try {
      CellConstraints cc = new CellConstraints();
      EnhancedPanelBuilder pb = new EnhancedPanelBuilder("5dlu,pref,5dlu,pref:grow,pref,5dlu", this);
      pb.setDefaultDialogBorder();

      pb.addParagraph(mLocalizer.msg("parametersRecord", "Parameters for recording"));

      pb.addGrowingRow();
      mAddFormatTextField = new ParamInputField(new CaptureParamLibrary(mData), mData.getParameterFormatAdd(), false);
      pb.add(mAddFormatTextField, cc.xyw(2, pb.getRow(), pb.getColumnCount() - 1));
      
      pb.addParagraph(mLocalizer.msg("parametersDelete", "Parameters for deletion"));

      pb.addGrowingRow();
      mRemFormatTextField = new ParamInputField(new CaptureParamLibrary(mData), mData.getParameterFormatRem(), false);
      pb.add(mRemFormatTextField, cc.xyw(2, pb.getRow(), pb.getColumnCount() - 1));
      
      pb.addParagraph(mLocalizer.msg("parametersAdditional", "Additional commands"));
      pb.addRow();
      JButton additional = new JButton(mLocalizer.msg("Additional", "Define additional commands"));
      
      additional.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          AdditionalParams params = new AdditionalParams(mKonfigurator, mData);
          UiUtilities.centerAndShow(params);
        }
      });
        
      pb.add(additional, cc.xy(pb.getColumnCount() - 1, pb.getRow()));
      
      }catch(Exception e){e.printStackTrace();}
    }

    /**
     * invoked when the addFormat - TextField lost the Focus, the value of the
     * TextField will then be stored.
     */
    public void addFormatChanged() {
        mData.setParameterFormatAdd(mAddFormatTextField.getText());
    }

    /**
     * invoked when the remFormat - TextField lost the Focus, the value of the
     * TextField will then be stored.
     */
    public void remFormatChanged() {
        mData.setParameterFormatRem(mRemFormatTextField.getText());
    }
}