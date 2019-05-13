package Common;

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

        sb.append("Membership info renewed by server ").append(this.serverID);
        sb.append(" -> ").append(this.number).append(" server remaining");

        return sb.toString();
    }
}
