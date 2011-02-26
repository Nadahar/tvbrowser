/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package wirschauenplugin;

import java.awt.Dimension;
import java.awt.Window;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import util.ui.DontShowAgainMessageBox;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.Date;
import devplugin.Program;

/**
 * this is the main logic class of the plugin. it controls the dialogs
 * and triggers the communication with the different backends (omdb and
 * wirschauen).
 *
 * @author uzi
 */
public class DialogController
{
  /**
   * the parent of the different dialogs.
   */
  private Window mParent;

  /**
   * the program to describe.
   */
  private Program mProgram;


  /**
   * logging for this class
   */
  private static final Logger mLog = Logger.getLogger(DialogController.class.getName());

  /**
   * @param parent the parent of the different dialogs that are opened during the processing
   */
  public DialogController(final Window parent)
  {
    this.mParent = parent;
  }



  /**
   * this is the entry point for the whole processing. it will load the corresponding data from
   * wirschauen (new thread). if there are data and the program is already linked to omdb, it
   * then opens the dialog to insert/change the omdb abstract for the program. otherwise it will
   * ask the user if he wants to create an omdb link or if he wants to insert wirschauen data.
   * the corresponding dialogs are opened in both cases.
   *
   * caution: todays programs or even older programs can't be described via wirschauen. the omdb
   * link dialog is always shown for those programs.
   *
   * @param program the program to describe
   */
  public void startDialogs(final Program program)
  {
    this.mProgram = program;

    //just a 'im doing something' dialog. may be cancelled by the user.
    final LoadingInfoDialog loadingInfoDialog = new LoadingInfoDialog(mParent, "WirSchauen");

    //load the wirschauen data in a new thread.
    Thread loader = new Thread("Load wirschauen data") {
      @Override
      public void run()
      {
        try
        {
          final WirSchauenEvent wirSchauenEvent = WirSchauenConnection.getEvent(program);
          //switch back to the event dispatching thread (swing)
          SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              wirSchauenDataLoaded(wirSchauenEvent, loadingInfoDialog);
            }
          });
        }
        catch (final IOException e)
        {
          handleConnectionError(e, "wirschauen");
        }
      }
    };
    loader.start();

    UiUtilities.centerAndShow(loadingInfoDialog);
  }


  /**
   * must be called from the event dispatching thread after the loadingInfoDialog was built!
   * if the loading dialog was closed by the user (i.e. cancel), the result will be ignored.
   * otherwise the loading dialog is closed and the result will be processed: if the program
   * is linked to the omdb, the corresponding dialog is shown. otherwise the user has to
   * decide if he wants to insert omdb data or wirschauen data.
   *
   * @param wirSchauenEvent the result of the loading task.
   * @param loadingWirSchauenDataDialog the loading-dialog (for closing and cancel-action)
   */
  private void wirSchauenDataLoaded(final WirSchauenEvent wirSchauenEvent, final LoadingInfoDialog loadingWirSchauenDataDialog)
  {
    //fall through if the user aborted the loading of wirschauen data
    if (!loadingWirSchauenDataDialog.isCancelled())
    {
      //close the loading-dialog
      loadingWirSchauenDataDialog.setVisible(false);
      loadingWirSchauenDataDialog.dispose();

      //is the program linked to the omdb?
      if (wirSchauenEvent.getOmdbUrl() == null || "".equals(wirSchauenEvent.getOmdbUrl()))
      {
        //the event has no omdb-link. if it is a future program, show a dialog (choose between
        //omdb-link and wirschauen-stuff). show the omdb-dialog otherwise.
        Object[] options = {WirSchauenPlugin.LOCALIZER.msg("Yes", "Yes"), WirSchauenPlugin.LOCALIZER.msg("No", "No"), Localizer.getLocalization(Localizer.I18N_CANCEL)};
        int buttonPressed = 0;
        if (mProgram.getDate().compareTo(Date.getCurrentDate()) > 0)
        {
          buttonPressed = JOptionPane.showOptionDialog(mParent, WirSchauenPlugin.LOCALIZER.msg("NoOmdbLink", "This program is not yet linked with omdb. Do you want to create a link now?"), WirSchauenPlugin.LOCALIZER.msg("CreateLinkQuestionTitle", "Create OMDB Link"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        }
        if (buttonPressed == 0)
        {
          //create omdb link
          final CreateOmdbLinkDialog createOmdbLinkDialog = new CreateOmdbLinkDialog(mParent, mProgram);
          //restore size and position
          WirSchauenPlugin.getInstance().layoutWindow("wirschauenplugin.CreateOmdbLinkDialog", createOmdbLinkDialog, new Dimension(360, 180));
          createOmdbLinkDialog.setVisible(true);
          //fall through if user cancelled
          if (!createOmdbLinkDialog.isCancelled())
          {
            //save the new data (= omdb link) in wirschauen (in its own thread)
            Thread saver = new Thread("Save wirschauen data")
            {
              @Override
              public void run()
              {
                try
                {
                  WirSchauenConnection.saveEvent(new WirSchauenEvent(createOmdbLinkDialog.getOmdbId(), createOmdbLinkDialog.getCategory()), mProgram);
                  //add the program to the plugin program tree
                  WirSchauenPlugin.getInstance().updateTreeAndMarks(mProgram);
                  //be polite but switch back to event dispatching thread (swing)
                  SwingUtilities.invokeLater(new Runnable()
                  {
                    @Override
                    public void run()
                    {
                      JOptionPane.showMessageDialog(mParent, WirSchauenPlugin.LOCALIZER.msg("Thanks", "Thanks"), WirSchauenPlugin.LOCALIZER.msg("Thanks", "Thanks"), JOptionPane.INFORMATION_MESSAGE);
                    }
                  });
                }
                catch (final IOException e)
                {
                  handleConnectionError(e, "wirschauen");
                }
              }
            };
            saver.start();
          }
        }
        else if (buttonPressed == 1)
        {
          //Don't create a omdb link but create/change wirschauen data
          final CreateWirSchauenDataDialog createWirSchauenDataDialog = new CreateWirSchauenDataDialog(mParent, mProgram, wirSchauenEvent);
          //restore window size and position
          WirSchauenPlugin.getInstance().layoutWindow("wirschauenplugin.CreateWirSchauenDataDialog", createWirSchauenDataDialog, new Dimension(410, 360));
          createWirSchauenDataDialog.setVisible(true);
          //fall through if the user cancelled
          if (!createWirSchauenDataDialog.isCancelled() && !createWirSchauenDataDialog.getWirSchauenInput().equals(wirSchauenEvent, true))
          {
            //save the new data in wirschauen (in its own thread)
            Thread saver = new Thread("Save wirschauen data")
            {
              @Override
              public void run()
              {
                try
                {
                  WirSchauenConnection.saveEvent(createWirSchauenDataDialog.getWirSchauenInput(), mProgram);
                  //add the program to the plugin program tree
                  WirSchauenPlugin.getInstance().updateTreeAndMarks(mProgram);
                  //be polite but switch back to event dispatching thread (swing)
                  SwingUtilities.invokeLater(new Runnable()
                  {
                    @Override
                    public void run()
                    {
                      JOptionPane.showMessageDialog(mParent, WirSchauenPlugin.LOCALIZER.msg("Thanks", "Thanks"), WirSchauenPlugin.LOCALIZER.msg("Thanks", "Thanks"), JOptionPane.INFORMATION_MESSAGE);
                    }
                  });
                }
                catch (final IOException e)
                {
                  //connection to wirschauen failed.
                  e.printStackTrace(System.err);
                  //switch back to event dispatching thread (swing)
                  SwingUtilities.invokeLater(new Runnable()
                  {
                    @Override
                    public void run()
                    {
                      JOptionPane.showMessageDialog(mParent, WirSchauenPlugin.LOCALIZER.msg("ConnectionFailed", "Connection failed", e.getMessage()), Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
                    }
                  });
                }
              }
            };
            saver.start();
          }
        }
      }
      else
      {
        //the event has an omdb-link. load omdb-data.
        //create i-am-busy-dialog
        final LoadingInfoDialog loadingOmdbDataDialog = new LoadingInfoDialog(mParent, "omdb.org");
        //load omdb-data in its own thread.
        Thread loader = new Thread("Load omdb abstract")
        {
          @Override
          public void run()
          {
            try
            {
              //TODO reuse the omdb connection (and the HttpClient in it) for more efficiency. problem: omdb sometimes
              //loses the session. the language is set to en in that case.
              final String omdbAbstract = new OmdbConnection().loadAbstract(OmdbConnection.getIdFromUrl(wirSchauenEvent.getOmdbUrl()), OmdbConnection.DE);
              //switch back to event dispatching thread (swing)
              SwingUtilities.invokeLater(new Runnable()
              {
                @Override
                public void run()
                {
                  omdbDataLoaded(omdbAbstract, loadingOmdbDataDialog, wirSchauenEvent);
                }
              });
            }
            catch (final IOException e)
            {
              handleConnectionError(e, "omdb");
              SwingUtilities.invokeLater(new Runnable()
              {
                @Override
                public void run()
                {
                  loadingOmdbDataDialog.setVisible(false);
                  loadingOmdbDataDialog.dispose();
                }
              });
            }
          }
        };
        loader.start();
        //show i-am-busy-dialog
        UiUtilities.centerAndShow(loadingOmdbDataDialog);
      }
    }
  }


  /**
   * must be called from the event dispatching thread after the loadingInfoDialog was built!
   * if the loading dialog was closed by the user (i.e. cancel), the result will be ignored.
   * otherwise the loading dialog is closed and the result will be processed: show a dialog
   * to add/change the abstract for the program in the omdb.
   *
   * @param omdbAbstract the result of the loading task.
   * @param loadingOmdbDataDialog the loading-dialog (for closing and cancel-action)
   * @param wirSchauenEvent the wirschauen data for the program
   */
  private void omdbDataLoaded(final String omdbAbstract, final LoadingInfoDialog loadingOmdbDataDialog, final WirSchauenEvent wirSchauenEvent)
  {
    //check if the loading was cancelled by the user -> fall through
    if (!loadingOmdbDataDialog.isCancelled())
    {
      //close the loading-dialog
      loadingOmdbDataDialog.setVisible(false);
      loadingOmdbDataDialog.dispose();

      //show the dialog for changing the abstract.
      final CreateOmdbAbstractDialog createOmdbAbstractDialog = new CreateOmdbAbstractDialog(mParent, mProgram, wirSchauenEvent, omdbAbstract);
      WirSchauenPlugin.getInstance().layoutWindow("wirschauenplugin.createOmdbAbstractDialog", createOmdbAbstractDialog, new Dimension(410, 310));
      createOmdbAbstractDialog.setVisible(true);

      //if the abstract was changed and the user did not cancel the dialog - save the new abstract to omdb in its own thread
      if (!createOmdbAbstractDialog.isCancelled() && !createOmdbAbstractDialog.getOmdbAbstractInput().equals(omdbAbstract))
      {
        //save the abstract to omdb
        Thread saver = new Thread("Save omdb abstract")
        {
          @Override
          public void run()
          {
            try
            {
              //TODO reuse the omdb connection (and the HttpClient in it) for more efficiency. problem: omdb sometimes
              //loses the session. the language is set to en in that case.
              new OmdbConnection().saveAbstract(OmdbConnection.getIdFromUrl(wirSchauenEvent.getOmdbUrl()), createOmdbAbstractDialog.getOmdbAbstractInput(), OmdbConnection.DE);
              //add the program to the plugin program tree
              WirSchauenPlugin.getInstance().updateTreeAndMarks(mProgram);
              //be polite but switch back to event dispatching thread (swing)
              SwingUtilities.invokeLater(new Runnable()
              {
                @Override
                public void run()
                {
                  DontShowAgainMessageBox.dontShowAgainMessageBox(WirSchauenPlugin.getInstance(), "saved", mParent, WirSchauenPlugin.LOCALIZER.msg("Thanks", "Thanks"));
                }
              });
            }
            catch (final IOException e)
            {
              handleConnectionError(e, "omdb");
            }
          }
        };
        saver.start();
      }
    }
  }



  private void handleConnectionError(final IOException e, final String website) {
    //connection failed.
    mLog.warning(website + " connection failed: " + e.getMessage());
    //switch back to event dispatching thread (swing)
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        JOptionPane.showMessageDialog(mParent, WirSchauenPlugin.LOCALIZER.msg("ConnectionFailed", "Connection failed", e.getMessage()), Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
      }
    });
  }
}
