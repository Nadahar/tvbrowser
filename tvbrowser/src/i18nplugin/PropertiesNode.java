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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package i18nplugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import tvbrowser.core.Settings;
import util.io.stream.InputStreamProcessor;
import util.io.stream.StreamUtilities;

/**
 * A Properties-File
 * 
 * @author bodum
 */
public class PropertiesNode extends AbstractHierarchicalNode implements
    LanguageNodeIf, FilterNodeIf {
  private Properties mProp;
  private JarFile mJarFile;
  private String mPropertiesFile;
  private HashMap<Locale, Properties> mOriginalPropertyMap;
  private HashMap<Locale, Properties> mUserPropertyMap;
  private List<TreeNode> filteredChildren = new ArrayList<TreeNode>();

  /**
   * Create the Properties-File
   * 
   * @param jarfile
   *          Jar-File that contains the Entry
   * @param entry
   *          Property-File
   */
  public PropertiesNode(JarFile jarfile, JarEntry entry) {
    super(entry.getName().substring(entry.getName().lastIndexOf('/') + 1));
    mJarFile = jarfile;
    mPropertiesFile = entry.getName();

    mOriginalPropertyMap = new HashMap<Locale, Properties>();
    mUserPropertyMap = new HashMap<Locale, Properties>();

    createPropertyEntries(jarfile, entry);
  }

  /**
   * Creates all PropertyEntryNodes
   * 
   * @param jarfile
   * @param entry
   */
  private void createPropertyEntries(JarFile jarfile, JarEntry entry) {
    mProp = new Properties();
    try {
      mProp.load(jarfile.getInputStream(entry));

      Enumeration<Object> keys = mProp.keys();

      while (keys.hasMoreElements()) {
        add(new PropertiesEntryNode((String) keys.nextElement()));
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void insert(MutableTreeNode newChild, int childIndex) {
    super.insert(newChild, childIndex);

    Collections.sort(children, new Comparator<TreeNode>() {
      public int compare(TreeNode o1, TreeNode o2) {
        return o1.toString().compareTo(o2.toString());
      }
    });
  }

  /**
   * This method returns the default value for a key.
   * 
   * @param key
   *          key to get property for
   * @return the default value of a key
   */
  public String getPropertyValue(String key) {
    return mProp.getProperty(key, "");
  }

  /**
   * This method returns a specific value for a key
   * 
   * @param locale
   *          get value for this locale
   * @param key
   *          get value for this key
   * @return value for specific locale and key
   */
  public String getPropertyValue(Locale locale, String key) {
    String value = getUserProperty(locale).getProperty(key, null);

    if (value != null) {
      return value;
    }

    return getOriginalProperty(locale).getProperty(key, "");
  }

  /**
   * 
   * @param locale
   *          Locale to get Properties for
   * @return Properties for a certain Locale
   */
  private Properties getOriginalProperty(Locale locale) {
    Properties prop = mOriginalPropertyMap.get(locale);

    if (prop == null) {
      StringBuilder propName = new StringBuilder();

      propName.append(mPropertiesFile.substring(0, mPropertiesFile
          .lastIndexOf(".properties")));

      propName.append('_').append(locale.getLanguage());

      if (locale.getCountry().length() > 0) {
        propName.append('_').append(locale.getCountry());
      }

      if (locale.getVariant().length() > 0) {
        propName.append('_').append(locale.getVariant());
      }

      propName.append(".properties");

      prop = new Properties();

      try {
        InputStream in = mJarFile.getInputStream(new JarEntry(propName
            .toString()));
        if (in != null) {
          prop.load(in);
        }
      } catch (IOException e) {
        e.printStackTrace();
        prop = new Properties();
      }

      mOriginalPropertyMap.put(locale, prop);
    }

    return prop;
  }

  /**
   * @param locale
   * @return Returns the filename for the properties file of the current user
   */
  private String getUserPropertiesFileName(Locale locale) {
    StringBuilder propName = new StringBuilder(Settings
        .getUserSettingsDirName())
        .append(File.separatorChar).append("lang").append(File.separatorChar);

    propName.append(mPropertiesFile.substring(0, mPropertiesFile
        .lastIndexOf(".properties")));

    propName.append('_').append(locale.getLanguage());

    if (locale.getCountry().length() > 0) {
      propName.append('_').append(locale.getCountry());
    }

    if (locale.getVariant().length() > 0) {
      propName.append('_').append(locale.getVariant());
    }

    propName.append(".properties");

    return propName.toString();
  }

  /**
   * @param locale
   * @return Returns the user defined properties
   */
  private Properties getUserProperty(Locale locale) {
    Properties prop = mUserPropertyMap.get(locale);
    if (prop != null) {
      return prop;
    }

    final Properties newProp = new Properties();
    boolean loaded = true;

    File file = new File(getUserPropertiesFileName(locale));
    if (file.exists()) {
      try {
        StreamUtilities.inputStream(file, new InputStreamProcessor() {
          @Override
          public void process(InputStream input) throws IOException {
            newProp.load(input);
          }
        });
      } catch (IOException e) {
        e.printStackTrace();
        loaded = false;
      }
    }

    if (loaded) {
      mUserPropertyMap.put(locale, newProp);
    } else {
      mUserPropertyMap.put(locale, new Properties());
    }
    return mUserPropertyMap.get(locale);
  }

  /**
   * Set the Property-Value. If the value is null, the key will be removed.
   * 
   * @param locale
   * @param key
   * @param value
   */
  public void setPropertyValue(Locale locale, String key, String value) {
    String oldvalue = getPropertyValue(locale, key);

    if (!oldvalue.equals(value)) {
      if (value == null) {
        getUserProperty(locale).remove(key);
      } else {
        getUserProperty(locale).setProperty(key, value);
      }
    }
  }

  /**
   * Checks if a key is available in a locale
   * 
   * @param locale
   * @param key
   * @return true if key is available
   */
  public boolean containsKey(Locale locale, String key) {
    return ((getOriginalProperty(locale).getProperty(key) != null) || (getUserProperty(
        locale).getProperty(key) != null));
  }

  /*
   * (non-Javadoc)
   * 
   * @see i18nplugin.LanguageNodeIf#save()
   */
  public void save() throws IOException {
    Set<Locale> keys = mUserPropertyMap.keySet();
    for (Locale locale : keys) {
      Properties prop = mUserPropertyMap.get(locale);
      if (prop.size() > 0) {

        // Create new Property that has the default values AND the user defined
        // values
        Properties newprop = mixProperties(getOriginalProperty(locale),
            getUserProperty(locale));

        File propFile = new File(getUserPropertiesFileName(locale));
        propFile.getParentFile().mkdirs();
        storeSorted(newprop, new FileOutputStream(propFile),
            "Saved by i18n Plugin Version "
                + I18NPlugin.getInstance().getInfo().getVersion());
      }
    }

  }

  /**
   * Mixes two properties. The userProperty overwrites the original property
   * 
   * @param originalProperty
   * @param userProperty
   * @return mixed properties
   */
  private Properties mixProperties(Properties originalProperty,
      Properties userProperty) {
    Properties prop = new Properties();

    Enumeration<Object> e = originalProperty.keys();

    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      prop.setProperty(key, originalProperty.getProperty(key));
    }

    e = userProperty.keys();

    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      prop.setProperty(key, userProperty.getProperty(key));
    }

    return prop;
  }

  /*
   * the following methods are copied from the Java sources as it is otherwise
   * not possible to override the save() method in such a way that old
   * properties are stored in alphabetical order
   */

  /**
   * Writes this property list (key and element pairs) in this
   * <code>Properties</code> table to the output stream in a format suitable for
   * loading into a <code>Properties</code> table using the
   * {@link #load(InputStream) load} method. The stream is written using the ISO
   * 8859-1 character encoding.
   * <p>
   * Properties from the defaults table of this <code>Properties</code> table
   * (if any) are <i>not</i> written out by this method.
   * <p>
   * If the comments argument is not null, then an ASCII <code>#</code>
   * character, the comments string, and a line separator are first written to
   * the output stream. Thus, the <code>comments</code> can serve as an
   * identifying comment.
   * <p>
   * Next, a comment line is always written, consisting of an ASCII
   * <code>#</code> character, the current date and time (as if produced by the
   * <code>toString</code> method of <code>Date</code> for the current time),
   * and a line separator as generated by the Writer.
   * <p>
   * Then every entry in this <code>Properties</code> table is written out, one
   * per line. For each entry the key string is written, then an ASCII
   * <code>=</code>, then the associated element string. Each character of the
   * key and element strings is examined to see whether it should be rendered as
   * an escape sequence. The ASCII characters <code>\</code>, tab, form feed,
   * newline, and carriage return are written as <code>\\</code>,
   * <code>\t</code>, <code>\f</code> <code>\n</code>, and <code>\r</code>,
   * respectively. Characters less than <code>&#92;u0020</code> and characters
   * greater than <code>&#92;u007E</code> are written as <code>&#92;u</code>
   * <i>xxxx</i> for the appropriate hexadecimal value <i>xxxx</i>. For the key,
   * all space characters are written with a preceding <code>\</code> character.
   * For the element, leading space characters, but not embedded or trailing
   * space characters, are written with a preceding <code>\</code> character.
   * The key and element characters <code>#</code>, <code>!</code>,
   * <code>=</code>, and <code>:</code> are written with a preceding backslash
   * to ensure that they are properly loaded.
   * <p>
   * After the entries have been written, the output stream is flushed. The
   * output stream remains open after this method returns.
   * 
   * @param out
   *          an output stream.
   * @param comments
   *          a description of the property list.
   * @exception IOException
   *              if writing this property list to the specified output stream
   *              throws an <tt>IOException</tt>.
   * @exception ClassCastException
   *              if this <code>Properties</code> object contains any keys or
   *              values that are not <code>Strings</code>.
   * @exception NullPointerException
   *              if <code>out</code> is null.
   * @since 1.2
   */
  public synchronized void storeSorted(Properties prop, OutputStream out,
      String comments) throws IOException {
    BufferedWriter awriter;
    awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
    if (comments != null) {
      awriter.write("#" + comments);
      awriter.newLine();
    }
    awriter.write("#" + new Date().toString());
    awriter.newLine();
    String[] keys = new String[prop.keySet().size()];
    keys = prop.keySet().toArray(keys);
    Arrays.sort(keys);
    for (String key : keys) {
      String val = (String) prop.get(key);
      key = saveConvert(key, true);

      /*
       * No need to escape embedded and trailing spaces for value, hence pass
       * false to flag.
       */
      val = saveConvert(val, false);
      awriter.write(key + "=" + val);
      awriter.newLine();
    }
    awriter.flush();
  }

  /*
   * Converts unicodes to encoded &#92;uxxxx and escapes special characters with
   * a preceding slash
   */
  private String saveConvert(String theString, boolean escapeSpace) {
    int len = theString.length();
    int bufLen = len * 2;
    if (bufLen < 0) {
      bufLen = Integer.MAX_VALUE;
    }
    StringBuilder outBuffer = new StringBuilder(bufLen);

    for (int x = 0; x < len; x++) {
      char aChar = theString.charAt(x);
      // Handle common case first, selecting largest block that
      // avoids the specials below
      if ((aChar > 61) && (aChar < 127)) {
        if (aChar == '\\') {
          outBuffer.append('\\');
          outBuffer.append('\\');
          continue;
        }
        outBuffer.append(aChar);
        continue;
      }
      switch (aChar) {
      case ' ':
        if (x == 0 || escapeSpace)
          outBuffer.append('\\');
        outBuffer.append(' ');
        break;
      case '\t':
        outBuffer.append('\\');
        outBuffer.append('t');
        break;
      case '\n':
        outBuffer.append('\\');
        outBuffer.append('n');
        break;
      case '\r':
        outBuffer.append('\\');
        outBuffer.append('r');
        break;
      case '\f':
        outBuffer.append('\\');
        outBuffer.append('f');
        break;
      case '=': // Fall through
      case ':': // Fall through
      case '#': // Fall through
      case '!':
        outBuffer.append('\\');
        outBuffer.append(aChar);
        break;
      default:
        if ((aChar < 0x0020) || (aChar > 0x007e)) {
          outBuffer.append('\\');
          outBuffer.append('u');
          outBuffer.append(toHex((aChar >> 12) & 0xF));
          outBuffer.append(toHex((aChar >> 8) & 0xF));
          outBuffer.append(toHex((aChar >> 4) & 0xF));
          outBuffer.append(toHex(aChar & 0xF));
        } else {
          outBuffer.append(aChar);
        }
      }
    }
    return outBuffer.toString();
  }

  /**
   * Convert a nibble to a hex character
   * 
   * @param nibble
   *          the nibble to convert.
   */
  private static char toHex(int nibble) {
    return hexDigit[(nibble & 0xF)];
  }

  /** A table of hex digits */
  private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6',
      '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

  public int getMatchCount() {
    return filteredChildren.size();
  }

}