import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyPair;

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
import java.util.Scanner;
import java.util.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

public class AuctionClient 
{ //buyer Client
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
        CRequest clientReq = new CRequest(n);
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
            File myObj = new File("/Users/cameronpugh/Documents/311/SCC_311_CW/Stage2/key.txt");
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

            System.out.print("Enter a function - [Get (itemID), Show_All, Bid(AuctionId,Bid,Name,Email)]: \n");

            String str = scanner.nextLine();  
            String[] userInput = str.split(",");

            if(userInput[0].equals("Get"))
            {
                try
                {
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey);

                    int itemId = Integer.parseInt(userInput[1]);
                    SealedObject x = new SealedObject(clientReq, cipher);    
                    SealedObject result = server.getSpec(itemId,x);
                    AuctionItem a = (AuctionItem) result.getObject(aesKey);
                    String s = (a.getItemCondition() +","+a.getItemDescription()+","+a.getItemTitle());
                    System.out.println("result is " + s);
                }
                catch (Exception e)
                {
                    System.err.println("Exception:");
                    e.printStackTrace();
                }

            }
            if(userInput[0].equals("Show_All"))
            {
                try
                {
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey);

                    List<SealedObject> result = server.showAllListings();
                    for (SealedObject item : result)
                    {
                        Listing a = (Listing) item.getObject(aesKey);
                        String s1 = (Integer.toString(a.getAuctionID()));
                        String s2 = (Integer.toString(a.getStartingPrice()));
                        String s3 = (Integer.toString(a.getClientID()));
                        AuctionItem i = a.getAuctionItem();
                        String s4 = (Integer.toString(i.getID()));

                        System.out.println("Auction " + s1 + " :" + " Current Bid = " + s2 + " , ItemID = " + s4 + ", Listed by client " + s3);

                    }
                }
                catch (Exception e)
                {
                    System.err.println("Exception:");
                    e.printStackTrace();
                }

            }
            if(userInput[0].equals("Bid"))
            {
                try
                {
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey);

                    int auctionId = Integer.parseInt(userInput[1]);
                    int newBid = Integer.parseInt(userInput[2]);

                    SealedObject x = new SealedObject(clientReq, cipher);    
                    int bidProcessed = server.bid(auctionId,newBid,userInput[3],userInput[4],x);
                    if(bidProcessed==1)
                    {
                        System.out.println("New Bid Processed");
                    }
                    else{
                        System.out.println("Bid entered no big enough");
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
   
