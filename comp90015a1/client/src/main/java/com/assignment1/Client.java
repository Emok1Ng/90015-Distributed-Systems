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
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

  public AtomicInteger status;
  //1:Creating 2:Deleting 0:others
  public Client() {
    status.set(0);
  }

  public static void main(String[] args)throws IOException {
    Client client = new Client();
    client.handle();
  }

  public void handle() throws IOException{
    String remoteHostname = "127.0.0.1";
    int remotePort = 6379;
    Socket socket = new Socket(remoteHostname, remotePort);
    ExecutorService threadpool = Executors.newFixedThreadPool(5);
    Sender sender = new Sender(socket);
    Receiver receiver = new Receiver(socket);
    threadpool.execute(sender);
    threadpool.execute(receiver);
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
      while (true) {
        try {
          String message = keyboard.readLine();
          String toSend = outputParser.toJSON(message);
          //System.out.println(toSend);
          if (toSend != null) {
            if(JSONObject.parseObject(toSend).get("type").equals(Command.CREATEROOM.getCommand())){
              status.set(1);
            }
            else if(JSONObject.parseObject(toSend).get("type").equals(Command.DELETEROOM.getCommand())){
              status.set(2);
            }
            writer.println(toSend);
          } else {
            System.out.println("[ERROR]Unable to send message due to Invalid command/Lack of arguments/Invalid identity(names begin with 'guest' followed by numbers are preserved) or roomid");
          }
        } catch (IOException e) {
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
      while(true){
        try {
          String response = reader.readLine();
          inputParser.print(response, status.get());
          status.set(0);
        }
        catch (IOException e){
          e.printStackTrace();
        }
      }
    }
  }
}
