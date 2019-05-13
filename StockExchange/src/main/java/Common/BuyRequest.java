package Common;

import io.atomix.utils.net.Address;

public class BuyRequest extends Msg{
    private String company;
    private long actions;

    public BuyRequest(int t, Address c, String cm, long a){
        super(t,c);
        this.company = cm;
        this.actions = a;
    }

    public String getCompany() {
        return company;
    }

    public long getActions() {
        return actions;
    }
}
