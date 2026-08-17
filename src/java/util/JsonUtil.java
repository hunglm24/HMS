package util;

import model.Room;
import model.RoomType;
import java.util.List;

public class JsonUtil {

    // Simple manual JSON serializer to avoid external dependencies like Gson/Jackson
    
    public static String toJson(List<?> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (obj instanceof RoomType) {
                json.append(toJson((RoomType) obj));
            } else if (obj instanceof Room) {
                json.append(toJson((Room) obj));
            }
            if (i < list.size() - 1) {
                json.append(",\n");
            } else {
                json.append("\n");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String toJson(RoomType rt) {
        return "  {\n" +
               "    \"id\": " + rt.getId() + ",\n" +
               "    \"name\": \"" + escape(rt.getName()) + "\",\n" +
               "    \"description\": \"" + escape(rt.getDescription()) + "\",\n" +
               "    \"capacity\": " + rt.getCapacity() + ",\n" +
               "    \"basePrice\": " + (rt.getBasePrice() != null ? rt.getBasePrice().toString() : "0") + ",\n" +
               "    \"availableCount\": " + rt.getAvailableCount() + "\n" +
               "  }";
    }

    private static String toJson(Room r) {
        return "  {\n" +
               "    \"id\": " + r.getId() + ",\n" +
               "    \"roomNumber\": \"" + escape(r.getRoomNumber()) + "\",\n" +
               "    \"roomTypeName\": \"" + escape(r.getRoomTypeName()) + "\",\n" +
               "    \"description\": \"" + escape(r.getDescription()) + "\",\n" +
               "    \"status\": \"" + escape(r.getStatus()) + "\"\n" +
               "  }";
    }

    private static String escape(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
