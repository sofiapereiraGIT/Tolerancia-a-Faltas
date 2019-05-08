package Common;

public class ActionsReply extends Msg{
    private int serverID;
    private long actions;

    public ActionsReply(int t, int c, int s, long a){
        super(t, c);
        this.serverID = s;
        this.actions = a;
    }

    public int getServerID() {
        return serverID;
    }

    public long getActions() {
        return actions;
    }
}
