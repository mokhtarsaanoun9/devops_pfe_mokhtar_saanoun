package OneWayDev.tn.OneWayDev.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

@Service
public class FileService {
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException(" File  is required ,should select a File");
        }
        long currentTimestampSeconds = Instant.now().getEpochSecond();
        String filename = currentTimestampSeconds+ StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (filename.contains("..")) {
                throw new IllegalArgumentException("Cannot upload file with relative path outside current directory");
            }
            Path uploadDir = Paths.get("src/main/resources/upload");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            return filename;

        } catch (Exception e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }
}
