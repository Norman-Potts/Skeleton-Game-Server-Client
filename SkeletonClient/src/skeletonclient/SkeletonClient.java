/**
 *  Skeleton Client
 * 
 *  How to run program.
 *      1. Start the SkeletonServer java project.
 *      2. Get the port number from the output of the server program.
 *      3. Determine the hostname of computer.
 *      4. Run SkeletonClient with hostname, and port number in the args. 
 *          Ex.  java -jar SkeletonClient.jar 'localhost' '56946'
 *      5. Do step 4 One too Five times.
 *      6. Play hangman game on each client.
 */
package skeletonclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/** Public Class Skeleton Client
 *      Purpose: Communicates with server. Provides an way for the player to 
 *               play the hangman game.
 */
public class SkeletonClient {
    public static void main(String [] args) {
        String serverName = args[0]; //Get the name of the computer running this in first args parameter.                                           
        int port = Integer.parseInt( args[1] ); //Get the port number of the server from the second args parameter.                                 
        Scanner keyboard = new Scanner(System.in);                              
        String typing = "";//Will hold input from user.                                               
        /** Try to...
         *      Set up connection to server and play hangman.
         *   Catch Input Output errors.
         *      When errors, print errors out.
         */
        try {  
            /* Set up connects to server. */
            Socket client = new Socket(serverName, port);                       
            String printMSG = ""+client.getRemoteSocketAddress();
            System.out.println("Just connected to " +printMSG);                 
            OutputStream outToServer = client.getOutputStream();                
            DataOutputStream out = new DataOutputStream(outToServer);           
            out.writeUTF("Hello from " + client.getLocalSocketAddress());       
            InputStream inFromServer = client.getInputStream();                 
            DataInputStream in = new DataInputStream(inFromServer);            
            /* Print out the first message the sever has sent. */
            String messageFromServer = ""+in.readUTF();            
            System.out.print( messageFromServer );            
            Boolean EXITloop = false; //Flag controls do while loop.
            /**
             *  Do while,
             *      The user has not said exit or, 
             *      The server has not said the game finished.
             */
            do {                
                typing = keyboard.nextLine(); //Get user input.                                   
                out.writeUTF(typing); //Send user input to server.                                                                
                /*  Try to put thread to sleep for 1000 miliseconds so software can catch up.  */
                try{Thread.sleep(1000);}catch(InterruptedException ie){System.err.print(ie);}                          
                /**
                 *  If data in is available,
                 *      Receive the message from the server and,
                 *      prepare it as a string and,
                 *      determine if it is the ending game message.
                 */
                if(in.available() > 0) { 
                    messageFromServer = ""+in.readUTF();                        
                    String lastThree  = "";
                    int messageLength = messageFromServer.length();
                    /**
                     *  If messageLength is greater than 19 than find out if 
                     *  the message is the ending game message.
                     */                                    
                    if( messageLength >= 20 ) {
                        int x = messageLength-1;
                        int y = x-3;
                        lastThree = messageFromServer.substring( y,x );
                        /**
                         *  If last three characters is '...' than it is the end 
                         *  of the game so exit the loop.
                         */
                        if(lastThree.equals("...") ) {   
                            System.out.println( messageFromServer );  
                            messageFromServer = ""+in.readUTF();
                            EXITloop = true;
                        }
                    }                    
                    /* Print message from server. */ 
                    System.out.println( messageFromServer );                   
                }/* End of If data is avaliable. */                
                /**
                 *  If user entered the string 'exit' than end while loop and 
                 *  begin turn off connections.
                 */
                if (typing.equals("exit"))
                {   EXITloop = true;    }                
            } while(EXITloop == false);                    
            /** 
             *  Tell Server to close up connection to this client and close
             *  the connections on the client side.   
             */
            out.writeUTF("exit");                                               
            out.close();                                                        
            in.close();                                                         
            client.close();                                                     
        }
        catch(IOException e)
        {   e.printStackTrace();    }
        /**
         *  End of try to connect to server and play hang man.    
         *  Catch will handle Input Output Exceptions.
         */
    }/*End of Method main*/
}/*End of Skeleton Client*/