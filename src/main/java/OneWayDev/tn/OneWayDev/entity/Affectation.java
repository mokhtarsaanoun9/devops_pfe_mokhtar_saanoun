package OneWayDev.tn.OneWayDev.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Affectation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titre;
    @ManyToOne
    private User utilisateur;

    @ManyToOne
    @JsonIgnore
    private Mission mission;

    @Enumerated(EnumType.STRING)
    private StatutAffectation statut;

}
