package cn.hestyle.tcp;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * tcp请求格式
 */
public abstract class TcpMessage implements Serializable {
    /** 消息类型 */
    private String typeName;
    /** 消息概述 */
    private String description;
    /** 消息附加data */
    private Map<String, String> dataMap;
    /** 消息中的操作项name（必须与数据库t_exam_operation表一致） */
    private List<String> examItemOperationName;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, String> dataMap) {
        this.dataMap = dataMap;
    }

    public List<String> getExamItemOperationName() {
        return examItemOperationName;
    }

    public void setExamItemOperationName(List<String> examItemOperationName) {
        this.examItemOperationName = examItemOperationName;
    }

    @Override
    public String toString() {
        return "TcpMessage{" +
                "typeName='" + typeName + '\'' +
                ", description='" + description + '\'' +
                ", dataMap=" + dataMap +
                ", examItemOperationName=" + examItemOperationName +
                '}';
    }
}
