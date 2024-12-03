package com.amoura.book.book;

import org.springframework.data.jpa.domain.Specification;

/**
 * Classe utilitaire pour créer des spécifications dynamiques pour les entités {@link Book}.
 * <p>
 * Cette classe utilise l'API de spécifications de Spring Data JPA pour définir
 * des critères de requête réutilisables et dynamiques.
 * </p>
 */
public class BookSpecification {

    /**
     * Crée une spécification pour filtrer les livres par l'ID du propriétaire.
     * <p>
     * Cette méthode retourne une spécification JPA qui génère une condition SQL
     * équivalente à : <code>WHERE owner.id = :ownerId</code>. Elle est utile
     * pour effectuer des requêtes sur la base de données en fonction du propriétaire
     * des livres.
     * </p>
     *
     * @param ownerId l'identifiant du propriétaire des livres
     * @return une spécification permettant de filtrer les livres par l'ID du propriétaire
     * @implNote La spécification utilise l'API Criteria pour construire le critère
     * de filtre basé sur l'attribut imbriqué {@code owner.id}.
     * @see org.springframework.data.jpa.domain.Specification
     * //@see javax.persistence.criteria.CriteriaBuilder#equal
     */
    public static Specification<Book> withOwnerId(Integer ownerId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("owner").get("id"), ownerId);
    }

}
