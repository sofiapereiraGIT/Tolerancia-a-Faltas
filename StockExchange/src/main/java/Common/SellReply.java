package Common;

public class SellReply extends Msg{
    private int serverID;
    private boolean result;

    public SellReply(int t, String c, int s, boolean b){
        super(t, c);
        this.serverID = s;
        this.result = b;
    }

    public int getServerID(){
        return this.serverID;
    }

    public boolean getResult(){
        return this.result;
    }
}
