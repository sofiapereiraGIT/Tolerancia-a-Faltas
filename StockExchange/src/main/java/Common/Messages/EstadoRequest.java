package Common.Messages;

public class EstadoRequest extends Message {
    private int serverID;
    private int nextMsg;

    public EstadoRequest(String c, int add, int n){
        super(-1, c);
        this.serverID = add;
        this.nextMsg = n;
    }

    public int getServerID(){
        return this.serverID;
    }

    public int getNextMsg(){ return this.nextMsg;}

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("--- EstadoRequest ---\n");
        sb.append("From server: ").append(this.serverID).append("\n");
        sb.append("Wants messages after, inclusive, ").append(this.nextMsg).append(" id.\n");

        return sb.toString();
    }
}
