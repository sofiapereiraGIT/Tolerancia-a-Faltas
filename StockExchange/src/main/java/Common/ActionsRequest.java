package Common;

import io.atomix.utils.net.Address;

public class ActionsRequest extends Msg{
    private String company;

    public ActionsRequest(int t, Address cl, String cm){
        super(t, cl);
        this.company = cm;
    }

    public String getCompany() {
        return company;
    }
}
