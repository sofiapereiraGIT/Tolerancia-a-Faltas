package Common.Messages;

public class ActionsReply extends Message {
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

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString());
        sb.append("Server who replied: ").append(this.serverID).append("\n");
        sb.append("--- ActionsReply ---\n");
        sb.append("Company: ").append(this.company).append("\n");
        sb.append("Number of actions: ").append(this.actions).append("\n");

        return sb.toString();
    }
}
