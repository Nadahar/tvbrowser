/*
 * Created on 04.06.2004
 */
package util.tvdataservice;

import devplugin.ChannelGroup;


/**
 * This is a small Implementation of the ChannelGroup Interface.
 * 
 * @author bodo
 */
public class ChannelGroupImpl implements ChannelGroup {

    /**
     * Creats the ChannelGroup
     * @param name Name of the Group
     * @param id ID of the Group
     * @param desc Description of the Group
     */
    public ChannelGroupImpl(String name, String id, String desc) {
        _name = name;
        _id = id;
        _desc = desc;
    }
    
    /* (non-Javadoc)
     * @see devplugin.ChannelGroup#getName()
     */
    public String getName() {
        return _name;
    }

    /* (non-Javadoc)
     * @see devplugin.ChannelGroup#getId()
     */
    public String getId() {
        return _id;
    }

    /* (non-Javadoc)
     * @see devplugin.ChannelGroup#getDescription()
     */
    public String getDescription() {
        return _desc;
    }

    
    private String _name;
    private String _id;
    private String _desc;
}
