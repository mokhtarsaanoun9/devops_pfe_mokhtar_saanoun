package OneWayDev.tn.OneWayDev.Service;

import OneWayDev.tn.OneWayDev.Repository.AffectationRepository;
import OneWayDev.tn.OneWayDev.Repository.MissionRepository;
import OneWayDev.tn.OneWayDev.Repository.RapportRepository;
import OneWayDev.tn.OneWayDev.Repository.UserRepository;
import OneWayDev.tn.OneWayDev.dto.request.MissionCreationDTO;
import OneWayDev.tn.OneWayDev.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RapportService {
    private final RapportRepository rapportRepository;

    public Rapport save(String rapportName, String fileName) {
        Rapport rapport = Rapport.builder()
                .rapportName(rapportName)
                .fileName(fileName)
                .build();
        return rapportRepository.save(rapport);
    }

    public List<Rapport> getAll() {
        return rapportRepository.findAll();
    }
    public void deleteById(Long id) {
        Rapport rapport = rapportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rapport not found"));

        // Delete file from disk
        if (rapport.getFileName() != null) {
            Path filePath = Paths.get("src/main/resources/upload", rapport.getFileName());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete file: " + rapport.getFileName(), e);
            }
        }

        // Delete entity from DB
        rapportRepository.deleteById(id);
    }
}











