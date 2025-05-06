package org.example.domain.message;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ApplicationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.example.common.MessageApplicationError.CANNOT_STORE_FILE;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {
    private final BlobServiceClient blobServiceClient;
    @Value("${azure.storage.container-name}")
    private String containerName;
    @Value("${azure.storage.cdn-base-url}")
    private String cdnBaseUrl;
    private final static String BLOB_NAME = "files/";


    public String storeFile(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + getFileExtension(file.getOriginalFilename());

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(BLOB_NAME + filename);

            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType());

            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);

            return cdnBaseUrl + "/" + BLOB_NAME + filename;
        } catch (IOException e) {
            log.info("Failed to store file: ", e);
            throw new ApplicationException(CANNOT_STORE_FILE);
        }
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse("");
    }
}
