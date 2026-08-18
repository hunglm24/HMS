package model;

import java.sql.Timestamp;

public class Amenity extends BaseEntity {
    private String name, description, icon, status;
    private Timestamp updatedAt;

    public String getName() { return name; }
    public void setName(String v) { name = v; }

    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }

    public String getIcon() { return icon; }
    public void setIcon(String v) { icon = v; }

    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp v) { updatedAt = v; }
}
