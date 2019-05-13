package Stock;

import java.util.HashMap;
import java.util.Map;

public class StockImp {
    private Map<String, Long> companys;

    public StockImp(){
        this.companys = new HashMap<String, Long>();
        this.companys.put("Mango", (long) 500);
        this.companys.put("Zara", (long) 500);
        this.companys.put("Nike", (long) 500);
    }

    public StockImp(Map<String, Long> c){
        this.companys = new HashMap<String, Long>();
        this.setCompanys(c);
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

    public long actions(String c){
        if(this.companys.containsKey(c)) return this.companys.get(c);
        return -1;
    }

    public boolean sell(String c, long a){
        if(this.companys.containsKey(c)){
            long tmp = this.companys.get(c);
            this.companys.put(c, tmp+a);

            return true;
        }

        return false;
    }

    public boolean buy(String c, long a){
        if(this.companys.containsKey(c)){
            long tmp = this.companys.get(c);

            if(tmp >= a){
                this.companys.put(c, tmp-a);
                return true;
            }

            return false;
        }

        return false;
    }
}
