package com.github.quynj.quynjclaw.api;

import com.github.quynj.quynjclaw.application.LocalFileService;
import com.github.quynj.quynjclaw.common.Result;
import com.github.quynj.quynjclaw.dto.FileAttachmentDTO;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/sessions/{sessionId}/files")
public class FileController {
    private final LocalFileService fileService;

    public FileController(LocalFileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public Result<FileAttachmentDTO> upload(@PathVariable String sessionId, @RequestParam("file") MultipartFile file) {
        return Result.ok(fileService.saveImage(sessionId, file));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileSystemResource> get(@PathVariable String sessionId, @PathVariable String fileId) {
        FileAttachmentDTO attachment = fileService.findImage(sessionId, fileId);
        Path path = fileService.imagePath(sessionId, fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.contentType))
                .body(new FileSystemResource(path));
    }
}
