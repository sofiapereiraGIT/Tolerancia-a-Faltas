package Common.Messages;

import java.io.Serializable;
import java.util.List;

public class MembershipInfoReply extends Message implements Serializable {
    private int serverID;
    private List<String> allActiveServers;
    private int number;

    public MembershipInfoReply(int t, String c, int s, int n, List<String> list){
        super(t, c);
        this.serverID = s;
        this.number = n;
        this.allActiveServers = list;
    }

    public int getServerID() { return this.serverID;}

    public int getNumber() { return this.number;}

    public List<String> getAllActiveServers() {
        return this.allActiveServers;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString());
        sb.append("--- MembershipInfoReply ---\n");
        sb.append("From server: ").append(this.serverID).append(".\n");
        sb.append("Number of active servers: ").append(this.number).append(".\n");
        sb.append("All active servers: \n");
        for(String s : this.allActiveServers) {
            sb.append("- ").append(s).append("\n");
        }

        return sb.toString();
    }
}
