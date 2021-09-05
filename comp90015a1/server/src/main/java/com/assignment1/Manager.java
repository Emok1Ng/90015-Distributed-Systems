package com.assignment1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.assignment1.base.Enum.Command;
import com.assignment1.base.Enum.MessageType;
import com.assignment1.base.Message.S2C.*;


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
                Message message = new Message();
                message.setIdentity(this.guestHashMap.get(connection).getIdentity());
                message.setContent(s);
                message.setType(MessageType.MESSAGE.getType());
                info = new BroadcastInfo();
                info.setContent(s);
                ChatRoom room = this.roomHashMap.get(this.guestHashMap.get(connection).getCurrentRoom());
                for(int i=0;i<room.getMembers().size();i++){
                    info.addConnection(this.connectionHashMap.get(room.getMembers().get(i)));
                }
            }
        }
        return info;
    }

    private synchronized BroadcastInfo NewIdentity(Guest g, Server.ChatConnection connection){
        g.setIdentity("guest" + this.count.toString());
        this.count += 1;
        g.setCurrentRoom("");
        this.connectionHashMap.put(g, connection);
        this.guestHashMap.put(connection, g);
        this.identityList.add(g.getIdentity());
        NewIdentity ni = new NewIdentity();
        ni.setType(MessageType.NEWIDENTITY.getType());
        ni.setFormer("");
        ni.setIdentity(g.getIdentity());
        BroadcastInfo info = new BroadcastInfo();
        info.setContent(JSON.toJSONString(ni));
        for(Server.ChatConnection c:this.guestHashMap.keySet()){
            info.addConnection(c);
        }
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
            if(g.getCurrentRoom()!=""){
                this.roomHashMap.get(g.getCurrentRoom()).deleteMember(g);
            }
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
        RoomList rl = new RoomList();
        rl.setType(MessageType.ROOMLIST.getType());
        BroadcastInfo info = new BroadcastInfo();
        if(this.roomHashMap.containsKey(roomid) && this.roomHashMap.get(roomid).getOwner().equals(g)){
            ChatRoom room = this.roomHashMap.get(roomid);
            for(int i=0;i<room.getMembers().size();i++){
                Guest guest = room.getMembers().get(i);
                guest.setCurrentRoom("MainHall");
                this.roomHashMap.get("MainHall").addMember(guest);
            }
            g.deleteOwnership(roomid);
            this.roomList.remove(room);
            this.roomHashMap.remove(roomid);
        }
        ArrayList<HashMap> rooms = this.getRooms();
        rl.setRooms(rooms);
        info.setContent(JSON.toJSONString(rl));
        info.addConnection(this.connectionHashMap.get(g));
        return info;
    }

    private synchronized BroadcastInfo Who(String roomid, Guest g){
        RoomContents rc = new RoomContents();
        rc.setType(MessageType.ROOMCONTENTS.getType());
        rc.setRoomid(roomid);
        BroadcastInfo info = new BroadcastInfo();
        if(!this.roomHashMap.containsKey(roomid)){
            return info;
        }
        ArrayList<Guest> guests = this.roomHashMap.get(roomid).getMembers();
        ArrayList<String> identities = new ArrayList<>();
        for(int i =0;i<guests.size();i++){
            identities.add(guests.get(i).getIdentity());
        }
        rc.setIdentities(identities);
        Guest owner = this.roomHashMap.get(roomid).getOwner();
        rc.setOwner(owner!=null ? owner.getIdentity() : "");
        info.setContent(JSON.toJSONString(rc));
        info.addConnection(this.connectionHashMap.get(g));
        return info;
    }

    private synchronized BroadcastInfo Quit(Guest g){
        BroadcastInfo info = new BroadcastInfo();
        RoomChange rc = new RoomChange();
        rc.setType(MessageType.ROOMCHANGE.getType());
        rc.setFormer(g.getCurrentRoom());
        rc.setIdentity(g.getIdentity());
        rc.setRoomid("");
        info.setContent(JSON.toJSONString(rc));
        ArrayList<Guest> guestsToSend = this.roomHashMap.get(g.getCurrentRoom()).getMembers();
        for(int i=0;i<guestsToSend.size();i++){
            info.addConnection(this.connectionHashMap.get(guestsToSend.get(i)));
        }
        ArrayList<String> roomids = g.getOwnership();
        for(int i=0;i<roomids.size();i++){
            this.roomHashMap.get(roomids.get(i)).setOwner(null);
        }
        this.roomHashMap.get(g.getCurrentRoom()).deleteMember(g);
        return info;
    }

    private void CheckRoom(String roomid){

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
