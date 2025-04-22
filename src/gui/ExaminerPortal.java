package gui;
import db.DBConnection;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import model.Exam;
import model.Option;
import model.Question;
import services.ExamService;



public class ExaminerPortal extends JFrame {
    private String userId;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    
    public ExaminerPortal(String userId) {
        this.userId = userId;
        initializeUI();
//        loadExaminerData();
    }
    
    private void initializeUI() {
        setTitle("Examiner Portal - Online Examination System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        
        // Navigation Panel
        JPanel navPanel = createNavigationPanel();
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(234, 233, 232)));
        
        // Content Panel (CardLayout for switching views)
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);
        
        // Create and add all panels
        cardPanel.add(createDashboardPanel(), "dashboard");
        cardPanel.add(createExamsPanel(), "exams");
        cardPanel.add(createQuestionsPanel(), "questions");
        cardPanel.add(createResultsPanel(), "results");
        
        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(navPanel, BorderLayout.WEST);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(73, 125, 116));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT u.Name, e.Department FROM user u JOIN examiner e ON u.userID = e.userID WHERE u.userID = ?");
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                JLabel welcomeLabel = new JLabel("Welcome, " + rs.getString("Name") + " (" + rs.getString("Department") + ")");
                welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
                welcomeLabel.setForeground(Color.WHITE);
                headerPanel.add(welcomeLabel, BorderLayout.WEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(new Color(73, 125, 116));
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            new LoginPage().setVisible(true);
            dispose();
        });
        headerPanel.add(logoutBtn, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] navItems = {"Dashboard", "Exams", "Questions", "Results"};
        String[] cardNames = {"dashboard", "exams", "questions", "results"};
        
        for (int i = 0; i < navItems.length; i++) {
            JButton navButton = new JButton(navItems[i]);
            navButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            navButton.setForeground(new Color(73, 125, 116));
            navButton.setBackground(Color.WHITE);
            navButton.setBorderPainted(false);
            navButton.setFocusPainted(false);
            navButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            navButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            navButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            
            final String cardName = cardNames[i];
            navButton.addActionListener(e -> cardLayout.show(cardPanel, cardName));
            
            navPanel.add(navButton);
            navPanel.add(Box.createVerticalStrut(10));
        }
        
        return navPanel;
    }
    
    private void refreshPanel(String panelName) {
        // Remove all panels
        cardPanel.removeAll();
        
        // Add all panels back in the correct order
        cardPanel.add(createDashboardPanel(), "dashboard");
        cardPanel.add(createExamsPanel(), "exams");
        cardPanel.add(createQuestionsPanel(), "questions");
        cardPanel.add(createResultsPanel(), "results");
        
        // Show the current panel
        cardLayout.show(cardPanel, panelName);
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(234, 233, 232)); // Light gray background
        
        JLabel titleLabel = new JLabel("Examiner Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(73, 125, 116)); // Teal green color
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Stats panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        try {
            Connection conn = DBConnection.getConnection();
            
            // Total exams created
            PreparedStatement ps1 = conn.prepareStatement(
                "SELECT COUNT(*) AS total FROM Exam WHERE UserID = ?");
            ps1.setString(1, userId);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            addStatCard(statsPanel, "Total Exams", rs1.getString("total"), new Color(52, 152, 219)); // Blue
            
            // Total questions added
            PreparedStatement ps2 = conn.prepareStatement(
                "SELECT COUNT(*) AS total FROM manages WHERE UserID = ?");
            ps2.setString(1, userId);
            ResultSet rs2 = ps2.executeQuery();
            rs2.next();
            addStatCard(statsPanel, "Total Questions", rs2.getString("total"), new Color(46, 204, 113)); // Green
            
            // Active exams
            PreparedStatement ps3 = conn.prepareStatement(
                "SELECT COUNT(*) AS total FROM Exam WHERE UserID = ? AND Status = 'Active'");
            ps3.setString(1, userId);
            ResultSet rs3 = ps3.executeQuery();
            rs3.next();
            addStatCard(statsPanel, "Active Exams", rs3.getString("total"), new Color(155, 89, 182)); // Purple
            
            // Students attempted
            PreparedStatement ps4 = conn.prepareStatement(
                "SELECT COUNT(DISTINCT a.UserID) AS total FROM Attempt a " +
                "JOIN Exam e ON a.ExamID = e.ExamID WHERE e.UserID = ?");
            ps4.setString(1, userId);
            ResultSet rs4 = ps4.executeQuery();
            rs4.next();
            addStatCard(statsPanel, "Students Attempted", rs4.getString("total"), new Color(230, 126, 34)); // Orange
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        panel.add(statsPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private void addStatCard(JPanel panel, String title, String value, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        card.add(titleLabel, BorderLayout.NORTH);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLabel, BorderLayout.CENTER);
        
        panel.add(card);
    }
    
    private JPanel createExamsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(234, 233, 232));
        
        JLabel titleLabel = new JLabel("Manage Exams");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(73, 125, 116));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Create exam button
        JButton createExamBtn = new JButton("Create New Exam");
        createExamBtn.setBackground(new Color(46, 204, 113)); // Green color
        createExamBtn.setForeground(Color.WHITE);
        createExamBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        createExamBtn.setBorderPainted(false);
        createExamBtn.setFocusPainted(false);
        createExamBtn.addActionListener(e -> showCreateExamDialog());
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(createExamBtn);
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Exams table
        String[] columnNames = {"Exam ID", "Subject", "Total Marks", "Duration", "Status", "Actions"};
        Object[][] data = getExamsData();
        JTable examsTable = new JTable(data, columnNames);
        examsTable.setRowHeight(30);
        examsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Add delete button to each row
        examsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setBackground(Color.WHITE);
                JButton deleteBtn = new JButton("Delete");
                deleteBtn.setBackground(new Color(231, 76, 60)); // Red color
                deleteBtn.setForeground(Color.WHITE);
                deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                deleteBtn.setBorderPainted(false);
                deleteBtn.setFocusPainted(false);
                panel.add(deleteBtn);
                return panel;
            }
        });
        
        examsTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setBackground(Color.WHITE);
                JButton deleteBtn = new JButton("Delete");
                deleteBtn.setBackground(new Color(231, 76, 60)); // Red color
                deleteBtn.setForeground(Color.WHITE);
                deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                deleteBtn.setBorderPainted(false);
                deleteBtn.setFocusPainted(false);
                
                deleteBtn.addActionListener(e -> {
                    String examId = (String) table.getValueAt(row, 0);
                    int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Are you sure you want to delete this exam?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            ExamService examService = new ExamService();
                            boolean success = examService.deleteExam(examId);
                            
                            if (success) {
                                JOptionPane.showMessageDialog(
                                    panel,
                                    "Exam deleted successfully!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                                refreshPanel("exams");
                            } else {
                                JOptionPane.showMessageDialog(
                                    panel,
                                    "Failed to delete exam!",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                                );
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(
                                panel,
                                "An error occurred while deleting the exam.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                });
                
                panel.add(deleteBtn);
                return panel;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(examsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void showCreateExamDialog() {
        JDialog dialog = new JDialog(this, "Create New Exam", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel subjectLabel = new JLabel("Subject:");
        JTextField subjectField = new JTextField();
        
        JLabel marksLabel = new JLabel("Total Marks:");
        JTextField marksField = new JTextField();
        
        JLabel durationLabel = new JLabel("Duration (mins):");
        JTextField durationField = new JTextField();
        
        JButton createBtn = new JButton("Create");
        createBtn.setBackground(new Color(46, 204, 113)); // Green color
        createBtn.setForeground(Color.WHITE);
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        createBtn.setBorderPainted(false);
        createBtn.setFocusPainted(false);
        createBtn.addActionListener(e -> {
            String subject = subjectField.getText().trim();
            String marksText = marksField.getText().trim();
            String durationText = durationField.getText().trim();

            if (subject.isEmpty() || marksText.isEmpty() || durationText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int totalMarks = Integer.parseInt(marksText);
                int duration = Integer.parseInt(durationText);
                
                Exam exam = new Exam(
                    "TEMP",
                    totalMarks,
                    duration + " mins",
                    subject,
                    "Scheduled",
                    userId
                );

                ExamService examService = new ExamService();
                boolean success = examService.createExam(exam);

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Exam created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshPanel("exams");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create exam!", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Enter valid numeric values for marks and duration.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "An error occurred while creating the exam.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(subjectLabel);
        panel.add(subjectField);
        panel.add(marksLabel);
        panel.add(marksField);
        panel.add(durationLabel);
        panel.add(durationField);
        panel.add(new JLabel());
        panel.add(createBtn);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private Object[][] getExamsData() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT ExamID, Subject, TotalMarks, Duration, Status FROM Exam WHERE UserID = ?");
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            // Use a dynamic list to collect rows
            java.util.List<Object[]> rows = new java.util.ArrayList<>();

            while (rs.next()) {
                Object[] row = new Object[6];
                row[0] = rs.getString("ExamID");
                row[1] = rs.getString("Subject");
                row[2] = rs.getString("TotalMarks");
                row[3] = rs.getString("Duration");
                String ex= rs.getString("Status");
                row[4] = "done";
                rows.add(row);
            }

            return rows.toArray(new Object[0][0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0][0];
        }
    }
    
    private JPanel createQuestionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(234, 233, 232));
        
        JLabel titleLabel = new JLabel("Manage Questions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(73, 125, 116));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Toolbar with buttons
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addQuestionBtn = new JButton("Add New Question");
        addQuestionBtn.setBackground(new Color(46, 204, 113)); // Green color
        addQuestionBtn.setForeground(Color.WHITE);
        addQuestionBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addQuestionBtn.setBorderPainted(false);
        addQuestionBtn.setFocusPainted(false);
        addQuestionBtn.addActionListener(e -> showAddQuestionDialog());
        
        toolbar.add(addQuestionBtn);
        panel.add(toolbar, BorderLayout.CENTER);
        
        // Table to display questions
        String[] columnNames = {"Question ID", "Category", "Level", "Text", "Marks", "Actions"};
        Object[][] data = getQuestionsData();
        
        JTable questionTable = new JTable(data, columnNames);
        questionTable.setRowHeight(30);
        questionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Add delete button to each row
        questionTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setBackground(Color.WHITE);
                JButton deleteBtn = new JButton("Delete");
                deleteBtn.setBackground(new Color(231, 76, 60)); // Red color
                deleteBtn.setForeground(Color.WHITE);
                deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                deleteBtn.setBorderPainted(false);
                deleteBtn.setFocusPainted(false);
                panel.add(deleteBtn);
                return panel;
            }
        });
        
        questionTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setBackground(Color.WHITE);
                JButton deleteBtn = new JButton("Delete");
                deleteBtn.setBackground(new Color(231, 76, 60)); // Red color
                deleteBtn.setForeground(Color.WHITE);
                deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                deleteBtn.setBorderPainted(false);
                deleteBtn.setFocusPainted(false);
                
                deleteBtn.addActionListener(e -> {
                    String questionId = (String) table.getValueAt(row, 0);
                    int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Are you sure you want to delete this question?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            ExamService examService = new ExamService();
                            boolean success = examService.deleteQuestion(questionId);
                            
                            if (success) {
                                JOptionPane.showMessageDialog(
                                    panel,
                                    "Question deleted successfully!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                                refreshPanel("questions");
                            } else {
                                JOptionPane.showMessageDialog(
                                    panel,
                                    "Failed to delete question!",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                                );
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(
                                panel,
                                "An error occurred while deleting the question.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                });
                
                panel.add(deleteBtn);
                return panel;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(questionTable);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private Object[][] getQuestionsData() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT q.QuestionID, q.Category, q.Level, q.Text, q.Marks " +
                "FROM question q JOIN manages m ON q.QuestionID = m.QuestionID " +
                "WHERE m.UserID = ?",   ResultSet.TYPE_SCROLL_INSENSITIVE,
    ResultSet.CONCUR_READ_ONLY);
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            
            // Count rows
            rs.last();
            int rowCount = rs.getRow();
            rs.beforeFirst();
            
            Object[][] data = new Object[rowCount][6];
            int i = 0;
            while (rs.next()) {
                data[i][0] = rs.getString("QuestionID");
                data[i][1] = rs.getString("Category");
                data[i][2] = rs.getString("Level");
                data[i][3] = rs.getString("Text");
                data[i][4] = rs.getString("Marks");
                data[i][5] = "Edit | Delete";
                i++;
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0][0];
        }
    }
    
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(234, 233, 232)); // Light gray background
        
        // Title panel with refresh button
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Exam Results");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(73, 125, 116)); // Teal green color
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(73, 125, 116)); // Teal green color
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.setBorderPainted(false);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshPanel("results"));
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(refreshButton, BorderLayout.EAST);
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // Create a panel for filters
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Exam filter
        JLabel examLabel = new JLabel("Select Exam:");
        examLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JComboBox<String> examComboBox = new JComboBox<>();
        examComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT ExamID, Subject FROM Exam WHERE UserID = ?");
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            
            examComboBox.addItem("All Exams");
            while (rs.next()) {
                examComboBox.addItem(rs.getString("ExamID") + " - " + rs.getString("Subject"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Add filter components
        filterPanel.add(examLabel);
        filterPanel.add(examComboBox);
        
        // Add filter panel to main panel
        panel.add(filterPanel, BorderLayout.CENTER);
        
        // Table to display results
        String[] columnNames = {"Exam ID", "Subject", "Student Name", "Score", "Grade", "Accuracy", "Rank"};
        Object[][] data = getExamResultsData();
        
        JTable resultsTable = new JTable(data, columnNames);
        resultsTable.setRowHeight(30);
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        resultsTable.setDefaultEditor(Object.class, null);
        
        // Add action listener to exam filter
        examComboBox.addActionListener(e -> {
            String selectedExam = (String) examComboBox.getSelectedItem();
            if (selectedExam != null) {
                String examId = selectedExam.equals("All Exams") ? null : selectedExam.split(" - ")[0];
                Object[][] filteredData = getExamResultsData(examId);
                DefaultTableModel model = new DefaultTableModel(filteredData, columnNames);
                resultsTable.setModel(model);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private Object[][] getExamResultsData(String examId) {
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT e.ExamID, e.Subject, u.Name AS StudentName, " +
                          "r.Score, r.Grade, pr.Accuracy, pr.Ranks " +
                          "FROM Result r " +
                          "JOIN Exam e ON r.ExamID = e.ExamID " +
                          "JOIN user u ON r.UserID = u.userID " +
                          "JOIN PerformanceReport pr ON r.ResultID = pr.ResultID " +
                          "WHERE e.UserID = ?";
            
            if (examId != null) {
                query += " AND e.ExamID = ?";
            }
            
            PreparedStatement ps = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setString(1, userId);
            if (examId != null) {
                ps.setString(2, examId);
            }
            
            ResultSet rs = ps.executeQuery();
            
            // First, count the number of rows
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
            }
            rs.beforeFirst(); // Reset the cursor
            
            Object[][] data = new Object[rowCount][7];
            int i = 0;
            while (rs.next()) {
                data[i][0] = rs.getString("ExamID");
                data[i][1] = rs.getString("Subject");
                data[i][2] = rs.getString("StudentName");
                data[i][3] = rs.getString("Score");
                data[i][4] = rs.getString("Grade");
                data[i][5] = rs.getString("Accuracy");
                data[i][6] = rs.getString("Ranks");
                i++;
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0][0];
        }
    }
    
    private Object[][] getExamResultsData() {
        return getExamResultsData(null);
    }
    
    private void showAddQuestionDialog() {
        ExamService examService = new ExamService();
        JDialog dialog = new JDialog(this, "Add New Question", true);
        dialog.setSize(700, 700);
        dialog.setLocationRelativeTo(this);
        
        // Main container with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // North panel for header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel headerLabel = new JLabel("Add New Question");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(headerLabel);
        
        // Center panel with form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        
        // Basic information section
        JPanel basicInfoPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        basicInfoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Basic Information"));
        
        basicInfoPanel.add(new JLabel("Exam ID:"));
        JTextField examIdField = new JTextField();
        basicInfoPanel.add(examIdField);
        
        basicInfoPanel.add(new JLabel("Category:"));
        JTextField categoryField = new JTextField();
        basicInfoPanel.add(categoryField);
        
        basicInfoPanel.add(new JLabel("Level:"));
        JTextField levelField = new JTextField();
        basicInfoPanel.add(levelField);
        
        basicInfoPanel.add(new JLabel("Marks:"));
        JTextField marksField = new JTextField();
        basicInfoPanel.add(marksField);
        
        // Question content section
        JPanel questionPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        questionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Question Content"));
        
        questionPanel.add(new JLabel("MCQ:"));
        JTextField mcqField = new JTextField();
        questionPanel.add(mcqField);
        
        questionPanel.add(new JLabel("FIB:"));
        JTextField fibField = new JTextField();
        questionPanel.add(fibField);
        
        questionPanel.add(new JLabel("Question Text:"));
        JTextField textField = new JTextField();
        questionPanel.add(textField);
        
        // Options section 
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Answer Options"));
        
        JTextField[] optionFields = new JTextField[4];
        JRadioButton[] correctButtons = new JRadioButton[4];
        ButtonGroup group = new ButtonGroup();
        
        for (int i = 0; i < 4; i++) {
            JPanel optRow = new JPanel(new BorderLayout(10, 0));
            JLabel optLabel = new JLabel("Option " + (i + 1) + ":");
            optLabel.setPreferredSize(new Dimension(80, 25));
            
            optionFields[i] = new JTextField();
            correctButtons[i] = new JRadioButton("Correct");
            correctButtons[i].setBackground(Color.WHITE);
            group.add(correctButtons[i]);
            
            JPanel innerPanel = new JPanel(new BorderLayout());
            innerPanel.add(optionFields[i], BorderLayout.CENTER);
            innerPanel.add(correctButtons[i], BorderLayout.EAST);
            
            optRow.add(optLabel, BorderLayout.WEST);
            optRow.add(innerPanel, BorderLayout.CENTER);
            optionsPanel.add(optRow);
        }
        
        // Add panels to form
        formPanel.add(basicInfoPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(questionPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(optionsPanel);
        
        // South panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        JButton addButton = new JButton("Add Question");
        addButton.setBackground(new Color(46, 204, 113)); // Green color
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        
        addButton.addActionListener(e -> {
            try {
                String examId = examIdField.getText().trim();
                String category = categoryField.getText().trim();
                String level = levelField.getText().trim();
                String mcq = mcqField.getText().trim();
                String fib = fibField.getText().trim();
                String text = textField.getText().trim();
                int marks = Integer.parseInt(marksField.getText().trim());
                
                if (examId.isEmpty() || category.isEmpty() || level.isEmpty() || text.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!examService.examExists(examId)) {
                    JOptionPane.showMessageDialog(dialog, "Invalid Exam ID. Please check again.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String tempQuestionId = "TEMP";
                Question question = new Question(tempQuestionId, category, level, mcq, fib, text, marks);
                
                List<Option> options = new ArrayList<>();
                boolean correctSelected = false;
                
                for (int i = 0; i < 4; i++) {
                    String optionText = optionFields[i].getText().trim();
                    if (optionText.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "All options must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    char isCorrect = correctButtons[i].isSelected() ? 'Y' : 'N';
                    if (isCorrect == 'Y') correctSelected = true;
                    
                    Option option = new Option("TEMP_O" + (i + 1), optionText, isCorrect, tempQuestionId);
                    options.add(option);
                }
                
                if (!correctSelected) {
                    JOptionPane.showMessageDialog(dialog, "Please select one correct option.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean success = examService.addQuestionAndOptions(examId, question, options, userId);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Question added successfully.");
                    dialog.dispose();
                    refreshPanel("questions");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add question.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Invalid input! Make sure all fields are filled properly.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        
        // Add all components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
//    private void loadExaminerData() {
//        // Additional initialization if needed
//    }
//    
    
}