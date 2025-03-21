package ma.estfbs.pfe_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import ma.estfbs.pfe_management.model.*;

@Configuration
public class RestConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        // Expose entity IDs in the REST API
        config.exposeIdsFor(
            Binome.class,
            DemandeBinome.class,
            DocumentsEvaluation.class,
            Etudiant.class,
            Filiere.class,
            NoteFinale.class,
            NoteSoutenance.class,
            Pourcentage.class,
            ProposerSujets.class,
            Rapport.class,
            Salle.class,
            Soutenance.class,
            Sujet.class,
            Utilisateur.class
        );
        
        // Set the base path for all REST endpoints
        config.setBasePath("/api");
        
        // Configure CORS
        cors.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
            .allowCredentials(false)
            .maxAge(3600);
    }
}
