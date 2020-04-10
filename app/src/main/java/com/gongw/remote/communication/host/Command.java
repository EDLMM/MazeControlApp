package com.gongw.remote.communication.host;

/**
 * Created by gw on 2017/9/1.
 */
import com.example.mazecontrol.Views.CellGroup;

public class Command {
    private String destIp;
    private String content;
    private Callback callback;
    private CellGroup mazecells;

    public Command(String command, Callback callback){
        this.content = command;
        this.callback = callback;
    }
    public Command(String command, Callback callback,CellGroup current_cells){
        this.content = command;
        this.callback = callback;
        this.mazecells = current_cells;
    }


    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public CellGroup getMazecells(){return this.mazecells;}

    public interface Callback {
        void onRequest(String msg);
        void onSuccess(String msg);
        void onError(String msg);
        void onEcho(String msg);
    }
}
