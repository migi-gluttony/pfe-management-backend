USE pfe_management;

-- Insert into filiere (departments)
INSERT INTO filliere (id, nom) VALUES 
(1, 'Informatique'),
(2, 'Mathématiques'),
(3, 'Physique');

-- Insert into etudiant (linking students to their department)
-- Using ID 1 (Karim Benomar) as student
INSERT INTO etudiant (id, filiere_id) VALUES 
(1, 1);  -- Karim Benomar in Informatique

-- Insert into sujet (topics)
INSERT INTO sujet (id, titre, theme, description, filiere_id) VALUES 
(1, 'Système de gestion des PFE', 'Web Development', 'Développement d''un système pour gérer les projets de fin d''études', 1),
(2, 'Analyse des algorithmes d''apprentissage automatique', 'Machine Learning', 'Comparaison des performances des algorithmes de ML sur des ensembles de données variés', 1),
(3, 'Modélisation mathématique des phénomènes de diffusion', 'Mathématiques appliquées', 'Étude théorique et numérique des équations de diffusion', 2);

-- Insert into binome (student pairs with supervisor and topic)
INSERT INTO binome (id, etudiant1_id, etudiant2_id, encadrant_id, sujet_id) VALUES 
(1, 1, NULL, 4, 1);  -- Karim Benomar supervised by Souad Benali on the PFE management system

-- Insert into salle (rooms)
INSERT INTO salle (id, nom) VALUES 
(1, 'Salle A101'),
(2, 'Salle B202'),
(3, 'Salle C303');

-- Insert into soutenance (defense sessions)
INSERT INTO soutenance (id, date, heure, salle_id, binome_id, jury1_id, jury2_id) VALUES 
(1, '2025-06-15', '10:00:00', 1, 1, 2, 9);  -- Defense for Karim Benomar's project with Mohammed El Alami and Laila Hakimi as jury

-- Insert into note_soutenance (defense evaluation)
INSERT INTO note_soutenance (id, jury_id, note) VALUES 
(1, 2, 16),  -- Mohammed El Alami gives a score of 16
(2, 9, 17);  -- Laila Hakimi gives a score of 17

-- Insert into rapport (reports)
INSERT INTO rapport (id, binome_id, titre, localisation_rapport, note, commentaire) VALUES 
(1, 1, 'Rapport final - Système de gestion PFE', '/uploads/rapports/rapport_1.pdf', 15, 'Bon rapport, bonne structure et méthodologie claire.');

-- Insert into demande_binome (pair formation requests)
INSERT INTO demande_binome (id, demandeur_id, demande_id, statut) VALUES 
(1, 1, 1, 'EN_ATTENTE');  -- Self-request (just as a placeholder)

-- Insert into documents_evaluation (evaluation documents)
INSERT INTO documents_evaluation (id, binome_id, localisation_doc, commentaire) VALUES 
(1, 1, '/uploads/evaluations/evaluation_1.pdf', 'Documentation complète du système développé');

-- Insert into pourcentage (weighting for final grades)
INSERT INTO pourcentage (id, pourcentage_rapport, pourcentage_soutenance, pourcentage_encadrant) VALUES 
(1, 30, 40, 30);  -- 30% report, 40% defense, 30% supervisor evaluation

-- Insert into note_finale (final grades)
INSERT INTO note_finale (id, etudiant_id, note_rapport, note_soutenance) VALUES 
(1, 1, 15, 17);  -- Final grades for Karim Benomar

-- Insert into proposer_sujets (topic proposals)
INSERT INTO proposer_sujets (id, binome_proposer_par_id, titre, theme, description, status) VALUES 
(1, 1, 'Développement d''une application mobile pour l''orientation universitaire', 'Mobile Development', 'Application qui aide les lycéens à choisir leur parcours universitaire en fonction de leurs intérêts et capacités', 'EN_ATTENTE');