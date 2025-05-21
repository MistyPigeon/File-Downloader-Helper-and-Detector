import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class FileDownloadApp {
    private JFrame frame;
    private DefaultListModel<String> fileListModel;
    private List<File> filesToZip;
    private File outputZipFile;

    public FileDownloadApp() {
        filesToZip = new ArrayList<>();
        setupGUI();
    }

    private void setupGUI() {
        frame = new JFrame("File Download Utility");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        // File list
        fileListModel = new DefaultListModel<>();
        JList<String> fileList = new JList<>(fileListModel);
        JScrollPane scrollPane = new JScrollPane(fileList);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton addButton = new JButton("Add File");
        addButton.addActionListener(new AddFileAction());
        buttonPanel.add(addButton);

        JButton createZipButton = new JButton("Create ZIP");
        createZipButton.addActionListener(new CreateZipAction());
        buttonPanel.add(createZipButton);

        JButton startServerButton = new JButton("Start Server");
        startServerButton.addActionListener(new StartServerAction());
        buttonPanel.add(startServerButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    // Action to add files
    private class AddFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                for (File file : selectedFiles) {
                    filesToZip.add(file);
                    fileListModel.addElement(file.getAbsolutePath());
                }
            }
        }
    }

    // Action to create ZIP file
    private class CreateZipAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (filesToZip.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No files selected to zip!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save ZIP File");
            fileChooser.setSelectedFile(new File("downloadable_files.zip"));
            int result = fileChooser.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                outputZipFile = fileChooser.getSelectedFile();
                try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputZipFile))) {
                    for (File file : filesToZip) {
                        try (FileInputStream fis = new FileInputStream(file)) {
                            ZipEntry zipEntry = new ZipEntry(file.getName());
                            zipOut.putNextEntry(zipEntry);

                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fis.read(buffer)) >= 0) {
                                zipOut.write(buffer, 0, length);
                            }
                        }
                    }
                    JOptionPane.showMessageDialog(frame, "ZIP file created successfully: " + outputZipFile.getAbsolutePath());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error creating ZIP file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Action to start HTTP server
    private class StartServerAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (outputZipFile == null || !outputZipFile.exists()) {
                JOptionPane.showMessageDialog(frame, "No ZIP file found to serve! Please create one first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
                server.createContext("/", new FileHandler(outputZipFile));
                server.setExecutor(null); // default executor
                server.start();
                JOptionPane.showMessageDialog(frame, "Server started at http://localhost:8000\nPress Ctrl+C in terminal to stop.");
                System.out.println("Serving file: " + outputZipFile.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error starting server: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // HTTP Handler to serve the ZIP file
    private static class FileHandler implements HttpHandler {
        private final File file;

        public FileHandler(File file) {
            this.file = file;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileDownloadApp::new);
    }
}
