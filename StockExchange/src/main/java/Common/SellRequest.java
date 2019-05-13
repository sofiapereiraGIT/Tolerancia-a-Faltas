package Common;

import io.atomix.utils.net.Address;

public class SellRequest extends Msg{
    private String company;
    private long actions;

    public SellRequest(int t, Address c, String cm, long a){
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
}
