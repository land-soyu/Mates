package kr.co.netseason.myclebot.API;

/**
 * Created by Administrator on 2015-06-08.
 */

public class CreateIDData {
    public String id;
    public String pwd;
    public String name;
    public String email;
    public String sex;
    public String bday;

    public CreateIDData() {
        this.id = "";
        this.pwd = "";
        this.name = "";
        this.email = "";
        this.sex = "";
        this.bday = "";
    }
    public CreateIDData(String id, String pwd, String name, String email, String sex, String bday) {
        this.id = id;
        this.pwd = pwd;
        this.name = name;
        this.email = email;
        this.sex = sex;
        this.bday = bday;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBday() {
        return bday;
    }

    public void setBday(String bday) {
        this.bday = bday;
    }
}