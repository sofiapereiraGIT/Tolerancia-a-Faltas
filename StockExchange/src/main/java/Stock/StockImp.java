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
        this.companys = c;
    }

    public Map<String, Long> getCompanys(){
        return this.companys;
    }

    public long actions(String c){
        return this.companys.get(c);
    }

    public void sell(String c, long a){
        long tmp = this.companys.get(c);
        this.companys.put(c, tmp+a);
    }

    public boolean buy(String c, long a){
        long tmp = this.companys.get(c);

        if(tmp >= a){
            this.companys.put(c, tmp-a);
            return true;
        }

        return false;
    }
}
