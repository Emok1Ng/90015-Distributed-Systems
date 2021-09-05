package com.assignment1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

  public static final int port = 6379;
  private boolean alive;
  private final Manager manager = new Manager();

  public static void main(String[] args) {
    Server server_chat = new Server();
    server_chat.handle();
  }

  private synchronized void broadCast(Manager.BroadcastInfo info) {
    String content = info.getContent();
    System.out.printf("[Server] %s\n", content);
    for(int i=0;i<info.getConnections().size();i++){
      System.out.println(info.getConnections().get(i));
      info.getConnections().get(i).sendMessage(content);
    }
  }

  public void handle() {
    ServerSocket serverSocket;
    ExecutorService threadpool = Executors.newFixedThreadPool(5);
    try {
      System.out.println("[Server] Waiting for connection......");
      serverSocket = new ServerSocket((port));
      System.out.printf("[Server] Listening on port %d\n", port);
      alive = true;
      while(alive){
        Socket socket = serverSocket.accept();
        ChatConnection connection = new ChatConnection(socket);
        threadpool.execute(connection);
      }
    }
    catch(IOException e){
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
      connection_alive = true;
      broadCast(manager.Analyze("",this));
      broadCast(manager.Analyze("{\"type\":\"join\",\"roomid\":\"MainHall\"}",this));
      while (connection_alive) {
        try{
          String input  = reader.readLine();
          if(input != null){
            System.out.printf("[Client] :%s",input);
            System.out.println(this);
            broadCast(manager.Analyze(input,this));
          }
          else{
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
        reader.close();
        writer.close();
        socket.close();
      }
      catch (IOException e){
        e.printStackTrace();
      }
    }

    public void sendMessage(String message) {
      writer.print(message+"\n");
      writer.flush();
    }
  }
}
