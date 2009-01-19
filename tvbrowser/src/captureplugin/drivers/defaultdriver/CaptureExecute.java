/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
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
 *
 * Change History:
 *
 *    2008-06-13 Frank Schaeckermann (FSCHAECK)
 *      Added the capture of STDERR output to method executeApplication for
 *      easier troubleshooting of capture driver problems.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package captureplugin.drivers.defaultdriver;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;

import org.apache.commons.codec.binary.Base64;

import util.exc.ErrorHandler;
import util.io.ExecutionHandler;
import util.paramhandler.ParamParser;
import util.ui.Localizer;
import util.ui.UiUtilities;
import captureplugin.drivers.utils.ProgramTime;
import captureplugin.utils.CaptureUtilities;

/**
 * This Class contains excutes the Application/URL
 */
public class CaptureExecute {

    /** Translator */
    private static final Localizer mLocalizer = Localizer.getLocalizerFor(CaptureExecute.class);

    /** Data for Export */
    private DeviceConfig mData = new DeviceConfig();

    /** parent window */
  private Window mParent;
    
    /** Success ? */
    private boolean mError = true;
    private int mExitValue = 0;
    
    private static CaptureExecute mInstance = null;

  /**
   * Creates the Execute
   * 
   * @param window
   *          Frame
   * @param data
   *          Data
   */
    private CaptureExecute(Window window, DeviceConfig data) {
    mParent = window;
        mData = data;
        mInstance = this;
    }
    
    private DefaultKonfigurator createDialog() {
      return new DefaultKonfigurator(mParent, mData);
    }

  /**
   * Gets the capture execute for the given values.
   * 
   * @param window
   *          The parent frame for the capture execute.
   * @param data
   *          The configuration for the capture execute.
   * @return The capture execute for the given values.
   */
    public static CaptureExecute getInstance(Window window, DeviceConfig data) {
      if(mInstance == null)
        new CaptureExecute(window, data);
      else {
        mInstance.mParent = window;
        mInstance.mData = data;
      }
      
      return mInstance;
    }
    
    /**
     * Add a Program
     * @param programTime Program to add
     * @return Success?
     */
    public boolean addProgram(ProgramTime programTime) {
        if (mData.getParameterFormatAdd().trim().length() == 0){
            JOptionPane.showMessageDialog(mParent, mLocalizer.msg("NoParamsAdd", "Please specify Parameters for adding of the Program!"),
                    mLocalizer.msg("CapturePlugin", "Capture Plugin"), JOptionPane.OK_OPTION);
            createDialog().show(DefaultKonfigurator.TAB_PARAMETER);
            return false;
        }

        if (mData.getParameterFormatRem().trim().length() == 0){
          JOptionPane.showMessageDialog(mParent, mLocalizer.msg("NoParamsRemove", "Please specify Parameters for removing of the Program!"),
                  mLocalizer.msg("CapturePlugin", "Capture Plugin"), JOptionPane.OK_OPTION);
          createDialog().show(DefaultKonfigurator.TAB_PARAMETER);
          return false;
      }
        
        return execute(programTime, mData.getParameterFormatAdd());
    }
    
    /**
     * Remove a Program
     * @param programTime Program to remove
     * @return Success?
     */
    public boolean removeProgram(ProgramTime programTime) {
        if ((mData.getParameterFormatAdd().trim().length() == 0) || ((mData.getParameterFormatRem().trim().length() == 0))){
            JOptionPane.showMessageDialog(mParent, mLocalizer.msg("NoParams", "Please specify Parameters for the Program!"),
                    mLocalizer.msg("CapturePlugin", "Capture Plugin"), JOptionPane.OK_OPTION);
            createDialog().show(DefaultKonfigurator.TAB_PARAMETER);
            return false;
        }
        
        return execute(programTime, mData.getParameterFormatRem());
    }
    
