package ma.estfbs.pfe_management.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import ma.estfbs.pfe_management.model.AnneeScolaire;
import ma.estfbs.pfe_management.repository.AnneeScolaireRepository;

@Service
@RequiredArgsConstructor
public class AcademicYearService {
    
    private final AnneeScolaireRepository anneeScolaireRepository;
    
    /**
     * Get the current academic year
     */
    public AnneeScolaire getCurrentAcademicYear() {
        return anneeScolaireRepository.findByCourante(true)
            .orElseThrow(() -> new RuntimeException("No current academic year defined in the system"));
    }
    
    /**
     * Get the ID of the current academic year
     */
    public Long getCurrentAcademicYearId() {
        return getCurrentAcademicYear().getId();
    }
}