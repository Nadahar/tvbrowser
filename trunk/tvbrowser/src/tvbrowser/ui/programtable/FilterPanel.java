package tvbrowser.ui.programtable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.persona.Persona;
import devplugin.ProgramFilter;

/**
 * This Class represents the Panel above the ProgramPanel. If a
 * Filter is selected, this Panel is visible and shows the name of
 * the current selected filter
 *
 * @author bodum
 */
public class FilterPanel extends JPanel {
  /** Label that is used */
  private JLabel mFilterLabel;

  /**
   * remember current filter name to avoid repeated UI updates
   */
  private String mCurrentName;
  private JButton mDeactivate;
  private boolean mDefaultRolloverEnabledState;
  
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(FilterPanel.class);

  /**
   * Create the Filter-Panel
   * @param keyListener The key listener for FAYT.
   */
  public FilterPanel(KeyListener keyListener) {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    setBackground(Color.WHITE);
    setOpaque(false);

    mFilterLabel = new JLabel() {
      protected void paintComponent(Graphics g) {
        if(Persona.getInstance().getHeaderImage() != null) {

          String info = mLocalizer.msg("filterActive", "Active Filter:") + ": ";
          
          Font boldFont = getFont().deriveFont(Font.BOLD);
          
          FontMetrics metrics = g.getFontMetrics(boldFont);
          int textWidth = metrics.stringWidth(info);
          int baseLine = getBaseline(getWidth(),getHeight());
        
      
        if(!Persona.getInstance().getShadowColor().equals(Persona.getInstance().getTextColor())) {
          g.setColor(Persona.getInstance().getShadowColor());
          
          g.setFont(boldFont);
          g.drawString(info,getIconTextGap()+getInsets().left+1,baseLine+1);
          g.drawString(info,getIconTextGap()+getInsets().left+2,baseLine+2);
          
          g.setFont(getFont());
          g.drawString(mCurrentName,getIconTextGap()+getInsets().left+1+textWidth,baseLine+1);
          g.drawString(mCurrentName,getIconTextGap()+getInsets().left+2+textWidth,baseLine+2);
        }
        
        g.setColor(Persona.getInstance().getTextColor());
        
        g.setFont(boldFont);
        g.drawString(info,getIconTextGap()+getInsets().left,baseLine);
        
        g.setFont(getFont());
        g.drawString(mCurrentName,getIconTextGap()+getInsets().left+textWidth,baseLine);
      }
      else {
        super.paintComponent(g);
      }
      }
    };
    mFilterLabel.addKeyListener(keyListener);
    
    mFilterLabel.setHorizontalAlignment(SwingConstants.LEFT);
    add(mFilterLabel, BorderLayout.CENTER);
    mDeactivate = new JButton(mLocalizer.msg("deactivate", "Deactivate")) {
      protected void paintComponent(Graphics g) {
        if(Persona.getInstance().getHeaderImage() != null && Persona.getInstance().getTextColor() != null && Persona.getInstance().getShadowColor() != null) {
          Persona.paintButton(g,this);
        }
        else {
          super.paintComponent(g);
        }
      }
    };
    mDeactivate.addKeyListener(keyListener);
    mDefaultRolloverEnabledState = mDeactivate.isRolloverEnabled();
    mDeactivate.addActionListener(new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        MainFrame.getInstance().setProgramFilter(FilterManagerImpl.getInstance().getDefaultFilter());
      };
    });
    
    if(Persona.getInstance().getHeaderImage() != null) {
      mDeactivate.setRolloverEnabled(true);
      mDeactivate.setOpaque(false);
    }
    
    add(mDeactivate, BorderLayout.EAST);
  }

  /**
   * Set the Name of the Filter
   * @param name Name of the Filter
   */
  private void setFilterLabel(String name) {
    // avoid repainting during repeated filter updates
    if (name.equals(mCurrentName)) {
      return;
    }
    mCurrentName = name;
    mFilterLabel.setText("<html><body><b>" + mLocalizer.msg("filterActive", "Active Filter:")+ ":</b> "+name+"</body></html>");
  }

  /**
   * Set the current Filter.
   * This updates the JLabel to represent the current Filter
   * @param filter current selected Filter
   */
  public void setCurrentFilter(ProgramFilter filter) {
    setFilterLabel(filter.getName());
  }

  /**
   * Paints the component
   */
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2d = (Graphics2D)g;

    int width = getWidth();
    int height = getHeight();

    Color c2 = Persona.getInstance().getTextColor() != null ? Persona.getInstance().getTextColor() : UIManager.getColor("List.selectionBackground");
    Color c = Persona.getInstance().getAccentColor() != null ? Persona.getInstance().getAccentColor() : getBackground();
    
    if(Persona.getInstance().getHeaderImage() != null) {
      c = Persona.testPersonaForegroundAgainst(c);
      c2 = new Color(c2.getRed(),c2.getGreen(),c2.getBlue(),200);
    }
    
    // Create the gradient paint
    GradientPaint paint = new GradientPaint((float)width / 3, 0, c, width, height, c2, false);

    g2d.setPaint(paint);
    g2d.fillRect(0, 0, width, height);
  }
  
  /**
   * Updates the filter panel on Persona change.
   */
  public void updatePersona() {
    if(mDeactivate != null) {
      if(Persona.getInstance().getHeaderImage() != null) {
        mDeactivate.setRolloverEnabled(true);
        mDeactivate.setOpaque(false);
      }
      else {
        mDeactivate.setRolloverEnabled(mDefaultRolloverEnabledState);
        mDeactivate.setOpaque(true);
      }
    }
  }
  

}
