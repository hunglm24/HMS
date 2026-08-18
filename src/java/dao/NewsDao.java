package dao;

import model.News;
import util.DBConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NewsDao {

    public List<News> getAllNews(String search, String status, int offset, int limit) {
        List<News> newsList = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT n.*, a.full_name as creator_name " +
            "FROM news n " +
            "JOIN accounts a ON n.created_by = a.id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND n.title LIKE ? ");
            params.add("%" + search.trim() + "%");
        }

        if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
            sql.append("AND n.status = ? ");
            params.add(status);
        }

        sql.append("ORDER BY n.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    newsList.add(mapResultSetToNews(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newsList;
    }

    public int getTotalNewsCount(String search, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM news n WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND n.title LIKE ? ");
            params.add("%" + search.trim() + "%");
        }

        if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
            sql.append("AND n.status = ? ");
            params.add(status);
        }

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<News> getPublishedNews(int offset, int limit) {
        List<News> newsList = new ArrayList<>();
        String sql = "SELECT n.*, a.full_name as creator_name " +
                     "FROM news n " +
                     "JOIN accounts a ON n.created_by = a.id " +
                     "WHERE n.status = 'PUBLISHED' " +
                     "ORDER BY n.published_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    newsList.add(mapResultSetToNews(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newsList;
    }

    public int getTotalPublishedNewsCount() {
        String sql = "SELECT COUNT(*) FROM news WHERE status = 'PUBLISHED'";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<News> getLatestNews(int limit) {
        return getPublishedNews(0, limit);
    }

    public Optional<News> getNewsById(long id) {
        String sql = "SELECT n.*, a.full_name as creator_name " +
                     "FROM news n " +
                     "JOIN accounts a ON n.created_by = a.id " +
                     "WHERE n.id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToNews(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public News insertNews(News news) {
        String sql = "INSERT INTO news (title, content, thumbnail_url, status, created_by, published_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, news.getTitle());
            stmt.setString(2, news.getContent());
            stmt.setString(3, news.getThumbnailUrl());
            stmt.setString(4, news.getStatus());
            stmt.setLong(5, news.getCreatedBy());
            stmt.setTimestamp(6, news.getPublishedAt());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating news failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    news.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating news failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return news;
    }

    public boolean updateNews(News news) {
        String sql = "UPDATE news SET title = ?, content = ?, thumbnail_url = ?, status = ?, published_at = ? " +
                     "WHERE id = ?";

        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, news.getTitle());
            stmt.setString(2, news.getContent());
            stmt.setString(3, news.getThumbnailUrl());
            stmt.setString(4, news.getStatus());
            stmt.setTimestamp(5, news.getPublishedAt());
            stmt.setLong(6, news.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteNews(long id) {
        String sql = "DELETE FROM news WHERE id = ?";
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateStatus(long id, String status) {
        String sql = "UPDATE news SET status = ?";
        if ("PUBLISHED".equals(status)) {
            sql += ", published_at = CURRENT_TIMESTAMP ";
        }
        sql += " WHERE id = ?";
        
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private News mapResultSetToNews(ResultSet rs) throws SQLException {
        News news = new News();
        news.setId(rs.getLong("id"));
        news.setTitle(rs.getString("title"));
        news.setContent(rs.getString("content"));
        news.setThumbnailUrl(rs.getString("thumbnail_url"));
        news.setStatus(rs.getString("status"));
        news.setCreatedBy(rs.getLong("created_by"));
        news.setCreatedAt(rs.getTimestamp("created_at"));
        news.setUpdatedAt(rs.getTimestamp("updated_at"));
        news.setPublishedAt(rs.getTimestamp("published_at"));
        
        // creator_name logic
        try {
            news.setCreatorName(rs.getString("creator_name"));
        } catch (SQLException e) {
            // column might not exist in some queries (like basic selects if not joined)
        }

        return news;
    }
}
