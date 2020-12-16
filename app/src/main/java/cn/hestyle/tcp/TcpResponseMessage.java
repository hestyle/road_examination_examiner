package cn.hestyle.tcp;

import java.io.Serializable;

public class TcpResponseMessage extends TcpMessage implements Serializable {
    /** TcpResponseMessage 类型 */
    public static final String RESPONSE_CHECK_IP_AND_MAC = "REPLY_CHECK_IP_AND_MAC";
    /** 数据库中定义的操作 */
    public static final String RESPONSE_OPERATION_NAME = "REPLY_OPERATION_NAME";
    /** 车速、车门、 安全带*/
    public static final String RESPONSE_BASE_STATE = "REPLY_BASE_STATE";
}
