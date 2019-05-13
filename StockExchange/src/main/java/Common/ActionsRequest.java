package Common;

public class ActionsRequest extends Message {
    private String company;

    public ActionsRequest(int t, String cl, String cm){
        super(t, cl);
        this.company = cm;
    }

    public String getCompany() {
        return company;
    }
}
