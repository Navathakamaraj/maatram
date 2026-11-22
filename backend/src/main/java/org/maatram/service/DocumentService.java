package org.maatram.service;

import org.maatram.dto.PaginatedResponse;
import org.maatram.exception.ApiException;
import org.maatram.model.Application;
import org.maatram.model.DocumentRecord;
import org.maatram.repository.DataStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DocumentService {

    private final DataStore dataStore;
    private static final List<String> ALLOWED_TYPES = List.of("application/pdf", "image/jpeg", "image/png");

    public DocumentService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public PaginatedResponse<Map<String, Object>> listDocuments(String uploadStatus, String search, int page, int pageSize) {
        String sLower = search != null ? search.trim().toLowerCase() : null;

        List<Map<String, Object>> rows = new ArrayList<>();
        for (DocumentRecord d : dataStore.documents) {
            if (uploadStatus != null && !uploadStatus.equalsIgnoreCase(d.getUpload_status())) continue;

            String owner = dataStore.personName(d.getPerson_id());
            String appNum = dataStore.applications.stream()
                    .filter(a -> a.getId().equals(d.getApplication_id()))
                    .findFirst()
                    .map(Application::getApplication_number)
                    .orElse("-");

            if (sLower != null && !sLower.isEmpty()) {
                boolean match = owner.toLowerCase().contains(sLower) ||
                        d.getFile_name().toLowerCase().contains(sLower) ||
                        appNum.toLowerCase().contains(sLower);
                if (!match) continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("person_id", d.getPerson_id());
            map.put("application_id", d.getApplication_id());
            map.put("file_name", d.getFile_name());
            map.put("content_type", d.getContent_type());
            map.put("file_size", d.getFile_size());
            map.put("storage_key", d.getStorage_key());
            map.put("upload_status", d.getUpload_status());
            map.put("uploaded_at", d.getUploaded_at());
            map.put("owner", owner);
            map.put("application_number", appNum);
            rows.add(map);
        }

        rows.sort((a, b) -> Long.compare((Long) b.get("id"), (Long) a.get("id")));
        return PaginatedResponse.of(rows, page, pageSize);
    }

    public Map<String, Object> presignUpload(String contentType, long fileSize) {
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ApiException(415, "File type " + (contentType != null ? contentType : "unknown") + " is not allowed (PDF, JPG, PNG only)");
        }
        if (fileSize > 5 * 1024 * 1024) {
            throw new ApiException(413, "File is larger than 5 MB");
        }

        return Map.of(
                "upload_url", "http://localhost:9000/maatram-docs/…?X-Amz-Expires=300",
                "expires_in", 300
        );
    }
}
