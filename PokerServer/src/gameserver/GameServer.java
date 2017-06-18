/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

/**
 *
 * @author rajith
 */
public class GameServer {
    
    private static final int PORT = 9001;

    // User Names 
    private static HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
    */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
  
       /**
     * The appplication main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }



private static class Handler extends Thread {
      
        private String name;
        private Socket socket;
        private BufferedReader inputStream;
        private PrintWriter outStream;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }
        
        public void run () {
        
        try {
            
           // Create character streams for the socket.
          inputStream = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
         outStream = new PrintWriter(socket.getOutputStream());
                
          // Request a name from the Player  Keep requesting until
          // a name is submitted that is not already used.  Note that
          // checking for the existence of a name and adding the name
          // must be done while locking the set of names.       
          
          
            while (true) {                
                outStream.println("UserName");
                name = inputStream.readLine();
                if (name == null) {
                    return;
                }
                synchronized (names) {
                    if (!names.contains(name)) {
                        names.add(name);
                        break;
                    }
                }
            }
            
                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                outStream.println("NAMEACCEPTED");
                writers.add(outStream);
                
                   // Do pass values here
                 while (true) {
                    String input = inputStream.readLine();
                    if (input == null) {
                        return;
                    }
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                }
                   
                   
        
        } catch (Exception e) {
          System.out.println("Exception +"+ e);
          } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                }
                if (outStream != null) {
                    writers.remove(outStream);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            } 
        }
    }
}

