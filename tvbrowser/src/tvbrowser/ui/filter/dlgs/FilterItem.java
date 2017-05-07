package tvbrowser.ui.filter.dlgs;

import java.util.Locale;

import tvbrowser.core.filters.FilterComponent;

public class FilterItem {
  static String AND_KEY = "and";
  static String OR_KEY = "or";
  static String NOT_KEY = "not";
  static String OPEN_BRACKET_KEY = "open_bracket";
  static String CLOSE_BRACKET_KEY = "close_bracket";
  
  private String mRuleType;
  private FilterComponent mComponent;
  private int mLevel;
  
  private FilterItem(String ruleType, int level, FilterComponent comp) {
    mRuleType = ruleType;
    mLevel = level;
    mComponent = comp;
  }
  
  public FilterItem(String ruleType, int level) {
    mRuleType = ruleType;
    mComponent = null;
    mLevel = level;
  }
  public FilterItem(FilterComponent comp, int level) {
    mRuleType = null;
    mComponent = comp;
    mLevel = level;
  }
  
  public String toString() {
    if(mRuleType != null) {
      if(Locale.getDefault().getLanguage().equals("de")) {
        if(mRuleType.equals(AND_KEY)) {
          return "UND";
        }
        else if(mRuleType.equals(OR_KEY)) {
          return "ODER";
        }
        else if(mRuleType.equals(NOT_KEY)) {
          return "NICHT";
        } 
      }
      else {
        if(mRuleType.equals(AND_KEY)) {
          return "AND";
        }
        else if(mRuleType.equals(OR_KEY)) {
          return "OR";
        }
        else if(mRuleType.equals(NOT_KEY)) {
          return "NOT";
        } 
      }
      
      if(mRuleType.equals(OPEN_BRACKET_KEY)) {
        return "(";
      }
      else if(mRuleType.equals(CLOSE_BRACKET_KEY)) {
        return ")";
      }
    }
    return mComponent.getName();
  }
  
  public void setLevel(int level) {
    mLevel = level;
  }
  
  public int getLevel() {
    return mLevel;
  }
  
  public FilterComponent getComponent() {
    return mComponent;
  }
  
  public FilterItem clone(int level) {
    return new FilterItem(mRuleType,level,mComponent);
  }
  
  public boolean isOpenBracketItem() {
    return mRuleType != null && mRuleType.equals(OPEN_BRACKET_KEY);
  }
  
  public boolean isCloseBracketItem() {
    return mRuleType != null && mRuleType.equals(CLOSE_BRACKET_KEY);
  }
  
  public boolean isAndItem() {
    return mRuleType != null && mRuleType.equals(AND_KEY);
  }

  public boolean isOrItem() {
    return mRuleType != null && mRuleType.equals(OR_KEY);
  }
  
  public boolean isNotItem() {
    return mRuleType != null && mRuleType.equals(NOT_KEY);
  }
}
