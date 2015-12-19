package devplugin;

public final class ToolTipIcon {
  private String mAddress;
  private String mToolTipText;
  
  public ToolTipIcon(final String address, final String toolTipText) {
    mAddress = address;
    mToolTipText = toolTipText;
  }
  
  public StringBuilder append(StringBuilder builder) {
    builder.append("<tr><td valign=\"middle\" align=\"center\"><img src=\"")
    .append(mAddress).append("\"></td><td>&nbsp;")
    .append(mToolTipText).append("</td></tr>");
    
    return builder;
  }
}
