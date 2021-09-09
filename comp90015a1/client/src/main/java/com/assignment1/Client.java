package com.assignment1;

import com.alibaba.fastjson.JSONObject;
import com.assignment1.base.Enum.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

  public volatile int status;
  public volatile boolean senderAlive;
  public volatile boolean receiverAlive;
  public volatile String roomid;
  //1:Creating 2:Deleting 0:others
  public Client() {
    status = 0;
    senderAlive = false;
    receiverAlive = false;
  }

  public static void main(String[] args)throws IOException {
    Client client = new Client();
    client.handle();
  }

  public void handle() throws IOException{
    String remoteHostname = "127.0.0.1";
    int remotePort = 6379;
    Socket socket = new Socket(remoteHostname, remotePort);
    ExecutorService threadpool = Executors.newFixedThreadPool(2);
    Sender sender = new Sender(socket);
    Receiver receiver = new Receiver(socket);
    threadpool.execute(sender);
    threadpool.execute(receiver);
    threadpool.shutdown();
  }

  class Sender implements Runnable{
    private PrintWriter writer;
    private BufferedReader keyboard;
    private OutputParser outputParser;

    public Sender(Socket socket) throws IOException {
      this.writer = new PrintWriter(socket.getOutputStream(), true);
      this.keyboard = new BufferedReader(new InputStreamReader(System.in));
      this.outputParser = new OutputParser();
    }
    @Override
    public void run() {
      senderAlive = true;
      while (senderAlive) {
        try {
          String message = keyboard.readLine();
          String toSend = outputParser.toJSON(message);
          //System.out.println(toSend);
          if (toSend != null) {
            if(JSONObject.parseObject(toSend).get("type").equals(Command.CREATEROOM.getCommand())){
              status = 1;
              roomid = JSONObject.parseObject(toSend).get("roomid").toString();
            }
            else if(JSONObject.parseObject(toSend).get("type").equals(Command.DELETEROOM.getCommand())){
              status = 2;
              roomid = JSONObject.parseObject(toSend).get("roomid").toString();
            }
            else if(JSONObject.parseObject(toSend).get("type").equals(Command.QUIT.getCommand())){
              senderAlive = false;
            }
            writer.println(toSend);
          } else {
            System.out.println("[ERROR]Unable to send message due to Invalid command/Lack of arguments/Invalid identity(names begin with 'guest' followed by numbers are preserved) or roomid");
          }
        } catch (IOException e) {
          senderAlive = false;
          e.printStackTrace();
        }
      }
    }
  }

  class Receiver implements Runnable{
    private BufferedReader reader;
    private InputParser inputParser;
    public Receiver(Socket socket) throws IOException {
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.inputParser = new InputParser();
    }
    public void run() {
      receiverAlive = true;
      while(receiverAlive){
        try {
          String response = reader.readLine();
          receiverAlive = inputParser.print(response, status, roomid);
          roomid = "";
          status = 0;
        }
        catch (IOException e){
          receiverAlive = false;
          e.printStackTrace();
        }
      }
    }
  }
}
