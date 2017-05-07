/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.ui.settings;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import tvbrowser.core.Settings;
import tvbrowser.core.contextmenu.ConfigMenuItem;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.contextmenu.DoNothingContextMenuItem;
import tvbrowser.core.contextmenu.LeaveFullScreenMenuItem;
import tvbrowser.core.contextmenu.SelectProgramContextMenuItem;
import tvbrowser.core.contextmenu.SeparatorMenuItem;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.ui.mainframe.MainFrame;
import util.settings.ContextMenuMouseActionSetting;
import util.ui.CustomComboBoxRenderer;
import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Plugin;
import devplugin.Program;

public class MouseSettingsTab implements devplugin.SettingsTab {

	private static final util.ui.Localizer mLocalizer = util.ui.Localizer
			.getLocalizerFor(MouseSettingsTab.class);
	
  private static final String[] mLeftModifiersName = {
    mLocalizer.msg("modifier.none","None"),
    mLocalizer.msg("modifier.ctrl","Ctrl"),
    mLocalizer.msg("modifier.ctrlShift","Ctrl+Shift"),
    mLocalizer.msg("modifier.ctrlAlt","Ctrl+Alt"),
    mLocalizer.msg("modifier.altShift","Alt+Shift"),
    mLocalizer.msg("modifier.ctrlAltShift","Ctrl+Alt+Shift")
  };
  
  private static final int[] mLeftModifiersEx = {
    ContextMenuManager.NO_MOUSE_MODIFIER_EX,
    MouseEvent.CTRL_DOWN_MASK,
    MouseEvent.CTRL_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK,
    MouseEvent.CTRL_DOWN_MASK | MouseEvent.ALT_DOWN_MASK,
    MouseEvent.SHIFT_DOWN_MASK | MouseEvent.ALT_DOWN_MASK,
    MouseEvent.CTRL_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK | MouseEvent.ALT_DOWN_MASK
  };
  
  private static final String[] mMiddleModifiersName = {
    mLocalizer.msg("modifier.none","None"),
    mLocalizer.msg("modifier.ctrl","Ctrl"),
    mLocalizer.msg("modifier.shift","Shift"),
    mLocalizer.msg("modifier.ctrlShift","Ctrl+Shift"),
    mLocalizer.msg("modifier.ctrlAlt","Ctrl+Alt"),
    mLocalizer.msg("modifier.altShift","Alt+Shift"),
    mLocalizer.msg("modifier.ctrlAltShift","Ctrl+Alt+Shift")
  };
  
  private static final int[] mMiddleModifiersEx = {
    ContextMenuManager.NO_MOUSE_MODIFIER_EX,
    MouseEvent.CTRL_DOWN_MASK,
    MouseEvent.SHIFT_DOWN_MASK,
    MouseEvent.CTRL_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK,
    MouseEvent.CTRL_DOWN_MASK | MouseEvent.ALT_DOWN_MASK,
    MouseEvent.SHIFT_DOWN_MASK | MouseEvent.ALT_DOWN_MASK,
    MouseEvent.CTRL_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK | MouseEvent.ALT_DOWN_MASK
  };
  
  private static final String[] MOUSE_BUTTON_TEXT = {mLocalizer.msg("button.left", "Left"),mLocalizer.msg("button.middle", "Middle")};
  private static final String[] CLICK_COUNT_TEXT = {mLocalizer.msg("click.single", "Single"),mLocalizer.msg("click.double", "Double")};
  
	private ArrayList<ContextMenuPanel> mMouseActions = new ArrayList<ContextMenuPanel>();
	private JPanel mMainPanel;
	
