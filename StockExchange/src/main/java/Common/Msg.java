package Common;

import io.atomix.utils.net.Address;

public class Msg {
    private int transactionID;
    private Address client;

    public Msg(int t, Address c){
        this.transactionID = t;
        this.client = c;
    }

    public int getTransactionID() {
        return this.transactionID;
    }

    public Address getClientID() {
        return this.client;
    }
}
