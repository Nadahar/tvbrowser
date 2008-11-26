/*
 * TV-Pearl by Reinhard Lehrbaum
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
 */
package tvpearlplugin;

import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;

import javax.swing.*;

import devplugin.*;
import util.ui.*;

public class PearlDialog extends JDialog implements WindowClosingIf
{
	private static final long serialVersionUID = 1L;

	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TVPearlPlugin.class);

	private JScrollPane mScrollPane;
	private JList mDataList;
	private JButton mCloseBn;
	private JButton mUpdateBn;
	private DefaultListModel mProgramList;
	private TVPProgram mPopupProgram = null;

  public PearlDialog(Dialog dialog)
  {
    super(dialog, true);

    setTitle(mLocalizer.msg("name", "TV Pearl"));
    createGUI();
    UiUtilities.registerForClosing(this);
  }

	public PearlDialog(Frame frame)
	{
		super(frame, true);

		setTitle(mLocalizer.msg("name", "TV Pearl"));
		createGUI();
		UiUtilities.registerForClosing(this);
	}

	private void createGUI()
	{
		JPanel main = new JPanel(new BorderLayout());
		main.setBorder(UiUtilities.DIALOG_BORDER);

		setContentPane(main);

		mProgramList = new DefaultListModel();
		mDataList = new JList(mProgramList)
		{
			private static final long serialVersionUID = 1L;

			public JToolTip createToolTip()
			{
				return new TVPearlToolTip();
			}

			public String getToolTipText(MouseEvent evt)
			{
				int index = locationToIndex(evt.getPoint());

				Object item = getModel().getElementAt(index);
				if (item instanceof TVPProgram)
				{
					TVPProgram p = (TVPProgram) item;
					//return "<html>" + p.getInfo().replaceAll("\n", "<br>") + "</html>";
					HTTPConverter converter = new HTTPConverter();
					return converter.convertToString(p.getInfo());
				}
				return null;
			}
		};
		mDataList.setCellRenderer(new TVPearlListCellRenderer());
		mDataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mDataList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				PluginManager mng = Plugin.getPluginManager();
				int index = mDataList.locationToIndex(e.getPoint());

				if (mDataList.getModel().getElementAt(index) instanceof TVPProgram)
				{
					
					TVPProgram p = (TVPProgram) mDataList.getModel().getElementAt(index);
					if (p.getProgramID().length() > 0)
					{
						Program prog = mng.getProgram(new Date(p.getStart()), p.getProgramID());

						if (prog != null)
						{
							if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
							{
								mng.handleProgramDoubleClick(prog, TVPearlPlugin.getInstance());
							}
							else if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1))
							{
								mng.handleProgramMiddleClick(prog);
							}
						}
					}
					if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1 && e.isShiftDown())
					{
						TVPearlPlugin.getInstance().showPearlInfo(p);
					}
				}
			}

			public void mouseReleased(MouseEvent e)
			{
				checkPopup(e);
			}

			public void mousePressed(MouseEvent e)
			{
				checkPopup(e);
			}
		});

		mScrollPane = new JScrollPane(mDataList);
		main.add(mScrollPane, BorderLayout.CENTER);

		JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		main.add(buttonPn, BorderLayout.SOUTH);

		mUpdateBn = new JButton(mLocalizer.msg("update", "Update"));
		mUpdateBn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					mUpdateBn.setEnabled(false);
					getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					// TVPearlPlugin.getInstance().update();
					TVPearlPlugin.getInstance().run();
				}
				catch (Exception e)
				{
					mUpdateBn.setEnabled(true);
				}
				finally
				{
					getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
		mUpdateBn.setVisible(TVPearlPlugin.getInstance().getPropertyBoolean("UpdateManual"));
		buttonPn.add(mUpdateBn);

		mCloseBn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
		mCloseBn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				dispose();
			}
		});
		buttonPn.add(mCloseBn);

		getRootPane().setDefaultButton(mCloseBn);

		pack();

		updateProgramList();
	}

	private void checkPopup(final MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			PluginManager manager = Plugin.getPluginManager();
			Program program;
			JPopupMenu popup = null;
			int index = mDataList.locationToIndex(e.getPoint());

			if (mDataList.getModel().getElementAt(index) instanceof TVPProgram)
			{
				mDataList.setSelectedIndex(index);
				TVPProgram p = (TVPProgram) mDataList.getModel().getElementAt(index);
				if (p.getProgramID().length() > 0)
				{
					program = manager.getProgram(new Date(p.getStart()), p.getProgramID());

					if (program != null)
					{
						popup = manager.createPluginContextMenu(program, null);
					}
				}
				if (popup == null)
				{
					mPopupProgram = p;
					popup = new JPopupMenu();
					JMenuItem item = new JMenuItem(mLocalizer.msg("comment", "TV Pearl comment"));
					item.setIcon(TVPearlPlugin.getInstance().getSmallIcon());
					item.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent arg0)
						{
							if (mPopupProgram != null)
							{
								TVPearlPlugin.getInstance().showPearlInfo(mPopupProgram);
							}
						}
					});
					popup.add(item);
				}

				final JPopupMenu openPopup = popup;
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						openPopup.show(mDataList, e.getX() - 15, e.getY() - 15);
					}
				});
			}
		}
	}

	public void updateProgramList()
	{
		Calendar now = Calendar.getInstance();
		int index = 0;

		mProgramList.clear();
		for (TVPProgram item : TVPearlPlugin.getInstance().getProgramList())
		{
			mProgramList.addElement(item);
			if (now.compareTo(item.getStart()) > 0)
			{
				index++;
			}
		}
		if (mUpdateBn != null)
		{
			mUpdateBn.setEnabled(true);
		}
		mDataList.revalidate();
		mDataList.repaint();
		if (mProgramList.getSize() > 0)
		{
			mDataList.setSelectedIndex(0);
			if (mProgramList.getSize() > index)
			{
				mDataList.setSelectedIndex(index);
				mDataList.ensureIndexIsVisible(index);
			}
		}
	}

	public void close()
	{
		dispose();
	}

	public void update()
	{
		mDataList.repaint();
	}
}
