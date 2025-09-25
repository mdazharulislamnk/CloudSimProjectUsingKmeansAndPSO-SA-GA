
package org.example;

import com.itextpdf.html2pdf.HtmlConverter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MainGUI extends JFrame {
    private JPanel mainPanel;
    private JTextPane outputArea;
    private JButton runButton, exportPdfButton, exportExcelButton;
    private List<String> simulationOutput;
    private final String currentDateTime = ZonedDateTime.now()
            .format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy, hh:mm a XXX"));

    public MainGUI() {
        setTitle("CloudSim Simulation GUI");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        simulationOutput = new ArrayList<>();

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(249, 249, 249));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title bar
        JPanel titleBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(52, 73, 94), getWidth(), 0, new Color(44, 62, 80));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        titleBar.setPreferredSize(new Dimension(800, 40));
        titleBar.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("CloudSim Simulation GUI");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleBar.add(titleLabel, BorderLayout.CENTER);

        JButton closeButton = new JButton("X");
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(231, 76, 60));
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setPreferredSize(new Dimension(40, 40));
        closeButton.addActionListener(e -> System.exit(0));
        titleBar.add(closeButton, BorderLayout.EAST);

        // JTextPane setup
        outputArea = new JTextPane();
        outputArea.setContentType("text/html");
        outputArea.setEditable(false);
        outputArea.setBackground(Color.WHITE);
        outputArea.setFont(new Font("Arial", Font.PLAIN, 12));
        outputArea.setText("<html><body><p style='color: #333;'>GUI Initialized. Click 'Run Simulation' to start.</p></body></html>");
        outputArea.setPreferredSize(new Dimension(780, 500));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setPreferredSize(new Dimension(780, 500));
        scrollPane.setMinimumSize(new Dimension(780, 500));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(52, 73, 94), 2, true));

        // Button setup
        exportPdfButton = new JButton("Export as PDF") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(46, 204, 113), 0, getHeight(), new Color(39, 174, 96));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        exportPdfButton.setForeground(Color.WHITE);
        exportPdfButton.setFont(new Font("Arial", Font.BOLD, 14));
        exportPdfButton.setFocusPainted(false);
        exportPdfButton.setBorderPainted(false);
        exportPdfButton.setContentAreaFilled(false);
        exportPdfButton.setOpaque(false);
        exportPdfButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        exportPdfButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportPdfButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { exportPdfButton.setForeground(new Color(236, 240, 241)); }
            @Override
            public void mouseExited(MouseEvent e) { exportPdfButton.setForeground(Color.WHITE); }
        });
        exportPdfButton.addActionListener(e -> exportToPdf());

        exportExcelButton = new JButton("Export as Excel") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(241, 196, 15), 0, getHeight(), new Color(243, 156, 18));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        exportExcelButton.setForeground(Color.WHITE);
        exportExcelButton.setFont(new Font("Arial", Font.BOLD, 14));
        exportExcelButton.setFocusPainted(false);
        exportExcelButton.setBorderPainted(false);
        exportExcelButton.setContentAreaFilled(false);
        exportExcelButton.setOpaque(false);
        exportExcelButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        exportExcelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportExcelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { exportExcelButton.setForeground(new Color(236, 240, 241)); }
            @Override
            public void mouseExited(MouseEvent e) { exportExcelButton.setForeground(Color.WHITE); }
        });
        exportExcelButton.addActionListener(e -> exportToExcel());

        runButton = new JButton("Run Simulation") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(52, 152, 219), 0, getHeight(), new Color(41, 128, 185));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        runButton.setForeground(Color.WHITE);
        runButton.setFont(new Font("Arial", Font.BOLD, 14));
        runButton.setFocusPainted(false);
        runButton.setBorderPainted(false);
        runButton.setContentAreaFilled(false);
        runButton.setOpaque(false);
        runButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        runButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        runButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { runButton.setForeground(new Color(236, 240, 241)); }
            @Override
            public void mouseExited(MouseEvent e) { runButton.setForeground(Color.WHITE); }
        });
        runButton.addActionListener(e -> runSimulation());

        // Layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(runButton);
        buttonPanel.add(exportPdfButton);
        buttonPanel.add(exportExcelButton);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(true);
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(titleBar, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void runSimulation() {
        runButton.setEnabled(false);
        outputArea.setText("<html><body><p style='color: #333;'>Running simulation...</p></body></html>");
        simulationOutput.clear();

        Thread simThread = new Thread(() -> {
            try {
                PrintStream printStream = new PrintStream(new OutputStream() {
                    private final StringBuilder buffer = new StringBuilder();

                    @Override
                    public void write(int b) {
                        char c = (char) b;
                        if (c == '\r') return;
                        buffer.append(c);
                        if (c == '\n') {
                            final String line = buffer.toString().trim();
                            buffer.setLength(0);
                            if (!line.isEmpty()) simulationOutput.add(line);
                        }
                    }

                    @Override
                    public void write(byte[] b, int off, int len) {
                        String text = new String(b, off, len, StandardCharsets.UTF_8);
                        for (char c : text.toCharArray()) write(c);
                    }
                }, true);

                PrintStream oldOut = System.out;
                PrintStream oldErr = System.err;
                System.setOut(printStream);
                System.setErr(printStream);

                Main.main(new String[]{});

                System.setOut(oldOut);
                System.setErr(oldErr);

                // Save raw output for debugging
                try (FileWriter writer = new FileWriter("raw_output.txt")) {
                    writer.write(String.join("\n", simulationOutput));
                } catch (IOException e) {
                    System.err.println("Error saving raw output: " + e.getMessage());
                }

                SwingUtilities.invokeAndWait(() -> {
                    if (simulationOutput.isEmpty()) {
                        outputArea.setText("<html><body><p style='color: red;'>No simulation output generated.</p></body></html>");
                    } else {
                        String html = generateHtmlContent();
                        outputArea.setText(html);
                        outputArea.setCaretPosition(0);
                        outputArea.revalidate();
                        outputArea.repaint();
                        outputArea.getParent().revalidate();
                        outputArea.getParent().repaint();
                        try (FileWriter writer = new FileWriter("debug.html")) {
                            writer.write(html);
                        } catch (IOException e) {
                            System.err.println("Error saving debug HTML: " + e.getMessage());
                        }
                        System.out.println("Setting HTML in outputArea, length: " + html.length() + " characters");
                        System.out.println("outputArea content type: " + outputArea.getContentType());
                        System.out.println("outputArea visible: " + outputArea.isVisible());
                        System.out.println("outputArea size: " + outputArea.getSize());
                        if (outputArea.getText().trim().isEmpty()) {
                            outputArea.setContentType("text/plain");
                            outputArea.setText(String.join("\n", simulationOutput));
                            outputArea.setCaretPosition(0);
                            outputArea.revalidate();
                            outputArea.repaint();
                            System.out.println("Fallback to plain text in outputArea");
                        }
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    outputArea.setText("<html><body><p style='color: red;'>Error: " + ex.getMessage() + "</p></body></html>");
                    outputArea.setCaretPosition(0);
                    outputArea.revalidate();
                    outputArea.repaint();
                    outputArea.getParent().revalidate();
                    outputArea.getParent().repaint();
                });
                ex.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(() -> {
                    runButton.setEnabled(true);
                    mainPanel.revalidate();
                    mainPanel.repaint();
                });
            }
        });

        simThread.start();
    }

    private String generateHtmlContent() {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><head><style>");
        htmlContent.append("body { font-family: Arial, sans-serif; margin: 20px; color: #333; }");
        htmlContent.append("h1 { color: #2c3e50; text-align: center; }");
        htmlContent.append("h2 { color: #34495e; margin-top: 20px; border-bottom: 2px solid #3498db; padding-bottom: 5px; }");
        htmlContent.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; background-color: white; }");
        htmlContent.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        htmlContent.append("th { background-color: #3498db; color: white; }");
        htmlContent.append("tr:nth-child(even) { background-color: #f2f2f2; }");
        htmlContent.append("p { line-height: 1.6; margin: 5px 0; }");
        htmlContent.append(".summary { font-weight: bold; color: #2c3e50; }");
        htmlContent.append(".datetime { font-style: italic; color: #7f8c8d; text-align: center; }");
        htmlContent.append("</style></head><body>");

        htmlContent.append("<p class=\"datetime\">Generated on ").append(currentDateTime).append("</p>");
        htmlContent.append("<h1>CloudSim Simulation Report</h1>");


        boolean inMipsTable = false;
        boolean inCloudletTable = false;
        boolean inSection = false;
        String currentSection = "";
        StringBuilder sectionContent = new StringBuilder();

        for (int i = 0; i < simulationOutput.size(); i++) {
            String line = simulationOutput.get(i).replace("<", "&lt;").replace(">", "&gt;"); // Escape HTML
            if (line.startsWith("===")) {
                // End previous section
                if (inSection) {
                    htmlContent.append("<h2>").append(currentSection).append("</h2>");
                    if (sectionContent.length() > 0) {
                        htmlContent.append("<p>").append(sectionContent.toString().replace("\n", "<br>")).append("</p>");
                    }
                    sectionContent.setLength(0);
                    inSection = false;
                }
                // Start new section
                currentSection = line.replace("===", "").trim();
                inSection = true;
                if (currentSection.equals("GA Best Allocation")) {
                    i++; // Skip header
                    htmlContent.append("<h2>").append(currentSection).append("</h2>");
                    htmlContent.append("<table><tr><th>VM ID</th><th>MIPS</th><th>Assigned Host</th></tr>");
                    inMipsTable = true;
                    inSection = false;
                } else if (currentSection.equals("Cloudlet Results")) {
                    i++; // Skip header
                    htmlContent.append("<h2>").append(currentSection).append("</h2>");
                    htmlContent.append("<table><tr><th>CloudletID</th><th>STATUS</th><th>VMID</th><th>Time</th><th>Start</th><th>Finish</th></tr>");
                    inCloudletTable = true;
                    inSection = false;
                }
            } else if (inMipsTable && line.matches("\\d+\\s+\\d+\\.\\d+\\s+\\d+")) {
                String[] parts = line.trim().split("\\s+");
                sectionContent.append("<tr><td>").append(parts[0]).append("</td><td>")
                        .append(parts[1]).append("</td><td>").append(parts[2]).append("</td></tr>");
            } else if (inMipsTable && !line.matches("\\d+\\s+\\d+\\.\\d+\\s+\\d+")) {
                htmlContent.append(sectionContent.toString()).append("</table>");
                inMipsTable = false;
                sectionContent.setLength(0);
                if (line.contains("VM #")) {
                    currentSection = "VM Allocation Details";
                    inSection = true;
                    sectionContent.append(line).append("\n");
                }
            } else if (inCloudletTable && line.matches("\\d+\\s+\\w+\\s+\\d+\\s+\\d+\\.\\d+\\s+\\d+\\.\\d+\\s+\\d+\\.\\d+")) {
                String[] parts = line.trim().split("\\s+");
                sectionContent.append("<tr><td>").append(parts[0]).append("</td><td>")
                        .append(parts[1]).append("</td><td>").append(parts[2]).append("</td><td>")
                        .append(parts[3]).append("</td><td>").append(parts[4]).append("</td><td>")
                        .append(parts[5]).append("</td></tr>");
            } else if (inCloudletTable && !line.matches("\\d+\\s+\\w+\\s+\\d+\\s+\\d+\\.\\d+\\s+\\d+\\.\\d+\\s+\\d+\\.\\d+")) {
                htmlContent.append(sectionContent.toString()).append("</table>");
                inCloudletTable = false;
                sectionContent.setLength(0);
                if (!line.isEmpty()) {
                    currentSection = "Simulation Process";
                    inSection = true;
                    sectionContent.append(line).append("\n");
                }
            } else if (inSection) {
                sectionContent.append(line).append("\n");
            } else if (!line.isEmpty()) {
                // Handle simulation process or other lines
                if (line.contains("Broker") || line.contains("Datacenter") || line.contains("Simulation:") || line.contains("CloudSim") || line.contains("Entities")) {
                    if (!currentSection.equals("Simulation Process")) {
                        if (inSection && sectionContent.length() > 0) {
                            htmlContent.append("<h2>").append(currentSection).append("</h2>");
                            htmlContent.append("<p>").append(sectionContent.toString().replace("\n", "<br>")).append("</p>");
                            sectionContent.setLength(0);
                            inSection = false;
                        }
                        currentSection = "Simulation Process";
                        inSection = true;
                    }
                    sectionContent.append(line).append("\n");
                } else {
                    sectionContent.append(line).append("\n");
                    inSection = true;
                    if (currentSection.isEmpty()) currentSection = "Miscellaneous";
                }
            }
        }

        // Close any open section
        if (inSection && sectionContent.length() > 0) {
            htmlContent.append("<h2>").append(currentSection).append("</h2>");
            htmlContent.append("<p>").append(sectionContent.toString().replace("\n", "<br>")).append("</p>");
        } else if (inMipsTable) {
            htmlContent.append(sectionContent.toString()).append("</table>");
        } else if (inCloudletTable) {
            htmlContent.append(sectionContent.toString()).append("</table>");
        }

        htmlContent.append("</body></html>");
        String result = htmlContent.toString();
        System.out.println("Generated HTML length: " + result.length() + " characters");
        return result;
    }

    private void updateOutputArea() {
        String html = generateHtmlContent();
        outputArea.setText(html);
        outputArea.setCaretPosition(0);
    }

    private void exportToPdf() {
        if (simulationOutput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No simulation data to export!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF File");
        fileChooser.setSelectedFile(new File("simulation_report.pdf"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                String htmlContent = generateHtmlContent();
                HtmlConverter.convertToPdf(htmlContent, new FileOutputStream(fileToSave));
                JOptionPane.showMessageDialog(this, "PDF exported successfully to " + fileToSave.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting to PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void exportToExcel() {
        if (simulationOutput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No simulation data to export!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new File("simulation_report.xlsx"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Simulation Report");
                int rowNum = 0;
                Row row;
                Cell cell;

                row = sheet.createRow(rowNum++);
                cell = row.createCell(0);
                cell.setCellValue("Generated on " + currentDateTime);

                row = sheet.createRow(rowNum++);

                boolean inMipsTable = false;
                boolean inCloudletTable = false;
                List<String[]> mipsTableData = new ArrayList<>();
                List<String[]> cloudletTableData = new ArrayList<>();

                for (String line : simulationOutput) {
                    if (line.contains("Simulated Annealing Best Allocation")) {
                        inMipsTable = true;
                        row = sheet.createRow(rowNum++);
                        String[] headers = {"VM ID", "MIPS", "Assigned Host"};
                        for (int i = 0; i < headers.length; i++) {
                            cell = row.createCell(i);
                            cell.setCellValue(headers[i]);
                        }
                    } else if (inMipsTable && line.matches("\\d+\\s+\\d+\\.\\d+\\s+\\d+")) {
                        mipsTableData.add(line.split("\\s+"));
                    } else if (inMipsTable && line.contains("VM #")) {
                        inMipsTable = false;
                        for (String[] data : mipsTableData) {
                            row = sheet.createRow(rowNum++);
                            for (int i = 0; i < data.length; i++) {
                                cell = row.createCell(i);
                                cell.setCellValue(data[i]);
                            }
                        }
                        mipsTableData.clear();
                        row = sheet.createRow(rowNum++);
                        cell = row.createCell(0);
                        cell.setCellValue(line);
                    } else if (line.contains("Cloudlet Results")) {
                        inCloudletTable = true;
                        row = sheet.createRow(rowNum++);
                        String[] headers = {"CloudletID", "STATUS", "VMID", "Time", "Start", "Finish"};
                        for (int i = 0; i < headers.length; i++) {
                            cell = row.createCell(i);
                            cell.setCellValue(headers[i]);
                        }
                    } else if (inCloudletTable && line.matches("\\d+\\s+\\w+\\s+\\d+\\s+\\d+\\.\\d+\\s+\\d+\\.\\d+\\s+\\d+\\.\\d+")) {
                        cloudletTableData.add(line.split("\\s+"));
                    } else if (inCloudletTable && line.isEmpty()) {
                        inCloudletTable = false;
                        for (String[] data : cloudletTableData) {
                            row = sheet.createRow(rowNum++);
                            for (int i = 0; i < data.length; i++) {
                                cell = row.createCell(i);
                                cell.setCellValue(data[i]);
                            }
                        }
                        cloudletTableData.clear();
                    } else if (!line.isEmpty()) {
                        row = sheet.createRow(rowNum++);
                        cell = row.createCell(0);
                        cell.setCellValue(line);
                    }
                }

                try (FileOutputStream fileOut = new FileOutputStream(fileToSave)) {
                    workbook.write(fileOut);
                }
                JOptionPane.showMessageDialog(this, "Excel exported successfully to " + fileToSave.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting to Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            gui.setVisible(true);
        });
    }
}