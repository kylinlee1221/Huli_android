package cn.kylin.huli.model;

import android.util.Log;

public class User {
    private String fullname;
    private String userId;
    private String telephone;
    private String role;
    private String description;
    private String username;
    private Long id;
    public User(Long id,String fullname,String userId,String telephone,String role,String description){
        this.id=id;
        this.userId=userId;
        this.fullname=fullname;
        this.telephone=telephone;
        this.role=role;
        this.description=description;
    }
    public User(Long id,String fullname,String telephone,String username){
        this.id=id;
        this.fullname=fullname;
        this.telephone=telephone;
        this.username=username;
    }
    public User(Long id,String fullname,String telephone){
        this.id=id;
        this.fullname=fullname;
        this.telephone=telephone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public String getFullname() {
        return fullname;
    }

    public String getRole() {
        return role;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getUserId() {
        return userId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    //p
}
