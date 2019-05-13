package Client;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Client implements Serializable {
    private Map<String, Long> companys;

    public Client(){
        this.companys = new HashMap<String, Long>();
    }

    public Client(Map<String, Long> cm){
        this.companys = new HashMap<>();
        this.setCompanys(cm);
    }

    public Map<String, Long> getCompanys() {
        Map<String, Long> result = new HashMap<String, Long>();

        for(Map.Entry<String, Long> entry: this.companys.entrySet()){
            result.put(entry.getKey(), entry.getValue());
        }

        return result;

    }

    public void setCompanys(Map<String, Long> cm){
        this.companys.clear();

        for(Map.Entry<String, Long> entry: cm.entrySet()){
            this.companys.put(entry.getKey(), entry.getValue());
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("Stocks from this Client.\n");

        for(Map.Entry<String, Long> entry: this.companys.entrySet()){
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" actions.\n");
        }

        return sb.toString();
    }

    public void writeInTextFile(String fileName) {
        try {
            PrintWriter fich = new PrintWriter(fileName);
            fich.println(this.toString());
            fich.flush();
            fich.close();
        } catch (IOException e) {
            System.out.println("Error saving state in text file.");
        }
    }

    public void storeState(String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.flush();
            oos.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("Error saving state.");
        }
    }

    public Client loadState(String fileName) {
        Client c = new Client();

        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            c = (Client) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Could not find previous state.");
        }

        return c;
    }
}
