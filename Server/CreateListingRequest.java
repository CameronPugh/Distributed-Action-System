import java.io.Serializable;

public class CreateListingRequest implements Serializable {
    
    private static final long serialVersionUID = 1190476516911661470L;
    private int reservePrice;

    public CreateListingRequest(int reservePrice){
        this.reservePrice = reservePrice;
    }
    public int getReservePrice(){
        return reservePrice;
    }
}