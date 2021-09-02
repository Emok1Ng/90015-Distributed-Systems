package com.assignment1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.InputStreamReader;

public class Client {

  public static void main(String[] args)throws IOException {
    client_connection();
  }
  static void client_connection() throws IOException {
    String remoteHostname = "127.0.0.1";
    int remotePort = 6379;
    Socket socket = new Socket(remoteHostname, remotePort);
    PrintWriter writer = new PrintWriter(socket.getOutputStream(),true);
    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
    InputParser ip = new InputParser();
    while (true) {
      String message = keyboard.readLine();
      String toSend = ip.toJSON(message);
      if(toSend != null){
        writer.println(toSend);
      }
      else{
        System.out.println("[ERROR]Unable to send message due to Invalid Command or Lack of arguments.");
        continue;
      }
      if (message.equals("quit")) break;
      String response = reader.readLine();
      System.out.format("[Server]> %s\n", response);
    }
    socket.close();
  }
}
