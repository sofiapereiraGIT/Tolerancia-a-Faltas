package Common.Messages;

import java.util.List;

public class MembershipInfo {
    private int serverID;
    private List<String> allActiveServers;
    private int number;

    public MembershipInfo(int s, int n, List<String> list){
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

        sb.append("--- MembershipInfo ---\n");
        sb.append("From server: ").append(this.serverID).append(".\n");
        sb.append("Number of active servers: ").append(this.number).append(".\n");
        sb.append("All active servers: \n");
        for(String s : this.allActiveServers) {
            sb.append("- ").append(s).append("\n");
        }

        return sb.toString();
    }
}
