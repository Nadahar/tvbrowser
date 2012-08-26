package tvbrowser.ui.mainframe.toolbar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.MenuHelpTextAdapter;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.persona.Persona;

/**
 * MySwing: Advanced Swing Utilites
 * Copyright (C) 2005  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

/**
 * to use this feature replace:
 *   frame.getContentPane().add(toolbar, BorderLayout.NORTH);
 * with
 *   frame.getContentPane().add(MoreButton.wrapToolBar(toolBar), BorderLayout.NORTH);
 *
 * @author Santhosh Kumar T (santhosh@in.fiorano.com)
 *
 * Changed for support of the TV-Browser ToolBar.
 */

public class MoreButton extends JToggleButton implements ActionListener {
    private JToolBar toolbar;
    private JPopupMenu mPopupMenu;
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(MoreButton.class);
    private JLabel mLabel;
    
    private MoreButton(final JToolBar toolbar, MainFrame mainFrame, final JLabel statusLabel){
      mLabel = statusLabel;
      
        if((((ToolBar)toolbar).getStyle() == (ToolBar.STYLE_TEXT | ToolBar.STYLE_ICON)) || ((ToolBar)toolbar).getStyle() == ToolBar.STYLE_TEXT ) {
          setText(mLocalizer.msg("more","more"));
        }

        setToolTipText(mLocalizer.msg("moreTooltip","Opens a menu with the Toolbar Buttons that aren't visible"));

        if(((ToolBar)toolbar).getStyle() != ToolBar.STYLE_TEXT) {
          if(((ToolBar)toolbar).useBigIcons()) {
            setIcon(IconLoader.getInstance().getIconFromTheme("emblems", "emblem-symbolic-link", 22));
          } else {
            setIcon(IconLoader.getInstance().getIconFromTheme("emblems", "emblem-symbolic-link", 16));
          }
        }

        this.toolbar = toolbar;
        addActionListener(this);
        setBorderPainted(false);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.BOTTOM);
        setFont(ToolBar.TEXT_FONT);
        addMouseAdapter(this);
        addMouseListener(new MouseAdapter() {
          public void mouseEntered(MouseEvent e) {
            if (!isSelected()) {
              setBorderPainted(true);
            }
            
            if(getToolTipText() != null && getToolTipText().trim().length() > 0) {
              statusLabel.setText(getToolTipText());
            }
          }

          public void mouseExited(MouseEvent e) {
            if (!isSelected()) {
              setBorderPainted(false);
            }
            
            if(getToolTipText() != null && getToolTipText().equals(statusLabel.getText())) {
              statusLabel.setText("");
            }
          }
        });
        setFocusPainted(false);
        setOpaque(false);

