package util;

import jakarta.servlet.http.Part;

import java.util.Locale;
import java.util.Set;

public final class MultipartUtil {
    // Utility class; do not instantiate.
    private MultipartUtil() {
    }

    // Read the submitted filename from a multipart request part.
    public static String getSubmittedFileName(Part part) {
        if (part == null) {
            return null;
        }

        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) {
            return null;
        }

        for (String token : contentDisposition.split(";")) {
            String trimmed = token.trim();
            if (trimmed.startsWith("filename=")) {
                String fileName = trimmed.substring("filename=".length()).trim().replace("\"", "");
                return fileName.isBlank() ? null : fileName;
            }
        }
        return null;
    }

    // Extract the extension from a submitted file name.
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return null;
        }

        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    // Validate an uploaded file part by size, extension, and content type.
    public static void validateImagePart(
            Part part,
            long maxSize,
            Set<String> allowedExtensions,
            Set<String> allowedContentTypes,
            String fieldLabel
    ) {
        if (part == null || part.getSize() <= 0) {
            return;
        }

        if (part.getSize() > maxSize) {
            throw new IllegalArgumentException(fieldLabel + " must be " + (maxSize / 1024 / 1024) + " MB or smaller.");
        }

        String submittedName = getSubmittedFileName(part);
        String extension = getFileExtension(submittedName);
        String contentType = part.getContentType();

        if (extension == null || allowedExtensions == null || !allowedExtensions.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(fieldLabel + " must be JPG, JPEG, PNG, or WEBP.");
        }

        if (contentType != null && !contentType.isBlank()
                && allowedContentTypes != null
                && !allowedContentTypes.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(fieldLabel + " content type is not supported.");
        }
    }
}
