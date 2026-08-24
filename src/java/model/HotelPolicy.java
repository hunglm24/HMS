package model;

import service.CancellationPolicyService;

import java.sql.Timestamp;

public class HotelPolicy extends BaseEntity {
    private String title;
    private String category;
    private String content;
    private String status;
    private Timestamp updatedAt;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getDisplayContent() { return CancellationPolicyService.displayContent(content); }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
