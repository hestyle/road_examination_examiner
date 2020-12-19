package cn.hestyle.road_examination_examiner.entity;

import java.io.Serializable;
import java.util.List;

public class Gear implements Serializable {
    /** 档位id 主键，自动增长 */
    private Integer id;
    /** 档位名称 */
    private String name;
    /** 档位最小速度 */
    private Integer minSpeed;
    /** 档位最大速度 */
    private Integer maxSpeed;
    /** 档位描述 */
    private String description;
    /** 是否删除 1:是 0:否 */
    private Integer isDel;

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMinSpeed(Integer minSpeed) {
        this.minSpeed = minSpeed;
    }

    public void setMaxSpeed(Integer maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getMinSpeed() {
        return minSpeed;
    }

    public Integer getMaxSpeed() {
        return maxSpeed;
    }

    public String getDescription() {
        return description;
    }

    public Integer getIsDel() {
        return isDel;
    }

    @Override
    public String toString() {
        return "Gear{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", minSpeed=" + minSpeed +
                ", maxSpeed=" + maxSpeed +
                ", description='" + description + '\'' +
                ", isDel=" + isDel +
                '}';
    }

    /**
     * 通过档位操作名称获取档位
     * @param gearList          档位list
     * @param operationName     操作名（挂挡操作
     * @return                  档位
     */
    public static Gear findGearByOperationName(List<Gear> gearList, String operationName) {
        if (gearList == null || operationName == null) {
            return null;
        }
        // 遍历gearList
        for (Gear gear : gearList) {
            if (operationName.endsWith(gear.getName())) {
                return gear;
            }
        }
        return null;
    }

    /**
     * 判断是否是升档操作
     * @param nowGearName       当前档位name
     * @param operationName     操作name
     * @return                  true false
     */
    public static boolean isSetUpGearOperationName(String nowGearName, String operationName) {
        if ("FORWARD_GEAR".equals(nowGearName)) {
            return ExamOperation.SET_SECOND_GEAR.equals(operationName);
        } else if ("SECOND_GEAR".equals(nowGearName)) {
            return ExamOperation.SET_THIRD_GEAR.equals(operationName);
        } else if ("THIRD_GEAR".equals(nowGearName)) {
            return ExamOperation.SET_FOURTH_GEAR.equals(operationName);
        } else if ("FOURTH_GEAR".equals(nowGearName)) {
            return ExamOperation.SET_FIFTH_GEAR.equals(operationName);
        } else {
            return false;
        }
    }

    /**
     * 判断是否是降档操作
     * @param nowGearName       当前档位name
     * @param operationName     操作name
     * @return                  true false
     */
    public static boolean isSeDownGearOperationName(String nowGearName, String operationName) {
        if ("SECOND_GEAR".equals(nowGearName)) {
            return ExamOperation.SET_FORWARD_GEAR.equals(operationName);
        } else if ("THIRD_GEAR".equals(nowGearName)) {
            return ExamOperation.SET_SECOND_GEAR.equals(operationName);
        } else if ("FOURTH_GEAR".equals(nowGearName)) {
            return ExamOperation.SET_THIRD_GEAR.equals(operationName);
        } else if ("FIFTH_GEAR".equals(nowGearName)) {
            return ExamOperation.SET_FOURTH_GEAR.equals(operationName);
        } else {
            return false;
        }
    }
}
