package cn.hestyle.road_examination_examiner.entity;

import java.io.Serializable;

/**
 * 考试操作项entity
 * @author hestyle
 */
public class ExamOperation implements Serializable {
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
