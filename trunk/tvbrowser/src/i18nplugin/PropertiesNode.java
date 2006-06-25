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
 *     $Date: 2006-06-05 21:02:43 +0200 (Mo, 05 Jun 2006) $
 *   $Author: darras $
 * $Revision: 2466 $
 */
package i18nplugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * A Properties-File
 * 
 * @author bodum
 */
public class PropertiesNode extends DefaultMutableTreeNode implements LanguageNodeIf {
  private Properties mProp;
  private JarFile mJarFile;
  private String mPropertiesFile;
  private HashMap<Locale, Properties> mPropertyMap;
  
  /**
   * Create the Properties-File
   * 
   * @param jarfile Jar-File that contains the Entry
   * @param entry Property-File
   */
  public PropertiesNode(JarFile jarfile, JarEntry entry) {
    super(entry.getName().substring(entry.getName().lastIndexOf('/')+1));
    mJarFile = jarfile;
    mPropertiesFile = entry.getName();
    
    mPropertyMap = new HashMap<Locale, Properties>();
    
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
      
      Enumeration keys = mProp.keys();
      
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
   * @param key key to get property for
   * @return the default value of a key
   */
  public String getPropertyValue(String key) {
    return mProp.getProperty(key, "");
  }

  /**
   * This method returns a specific value for a key
   * 
   * @param locale get value for this locale
   * @param key get value for this key
   * @return value for specific locale and key
   */
  public String getPropertyValue(Locale locale, String key) {
    return getProperty(locale).getProperty(key, "");
  }

  /**
   * 
   * @param locale Locale to get Properties for
   * @return Properties for a certain Locale
   */
  private Properties getProperty(Locale locale) {
    Properties prop = mPropertyMap.get(locale);
    
    if (prop == null) {
      StringBuffer propName = new StringBuffer();
      
      propName.append(mPropertiesFile.substring(0, mPropertiesFile.lastIndexOf(".properties")));
     
      propName.append('_').append(locale.getLanguage());
      
      if (locale.getCountry().length() > 0)
        propName.append('_').append(locale.getCountry());

      if (locale.getVariant().length() > 0)
        propName.append('_').append(locale.getVariant());
      
      propName.append(".properties");
      
      prop = new Properties();
      
      try {
        InputStream in = mJarFile.getInputStream(new JarEntry(propName.toString())); 
        if (in != null)
          prop.load(in);
      } catch (IOException e) {
        e.printStackTrace();
        prop = new Properties();
      }
      
      mPropertyMap.put(locale, prop);
    }
    
    return prop;
  }

  /**
   * Set the Property-Value. If the value is null, the key will be removed.
   * 
   * @param locale
   * @param key
   * @param value
   */
  public void setPropertyValue(Locale locale, String key, String value) {
    if (value == null)
      getProperty(locale).remove(key);
    else
      getProperty(locale).setProperty(key, value);
  }

  /**
   * Checks if a key is available in a locale 
   * 
   * @param locale
   * @param key
   * @return true if key is available
   */
  public boolean containsKey(Locale locale, String key) {
    return getProperty(locale).getProperty(key) != null;
  }

  /*
   * (non-Javadoc)
   * @see i18nplugin.LanguageNodeIf#allTranslationsAvailableFor(java.util.Locale)
   */
  public boolean allTranslationsAvailableFor(Locale locale) {
    int max = getChildCount();
    
    for (int i=0;i<max;i++) {
      if (!((LanguageNodeIf)getChildAt(i)).allTranslationsAvailableFor(locale)) {
        return false;
      }
    }
    
    return true;
  }  
}