package Common.Messages;

public class MembershipInfoRequest extends Message{

    public MembershipInfoRequest(int t, String c){
        super(t, c);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("--- MembershipInfoRequest ---\n");
        sb.append("From client: ").append(this.getClientName()).append(".\n");
        return sb.toString();
    }
}
