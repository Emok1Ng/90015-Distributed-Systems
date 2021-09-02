package com.assignment1;

import com.alibaba.fastjson.JSON;
import com.assignment1.base.Enum.Command;
import com.assignment1.base.Enum.MessageType;
import com.assignment1.base.Message.C2S.*;

public class InputParser {

    public String toJSON(String s){
        String toSend = "";
        if(s.isEmpty()){
            return null;
        }
        else if(s.substring(0,1).equals("#")){
            toSend = parseToCommand(s.substring(1));
        }
        else{
            toSend = parseToMessage(s);
        }
        return toSend;
    }

    public String parseToCommand(String s){
        String[] parts = s.split(" ");
        String command = parts[0];
        String arg1  = "";
        if(command.equals(Command.IDENTITYCHANGE.getCommand()) && parts.length >= 2){
            arg1 = parts[1];
            IdentityChange ic = new IdentityChange();
            ic.setType(Command.IDENTITYCHANGE.getCommand());
            ic.setIdentity(arg1);
            return JSON.toJSONString(ic);
        }
        else if(command.equals(Command.JOIN.getCommand()) && parts.length >= 2){
            arg1 = parts[1];
            Join j = new Join();
            j.setType(Command.JOIN.getCommand());
            j.setRoomid(arg1);
            return JSON.toJSONString(j);
        }
        else if(command.equals(Command.LIST.getCommand())){
            List l = new List();
            l.setType(Command.LIST.getCommand());
            return JSON.toJSONString(l);
        }
        else if(command.equals(Command.CREATEROOM.getCommand()) && parts.length >= 2){
            arg1 = parts[1];
            CreateRoom cr = new CreateRoom();
            cr.setType(Command.CREATEROOM.getCommand());
            cr.setRoomid(arg1);
            return JSON.toJSONString(cr);
        }
        else if(command.equals(Command.DELETEROOM.getCommand()) && parts.length >= 2){
            arg1 = parts[1];
            Delete d = new Delete();
            d.setType(Command.DELETEROOM.getCommand());
            d.setRoomid(arg1);
            return JSON.toJSONString(d);
        }
        else if(command.equals(Command.WHO.getCommand()) && parts.length >= 2){
            arg1 = parts[1];
            Who w = new Who();
            w.setType(Command.WHO.getCommand());
            w.setRoomid(arg1);
            return JSON.toJSONString(w);
        }
        else if(command.equals(Command.QUIT.getCommand())){
            Quit q = new Quit();
            q.setType(Command.QUIT.getCommand());
            return JSON.toJSONString(q);
        }
        else{
            return null;
        }
    }

    public String parseToMessage(String s){
        Message message = new Message();
        message.setType(MessageType.MESSAGE.getType());
        message.setContent(s);
        return JSON.toJSONString(message);
    }
}
