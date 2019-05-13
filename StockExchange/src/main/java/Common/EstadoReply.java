package Common;

import java.util.HashMap;
import java.util.Map;

public class EstadoReply extends Msg{
    private int serverId;
    private Map<String, Long> companys;

    public EstadoReply(int server, Map<String, Long> cm){
        super(-1,"");
        this.serverId = server;
        this.companys = new HashMap<>();
        this.setCompanys(cm);
    }

    public int getServerId(){
        return this.serverId;
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

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("EstadoReply para o servidor ").append(this.getServerId());

        for(Map.Entry<String, Long> entry: this.companys.entrySet()){
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" actions.\n");
        }

        return sb.toString();
    }
}
