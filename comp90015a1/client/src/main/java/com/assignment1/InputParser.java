package com.assignment1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.assignment1.base.Enum.MessageType;

public class InputParser {

    private String identity;
    private String currentRoom;

    public InputParser() {
        this.identity = "";
        this.currentRoom = "MainHall";
    }

    public Integer print(String s, int status){
        JSONObject json = JSON.parseObject(s);
        String type = json.get("type").toString();
        if(type.equals(MessageType.MESSAGE.getType())){
            String speaker = json.get("identity").toString();
            String content = json.get("content").toString();
            System.out.printf("[%s] %s>%s\n",this.currentRoom,speaker,content);
        }
        else if(type.equals(MessageType.ROOMLIST.getType())){

        }
        else if(type.equals(MessageType.NEWIDENTITY.getType())){

        }
        else if(type.equals(MessageType.ROOMCHANGE.getType())){

        }
        else if(type.equals(MessageType.ROOMCONTENTS.getType())){

        }
        else if(type.equals(MessageType.MESSAGE.getType())) {

        }
        return 0;
    }
}
