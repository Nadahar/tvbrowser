package verysimplelist;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import devplugin.Program;

public class VerySimpleListDialog extends JDialog {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(VerySimpleListDialog.class);

  public VerySimpleListDialog(Frame parent, String caption, ArrayList<Program> list) {
    super(parent,true);
    Rectangle screenRect = parent.getGraphicsConfiguration().getBounds();
    // setSize((screenRect.width > 848) ? 800 : screenRect.width - 48, (screenRect.height > 648) ? 600 : screenRect.height - 48);
    setSize(screenRect.width - 80, screenRect.height - 80);
    setTitle(caption);

    JPanel contentpane = (JPanel)getContentPane();
    contentpane.setLayout(new BorderLayout(0,12));
    contentpane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    JPanel mListPanel = new JPanel();
    mListPanel.setLayout(new BoxLayout(mListPanel, BoxLayout.Y_AXIS));
    GridBagLayout gridbag = new GridBagLayout();
    mListPanel.setLayout(gridbag);

    if (list != null) {
      VerySimpleListUtilities.make4Headers(mListPanel, gridbag, mLocalizer.msg("time", "Time"), mLocalizer.msg("channel", "Channel"), mLocalizer.msg("program", "Program"), mLocalizer.msg("episode", "Episode"));

      Iterator<Program> it = list.iterator();
      while ( it.hasNext() )
      {
        Program prog = it.next();
        //mListPanel.add(createListItemPanel(prog));

        VerySimpleListUtilities.make4Labels(mListPanel, prog, gridbag);
     }
    }

    JScrollPane mScrollPane = new JScrollPane(mListPanel);
    mScrollPane.getVerticalScrollBar().setUnitIncrement(30);
    mScrollPane.getHorizontalScrollBar().setUnitIncrement(30);
    contentpane.add(mScrollPane, BorderLayout.CENTER);

    JPanel btnPanel = new JPanel(new BorderLayout());
    JButton closeBtn = new JButton(mLocalizer.msg("close", "Close"));
    getRootPane().setDefaultButton(closeBtn);

    closeBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        setVisible(false); // hide();
      }
    });

    btnPanel.add(closeBtn,BorderLayout.EAST);
    contentpane.add(btnPanel,BorderLayout.SOUTH);
  }

  /* VerySimpleListDialog(Frame parent, String caption, String msg)
  public VerySimpleListDialog(Frame parent, String caption, String msg) {
    super(parent,true);
    Rectangle screenRect = parent.getGraphicsConfiguration().getBounds();
    // setSize((screenRect.width > 848) ? 800 : screenRect.width - 48, (screenRect.height > 648) ? 600 : screenRect.height - 48);
    setSize(screenRect.width - 80, screenRect.height - 80);
    setTitle(caption);

    JPanel contentpane = (JPanel)getContentPane();
    contentpane.setLayout(new BorderLayout(0,12));
    contentpane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    JPanel mListPanel=new JPanel();
    mListPanel.setLayout(new BoxLayout(mListPanel,BoxLayout.Y_AXIS));

    JScrollPane mScrollPane = new JScrollPane(mListPanel);
    mScrollPane.getVerticalScrollBar().setUnitIncrement(30);
    mScrollPane.getHorizontalScrollBar().setUnitIncrement(30);
    contentpane.add(mScrollPane, BorderLayout.CENTER);

    JEditorPane ep = new JEditorPane();
    ep.setContentType("text/html");
    ep.setText(msg);
    ep.setEditable(false);
    mScrollPane.getViewport().add(ep);

    JPanel btnPanel=new JPanel(new BorderLayout());
    JButton closeBtn = new JButton(mLocalizer.msg("close", "Close"));
    getRootPane().setDefaultButton(closeBtn);

    closeBtn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        setVisible(false); // hide();
      }
    });

    btnPanel.add(closeBtn,BorderLayout.EAST);
    contentpane.add(btnPanel,BorderLayout.SOUTH);
  }
  */
}
