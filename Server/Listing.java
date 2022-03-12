import java.io.Serializable;

public class Listing implements Serializable 
{
    private static final long serialVersionUID = 1190476516911661470L;
    private int auctionID;
    private int startingPrice;
    private int clientID;//client who listed auction
    private AuctionItem itemID; //item details for sale 

    public Listing(int alid, int sPrice, int id, AuctionItem iID){
        this.auctionID = alid;
        this.startingPrice = sPrice;
        this.clientID = id;
        this.itemID = iID;     
    }
    public int getAuctionID(){
        return auctionID;
    }
    public int getStartingPrice(){
        return startingPrice;
    }
    public int getClientID(){
        return clientID;
    }
    public AuctionItem getAuctionItem(){
        return itemID;
    }
}
