package Common;

public class BuyRequest extends Msg{
    private String company;
    private long actions;

    public BuyRequest(int t, int c, String cm, long a){
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
