package cn.hestyle.tcp;

import java.io.Serializable;

public class TcpResponseMessage extends TcpMessage implements Serializable {
    /** TcpResponseMessage 类型 */
    public static final String RESPONSE_CHECK_IP_AND_MAC = "REPLY_CHECK_IP_AND_MAC";
}
