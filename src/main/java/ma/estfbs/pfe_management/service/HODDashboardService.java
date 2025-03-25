package ma.estfbs.pfe_management.service;

import java.text.SimpleDateFormat;
import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.HODDashboardDTOs.ActivityDTO;
import ma.estfbs.pfe_management.dto.HODDashboardDTOs.BinomeDTO;
import ma.estfbs.pfe_management.dto.HODDashboardDTOs.DashboardStatsDTO;
import ma.estfbs.pfe_management.dto.HODDashboardDTOs.FiliereStatsDTO;
import ma.estfbs.pfe_management.dto.HODDashboardDTOs.SalleDTO;
import ma.estfbs.pfe_management.dto.HODDashboardDTOs.SujetDTO;
import ma.estfbs.pfe_management.dto.HODDashboardDTOs.UpcomingSoutenanceDTO;
import ma.estfbs.pfe_management.dto.HODDashboardDTOs.UserDTO;
import ma.estfbs.pfe_management.model.Binome;
import ma.estfbs.pfe_management.model.Etudiant;
import ma.estfbs.pfe_management.model.Filiere;
import ma.estfbs.pfe_management.model.NoteFinale;
import ma.estfbs.pfe_management.model.ProposerSujets;
import ma.estfbs.pfe_management.model.Soutenance;
import ma.estfbs.pfe_management.model.Utilisateur;
import ma.estfbs.pfe_management.model.Utilisateur.Role;
import ma.estfbs.pfe_management.repository.BinomeRepository;
import ma.estfbs.pfe_management.repository.EtudiantRepository;
import ma.estfbs.pfe_management.repository.FiliereRepository;
import ma.estfbs.pfe_management.repository.NoteFinaleRepository;
import ma.estfbs.pfe_management.repository.ProposerSujetsRepository;
import ma.estfbs.pfe_management.repository.SoutenanceRepository;
import ma.estfbs.pfe_management.repository.SujetRepository;
import ma.estfbs.pfe_management.repository.UtilisateurRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HODDashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final BinomeRepository binomeRepository;
    private final SujetRepository sujetRepository;
    private final ProposerSujetsRepository proposerSujetsRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final NoteFinaleRepository noteFinaleRepository;
    private final FiliereRepository filiereRepository;
    private final EtudiantRepository etudiantRepository;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Get dashboard statistics for the chef de département
     */
    public DashboardStatsDTO getDashboardStats() {
        // Get user counts by role, excluding CHEF_DE_DEPARTEMENT accounts
        List<Utilisateur> allUsers = utilisateurRepository.findAll();
        List<Utilisateur> managedUsers = allUsers.stream()
            .filter(u -> u.getRole() != Role.CHEF_DE_DEPARTEMENT && u.getRole() != Role.ADMIN)
            .collect(Collectors.toList());
        
        int totalStudents = (int) managedUsers.stream().filter(u -> u.getRole() == Role.ETUDIANT).count();
        int totalSupervisors = (int) managedUsers.stream().filter(u -> u.getRole() == Role.ENCADRANT).count();
        int totalJuries = (int) managedUsers.stream().filter(u -> u.getRole() == Role.JURY).count();
        
        // Get binome statistics
        List<Binome> allBinomes = binomeRepository.findAll();
        int totalBinomes = allBinomes.size();
        
        // Count binomes with soutenances
        int binomesWithSoutenance = 0;
        for (Binome binome : allBinomes) {
            if (soutenanceRepository.findByBinome(binome).isPresent()) {
                binomesWithSoutenance++;
            }
        }
        
        // Get sujet statistics
        int totalSujets = sujetRepository.findAll().size();
        int pendingSuggestions = proposerSujetsRepository.findByStatus(ProposerSujets.Status.EN_ATTENTE).size();
        
        // Get soutenance statistics
        List<Soutenance> allSoutenances = soutenanceRepository.findAll();
        int totalSoutenances = allSoutenances.size();
        LocalDate today = LocalDate.now();
        int plannedSoutenances = (int) allSoutenances.stream()
                .filter(s -> s.getDate().isAfter(today) || s.getDate().isEqual(today))
                .count();
        int completedSoutenances = totalSoutenances - plannedSoutenances;
        
        // Get grades statistics
        List<NoteFinale> allNotes = noteFinaleRepository.findAll();
        double averageGrade = allNotes.isEmpty() ? 0.0 : 
                allNotes.stream()
                        .mapToDouble(n -> (n.getNoteRapport() + n.getNoteSoutenance()) / 2.0)
                        .average()
                        .orElse(0.0);
        
        // Count grades by range
        int honorsCount = 0;  // >= 16
        int goodCount = 0;    // >= 14 and < 16
        int passCount = 0;    // >= 10 and < 14
        
        for (NoteFinale note : allNotes) {
            double finalGrade = (note.getNoteRapport() + note.getNoteSoutenance()) / 2.0;
            if (finalGrade >= 16) {
                honorsCount++;
            } else if (finalGrade >= 14) {
                goodCount++;
            } else if (finalGrade >= 10) {
                passCount++;
            }
        }
        
        // Get filiere statistics
        List<Filiere> allFilieres = filiereRepository.findAll();
        int totalFilieres = allFilieres.size();
        
        // Create filiere stats DTOs
        List<FiliereStatsDTO> filiereStats = new ArrayList<>();
        for (Filiere filiere : allFilieres) {
            int etudiantCount = etudiantRepository.findByFiliere(filiere).size();
            
            filiereStats.add(FiliereStatsDTO.builder()
                    .id(filiere.getId())
                    .nom(filiere.getNom())
                    .etudiantCount(etudiantCount)
                    .build());
        }
        
        // Sort filieres by student count (descending)
        filiereStats.sort(Comparator.comparing(FiliereStatsDTO::getEtudiantCount).reversed());
        
        // Build and return the DTO
        return DashboardStatsDTO.builder()
                .totalUsers(managedUsers.size())
                .totalStudents(totalStudents)
                .totalSupervisors(totalSupervisors)
                .totalJuries(totalJuries)
                .totalBinomes(totalBinomes)
                .binomesWithSoutenance(binomesWithSoutenance)
                .totalSujets(totalSujets)
                .pendingSuggestions(pendingSuggestions)
                .totalSoutenances(totalSoutenances)
                .plannedSoutenances(plannedSoutenances)
                .completedSoutenances(completedSoutenances)
                .averageGrade(averageGrade)
                .honorsCount(honorsCount)
                .goodCount(goodCount)
                .passCount(passCount)
                .totalFilieres(totalFilieres)
                .filieres(filiereStats)
                .build();
    }

    /**
     * Get upcoming soutenances for the dashboard (next 7 days)
     */
    public List<UpcomingSoutenanceDTO> getUpcomingSoutenances() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        
        List<Soutenance> upcomingSoutenances = soutenanceRepository.findByDateBetween(today, nextWeek);
        
        // Sort by date and time
        upcomingSoutenances.sort(Comparator
                .comparing(Soutenance::getDate)
                .thenComparing(Soutenance::getHeure));
        
        // Take only the first 5 soutenances
        List<Soutenance> limitedSoutenances = upcomingSoutenances.stream()
                .limit(5)
                .collect(Collectors.toList());
        
        // Convert to DTOs
        return limitedSoutenances.stream()
                .map(this::mapToUpcomingSoutenanceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recent activities for the dashboard
     */
    public List<ActivityDTO> getRecentActivities() {
        // This is a placeholder since there's no activity tracking in the current model
        // In a real implementation, you would have an Activity entity and repository
        
        // For now, we'll create sample activities based on recent data
        List<ActivityDTO> activities = new ArrayList<>();
        
        // Add recent soutenances
        LocalDate recentDate = LocalDate.now().minusDays(7);
        List<Soutenance> recentSoutenances = soutenanceRepository.findByDateBetween(recentDate, LocalDate.now());
        
        for (Soutenance soutenance : recentSoutenances) {
            String description = String.format(
                "Soutenance programmée pour %s et %s",
                soutenance.getBinome().getEtudiant1().getPrenom() + " " + soutenance.getBinome().getEtudiant1().getNom(),
                soutenance.getBinome().getEtudiant2() != null 
                    ? soutenance.getBinome().getEtudiant2().getPrenom() + " " + soutenance.getBinome().getEtudiant2().getNom()
                    : ""
            );
            
            activities.add(ActivityDTO.builder()
                    .id(soutenance.getId())
                    .description(description)
                    .timestamp(soutenance.getDate().format(dateFormatter) + " " + soutenance.getHeure())
                    .icon("pi pi-calendar")
                    .type("soutenance")
                    .build());
        }
        
        // Add recent subject suggestions
        List<ProposerSujets> recentSuggestions = proposerSujetsRepository.findAll().stream()
                .limit(3)
                .collect(Collectors.toList());
        
        for (ProposerSujets suggestion : recentSuggestions) {
            String etudiants = suggestion.getBinomeProposerPar().getEtudiant1().getPrenom() + " " + 
                    suggestion.getBinomeProposerPar().getEtudiant1().getNom();
            
            if (suggestion.getBinomeProposerPar().getEtudiant2() != null) {
                etudiants += " et " + suggestion.getBinomeProposerPar().getEtudiant2().getPrenom() + " " + 
                        suggestion.getBinomeProposerPar().getEtudiant2().getNom();
            }
            
            String description = String.format(
                "Nouvelle proposition de sujet \"%s\" par %s",
                suggestion.getTitre(),
                etudiants
            );
            
            activities.add(ActivityDTO.builder()
                    .id(suggestion.getId())
                    .description(description)
                    .timestamp(suggestion.getId().toString()) // Using ID as a proxy for timestamp
                    .icon("pi pi-file")
                    .type("suggestion")
                    .build());
        }
        
        // Add recent user registrations
        List<Utilisateur> recentUsers = utilisateurRepository.findAll().stream()
                .limit(3)
                .collect(Collectors.toList());
        
        for (Utilisateur user : recentUsers) {
            String description = String.format(
                "Nouveau compte %s créé pour %s %s",
                getRoleName(user.getRole()),
                user.getPrenom(),
                user.getNom()
            );
            
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            activities.add(ActivityDTO.builder()
                    .id(user.getId())
                    .description(description)
                    .timestamp(formatter.format(new Date())) // Using current date as a placeholder
                    .icon("pi pi-user-plus")
                    .type("user")
                    .build());
        }
        
        // Sort by most recent first (using id as a proxy for creation date)
        activities.sort(Comparator.comparing(ActivityDTO::getId).reversed());
        
        return activities;
    }
    
    // Helper methods
    private UpcomingSoutenanceDTO mapToUpcomingSoutenanceDTO(Soutenance soutenance) {
        return UpcomingSoutenanceDTO.builder()
                .id(soutenance.getId())
                .date(soutenance.getDate().toString())
                .heure(soutenance.getHeure().toString())
                .salle(SalleDTO.builder()
                        .id(soutenance.getSalle().getId())
                        .nom(soutenance.getSalle().getNom())
                        .build())
                .binome(mapToBinomeDTO(soutenance.getBinome()))
                .jury1(mapToUserDTO(soutenance.getJury1()))
                .jury2(mapToUserDTO(soutenance.getJury2()))
                .build();
    }
    
    private BinomeDTO mapToBinomeDTO(Binome binome) {
        // Get filiere name from etudiant
        String filiereName = null;
        List<Etudiant> etudiants = etudiantRepository.findByUtilisateur(binome.getEtudiant1())
                .map(List::of)
                .orElse(List.of());
        
        if (!etudiants.isEmpty() && etudiants.get(0).getFiliere() != null) {
            filiereName = etudiants.get(0).getFiliere().getNom();
        }
        
        return BinomeDTO.builder()
                .id(binome.getId())
                .etudiant1(mapToUserDTO(binome.getEtudiant1()))
                .etudiant2(binome.getEtudiant2() != null ? mapToUserDTO(binome.getEtudiant2()) : null)
                .encadrant(mapToUserDTO(binome.getEncadrant()))
                .sujet(SujetDTO.builder()
                        .id(binome.getSujet().getId())
                        .titre(binome.getSujet().getTitre())
                        .build())
                .filiereName(filiereName)
                .build();
    }
    
    private UserDTO mapToUserDTO(Utilisateur utilisateur) {
        if (utilisateur == null) return null;
        
        return UserDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .build();
    }
    
    private String getRoleName(Role role) {
        switch (role) {
            case ETUDIANT:
                return "étudiant";
            case ENCADRANT:
                return "encadrant";
            case JURY:
                return "jury";
            case CHEF_DE_DEPARTEMENT:
                return "chef de département";
            default:
                return role.toString().toLowerCase();
        }
    }
}