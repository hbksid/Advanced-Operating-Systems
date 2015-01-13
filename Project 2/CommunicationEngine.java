package Projects;

//package Project1;

import java.io.*;
import java.util.*;
import java.net.*;

/**
 * Handles communication to/from other processes
 * @author Rusty
 */
public class CommunicationEngine {
    // Store map of ports to ServerSockets
    private final Map<Integer, ServerSocket> serverSockets = new HashMap<>();
    private final AOSProtocol protocol;
    
    public CommunicationEngine(AOSProtocol protocol) {
        this.protocol = protocol;
    }
    
    // Method to open a server socket with threads to listen to multiple 
    // clients and specifying the port
    public void openServer(final int port) {
        if (!serverSockets.containsKey(port)) {
            try {
                final ServerSocket serverSocket = new ServerSocket(port);
                serverSockets.put(port, serverSocket);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        System.out.println("Server started.");
                        // Enter while loop accepting new connections
                        while (true) {
                            try {
                                final Socket socket = serverSocket.accept();
                                
                                // Creat object streams
                                final ObjectInputStream inputStream = 
                                        new ObjectInputStream(
                                        socket.getInputStream());
                                final ObjectOutputStream outputStream = 
                                        new ObjectOutputStream(
                                        socket.getOutputStream());
                                
                                // Retrieve the input object
                                final Object input = inputStream.readObject();
                                
                                // If it is a KillServerMessage, then exit loop
                                if (input instanceof KillServerMessage) {
//                                    System.out.println("Server on port " + port 
//                                            + " stopping.");
                                    serverSockets.remove(port);
                                    break;
                                }
                                
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
//                                            System.out.println("Server "
//                                                    + "received a request.");
                                            Object output = 
                                                    protocol.serverReceive(
                                                    socket.getInetAddress(),
                                                    input);

                                            // If object returned, then send it
                                            if (output != null) {
                                                outputStream.
                                                        writeObject(output);
                                            }
                                        } catch (IOException ex) {
                                            System.err.println(" Client " + 
                                                    ex);
                                            System.exit(0);
                                        }
                                    }
                                }).start();
                            } catch (IOException | ClassNotFoundException ex) {
                                System.err.println(" Server " + ex);
                                System.exit(0);
                            }
                        }
                    }
                }).start();
            } catch (IOException ex) {
                System.err.println(ex);
                System.exit(0);
            }
        }
    }
    
    // Method for sending client messages to other process servers
    public Object clientSend(String host, int port, Object message, 
            boolean noReply) {
        try {
            Socket socket = new Socket(host, port);
//            System.out.println("CommEngine sending to host " + host + " port " 
//                    + port);
            new ObjectOutputStream(socket.getOutputStream())
                    .writeObject(message);
            return noReply ? null : 
                    new ObjectInputStream(socket.getInputStream())
                    .readObject();
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println(" Client " + ex);
            System.exit(0);
        }
        
        return null;
    }
    
    // Method for protocol to call to kill server for a specific port
    public void killServer(int port) {
        if (serverSockets.containsKey(port)) {
            clientSend("localhost", port, new KillServerMessage(), true);
        }
    }
}
