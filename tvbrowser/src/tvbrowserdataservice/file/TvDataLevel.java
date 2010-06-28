

package tvbrowserdataservice.file;

public class TvDataLevel {
  
  private String mId, mDescription;
  private boolean mIsRequired;
  
  public TvDataLevel(String id, String desc, boolean isRequired) {
    mId=id;
    mDescription=desc;
    mIsRequired=isRequired;
  }
  
  public TvDataLevel(String id, String desc) {
     this(id,desc,false);
  }
  
  public String getId() {
    return mId;
  }
  
  public String getDescription() {
    return mDescription;
  }
  
  public boolean isRequired() {
    return mIsRequired;
  }
  
}