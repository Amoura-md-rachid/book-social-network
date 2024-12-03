package com.amoura.book.book;


import com.amoura.book.common.PageResponse;
import com.amoura.book.exception.OperationNotPermittedException;
import com.amoura.book.file.FileStorageService;
import com.amoura.book.history.BookTransactionHistory;
import com.amoura.book.history.BookTransationHistoryRespository;
import com.amoura.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookTransationHistoryRespository transactionHistoryRepository;
    private final FileStorageService fileStorageService;

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
     * @implNote <ul>
     * <li>La pagination est gérée par {@link PageRequest} avec un tri descendant sur la date de création des livres.</li>
     * <li>La méthode utilise {@link BookSpecification withOwnerId(String)} pour construire la condition de recherche.</li>
     * <li>Les entités {@link Book} sont transformées en objets {@link BookResponse} à l'aide de {@link BookMapper#toBookResponse(Book)}.</li>
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

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrorwedBooks = transactionHistoryRepository.findAllBorowedBooks(pageable, user.getId());
        List<BorrowedBookResponse> bookResponse = allBorrorwedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                allBorrorwedBooks.getNumber(),
                allBorrorwedBooks.getSize(),
                allBorrorwedBooks.getTotalElements(),
                allBorrorwedBooks.getTotalPages(),
                allBorrorwedBooks.isFirst(),
                allBorrorwedBooks.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrorwedBooks = transactionHistoryRepository.findAllReturnedBooks(pageable, user.getId());
        List<BorrowedBookResponse> bookResponse = allBorrorwedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                allBorrorwedBooks.getNumber(),
                allBorrorwedBooks.getSize(),
                allBorrorwedBooks.getTotalElements(),
                allBorrorwedBooks.getTotalPages(),
                allBorrorwedBooks.isFirst(),
                allBorrorwedBooks.isLast()
        );
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        User user = ((User) connectedUser.getPrincipal());

        if (!Objects.equals(book.getOwner(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others books shareable status");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;
    }


    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));
        User user = ((User) connectedUser.getPrincipal());

        if (!Objects.equals(book.getOwner(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others books arvhived status");
        }
        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }


    /**
     * Permet à un utilisateur d'emprunter un livre.
     * <p>
     * Cette méthode vérifie si le livre existe, s'il est archivé ou non partageable,
     * si l'utilisateur est le propriétaire du livre, et si le livre est déjà emprunté
     * par l'utilisateur. Si toutes les vérifications sont passées, elle crée un nouvel
     * enregistrement dans l'historique des transactions et retourne l'ID de cet enregistrement.
     *
     * @param bookId        L'ID du livre à emprunter.
     * @param connectedUser L'objet Authentication représentant l'utilisateur connecté.
     * @return L'ID de l'enregistrement dans l'historique des transactions.
     * @throws EntityNotFoundException        Si le livre n'est pas trouvé.
     * @throws OperationNotPermittedException Si le livre est archivé ou non partageable,
     *                                        si l'utilisateur est le propriétaire du livre,
     *                                        ou si le livre est déjà emprunté par l'utilisateur.
     */
    public Integer borrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
        }

        User user = (User) connectedUser.getPrincipal();

        if (Objects.equals(book.getOwner(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }

        final boolean isAlreadyBorrowed = transactionHistoryRepository.isAlreadyBorrowedByUser(bookId, user.getId());

        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException("The requested book is already borrowed");
        }

        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();

        return transactionHistoryRepository.save(bookTransactionHistory).getId();
    }


    /**
     * Permet à un utilisateur de retourner un livre emprunté.
     * <p>
     * Cette méthode vérifie si le livre existe, s'il est archivé ou non partageable,
     * si l'utilisateur est le propriétaire du livre, et si l'utilisateur a effectivement
     * emprunté le livre. Si toutes les vérifications sont passées, elle met à jour l'enregistrement
     * dans l'historique des transactions pour indiquer que le livre a été retourné et retourne
     * l'ID de cet enregistrement.
     *
     * @param bookId        L'ID du livre à retourner.
     * @param connectedUser L'objet Authentication représentant l'utilisateur connecté.
     * @return L'ID de l'enregistrement dans l'historique des transactions.
     * @throws EntityNotFoundException        Si le livre n'est pas trouvé.
     * @throws OperationNotPermittedException Si le livre est archivé ou non partageable,
     *                                        si l'utilisateur est le propriétaire du livre,
     *                                        ou si l'utilisateur n'a pas emprunté le livre.
     */
    public Integer returnBorrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
        }

        User user = (User) connectedUser.getPrincipal();

        if (Objects.equals(book.getOwner(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }

        BookTransactionHistory bookTransactionHistory = transactionHistoryRepository.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this book"));
        bookTransactionHistory.setReturned(true);

        return transactionHistoryRepository.save(bookTransactionHistory).getId();
    }


    /**
     * Approuve le retour d'un livre emprunté par un utilisateur.
     * <p>
     * Cette méthode vérifie si le livre existe, s'il est archivé ou non partageable,
     * si l'utilisateur est le propriétaire du livre, et si le livre a été retourné par l'utilisateur.
     * Si toutes les vérifications sont passées, elle met à jour l'enregistrement dans l'historique
     * des transactions pour indiquer que le retour du livre a été approuvé et retourne l'ID de cet
     * enregistrement.
     *
     * @param bookId        L'ID du livre dont le retour doit être approuvé.
     * @param connectedUser L'objet Authentication représentant l'utilisateur connecté.
     * @return L'ID de l'enregistrement dans l'historique des transactions.
     * @throws EntityNotFoundException        Si le livre n'est pas trouvé.
     * @throws OperationNotPermittedException Si le livre est archivé ou non partageable,
     *                                        si l'utilisateur est le propriétaire du livre,
     *                                        ou si le livre n'a pas encore été retourné.
     */
    public Integer approveReturnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No book found with ID:: " + bookId));

        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived or not shareable");
        }

        User user = (User) connectedUser.getPrincipal();
        if (Objects.equals(book.getOwner(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }

        BookTransactionHistory bookTransactionHistory = transactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("The book is not returned yet. You cannot approve its return"));

        bookTransactionHistory.setReturnApproved(true);
        return transactionHistoryRepository.save(bookTransactionHistory).getId();
    }


    public void uploadBookCoverPicture(MultipartFile file, Authentication connectedUser, Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new EntityNotFoundException("No book found with ID::"+ bookId));
        User user = ((User)  connectedUser.getPrincipal());
        var bookCover = fileStorageService.saveFile(file, String.valueOf(user.getId()));
        book.setBookCover(bookCover);
        bookRepository.save(book);

    }
}
