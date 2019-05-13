package Common.Messages;

public class CompaniesRequest extends Message {

    public CompaniesRequest(int t, String c){
        super(t, c);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString());
        sb.append("--- CompaniesRequest ---\n");
        sb.append("Show me all the information, please.\n");

        return sb.toString();
    }
}