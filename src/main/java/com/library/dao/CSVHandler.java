package com.library.dao;

import com.library.model.*;
import com.opencsv.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

public class CSVHandler {
    private static final String DATA_DIR = "src/main/java/com/library/database/";
    private static final String BOOKS_FILE = DATA_DIR + "books.csv";
    private static final String USERS_FILE = DATA_DIR + "users.csv";
    private static final String LOANS_FILE = DATA_DIR + "loans.csv";

    public CSVHandler() {
        initializeDataDirectory();
    }

    private void initializeDataDirectory() {
        new File(DATA_DIR).mkdirs();
        createFileIfNotExists(BOOKS_FILE);
        createFileIfNotExists(USERS_FILE);
        createFileIfNotExists(LOANS_FILE);
    }

    private void createFileIfNotExists(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
                writeHeaders(file);
                if (filename.equals(USERS_FILE)) {
                    createDefaultUsers();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeHeaders(File file) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            String[] headers;
            if (file.getName().equals("books.csv")) {
                headers = new String[]{"id", "title", "author", "genre", "year", "available"};
            } else if (file.getName().equals("users.csv")) {
                headers = new String[]{"id", "name", "email", "password", "role"};
            } else {
                headers = new String[]{"id", "userId", "bookId", "loanDate", "dueDate", "returnDate", "penalty"};
            }
            writer.writeNext(headers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(BOOKS_FILE))) {
            String[] line;
            reader.readNext(); // Skip header
            while ((line = reader.readNext()) != null) {
                Book book = new Book(
                        line[0], // id
                        line[1], // title
                        line[2], // author
                        line[3], // genre
                        Integer.parseInt(line[4]) // year
                );
                book.setAvailable(Boolean.parseBoolean(line[5]));
                books.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }

    public void saveBooks(List<Book> books) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(BOOKS_FILE))) {
            writer.writeNext(new String[]{"id", "title", "author", "genre", "year", "available"});
            for (Book book : books) {
                writer.writeNext(new String[]{
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getGenre(),
                        String.valueOf(book.getYear()),
                        String.valueOf(book.isAvailable())
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(USERS_FILE))) {
            String[] line;
            reader.readNext(); // Skip header
            while ((line = reader.readNext()) != null) {
                users.add(new User(
                        line[0],                    // id
                        line[1],                    // name
                        line[2],                    // email
                        line[3],                    // password
                        UserRole.valueOf(line[4])   // role
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public void saveUsers(List<User> users) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(USERS_FILE))) {
            writer.writeNext(new String[]{"id", "name", "email", "password", "role"});
            for (User user : users) {
                writer.writeNext(new String[]{
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getRole().toString()
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Loan> loadLoans() {
        List<Loan> loans = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(LOANS_FILE))) {
            String[] line;
            reader.readNext(); // Skip header
            while ((line = reader.readNext()) != null) {
                Loan loan = new Loan(
                        line[0],                    // id
                        line[1],                    // userId
                        line[2],                    // bookId
                        LocalDate.parse(line[3]),   // loanDate
                        LocalDate.parse(line[4])    // dueDate
                );

                if (line.length > 5 && !line[5].isEmpty()) {
                    loan.setReturnDate(LocalDate.parse(line[5]));
                }
                if (line.length > 6 && !line[6].isEmpty()) {
                    loan.setPenalty(Double.parseDouble(line[6]));
                }
                loans.add(loan);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loans;
    }

    public void saveLoans(List<Loan> loans) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(LOANS_FILE))) {
            writer.writeNext(new String[]{
                    "id", "userId", "bookId", "loanDate", "dueDate", "returnDate", "penalty"
            });

            for (Loan loan : loans) {
                writer.writeNext(new String[]{
                        loan.getId(),
                        loan.getUserId(),
                        loan.getBookId(),
                        loan.getLoanDate().toString(),
                        loan.getDueDate().toString(),
                        loan.getReturnDate() != null ? loan.getReturnDate().toString() : "",
                        loan.getPenalty() > 0 ? String.valueOf(loan.getPenalty()) : "0.0"
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void DefaultUsers() {
        List<User> defaultUsers = new ArrayList<>();
        defaultUsers.add(new User(
                UUID.randomUUID().toString(),
                "Admin",
                "admin@library.com",
                "admin123",
                UserRole.ADMIN
        ));
        defaultUsers.add(new User(
                UUID.randomUUID().toString(),
                "Librarian",
                "librarian@library.com",
                "lib123",
                UserRole.LIBRARIAN
        ));
        saveUsers(defaultUsers);
    }

    public void clearAllData() {
        try {
            new FileWriter(BOOKS_FILE).close();
            new FileWriter(USERS_FILE).close();
            new FileWriter(LOANS_FILE).close();
            writeHeaders(new File(BOOKS_FILE));
            writeHeaders(new File(USERS_FILE));
            writeHeaders(new File(LOANS_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void backupData() {
        String backupDir = DATA_DIR + "/backup_" + LocalDate.now().toString();
        new File(backupDir).mkdirs();

        try {
            Files.copy(new File(BOOKS_FILE).toPath(),
                    new File(backupDir + "/books.csv").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.copy(new File(USERS_FILE).toPath(),
                    new File(backupDir + "/users.csv").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.copy(new File(LOANS_FILE).toPath(),
                    new File(backupDir + "/loans.csv").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultUsers() {
        List<User> defaultUsers = new ArrayList<>();

        // Add Admin users
        defaultUsers.add(new User(
                "550e8400-e29b-41d4-a716-446655440001",
                "Admin User",
                "admin@library.com",
                "admin123",
                UserRole.ADMIN
        ));
        defaultUsers.add(new User(
                "550e8400-e29b-41d4-a716-446655440008",
                "Head Librarian",
                "head@library.com",
                "head123",
                UserRole.ADMIN
        ));

        // Add Librarian users
        defaultUsers.add(new User(
                "550e8400-e29b-41d4-a716-446655440004",
                "Sarah Wilson",
                "sarah@library.com",
                "sarah123",
                UserRole.LIBRARIAN
        ));
        defaultUsers.add(new User(
                "550e8400-e29b-41d4-a716-446655440006",
                "David Brown",
                "david@library.com",
                "david123",
                UserRole.LIBRARIAN
        ));
        defaultUsers.add(new User(
                "550e8400-e29b-41d4-a716-446655440010",
                "Tom Wilson",
                "tom@library.com",
                "tom123",
                UserRole.LIBRARIAN
        ));

        // Add Member users
        defaultUsers.add(new User(
                "550e8400-e29b-41d4-a716-446655440002",
                "John Doe",
                "john@library.com",
                "john123",
                UserRole.MEMBER
        ));
        defaultUsers.add(new User(
                "550e8400-e29b-41d4-a716-446655440003",
                "Jane Smith",
                "jane@library.com",
                "jane123",
                UserRole.MEMBER
        ));
        defaultUsers.add(new User(
                "550e8400-e29b-41d4-a716-446655440005",
                "Mike Johnson",
                "mike@library.com",
                "mike123",
                UserRole.MEMBER
        ));
        defaultUsers.add(new User(
                "550e8400-e29b-41d4-a716-446655440007",
                "Emily Davis",
                "emily@library.com",
                "emily123",
                UserRole.MEMBER
        ));
        defaultUsers.add(new User(
                "550e8400-e29b-41d4-a716-446655440009",
                "Lisa Anderson",
                "lisa@library.com",
                "lisa123",
                UserRole.MEMBER
        ));

        // Save all default users
        saveUsers(defaultUsers);

        // Print confirmation message
        System.out.println("Default users created successfully:");
        System.out.println("Admin credentials: admin@library.com / admin123");
        System.out.println("Librarian credentials: sarah@library.com / sarah123");
        System.out.println("Member credentials: john@library.com / john123");
    }

    // Add this method to check if users exist
    private boolean isUsersFileEmpty() {
        try (CSVReader reader = new CSVReader(new FileReader(USERS_FILE))) {
            // Skip header
            reader.readNext();
            // Check if there's at least one user
            return reader.readNext() == null;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    // Modify createFileIfNotExists method to use the check
    private void FileIfNotExists(String filename) {
        File file = new File(filename);
        boolean isNewFile = !file.exists();
        if (isNewFile) {
            try {
                file.createNewFile();
                writeHeaders(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Create default users if users file is empty or new
        if (filename.equals(USERS_FILE) && (isNewFile || isUsersFileEmpty())) {
            DefaultUsers();
        }
    }
}