package Common.Messages;

import java.io.Serializable;

public class BuyReply extends Message implements Serializable {
    private int serverID;
    private String company;
    private boolean result;

    public BuyReply(int t, String c, int s, String cm, boolean r){
        super(t,c);
        this.serverID = s;
        this.company = cm;
        this.result = r;
    }

    public int getServerID() {
        return this.serverID;
    }

    public String getCompany(){ return this.company;}

    public boolean getResult() {
        return this.result;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString());
        sb.append("Server who replied: ").append(this.serverID).append("\n");
        sb.append("--- BuyReply ---\n");
        sb.append("Company: ").append(this.company).append("\n");
        sb.append("Success: ").append(this.result).append("\n");

        return sb.toString();
    }
}
