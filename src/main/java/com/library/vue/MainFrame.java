package com.library.vue;

import com.library.controller.LibraryController;
import com.library.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.UUID;

public class MainFrame extends JFrame {

    private LibraryController controller;
    private JTabbedPane tabbedPane;


    public MainFrame(LibraryController controller) {
        this.controller = controller;
        setupUI();
    }

    private void setupUI() {
        setTitle("Bilbl-Tech");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Create menu bar
        setJMenuBar(createMenuBar());

        // Create and set up the tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Books", createBooksPanel());
        tabbedPane.addTab("Users", createUsersPanel());
        tabbedPane.addTab("Loans", createLoansPanel());
        tabbedPane.addTab("Statistics", createStatisticsPanel());

        // Add tabbedPane to frame
        add(tabbedPane);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        JMenu logoutMenu = new JMenu("Account");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> handleLogout());
        logoutMenu.add(logoutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        menuBar.add(Box.createHorizontalGlue()); // Add glue to push logout to the right
        menuBar.add(logoutMenu);

        return menuBar;
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Library Management System\nVersion 1.0\n© 2024",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create statistics cards
        panel.add(createStatCard("Total Books", String.valueOf(controller.getAllBooks().size())));
        panel.add(createStatCard("Available Books", String.valueOf(controller.getAllBooks().stream()
                .filter(Book::isAvailable)
                .count())));
        panel.add(createStatCard("Total Loans", String.valueOf(controller.getAllLoans().size())));
        panel.add(createStatCard("Active Loans", String.valueOf(controller.getAllLoans().stream()
                .filter(loan -> loan.getReturnDate() == null)
                .count())));
        return panel;
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(new Color(41, 128, 185));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(valueLabel);

        return card;
    }


    //********************************************************************************************
   /**************************Book Pannel********************************************/
    private JTable bookTable;
    private DefaultTableModel bookTableModel;


    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Book");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        controlPanel.add(addButton);
        controlPanel.add(editButton);
        controlPanel.add(deleteButton);
        controlPanel.add(new JLabel("Search: "));
        controlPanel.add(searchField);
        controlPanel.add(searchButton);

        // Table
        String[] columns = {"ID", "Title", "Author", "Genre", "Year", "Available"};
        bookTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookTable = new JTable(bookTableModel); // Use class field
        JScrollPane scrollPane = new JScrollPane(bookTable);

