package compat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

import javax.swing.JFrame;

import devplugin.ProgramFilter;
import devplugin.Version;
import tvbrowser.TVBrowser;
import tvbrowser.core.filters.FilterComponent;
import tvbrowser.core.filters.FilterComponentList;
import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.core.filters.filtercomponents.ChannelFilterComponent;
import tvbrowser.ui.filter.dlgs.EditFilterComponentDlg;

public final class FilterCompat {
  private static FilterCompat INSTANCE;
  private ArrayList<FilterChangeListener> mListListeners;
  
  private FilterCompat() {
    INSTANCE = this;
    mListListeners = new ArrayList<FilterCompat.FilterChangeListener>();
    
    try {
      String clazzName = null;
      
      if(TVBrowser.VERSION.compareTo(new Version(3,34,true)) >= 0) {
        clazzName = "devplugin.FilterChangeListenerV2";
      }
      else if(TVBrowser.VERSION.compareTo(new Version(3,33,true)) >= 0) {
        clazzName = "devplugin.FilterChangeListener";
      }
      
      if(clazzName != null) {
        Class<?> filterListenerClass = Class.forName(clazzName);
        Method addFilterListener = FilterManagerImpl.class.getDeclaredMethod("registerFilterChangeListener", filterListenerClass);
        
        Object filterListener = Proxy.newProxyInstance(filterListenerClass.getClassLoader(), new Class<?>[] {filterListenerClass}, new HandlerFilterListener(this));
        
        addFilterListener.invoke(FilterManagerImpl.getInstance(), filterListener);
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public static synchronized final FilterCompat getInstance() {
    if(INSTANCE == null) {
      new FilterCompat();
    }
    
    return INSTANCE;
  }
  
  public static String[] getChannelFilterComponentNames() {
    String[] result = null;
    if(TVBrowser.VERSION.compareTo(new Version(3,21,true)) >= 0) {
      try {
        Method m = FilterManagerImpl.class.getDeclaredMethod("getChannelFilterComponentNames");
        result = (String[])m.invoke(FilterManagerImpl.getInstance());
      }catch(Exception e) {}
    }
    else {
      result = getChannelFilterComponentNamesCompat();
    }
    
    return result;
  }
  
  public static String addNewChannelFilterComponent() {
    String result = null;
    
    if(TVBrowser.VERSION.compareTo(new Version(3,21,true)) >= 0) {
      try {
        Method m = FilterManagerImpl.class.getDeclaredMethod("addNewChannelFilterComponent");
        result = (String)m.invoke(FilterManagerImpl.getInstance());
      }catch(Exception e) {}
    }
    else {
      result = addNewChannelFilterComponentCompat();
    }
    
    return result;
  }
  
  private static String[] getChannelFilterComponentNamesCompat() {
    return FilterComponentList.getInstance().getChannelFilterNames();
  }
  
  private static String addNewChannelFilterComponentCompat() {
    EditFilterComponentDlg dlg = new EditFilterComponentDlg((JFrame)null, null, ChannelFilterComponent.class);
    FilterComponent rule = dlg.getFilterComponent();
    if (rule == null) {
      return null;
    }
    if (! (rule instanceof ChannelFilterComponent)) {
      return null;
    }
    FilterComponentList.getInstance().add(rule);
    FilterComponentList.getInstance().store();
    
    return rule.getName();
  }
  
  public void registerFilterChangeListener(final FilterChangeListener listener) {
    if(!mListListeners.contains(listener)) {
      mListListeners.add(listener);
    }
  }
  
  public void unregisterFilterChangeListener(final FilterChangeListener listener) {
    mListListeners.remove(listener);
  }
  
  private void handleFilterEvent(final String method, final ProgramFilter filter) {
    if("filterAdded".equals(method)) {
      for(FilterChangeListener l : mListListeners) {
        l.filterAdded(filter);
      }
    }
    else if("filterRemoved".equals(method)) {
      for(FilterChangeListener l : mListListeners) {
        l.filterRemoved(filter);
      }
    }
    else if("filterTouched".equals(method)) {
      for(FilterChangeListener l : mListListeners) {
        l.filterTouched(filter);
      }
    }
    else if("filterDefaultChanged".equals(method)) {
      for(FilterChangeListener l : mListListeners) {
        l.filterDefaultChanged(filter);
      }
    }
  }
  
  public static interface FilterChangeListener {
    /**
     * Called when a filter is added.
     * <p>
     * @param filter The filter that was added.
     */
    public void filterAdded(ProgramFilter filter);
    
    /**
     * Called when a filter was removed.
     * <p>
     * @param filter The filter that was removed.
     */
    public void filterRemoved(ProgramFilter filter);
    
    /**
     * Called when user edited the filter.
     * <p>
     * @param filter The filter that was touched.
     */
    public void filterTouched(ProgramFilter filter);
    
    /**
     * Called when the default filter was changed.
     * <p>
     * @param filter The new default filter, or the
     * all filter if no default filter exists.
     */
    public void filterDefaultChanged(ProgramFilter filter);
  }
  
  private static final class HandlerFilterListener implements InvocationHandler {
    private FilterCompat mFilterCompat;
    
    private HandlerFilterListener(FilterCompat filterCompat) {
      mFilterCompat = filterCompat;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if(method != null && args != null && args.length == 1) {
        mFilterCompat.handleFilterEvent(method.getName(), (ProgramFilter)args[0]);
      }
      
      return null;
    }
  }
}
