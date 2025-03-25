package ma.estfbs.pfe_management.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.dto.BinomeManagementDTOs.BinomeAddRequest;
import ma.estfbs.pfe_management.dto.BinomeManagementDTOs.BinomeDTO;
import ma.estfbs.pfe_management.dto.BinomeManagementDTOs.BinomeEditRequest;
import ma.estfbs.pfe_management.dto.BinomeManagementDTOs.BinomeManagementResponse;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.CompteAddRequest;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.CompteDTO;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.CompteEditRequest;
import ma.estfbs.pfe_management.dto.CompteManagementDTOs.CompteManagementResponse;
import ma.estfbs.pfe_management.dto.FiliereDTO;
import ma.estfbs.pfe_management.dto.HODDashboardDTOs.ActivityDTO;
import ma.estfbs.pfe_management.dto.HODDashboardDTOs.DashboardStatsDTO;
import ma.estfbs.pfe_management.dto.HODDashboardDTOs.UpcomingSoutenanceDTO;
import ma.estfbs.pfe_management.dto.NoteManagementDTOs.NoteDTO;
import ma.estfbs.pfe_management.dto.NoteManagementDTOs.NoteManagementResponse;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.SoutenanceAddRequest;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.SoutenanceDTO;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.SoutenanceUpdateRequest;
import ma.estfbs.pfe_management.dto.SoutenanceManagementDTOs.ValidationResponse;
import ma.estfbs.pfe_management.dto.SujetDTO;
import ma.estfbs.pfe_management.dto.SujetManagementResponse;
import ma.estfbs.pfe_management.dto.SujetRequestDTOs.SujetAddRequest;
import ma.estfbs.pfe_management.dto.SujetRequestDTOs.SujetEditRequest;
import ma.estfbs.pfe_management.dto.SujetSuggestionDTO;
import ma.estfbs.pfe_management.model.Utilisateur.Role;
import ma.estfbs.pfe_management.service.BinomeManagementService;
import ma.estfbs.pfe_management.service.CompteManagementService;
import ma.estfbs.pfe_management.service.HODDashboardService;
import ma.estfbs.pfe_management.service.NoteManagementService;
import ma.estfbs.pfe_management.service.SoutenanceManagementService;
import ma.estfbs.pfe_management.service.SujetManagementService;
import ma.estfbs.pfe_management.service.SujetSuggestionService;

@RestController
@RequestMapping("/api/chef_de_departement")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CHEF_DE_DEPARTEMENT')")
public class ChefDepartementController {

    private final SujetManagementService sujetManagementService;
    private final SujetSuggestionService sujetSuggestionService;
    private final BinomeManagementService binomeManagementService;
    private final CompteManagementService compteManagementService;
    private final SoutenanceManagementService soutenanceManagementService;
    private final NoteManagementService noteManagementService;
    private final HODDashboardService hodDashboardService;
    
    // ============= SUJET MANAGEMENT ENDPOINTS =============
    
    /**
     * Get all subjects and filieres for subject management page
     */
    @GetMapping("/sujets")
    public ResponseEntity<SujetManagementResponse> getAllSujetsAndFilieres() {
        return ResponseEntity.ok(sujetManagementService.getAllSujetsAndFilieres());
    }
    
    /**
     * Add new subject
     */
    @PostMapping("/sujets")
    public ResponseEntity<SujetDTO> addSujet(@RequestBody SujetAddRequest request) {
        return ResponseEntity.ok(sujetManagementService.addSujet(request));
    }
    
    /**
     * Edit subject (only title, theme, description)
     */
    @PutMapping("/sujets/{id}")
    public ResponseEntity<SujetDTO> editSujet(
            @PathVariable Long id,
            @RequestBody SujetEditRequest request) {
        return ResponseEntity.ok(sujetManagementService.editSujet(id, request));
    }
    
    /**
     * Delete subject
     */
    @DeleteMapping("/sujets/{id}")
    public ResponseEntity<Void> deleteSujet(@PathVariable Long id) {
        sujetManagementService.deleteSujet(id);
        return ResponseEntity.ok().build();
    }
    
    // ============= SUJET SUGGESTIONS ENDPOINTS =============
    
    /**
     * Get all sujet suggestions
     */
    @GetMapping("/sujet-suggestions")
    public ResponseEntity<List<SujetSuggestionDTO>> getAllSuggestions() {
        return ResponseEntity.ok(sujetSuggestionService.getAllSuggestions());
    }
    
