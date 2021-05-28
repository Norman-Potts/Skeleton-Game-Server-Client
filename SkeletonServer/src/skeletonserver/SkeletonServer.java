/**
 *  Skeleton Server
 * 
 *  How to run program.
 *      1. Start the SkeletonServer java project.
 *      2. Get the port number from the output of the server program.
 *      3. Determine the hostname of computer.
 *      4. Run SkeletonClient with hostname, and port number in the args.
 *      5. Do step 4 One too Five times.
 *      6. Play hangman game on each client.
 * 
 */
package skeletonserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/** Public Class SkeletonServer
 *      Purpose: Communicate with skeleton client. Manages the multiple clients 
 *               playing the skeleton game.
 */
public class SkeletonServer extends Thread
{
    /**
     *  Shared data variables and their monitors.
     */
    /* countofCurrentClients: A count of current clients connected to the server. This variable gets its own monitor. */
    private static int countofCurrentClients;   private final static Object LOCK_FOR_CLIENT_COUNT = new Object();                                                                    
    /* hangmanGuessWord: The hangman word of the server's game. This variable gets its own monitor. */
    private static String hangmanGuessWord = "";    private final static Object LOCK_FOR_HANGMAN_GUESS_WORD = new Object();                         
    /* everyoneisDone: A flag that switches to true when everyone is done. This variable gets it's own monitor.*/
    private static Boolean everyoneisDone = false;   private final static Object LOCK_FOR_EVERYONE_IS_DONE = new Object();                           
    /* CountOfClientsDone: A count of clients that have finished the hang man. This variable gets its own monitor. */
    private static int CountOfClientsDone;           private final static Object LOCK_FOR_COUNT_OF_CLIENTS_DONE = new Object();                        
    /* EndServer: A flag that singles the sever has closed up and main can end. This variable gets its own monitor. */
    private static Boolean EndServer = false;   private final static Object LOCK_FOR_END_SERVER = new Object();
    /* mySocket: A ServerSocket variable that will hold the socket of the server. */
    private static ServerSocket mySocket;   private final static Object LOCK_FOR_MY_SOCKET = new Object();   
    /* multiDataMaps: A concurrentHashMap that will hold the game data for mulitple clients.    This variable gets its own monitor. */
    private final static ConcurrentHashMap<Integer, Hashtable> multiDataMaps = new ConcurrentHashMap<Integer, Hashtable>();     private final static Object LOCK_FOR_MULTI_DATA_MAPS = new Object();

