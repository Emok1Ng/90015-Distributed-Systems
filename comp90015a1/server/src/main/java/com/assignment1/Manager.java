package com.assignment1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.assignment1.base.Enum.Command;
import com.assignment1.base.Enum.MessageType;
import com.assignment1.base.Message.C2S.IdentityChange;
import com.assignment1.base.Message.S2C.NewIdentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Manager {

    private ArrayList<ChatRoom> roomList;
    private ArrayList<String> identityList;
    private HashMap<Server.ChatConnection, Guest> guestHashMap;
    private HashMap<Guest, Server.ChatConnection> connectionHashMap;
    private HashMap<String, ChatRoom> roomHashMap;
    private Integer count;

    public Manager() {
        this.roomList = new ArrayList<>();
        ChatRoom hall = new ChatRoom("MainHall");
        this.roomList.add(hall);
        this.identityList = new ArrayList<>();
        this.guestHashMap = new HashMap<>();
        this.connectionHashMap = new HashMap<>();
        this.roomHashMap = new HashMap<>();
        this.roomHashMap.put("MainHall", hall);
        this.count = 0;
    }

    public BroadcastInfo Analyze(String s, Server.ChatConnection connection){
        BroadcastInfo info;
        if(!this.guestHashMap.containsKey(connection)){
            Guest g = new Guest();
            info = NewIdentity(g, connection);
        }
        else{
            JSONObject json = JSON.parseObject(s);
            String command = json.get("type").toString();
            Guest g = this.guestHashMap.get(connection);
            if(command.equals(Command.IDENTITYCHANGE.getCommand())){
                String identity = json.get("identity").toString();
                info = this.IdentityChange(identity, g);
            }
            else if(command.equals(Command.JOIN.getCommand())){
                String roomid = json.get("roomid").toString();
                info = this.Join(roomid, g);
            }
            else if(command.equals(Command.LIST.getCommand())){
                info = this.List(g);
            }
            else if(command.equals(Command.CREATEROOM.getCommand())){
                String roomid = json.get("roomid").toString();
                info = this.CreateRoom(roomid, g);
            }
            else if(command.equals(Command.DELETEROOM.getCommand())){
                String roomid = json.get("roomid").toString();
                info = this.DeleteRoom(roomid, g);
            }
            else if(command.equals(Command.WHO.getCommand())){
                String roomid = json.get("roomid").toString();
                info = this.Who(roomid, g);
            }
            else if(command.equals(Command.QUIT.getCommand())){
                info = this.Quit(g);
            }
            else{
                info = null;
            }
        }
        return info;
    }

    private synchronized BroadcastInfo NewIdentity(Guest g, Server.ChatConnection connection){
        g.setIdentity("guest" + this.count.toString());
        this.count += 1;
        g.setCurrentRoom("MainHall");
        this.roomHashMap.get("MainHall").addMember(g);
        this.connectionHashMap.put(g, connection);
        this.guestHashMap.put(connection, g);
        this.identityList.add(g.getIdentity());
        NewIdentity ni = new NewIdentity();
        ni.setType(MessageType.NEWIDENTITY.getType());
        ni.setFormer("");
        ni.setIdentity(g.getIdentity());
        BroadcastInfo info = new BroadcastInfo();
        info.setContent(JSON.toJSONString(ni));
        info.addConnection(connection);
        return info;
    }

    private synchronized BroadcastInfo IdentityChange(String identity, Guest g){
        String pattern = "[a-zA-Z0-9]{3,16}";
        String defaultPattern = "guest[0-9]{0,11}";
        NewIdentity ni = new NewIdentity();
        ni.setType(MessageType.NEWIDENTITY.getType());
        ni.setFormer(g.getIdentity());
        if(Pattern.matches(pattern, identity) &&
                !Pattern.matches(defaultPattern, identity) &&
                !this.identityList.contains(identity)){
            g.setIdentity(identity);
            ni.setIdentity(identity);
        }
        else{
            ni.setIdentity(g.getIdentity());
        }
        BroadcastInfo info = new BroadcastInfo();
        info.setContent(JSON.toJSONString(ni));
        ArrayList<Guest> guestsToSend = this.roomHashMap.get(g.getCurrentRoom()).getMembers();
        for(int i=0;i<guestsToSend.size();i++){
            info.addConnection(this.connectionHashMap.get(guestsToSend.get(i)));
        }
        return info;
    }

    private synchronized BroadcastInfo Join(String roomid, Guest g){
        return null;
    }

    private synchronized BroadcastInfo List(Guest g){
        return null;
    }

    private synchronized BroadcastInfo CreateRoom(String roomid, Guest g){
        return null;
    }

    private synchronized BroadcastInfo DeleteRoom(String roomid, Guest g){
        return null;
    }

    private synchronized BroadcastInfo Who(String roomid, Guest g){
        return null;
    }

    private synchronized BroadcastInfo Quit(Guest g){
        return null;
    }

    class BroadcastInfo{

        private String content;
        private ArrayList<Server.ChatConnection> connections;

        public BroadcastInfo(){
            this.connections = new ArrayList<>();
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public ArrayList<Server.ChatConnection> getConnections() {
            return connections;
        }

        public void addConnection(Server.ChatConnection connect) {
            this.connections.add(connect);
        }

        public void deleteConnection(Server.ChatConnection connect){
            this.connections.remove(connect);
        }
    }
}
