package Common;

public class ActionsRequest extends Msg{
    private String company;

    public ActionsRequest(int t, int cl, String cm){
        super(t, cl);
        this.company = cm;
    }

    public String getCompany() {
        return company;
    }
}