	public JPanel createSettingsPanel() {
	  FormLayout layout = new FormLayout("5dlu, pref, 3dlu, pref, fill:pref:grow, 3dlu");
		EnhancedPanelBuilder contentPanel = new EnhancedPanelBuilder(layout);
		contentPanel.border(Borders.DIALOG);
		
		mMouseActions.clear();
		
		contentPanel.addRow();
		contentPanel.addSeparator(mLocalizer.msg("title", "Title"), CC.xyw(1, contentPanel.getRow(), 6));

		contentPanel.addRow();
		contentPanel.add(
				new JLabel(mLocalizer.msg("MouseButtons", "Mouse Buttons:")),
				CC.xyw(2, contentPanel.getRow(), 4));


		mMainPanel = new JPanel();
		mMainPanel.setLayout(new BoxLayout(mMainPanel, BoxLayout.Y_AXIS));
		
		contentPanel.addRow();
		contentPanel.add(mMainPanel, CC.xyw(2, contentPanel.getRow(), 4));
		
    ContextMenuMouseActionSetting[] leftSingleClick = Settings.propLeftSingleClickIfArray.getContextMenuMouseActionArray();
    ContextMenuMouseActionSetting[] leftDoubleClick = Settings.propLeftDoubleClickIfArray.getContextMenuMouseActionArray();
    ContextMenuMouseActionSetting[] middleSingleClick = Settings.propMiddleSingleClickIfArray.getContextMenuMouseActionArray();
    ContextMenuMouseActionSetting[] middleDoubleClick = Settings.propMiddleDoubleClickIfArray.getContextMenuMouseActionArray();
    
    addListEntries(leftSingleClick,1,1);
    addListEntries(leftDoubleClick,1,2);
    addListEntries(middleSingleClick,2,1);
    addListEntries(middleDoubleClick,2,2);
        
    updateList();

    JButton add = new JButton(mLocalizer.msg("add","Add a new mouse action"),TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    add.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ContextMenuMouseActionSetting setting = new ContextMenuMouseActionSetting(ContextMenuManager.NO_MOUSE_MODIFIER_EX, DoNothingContextMenuItem.getInstance().getId(), ActionMenu.ID_ACTION_NONE);
        
        ContextMenuPanel contextMenuPanel = new ContextMenuPanel(setting, 1, 1);
        mMouseActions.add(contextMenuPanel);
        
        mMainPanel.add(contextMenuPanel);
        mMainPanel.updateUI();
      }
    });
    
    contentPanel.addRow();
    contentPanel.add(add, CC.xy(2,contentPanel.getRow()));
		
		return contentPanel.getPanel();
	}
	
	private void addListEntries(ContextMenuMouseActionSetting[] actions, int mouseButton, int clickCount) {
    for(ContextMenuMouseActionSetting setting : actions) {
      ContextMenuPanel contextMenuPanel = new ContextMenuPanel(setting, mouseButton, clickCount);
      mMouseActions.add(contextMenuPanel);
    }
	}
	
	private void updateList() {
	  mMainPanel.removeAll();
	  
	  for(ContextMenuPanel panel : mMouseActions) {
	    mMainPanel.add(panel);
	  }
	  
	  mMainPanel.updateUI();
	}

	public void saveSettings() {
	  try {
	  ArrayList<ContextMenuMouseActionSetting> singleLeft = new ArrayList<ContextMenuMouseActionSetting>();
	  ArrayList<ContextMenuMouseActionSetting> doubleLeft = new ArrayList<ContextMenuMouseActionSetting>();
	  ArrayList<ContextMenuMouseActionSetting> singleMiddle = new ArrayList<ContextMenuMouseActionSetting>();
	  ArrayList<ContextMenuMouseActionSetting> doubleMiddle = new ArrayList<ContextMenuMouseActionSetting>();
	  
		for (ContextMenuPanel mouseAction : mMouseActions) {
		  ContextMenuMouseActionSetting setting = mouseAction.getSetting();
		  
		  if(mouseAction.isLeftMouseButton()) {
		    if(mouseAction.isSingleClick()) {
		      if(!containsModifier(singleLeft, setting.getModifiersEx())) {
		        singleLeft.add(setting);
		      }
		    }
		    else if(!containsModifier(doubleLeft, setting.getModifiersEx())) {
		      doubleLeft.add(setting);
		    }
		  }
		  else {
		    if(mouseAction.isSingleClick()) {
		      if(!containsModifier(singleMiddle, setting.getModifiersEx())) {
		        singleMiddle.add(setting);
		      }
		    }
		    else if(!containsModifier(doubleMiddle, setting.getModifiersEx())) {
		      doubleMiddle.add(setting);
		    }
		  }
		}
		
		Settings.propLeftSingleClickIfArray.setContextMenuMouseActionArray(singleLeft.toArray(new ContextMenuMouseActionSetting[singleLeft.size()]));
		Settings.propLeftDoubleClickIfArray.setContextMenuMouseActionArray(doubleLeft.toArray(new ContextMenuMouseActionSetting[doubleLeft.size()]));
		Settings.propMiddleSingleClickIfArray.setContextMenuMouseActionArray(singleMiddle.toArray(new ContextMenuMouseActionSetting[singleMiddle.size()]));
		Settings.propMiddleDoubleClickIfArray.setContextMenuMouseActionArray(doubleMiddle.toArray(new ContextMenuMouseActionSetting[doubleMiddle.size()]));		
		
		ContextMenuManager.getInstance().init();
		MainFrame.getInstance().addKeyboardAction();
	  }catch(Throwable t) {
	    t.printStackTrace();
	  }
	}
	
	private boolean containsModifier(ArrayList<ContextMenuMouseActionSetting> list, int modifier) {
	  for(ContextMenuMouseActionSetting setting : list) {
	    if(setting.getModifiersEx() == modifier) {
	      return true;
	    }
	  }
	  
	  return false;
	}

	public Icon getIcon() {
		return IconLoader.getInstance().getIconFromTheme("devices", "input-mouse",
				16);
	}

	public String getTitle() {
		return mLocalizer.msg("title", "context menu");
	}
	
	private class ContextMenuPanel extends JPanel {
	  private JComboBox<String> mMouseButton;
	  private JComboBox<String> mModifiersEx;
	  private JComboBox<String> mClickCount;
	  private MouseClickSetting mMouseClickSetting;
	  
	  private ContextMenuPanel(ContextMenuMouseActionSetting setting, int mouseButton, int clickCount) {
	    EnhancedPanelBuilder pb = new EnhancedPanelBuilder("default,3dlu,default,3dlu,default,3dlu,default,3dlu,default",this);

	    pb.addRow();
	    pb.addLabel(mLocalizer.msg("mouseButton", "Mouse button"), CC.xy(1, pb.getRow()));
	    pb.addLabel(mLocalizer.msg("clickCount", "Click count"),CC.xy(3, pb.getRow()));
	    pb.addLabel(mLocalizer.msg("modifier", "Keyboard"), CC.xy(5, pb.getRow()));
	    pb.addLabel(mLocalizer.msg("action", "Action"), CC.xy(7, pb.getRow()));
	    
	    pb.addRow();
	    
	    mMouseButton = new JComboBox<>(MOUSE_BUTTON_TEXT);
	    mMouseButton.setSelectedIndex(mouseButton-1);
	    mModifiersEx = new JComboBox<>(mouseButton == 1 ? mLeftModifiersName : mMiddleModifiersName);
	    mModifiersEx.setSelectedIndex(indexOfModifier(mouseButton == 1 ? mLeftModifiersEx : mMiddleModifiersEx, setting.getModifiersEx()));
	    mMouseClickSetting = new MouseClickSetting(setting.getContextMenuIf(), setting.getContextMenuActionId());
	    mClickCount = new JComboBox<>(CLICK_COUNT_TEXT);
	    mClickCount.setSelectedIndex(clickCount-1);
	    
	    mMouseButton.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          if(e.getStateChange() == ItemEvent.SELECTED) {
            ((DefaultComboBoxModel<String>)mModifiersEx.getModel()).removeAllElements();
            
            if(e.getItem().equals(MOUSE_BUTTON_TEXT[0])) {
              for(String name : mLeftModifiersName) {
                ((DefaultComboBoxModel<String>)mModifiersEx.getModel()).addElement(name);
              }
            }
            else {
              for(String name : mMiddleModifiersName) {
                ((DefaultComboBoxModel<String>)mModifiersEx.getModel()).addElement(name);
              }              
            }
          }
        }
      });
	    
	    JButton delete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
	    delete.setToolTipText(Localizer.getLocalization(Localizer.I18N_DELETE));
	    delete.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          mMouseActions.remove(ContextMenuPanel.this);
          mMainPanel.remove(ContextMenuPanel.this);
          mMainPanel.updateUI();
        }
      });
	    
	    pb.add(mMouseButton, CC.xy(1, pb.getRow()));
	    pb.add(mClickCount, CC.xy(3, pb.getRow()));
	    pb.add(mModifiersEx, CC.xy(5, pb.getRow()));
	    pb.add(mMouseClickSetting.createComboxBox(), CC.xy(7, pb.getRow()));
	    pb.add(delete, CC.xy(9, pb.getRow()));
	  }
	  
	  private int indexOfModifier(int[] modifierExArr, int modifierEx) {
	    for(int i = 0; i < modifierExArr.length; i++) {
	      if(modifierExArr[i] == modifierEx) {
	        return i;
	      }
	    }
	    
	    return 0;
	  }
	  
	  public ContextMenuMouseActionSetting getSetting() {
	    int mouseButton = mMouseButton.getSelectedIndex() + 1;
	    ContextMenuActionEntry selected = mMouseClickSetting.getSelectedContextMenuActionEntry();
	    
	    int modifierEx = mouseButton == 1 ? mLeftModifiersEx[mModifiersEx.getSelectedIndex()] : mMiddleModifiersEx[mModifiersEx.getSelectedIndex()];
	    
	    ContextMenuMouseActionSetting setting = new ContextMenuMouseActionSetting(modifierEx,selected.mContextMenuIf.getId(),selected.mActionMenu.getActionId());
	    
	    return setting;
	  }
	  
	  public boolean isLeftMouseButton() {
	    return mMouseButton.getSelectedIndex() == 0;
	  }
	  
	  public boolean isSingleClick() {
	    return mClickCount.getSelectedIndex() == 0;
	  }
	}
	
  private static class ContextMenuCellRenderer extends CustomComboBoxRenderer {
    public ContextMenuCellRenderer(ListCellRenderer<Object> backendRenderer) {
		  super(backendRenderer);
		}

		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			JLabel label = (JLabel) getBackendRenderer().getListCellRendererComponent(list, value,
					index, isSelected, cellHasFocus);

			if (value instanceof ContextMenuActionEntry) {
				ContextMenuIf menuIf = ((ContextMenuActionEntry) value).mContextMenuIf;
				//Program exampleProgram = Plugin.getPluginManager().getExampleProgram();

				// Get the context menu item text
				StringBuilder text = new StringBuilder();
				Icon icon = null;
				ActionMenu actionMenu = ((ContextMenuActionEntry) value).mActionMenu;
				if (actionMenu != null) {
				  Action action = actionMenu.getAction();
					if (action != null) {
						text.append((String) action.getValue(Action.NAME));
						icon = (Icon) action.getValue(Action.SMALL_ICON);
					} else if (menuIf instanceof PluginProxy) {
						text.append(((PluginProxy) menuIf).getInfo().getName());
						icon = ((PluginProxy) menuIf).getMarkIcon();
					} else {
						text.append("unknown");
						icon = null;
					}
				}
				label.setIcon(icon);
        label.setText(text.toString());
			}

			return label;
		}
	}

	private static class MouseClickSetting {
		private ContextMenuIf mClickInterface;
		private ActionMenu mActionMenu;
		private JComboBox<Object> mComboBox;

		public MouseClickSetting(ContextMenuIf clickIf, int actionMenuId) {
			mClickInterface = clickIf;
			
			if(clickIf != null) {
  			mActionMenu = clickIf.getContextMenuActions(Plugin.getPluginManager().getExampleProgram());
  			
  			if(actionMenuId != ActionMenu.ID_ACTION_NONE) {
  			  mActionMenu = ContextMenuManager.loadActionMenu(mActionMenu, actionMenuId);
  			}
			}
			else {
			  mActionMenu = new ActionMenu((Action)null);
			}
		}

		public ContextMenuActionEntry getSelectedContextMenuActionEntry() {
		  return (ContextMenuActionEntry) mComboBox.getSelectedItem();
		}

		public JComboBox<Object> createComboxBox() {
			mComboBox = new JComboBox<>();
			mComboBox.setSelectedItem(new ContextMenuActionEntry(mClickInterface,mActionMenu));
			mComboBox.setMaximumRowCount(15);
			mComboBox.setRenderer(new ContextMenuCellRenderer(mComboBox.getRenderer()));
			mComboBox.removeAllItems();
			DoNothingContextMenuItem doNothing = DoNothingContextMenuItem
					.getInstance();
			mComboBox.addItem(new ContextMenuActionEntry(doNothing, doNothing.getContextMenuActions(null)));
			mComboBox.addItem(new ContextMenuActionEntry(SelectProgramContextMenuItem.getInstance(), SelectProgramContextMenuItem.getInstance().getContextMenuActions(null)));
			fillListBox();
			if (mClickInterface != null) {
				mComboBox.setSelectedItem(new ContextMenuActionEntry(mClickInterface,mActionMenu));
			} else {
				mComboBox.setSelectedItem(doNothing);
			}
			return mComboBox;
		}

		private void fillListBox() {
			ContextMenuIf[] menuIfList = ContextMenuManager.getInstance()
					.getAvailableContextMenuIfs(true, false);
			final Program exampleProgram = Plugin.getPluginManager().getExampleProgram();
			
			for (ContextMenuIf element : menuIfList) {
				if (element instanceof SeparatorMenuItem) {
				} else if (element instanceof ConfigMenuItem
						|| element instanceof LeaveFullScreenMenuItem) {
				} else {
					ActionMenu actionMenu = element.getContextMenuActions(exampleProgram);
					
					final ArrayList<ActionMenu> listActionMenus = new ArrayList<ActionMenu>();
					
					loadActionMenus(actionMenu,listActionMenus);
					
					if (actionMenu != null && listActionMenus.isEmpty()) {
						mComboBox.addItem(new ContextMenuActionEntry(element,actionMenu));
					}
					else if(!listActionMenus.isEmpty()) {
					  for(ActionMenu menu : listActionMenus) {
					    mComboBox.addItem(new ContextMenuActionEntry(element,menu));
					  }
					}
				}
			}
		}
	}
	
	private static void loadActionMenus(final ActionMenu actionMenu, final ArrayList<ActionMenu> result) {
	  if(actionMenu != null) {
	    
  	  final ActionMenu[] subItems = actionMenu.getSubItems();
  	  
  	  if(subItems != null) {
  	    for(ActionMenu item : subItems) {
  	      
  	      if(item.hasSubItems()) {
  	        loadActionMenus(item, result);
  	      }
  	      else if(item.getActionId() != ActionMenu.ID_ACTION_NONE) {
  	        result.add(item);
  	      }
  	    }
  	  }
	  }
	}
	
	
	
	private final static class ContextMenuActionEntry {
	  private ContextMenuIf mContextMenuIf;
	  private ActionMenu mActionMenu;
	  
	  public ContextMenuActionEntry(ContextMenuIf contextMenuIf, ActionMenu actionMenu) {
      mContextMenuIf = contextMenuIf;
      mActionMenu = actionMenu;
    }
	  
	  @Override
	  public boolean equals(Object obj) {
	    if(obj instanceof ContextMenuActionEntry) {
	      return mContextMenuIf.equals(((ContextMenuActionEntry) obj).mContextMenuIf) && (mActionMenu == null && ((ContextMenuActionEntry) obj).mActionMenu == null || (mActionMenu != null && ((ContextMenuActionEntry) obj).mActionMenu != null && mActionMenu.getActionId() == ((ContextMenuActionEntry) obj).mActionMenu.getActionId()));
	    }
	    
	    return super.equals(obj);
	  }
	}
}