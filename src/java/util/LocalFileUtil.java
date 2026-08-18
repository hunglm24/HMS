package util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Locale;

public final class LocalFileUtil {
    // Utility class; do not instantiate.
    private LocalFileUtil() {
    }

    // Save an uploaded image under the web folder and return a web-accessible relative path.
    public static String saveImagePart(Part part, ServletContext servletContext, String webRelativeDir, String filePrefix)
            throws IOException {
        if (part == null || part.getSize() <= 0) {
            return null;
        }

        if (servletContext == null) {
            throw new IOException("Servlet context is not available.");
        }

        String submittedName = MultipartUtil.getSubmittedFileName(part);
        String extension = MultipartUtil.getFileExtension(submittedName);
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("Cover image must have a valid file extension.");
        }

        String safePrefix = sanitizePathSegment(filePrefix);
        if (safePrefix.isBlank()) {
            safePrefix = "room-type";
        }

        String safeDir = normalizeWebDir(webRelativeDir);
        String fileName = safePrefix + "-" + System.currentTimeMillis() + "." + extension.toLowerCase(Locale.ROOT);

        Set<Path> targetFiles = resolveTargetFiles(servletContext, safeDir, fileName);
        if (targetFiles.isEmpty()) {
            throw new IOException("Unable to resolve upload directory.");
        }

        try (InputStream inputStream = part.getInputStream()) {
            byte[] imageBytes = inputStream.readAllBytes();
            for (Path targetFile : targetFiles) {
                Files.createDirectories(targetFile.getParent());
                Files.write(targetFile, imageBytes);
            }
        }

        return buildWebPath(safeDir, fileName);
    }

    // Delete a previously saved local file if it exists.
    public static void deleteByWebPath(ServletContext servletContext, String webPath) {
        if (servletContext == null || ValidationUtil.isBlank(webPath)) {
            return;
        }

        try {
            for (Path filePath : resolveAllDiskPaths(servletContext, webPath)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ignored) {
            // Best-effort cleanup only.
        }
    }

    // Resolve a web path such as /uploads/room-types/file.jpg to a disk path.
    public static Path resolveWebPathToDiskPath(ServletContext servletContext, String webPath) {
        if (servletContext == null || ValidationUtil.isBlank(webPath)) {
            return null;
        }

        String normalized = webPath.startsWith("/") ? webPath : "/" + webPath;
        String realPath = servletContext.getRealPath(normalized);
        if (!ValidationUtil.isBlank(realPath)) {
            return Path.of(realPath);
        }

        Path projectWebRoot = resolveProjectWebRoot(servletContext);
        if (projectWebRoot != null) {
            return projectWebRoot.resolve(normalized.substring(1).replace("/", File.separator));
        }

        File fallbackWebDir = new File(System.getProperty("user.dir", "."), "web");
        return fallbackWebDir.toPath().resolve(normalized.substring(1).replace("/", File.separator));
    }

    // Resolve every possible physical location for the same web path.
    private static Set<Path> resolveAllDiskPaths(ServletContext servletContext, String webPath) {
        Set<Path> paths = new LinkedHashSet<>();
        Path primaryPath = resolveWebPathToDiskPath(servletContext, webPath);
        if (primaryPath != null) {
            paths.add(primaryPath);
        }

        Path projectWebRoot = resolveProjectWebRoot(servletContext);
        if (projectWebRoot != null) {
            Path projectPath = projectWebRoot.resolve(webPath.startsWith("/") ? webPath.substring(1) : webPath);
            paths.add(projectPath);
        } else {
            Path projectPath = resolveProjectWebRootFallback().resolve(webPath.startsWith("/") ? webPath.substring(1) : webPath);
            paths.add(projectPath);
        }
        return paths;
    }

    // Build a web path from a directory and file name.
    private static String buildWebPath(String webRelativeDir, String fileName) {
        String normalizedDir = normalizeWebDir(webRelativeDir);
        if (normalizedDir.isBlank()) {
            return "/" + fileName;
        }
        return "/" + normalizedDir + "/" + fileName;
    }

    // Convert a web directory input into a clean relative path without leading/trailing slashes.
    private static String normalizeWebDir(String webRelativeDir) {
        if (ValidationUtil.isBlank(webRelativeDir)) {
            return "";
        }
        String normalized = webRelativeDir.trim().replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    // Keep only safe characters for file name segments.
    private static String sanitizePathSegment(String value) {
        if (ValidationUtil.isBlank(value)) {
            return "";
        }
        String normalized = ValidationUtil.normalizeLower(value)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return normalized;
    }

    // Resolve the target directory used for file storage.
    private static Path resolveTargetDirectory(ServletContext servletContext, String webRelativeDir) {
        String realPath = servletContext.getRealPath("/" + webRelativeDir);
        if (!ValidationUtil.isBlank(realPath)) {
            return Path.of(realPath);
        }
        Path projectWebRoot = resolveProjectWebRoot(servletContext);
        if (projectWebRoot != null) {
            return projectWebRoot.resolve(webRelativeDir.replace("/", File.separator));
        }
        return resolveProjectWebRootFallback().resolve(webRelativeDir.replace("/", File.separator));
    }

    // Resolve the source project web folder that developers usually inspect in the IDE.
    private static Path resolveProjectWebRoot(ServletContext servletContext) {
        if (servletContext != null) {
            String runtimeRoot = servletContext.getRealPath("/");
            if (!ValidationUtil.isBlank(runtimeRoot)) {
                Path runtimeRootPath = Path.of(runtimeRoot).normalize();
                Path parent = runtimeRootPath.getParent();
                if (parent != null) {
                    Path projectRoot = parent.getParent();
                    if (projectRoot != null) {
                        Path candidate = projectRoot.resolve("web");
                        if (Files.exists(candidate)) {
                            return candidate;
                        }
                    }
                }
            }
        }

        return null;
    }

    // Fallback project web root when runtime root cannot be used to infer the source folder.
    private static Path resolveProjectWebRootFallback() {
        File projectWebDir = new File(System.getProperty("user.dir", "."), "web");
        return projectWebDir.toPath();
    }

    // Build the full set of runtime and source files that should receive the upload.
    private static Set<Path> resolveTargetFiles(ServletContext servletContext, String webRelativeDir, String fileName) {
        Set<Path> targetFiles = new LinkedHashSet<>();

        Path runtimeDirectory = resolveTargetDirectory(servletContext, webRelativeDir);
        if (runtimeDirectory != null) {
            targetFiles.add(runtimeDirectory.resolve(fileName));
        }

        Path projectWebRoot = resolveProjectWebRoot(servletContext);
        if (projectWebRoot != null) {
            targetFiles.add(projectWebRoot.resolve(webRelativeDir.replace("/", File.separator)).resolve(fileName));
        } else {
            targetFiles.add(resolveProjectWebRootFallback().resolve(webRelativeDir.replace("/", File.separator)).resolve(fileName));
        }
        return targetFiles;
    }
}
