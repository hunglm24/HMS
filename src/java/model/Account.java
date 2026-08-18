package model;

import java.sql.Timestamp;

public class Account extends BaseEntity {
    private long roleId;
    private String fullName, email, phone, avatarUrl, password, status, roleName;
    private Timestamp updatedAt;
    public long getRoleId(){return roleId;} public void setRoleId(long v){roleId=v;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getAvatarUrl(){return avatarUrl;} public void setAvatarUrl(String v){avatarUrl=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getRoleName(){return roleName;} public void setRoleName(String v){roleName=v;}
    public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;}
}
