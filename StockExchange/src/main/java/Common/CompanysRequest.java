package Common;

import io.atomix.utils.net.Address;

public class CompanysRequest extends Msg{

    public CompanysRequest(int t, Address c){
        super(t, c);
    }
}
