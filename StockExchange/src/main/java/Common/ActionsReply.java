package Common;

public class ActionsReply extends Msg{
    private int serverID;
    private String company;
    private long actions;

    public ActionsReply(int t, String c, int s, String cm, long a){
        super(t, c);
        this.serverID = s;
        this.company = cm;
        this.actions = a;
    }

    public int getServerID() {
        return this.serverID;
    }

    public String getCompany(){ return this.company;}

    public long getActions() {
        return this.actions;
    }
}
