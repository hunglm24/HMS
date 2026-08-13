package model;

import java.sql.Timestamp;

public abstract class BaseEntity {
    protected Long id;
    protected Timestamp createdAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
