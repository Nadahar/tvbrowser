package util.ui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;

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

    setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

    component.setVisible(false);
    getViewport().setBackground(component.getBackground());

    timer = new Timer(delay, this);
  }

  /**
   * Start the scrolling. Please don't forget to call stopScrolling
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
