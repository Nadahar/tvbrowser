package devplugin;

import javax.swing.*;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 02.01.2005
 * Time: 14:53:33
 */


public class ActionMenu {

  private Action mAction;
  private ActionMenu[] mSubItems;
  private boolean mIsSelected;

  public ActionMenu(Action action, ActionMenu[] subItems) {
    mAction = action;
    mSubItems = subItems;
  }

  public ActionMenu(Action action, Action[] subItems) {
    mAction = action;
    mSubItems = new ActionMenu[subItems.length];
    for (int i=0; i<mSubItems.length; i++) {
      mSubItems[i] = new ActionMenu(subItems[i]);
    }
  }

  public ActionMenu(Action action, boolean isSelected) {
    this(action,(ActionMenu[])null);
    mIsSelected = isSelected;
  }

  public ActionMenu(Action action) {
    this(action, false);
  }

  public String getTitle() {
    return mAction.getValue(Action.NAME).toString();
  }

  public ActionMenu[] getSubItems() {
    return mSubItems;
  }

  public boolean hasSubItems() {
    return mSubItems!=null;
  }


  public boolean isSelected() {
    return mIsSelected;
  }

  public Action getAction() {
    return mAction;
  }

}
