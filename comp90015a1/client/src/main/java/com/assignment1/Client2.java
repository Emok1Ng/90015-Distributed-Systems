package com.assignment1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client2 {

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
        while (true) {
            String message = keyboard.readLine();
            writer.println(message);
            if (message.equals("quit")) break;
            String response = reader.readLine();
            System.out.format("[Server]> %s\n", response);
        }
        socket.close();
    }
}



