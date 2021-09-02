package com.assignment1;

import com.assignment1.base.Message.Identity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

  public static final int port = 6379;
  private boolean alive;
  private List<ChatConnection> connectionList = new ArrayList<>(); // store client connections
  private Manager manager = new Manager();

  public static void main(String[] args) {
    Server server_chat = new Server();

    server_chat.handle();


  }

  private void enter(ChatConnection connection) {
    broadCast((String.format("New guest  has joined the main hall\n")), connection);
    connectionList.add(connection);

  }

  private void leave(ChatConnection connection) {
    //
    broadCast((String.format("Someone has left the main hall\n")), connection);
    connectionList.remove(connection);

  }

  private synchronized void broadCast(String message, ChatConnection ignored) {
    //
    System.out.printf(message);
    for(ChatConnection c: connectionList){
      if(ignored == null || ignored.equals(c))
        c.sendMessage(message);

    }
  }

  public void handle() {
    ServerSocket serverSocket;
    //Creating a thread Pool which contains 5 threads
    ExecutorService threadpool = Executors.newFixedThreadPool(2);
    try {
      System.out.println("[Server] Waiting for connection......");
      serverSocket = new ServerSocket((port));
      System.out.printf("[Server] Listening on port %d\n", port);
      alive = true;
      while(alive){
        Socket socket = serverSocket.accept();
        //System.out.println("[Server] Connected to client ");
        ChatConnection connection_thread = new ChatConnection((socket));

        enter(connection_thread);

        threadpool.execute(connection_thread);



      }
    }catch(IOException e){
      e.printStackTrace();
    }

  }

  class ChatConnection implements Runnable {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean connection_alive = false;

    public ChatConnection(Socket socket) throws IOException {
      this.socket = socket;
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.writer = new PrintWriter(socket.getOutputStream(), true);

    }
    @Override
    public void run() {
      //manage the connection
      connection_alive = true;
      Identity clientIdentity = new Identity();

      while (connection_alive) {
        //
        try{
          String input  = reader.readLine();
          //someone type message broadcast to everyone in the chat
          clientIdentity.setIdentity("Guest id");
          if(input != null){
            broadCast(String.format("[Main hall] %s> %s\n", clientIdentity.getIdentity(),input), this);
          }else{
            connection_alive = false;
          }

        }catch (IOException e){
          e.printStackTrace();
          connection_alive  = false;
        }
      }
      close();
    }

    public void close() {
      try {
        leave(this);
        reader.close();
        writer.close();
        socket.close();
      }catch (IOException e){
        e.printStackTrace();
      }

    }

    public void sendMessage(String message) {
      //
      writer.print(message);
      writer.flush();
    }
  }
}
