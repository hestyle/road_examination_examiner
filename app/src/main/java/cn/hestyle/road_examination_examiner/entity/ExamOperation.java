package cn.hestyle.road_examination_examiner.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 考试操作项entity
 * @author hestyle
 */
public class ExamOperation implements Serializable {
    /** 开左转灯 */
    public static final String TURN_ON_LEFT_TURN_SIGNAL = "TURN_ON_LEFT_TURN_SIGNAL";
    /** 开右转灯 */
    public static final String TURN_ON_RIGHT_TURN_SIGNAL = "TURN_ON_RIGHT_TURN_SIGNAL";
    /** 开近光灯 */
    public static final String TURN_ON_DIPPED_LIGHT = "TURN_ON_DIPPED_LIGHT";
    /** 开远光灯 */
    public static final String TURN_ON_HIGH_LIGHT = "TURN_ON_HIGH_LIGHT";
    /** 开示廊灯 */
    public static final String TURN_ON_OUTLINE_MARK_LIGHT = "TURN_ON_OUTLINE_MARK_LIGHT";
    /** 开雾灯 */
    public static final String TURN_ON_FOG_LIGHT = "TURN_ON_FOG_LIGHT";
    /** 开危险警报闪光灯 */
    public static final String TURN_ON_HAZARD_WARN_LIGHT = "TURN_ON_HAZARD_WARN_LIGHT";
    /** 开远、近灯光交替 */
    public static final String TURN_ON_HIGH_DIPPED_LIGHT = "TURN_ON_HIGH_DIPPED_LIGHT";

    private static final Map<String, String> descriptionMap = new HashMap<>();

    static {
        descriptionMap.put("TURN_ON_LEFT_TURN_SIGNAL", "开启左转灯");
        descriptionMap.put("TURN_ON_RIGHT_TURN_SIGNAL", "开启右转灯");
        descriptionMap.put("TURN_ON_DIPPED_LIGHT", "开启近光灯");
        descriptionMap.put("TURN_ON_HIGH_LIGHT", "开启远光灯");
        descriptionMap.put("TURN_ON_OUTLINE_MARK_LIGHT", "开启示廊灯");
        descriptionMap.put("TURN_ON_FOG_LIGHT", "开启雾灯");
        descriptionMap.put("TURN_ON_HAZARD_WARN_LIGHT", "开启危险警报闪光灯");
        descriptionMap.put("TURN_ON_HIGH_DIPPED_LIGHT", "开启远、近灯光交替");
    }

    /**
     * 判断是否是灯光操作
     * @param operationName 操作名字
     * @return              判断结果
     */
    public static boolean isLightOperation(String operationName) {
        if (operationName == null) {
            return false;
        } else if (TURN_ON_LEFT_TURN_SIGNAL.equals(operationName)) {
            return true;
        } else if (TURN_ON_RIGHT_TURN_SIGNAL.equals(operationName)) {
            return true;
        } else if (TURN_ON_DIPPED_LIGHT.equals(operationName)) {
            return true;
        } else if (TURN_ON_HIGH_LIGHT.equals(operationName)) {
            return true;
        } else if (TURN_ON_OUTLINE_MARK_LIGHT.equals(operationName)) {
            return true;
        } else if (TURN_ON_FOG_LIGHT.equals(operationName)) {
            return true;
        } else if (TURN_ON_HAZARD_WARN_LIGHT.equals(operationName)) {
            return true;
        } else {
            return TURN_ON_HIGH_DIPPED_LIGHT.equals(operationName);
        }
    }

    /**
     * 通过operationName获取描述
     * @param operationName operationName
     * @return              描述
     */
    public static String getOpMsgByOpName(String operationName) {
        return descriptionMap.get(operationName);
    }

    /**id 主键、自动增长*/
    private Integer id;
    /**操作项名称*/
    private String name;
    /**操作项描述*/
    private String description;
    /**是否删除，0未删除，1已删除*/
    private Integer isDel;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    @Override
    public String toString() {
        return "ExamOperation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isDel=" + isDel +
                '}';
    }
}
