package com.library;

import com.library.controller.LibraryController;

import com.library.vue.LoginFrame;
import com.library.vue.MainFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryController controller = new LibraryController();
            new LoginFrame(controller).setVisible(true);
        });
    }
}
