package Common.Messages;

import java.io.Serializable;

public class ActionsRequest extends Message implements Serializable {
    private String company;

    public ActionsRequest(int t, String cl, String cm){
        super(t, cl);
        this.company = cm;
    }

    public String getCompany() {
        return company;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString());
        sb.append("--- ActionsRequest ---\n");
        sb.append("Company: ").append(this.company).append("\n");

        return sb.toString();
    }
}