        // hide & seek
        toolbar.addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent e){
              setVisible(toolbar.getComponentCount() > 1 && !isVisible(toolbar.getComponent(toolbar.getComponentCount()-1), null));
            }
        });

        mainFrame.addWindowFocusListener(new WindowFocusListener() {
          public void windowGainedFocus(WindowEvent e) {
          }

          public void windowLostFocus(WindowEvent e) {
           if(mPopupMenu != null && mPopupMenu.isVisible()) {
            mPopupMenu.setVisible(false);
          }
          }
        });
    }

    // check visibility
    // partially visible is treated as not visible
    private boolean isVisible(Component comp, Rectangle rect){
        if(rect==null) {
          rect = toolbar.getVisibleRect();
        }
        String location = ((ToolBar)toolbar).getToolbarLocation();

        if(location.compareTo(BorderLayout.NORTH) == 0) {
          return comp.getLocation().x+comp.getWidth() <=rect.getWidth();
        } else {
          return comp.getLocation().y+comp.getHeight() <=rect.getHeight();
        }
    }

    private static void addMouseAdapter(final JComponent c) {
      
      
      c.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          if(e.isPopupTrigger()) {
            ContextMenu menu = new ContextMenu(c);
            menu.show(e.getX(), e.getY());
          }
        }

        public void mouseReleased(MouseEvent e) {
          if(e.isPopupTrigger()) {
            ContextMenu menu = new ContextMenu(c);
            menu.show(e.getX(), e.getY());
          }
        }

      });
    }

    public void actionPerformed(ActionEvent e) {
        Component[] comp = toolbar.getComponents();
        Action[] actions = DefaultToolBarModel.getInstance().getActions();

        Rectangle visibleRect = toolbar.getVisibleRect();
        for(int i = 0; i<comp.length; i++){
            if(!isVisible(comp[i], visibleRect)){
                mPopupMenu = new JPopupMenu();
                for(; i<comp.length; i++){
                    if(comp[i] instanceof AbstractButton) {
                      if(actions[i] != null) {                        
                        new MenuHelpTextAdapter(mPopupMenu.add(actions[i]),
                            actions[i].getValue(Action.SHORT_DESCRIPTION).toString(), mLabel);
                      }
                    } else if(comp[i] instanceof JSeparator) {
                      mPopupMenu.addSeparator();
                    }
                }

                //on popup close make more-button unselected
                mPopupMenu.addPopupMenuListener(new PopupMenuListener(){
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e){
                        setSelected(false);
                        setBorderPainted(false);
                    }
                    public void popupMenuCanceled(PopupMenuEvent e){}
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e){}
                });
                mPopupMenu.show(this, 0, getHeight());
            }
        }
    }

    public static Component wrapToolBar(ToolBar toolbar, MainFrame mainFrame, JLabel statusLabel) {
      JToolBar moreToolbar = new JToolBar() {
        protected void paintComponent(Graphics g) {
          if(!UiUtilities.isGTKLookAndFeel() || Persona.getInstance().getHeaderImage() == null) {
            super.paintComponent(g);
          }
        }
      };
      moreToolbar.setOpaque(false);
      moreToolbar.setLayout(new GridLayout());
      moreToolbar.setRollover(true);
      moreToolbar.setFloatable(false);
      moreToolbar.add(new MoreButton(toolbar,mainFrame,statusLabel));
      addMouseAdapter(moreToolbar);

        JPanel panel = new JPanel(new BorderLayout(0,0));
        panel.setOpaque(false);

        panel.add(toolbar, BorderLayout.CENTER);

        if (toolbar.getToolbarLocation().compareTo(BorderLayout.NORTH) == 0) {
          moreToolbar.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
          panel.add(moreToolbar, BorderLayout.EAST);
        }
        else {
          moreToolbar.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
          panel.add(moreToolbar, BorderLayout.SOUTH);
        }

        return panel;
    }
    
    protected void paintComponent(Graphics g) {
      if(Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null && Persona.getInstance().getShadowColor() != null) {
        if(UiUtilities.isGTKLookAndFeel()) {
          if(isBorderPainted()) {
            g.setColor(UIManager.getColor("List.selectionBackground"));
            g.fillRect(0, 0, getWidth(), getHeight());
          }
          if(isSelected()) {
            g.draw3DRect(0,0,getWidth(),getHeight(),false);
          }
        }
        
        if(Settings.propToolbarButtonStyle.getString().equals("text&icon")) {
          getIcon().paintIcon(this,g,getWidth()/2-getIcon().getIconWidth()/2,getInsets().top);
        }
        
        if(Settings.propToolbarButtonStyle.getString().contains("text")) {
          FontMetrics metrics = g.getFontMetrics(getFont());
          int textWidth = metrics.stringWidth(getText());
        
          if(!Persona.getInstance().getShadowColor().equals(Persona.getInstance().getTextColor())) {
            g.setColor(Persona.getInstance().getShadowColor());
            
            g.drawString(getText(),getWidth()/2-textWidth/2+1,getHeight()-getInsets().bottom-getInsets().top+1);
            g.drawString(getText(),getWidth()/2-textWidth/2+2,getHeight()-getInsets().bottom-getInsets().top+2);
          }
          
          g.setColor(Persona.getInstance().getTextColor());
          g.drawString(getText(),getWidth()/2-textWidth/2,getHeight()-getInsets().bottom-getInsets().top);
        }
        else {
          super.paintComponent(g);
        }
      }
      else {
        super.paintComponent(g);
      }
    }
}
