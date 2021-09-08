package com.assignment1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client2 {

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
                    System.out.println(toSend);
                    if (toSend != null) {
                        writer.println(toSend);
                    } else {
                        System.out.println("[ERROR]Unable to send message due to Invalid Command or Lack of arguments.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Receiver implements Runnable{
        private BufferedReader reader;
        public Receiver(Socket socket) throws IOException {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        public void run() {
            while(true){
                try {
                    if(reader.readLine()!=null){
                        String response = reader.readLine();
                        System.out.format("[Server]> %s\n", response);
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
