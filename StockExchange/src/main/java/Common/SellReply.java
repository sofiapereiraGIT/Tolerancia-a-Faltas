package Common;

import io.atomix.utils.net.Address;

public class SellReply extends Msg{
    private int serverID;
    private boolean result;

    public SellReply(int t, Address c, int s, boolean b){
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
