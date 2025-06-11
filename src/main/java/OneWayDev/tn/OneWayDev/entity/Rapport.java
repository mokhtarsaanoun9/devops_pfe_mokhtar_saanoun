package OneWayDev.tn.OneWayDev.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String rapportName;
    private String fileName;
    private LocalDateTime uploadDate;
    @ManyToOne
    private User uploadBy;


}
