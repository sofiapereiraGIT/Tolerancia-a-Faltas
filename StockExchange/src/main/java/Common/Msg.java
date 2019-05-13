package Common;

public class Msg {
    private int transactionID;
    private String clientName;

    public Msg(int t, String c){
        this.transactionID = t;
        this.clientName = c;
    }

    public int getTransactionID() {
        return this.transactionID;
    }

    public String getClientName() {
        return this.clientName;
    }
}
