package tvbrowser.extras.programinfo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.*;

import devplugin.SettingsTab;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ActionMenu;

public class ProgramInfoSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer =
      util.ui.Localizer.getLocalizerFor(ProgramInfoSettingsTab.class);



  private JTextArea mStyleSheetTa;
  private Properties mSettings;
  
  

  public ProgramInfoSettingsTab() {
    mSettings = ProgramInfo.getInstance().getSettings();
  }

	public JPanel createSettingsPanel() {
    
    JPanel content = new JPanel(new BorderLayout(0,4));
    content.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("stylesheet","Stylesheet for program information window")));
       
    mStyleSheetTa = new JTextArea();
    String styleSheet = mSettings.getProperty("stylesheet_v1");
    if (styleSheet == null) {
      styleSheet = ProgramInfo.DEFAULT_STYLE_SHEET;
    }
    
    mStyleSheetTa.setText(styleSheet);
    
    mStyleSheetTa.setRows(10);
    mStyleSheetTa.setFont(new Font("Monospaced",Font.PLAIN,12));
    
    JButton previewBtn = new JButton(mLocalizer.msg("preview","Preview"));
    JButton defaultBtn = new JButton(mLocalizer.msg("default","Default"));
    
    previewBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
			  saveSettings();
        Program program = Plugin.getPluginManager().getExampleProgram();
        ActionMenu actionMenu = ProgramInfo.getInstance().getContextMenuActions(program);
        actionMenu.getAction().actionPerformed(event);
        //Action[] action = ProgramInfo.getInstance().getContextMenuActions(program);
        //action[0].actionPerformed(event);
			}
    });
    
    defaultBtn.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        mStyleSheetTa.setText(ProgramInfo.DEFAULT_STYLE_SHEET);
      }
    });
    
    
    JPanel eastPn = new JPanel(new BorderLayout());
    eastPn.setBorder(BorderFactory.createEmptyBorder(0,3,0,0));
    JPanel btnPn = new JPanel(new GridLayout(2,1,3,0));
    btnPn.add(previewBtn);
    btnPn.add(defaultBtn);
    eastPn.add(btnPn,BorderLayout.NORTH);
    
    JScrollPane sp = new JScrollPane(mStyleSheetTa);
    sp.getViewport().setViewPosition(new Point(0,0));
    content.add(sp,BorderLayout.CENTER);
    content.add(eastPn, BorderLayout.EAST);
       
		return content;
	}
  
  

	
	public void saveSettings() {
    mSettings.setProperty("stylesheet_v1",mStyleSheetTa.getText());
	}


	public Icon getIcon() {
		return null;
	}

	
	public String getTitle() {
		return "ProgramInfo";
	}


	  
  
  
}