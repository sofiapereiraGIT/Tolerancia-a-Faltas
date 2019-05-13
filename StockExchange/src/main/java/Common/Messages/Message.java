package Common.Messages;

public class Message {
    private int transactionID;
    private String clientName;

    public Message(int t, String c){
        this.transactionID = t;
        this.clientName = c;
    }

    public int getTransactionID() {
        return this.transactionID;
    }

    public String getClientName() {
        return this.clientName;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("--- Message ---\n");
        sb.append("From: ").append(this.clientName).append("\n");
        sb.append("Transaction ID: ").append(this.transactionID).append("\n");

        return sb.toString();
    }
}
