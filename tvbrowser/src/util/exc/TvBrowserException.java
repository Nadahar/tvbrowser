/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package util.exc;

import util.ui.Localizer;

/**
 * The base exception used within the TV-Browser. It supports
 * internationalization and should be the only exception thrown by methods
 * of the TV-Browser or its plugins.
 *
 * @author  Til Schneider, www.murfman.de
 */
public class TvBrowserException extends Exception {
  
  private Class mMsgClass;
  private String mMsgKey;
  private String mDefaultMsg;
  private Object[] mMsgArgs;

  
  
  /**
   * Creates a new instance of TvBrowserException.
   */
  public TvBrowserException(Class msgClass, String msgKey, String defaultMsg, 
    Object msgArg1)
  {
    this(msgClass, msgKey, defaultMsg, msgArg1, (Throwable)null);
  }

  
  
  /**
   * Creates a new instance of TvBrowserException.
   */
  public TvBrowserException(Class msgClass, String msgKey, String defaultMsg, 
    Object msgArg1, Object msgArg2)
  {
    this(msgClass, msgKey, defaultMsg, msgArg1, msgArg2, (Throwable)null);
  }
  
  
  
  /**
   * Creates a new instance of TvBrowserException.
   */
  public TvBrowserException(Class msgClass, String msgKey, String defaultMsg, 
    Object msgArg1, Object msgArg2, Object msgArg3)
  {
    this(msgClass, msgKey, defaultMsg, msgArg1, msgArg2, msgArg3, (Throwable)null);
  }
  
  
  
  /**
   * Creates a new instance of TvBrowserException.
   */
  public TvBrowserException(Class msgClass, String msgKey, String defaultMsg, 
    Object[] msgArgs)
  {
    this(msgClass, msgKey, defaultMsg, msgArgs, (Throwable)null);
  }

  
  
  /**
   * Creates a new instance of TvBrowserException.
   */
  public TvBrowserException(Class msgClass, String msgKey, String defaultMsg, 
    Object msgArg1, Throwable cause)
  {
    this(msgClass, msgKey, defaultMsg, new Object[] { msgArg1 }, cause);
  }

  
  
  /**
   * Creates a new instance of TvBrowserException.
   */
  public TvBrowserException(Class msgClass, String msgKey, String defaultMsg, 
    Object msgArg1, Object msgArg2, Throwable cause)
  {
    this(msgClass, msgKey, defaultMsg, new Object[] { msgArg1, msgArg2 }, cause);
  }

  
  
  /**
   * Creates a new instance of TvBrowserException.
   */
  public TvBrowserException(Class msgClass, String msgKey, String defaultMsg, 
    Object msgArg1, Object msgArg2, Object msgArg3, Throwable cause)
  {
    this(msgClass, msgKey, defaultMsg, new Object[] { msgArg1, msgArg2, msgArg3 }, cause);
  }
  
  
  
  /**
   * Creates a new instance of TvBrowserException.
   */
  public TvBrowserException(Class msgClass, String msgKey, String defaultMsg,
    Object[] msgArgs, Throwable cause)
  {
    super(cause);
    
    mMsgClass = msgClass;
    mMsgKey = msgKey;
    mDefaultMsg = defaultMsg;
    mMsgArgs = msgArgs;
  }
  
  
  
  public String getMessage() {
    if (mMsgArgs == null) {
      return mDefaultMsg;
    } else {
      return java.text.MessageFormat.format(mDefaultMsg, mMsgArgs);
    }
  }
  
  
  
  /**
   * Gets the localized message of this exception.
   */
  public String getLocalizedMessage() {
    return Localizer.getLocalizerFor(mMsgClass).msg(mMsgKey, mDefaultMsg, mMsgArgs);
  }
  
}
