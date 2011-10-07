package captureplugin.utils;

import devplugin.Channel;

/**
 * Created by IntelliJ IDEA.
 * User: bodotasche
 * Date: 04.09.2007
 * Time: 09:12:38
 * To change this template use File | Settings | File Templates.
 */
public interface ConfigIf {

    public ExternalChannelIf getExternalChannel(Channel subscribedChannel);

    public void setExternalChannel(Channel subscribedChannel, ExternalChannelIf externalChannel);

    public ExternalChannelIf[] getExternalChannels();
}
