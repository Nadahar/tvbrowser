
package tvbrowser.ui.settings;

import javax.swing.*;
import java.awt.*;  
  
 class PluginInfoPanel extends JPanel {
    
  
   private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(PluginInfoPanel.class);

    
   private JLabel nameLabel;
   private JLabel versionLabel;
   private JLabel authorLabel;
   private JTextArea descriptionArea;
    
   public PluginInfoPanel() {
     setLayout(new BorderLayout(10,0));
      
     String msg;
      
     JPanel leftPanel=new JPanel(new BorderLayout());
     JPanel rightPanel=new JPanel(new BorderLayout());
      
     msg = mLocalizer.msg("name", "Name");
     leftPanel.add(new JLabel(msg), BorderLayout.NORTH);
     rightPanel.add(nameLabel=new JLabel("-"),BorderLayout.NORTH);
      
     JPanel panel1=new JPanel(new BorderLayout());
     JPanel panel2=new JPanel(new BorderLayout());
      
     msg = mLocalizer.msg("version", "Version");
     panel1.add(new JLabel(msg), BorderLayout.NORTH);
     panel2.add(versionLabel=new JLabel("-"),BorderLayout.NORTH);
      
     JPanel panel3=new JPanel(new BorderLayout());
     JPanel panel4=new JPanel(new BorderLayout());
      
     msg = mLocalizer.msg("author", "Author");
     panel3.add(new JLabel(msg), BorderLayout.NORTH);
     panel4.add(authorLabel=new JLabel("-"),BorderLayout.NORTH);
      
     panel1.add(panel3,BorderLayout.CENTER);
     panel2.add(panel4,BorderLayout.CENTER);
      
     JPanel panel5=new JPanel(new BorderLayout());
     msg = mLocalizer.msg("description", "Description");
     panel5.add(new JLabel(msg), BorderLayout.NORTH);
      
     descriptionArea=new JTextArea(3,40);
     descriptionArea.setLineWrap(true);
     descriptionArea.setWrapStyleWord(true);
     descriptionArea.setEditable(false);
     descriptionArea.setOpaque(false);
      
     panel3.add(panel5,BorderLayout.CENTER);
     panel4.add(descriptionArea,BorderLayout.CENTER);
      
     leftPanel.add(panel1,BorderLayout.CENTER);
     rightPanel.add(panel2,BorderLayout.CENTER);
      
     add(leftPanel,BorderLayout.WEST);
     add(rightPanel,BorderLayout.CENTER);
   }
    
    
    
   public void setPluginInfo(devplugin.PluginInfo info) {
     nameLabel.setText(info.getName());
     versionLabel.setText(info.getVersion().toString());
     authorLabel.setText(info.getAuthor());
     descriptionArea.setText(info.getDescription());
   }
    
 } 
  