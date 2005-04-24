/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package util.paramhandler;

import java.net.URLEncoder;

import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 *  The Default-ParamLibrary.
 * 
 *  If you want to add new Params or Functions for your Plugin, extend this Plugin and
 *  overwrite the public Methods.
 *  
 *  For an example see the Code in the CapturePlugin
 *   
 *  @author bodum
 */
public class ParamLibrary {
  /** True if an Error occured*/
  private boolean mError = false;
  /** The Error */
  private String mErrorString = new String(); 
  
  /**
   * Has an Error occured ?
   * @return true if an error occured
   */
  public boolean hasErrors() {
    return mError;
  }
 
  /**
   * Set the Error-Boolean
   * @param errors True, if an error occured
   */
  public void setErrors(boolean errors) {
    mError = errors;
  }
  
  /**
   * Returns the Error, empty if none occured
   */
  public String getErrorString() {
    return mErrorString;
  }
  
  /**
   * Set the Error
   * @param error the Error
   */
  public void setErrorString(String error) {
    mErrorString = error;
  }
  
  /**
   * Get the possible Keys
   * @return Array with possible Keys
   */
  public String[] getPossibleKeys() {
    String[] str = {"title"};
    return str;
  }
  
  /**
   * Get the description for one Key
   * @return description for one key
   */
  public String getDescriptionForKey(String key) {
    return "no description";
  }

  /**
   * Get the List of possible Functions
   * @return List of possible Functions
   */
  public String[] getPossibleFunctions() {
    String[] str = {"functions"};
    return str;
  }
  
  /**
   * Get the description for a specific Funtion 
   * @return
   */
  public String getDescriptionForFunctions(String function) {
    return "no description";
  }
  
  /**
   * Get the String for a key
   * @param prg Program to use
   * @param key Key to use
   * @return Value of key in prg
   */
  public String getStringForKey(Program prg, String key) {
    
    if (key.equals("title")) {
      return prg.getTitle();
    } else if (key.equals("original_title")) {
      String ret = prg.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE);
      
      if (ret == null) {
        ret = "";
      }
      
      return ret;
    }

    mError = true;
    mErrorString = "Unknown Param : " + key;
    
    return null;
  }
  
  /**
   * Returns the Value of a function
   * 
   * @param prg Program to use
   * @param function Function to use
   * @param params Params for the Function
   * @return Return-Value of Function
   */
  public String getStringForFunction(Program prg, String function, String[] params) {
    String bla = new String();
    for (int i = 0; i < params.length;i++) {
      bla = bla + params[i]+";";
    }
    
    if (function.equals("isset")) {
      if (params.length != 2) {
        mError = true;
        mErrorString = "isset needs 2 Parameters";
        return null;
      }
      
      if ((params[0] != null) && (params[0].length() > 0)) {
        return params[0];
      }
      
      return params[1];
    } else if (function.equals("urlencode")) {
      if (params.length != 2) {
        mError = true;
        mErrorString = "urlencode needs 2 Parameters";
        return null;
      }
      
      try {
        return URLEncoder.encode(params[0], params[1]);
      } catch (Exception e) {
        mError = true;
        mErrorString = "Problems with encoding : " + e.toString();
        return null;
      }
    } else if (function.equals("concat")) {
      
      StringBuffer buffer = new StringBuffer();
      
      for (int i=0;i<params.length;i++) {
        buffer.append(params[i]);
      }
      return buffer.toString();
    }
    

    mError = true;
    mErrorString = "Unkown function : " + function;
    
    return null;
  }
  
}