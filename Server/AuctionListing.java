import java.io.Serializable;

public class AuctionListing implements Serializable 
{
    private static final long serialVersionUID = 1190476516911661470L;
    private int auctionID;
    private int startingPrice;
    private int reservePrice;
    private int clientID;//client who listed auction
    private AuctionItem itemID; //item details for sale

    
    private int currentBiddersID;
    private String buyersName;
    private String buyersEmailAddr;
    

    public AuctionListing(int alid, int sPrice, int rPrice, int id, AuctionItem iID){
        this.auctionID = alid;
        this.startingPrice = sPrice;
        this.reservePrice = rPrice;
        this.clientID = id;
        this.itemID = iID;
        
        this.currentBiddersID = 0;
        this.buyersName = "";
        this.buyersEmailAddr = "";
        
    }
    public AuctionListing(int alid){
        this.auctionID = alid;
        this.startingPrice = 0;
        this.reservePrice = 0;
        this.clientID = 0;
        this.itemID = null;
        
        this.currentBiddersID = 0;
        this.buyersName = "";
        this.buyersEmailAddr = "";
        
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
    public int getReservePrice(){
        return reservePrice;
    }
    public String getBuyersName(){
        return buyersName;
    }
    public String getBuyersEmail(){
        return buyersEmailAddr;
    }
    
    public void setBiddersID(int bID){
        this.currentBiddersID = bID;
    }
    public void setName(String name){
        this.buyersName = name;
    }
    public void setEmail(String email){
        this.buyersEmailAddr = email;
    }
    public void setStartingBid(int bid){
        this.startingPrice = bid;
    }
}
