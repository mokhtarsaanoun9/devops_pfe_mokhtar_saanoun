package OneWayDev.tn.OneWayDev.controller;

import OneWayDev.tn.OneWayDev.Repository.UserRepository;
import OneWayDev.tn.OneWayDev.Service.FileService;
import OneWayDev.tn.OneWayDev.Service.RapportService;
import OneWayDev.tn.OneWayDev.Service.UserService;
import OneWayDev.tn.OneWayDev.dto.request.ChangePasswordRequest;
import OneWayDev.tn.OneWayDev.dto.request.CustomErrorResponse;
import OneWayDev.tn.OneWayDev.dto.request.ProfileRequest;
import OneWayDev.tn.OneWayDev.entity.Rapport;
import OneWayDev.tn.OneWayDev.entity.User;
import OneWayDev.tn.OneWayDev.exception.NotFoundExecption;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rapports")
@CrossOrigin("*")
@Validated
public class RapportController {

    private final FileService fileService;
    private final RapportService rapportService;
    @PostMapping("/upload")
    public ResponseEntity<?> uploadRapport(@RequestParam("file") MultipartFile file,
                                           @RequestParam("rapportName") String rapportName) {
        String fileName = fileService.uploadFile(file);
        Rapport rapport = rapportService.save(rapportName, fileName);
        return ResponseEntity.ok(rapport);
    }

    @GetMapping
    public ResponseEntity<List<Rapport>> getAllRapports() {
        return ResponseEntity.ok(rapportService.getAll());
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws IOException {
        Path path = Paths.get("src/main/resources/upload/" + fileName);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRapport(@PathVariable Long id) {
        rapportService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
