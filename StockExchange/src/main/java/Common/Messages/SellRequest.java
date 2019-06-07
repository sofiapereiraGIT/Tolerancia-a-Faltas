package Common.Messages;

import java.io.Serializable;

public class SellRequest extends Message implements Serializable {
    private String company;
    private long actions;

    public SellRequest(int t, String c, String cm, long a){
        super(t,c);
        this.company = cm;
        this.actions = a;
    }

    public String getCompany(){
        return this.company;
    }

    public long getActions(){
        return this.actions;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString());
        sb.append("--- SellRequest ---\n");
        sb.append("Company: ").append(this.company).append("\n");
        sb.append("Number of actions: ").append(this.actions).append("\n");

        return sb.toString();
    }
}
