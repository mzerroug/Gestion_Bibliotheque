package com.library.controller;

import com.library.model.*;
import com.library.dao.CSVHandler;
import com.library.vue.LoginFrame;
import com.library.vue.MainFrame;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.time.LocalDate;

public class LibraryController {
    private CSVHandler csvHandler;
    private List<Book> books;
    private List<User> users;
    private List<Loan> loans;
    private User currentUser;

    public LibraryController() {
        this.csvHandler = new CSVHandler();
        loadData();
    }

    private void loadData() {
        this.books = csvHandler.loadBooks();
        this.users = csvHandler.loadUsers();
        this.loans = csvHandler.loadLoans();
    }

    // Authentication
    public boolean login(String email, String password) {
        Optional<User> user = users.stream()
                .filter(u -> u.getEmail().equals(email) && u.getPassword().equals(password))
                .findFirst();

        if (user.isPresent()) {
            currentUser = user.get();
            System.out.println("Login successful for user: " + currentUser.getName()); // Debug line
            LibraryController controller = new LibraryController();
            new MainFrame(controller).setVisible(true);
            return true;
        }
        System.out.println("Login failed for email: " + email); // Debug line
        return false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    // Book management methods
    public void addBook(Book book) {
        books.add(book);
        csvHandler.saveBooks(books);
    }

    public void updateBook(Book book) {
        int index = findBookIndex(book.getId());
        if (index != -1) {
            books.set(index, book);
            csvHandler.saveBooks(books);
        }
    }

    public void deleteBook(String bookId) {
        books.removeIf(book -> book.getId().equals(bookId));
        csvHandler.saveBooks(books);
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public List<Book> searchBooks(String query) {
        // Create a final copy of the query
        final String searchQuery = query.toLowerCase();
        return books.stream()
                .filter(book -> containsSearchTerm(book, searchQuery))
                .toList();
    }

    // Helper method for search
    private boolean containsSearchTerm(Book book, String searchQuery) {
        return book.getTitle().toLowerCase().contains(searchQuery) ||
                book.getAuthor().toLowerCase().contains(searchQuery) ||
                book.getGenre().toLowerCase().contains(searchQuery);
    }

    public List<Book> getAvailableBooks() {
        return books.stream()
                .filter(Book::isAvailable)
                .toList();
    }

    private int findBookIndex(String bookId) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId().equals(bookId)) {
                return i;
            }
        }
        return -1;
    }

    // User management methods
    public void addUser(User user) {
        users.add(user);
        csvHandler.saveUsers(users);
    }

    public void updateUser(User user) {
        int index = findUserIndex(user.getId());
        if (index != -1) {
            users.set(index, user);
            csvHandler.saveUsers(users);
        }
    }

    public void deleteUser(String userId) {
        users.removeIf(user -> user.getId().equals(userId));
        csvHandler.saveUsers(users);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public User getUserById(String userId) {
        return users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    private int findUserIndex(String userId) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(userId)) {
                return i;
            }
        }
        return -1;
    }
    // Add this method to your LibraryController class
    private void createInitialAdminUser() {
        if (users.isEmpty()) {
            User adminUser = new User(
                    UUID.randomUUID().toString(),
                    "Admin",
                    "admin@library.com",
                    "admin123",
                    UserRole.ADMIN
            );
            users.add(adminUser);
            csvHandler.saveUsers(users);
        }
    }

    public double returnBook(String loanId) {
        Loan loan = loans.stream()
                .filter(l -> l.getId().equals(loanId))
                .findFirst()
                .orElse(null);

        if (loan != null && loan.getReturnDate() == null) {
            loan.setReturnDate(LocalDate.now());
            Book book = books.stream()
                    .filter(b -> b.getId().equals(loan.getBookId()))
                    .findFirst()
                    .orElse(null);

            if (book != null) {
                book.setAvailable(true);
                double penalty = calculatePenalty(loan);
                loan.setPenalty(penalty);
                csvHandler.saveLoans(loans);
                csvHandler.saveBooks(books);
                return penalty;
            }
        }
        return 0.0;
    }


    // Statistics methods
    public int getTotalBooks() {
        return books.size();
    }

    public int getAvailableBooksCount() {

        return (int) books.stream().filter(Book::isAvailable).count();
    }
    public List<Book> getMostPopularBooks(int limit) {
        Map<String, Long> bookLoanCounts = new HashMap<>();
        for (Loan loan : loans) {
            bookLoanCounts.merge(loan.getBookId(), 1L, Long::sum);
        }

        return bookLoanCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> books.stream()
                        .filter(book -> book.getId().equals(entry.getKey()))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }
    //***********************************************************************************
//Loan management methods
    public Loan createLoan(String userId, String bookId) {
        Book book = books.stream()
                .filter(b -> b.getId().equals(bookId))
                .findFirst()
                .orElse(null);

        if (book != null && book.isAvailable()) {
            Loan loan = new Loan(UUID.randomUUID().toString(), userId, bookId,
                    LocalDate.now(), LocalDate.now().plusDays(14));
            book.setAvailable(false);
            loans.add(loan);
            csvHandler.saveLoans(loans);
            csvHandler.saveBooks(books);
            return loan;
        }
        return null;
    }

    public int getActiveLoansCount() {
        return (int) loans.stream().filter(loan -> loan.getReturnDate() == null).count();
    }

    public int getOverdueLoansCount() {
        LocalDate today = LocalDate.now();
        return (int) loans.stream()
                .filter(loan -> loan.getReturnDate() == null &&
                        loan.getDueDate().isBefore(today))
                .count();
    }
    public boolean extendLoan(String loanId, int days) {
        Loan loan = loans.stream()
                .filter(l -> l.getId().equals(loanId))
                .findFirst()
                .orElse(null);

        if (loan != null && loan.getReturnDate() == null) {
            loan.setDueDate(loan.getDueDate().plusDays(days));
            csvHandler.saveLoans(loans);
            return true;
        }
        return false;
    }

    public List<Loan> getAllLoans() {
        return new ArrayList<>(loans);
    }

    public List<Loan> getActiveLoans() {
        return loans.stream()
                .filter(loan -> loan.getReturnDate() == null)
                .toList();
    }

    public List<Loan> getUserLoans(String userId) {
        return loans.stream()
                .filter(loan -> loan.getUserId().equals(userId))
                .toList();
    }

    public List<Loan> getOverdueLoans() {
        LocalDate today = LocalDate.now();
        return loans.stream()
                .filter(loan -> loan.getReturnDate() == null &&
                        loan.getDueDate().isBefore(today))
                .toList();
    }

    private double calculatePenalty(Loan loan) {
        if (loan.getReturnDate().isAfter(loan.getDueDate())) {
            long daysLate = loan.getReturnDate().toEpochDay() - loan.getDueDate().toEpochDay();
            return daysLate * 1.0; // $1 per day late
        }
        return 0.0;
    }

    public void deleteLoan(String loanId) {
        loans.removeIf(loan -> loan.getId().equals(loanId));
        csvHandler.saveLoans(loans);
    }

    public void updateLoan(Loan loan) {
        int index = findBookIndex(loan.getId());
        if (index != -1) {
            loans.set(index, loan);
            csvHandler.saveLoans(loans);
        }
    }

    public Loan getLoanById(String loanId) {
        return loans.stream()
                .filter(loan -> loan.getId().equals(loanId))
                .findFirst()
                .orElse(null);
    }

    public void addLoan(Loan newLoan) {
        loans.add(newLoan);
        csvHandler.saveLoans(loans);
    }
}
