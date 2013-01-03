package captureplugin.drivers.dreambox.connector.cs;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class JTabbedPaneRefresh extends JTabbedPane implements ChangeListener {

	public void stateChanged(ChangeEvent e) {
		JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
		JPanelRefreshAbstract panel = (JPanelRefreshAbstract) tabbedPane
				.getSelectedComponent();
		panel.refresh();
	}
}
