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

import tvbrowser.core.Settings;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A Properties-File
 * 
 * @author bodum
 */
public class PropertiesNode extends DefaultMutableTreeNode implements LanguageNodeIf {
  private Properties mProp;
  private JarFile mJarFile;
  private String mPropertiesFile;
  private HashMap<Locale, Properties> mOriginalPropertyMap;
  private HashMap<Locale, Properties> mUserPropertyMap;
  
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
    String value = getUserProperty(locale).getProperty(key, null);
    
    if (value != null)
      return value;
    
    return getOriginalProperty(locale).getProperty(key, "");
  }

  /**
   * 
   * @param locale Locale to get Properties for
   * @return Properties for a certain Locale
   */
  private Properties getOriginalProperty(Locale locale) {
    Properties prop = mOriginalPropertyMap.get(locale);
    
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
      
      mOriginalPropertyMap.put(locale, prop);
    }
    
    return prop;
  }

  /**
   * @param locale
   * @return Returns the filename for the properties file of the current user
   */
  private String getUserPropertiesFileName(Locale locale) {
    StringBuffer propName = new StringBuffer(Settings.getUserSettingsDirName()).append(File.separatorChar).append("lang").append(File.separatorChar);
    
    propName.append(mPropertiesFile.substring(0, mPropertiesFile.lastIndexOf(".properties")));
   
    propName.append('_').append(locale.getLanguage());
    
    if (locale.getCountry().length() > 0)
      propName.append('_').append(locale.getCountry());

    if (locale.getVariant().length() > 0)
      propName.append('_').append(locale.getVariant());
    
    propName.append(".properties");

    return propName.toString();
  }

  /**
   * @param locale
   * @return Returns the user defined properties
   */
  private Properties getUserProperty(Locale locale) {
    Properties prop = mUserPropertyMap.get(locale);
    
    if (prop == null) {
      
      prop = new Properties();
      
      try {
        File file = new File(getUserPropertiesFileName(locale));
        if (file.exists()) {
          InputStream in = new FileInputStream(file); 
          prop.load(in);
        }
      } catch (IOException e) {
        e.printStackTrace();
        prop = new Properties();
      }
      
      mUserPropertyMap.put(locale, prop);
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
    String oldvalue = getPropertyValue(locale, key);
    
    if (! oldvalue.equals(value)) {
      if (value == null)
        getUserProperty(locale).remove(key);
      else
        getUserProperty(locale).setProperty(key, value);
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
    return ((getOriginalProperty(locale).getProperty(key) != null)||(getUserProperty(locale).getProperty(key) != null));
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

  /*
   * (non-Javadoc)
   * @see i18nplugin.LanguageNodeIf#save()
   */
  public void save() throws IOException{
    Set<Locale> keys = mUserPropertyMap.keySet();
    for (Locale locale : keys) {
      Properties prop = mUserPropertyMap.get(locale);
      if (prop.size() > 0) {
        
        // Create new Property that has the default values AND the user defined values
        Properties newprop = mixProperties(getOriginalProperty(locale), getUserProperty(locale));
        
        File propFile = new File(getUserPropertiesFileName(locale));
        propFile.getParentFile().mkdirs();
        newprop.store(new FileOutputStream(propFile), "Saved by i18n Plugin Version " + I18NPlugin.getInstance().getInfo().getVersion());
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
  private Properties mixProperties(Properties originalProperty, Properties userProperty) {
    Properties prop = new Properties();
    
    Enumeration e = originalProperty.keys();
    
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
}