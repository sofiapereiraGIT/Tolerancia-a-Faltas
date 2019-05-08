package Stock;

import java.util.Map;

public interface Stock{
    Map<String, Long> getCompanys();
    long actions(String company);
    void sell(String company, long value);
    boolean buy(String company, long value);

}
