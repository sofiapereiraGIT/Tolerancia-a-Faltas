package Common;

import io.atomix.utils.net.Address;

public class BuyReply extends Msg{
    private int serverID;
    private boolean result;

    public BuyReply(int t, Address c, int s, boolean r){
        super(t,c);
        this.serverID = s;
        this.result = r;
    }

    public int getServerID() {
        return serverID;
    }

    public boolean isResult() {
        return result;
    }
}
