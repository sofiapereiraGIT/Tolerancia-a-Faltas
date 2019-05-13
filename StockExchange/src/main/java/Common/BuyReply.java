package Common;

public class BuyReply extends Message {
    private int serverID;
    private String company;
    private boolean result;

        public BuyReply(int t, String c, int s, String cm, boolean r){
        super(t,c);
        this.serverID = s;
        this.company = cm;
        this.result = r;
    }

    public int getServerID() {
        return this.serverID;
    }

    public String getCompany(){ return this.company;}

    public boolean isResult() {
        return this.result;
    }
}