        // Action listeners
        addButton.addActionListener(e -> showAddBookDialog());
        editButton.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow != -1) {
                showEditBookDialog(bookTable.getValueAt(selectedRow, 0).toString());
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book to edit");
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow != -1) {
                deleteBook(bookTable.getValueAt(selectedRow, 0).toString());
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book to delete");
            }
        });

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshBookTable(bookTableModel);
        return panel;
    }

    private void showEditBookDialog(String bookId) {
        // Find the book to edit
        Book book = controller.getAllBooks().stream()
                .filter(b -> b.getId().equals(bookId))
                .findFirst()
                .orElse(null);

        if (book == null) {
            JOptionPane.showMessageDialog(this,
                    "Book not found",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create and configure the dialog
        JDialog dialog = new JDialog(this, "Edit Book", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        // Create the main panel with GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);
        JTextField titleField = new JTextField(20);
        titleField.setText(book.getTitle());
        gbc.gridx = 1;
        panel.add(titleField, gbc);

        // Author field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Author:"), gbc);
        JTextField authorField = new JTextField(20);
        authorField.setText(book.getAuthor());
        gbc.gridx = 1;
        panel.add(authorField, gbc);

        // Genre field
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Genre:"), gbc);
        JTextField genreField = new JTextField(20);
        genreField.setText(book.getGenre());
        gbc.gridx = 1;
        panel.add(genreField, gbc);

        // Year spinner
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Year:"), gbc);
        SpinnerNumberModel yearModel = new SpinnerNumberModel(
                book.getYear(),    // current value
                1000,              // minimum value
                LocalDate.now().getYear(), // maximum value
                1                  // step
        );
        JSpinner yearSpinner = new JSpinner(yearModel);
        gbc.gridx = 1;
        panel.add(yearSpinner, gbc);

        // Availability checkbox
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JCheckBox availableCheck = new JCheckBox("Available", book.isAvailable());
        panel.add(availableCheck, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        // Style buttons
        saveButton.setBackground(new Color(46, 204, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        cancelButton.setBackground(new Color(231, 76, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        // Add button actions
        saveButton.addActionListener(e -> {
            // Validate input
            if (titleField.getText().trim().isEmpty() ||
                    authorField.getText().trim().isEmpty() ||
                    genreField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please fill all fields",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update book object
            book.setTitle(titleField.getText().trim());
            book.setAuthor(authorField.getText().trim());
            book.setGenre(genreField.getText().trim());
            book.setYear((Integer) yearSpinner.getValue());
            book.setAvailable(availableCheck.isSelected());

            // Save changes
            try {
                controller.updateBook(book);
                refreshBookTable(bookTableModel);
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        "Book updated successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error updating book: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        // Add buttons to button panel
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add button panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        // Add some padding around the panel
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapperPanel.add(panel, BorderLayout.CENTER);

        // Add panel to dialog and show it
        dialog.add(wrapperPanel);
        dialog.setVisible(true);
    }

    private void showAddBookDialog() {
        // Create and configure dialog
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        // Create main panel with GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title field
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);
        JTextField titleField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(titleField, gbc);

        // Author field
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Author:"), gbc);
        JTextField authorField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(authorField, gbc);

        // Genre field
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Genre:"), gbc);
        JTextField genreField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(genreField, gbc);

        // Year spinner
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Year:"), gbc);
        SpinnerNumberModel yearModel = new SpinnerNumberModel(
                LocalDate.now().getYear(), // current year
                1000,                      // minimum
                LocalDate.now().getYear(), // maximum
                1                          // step
        );
        JSpinner yearSpinner = new JSpinner(yearModel);
        gbc.gridx = 1;
        panel.add(yearSpinner, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        // Style buttons
        saveButton.setBackground(new Color(46, 204, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        cancelButton.setBackground(new Color(231, 76, 60));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);

        // Add save button action
        saveButton.addActionListener(e -> {
            // Get and trim input values
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String genre = genreField.getText().trim();
            int year = (Integer) yearSpinner.getValue();

            // Validate input
            if (title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please fill all fields",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create and save new book
            try {
                Book newBook = new Book(UUID.randomUUID().toString(), title, author, genre, year);
                controller.addBook(newBook);
                refreshBookTable(bookTableModel);
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        "Book added successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error adding book: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Add cancel button action
        cancelButton.addActionListener(e -> dialog.dispose());

        // Add buttons to button panel
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add button panel to main panel
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        // Add padding around the panel
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wrapperPanel.add(panel, BorderLayout.CENTER);

        // Add panel to dialog and show it
        dialog.add(wrapperPanel);
        dialog.setVisible(true);
    }
    private void refreshBookTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Book book : controller.getAllBooks()) { // Changed from getBooks() to getAllBooks()
            model.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getGenre(),
                    book.getYear(),
                    book.isAvailable() ? "Yes" : "No"
            });
        }
    }

    // Add deleteBook method implementation
    private void deleteBook(String bookId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this book?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.deleteBook(bookId);
            refreshBookTable(bookTableModel);
        }
    }
    //*************************************************************************************************
    /***************************************Users Pannel *************************************************/
    // Add these fields at the top of MainFrame class
    private JTable userTable;
    private DefaultTableModel userTableModel;

    // Replace the createUsersPanel method with this implementation
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add User");
        JButton editButton = new JButton("Edit User");
        JButton deleteButton = new JButton("Delete User");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        // Style buttons
        styleButton(addButton, new Color(46, 204, 113));
        styleButton(editButton, new Color(52, 152, 219));
        styleButton(deleteButton, new Color(231, 76, 60));
        styleButton(searchButton, new Color(52, 73, 94));

        controlPanel.add(addButton);
        controlPanel.add(editButton);
        controlPanel.add(deleteButton);
        controlPanel.add(new JLabel("Search: "));
        controlPanel.add(searchField);
        controlPanel.add(searchButton);

        // Table
        String[] columns = {"ID", "Name", "Email", "Role"};
        userTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userTableModel);
        JScrollPane scrollPane = new JScrollPane(userTable);

        // Add action listeners
        addButton.addActionListener(e -> showAddUserDialog());
        editButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow != -1) {
                showEditUserDialog(userTable.getValueAt(selectedRow, 0).toString());
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user to edit");
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow != -1) {
                deleteUser(userTable.getValueAt(selectedRow, 0).toString());
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user to delete");
            }
        });

        searchButton.addActionListener(e -> searchUsers(searchField.getText()));

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshUserTable();
        return panel;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog(this, "Add New User", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        JTextField nameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // Email field
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        JTextField emailField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        // Password field
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // Role combo box
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Role:"), gbc);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"MEMBER", "LIBRARIAN", "ADMIN"});
        gbc.gridx = 1;
        panel.add(roleCombo, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        styleButton(saveButton, new Color(46, 204, 113));
        styleButton(cancelButton, new Color(231, 76, 60));

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleCombo.getSelectedItem();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields");
                return;
            }

            try {
                User newUser = new User(UUID.randomUUID().toString(), name, email, password, UserRole.valueOf(role));
                controller.addUser(newUser);
                refreshUserTable();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "User added successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding user: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditUserDialog(String userId) {
        User user = controller.getUserById(userId);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "User not found");
            return;
        }

        JDialog dialog = new JDialog(this, "Edit User", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        JTextField nameField = new JTextField(user.getName(), 20);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // Email field
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        JTextField emailField = new JTextField(user.getEmail(), 20);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        // Password field
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("New Password:"), gbc);
        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // Role combo box
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Role:"), gbc);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"MEMBER", "LIBRARIAN", "ADMIN"});
        roleCombo.setSelectedItem(user.getRole().toString());
        gbc.gridx = 1;
        panel.add(roleCombo, gbc);

        // Add buttons
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        styleButton(saveButton, new Color(46, 204, 113));
        styleButton(cancelButton, new Color(231, 76, 60));

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleCombo.getSelectedItem();

            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name and email are required");
                return;
            }

            // Update user object
            user.setName(name);
            user.setEmail(email);
            if (!password.isEmpty()) {
                user.setPassword(password);
            }
            user.setRole(UserRole.valueOf(role));

            try {
                controller.updateUser(user);
                refreshUserTable();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "User updated successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating user: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteUser(String userId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this user?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.deleteUser(userId);
            refreshUserTable();
        }
    }

    private void refreshUserTable() {
        userTableModel.setRowCount(0);
        for (User user : controller.getAllUsers()) {
            userTableModel.addRow(new Object[]{
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole()
            });
        }
    }

    private void searchUsers(String query) {
        userTableModel.setRowCount(0);
        controller.getAllUsers().stream()
                .filter(user -> user.getName().toLowerCase().contains(query.toLowerCase()) ||
                        user.getEmail().toLowerCase().contains(query.toLowerCase()))
                .forEach(user -> userTableModel.addRow(new Object[]{
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole()
                }));
    }
    //******************************************************************************************
    /*********************************Loans Pannel******************************************************/
    // Add these fields at the top of the MainFrame class
    private JTable loanTable;
    private DefaultTableModel loanTableModel;

    // Replace the createLoansPanel method with this implementation
    private JPanel createLoansPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Loan");
        JButton editButton = new JButton("Edit Loan");
        JButton deleteButton = new JButton("Delete Loan");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        // Style buttons
        styleButton(addButton, new Color(46, 204, 113));
        styleButton(editButton, new Color(52, 152, 219));
        styleButton(deleteButton, new Color(231, 76, 60));
        styleButton(searchButton, new Color(52, 73, 94));

        controlPanel.add(addButton);
        controlPanel.add(editButton);
        controlPanel.add(deleteButton);
        controlPanel.add(new JLabel("Search: "));
        controlPanel.add(searchField);
        controlPanel.add(searchButton);

        // Table for loan data
        String[] columns = {"Loan ID", "User ID", "Book ID", "Loan Date", "Due Date", "Return Date", "Penalty"};
        loanTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        loanTable = new JTable(loanTableModel);
        JScrollPane scrollPane = new JScrollPane(loanTable);

        // Add action listeners
        addButton.addActionListener(e -> showAddLoanDialog());
        editButton.addActionListener(e -> {
            int selectedRow = loanTable.getSelectedRow();
            if (selectedRow != -1) {
                showEditLoanDialog(loanTable.getValueAt(selectedRow, 0).toString());
            } else {
                JOptionPane.showMessageDialog(this, "Please select a loan to edit");
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = loanTable.getSelectedRow();
            if (selectedRow != -1) {
                deleteLoan(loanTable.getValueAt(selectedRow, 0).toString());
            } else {
                JOptionPane.showMessageDialog(this, "Please select a loan to delete");
            }
        });

        searchButton.addActionListener(e -> searchLoans(searchField.getText()));

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshLoanTable();
        return panel;
    }

    private void showAddLoanDialog() {
        JDialog dialog = new JDialog(this, "Add New Loan", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // User ID field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("User ID:"), gbc);
        JTextField userIdField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(userIdField, gbc);

        // Book ID field
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Book ID:"), gbc);
        JTextField bookIdField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(bookIdField, gbc);

        // Loan Date field
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Loan Date (yyyy-mm-dd):"), gbc);
        JTextField loanDateField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(loanDateField, gbc);

        // Due Date field
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Due Date (yyyy-mm-dd):"), gbc);
        JTextField dueDateField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(dueDateField, gbc);

        // Penalty field
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Penalty:"), gbc);
        JTextField penaltyField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(penaltyField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        styleButton(saveButton, new Color(46, 204, 113));
        styleButton(cancelButton, new Color(231, 76, 60));

        saveButton.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String bookId = bookIdField.getText().trim();
            String loanDate = loanDateField.getText().trim();
            String dueDate = dueDateField.getText().trim();
            String penaltyText = penaltyField.getText().trim();

            if (userId.isEmpty() || bookId.isEmpty() || loanDate.isEmpty() || dueDate.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields");
                return;
            }

            try {
                double penalty = penaltyText.isEmpty() ? 0.0 : Double.parseDouble(penaltyText);
                Loan newLoan = new Loan(
                        UUID.randomUUID().toString(),
                        userId,
                        bookId,
                        LocalDate.parse(loanDate),
                        LocalDate.parse(dueDate)
                );
                controller.addLoan(newLoan);
                refreshLoanTable();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Loan added successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding loan: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }


    private void showEditLoanDialog(String loanId) {
        Loan loan = controller.getLoanById(loanId);
        if (loan == null) {
            JOptionPane.showMessageDialog(this, "Loan not found");
            return;
        }

        JDialog dialog = new JDialog(this, "Edit Loan", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // User ID field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("User ID:"), gbc);
        JTextField userIdField = new JTextField(loan.getUserId(), 20);
        gbc.gridx = 1;
        panel.add(userIdField, gbc);

        // Book ID field
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Book ID:"), gbc);
        JTextField bookIdField = new JTextField(loan.getBookId(), 20);
        gbc.gridx = 1;
        panel.add(bookIdField, gbc);

        // Loan Date field
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Loan Date (yyyy-mm-dd):"), gbc);
        JTextField loanDateField = new JTextField(loan.getLoanDate().toString(), 20);
        gbc.gridx = 1;
        panel.add(loanDateField, gbc);

        // Due Date field
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Due Date (yyyy-mm-dd):"), gbc);
        JTextField dueDateField = new JTextField(loan.getDueDate().toString(), 20);
        gbc.gridx = 1;
        panel.add(dueDateField, gbc);

        // Penalty field
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Penalty:"), gbc);
        JTextField penaltyField = new JTextField(String.valueOf(loan.getPenalty()), 20);
        gbc.gridx = 1;
        panel.add(penaltyField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        styleButton(saveButton, new Color(46, 204, 113));
        styleButton(cancelButton, new Color(231, 76, 60));

        saveButton.addActionListener(e -> {
            String userId = userIdField.getText().trim();
            String bookId = bookIdField.getText().trim();
            String loanDate = loanDateField.getText().trim();
            String dueDate = dueDateField.getText().trim();
            String penaltyText = penaltyField.getText().trim();

            if (userId.isEmpty() || bookId.isEmpty() || loanDate.isEmpty() || dueDate.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields");
                return;
            }

            try {
                double penalty = penaltyText.isEmpty() ? 0.0 : Double.parseDouble(penaltyText);

                loan.setUserId(userId);
                loan.setBookId(bookId);
                loan.setLoanDate(LocalDate.parse(loanDate));
                loan.setDueDate(LocalDate.parse(dueDate));
                loan.setPenalty(penalty);

                controller.updateLoan(loan);
                refreshLoanTable();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Loan updated successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating loan: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }


    private void deleteLoan(String loanId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this loan?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.deleteLoan(loanId);
            refreshLoanTable();
        }
    }

    private void refreshLoanTable() {
        loanTableModel.setRowCount(0);
        for (Loan loan : controller.getAllLoans()) {
            loanTableModel.addRow(new Object[]{
                    loan.getId(),
                    loan.getUserId(),
                    loan.getBookId(),
                    loan.getLoanDate(),
                    loan.getDueDate(),
                    loan.getReturnDate(),
                    loan.getPenalty()
            });
        }
    }

    private void searchLoans(String query) {
        loanTableModel.setRowCount(0);
        controller.getAllLoans().stream()
                .filter(loan -> loan.getUserId().toLowerCase().contains(query.toLowerCase()) ||
                        loan.getBookId().toLowerCase().contains(query.toLowerCase()))
                .forEach(loan -> loanTableModel.addRow(new Object[]{
                        loan.getId(),
                        loan.getUserId(),
                        loan.getBookId(),
                        loan.getLoanDate(),
                        loan.getDueDate(),
                        loan.getReturnDate(),
                        loan.getPenalty()
                }));
    }
//*******************************************************************************************
    /*************************** Logout Pannel***********************************/
    private void addLogoutButton(JPanel controlPanel) {
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);

        logoutButton.addActionListener(e -> handleLogout());

        controlPanel.add(logoutButton);
    }
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            clearSessionData(); // Optional: Clear any session-related data
            this.dispose();     // Close the main window
            showLoginScreen();  // Show the login screen
        }
    }
    private void clearSessionData() {
        // Example: Reset any global variables or static fields
        CurrentUser.setUser(null); // Assuming CurrentUser stores logged-in user details
    }
    private void showLoginScreen() {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame(controller).setVisible(true); // Assuming LoginFrame is your login window
        });
    }

}