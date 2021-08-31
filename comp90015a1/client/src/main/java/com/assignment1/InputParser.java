package com.assignment1;

import com.alibaba.fastjson.JSON;
import com.assignment1.base.Enum.Command;
import com.assignment1.base.Enum.MessageType;
import com.assignment1.base.Message.C2S.Message;

public class InputParser {

    public String toJSON(String s){
        String toSend = "";
        if(s.isEmpty()){
            return null;
        }
        else if(s.substring(0,1).equals("#")){
            toSend = parseToCommand(s);
        }
        else{
            toSend = parseToMessage(s);
        }
        return toSend;
    }

    public String parseToCommand(String s){
        String[] parts = s.split(" ");
        String command = parts[0];
        if(command.equals(Command.IDENTITYCHANGE.getCommand())){

        }
        else if(command.equals(Command.JOIN.getCommand())){

        }
        else if(command.equals(Command.LIST.getCommand())){

        }
        else if(command.equals(Command.CREATEROOM.getCommand())){

        }
        else if(command.equals(Command.DELETEROOM.getCommand())){

        }
        else if(command.equals(Command.WHO.getCommand())){

        }
        else if(command.equals(Command.QUIT.getCommand())){

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
