package model;

import java.sql.Timestamp;

/** Compatibility view used by the authentication layer; persisted in accounts. */
public class User extends Account {
    public int getUserId(){return getId()==null?0:getId().intValue();}
    public void setUserId(int v){setId((long)v);}
    public String getPasswordHash(){return getPassword();}
    public void setPasswordHash(String v){setPassword(v);}
    public long getRoleId(){return super.getRoleId();}
    public void setRoleId(int v){super.setRoleId(v);}
    public void setCreatedAt(java.util.Date v){super.setCreatedAt(v==null?null:new Timestamp(v.getTime()));}
}
