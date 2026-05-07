package com.github.quynj.quynjclaw.application;

import com.github.quynj.quynjclaw.common.IdGenerator;
import com.github.quynj.quynjclaw.config.QuynjClawProperties;
import com.github.quynj.quynjclaw.dto.FileAttachmentDTO;
import com.github.quynj.quynjclaw.dto.MessageAttachmentDTO;
import io.agentscope.core.message.Base64Source;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class LocalFileService {
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif");

    private final QuynjClawProperties properties;
    private final ConversationService conversationService;

    public LocalFileService(QuynjClawProperties properties, ConversationService conversationService) {
        this.properties = properties;
        this.conversationService = conversationService;
    }

    public FileAttachmentDTO saveImage(String sessionId, MultipartFile file) {
        conversationService.get(sessionId);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only PNG, JPEG, WebP, and GIF images are supported.");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Image file must be 10 MB or smaller.");
        }

        String id = IdGenerator.fileId();
        String extension = extensionFor(contentType, file.getOriginalFilename());
        String safeOriginal = safeFileName(file.getOriginalFilename());
        String storedName = id + extension;
        Path path = filesDir(sessionId).resolve(storedName);
        try {
            Files.createDirectories(path.getParent());
            file.transferTo(path);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save image file.", e);
        }

        FileAttachmentDTO attachment = new FileAttachmentDTO();
        attachment.id = id;
        attachment.sessionId = sessionId;
        attachment.type = "image";
        attachment.fileName = safeOriginal.isBlank() ? storedName : safeOriginal;
        attachment.contentType = contentType;
        attachment.size = file.getSize();
        attachment.url = fileUrl(sessionId, id);
        return attachment;
    }

    public FileAttachmentDTO findImage(String sessionId, String fileId) {
        conversationService.get(sessionId);
        Path path = findStoredFile(sessionId, fileId);
        FileAttachmentDTO attachment = new FileAttachmentDTO();
        attachment.id = fileId;
        attachment.sessionId = sessionId;
        attachment.type = "image";
        attachment.fileName = path.getFileName().toString();
        attachment.contentType = contentType(path);
        attachment.size = size(path);
        attachment.url = fileUrl(sessionId, fileId);
        return attachment;
    }

    public Path imagePath(String sessionId, String fileId) {
        conversationService.get(sessionId);
        return findStoredFile(sessionId, fileId);
    }

    public Base64Source base64Source(String sessionId, MessageAttachmentDTO attachment) {
        if (attachment == null || attachment.id == null || attachment.id.isBlank()) {
            throw new IllegalArgumentException("Attachment id is required.");
        }
        if (attachment.type != null && !"image".equals(attachment.type)) {
            throw new IllegalArgumentException("Only image attachments are supported.");
        }
        Path path = imagePath(sessionId, attachment.id);
        try {
            return Base64Source.builder()
                    .mediaType(contentType(path))
                    .data(Base64.getEncoder().encodeToString(Files.readAllBytes(path)))
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read image attachment.", e);
        }
    }

    public void deleteSessionFiles(String sessionId) {
        Path dir = filesDir(sessionId);
        if (!Files.exists(dir)) {
            return;
        }
        try (var paths = Files.walk(dir)) {
            List<Path> ordered = paths.sorted(Comparator.reverseOrder()).toList();
            for (Path path : ordered) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete session files for " + sessionId, e);
        }
    }

    private Path findStoredFile(String sessionId, String fileId) {
        if (fileId == null || !fileId.matches("[A-Za-z0-9_\\-]+")) {
            throw new IllegalArgumentException("Invalid file id.");
        }
        Path dir = filesDir(sessionId);
        try (var paths = Files.list(dir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(fileId + "."))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));
        } catch (IOException e) {
            throw new IllegalArgumentException("File not found: " + fileId, e);
        }
    }

    private Path filesDir(String sessionId) {
        return Path.of(properties.baseDir, "files", sessionId).normalize();
    }

    private String fileUrl(String sessionId, String fileId) {
        return "/api/sessions/" + encode(sessionId) + "/files/" + encode(fileId);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    }

    private String extensionFor(String contentType, String originalName) {
        String original = safeFileName(originalName).toLowerCase(Locale.ROOT);
        if (original.endsWith(".png") || original.endsWith(".jpg") || original.endsWith(".jpeg")
                || original.endsWith(".webp") || original.endsWith(".gif")) {
            return original.substring(original.lastIndexOf('.'));
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".img";
        };
    }

    private String safeFileName(String name) {
        if (name == null) {
            return "";
        }
        return Path.of(name).getFileName().toString().replaceAll("[\\r\\n]", "").trim();
    }

    private String contentType(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (name.endsWith(".webp")) {
            return "image/webp";
        }
        if (name.endsWith(".gif")) {
            return "image/gif";
        }
        try {
            String detected = Files.probeContentType(path);
            return detected == null ? "application/octet-stream" : detected;
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    private long size(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return 0;
        }
    }
}
