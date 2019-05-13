package Client;

import java.util.HashMap;
import java.util.Map;

public class Client {
    private Map<String, Long> myCompanys;

    public Client(){
        this.myCompanys = new HashMap<String, Long>();
    }

    public Client(Map<String, Long> cm){
        this.myCompanys = new HashMap<>();
        this.setCompanys(cm);
    }

    public Map<String, Long> getCompanys() {
        Map<String, Long> result = new HashMap<String, Long>();

        for(Map.Entry<String, Long> entry: this.myCompanys.entrySet()){
            result.put(entry.getKey(), entry.getValue());
        }

        return result;

    }

    public void setCompanys(Map<String, Long> cm){
        this.myCompanys.clear();

        for(Map.Entry<String, Long> entry: cm.entrySet()){
            this.myCompanys.put(entry.getKey(), entry.getValue());
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("Stocks from this Client.\n");

        for(Map.Entry<String, Long> entry: this.myCompanys.entrySet()){
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" actions.\n");
        }

        return sb.toString();
    }

    public void addActionsCompany(String c, long a){
        if(this.myCompanys.containsKey(c)){
            long tmp = this.myCompanys.get(c);
            this.myCompanys.put(c, tmp+a);
        }
        else{
            this.myCompanys.put(c, a);
        }
    }

    public boolean removeActionsCompany(String c, long a){
        if(this.myCompanys.containsKey(c)){
            long tmp = this.myCompanys.get(c);

            if(tmp >= a){
                this.myCompanys.put(c, tmp-a);
                return true;
            }

            return false;
        }

        return false;
    }
}
