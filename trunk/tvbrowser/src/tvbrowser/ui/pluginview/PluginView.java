package tvbrowser.ui.pluginview;

import java.awt.BorderLayout;

import javax.swing.*;
import javax.swing.tree.*;

public class PluginView extends JPanel {
    
  private JTree mTree;  
    
  public PluginView() {
    super(new BorderLayout());
    DefaultMutableTreeNode top = new DefaultMutableTreeNode("Plugins");
    createNodes(top);
    mTree = new JTree(top);
    add(new JScrollPane(mTree), BorderLayout.CENTER);
  }
  
  private void createNodes(DefaultMutableTreeNode top) {
      
      DefaultMutableTreeNode item;
      
      DefaultMutableTreeNode reminderTN = new DefaultMutableTreeNode("Reminder");
      top.add(reminderTN);
      
      for (int i=0; i<7; i++) {
        reminderTN.add(new DefaultMutableTreeNode("Sendung #"+i));  
      }
      
      DefaultMutableTreeNode favoritesTN = new DefaultMutableTreeNode("Lieblingssendungen");
      top.add(favoritesTN);
      
  
      DefaultMutableTreeNode moviesTN = new DefaultMutableTreeNode("Filme");
      favoritesTN.add(moviesTN);
  
      for (int i=0; i<3; i++) {
          moviesTN.add(new DefaultMutableTreeNode("Film #"+i));  
      }
      
      DefaultMutableTreeNode episodesTN = new DefaultMutableTreeNode("Serien");
      favoritesTN.add(episodesTN);
  
      for (int i=0; i<4; i++) {
        episodesTN.add(new DefaultMutableTreeNode("Serie #"+i));  
      }

      DefaultMutableTreeNode clipboardTN = new DefaultMutableTreeNode("Zwischenablage");
      top.add(clipboardTN);
      
      for (int i=0; i<7; i++) {
          clipboardTN.add(new DefaultMutableTreeNode("Sendung #"+i));  
      }
      
     
  } 
}