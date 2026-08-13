package model;

public class Role extends BaseEntity {
    private String name, description;
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
}
