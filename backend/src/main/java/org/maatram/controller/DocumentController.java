package org.maatram.controller;

import org.maatram.dto.PaginatedResponse;
import org.maatram.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/admin/documents")
    public ResponseEntity<PaginatedResponse<Map<String, Object>>> listDocuments(
            @RequestParam(required = false) String upload_status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ResponseEntity.ok(documentService.listDocuments(upload_status, search, page, pageSize));
    }

    @PostMapping("/uploads/presign")
    public ResponseEntity<Map<String, Object>> presignUpload(@RequestBody Map<String, Object> body) {
        String contentType = (String) body.get("content_type");
        Number fileSizeNum = (Number) body.get("file_size");
        long fileSize = fileSizeNum != null ? fileSizeNum.longValue() : 0L;
        return ResponseEntity.ok(documentService.presignUpload(contentType, fileSize));
    }
}
