package cn.hestyle.road_examination_examiner.entity;


import java.io.Serializable;
import java.util.Map;

/**
 * 考试过程中，与tcp远端交互，更新ui线程的通知
 */
public class ExamUpdateUiBroadcastMessage implements Serializable {
    /** extra name */
    public static final String MESSAGE_NAME = "ExamUpdateUiBroadcastMessage";
    /** 消息类型 */
    public static final String CAR_SETTING_RESULT = "CAR_SETTING_RESULT";
    public static final String EXAM_STOPPED_BY_EXCEPTION = "EXAM_STOPPED_BY_EXCEPTION";

    /** 消息类型 */
    private String typeName;
    /** 消息主体 */
    private String message;
    /** 消息附带数据 */
    private Map<String, Object> data;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ExamUpdateUiBroadcastMessage{" +
                "typeName='" + typeName + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
