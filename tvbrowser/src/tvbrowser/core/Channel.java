package tvbrowser.core;

public class Channel implements java.io.Serializable, devplugin.Channel {

    private String name;
    private int id;
    private int pos;
    public static final int NOT_SUBSCRIBED=-1;

    public Channel(String name, int id) {
        this.name=name;
        this.id=id;
        pos=NOT_SUBSCRIBED;

    }

    public void unsubscribe() {
        pos=NOT_SUBSCRIBED;
    }

    public String toString() {
        return name+" ("+id+") pos: "+pos;
    }

    public void setPos(int pos) {
        this.pos=pos;
    }

    public int getPos() {
        return pos;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public boolean equals(devplugin.Channel ch) {
        if (ch==null) return false;
        return (ch.getId()==id);
    }

    public boolean isSubscribed() {
        return pos!=NOT_SUBSCRIBED;
    }
}