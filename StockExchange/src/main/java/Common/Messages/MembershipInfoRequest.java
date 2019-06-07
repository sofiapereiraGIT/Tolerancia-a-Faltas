package Common.Messages;

import java.io.Serializable;

public class MembershipInfoRequest extends Message implements Serializable {

    public MembershipInfoRequest(int t, String c){
        super(t, c);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString());
        sb.append("--- MembershipInfoRequest ---\n");
        sb.append("From client: ").append(this.getClientName()).append(".\n");
        return sb.toString();
    }
}
