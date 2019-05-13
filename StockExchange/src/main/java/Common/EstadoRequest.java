package Common;

public class EstadoRequest extends Msg{
    private int id;

    public EstadoRequest(int add){
        super(-1,"");
        this.id = add;
    }

    public int getId(){
        return this.id;
    }

    public String toString(){
        return "Pedido de estado por parte do servidor "+this.id;
    }
}
