package tvbrowser.extras.programinfo;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;

import com.l2fprod.common.swing.JTaskPane;
import com.l2fprod.common.swing.JTaskPaneGroup;

import devplugin.ActionMenu;
import devplugin.Program;

/**
 * A class that holds a ContextMenuAction of a Plugin in button.
 * 
 * @author René Mach
 * 
 */
public class TaskMenuButton extends MouseAdapter implements ActionListener {

  private JButton mButton;

  private Action mAction;

  private ProgramInfoDialog mInfo;

  /**
   * 
   * @param root
   *          The root JTaskPane.
   * @param parent
   *          The parent JTaskPaneGroup
   * @param program
   *          The Program for the Action.
   * @param menu
   *          The ActionMenu.
   * @param info
   *          The ProgramInfoDialog.
   * @param id
   *          The id of the Plugin.
   */
  public TaskMenuButton(JTaskPane root, JTaskPaneGroup parent, Program program,
      ActionMenu menu, ProgramInfoDialog info, String id) {
    mInfo = info;

    if (!menu.hasSubItems())
      addButton(parent, menu);
    else
      addTaskPaneGroup(root, parent, program, menu, info, id);
  }
  
    // Adds the button to the TaskPaneGroup.
  private void addButton(JTaskPaneGroup parent, ActionMenu menu) {
    mAction = menu.getAction();

    mButton = new JButton((String) mAction.getValue(Action.NAME));
    mButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    mButton.setHorizontalAlignment(JButton.LEFT);

    if (mAction.getValue(Action.SMALL_ICON) != null)
      mButton.setIcon((Icon) mAction.getValue(Action.SMALL_ICON));

    mButton.addActionListener(this);
    mButton.addMouseListener(this);
    mButton.setOpaque(false);
    parent.add(mButton);
  }

    /* Adds a new TaskPaneGroup to the parent TaskPaneGroup
     * for an ActionMenu with submenus.
     */ 
  private void addTaskPaneGroup(JTaskPane root, JTaskPaneGroup parent,
      Program program, ActionMenu menu, ProgramInfoDialog info, final String id) {
    ActionMenu[] subs = menu.getSubItems();

    final JTaskPaneGroup group = new JTaskPaneGroup();
    group.setTitle((String) menu.getAction().getValue(Action.NAME));
    group.setExpanded(ProgramInfo.getInstance().getExpanded(id));
    

     /* Listener to get expand state changes and store the
      * state in the Properties for the Plguin.
      */
    group.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        ProgramInfo.getInstance().setExpanded(id, group.isExpanded());
      }
    });

    if (menu.getAction().getValue(Action.SMALL_ICON) != null)
      group.setIcon((Icon) menu.getAction().getValue(Action.SMALL_ICON));

    for (int i = 0; i < subs.length; i++)
      new TaskMenuButton(root, group, program, subs[i], info, id);

    parent.add(Box.createRigidArea(new Dimension(0, 10)));
    parent.add(group);
    parent.add(Box.createRigidArea(new Dimension(0, 5)));
    
  }

  public void mouseEntered(MouseEvent e) {
    mButton.setBorder(BorderFactory.createLineBorder(mButton.getForeground()
        .brighter()));
  }

  public void mouseExited(MouseEvent e) {
    mButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
  }

  public void actionPerformed(ActionEvent e) {
    mAction.actionPerformed(new ActionEvent(new JButton(),
        ActionEvent.ACTION_PERFORMED, (String) mAction
            .getValue(Action.ACTION_COMMAND_KEY)));
    mInfo.addPluginActions(true);
  }
}
