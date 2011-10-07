package personahandler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.browserlauncher.Launch;
import util.ui.LinkButton;
import util.ui.Localizer;
import util.ui.persona.Persona;
import util.ui.persona.PersonaInfo;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

public class PersonaHandlerPanel extends JPanel {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PersonaHandlerPanel.class);
  
  private JList mPersonaList;
  private DefaultListModel mPersonaListModel;
  
  private int mSelectedIndex;
  
  private JTextField mURL;
  
  private JButton mInstall;
  private JButton mAddNew;
  private JButton mActivate;
  private JButton mEdit;
  private JButton mDelete;
  private JButton mWebsite;
 // private boolean mShowHeader;
  
  public PersonaHandlerPanel(boolean header) {
   // mShowHeader = header;
    createGui(header);
  }
  
  private void createGui(boolean header) {
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default:grow,default,default:grow,5dlu,default,5dlu","default,5dlu,default,default,5dlu,default,5dlu,default,2dlu,default,2dlu,default,2dlu,default,fill:default:grow,default"),this);
    setOpaque(false);
    mInstall = createButton(mLocalizer.msg("install","Install"),PersonaHandler.getInstance().createImageIcon("actions", "web-search", 16));    
    mInstall.setEnabled(false);
    mInstall.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        installNewPersona();
      }
    });
        
    mURL = new PersonaInstallField();
    LinkButton link = new LinkButton(mLocalizer.msg("searchForPersonas","Search for new Personas"),"https://www.getpersonas.com/");
    
    mPersonaListModel = new DefaultListModel();
    mPersonaList = new JList();
    mPersonaList.setModel(mPersonaListModel);
    mPersonaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mPersonaList.setCellRenderer(new PersonaListCellRenderer());
    final JScrollPane listScrollPane = new JScrollPane(mPersonaList);
    
    mEdit = createButton(Localizer.getLocalization(Localizer.I18N_EDIT),PersonaHandler.getInstance().createImageIcon("actions","document-edit",16));
    mEdit.setEnabled(false);
    mEdit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editSelectedPersona();
      }
    });
    
    mDelete = createButton(Localizer.getLocalization(Localizer.I18N_DELETE),PersonaHandler.getInstance().createImageIcon("actions","edit-delete",16));
    mDelete.setEnabled(false);
    mDelete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        deleteSelectedPersona();
      }
    });
    
    mWebsite= createButton(mLocalizer.msg("website","Website"),PersonaHandler.getInstance().createImageIcon("apps","internet-web-browser",16));
    mWebsite.setEnabled(false);
    mWebsite.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        showWebsite();
      }
    });
    
    mAddNew = createButton(Localizer.getLocalization(Localizer.I18N_ADD),PersonaHandler.getInstance().createImageIcon("actions","document-new",16));
    mAddNew.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        addNewPersona();
      }
    });
    
    mActivate = createButton(mLocalizer.msg("activate","Activate"),PersonaHandler.getInstance().createImageIcon("apps","preferences-desktop-theme",16));
    mActivate.setEnabled(false);
    mActivate.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        activateSelectedPersona();
      }
    });
    
    mPersonaList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          updateButtons(false);
        }
      }
    });
    
    mPersonaList.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(e.isPopupTrigger()) {
          showContextMenu(e.getPoint());
        }
      }
      
      public void mouseReleased(MouseEvent e) {
        if(e.isPopupTrigger()) {
          showContextMenu(e.getPoint());
        }        
      }
      
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
          activateSelectedPersona();
        }
      }
    });
    
    PersonaInfo[] infos = Persona.getInstance().getInstalledPersonas();
    
    for(PersonaInfo info : infos) {
      mPersonaListModel.addElement(info);
      
      if(info.isSelectedPersona()) {
        mPersonaList.setSelectedIndex(mPersonaListModel.getSize()-1);
        mSelectedIndex = mPersonaListModel.getSize()-1;
      }
    }
    
    if(header) {
      pb.addSeparator(mLocalizer.msg("installPersona","Install Persona"),CC.xyw(1,1,7));
    }
    
    pb.add(mURL,CC.xyw(2,3,3));
    pb.add(mInstall,CC.xy(6,3));
    pb.add(link,CC.xy(3,4));
    
    pb.addSeparator(mLocalizer.msg("managePersona","Manage Personas"),CC.xyw(1,6,7));
    pb.add(listScrollPane,CC.xywh(2,8,3,9));
    pb.add(mActivate,CC.xy(6,8));
    pb.add(mAddNew,CC.xy(6,10));
    pb.add(mEdit,CC.xy(6,12));
    pb.add(mDelete,CC.xy(6,14));
    pb.add(mWebsite,CC.xy(6,16));

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        mPersonaList.requestFocus();
        mPersonaList.ensureIndexIsVisible(mSelectedIndex);
      }
    });
  }
  
  private void showWebsite() {
    if(mPersonaList.getSelectedIndex() >= 0) {
      Launch.openURL(((PersonaInfo)mPersonaList.getSelectedValue()).getDetailURL());
    }
  }
  
  private void showContextMenu(Point p) {
    int index = mPersonaList.locationToIndex(p);
    
    if(index >= 0) {
      mPersonaList.setSelectedIndex(index);
    
      JPopupMenu popupMenu = new JPopupMenu();
      
      JMenuItem activate = createMenuItem(mActivate.getText(),mActivate.getIcon());
      activate.setEnabled(mActivate.isEnabled());
      activate.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          activateSelectedPersona();
        }
      });

      JMenuItem addNew = createMenuItem(mAddNew.getText(),mAddNew.getIcon());
      addNew.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          addNewPersona();
        }
      });

      JMenuItem edit = createMenuItem(mEdit.getText(),mEdit.getIcon());
      edit.setEnabled(mEdit.isEnabled());
      edit.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          editSelectedPersona();
        }
      });

      JMenuItem delete = createMenuItem(mDelete.getText(),mDelete.getIcon());
      delete.setEnabled(mDelete.isEnabled());
      delete.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          deleteSelectedPersona();
        }
      });
      
      JMenuItem website = createMenuItem(mWebsite.getText(),mWebsite.getIcon());
      website.setEnabled(mWebsite.isEnabled());
      website.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          showWebsite();
        }
      });
      
      popupMenu.add(activate);
      popupMenu.add(addNew);
      popupMenu.add(edit);
      popupMenu.add(delete);
      popupMenu.addSeparator();
      popupMenu.add(website);
      popupMenu.show(mPersonaList, p.x, p.y);
    }
  }
  
  private void addNewPersona() {
    PersonaInfo info = PersonaHandler.getInstance().addNewPersona();
    
    if(info != null) {
      mPersonaListModel.addElement(info);
      mPersonaList.setSelectedIndex(mPersonaListModel.getSize()-1);
      editSelectedPersona();
      
      mPersonaList.ensureIndexIsVisible(mPersonaListModel.getSize()-1);
    }
  }
  
  private void editSelectedPersona() {
    int index = mPersonaList.getSelectedIndex();
    if(index >= 0) {
      PersonaInfo info = (PersonaInfo)mPersonaList.getSelectedValue();
      
      PersonaEditDialog editDialog = new PersonaEditDialog(info);
      editDialog.setVisible(true);
      
      if(editDialog.wasSuccessful()) {
        mPersonaList.repaint();
      }
    }
  }
  
  private void installNewPersona() {
    PersonaInfo info = PersonaHandler.getInstance().getAndInstallPersona(mURL.getText());
    
    if(info != null) {
      mPersonaListModel.addElement(info);
      mPersonaList.ensureIndexIsVisible(mPersonaListModel.getSize()-1);
    }
  }
  
  private void deleteSelectedPersona() {
    if(mPersonaList.getSelectedIndex() >= 0) {
      int index = mPersonaList.getSelectedIndex();
      PersonaInfo info = (PersonaInfo)mPersonaList.getSelectedValue();
      
      if(PersonaHandler.getInstance().deletePersona(info)) {
        if(index > 0) {
          mPersonaList.setSelectedIndex(index-1);
        }
        else if(mPersonaListModel.size() > 1) {
          mPersonaList.setSelectedIndex(index+1);
        }
        
        mPersonaListModel.removeElement(info);
      }
    }
  }
  
  private void activateSelectedPersona() {
    PersonaInfo info = (PersonaInfo)mPersonaList.getSelectedValue();
    
    if(!info.isSelectedPersona()) {
      Persona.getInstance().activatePersona(info);
      
      updateButtons(true);
    }
  }
  
  private void updateButtons(boolean repaint) {
    PersonaInfo info = (PersonaInfo)mPersonaList.getSelectedValue();
    
    mActivate.setEnabled(!info.isSelectedPersona());
    mEdit.setEnabled(info.isEditable());
    mDelete.setEnabled(info.isEditable() && !info.isSelectedPersona() && !info.getId().equals(Persona.getInstance().getId()));
    mWebsite.setEnabled(mPersonaList.getSelectedIndex() >= 0);
    
    if(mWebsite.isEnabled()) {
      mWebsite.setToolTipText(((PersonaInfo)mPersonaList.getSelectedValue()).getDetailURL());
    }
    
    if(repaint) {
      mPersonaList.repaint();
    }
  }
  
  private class PersonaListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      PersonaInfo info = (PersonaInfo)value;
      
      JLabel iconLabel = new JLabel(info.getIcon());
      JLabel name = new JLabel(info.getName());
      Font font = name.getFont();
      font = font.deriveFont(Font.BOLD);
      font = font.deriveFont((float)font.getSize()+2);
      
      name.setFont(font);
      JLabel description = new JLabel(info.getDescription());
      
      JPanel content = new JPanel(new FormLayout("25dlu,2dlu,100dlu:grow","1dlu,11dlu,11dlu,1dlu"));
      content.add(iconLabel,CC.xywh(1,1,1,4));
      content.add(name,CC.xy(3,2));
      content.add(description,CC.xy(3,3));

      content.setOpaque(isSelected);

      if(PersonaInfo.isRandomPersona(info) && info.isSelectedPersona()) {
        name.setText(name.getText() + ": " + Persona.getInstance().getPersonaInfo(Persona.getInstance().getId()).getName());
      }
      
      if(isSelected) {
        content.setBackground(UIManager.getColor("List.selectionBackground"));
        name.setForeground(UIManager.getColor("List.selectionForeground"));
        description.setForeground(UIManager.getColor("List.selectionForeground"));
      }
      else {
        name.setForeground(UIManager.getColor("List.foreground"));
        description.setForeground(UIManager.getColor("List.foreground"));
        
        if(info.isSelectedPersona()) {
          content.setOpaque(true);
          content.setBackground(new Color(140,255,0,120));
        }
        else if(Persona.getInstance().getId().equals(info.getId())) {
          content.setOpaque(true);
          content.setBackground(new Color(255,255,0,80));
        }
      }
      
      return content;
    }
  }
  
  private class PersonaInstallField extends JTextField implements CaretListener, FocusListener {
    private Color mTextColor;
    private Color mNoTextColor;
    
    public PersonaInstallField() {
      super(mLocalizer.msg("installHelp","Enter/Paste URL of Persona and click Install"));
    
      mTextColor = getForeground();
      
      int r = (mTextColor.getRed()   + getBackground().getRed())   >> 1;
      int g = (mTextColor.getGreen() + getBackground().getGreen()) >> 1;
      int b = (mTextColor.getBlue()  + getBackground().getBlue())  >> 1;
      
      final Color mNoTextColor = new Color(r,g,b);
      
      setForeground(mNoTextColor);
      addCaretListener(this);
      addFocusListener(this);
    }
    
    public void focusLost(FocusEvent e) {
      if(getText().trim().length() == 0) {
        setForeground(mNoTextColor);
        setText(mLocalizer.msg("installHelp","Enter/Paste URL of Persona and click Install"));
      }
    }
    
    @Override
    public void focusGained(FocusEvent e) {
      if(getText().equals(mLocalizer.msg("installHelp","Enter/Paste URL of Persona and click Install"))) {
        setText("");
        setForeground(mTextColor);
      }
    }
    
    @Override
    public void caretUpdate(CaretEvent e) {
      try {
        new URL(getText());
        mInstall.setEnabled(true);
      }catch(MalformedURLException ex) {
        mInstall.setEnabled(false);
      }
    }
    
    public void paste() {
      setText("");
      setForeground(mTextColor);
      
      super.paste();
    }
    
    public void cut() {
      super.cut();
      
      if(getText().length() == 0) {
        setForeground(mNoTextColor);
        setText(mLocalizer.msg("installHelp","Enter/Paste URL of Persona and click Install"));
      }
    }
  }
  
  private JMenuItem createMenuItem(String text, Icon icon) {
    JMenuItem menuItem = new JMenuItem(text,icon);
    return menuItem;
  }
  
  private JButton createButton(String text, Icon icon) {
    /*if(mShowHeader) {
      return Persona.createPersonaButton(text,icon);
    }*/
    
    return new JButton(text,icon);
  }
}
