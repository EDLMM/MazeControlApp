package com.remote;

public class UDPConstant {
    public static final String IP_ADDRESS = "224.0.0.1"; //地址范围是224.0.0.0至239.255.255.255
    public static final int PORT = 8189; // port for maze topology
    public static final int LOCATION_PORT = 8289;//port for updating location
    public static final int TTLTIME = 1;
    public static final int CELL_GROUP_SERIALIZABLE_BUF = 2048 ;
    public static final int SPOT_LOCATION_SERIALIZABLE_BUF = 2048 ;
    public static final long MAZE_CELL_SERIALIZABLE_UUID=1568398;
    public static final long MAZE_COMMAND_SERIALIZABLE_UUID=981274398;

    public static final int SEND_VIEW_UPDATA_INTERVAL_MS = 100 ; // 由于设备的处理速度跟不上，设成 30ms 感觉和 100 差不多

    public static final String RECEIVE_TOPOLOGY_UPDATE_BROADCAST_ACTION = "com.remote.MultiCastServiceReceive_updata_cellgroup";
    public static final String RECEIVE_LOCATION_UPDATE_BROADCAST_ACTION = "com.remote.MultiCastServiceReceive_updata_spotlocation";

    // control key word for background service
    public enum Control {
        PLAY, UPDATE, STOP
    }
}
