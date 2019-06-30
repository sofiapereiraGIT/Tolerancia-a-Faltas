package Client;

import io.atomix.utils.net.Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(final String[] args) {
        final Address cliAddress = Address.from("localhost:"+args[0]);
        final ClientStub stub = new ClientStub(args[0]);
        final Client client = new Client();

        System.out.println("\n--- client info ---");
        if(client.getCompanys().size()==0){
            System.out.println("You don't have any actions.\n");
        }
        else {
            System.out.println(client.toString()+"\n");
        }

        //--- OPERATIONS ---

        final List<String> companies = new ArrayList<>();

        //CompaniesRequest
        Map<String, Long> companiesReply = null;
        try {
            System.out.println("Sending companies request.");
            companiesReply = stub.getCompanies().get();
            StringBuilder sb1 = new StringBuilder();
            for(Map.Entry<String, Long> entry : companiesReply.entrySet()){
                sb1.append(entry.getKey()).append(": ").append(entry.getValue()).append(" actions.\n");
                companies.add(entry.getKey());
            }
            System.out.println(sb1.toString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        //Actions, Buy and Sell Request
        final int numOps = 5; // Total number of random requests
        final Random random = new Random();
        int randomOperation, randomCompany;
        Map<String, Long> clientCompanies;

        for (int i = 0; i < numOps; i++) {
            if(i==3){
                try {
                    System.out.println("\n>> sleeping\n");
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            randomOperation = random.nextInt(3);
            clientCompanies = client.getCompanys();

            if(randomOperation==2 && clientCompanies.size()==0){
                randomOperation=1;
            }

            switch (randomOperation) {
                case 0:
                    randomCompany = random.nextInt(companies.size());
                    String electedCompany = companies.get(randomCompany);

                    System.out.println("Sending actions request for "+electedCompany+".");

                    stub.getActions(electedCompany).thenAccept((var) -> {
                        StringBuilder sb = new StringBuilder();

                        for(Map.Entry<String, Long> entry : var.entrySet()){ //only one entry
                            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" actions.\n");
                        }

                        System.out.println(sb.toString());
                    });
                    break;

                case 1:
                    randomCompany = random.nextInt(companies.size());
                    final String selectedCompanyBuy = companies.get(randomCompany);
                    final int randomQuantityBuy = random.nextInt(20);

                    System.out.println("Buying "+randomQuantityBuy+" actions from "+selectedCompanyBuy+".");

                    stub.buy(selectedCompanyBuy, randomQuantityBuy).thenAccept((var) -> {
                        StringBuilder sb = new StringBuilder();

                        for(Map.Entry<String, Boolean> entry : var.entrySet()){ //only one entry
                            if(entry.getValue()){
                                sb.append("Success! You just bought ").append(randomQuantityBuy).append(" actions from ").append(selectedCompanyBuy).append(".\n");
                                client.addActionsCompany(entry.getKey(), randomQuantityBuy);
                            }
                            else{
                                sb.append("Sorry, it wasn't possible to buy ").append(randomQuantityBuy).append(" actions from ").append(selectedCompanyBuy).append(".\n");
                            }
                        }

                        System.out.println(sb.toString());
                    });
                    break;

                case 2:
                    randomCompany = random.nextInt(clientCompanies.size());
                    final String selectedCompanySell = (String) clientCompanies.keySet().toArray()[randomCompany];
                    final int randomQuantitySell = random.nextInt(Math.toIntExact(clientCompanies.get(selectedCompanySell)));

                    System.out.println("Selling "+randomQuantitySell+" actions from "+selectedCompanySell+".");

                    stub.sell(selectedCompanySell, randomQuantitySell).thenAccept((var) -> {
                        StringBuilder sb = new StringBuilder();

                        for(Map.Entry<String, Boolean> entry : var.entrySet()){ //only one entry
                            if(entry.getValue()){
                                sb.append("Success! You just sold ").append(randomQuantitySell).append(" actions from ").append(selectedCompanySell).append(".\n");
                                final boolean bool = client.removeActionsCompany(entry.getKey(), randomQuantitySell);
                                if(!bool){
                                    sb.append("Something went wrong while selling your actions.\n");
                                }
                            }
                            else{
                                sb.append("Sorry, it wasn't possible to sell ").append(randomQuantitySell).append(" actions from ").append(selectedCompanySell).append(".\n");
                            }
                        }

                        System.out.println(sb.toString());
                    });
                    break;

                default:
                    System.out.println("Invalid operation.");
                    System.exit(1);
                    break;
            }
        }
    }
}
