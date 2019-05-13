package Common.Messages;

public class MembershipInfo {
    private int serverID;
    private int number;

    public MembershipInfo(int s, int n){
        this.serverID = s;
        this.number = n;
    }

    public int getServerID(){ return this.serverID;}

    public int getNumber(){ return this.number;}

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("--- MembershipInfo ---\n");
        sb.append("From server: ").append(this.serverID).append(".\n");
        sb.append("Number of active servers: ").append(this.number).append(".\n");

        return sb.toString();
    }
}
