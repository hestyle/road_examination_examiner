package cn.hestyle.tcp;

import java.io.Serializable;

public class TcpRequestMessage extends TcpMessage implements Serializable {
    /** TcpRequestMessage 类型 */
    public static final String REQUEST_CHECK_IP_AND_MAC = "CHECK_IP_AND_MAC";
    public static final String REQUEST_TCP_CONNECT_CLOSE = "TCP_CONNECT_CLOSE";

}
