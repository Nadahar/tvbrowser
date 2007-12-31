package dreamboxdataservice;

import devplugin.ChannelGroup;

/**
 * Created by IntelliJ IDEA.
 * User: bodo
 * Date: 22.12.2007
 * Time: 13:29:40
 */
public class DreamboxChannelGroup implements ChannelGroup {
    public String getName() {
        return "Dreambox";
    }

    public String getId() {
        return "Dreambox";
    }

    public String getDescription() {
        return "Dreambox";
    }

    public String getProviderName() {
        return "Dreambox";
    }


    public boolean equals(Object obj) {

        if (obj instanceof devplugin.ChannelGroup) {
            devplugin.ChannelGroup group = (devplugin.ChannelGroup) obj;
            return group.getId().equalsIgnoreCase(getId());
        }
        return false;

    }
}
