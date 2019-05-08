package Common;

public class Msg {
    private int transactionID;
    private int clientID;

    public Msg(int t, int c){
        this.transactionID = t;
        this.clientID = c;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public int getClientID() {
        return clientID;
    }
}