    /**
     *  Not shared data variables, they do not need thread monitors.
     */
    /* server: Contains the server info */
    private static Socket server;                                               //Declare a Private Scoket variable called server   
    /* in: handles the data arriving in for this run() */
    private DataInputStream in;                                                 //Declare a private DataInputStream variable called in.    
    /* out: handles the data arriving out for this run() */
    private DataOutputStream out;                                               //Declare a private dataOutputStream variable called out.   
    /* DoubleAlphabet: A string of the alphabet with lower case first then uppercase.*/
    private final static String DoubleAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";//A string of alphabetical letters, first 26 are lowercase last are uppercase.
    /* AlphabetArr: The DoubleAplphabet converted to a character array, will be used to determine kind of letters later.*/
    private final static char[] AlphabetArr = DoubleAlphabet.toCharArray();//Convert the double alphabet to a character array. It will be used later to help convert letters to upper case.       
    
    
    
    
    /** Method main
     *      Purpose: Begins to set up the game and starts the server. Waits till
     *               someone connects. Ends when everyone has disconnected or an
     *               Input output error happens.
     */
    public static void main(String[] args) throws IOException 
    {    
        ChooseMovieTitle();//Set Hang mang guess word.                        
        
        /** Set up port for this server */
        int port = 0; mySocket = new ServerSocket(port);       
        countofCurrentClients= 0; CountOfClientsDone = 0;
                
        /** 
         * Do...
         *      Wait for a client to connect.
         *      Create a new thread when a client connects.
         *      Transfer the value of static variable EndServer into 
         *      endserverplz.
         * 
         * While endserverplz flag is equal to false.
         *      When the endserverplz flag has been switched to true 
         *      all clients have disconnected and game is done.
         */
        boolean endserverplz = false;
        do 
        {   try 
            {        
                /*Print the port that the server is waiting for it to be on.*/
                System.out.println("Waiting for client on port "+ mySocket.getLocalPort() + "...");
                /** Begin waiting until a client begins connecting or socket 
                 *  gets closed in run.
                 */
                Socket server = mySocket.accept(); 
                
                /*Set up a thread to run the game for this client that connected.*/
                Thread t = new SkeletonServer(server); //Creates SkeletonServer 
                t.start();// runs method run()                                                
            
            }catch (IOException e){ 
                /** Catch any input output exceptions and turn off server when
                 *  there is.
                 */
                synchronized(LOCK_FOR_END_SERVER)
                {endserverplz = EndServer; }
                /**  If endserverplz is false when this catch gets thrown then it 
                 *   did not occur when everyone finished. Report error to server 
                 *   console.
                 */
                if (endserverplz == false)
                {e.printStackTrace();}                
            }
            /* Transfer the value of static EndServer to local endserverplz. */
            synchronized(LOCK_FOR_END_SERVER)
            {endserverplz = EndServer; }
            
        }while (endserverplz == false);
        /*End while.*/
        
    }/* End of Method main */
    
    
    
    
    
    
    
    
    
    
    /** Method ChooseMovieTitle()
     *      Purpose: Reads the movies.txt file and chooses a hangman word.        
     */
    public static void  ChooseMovieTitle() throws FileNotFoundException, IOException
    {                
        //Open movies text and count lines.
        String path = "./src//skeletonserver//Movies.txt" ;
        File file = new File(path);                        
        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        int Countoflines = 0;        
        while (reader.readLine() != null) 
        {   Countoflines++; }
        reader.close();                
        // Lines have been counted into CountofLines.
        
        //Choose a random line number within countoflines.
        Random rand = new Random();        
        
        int linenumber = 7;
        
        //Reopen movie.text 
        reader = new BufferedReader( new FileReader( file ) );
        //Go to line number and get name of movie.        
        String movieText = "";        
        for(int i = 0; i < linenumber; i++ )
        {   movieText = reader.readLine();  }
        reader.close();                        
        
        /**
         *  Make hangmanGuessword upper case because this game assumes that 
         *  case does not matter for guessing in the hangman game. Meaning 
         *  players don't have to worry about case sensitivity when guessing.
         */
        char[] hangsplit = movieText.toCharArray();
        int j= 0;
        for( char character : hangsplit)
        {            
            int i = 0;
            for( char x : AlphabetArr)
            {
                if(x == character && i <= 25)
                {
                    character = AlphabetArr[ i+26  ];//needed
                    break;
                }                                                
                i++;
            }
            /* Character  has now been converted to capital.  */                                              
            hangsplit[j] = character;
            j++;
        }        
        hangmanGuessWord = new String(hangsplit); //Assign capitalised value of movie text to hangmanGuessWord        
        System.out.println("The movie is : "+ movieText ); //Print the value of movieText defalut case.
        
    }/*End of Method ChooseMovieTitle */    
    
    
    
    
    
    
    
    
    
    
    /** Public Constructor SkeletonServer
     *      Purpose: Assigns theSocket to the server for this thread and 
     *               increments The countofCurrentClients.
     */
    public SkeletonServer(Socket theSocket) throws IOException 
    {               
        /*Assign the value of theSocket to this client's server.*/
        server = theSocket;              
        /*Increment count of clients that have connected.*/
        synchronized(LOCK_FOR_CLIENT_COUNT){   countofCurrentClients++;    }       
    }/* End of SkeletonServer Constructor. */
    
    
    
    
    
    
    
    
    
    
    /** Public Method run()
     *      Purpose: Runs when a client connects. Manages the game for that 
     *               client. Knows when to end server.
     */
    public void run() 
    {
        /* Create Variables used in run */        
        String line = ""; /* Variable line: Will be used to hold clients input. */        
        boolean EXITdoLOOP = false; /* Variable EXITdoLOOP: A flag that controls the exitloop*/                                        
        int playerID;/* Variable playerID: will hold the value of this client's ID */
        synchronized(LOCK_FOR_CLIENT_COUNT) {   playerID = countofCurrentClients + 100;    }                        
        char[] GuessWordCharArr; /* Variable GuessWordCharArr: A local value of hangmanGuessWord as a Character array. */
        synchronized(LOCK_FOR_HANGMAN_GUESS_WORD) {   GuessWordCharArr = hangmanGuessWord.toCharArray();  }                
        
        /*  try to recive and send connections...       */  
        try 
        {
            /* Set up data inout connections */        
            System.out.println("\nJust connected to " + server.getRemoteSocketAddress());//print out connection to a client.          
            in = new DataInputStream(server.getInputStream());  //Assing Datainputstream from server to in object                        
            out = new DataOutputStream(server.getOutputStream());   //Assign Dataoutputstream from server to out object            
            System.out.println("Start Hangman game with client: "+ server.getRemoteSocketAddress());  
            
            /**
             *  Set up the gameDataMap, method setUpGameMap does this and returns
             *  the gameDataMap for this client.
             */                            
            Hashtable <String, String>  gameDataMap = setUpGameMap( playerID ,GuessWordCharArr );          
            /* gameDataMap is now setup for the hangman game. */                
            
        
            
            
            /** 
             * Do...
             *      Manage the game for this client.
             *      When appropriate exit loop and produce ending message.
             * 
             * While EXITdoLOOP is equal to false.
             *      EXITdoLOOP is switched to true when...
             *          All clients have completed the game. 
             *          This client says "exit".
             *          The count of clients is equal to the count of clients done.
             *          
             */            
            int helloSkip = 0;/* Variable helloSkip: Allows loop to know if it should skip reading the first message from the client.*/
            do
            {               
                /** If input is available, 
                 *      analyze it and determine what to reply with.
                 */        
                if ( in.available() > 0)                              
                {                                  
                    String MSgPort = ""; /*Will hold the string val of the clients port.*/        
                    line = in.readUTF(); /*Read data in from client*/
                    MSgPort = ""+server.getPort();       
                    System.out.println("Recived: "+line+" From:"+MSgPort+" PlayerID"+playerID); /*Display what was recived from client and the port of the client.*/
                    String status = gameDataMap.get("gameStatus");
                                        
                    /** if Client sent "exit", 
                     *       Write goodbye message to client
                     *  else if game status not equal to done,
                     *      continue to play game.
                     */                                                                
                    if(line.equals("exit"))
                    {   
                        out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress() + "\nGoodbye!");                                   
                    }
                    else if( !status.equals("Done") )
                    {   
                        /**
                         * Client sent something other than exit, and game isn't
                         * done.
                         */
                        String MessageOut = ""; /* Get The MessageOut ready.*/
                        
                        /**
                         *  if helloSkip is == 0 than
                         *      Increment it, making the next loop skip read the
                         *      input from the client.
                         *  else
                         *      The hello message from the client has been
                         *      skipped. Begin sending actual game messages.
                         */
                        if(helloSkip == 0)                                      /*Skips the hello message from the game.*/
                        {   helloSkip++;    }
                        else 
                        {   
                            /**
                             * Hello message has passed everything else should
                             * be guesses. Increment the guess score.
                             */
                            String strscore ="";        
                            strscore = gameDataMap.get("Score");                        
                            int score = Integer.parseInt(strscore);
                            score++;//Increment score
                            strscore = ""+score+""; 
                            MessageOut += "Your Score:"+strscore+".   ";                                    
                            gameDataMap.put("Score", strscore );//Put score back in game data.                
                            
                            synchronized(LOCK_FOR_MULTI_DATA_MAPS)// Place gameDataMap in multiDataMaps               
                            { multiDataMaps.put( playerID, gameDataMap);  }
                            
                            /** 
                             * End of Increment the guess score
                             * Is it a guess of whole word or letter?
                             */
                            final int xlinelength = line.length();
                            if( xlinelength == 1 )
                            {                                   
                                /**
                                 * client send a message equal to 1 character
                                 * so Guess for one letter.
                                 */
                                char letter = line.charAt(0);
                                int letterNo = 0; int posArr = 0;                                
                                //Is letter actually a letter?
                                Boolean actuallyaletter = false;
                                for(char a : AlphabetArr )
                                {   
                                    if( letter == a   )
                                    {   
                                        actuallyaletter = true;
                                        posArr = letterNo;
                                    }
                                    letterNo++;
                                }
                                if(actuallyaletter == true)
                                {
                                    /* Determine if the letter has already been guessed */                                    
                                    if(posArr <= 25)                            //if letter is not a capital, Make it a capital.
                                    {                                        
                                        posArr = posArr+26;                     //Should move to capital part of the array  
                                        letter = AlphabetArr[posArr];                                        
                                    }                                                                                          
                                    char[] alreadyguessed;                      //Get Guessed alphabet string and convert to Char Array        
                                    String hold = gameDataMap.get("GuessedLetters");
                                    alreadyguessed = hold.toCharArray();                                                                                    
                                    Boolean AlGuessed = false;                 
                                    for(char a : alreadyguessed)                //Determine if already guessed
                                    {                                                                                                                           
                                        if( letter == a)
                                        {AlGuessed = true;}                                            
                                    }
                                    if(AlGuessed == true)                       //if letter has already been guessed
                                    {                                    
                                        MessageOut += "Already guessed"+letter+".\n";
                                    }
                                    else
                                    {  
                                        /** 
                                         *  Letter has not been guessed already.                                                                                          
                                         *  Update The alphabet array
                                         */
                                        int AlphabetSpot = posArr-26; //posArr will be already +26 because everything is capitalised.
                                        alreadyguessed[AlphabetSpot] = letter;//At AlphabetSpot in Game alphabet write letter.
                                        String xstr = String.valueOf( alreadyguessed );        
                                        gameDataMap.put("GuessedLetters", xstr); // Place xstr into GuessedLetters of this gameDataMap
                                        synchronized(LOCK_FOR_MULTI_DATA_MAPS) // Place gameDataMap in multiDataMaps 
                                        {   multiDataMaps.put( playerID, gameDataMap);  }
                                        /** 
                                         * Alphabet array has been updated.
                                         * Determine if letter is in the hangman word.
                                         */
                                        Boolean correct = false;
                                        int increment_PlacewhereLetterIsPresent = 0; 
                                        ArrayList<Integer> placewhereLetterispresent = new ArrayList<Integer>();
                                        for(char character : GuessWordCharArr)          
                                        {                                                                                          
                                            /*make character capital...*/
                                            int i = 0;
                                            for(char e : AlphabetArr)
                                            {
                                                if(e == character && i <= 25)
                                                {
                                                    character = AlphabetArr[ i+26  ];//needed
                                                    break;
                                                }                                                
                                                i++;
                                            }
                                            /*character has now been converted to capital.*/
                                            
                                            /**
                                             * if letter equals character
                                             *      Record the location where character is present in the hangman word.      
                                             */
                                            if(letter == character)
                                            {   
                                                placewhereLetterispresent.add(increment_PlacewhereLetterIsPresent);/*Collect the element positions where letter is present in the hangman word.*/
                                                correct = true; //Declare guessed letter is correct.
                                            }
                                            increment_PlacewhereLetterIsPresent++;
                                        }/*End of for each char in guess word charArr*/                                                                                
                                        
                                        
                                        /** 
                                         *  if letter guess was correct
                                         *      update gamedata and determine
                                         *      if game is done.
                                         */
                                        if( correct == true )
                                        {   
                                            MessageOut += "Correct.\n";
                                            
                                            /**
                                             * Update gamedata.
                                             * Update Mysterword. where the letter is present remove dot and place letter.
                                             */                                            
                                            char[] arrMysteryWordChar;        
                                            arrMysteryWordChar = gameDataMap.get("MysteryWord").toCharArray();                                                   
                                            for(int cx : placewhereLetterispresent ) //At x change arrMysteryWordChar to letter. 
                                            {                                                                                              
                                                arrMysteryWordChar[cx] = letter; /*Change the mystoryword to letter at the placeswhereletterispresent...*/
                                            }
                                            String putbacktogther = String.valueOf( arrMysteryWordChar );                                                                                        
                                            gameDataMap.put("MysteryWord", putbacktogther);      
                                            synchronized(LOCK_FOR_MULTI_DATA_MAPS)/* Place gameDataMap in multiDataMaps */
                                            {   multiDataMaps.put( playerID, gameDataMap);  }                                            
                                            
                                            /**
                                             * Mysteryword has been updated. 
                                             * if the hangman equals mystory word...
                                             *  Than update gameStatus to Done.
                                             */   
                                            synchronized(LOCK_FOR_HANGMAN_GUESS_WORD)
                                            {                                                                                                   
                                                if( hangmanGuessWord.equals(putbacktogther))
                                                {  
                                                    gameDataMap.put("gameStatus","Done");  
                                                    synchronized(LOCK_FOR_MULTI_DATA_MAPS)/* Place gameDataMap in multiDataMaps */
                                                    {   multiDataMaps.put( playerID, gameDataMap);  }
                                                }                                                                                        
                                            }                                            
                                        }
                                        else
                                        {   /* Guessed letter was not correct. */
                                            MessageOut += "Input:\""+line+"\" was inncorect.\n";
                                        }/*End of determine if letter is in hangman word.*/                                        
                                        
                                    }/*End of determine if letter had already been guessed.*/                                    
                                }
                                else
                                {
                                    MessageOut += "Must be a letter.\n";
                                }/*End of if it is actually a letter.*/                                                                
                            }
                            else if ( xlinelength > 1 )
                            {   
                                /**
                                 * client send a message bigger than 1 character
                                 * so Guess for whole hangman word.
                                 */
                                String guess = line;    
                                char[] guessarr = guess.toCharArray();
                                int j= 0;
                                for( char character : guessarr) /*Convert character to a capital.*/
                                {            
                                    int i = 0;
                                    for( char e : AlphabetArr)
                                    {
                                        if(e == character && i <= 25)
                                        {
                                            character = AlphabetArr[ i+26  ];//needed
                                            break;
                                        } i++;                                                                                       
                                    }//character has now been converted to capital.                                                
                                    guessarr[j] = character;
                                    j++;
                                }
                                guess = new String(guessarr);
                                
                                /** 
                                 * Determine if guess is the hangmanguessword.
                                 */
                                synchronized(LOCK_FOR_HANGMAN_GUESS_WORD)
                                {                                    
                                    if( hangmanGuessWord.equals(guess))
                                    {   /* guess is correct. */                                    
                                        String done = "Done";                                                                        
                                        gameDataMap.put("MysteryWord",guess);
                                        gameDataMap.put("gameStatus",done); 
                                        synchronized(LOCK_FOR_MULTI_DATA_MAPS) /* Place gameDataMap in multiDataMaps */
                                        {   multiDataMaps.put( playerID, gameDataMap);  }
                                    }
                                    else
                                    {   /*guess is incorect*/
                                        MessageOut += "Input:\""+line+"\" was inncorect.\n";                                   
                                    }/*End of guess for who hangman word.*/
                                    
                                }/*End of lock for hangman guess word*/
                                
                            }/*End of if it is a one character or more*/
                            
                        }/*End of if hello skip.*/     
                        
                        
                        int CountOfDone;
                        synchronized(LOCK_FOR_COUNT_OF_CLIENTS_DONE)
                        {    CountOfDone = CountOfClientsDone;    }
                        int CountOfClients;                                                
                        synchronized(LOCK_FOR_CLIENT_COUNT)
                        {    CountOfClients = countofCurrentClients;    }
                        
                        /**
                         * If game is done or not done...
                         */
                        String isDone = gameDataMap.get("gameStatus");                        
                        if(isDone.equals("Done") && CountOfDone == 0)
                        {   
                            /** 
                             * This client completed and is the first to complete. 
                             *   Send game complete and wait message to client...
                             */                                                          
                            MessageOut += "Congratulations. You are the first to complete hangman. \n Please wait for other players to complete...\n";              
                            out.writeUTF(MessageOut);
                            System.out.println("The first client to finish is done.\n");                        
                            /*Begin wait...*/
                            SomeOneCompleted( CountOfDone );
                            MessageOut = ProducedGameData(playerID);
                            System.out.println("End waiting for first client\n");                                                        
                        }
                        else if( isDone.equals("Done") && CountOfDone > 0 )
                        {
                            /** 
                             * This client completed but is not the first to complete.
                             * Send game complete and wait message to client...
                             */                            
                            MessageOut += "Congratulations. You have completed hangman.  \n Please wait for other players to complete...\n";                                                                                        
                            out.writeUTF(MessageOut);
                            System.out.println("Another client is waiting. \n");
                            /*Begin waiting.*/
                            SomeOneCompleted( CountOfDone );
                            System.out.println(" End waiting for other client. \n");
                            MessageOut = ProducedGameData(playerID);                        
                        }                       
                        else 
                        {   
                            /** 
                             * Player isnt done. 
                             * Set MessageOut to an appropriate next turn message.
                             */                                                          
                            String mysWord = ""; String alpha = "";                        
                            mysWord =  gameDataMap.get("MysteryWord");
                            alpha = gameDataMap.get("GuessedLetters");                        
                            MessageOut += "Your Game: "+mysWord+" \nUsed Letters:"+alpha+"\n Your turn:";                                                          
                            
                        }/*End of if game is done or not.*/                        
                        
                        out.writeUTF(MessageOut);//MessageOut has been buid send it to client. 
                        
                    }/*End of if exit*/
                    
                }/*End of if input is available*/      
                
                /** 
                 * Place the status of everyone is done into EXITdoLOOP 
                 * because if everyone is done than close up connections.
                 */
                synchronized(LOCK_FOR_EVERYONE_IS_DONE)
                { EXITdoLOOP = everyoneisDone; }
                
                /*If client sent exit, than exit do loop and close connections.*/
                if(line.equals("exit"))
                {   EXITdoLOOP = true;  }
                        
            }while (EXITdoLOOP == false);
            /**
             * End of Do while not exit.
             */  



  
            /** Close connections...
             *  Turn data streams off and close the server socket. 
             *  Reduce count of clients by one.
             *  if count of clients equal to zero 
             *      Close the socket and notifiy main.
             */
            out.close(); System.out.println("Close out ");                      
            in.close();System.out.println("Close in");
            server.close(); System.out.println("Close Server");                        
            Boolean ENDS;
            synchronized(LOCK_FOR_END_SERVER)
            {   ENDS = EndServer; }            
            synchronized(LOCK_FOR_CLIENT_COUNT)                
            {   
                countofCurrentClients--;   
                if(countofCurrentClients == 0)
                {   ENDS = true;    }                    
            }
            if ( ENDS == true )
            synchronized(LOCK_FOR_MY_SOCKET)
            {   System.out.println("Close Socket."); mySocket.close();  }
            synchronized(LOCK_FOR_END_SERVER)
            {   EndServer = ENDS; }
            /**
             *  End of logic for ending client's server or entire server.
             *  Client should be disconnected and if all clients are gone,
             *  close up the entire server and exit main.
             */
            
            
            
        /** Catch for SocketTimeout and IOException */    
        }catch (SocketTimeoutException s) 
        {   System.err.println("Socket timed out!"); s.printStackTrace();    } 
        catch (IOException e) 
        {    System.err.println("IOException! "); e.printStackTrace();       }
    
    }/* End of Method run */

    







    
    /** Method setUpGameMap()
     *      Purpose: Sets up the game map for a client. Each client gets a their
     *               own hashtable that stores the information for their game.
     *               This hashtable gets stored in the concurrent hashtable 
     *               which holds all of the clients game data.
     *            
     */
    private Hashtable<String, String>  setUpGameMap(int playerID, char[] GuessWordCharArr )
    {                
        Hashtable <String, String> gameDataMap = new Hashtable<String, String>();          
        gameDataMap.put("PlayerID", ""+playerID+"" );
        gameDataMap.put("GuessedLetters", "__________________________");        //Alphabet. Letters client has used from the alphabet            
        gameDataMap.put("MysteryWord", "");                                     //MysteryWord. The hangman word with letters to be filled in.
        gameDataMap.put("Score", "0");                                          //Score. Number of guess this client has made.
        gameDataMap.put("gameStatus", "NotDone");                               //gameStatus. Done or NotDone.            
        int wordLength;
        synchronized(LOCK_FOR_HANGMAN_GUESS_WORD)                               /*Begin to Set up Mystory word as a string of dots the size of hangmanGuessword.*/
        {   wordLength = hangmanGuessWord.length(); }                           /*End of Lock4 HangmanGuessWord */
        String dots = "";                                                       //Start a string of dots.
        for(int i=0; i <wordLength; i++ )                                       //Make the string of dots the same length as the hangman word.
        {   dots += ".";    }                                               
        gameDataMap.put("MysteryWord", dots);                                   //Place doted string in Mysteryword.            
        for( int i = 0; i < wordLength; i++       )                         
        {   Boolean itis = false;                                               //In Mysteryword, where there is a character that isnt alphabetical,                
            char k = GuessWordCharArr[i];                                       //replace the dot with the character so guessess can stay alphabetical.
            for(char a : AlphabetArr)                                       
            {                                                               
                if( a == k )                                                
                { itis = true; }                                            
            }                                                               
            if(itis == false)                                               
            {    
               String mystor = gameDataMap.get("MysteryWord");                  //Replace the character in Mysterword to this character.
               char[] mysArr = mystor.toCharArray();
               mysArr[i] = k;                                               
               String mysStr = String.valueOf(mysArr);                      
               gameDataMap.put("MysteryWord", mysStr);                      
            }                                                               
        }                                                                       //MystoryWord now has special characters.                                                                                                                                                                          
        synchronized(LOCK_FOR_MULTI_DATA_MAPS)                                  /* Place gameDataMap in multiDataMaps */
        {   multiDataMaps.put( playerID, gameDataMap);  }
        return gameDataMap;
                
    }/* End of Method setUpGameMap */
    
    
    
    
    
    /** Method ProduceGameData()
     *      Purpose: Creates a string that is the ending message for a game.
     *               Shows the player who won and the score of each player.
     *               This method gets called for each client so the message is 
     *               specific for the client.
     */
    private String ProducedGameData(int callingplayerID)
    {        
        int countofclients = 0;
        synchronized(LOCK_FOR_CLIENT_COUNT)
        {countofclients = countofCurrentClients+100;}
        
        String FinishStats = "";    
        FinishStats = "PlayerID     Number of Guesses     \n";
        int lowestScore = -1;
        int lowestID = -1;
        for(int theplayerIDs = 101; theplayerIDs <= countofclients; theplayerIDs++ )
        {
            Hashtable <String, String> gameDataMap;
            synchronized(LOCK_FOR_MULTI_DATA_MAPS)                                  /* Place gameDataMap in multiDataMaps */
            {  
                gameDataMap = multiDataMaps.get( theplayerIDs );                  
            }
            String NumberOfGuessess = gameDataMap.get("Score");
            String PlayerID = gameDataMap.get("PlayerID");                
            String name = getName();
            FinishStats += "    "+PlayerID+"            "+NumberOfGuessess+"     \n";  
            int pid  = Integer.parseInt(PlayerID);
            int tempscore = Integer.parseInt(NumberOfGuessess);
           
            if ( lowestScore == -1)
            {
                lowestScore = tempscore;
                lowestID = pid;
            }            
            else if( tempscore <  lowestScore)
            {
                lowestScore = tempscore;
                lowestID = pid;
            }
            else if (tempscore == lowestScore)
            {    lowestID = -1;     }                                       
            
        }/* End of for loop */
        
        if( lowestID == callingplayerID )
        {
            FinishStats += "\n You Are the Winner!";  
        }
        else
        {
            FinishStats += "\n You are not first.";  
        }
        String plid = String.valueOf(callingplayerID);
        FinishStats += "\n Your player ID: "+plid+"\n";
                                             
        return FinishStats;
    }/* End of method ProduceGameData */
    
    
    
    
    
    
    
    
    
    
    /** Private Method SomeOneCompleted()
     *      Purpose: When someone completes their hangman game.
     *               Tell everyone a client has completed and make this
     *               client wait till everyone is done.
     */
    private void SomeOneCompleted(int CountOfDone)
    {                      
        int CountOfClients;
        synchronized(LOCK_FOR_CLIENT_COUNT)
        {    CountOfClients = countofCurrentClients;    }
        
        synchronized(LOCK_FOR_CLIENT_COUNT)
        {
            CountOfClientsDone++;//write to static counter needed lock...   
            CountOfDone = CountOfClientsDone;
        }
        
        System.out.println("   Count of done:"+CountOfDone+"  count of clients:"+CountOfClients+"\n");
        if( CountOfDone == CountOfClients )
        {    alltheplayersaredone();    }
        else
        {
            synchronized(LOCK_FOR_EVERYONE_IS_DONE)
            {
                try 
                {
                    System.out.println("Thread:\""+getName()+"\" is waiting for other clients to complete");
                    LOCK_FOR_EVERYONE_IS_DONE.wait();
                    System.out.println("Thread:\""+getName()+"\" is done waiting.");
                }
                catch (InterruptedException e) 
                {   e.printStackTrace();    }
            }
        }
    }/* End of method SomeOneCompleted() */
    
    
    
    
    
    
    
    
    
    
    /** Private Method alltheplayersaredone
     *   Purpose: Tell all threads waiting that all the clients are done playing
     *            the game.
     *   
     */
    private void alltheplayersaredone() {
        System.out.println("Notify all called.");
        synchronized(LOCK_FOR_EVERYONE_IS_DONE) {            
            everyoneisDone = true;
            LOCK_FOR_EVERYONE_IS_DONE.notifyAll();        
        }                
    }/* End of method alltheplayersaredone */
    
    
}/*End of Skeleton Server*/