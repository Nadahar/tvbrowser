

package tvbrowser.ui.settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import tvdataloader.TVDataServiceInterface;
import tvdataloader.SettingsPanel;
import tvbrowser.core.DataLoaderManager;


public class DataServiceConfigDlg extends JDialog implements ActionListener {
	
	private TVDataServiceInterface dataLoader;
	private JButton cancelBtn, okBtn;
	private SettingsPanel configPanel;
	
	public DataServiceConfigDlg(Frame parent, String dataloaderName) {
		super(parent,true);
    	this.setTitle("Configure "+dataloaderName);
    
		JPanel contentPane=(JPanel)getContentPane();
		
		
		contentPane.setLayout(new BorderLayout());
		
		dataLoader=DataLoaderManager.getDataLoader(dataloaderName);
		
		if (dataLoader!=null) {
			configPanel=dataLoader.getSettingsPanel();
			if (configPanel!=null) {
				contentPane.add(configPanel,BorderLayout.NORTH);
			}else{
				contentPane.add(new JLabel("no config pane available"),BorderLayout.CENTER);
			}
		}else{
			contentPane.add(new JLabel("Error: dataloader '"+dataloaderName+"' not found"),BorderLayout.CENTER);
		}
		
		
		JPanel pushButtonPanel=new JPanel();

		if (configPanel!=null) {
			okBtn=new JButton("OK");
			okBtn.addActionListener(this);
			pushButtonPanel.add(okBtn);
			getRootPane().setDefaultButton(okBtn);
		}
		cancelBtn=new JButton("Cancel");
		cancelBtn.addActionListener(this);
		pushButtonPanel.add(cancelBtn);
		
		contentPane.add(pushButtonPanel,BorderLayout.SOUTH);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==okBtn) {
			configPanel.ok();
		}else if (e.getSource()==cancelBtn) {
			this.hide();
		}
		
	}
}