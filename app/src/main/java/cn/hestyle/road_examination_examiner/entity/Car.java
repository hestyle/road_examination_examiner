package cn.hestyle.road_examination_examiner.entity;

import java.io.Serializable;

/**
 * 车辆entity
 * @author wjl
 */
public class Car implements Serializable {
    /**车辆id*/
    private Integer id;
    /**车辆仪器的ip地址*/
    private String ipAddress;
    /**车辆仪器的mac地址*/
    private String macAddress;
    /**车辆种类 C1 C2*/
    private String type;
    /**车辆描述*/
    private String description;
    /**是否删除，0未删除，1已删除*/
    private Integer isDel = 0;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", ipAddress='" + ipAddress + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", isDel=" + isDel +
                '}';
    }
}
