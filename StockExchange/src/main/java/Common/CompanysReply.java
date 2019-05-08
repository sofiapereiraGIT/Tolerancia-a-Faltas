package Common;

import java.util.Map;

public class CompanysReply extends Msg{
    private int serverID;
    private Map<String, Long> companys;

    public CompanysReply(int t, int c, int s, Map<String, Long> cm){
        super(t, c);
        this.serverID = s;
        this.companys = cm;
    }

    public int getServerID() {
        return serverID;
    }

    public Map<String, Long> getCompanys() {
        return companys;
    }
}
