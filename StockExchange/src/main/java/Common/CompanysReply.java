package Common;

import io.atomix.utils.net.Address;

import java.util.HashMap;
import java.util.Map;

public class CompanysReply extends Msg{
    private int serverID;
    private Map<String, Long> companys;

    public CompanysReply(int t, Address c, int s, Map<String, Long> cm){
        super(t, c);
        this.serverID = s;
        this.companys = new HashMap<String, Long>();
        this.setCompanys(cm);
    }

    public int getServerID() {
        return serverID;
    }

    public Map<String, Long> getCompanys() {
        Map<String, Long> result = new HashMap<String, Long>();

        for(Map.Entry<String, Long> entry: this.companys.entrySet()){
            result.put(entry.getKey(), entry.getValue());
        }

        return result;

    }

    public void setCompanys(Map<String, Long> cm){
        this.companys.clear();

        for(Map.Entry<String, Long> entry: cm.entrySet()){
            this.companys.put(entry.getKey(), entry.getValue());
        }
    }
}
