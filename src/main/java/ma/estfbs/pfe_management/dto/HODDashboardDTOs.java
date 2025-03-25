package ma.estfbs.pfe_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class HODDashboardDTOs {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DashboardStatsDTO {
        // User statistics
        private int totalUsers;
        private int totalStudents;
        private int totalSupervisors;
        private int totalJuries;
        
        // Binome statistics
        private int totalBinomes;
        private int binomesWithSoutenance;
        
        // Sujet statistics
        private int totalSujets;
        private int pendingSuggestions;
        
        // Soutenance statistics
        private int totalSoutenances;
        private int plannedSoutenances;
        private int completedSoutenances;
        
        // Grade statistics
        private double averageGrade;
        private int honorsCount;
        private int goodCount;
        private int passCount;
        
        // Filiere statistics
        private int totalFilieres;
        private List<FiliereStatsDTO> filieres;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FiliereStatsDTO {
        private Long id;
        private String nom;
        private int etudiantCount;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpcomingSoutenanceDTO {
        private Long id;
        private String date;
        private String heure;
        private SalleDTO salle;
        private BinomeDTO binome;
        private UserDTO jury1;
        private UserDTO jury2;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SalleDTO {
        private Long id;
        private String nom;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BinomeDTO {
        private Long id;
        private UserDTO etudiant1;
        private UserDTO etudiant2;
        private UserDTO encadrant;
        private SujetDTO sujet;
        private String filiereName;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDTO {
        private Long id;
        private String nom;
        private String prenom;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SujetDTO {
        private Long id;
        private String titre;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivityDTO {
        private Long id;
        private String description;
        private String timestamp;
        private String icon;
        private String type;
    }
}
