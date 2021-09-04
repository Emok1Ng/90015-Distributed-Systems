package com.assignment1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.assignment1.base.Enum.Command;
import com.assignment1.base.Enum.MessageType;
import com.assignment1.base.Message.S2C.NewIdentity;
import com.assignment1.base.Message.S2C.RoomChange;


import com.assignment1.base.Message.S2C.RoomContents;
import com.assignment1.base.Message.S2C.RoomList;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
        ChatRoom hall = new ChatRoom("MainHall", null);
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
        RoomChange rc = new RoomChange();
        rc.setType(MessageType.ROOMCHANGE.getType());
        rc.setIdentity(g.getIdentity());
        rc.setFormer(g.getCurrentRoom());


        BroadcastInfo info = new BroadcastInfo();
        if(g.getCurrentRoom().equals(roomid) || !this.roomHashMap.containsKey(roomid)){

            rc.setRoomid(g.getCurrentRoom());

            info.addConnection(this.connectionHashMap.get(g));
        }
        else{

            this.roomHashMap.get(g.getCurrentRoom()).deleteMember(g);
            this.roomHashMap.get(roomid).addMember(g);
            g.setCurrentRoom(roomid);
            rc.setRoomid(roomid);
            ArrayList<Guest> guestsToSend = this.roomHashMap.get(roomid).getMembers();
            for(int i=0;i<guestsToSend.size();i++){
                info.addConnection(this.connectionHashMap.get(guestsToSend.get(i)));
            }
        }
        info.setContent(JSON.toJSONString(rc));
        return info;
    }

    private synchronized ArrayList<HashMap> getRooms(){
        ArrayList<HashMap> rooms = new ArrayList<>();
        for(int i=0;i<this.roomList.size();i++){
            HashMap each = new HashMap();
            each.put("roomid", this.roomList.get(i).getRoomid());
            each.put("count", this.roomList.get(i).getMembers().size());
            rooms.add(each);
        }
        return rooms;
    }

    private synchronized BroadcastInfo List(Guest g){
        RoomList rl = new RoomList();
        rl.setType(MessageType.ROOMLIST.getType());
        BroadcastInfo info = new BroadcastInfo();
        info.addConnection(this.connectionHashMap.get(g));
        ArrayList<HashMap> rooms = this.getRooms();
        rl.setRooms(rooms);
        info.setContent(JSON.toJSONString(rl));
        return info;
    }

    private synchronized BroadcastInfo CreateRoom(String roomid, Guest g){
        String pattern = "^[a-zA-Z]{1}[a-zA-Z0-9]{2,31}";
        RoomList rl = new RoomList();
        rl.setType(MessageType.ROOMLIST.getType());
        BroadcastInfo info = new BroadcastInfo();
        ArrayList<HashMap> rooms = this.getRooms();
        if(Pattern.matches(pattern, roomid) && !this.roomHashMap.containsKey(roomid)){
            ChatRoom newRoom = new ChatRoom(roomid, g);
            this.roomList.add(newRoom);
            this.roomHashMap.put(roomid, newRoom);
            g.addOwnership(roomid);
            HashMap add = new HashMap();
            add.put("roomid", roomid);
            add.put("count", 0);
            rooms.add(add);
        }
        rl.setRooms(rooms);
        info.setContent(JSON.toJSONString(rl));
        info.addConnection(this.connectionHashMap.get(g));
        return info;
    }

    private synchronized BroadcastInfo DeleteRoom(String roomid, Guest g){
        BroadcastInfo info = new BroadcastInfo();
        RoomList rl = new RoomList();

        rl.setType(MessageType.ROOMLIST.getType());
        ArrayList<HashMap> rooms = this.getRooms();

        if(this.roomHashMap.containsKey(roomid)&&this.roomHashMap.get(roomid).getOwner().equals(g)){
            this.roomHashMap.remove(roomid);
            Iterator iter = this.roomList.iterator();
            while(iter.hasNext()){
                String roomId = (String) iter.next();
                if(roomId.equals(roomid)){
                    iter.remove();
                }
            }
            for(int i=0; i < rooms.size();i++){
                if( rooms.get(i).containsKey(roomid)){
                    rooms.remove(i);
                }
            }
        }
        rl.setRooms(rooms);
        info.setContent(JSON.toJSONString(rl));
        info.addConnection(this.connectionHashMap.get(g));
        return info;
    }

    private synchronized BroadcastInfo Who(String roomid, Guest g){
        BroadcastInfo info = new BroadcastInfo();
        RoomContents rc = new RoomContents();

        rc.setType(MessageType.ROOMCONTENTS.getType());
        rc.setRoomid(roomid);

        ArrayList<Guest> guestsInRoom = this.roomHashMap.get(g.getCurrentRoom()).getMembers();
        ArrayList<String> guestsIdentity = new ArrayList<>();
        for(int i =0; i < guestsInRoom.size();i++){
            guestsIdentity.add(guestsInRoom.get(i).getIdentity());
        }
        rc.setIdentities(guestsIdentity);
        rc.setOwner(this.roomHashMap.get(roomid).getOwner().getIdentity());
        info.setContent(JSON.toJSONString(rc));
        info.addConnection(this.connectionHashMap.get(g));
        return info;
    }

    private synchronized BroadcastInfo Quit(Guest g){
        BroadcastInfo info = new BroadcastInfo();
        RoomChange rc = new RoomChange();
        rc.setType(MessageType.ROOMCHANGE.getType());
        ArrayList<Guest> guestsToSend = this.roomHashMap.get(g.getCurrentRoom()).getMembers();
        //Implement DeleteRoom
        for(String roomid: this.roomHashMap.keySet()) {
            if (g.equals(this.roomHashMap.get(roomid).getOwner())) {
                DeleteRoom(roomid,g);
            }
        }
        this.identityList.remove(g.getIdentity());
        this.connectionHashMap.remove(g);
        info.setContent(JSON.toJSONString(rc));
        for(int i =0; i<guestsToSend.size(); i++){
            info.addConnection(this.connectionHashMap.get(guestsToSend.get(i)));
        }
        return info;
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
