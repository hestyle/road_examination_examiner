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
    /** 踩住离合踏板 */
    public static final String STEP_ON_CLUTCH_PEDAL = "STEP_ON_CLUTCH_PEDAL";
    /** 松开离合踏板 */
    public static final String STEP_OFF_CLUTCH_PEDAL = "STEP_OFF_CLUTCH_PEDAL";
    /** 踩住刹车踏板 */
    public static final String STEP_ON_BRAKE_PEDAL = "STEP_ON_BRAKE_PEDAL";
    /** 松开刹车踏板 */
    public static final String STEP_OFF_BRAKE_PEDAL = "STEP_OFF_BRAKE_PEDAL";
    /** 轻踏加速踏板 */
    public static final String LIGHT_STEP_ON_ACCELERATOR_PEDAL = "LIGHT_STEP_ON_ACCELERATOR_PEDAL";
    /** 松开加速踏板 */
    public static final String STEP_OFF_ACCELERATOR_PEDAL = "STEP_OFF_ACCELERATOR_PEDAL";
    /** 左打方向盘[0°,15°)，大约半圈 */
    public static final String STEER_WHEEL_SLIGHT_TURN_LEFT = "STEER_WHEEL_SLIGHT_TURN_LEFT";
    /** 右打方向盘(-15°,0°]，大约半圈 */
    public static final String STEER_WHEEL_SLIGHT_TURN_RIGHT = "STEER_WHEEL_SLIGHT_TURN_RIGHT";
    /** 左打方向盘[15°,45°)，大约一圈 */
    public static final String STEER_WHEEL_MODERATE_TURN_LEFT = "STEER_WHEEL_MODERATE_TURN_LEFT";
    /** 右打方向盘(-45°,-15°]，大约一圈 */
    public static final String STEER_WHEEL_MODERATE_TURN_RIGHT = "STEER_WHEEL_MODERATE_TURN_RIGHT";
    /** 拉起手刹 */
    public static final String PULL_UP_PARK_BRAKE = "PULL_UP_PARK_BRAKE";
    /** 放下手刹 */
    public static final String PULL_DOWN_PARK_BRAKE = "PULL_DOWN_PARK_BRAKE";
    /** 观察后视镜 */
    public static final String OBSERVE_REARVIEW_MIRROR = "OBSERVE_REARVIEW_MIRROR";
    /** 挂空挡 */
    public static final String SET_NEUTRAL_GEAR = "SET_NEUTRAL_GEAR";
    /** 挂前进挡（1档） */
    public static final String SET_FORWARD_GEAR = "SET_FORWARD_GEAR";
    /** 挂2档 */
    public static final String SET_SECOND_GEAR = "SET_SECOND_GEAR";
    /** 挂3档 */
    public static final String SET_THIRD_GEAR = "SET_THIRD_GEAR";
    /** 挂4档 */
    public static final String SET_FOURTH_GEAR = "SET_FOURTH_GEAR";
    /** 挂5档 */
    public static final String SET_FIFTH_GEAR = "SET_FIFTH_GEAR";
    /** 挂倒挡 */
    public static final String SET_REVERSE_GEAR = "SET_REVERSE_GEAR";



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
        descriptionMap.put("STEP_ON_CLUTCH_PEDAL", "踩住离合踏板");
        descriptionMap.put("STEP_OFF_CLUTCH_PEDAL", "松开离合踏板");
        descriptionMap.put("STEP_ON_BRAKE_PEDAL", "踩住刹车踏板");
        descriptionMap.put("STEP_OFF_BRAKE_PEDAL", "松开刹车踏板");
        descriptionMap.put("LIGHT_STEP_ON_ACCELERATOR_PEDAL", "轻踏加速踏板");
        descriptionMap.put("STEP_OFF_ACCELERATOR_PEDAL", "松开加速踏板");
        descriptionMap.put("STEER_WHEEL_SLIGHT_TURN_LEFT", "左打方向盘[0°,15°)，大约半圈");
        descriptionMap.put("STEER_WHEEL_SLIGHT_TURN_RIGHT", "右打方向盘(-15°,0°]，大约半圈");
        descriptionMap.put("STEER_WHEEL_MODERATE_TURN_LEFT", "左打方向盘[15°,45°)，大约一圈");
        descriptionMap.put("STEER_WHEEL_MODERATE_TURN_RIGHT", "右打方向盘(-45°,-15°]，大约一圈");
        descriptionMap.put("PULL_UP_PARK_BRAKE", "拉起手刹");
        descriptionMap.put("PULL_DOWN_PARK_BRAKE", "放下手刹");
        descriptionMap.put("OBSERVE_REARVIEW_MIRROR", "观察后视镜");
        descriptionMap.put("SET_NEUTRAL_GEAR", "挂空挡");
        descriptionMap.put("SET_FORWARD_GEAR", "挂前进挡（1档）");
        descriptionMap.put("SET_SECOND_GEAR", "挂2档");
        descriptionMap.put("SET_THIRD_GEAR", "挂3档");
        descriptionMap.put("SET_FOURTH_GEAR", "挂4档");
        descriptionMap.put("SET_FIFTH_GEAR", "挂5档");
        descriptionMap.put("SET_REVERSE_GEAR", "挂倒挡");
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
