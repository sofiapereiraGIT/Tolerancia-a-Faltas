package Common;

public class BuyRequest extends Message {
    private String company;
    private long actions;

    public BuyRequest(int t, String c, String cm, long a){
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
