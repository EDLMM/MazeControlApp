package com.remote;

import java.io.Serializable;

public class MazeTopoCommand implements Serializable {
    private static final long serialVersionUID= UDPConstant.MAZE_COMMAND_SERIALIZABLE_UUID;
    // control key word for background service
    public enum Sender {
        ADMIN, PLAYER
    }
    public Sender sender;
}
