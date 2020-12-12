package cn.hestyle.road_examination_examiner.entity;

/**
 * 考试项entity
 * @author hestyle
 */
public class ExamItem {
    /**id 主键、自动增长*/
    private Integer id;
    /**操作项名称*/
    private String name;
    /**考试项分值*/
    private Integer score;
    /**操作项音频路径*/
    private String voicePath;
    /**操作项操作项id列表（包含顺序）*/
    private String operationIds;
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getVoicePath() {
        return voicePath;
    }

    public void setVoicePath(String voicePath) {
        this.voicePath = voicePath;
    }

    public String getOperationIds() {
        return operationIds;
    }

    public void setOperationIds(String operationIds) {
        this.operationIds = operationIds;
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
        return "ExamItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", score=" + score +
                ", voicePath='" + voicePath + '\'' +
                ", operationIds='" + operationIds + '\'' +
                ", description='" + description + '\'' +
                ", isDel=" + isDel +
                '}';
    }
}
