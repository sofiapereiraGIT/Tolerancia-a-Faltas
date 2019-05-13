package Common;

public class SellRequest extends Message {
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
}
