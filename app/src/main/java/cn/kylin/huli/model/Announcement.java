package cn.kylin.huli.model;

public class Announcement {
    private Long id;
    private Long sendby;
    private String info;
    private String endtime;
    public Announcement(Long id,Long sendby,String info,String endtime){
        this.id=id;
        this.sendby=sendby;
        this.info=info;
        this.endtime=endtime;
    }
    public Announcement(){

    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getSendby() {
        return sendby;
    }

    public String getEndtime() {
        return endtime;
    }

    public String getInfo() {
        return info;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setSendby(Long sendby) {
        this.sendby = sendby;
    }
}
