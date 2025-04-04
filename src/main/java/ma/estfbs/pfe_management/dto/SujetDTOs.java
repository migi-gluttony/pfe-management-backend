package ma.estfbs.pfe_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class SujetDTOs {

    /**
     * Response DTO for available subjects
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableSujetsResponse {
        private List<SujetDTO> availableSujets;
    }

    /**
     * Request DTO for selecting a subject
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectSujetRequest {
        private Long sujetId;
    }

    /**
     * Response DTO after selecting a subject
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BinomeSujetResponse {
        private BinomeDTO binome;
        private SujetDTO sujet;
    }

    /**
     * Request DTO for proposing a new subject
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposerSujetRequest {
        private String titre;
        private String theme;
        private String description;
    }

    /**
     * DTO for subject data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SujetDTO {
        private Long id;
        private String titre;
        private String theme;
        private String description;
    }

    /**
     * DTO for subject proposition (extending SujetDTO)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropositionDTO {
        private Long id;
        private String titre;
        private String theme;
        private String description;
        private String statut;
    }

    /**
     * DTO for binome data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BinomeDTO {
        private Long id;
        private EtudiantDTO etudiant1;
        private EtudiantDTO etudiant2;
        private EncadrantDTO encadrant;
        private SujetDTO sujet;
    }

    /**
     * DTO for student data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EtudiantDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
    }

    /**
     * DTO for supervisor data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EncadrantDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
    }
}