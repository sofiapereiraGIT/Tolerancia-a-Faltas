package Stock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StockImp implements Serializable {
    private Map<String, Long> companies;

    public StockImp(){
        this.companies = new HashMap<String, Long>();
        this.companies.put("Mango", (long) 500);
        this.companies.put("Zara", (long) 500);
        this.companies.put("Nike", (long) 500);
    }

    public StockImp(Map<String, Long> c){
        this.companies = new HashMap<String, Long>();
        this.setCompanies(c);
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

        sb.append("Stocks from this Client\n");

        for(Map.Entry<String, Long> entry: this.companies.entrySet()){
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" actions.\n");
        }

        return sb.toString();
    }

    public long actions(String c){
        if(this.companies.containsKey(c)) return this.companies.get(c);
        return -1;
    }

    public boolean sell(String c, long a){
        if(this.companies.containsKey(c)){
            long tmp = this.companies.get(c);
            this.companies.put(c, tmp+a);

            return true;
        }

        return false;
    }

    public boolean buy(String c, long a){
        if(this.companies.containsKey(c)){
            long tmp = this.companies.get(c);

            if(tmp >= a){
                this.companies.put(c, tmp-a);
                return true;
            }

            return false;
        }

        return false;
    }
}
