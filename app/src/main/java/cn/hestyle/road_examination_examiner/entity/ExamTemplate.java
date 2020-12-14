package cn.hestyle.road_examination_examiner.entity;

import java.util.List;

public class ExamTemplate {
    /** 考试模板id */
    String id;
    /** 考试模板名称 夜考 道路考试 */
    String name;
    /** 考试模板总分 */
    Integer score;
    /** 考试类型 C1 C2 */
    String type;
    /** 是否删除 1:是 0:否 */
    Integer isDel;

    List<ExamItem> examItemList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public void setExamItemList(List<ExamItem> examItemList) {
        this.examItemList = examItemList;
    }

    public List<ExamItem> getExamItemList() {
        return examItemList;
    }
}
