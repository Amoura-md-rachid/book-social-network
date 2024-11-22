package com.amoura.book.book;


import com.amoura.book.common.PageResponse;
import com.amoura.book.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public Integer save(BookRequest request, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Book book = bookMapper.toBook(request);
        book.setOwner(user);
        return bookRepository.save(book).getId();

    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, user.getId());
        List<BookResponse> bookResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    /**
     * Récupère tous les livres appartenant à un utilisateur spécifique.
     * <p>
     * Cette méthode effectue une recherche paginée des livres en fonction de l'utilisateur actuellement connecté.
     * Elle utilise une spécification JPA pour filtrer les livres par l'ID du propriétaire, puis mappe
     * les entités {@link Book} en objets {@link BookResponse}.
     * </p>
     *
     * @param page          le numéro de la page à récupérer (commence à 0)
     * @param size          le nombre d'éléments par page
     * @param connectedUser l'utilisateur actuellement connecté, récupéré via l'authentification
     * @return une instance de {@link PageResponse} contenant les livres paginés et leurs métadonnées
     * @throws ClassCastException si l'objet principal de l'authentification n'est pas de type {@link User}
     * @implNote
     * <ul>
     *     <li>La pagination est gérée par {@link PageRequest} avec un tri descendant sur la date de création des livres.</li>
     *     <li>La méthode utilise {@link BookSpecification withOwnerId(String)} pour construire la condition de recherche.</li>
     *     <li>Les entités {@link Book} sont transformées en objets {@link BookResponse} à l'aide de {@link BookMapper#toBookResponse(Book)}.</li>
     * </ul>
     *
     * <h3>Exemple de requête :</h3>
     * <pre>
     * PageResponse<BookResponse> books = findAllBooksByOwner(0, 10, authentication);
     * books.getContent().forEach(book -> System.out.println(book.getTitle()));
     * </pre>
     */
    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        // Récupération de l'utilisateur connecté
        User user = ((User) connectedUser.getPrincipal());

        // Création des paramètres de pagination avec tri sur la date de création
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        // Recherche paginée des livres avec une spécification JPA
        Page<Book> books = bookRepository.findAll(BookSpecification.withOwnerId(user.getId()), pageable);

        // Mapping des entités Book en objets BookResponse
        List<BookResponse> bookResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();

        // Création de la réponse paginée
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

}
