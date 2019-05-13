package Common;

public class SellReply extends Msg{
    private int serverID;
    private String company;
    private boolean result;

    public SellReply(int t, String c, int s, String cm, boolean b){
        super(t, c);
        this.serverID = s;
        this.company = cm;
        this.result = b;
    }

    public int getServerID(){
        return this.serverID;
    }

    public String getCompany(){ return this.company;}

    public boolean getResult(){
        return this.result;
    }
}
