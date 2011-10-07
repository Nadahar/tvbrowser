package personahandler;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.persona.Persona;
import util.ui.persona.PersonaInfo;

public class PersonaEditDialog extends JDialog implements WindowClosingIf {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PersonaEditDialog.class);
  private JTextField mName;
  private JTextField mDescription;
  
  private JTextField mHeaderFile;
  private JTextField mFooterFile;
  private JTextField mIconFile;
  
  private ColorPanel mTextColor;
  private ColorPanel mShadowColor;
  private ColorPanel mAccentColor;
  
  private Properties mProperties;
  private File mPropFile;
  private File mLastFile;
  
  private boolean mSuccess;
  
  public PersonaEditDialog(PersonaInfo info) {
    super(UiUtilities.getLastModalChildOf(PersonaHandler.getInstance().getSuperFrame()));
    setModal(true);
    setTitle(mLocalizer.msg("editPersona","Edit Persona"));
    
    mLastFile = new File("");
    mSuccess = false;
    
    try {
      mPropFile = new File(Persona.getUserPersonaDir(),info.getId() + "/persona.prop");
      mProperties = new Properties();
      
      if(mPropFile.isFile()) {
        FileInputStream in = new FileInputStream(mPropFile);
        mProperties.load(in);
        in.close();
      }
      
    }catch(Exception e) {e.printStackTrace();}
    
    FormLayout mainLayout = new FormLayout("5dlu,default,5dlu,default:grow,5dlu,default,5dlu",
    "default,5dlu,default,2dlu,default,10dlu,default,5dlu,default,2dlu,default,2dlu,default,10dlu,default,5dlu,fill:default,2dlu,fill:default,2dlu,fill:default,5dlu,default,5dlu,default");
    
    PanelBuilder pb = new PanelBuilder(mainLayout,(JPanel)getContentPane());
    pb.setDefaultDialogBorder();
    
    int y = 1;
    
    mName = new JTextField(info.getName());
    mDescription = new JTextField(info.getDescription());
    
    mHeaderFile = new JTextField("header.jpg");
    mHeaderFile.setEditable(false);
    mFooterFile = new JTextField("footer.jpg");
    mFooterFile.setEditable(false);
    mIconFile = new JTextField("icon");
    mIconFile.setEditable(false);
    
    mTextColor = new ColorPanel(info.getTextColor());
    mShadowColor = new ColorPanel(info.getShadowColor());
    mAccentColor = new ColorPanel(info.getAccentColor());
    
    JButton selectHeader = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
    selectHeader.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectFile(mHeaderFile);
      }
    });
    
    JButton selectFooter = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
    selectFooter.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectFile(mFooterFile);
      }
    });

    JButton selectIcon = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
    selectIcon.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectFile(mIconFile);
      }
    });
    
    JButton selectTextColor = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
    selectTextColor.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectColor(mTextColor);
      }
    });
    
    JButton selectShadowColor = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
    selectShadowColor.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectColor(mShadowColor);
      }
    });

    JButton selectAccentColor = new JButton(Localizer.getLocalization(Localizer.I18N_SELECT));
    selectAccentColor.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        selectColor(mAccentColor);
      }
    });
    
    JButton save = new JButton(mLocalizer.msg("save","Save"));
    save.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        save();
      }
    });
    
    JButton exit = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    exit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    
    FormLayout layout = new FormLayout("default:grow,default,5dlu,default,default:grow","default");
    layout.setColumnGroups(new int[][] {{2,4}});
    
    JPanel buttonPanel = new JPanel(layout);
    
    buttonPanel.add(exit, CC.xy(2,1));
    buttonPanel.add(save, CC.xy(4,1));
    
    pb.addSeparator(mLocalizer.msg("basicSettings","Basic settings"), CC.xyw(1,y++,7));
    pb.addLabel(mLocalizer.msg("name","Name:"), CC.xy(2,++y));
    pb.add(mName, CC.xyw(4,y++,3));
    pb.addLabel(mLocalizer.msg("description","Description:"), CC.xy(2,++y));
    pb.add(mDescription, CC.xyw(4,y++,3));
    
    pb.addSeparator(mLocalizer.msg("images","Images"), CC.xyw(1,++y,7));
    y++;
    pb.addLabel(mLocalizer.msg("header","Header:"), CC.xy(2,++y));
    pb.add(selectHeader, CC.xy(6,y));
    pb.add(mHeaderFile, CC.xy(4,y++));
    pb.addLabel(mLocalizer.msg("footer","Footer:"), CC.xy(2,++y));
    pb.add(selectFooter, CC.xy(6,y));
    pb.add(mFooterFile, CC.xy(4,y++));
    pb.addLabel(mLocalizer.msg("icon","Icon:"), CC.xy(2,++y));
    pb.add(selectIcon, CC.xy(6,y));
    pb.add(mIconFile, CC.xy(4,y++));
    
    pb.addSeparator(mLocalizer.msg("colors","Colors"), CC.xyw(1,++y,7));
    y++;
    pb.addLabel(mLocalizer.msg("text","Text:"), CC.xy(2,++y));
    pb.add(selectTextColor, CC.xy(6,y));
    pb.add(mTextColor, CC.xy(4,y++));
    pb.addLabel(mLocalizer.msg("shadow","Shadow:"), CC.xy(2,++y));
    pb.add(selectShadowColor, CC.xy(6,y));
    pb.add(mShadowColor, CC.xy(4,y++));
    pb.addLabel(mLocalizer.msg("accent","Accent:"), CC.xy(2,++y));
    pb.add(selectAccentColor, CC.xy(6,y));
    pb.add(mAccentColor, CC.xy(4,y++));
    pb.addSeparator("",CC.xyw(1,++y,7));
    y++;
    pb.add(buttonPanel, CC.xyw(1,++y,7));
    
    PersonaHandler.getInstance().layoutWindow("personaEditDialog",this,new Dimension(400,0));
    UiUtilities.registerForClosing(this);
  }
  
  private void selectFile(JTextField target) {
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(mLastFile);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    
    if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      if(chooser.getSelectedFile() != null) {
        mLastFile = chooser.getSelectedFile().getParentFile();
        target.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    }
  }
  
  private void selectColor(ColorPanel colorPanel) {
    Color c = JColorChooser.showDialog(this,"",colorPanel.getColor());
    
    if(c != null) {
      colorPanel.setColor(c);
    }
  }
  
  private void save() {
    if(mPropFile.isFile()) {
      mProperties.setProperty(Persona.NAME_KEY,mName.getText());
      mProperties.setProperty(Persona.DESCRIPTION_KEY,mDescription.getText());
      
      Color c = mTextColor.getColor();
      mProperties.setProperty(Persona.TEXT_COLOR_KEY,c.getRed() + "," + c.getGreen() + "," + c.getBlue());
      
      c = mShadowColor.getColor();
      mProperties.setProperty(Persona.SHADOW_COLOR_KEY,c.getRed() + "," + c.getGreen() + "," + c.getBlue());
      
      c = mAccentColor.getColor();
      mProperties.setProperty(Persona.ACCENT_COLOR_KEY,c.getRed() + "," + c.getGreen() + "," + c.getBlue());
      
      mSuccess = copy(mHeaderFile, "header.jpg", 2500, 200);
      mSuccess = mSuccess && copy(mFooterFile, "footer.jpg", 2500, 100);
      mSuccess = mSuccess && copy(mIconFile, "icon", 32, 32);
      
      if(mSuccess) {
        try {
          FileOutputStream out = new FileOutputStream(mPropFile);
          mProperties.store(out,"");
          out.close();
        }catch(Exception e) {
          mSuccess = false;
        }
        
        if(mSuccess) {
          Persona.getInstance().updatePersona(mPropFile.getParentFile().getName());
          close();
        }
      }
    }
  }
  
  private boolean copy(JTextField source, String targetName, int width, int height) {
    String sourceText = source.getText();
    File sourceFile = new File(sourceText);
    
    if(!sourceText.equals(targetName) && sourceFile.isFile()) {
      try {
        BufferedImage headerImg = ImageIO.read(sourceFile);

        if(headerImg.getWidth() >= width && headerImg.getHeight() >= height) {
          File targetFile = new File(mPropFile.getParent(),targetName);
          
          boolean copy = true;
          
          if(targetFile.isFile()) {
            copy = targetFile.delete();
          }
          
          if(copy) {
            IOUtilities.copy(sourceFile,targetFile);
            source.setText(targetName);
            return true;
          }
        }
      } catch (IOException e) {}
      
      return false;
    }
    
    return true;
  }
  
  
  @Override
  public void close() {
    // TODO Auto-generated method stub
    setVisible(false);
  }
  
  public boolean wasSuccessful() {
    return mSuccess;
  }
}
