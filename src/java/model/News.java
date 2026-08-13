package model;
import java.sql.Timestamp;
public class News extends BaseEntity {
 private String title,content,thumbnailUrl,status; private long createdBy; private Timestamp updatedAt,publishedAt;
 public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getContent(){return content;} public void setContent(String v){content=v;} public String getThumbnailUrl(){return thumbnailUrl;} public void setThumbnailUrl(String v){thumbnailUrl=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public long getCreatedBy(){return createdBy;} public void setCreatedBy(long v){createdBy=v;} public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;} public Timestamp getPublishedAt(){return publishedAt;} public void setPublishedAt(Timestamp v){publishedAt=v;}
}
