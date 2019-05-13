package Common.Messages;

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

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString());
        sb.append("--- BuyRequest ---\n");
        sb.append("Company: ").append(this.company).append("\n");
        sb.append("Number of actions: ").append(this.actions).append("\n");

        return sb.toString();
    }
}
