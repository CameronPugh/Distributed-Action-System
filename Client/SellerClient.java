import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.crypto.SealedObject;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.*;
import java.io.ObjectInputStream;  // Import the File class
import java.io.ObjectOutputStream; 
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Object;
import java.io.File;
import java.util.Random;
import java.util.Scanner;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

public class SellerClient 
{
    private static final String SIGNING_ALGORITHM = "SHA256withRSA";
    private static final String RSA = "RSA";
    
    public static File getFileObj(String path){
        File myObj = null;
        try{
            myObj = new File(path);
        }catch(Exception e){
        }
        return myObj;
    }
    public static byte[] Create_Digital_Signature(int userID, int challenge)
    {
        try{
            Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
            File myObj = getFileObj("/Users/cameronpugh/Documents/311/SCC_311_CW/Stage2/AuthenticationKeys"+ (Integer.toString(userID)) +".txt");
            if(myObj != null){
                FileInputStream fos = new FileInputStream(myObj);
                ObjectInputStream oin = new ObjectInputStream(fos);
                KeyPair key = (KeyPair)oin.readObject();
                oin.close();

                signature.initSign(key.getPrivate());
                signature.update((Integer.toString(challenge)).getBytes());
                return signature.sign();
            }
        }catch(Exception e){      
        }
        return null;      
    }
    public static boolean verifySigniture(byte[] signatureToVerify, int challenge)
    {
        try{
            Signature signature = Signature.getInstance(SIGNING_ALGORITHM);

            File myObj = new File("/Users/cameronpugh/Documents/311/SCC_311_CW/Stage2/ServerKeys.txt");
            FileInputStream fos = new FileInputStream(myObj);
            ObjectInputStream oin = new ObjectInputStream(fos);
            KeyPair key = (KeyPair)oin.readObject();
            oin.close();

            signature.initVerify(key.getPublic());
            signature.update((Integer.toString(challenge)).getBytes());
            return signature.verify(signatureToVerify);

        }catch (Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return false;
    }
    public static boolean AuthenticateServer(IAuction server)
    {
        Random rand = new Random();
        int challengeValue = rand.nextInt(9999999);

        try{
            byte[] x = server.requestSignature(challengeValue);
            boolean request = verifySigniture(x,challengeValue);
            return request;

        }catch (Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return false;
    }
    public static boolean AuthenticateClient(int userID, IAuction server)
    {
        try{
            int x = server.requestChallenge();
            byte[] m = Create_Digital_Signature(userID,x);
            boolean request = server.verifySignature(m,userID);
            return request;
        }catch (Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return false;

    }
    public static void main(String[] args)
    {
        
        if (args.length < 1)
        {
        System.out.println("Usage: java Client n \n n = clientID");
        return;
        }

        Scanner scanner = new Scanner(System.in);  // Create a Scanner object
        int n = Integer.parseInt(args[0]);
        CRequest clientReq = new CRequest(n);//client request object containg clientID
        SecretKey aesKey = null;
        IAuction server = null;

       //Connect to Server
        try
        {
           String name = "myserver";
           Registry registry = LocateRegistry.getRegistry("localhost");
           server = (IAuction) registry.lookup(name);
       
        }
        catch (Exception e)
        {
            System.err.println("Exception:");
            e.printStackTrace();
        }
        
        //Authenication   
        if(AuthenticateServer(server))
        {       
            System.out.println("Server Authenticated");
            if(AuthenticateClient(n,server))
            {
                System.out.println("User Authenticated");
                System.out.println("Authentication Complete");
            }
            else{
                System.out.println("User Authentication failed");
                return;
            }
        }  
        else{
            System.out.println("Server Authentication failed");   
            return;
        }
        
        
        //GET KEY for encrytion
        try
        {
            File myObj = new File("/Users/cameronpugh/Documents/311/SCC_311_CW/Stage2/cw/key.txt");
            FileInputStream fos = new FileInputStream(myObj);
            ObjectInputStream oin = new ObjectInputStream(fos);
            aesKey = (SecretKey)oin.readObject();


            oin.close();
        }
        catch(Exception e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        
        //Run functions when detected
        while(true)
        {

            System.out.print("Enter a function - [Create_Listing (Starting Price, Reserve Price, Item Description , Item title , item condition) , Close_Listing(aucID)]: \n");

            String str = scanner.nextLine();  
            String[] userInput = str.split(",");

            if(userInput[0].equals("Create_Listing"))
            {
                try
                {
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey); 
                    
                    int sPrice = Integer.parseInt(userInput[1]);
                    int rPrice = Integer.parseInt(userInput[2]);
                    CreateListingRequest encapRPrice = new CreateListingRequest(rPrice);
                    
                    SealedObject soClientR = new SealedObject(clientReq, cipher);    
                    SealedObject soRPrice = new SealedObject(encapRPrice, cipher);   
                    
                    SealedObject result = server.createAuction(sPrice,userInput[3],soRPrice,userInput[4],userInput[5],soClientR);

                    AuctionListing a = (AuctionListing) result.getObject(aesKey);

                    String s = (Integer.toString(a.getAuctionID()));
                    System.out.println("New Auction created using acutionID:  " + s);
                }
                catch (Exception e)
                {
                    System.err.println("Exception:");
                    e.printStackTrace();
                }

            }
            if(userInput[0].equals("Close_Listing"))
            {
                try
                {
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey);

                    int auctionId = Integer.parseInt(userInput[1]);
                    SealedObject x = new SealedObject(clientReq, cipher);    
                    SealedObject result = server.closeAuction(auctionId,x);
                    
                    AuctionListing a = (AuctionListing) result.getObject(aesKey);

                    if(a.getAuctionID()==0){
                        System.out.println("Auction closes with no winner");
                        //return;
                    }
                    else if(a.getAuctionID()==-1){
                        System.out.println("Can't close this auction (You Didnt create this auction)");
                        //return;
                    }
                    else{
                        String s = (Integer.toString(a.getAuctionID()));
                        System.out.println("Closed Auction ID :" + s);

                        String nam = a.getBuyersName();
                        String e = a.getBuyersEmail();

                        System.out.println("Winner of Auction is:"+nam+","+e);
                    }
                    
                }
                catch (Exception e)
                {
                    System.err.println("Exception:");
                    e.printStackTrace();
                }
                
            }
        }

    }

}