    /**
     * Executes the Program in mData and uses program
     * 
     * @param programTime Program to use for Command-Line
     * @param param Parameter
     * @return true if successfull
     */
    public boolean execute(ProgramTime programTime, String param) {
        try {
            String output;

            if (!checkParams()) {
                return false;
            }
            
            ParamParser parser = new ParamParser(new CaptureParamLibrary(mData, programTime));
            
            String params = parser.analyse(param, programTime.getProgram());

            if (parser.hasErrors()) {
              JOptionPane.showMessageDialog(mParent, parser.getErrorString(), Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
              return false;
            }
            
            if (mData.getUseWebUrl()) {
                output = executeUrl(params);
            } else {
                output = executeApplication(params);
            }
                      
            params = CaptureUtilities.replaceIgnoreCase(params, mData.getPassword(), "***");
            output = CaptureUtilities.replaceIgnoreCase(output, mData.getPassword(), "***");
         
            if (!mData.useReturnValue()) {
                mError = false;
            }
            
            if (mError && mExitValue != 249) {
                ResultDialog dialog = new ResultDialog(mParent, params, output, true);
                UiUtilities.centerAndShow(dialog);
                return false;
            } 
            
            if (!mData.getDialogOnlyOnError() || (mData.getDialogOnlyOnError() && mError && mExitValue != 249)) {
                ResultDialog dialog = new ResultDialog(mParent, params, output, false);
                UiUtilities.centerAndShow(dialog);
            }

        } catch (Exception e) {
            ErrorHandler.handle( mLocalizer.msg("ErrorExecute", "Error while excecuting."), e);
            return false;
        }
        
        return mExitValue == 0;
    }
    
    /** 
     * Checks the Parameters
     * @return true if OK
     */
    private boolean checkParams() {
        if (!mData.getUseWebUrl() && (mData.getProgramPath().trim().length() == 0)) {
            JOptionPane.showMessageDialog(mParent, mLocalizer.msg("NoProgram", "Please specify Application to use!"), mLocalizer
                    .msg("CapturePlugin", "Capture Plugin"), JOptionPane.OK_OPTION);
            createDialog().show(DefaultKonfigurator.TAB_PATH);
            return false;
        }
        if (mData.getUseWebUrl() && (mData.getWebUrl().trim().length() == 0)) {
            JOptionPane.showMessageDialog(mParent, mLocalizer.msg("NoUrl", "Please specify URL to use!"), mLocalizer
                    .msg("CapturePlugin", "Capture Plugin"), JOptionPane.OK_OPTION);
            createDialog().show(DefaultKonfigurator.TAB_PATH);
            return false;
        }
        return true;
    }
    
    /**
     * Starts an Application
     * @param params Params for the Application
     * @return Output of Application
     */
    private String executeApplication(String params) {
        ExecutionHandler executionHandler;
        
        try {
            executionHandler = new ExecutionHandler(params, mData.getProgramPath());
            executionHandler.execute(true,true);
        } catch (Exception e) {
            ErrorHandler.handle(mLocalizer.msg("ProblemAtStart", "Problems while starting Application."), e);
            return null;
        }

        
        int time = 0;
        
        // wait until the process has exited, max MaxTimouts
        
        if (mData.getTimeOut() > 0 ){
            while (time < mData.getTimeOut() * 1000) {
                try {
                    Thread.sleep(100);
                    time += 100;
                    executionHandler.exitValue();
                    break;
                } catch (Exception e) {
                    // Empty
                }
            }
        } else {
            while (true) {
                try {
                    Thread.sleep(100);
                    executionHandler.exitValue();
                    break;
                } catch (Exception e) {
                    // Empty
                }
            }
        }
        
        while (time < mData.getTimeOut() * 1000) {
            try {
                Thread.sleep(100);
                time += 100;
                executionHandler.exitValue();
                break;
            } catch (Exception e) {
                // Empty
            }
        }

        // get the process output
        String output = "";
        String errors = ""; // also capture STDERR output FSCHAECK - 2008-06-13
        
        if(!executionHandler.getInputStreamReaderThread().isAlive()) {
          output = executionHandler.getInputStreamReaderThread().getOutput();
        }

        // read STDERR output and add to return value if necessary - FSCHAECK - 2008-06-13
        if(!executionHandler.getErrorStreamReaderThread().isAlive()) {
          errors = executionHandler.getErrorStreamReaderThread().getOutput();
          if (errors.length()>0) {
            if (output.length()>0) {
              output = output+"\n\n"+errors;
            } else {
              output = errors;
            }
          }
        }

        mError = executionHandler.exitValue() != 0;
        mExitValue = executionHandler.exitValue();

        return output;
    }
    
    
    /**
     * Executes the URL
     * @param params Params for the URL
     * @return Result-Page
     * @throws Exception Problems while loading the URL
     */
    private String executeUrl(String params) throws Exception{
        StringBuffer result = new StringBuffer();
        
        URL url = new URL (mData.getWebUrl() + "?" +params);
        
        URLConnection uc = url.openConnection();

        String userpassword = mData.getUserName() + ":" + mData.getPassword();
        String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));

        uc.setRequestProperty  ("Authorization", "Basic " + encoded);

        if (uc instanceof HttpURLConnection) {
            if (((HttpURLConnection)uc).getResponseCode() != HttpURLConnection.HTTP_OK) {
                InputStream content = ((HttpURLConnection)uc).getErrorStream();
                BufferedReader in =  new BufferedReader (new InputStreamReader (content));
                String line;
                while ((line = in.readLine()) != null) {
                  result.append(line);
                }
                mError = true;
                mExitValue = 1;
                return result.toString();
            }
        }
        
        InputStream content = uc.getInputStream();
        BufferedReader in =  new BufferedReader (new InputStreamReader (content));
        String line;
        while ((line = in.readLine()) != null) {
          result.append(line);
        }
        
        mError = false;
        mExitValue = 0;
        return result.toString();
    }
    

}