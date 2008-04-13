package util.ui;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.*;

/**
 * A scrolling panel for credits
 *
 * @since 2.7
 */
public class CreditsScrollerPanel extends JScrollPane implements ActionListener {
  private int scrollOffset;
  private Timer timer;
  private boolean firstTime = true;
  private int locationY;

  private CreditsScrollerPanel(JComponent component, int delay, int scrollOffset) {
    super(component);

    this.scrollOffset = scrollOffset;

    setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

    component.setVisible(false);
    getViewport().setBackground(component.getBackground());

    timer = new Timer(delay, this);
  }

  /**
   * Start the scrolling. Please don't forgett to call stopScrolling 
   * at the end
   */
  public void startScrolling() {
    JComponent component = (JComponent) getViewport().getView();
    locationY = getViewport().getExtentSize().height;
    component.setVisible(true);
    component.setLocation(0, locationY);
    timer.start();
  }

  /**
   * Stops the scrolling
   */
  public void stopScrolling() {
    timer.stop();
  }

  public void actionPerformed(ActionEvent e) {
    JComponent component = (JComponent) getViewport().getView();
    locationY -= scrollOffset;
    component.setLocation(0, locationY);

    if (component.getPreferredSize().height + locationY < 0) {
      locationY = getViewport().getExtentSize().height;
      component.setVisible(true);
      component.setLocation(0, locationY);
    }
  }

  public void paint(Graphics graphics) {
    super.paint(graphics);
    final Graphics2D g2 = (Graphics2D) graphics;

    int w = getWidth();
    int h = 70;
    GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 255, 255, 255), 2, h, new Color(255, 255, 255, 0), false);
    g2.setPaint(gradient);
    g2.fillRect(0, 0, w, h);

    gradient = new GradientPaint(0, getHeight() - h, new Color(255, 255, 255, 0), 2, getHeight(), new Color(255, 255, 255, 255), false);
    g2.setPaint(gradient);
    g2.fillRect(0, getHeight() - h, w, h);
  }

  /**
   * Creates a scroller. The Text has to be HTML
   * @param text HTML that is displayed in the scroller
   * @return Scroller
   */
  public static CreditsScrollerPanel createScroller(String text) {
    JEditorPane textPane = new JEditorPane();
    textPane.setEditable(false);
    textPane.setContentType("text/html");


    textPane.setText(text);

    return new CreditsScrollerPanel(textPane, 50, 1);
  }

}
