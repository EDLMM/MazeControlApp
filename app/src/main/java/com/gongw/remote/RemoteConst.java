package com.gongw.remote;

public class RemoteConst {
    /**
     * 用于设备搜索的端口
     */
    public static final int DEVICE_SEARCH_PORT = 8100;
    public static final int DEVICE_SEARCH_PORT_RECEIVE = 8101;
    public static final int DEVICE_SEARCH_PORT_SEND = 8102;

    /**
     * 用于接收命令的端口
     */
    public static final int COMMAND_RECEIVE_PORT = 60001;
    /**
     * 设备搜索次数
     */
    public static final int SEARCH_DEVICE_TIMES = 10;
    /**
     * 搜索的最大设备数量
     */
    public static final int SEARCH_DEVICE_MAX = 250;
    /**
     * 接收超时时间
     */
    public static final int RECEIVE_TIME_OUT = 1000;

    /**
     * udp数据包前缀
     */
    public static final int PACKET_PREFIX = '$';
    /**
     * udp数据包类型：搜索类型
     */
    public static final int PACKET_TYPE_SEARCH_DEVICE_REQ = 0x10;
    /**
     * udp数据包类型：搜索应答类型
     */
    public static final int PACKET_TYPE_SEARCH_DEVICE_RSP = 0x11;

    /**
     * udp数据包类型：搜索应答类型
     */
    public static final long CommandClassSerialID = 113621220484574L;

    public  static final String ADMIN_SEARCHER_ID = new String("Admin");
}
