package Common.Messages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CompaniesReply extends Message implements Serializable {
    private int serverID;
    private Map<String, Long> companies;

    public CompaniesReply(int t, String c, int s, Map<String, Long> cm){
        super(t, c);
        this.serverID = s;
        this.companies = new HashMap<String, Long>();
        this.setCompanies(cm);
    }

    public int getServerID() {
        return serverID;
    }

    public Map<String, Long> getCompanies() {
        Map<String, Long> result = new HashMap<String, Long>();

        for(Map.Entry<String, Long> entry: this.companies.entrySet()){
            result.put(entry.getKey(), entry.getValue());
        }

        return result;

    }

    public void setCompanies(Map<String, Long> cm){
        this.companies.clear();

        for(Map.Entry<String, Long> entry: cm.entrySet()){
            this.companies.put(entry.getKey(), entry.getValue());
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString());
        sb.append("Server who replied: ").append(this.serverID).append("\n");
        sb.append("--- CompaniesReply ---\n");

        for(Map.Entry<String, Long> entry : this.companies.entrySet()){
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" actions.\n");
        }

        return sb.toString();
    }
}