    /**
     * Accept a sujet suggestion
     */
    @PostMapping("/sujet-suggestions/{id}/accept")
    public ResponseEntity<Void> acceptSuggestion(@PathVariable Long id) {
        sujetSuggestionService.acceptSuggestion(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Reject a sujet suggestion
     */
    @PostMapping("/sujet-suggestions/{id}/reject")
    public ResponseEntity<Void> rejectSuggestion(@PathVariable Long id) {
        sujetSuggestionService.rejectSuggestion(id);
        return ResponseEntity.ok().build();
    }
    
    // ============= BINOME MANAGEMENT ENDPOINTS =============
    
    /**
     * Get binome management data
     */
    @GetMapping("/binomes")
    public ResponseEntity<BinomeManagementResponse> getBinomeManagementData(
            @RequestParam(required = false) Long filiereId) {
        return ResponseEntity.ok(binomeManagementService.getBinomeManagementData(filiereId));
    }
    
    /**
     * Add a new binome
     */
    @PostMapping("/binomes")
    public ResponseEntity<BinomeDTO> addBinome(@RequestBody BinomeAddRequest request) {
        return ResponseEntity.ok(binomeManagementService.addBinome(request));
    }
    
    /**
     * Edit a binome's encadrant
     */
    @PutMapping("/binomes/{id}")
    public ResponseEntity<BinomeDTO> editBinome(
            @PathVariable Long id,
            @RequestBody BinomeEditRequest request) {
        return ResponseEntity.ok(binomeManagementService.editBinome(id, request));
    }
    
    /**
     * Delete a binome
     */
    @DeleteMapping("/binomes/{id}")
    public ResponseEntity<Void> deleteBinome(@PathVariable Long id) {
        binomeManagementService.deleteBinome(id);
        return ResponseEntity.ok().build();
    }
    
    // ============= COMPTE MANAGEMENT ENDPOINTS =============
    
    /**
     * Get accounts by role
     */
    @GetMapping("/comptes")
    public ResponseEntity<CompteManagementResponse> getComptesByRole(
            @RequestParam(required = false) Role role) {
        return ResponseEntity.ok(compteManagementService.getComptesByRole(role));
    }
    
    /**
     * Add a new account
     */
    @PostMapping("/comptes")
    public ResponseEntity<CompteDTO> addCompte(@RequestBody CompteAddRequest request) {
        return ResponseEntity.ok(compteManagementService.addCompte(request));
    }
    
    /**
     * Edit an account
     */
    @PutMapping("/comptes/{id}")
    public ResponseEntity<CompteDTO> editCompte(
            @PathVariable Long id,
            @RequestBody CompteEditRequest request) {
        return ResponseEntity.ok(compteManagementService.editCompte(id, request));
    }
    
    /**
     * Delete an account
     */
    @DeleteMapping("/comptes/{id}")
    public ResponseEntity<Void> deleteCompte(@PathVariable Long id) {
        compteManagementService.deleteCompte(id);
        return ResponseEntity.ok().build();
    }
    
    // ============= SOUTENANCE MANAGEMENT ENDPOINTS =============
    
    /**
     * Get all soutenances
     */
    @GetMapping("/soutenances")
    public ResponseEntity<List<SoutenanceDTO>> getAllSoutenances() {
        return ResponseEntity.ok(soutenanceManagementService.getAllSoutenances());
    }
    
    /**
     * Get soutenance by ID
     */
    @GetMapping("/soutenances/{id}")
    public ResponseEntity<SoutenanceDTO> getSoutenanceById(@PathVariable Long id) {
        return ResponseEntity.ok(soutenanceManagementService.getSoutenanceById(id));
    }
    
    /**
     * Schedule a new soutenance
     */
    @PostMapping("/soutenances")
    public ResponseEntity<SoutenanceDTO> scheduleSoutenance(@RequestBody SoutenanceAddRequest request) {
        return ResponseEntity.ok(soutenanceManagementService.addSoutenance(request));
    }
    
    /**
     * Update an existing soutenance
     */
    @PutMapping("/soutenances/{id}")
    public ResponseEntity<SoutenanceDTO> updateSoutenance(
            @PathVariable Long id,
            @RequestBody SoutenanceUpdateRequest request) {
        return ResponseEntity.ok(soutenanceManagementService.updateSoutenance(id, request));
    }
    
    /**
     * Delete a soutenance
     */
    @DeleteMapping("/soutenances/{id}")
    public ResponseEntity<Void> deleteSoutenance(@PathVariable Long id) {
        soutenanceManagementService.deleteSoutenance(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Validate a soutenance request (dry run)
     */
    @PostMapping("/soutenances/validate")
    public ResponseEntity<ValidationResponse> validateSoutenanceRequest(
            @RequestBody SoutenanceAddRequest request) {
        ValidationResponse response = soutenanceManagementService.validateSoutenanceRequest(request, null);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Validate a soutenance update request (dry run)
     */
    @PostMapping("/soutenances/{id}/validate")
    public ResponseEntity<ValidationResponse> validateSoutenanceUpdateRequest(
            @PathVariable Long id,
            @RequestBody SoutenanceUpdateRequest request) {
        ValidationResponse response = soutenanceManagementService.validateSoutenanceRequest(request, id);
        return ResponseEntity.ok(response);
    }
    
    // ============= NOTE MANAGEMENT ENDPOINTS =============
    
    /**
     * Get all filieres
     */
    @GetMapping("/filieres")
    public ResponseEntity<List<FiliereDTO>> getAllFilieres() {
        return ResponseEntity.ok(noteManagementService.getAllFilieres());
    }

    /**
     * Get all student notes with filieres and pourcentages
     */
    @GetMapping("/notes")
    public ResponseEntity<NoteManagementResponse> getAllNotes() {
        return ResponseEntity.ok(noteManagementService.getAllNotesWithFilieres());
    }

    /**
     * Get notes filtered by filiere
     */
    @GetMapping("/notes/filiere/{filiereId}")
    public ResponseEntity<List<NoteDTO>> getNotesByFiliere(@PathVariable Long filiereId) {
        return ResponseEntity.ok(noteManagementService.getNotesByFiliere(filiereId));
    }

    // ============= DASHBOARD ENDPOINTS =============

    /**
     * Get dashboard statistics
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(hodDashboardService.getDashboardStats());
    }

    /**
     * Get upcoming soutenances for the dashboard
     */
    @GetMapping("/dashboard/upcoming-soutenances")
    public ResponseEntity<List<UpcomingSoutenanceDTO>> getUpcomingSoutenances() {
        return ResponseEntity.ok(hodDashboardService.getUpcomingSoutenances());
    }

    /**
     * Get recent activities for the dashboard
     */
    @GetMapping("/dashboard/activities")
    public ResponseEntity<List<ActivityDTO>> getRecentActivities() {
        return ResponseEntity.ok(hodDashboardService.getRecentActivities());
    }
}