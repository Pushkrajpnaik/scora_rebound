package gui;

import db.DBConnection;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * StudentPortal class represents the main interface for student users. It
 * provides access to upcoming exams, past results, and profile information.
 */
public class StudentPortal extends JFrame {

    private String userId;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    /**
     * Constructor for StudentPortal
     *
     * @param userId The ID of the student user
     */
    public StudentPortal(String userId) {
        this.userId = userId;
        initializeUI();
        loadStudentData();
        
        setVisible(true);
    }

    /**
     * Initializes the user interface components Sets up the main layout with
     * header, navigation, and content panels
     */
    private void initializeUI() {
        setTitle("Student Portal - Online Examination System");
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
        cardPanel.add(createUpcomingExamsPanel(), "upcoming");
        cardPanel.add(createPastResultsPanel(), "results");
        cardPanel.add(createProfilePanel(), "profile");

        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(navPanel, BorderLayout.WEST);
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    /**
     * Creates the header panel with welcome message and logout button
     *
     * @return JPanel containing the header elements
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(73, 125, 116));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT Name FROM user WHERE userID = ?");
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JLabel welcomeLabel = new JLabel("Welcome, " + rs.getString("Name"));
                welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
                welcomeLabel.setForeground(Color.WHITE);
                headerPanel.add(welcomeLabel, BorderLayout.WEST);
            }

            rs.close();
            ps.close();
            conn.close();
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

    /**
     * Creates the navigation panel with buttons for switching between views
     *
     * @return JPanel containing navigation buttons
     */
    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] navItems = {"Upcoming Exams", "Past Results", "Profile"};
        String[] cardNames = {"upcoming", "results", "profile"};

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

    /**
     * Creates the panel showing upcoming exams
     *
     * @return JPanel with upcoming exams data
     */
    private JPanel createUpcomingExamsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(234, 233, 232));
        
        JLabel titleLabel = new JLabel("Upcoming Exams");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(73, 125, 116));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Table to display eligible exams
        String[] columnNames = {"Exam ID", "Subject", "Total Marks", "Duration", "Status", "Action"};
        Object[][] data = getEligibleExamsData();
        
        // Create table model that makes only the Action column editable
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        
        JTable examTable = new JTable(model);
        examTable.setRowHeight(30);
        examTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Set up the button column with custom renderer and editor
        examTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setBackground(Color.WHITE);
                JButton startBtn = new JButton("Start Exam");
                startBtn.setBackground(new Color(46, 204, 113)); // Green color
                startBtn.setForeground(Color.WHITE);
                startBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                startBtn.setBorderPainted(false);
                startBtn.setFocusPainted(false);
                panel.add(startBtn);
                return panel;
            }
        });
        
        examTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setBackground(Color.WHITE);
                JButton startBtn = new JButton("Start Exam");
                startBtn.setBackground(new Color(46, 204, 113)); // Green color
                startBtn.setForeground(Color.WHITE);
                startBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                startBtn.setBorderPainted(false);
                startBtn.setFocusPainted(false);
                
                startBtn.addActionListener(e -> {
                    String examId = (String) table.getValueAt(row, 0);
                    SwingUtilities.invokeLater(() -> {
                        new ExamPage(userId, examId).setVisible(true);
                        dispose();
                    });
                });
                
                panel.add(startBtn);
                return panel;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(examTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Retrieves eligible exams data from the database for the logged-in student
     *
     * @return 2D array of exam data to display in table
     */
    private Object[][] getEligibleExamsData() {
        try {
            Connection conn = DBConnection.getConnection();

            // SQL query to find eligible exams for the logged-in student
            // Matches students with exams where:
            // 1. Examiner's department matches student's course
            // 2. Examiner is associated with the exam
            String query = "SELECT "
                    + "    s.userID, "
                    + "    u.Name AS StudentName, "
                    + "    e.ExamID, "
                    + "    e.TotalMarks, "
                    + "    e.Duration, "
                    + "    e.Subject, "
                    + "    e.Status "
                    + "FROM "
                    + "    Student s "
                    + "JOIN "
                    + "    User u ON s.userID = u.userID "
                    + "JOIN "
                    + "    Examiner ex ON s.Course = ex.Department "
                    + "JOIN "
                    + "    Exam e ON ex.userID = e.UserID "
                    + "WHERE "
                    + "    s.userID = ? "
                    + "ORDER BY "
                    + "    e.ExamID";

            PreparedStatement pstmt = conn.prepareStatement(query,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setString(1, userId);

            ResultSet rs = pstmt.executeQuery();

            // Count the number of rows
            rs.last();
            int rowCount = rs.getRow();
            rs.beforeFirst();

            Object[][] data = new Object[rowCount][6];
            int i = 0;

            while (rs.next()) {
                data[i][0] = rs.getString("ExamID");
                data[i][1] = rs.getString("Subject");
                data[i][2] = rs.getString("TotalMarks");
                data[i][3] = rs.getString("Duration");
                data[i][4] = rs.getString("Status");
                data[i][5] = "Start Exam";
                i++;
            }

            rs.close();
            pstmt.close();
            conn.close();

            return data;
        } catch (Exception e) {
            e.printStackTrace(); // Keep this for debugging
            // Return a fallback dataset if there's an error
            return new Object[][]{
                {"E001", "Data Structures", "8", "20 mins", "Available", "Start Exam"},
                {"E002", "Introduction to AI", "8", "20 mins", "Available", "Start Exam"}
            };
        }
    }

    /**
     * Creates the panel showing past exam results
     *
     * @return JPanel with past results data
     */
    private JPanel createPastResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(234, 233, 232));
        
        JLabel titleLabel = new JLabel("Past Results");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(73, 125, 116));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Table to display past results
        String[] columnNames = {"Exam ID", "Subject", "Result"};
        Object[][] data = getPastResultsData();
        
        // Create table model that makes only the Result column editable
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };
        
        JTable resultsTable = new JTable(model);
        resultsTable.setRowHeight(30);
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Set up the button column with custom renderer and editor
        resultsTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setBackground(Color.WHITE);
                JButton viewBtn = new JButton("View Result");
                viewBtn.setBackground(new Color(46, 204, 113)); // Green color
                viewBtn.setForeground(Color.WHITE);
                viewBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                viewBtn.setBorderPainted(false);
                viewBtn.setFocusPainted(false);
                panel.add(viewBtn);
                return panel;
            }
        });
        
        resultsTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.setBackground(Color.WHITE);
                JButton viewBtn = new JButton("View Result");
                viewBtn.setBackground(new Color(46, 204, 113)); // Green color
                viewBtn.setForeground(Color.WHITE);
                viewBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                viewBtn.setBorderPainted(false);
                viewBtn.setFocusPainted(false);
                
                viewBtn.addActionListener(e -> {
                    String examId = (String) table.getValueAt(row, 0);
                    SwingUtilities.invokeLater(() -> {
                        new ResultPage(userId, examId).setVisible(true);
                        dispose();
                    });
                });
                
                panel.add(viewBtn);
                return panel;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Retrieves past results data from the database
     *
     * @return 2D array of result data to display in table
     */
    private Object[][] getPastResultsData() {
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT a.ExamID, e.Subject " +
                           "FROM Attempt a " +
                           "JOIN Exam e ON a.ExamID = e.ExamID " +
                           "WHERE a.UserID = ?";
            PreparedStatement ps = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                         ResultSet.CONCUR_READ_ONLY);
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            // Count rows
            rs.last();
            int rowCount = rs.getRow();
            rs.beforeFirst();

            Object[][] data = new Object[rowCount][3]; // 3 columns: ExamID, Subject, Button
            int i = 0;
            while (rs.next()) {
                data[i][0] = rs.getString("ExamID");
                data[i][1] = rs.getString("Subject");
                data[i][2] = "View Result"; // Button text, result page will handle generation
                i++;
            }

            // Close resources
            rs.close();
            ps.close();
            conn.close();

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0][0];
        }
    }

    /**
     * Creates the panel showing user profile information
     *
     * @return JPanel with profile data
     */
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(234, 233, 232)); // Light gray background
        
        JLabel titleLabel = new JLabel("Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(73, 125, 116)); // Teal green color
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel profilePanel = new JPanel(new GridLayout(5, 2, 10, 10));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        try {
            // Query joins user and student tables to get complete profile information
            Connection conn = DBConnection.getConnection();
            String query = "SELECT u.Name, u.userID, s.Course, s.AcademicYear " +
                          "FROM user u JOIN student s ON u.userID = s.userID " +
                          "WHERE u.userID = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                // Display profile fields with labels
                addProfileField(profilePanel, "Student ID:", rs.getString("userID"));
                addProfileField(profilePanel, "Name:", rs.getString("Name"));
                addProfileField(profilePanel, "Course:", rs.getString("Course"));
                addProfileField(profilePanel, "Academic Year:", rs.getString("AcademicYear"));
            }
            
            // Close resources
            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        panel.add(profilePanel, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Helper method to add a label-value pair to the profile panel
     * @param panel The panel to add fields to
     * @param label The field label
     * @param value The field value
     */
    private void addProfileField(JPanel panel, String label, String value) {
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(fieldLabel);
        
        JLabel fieldValue = new JLabel(value);
        fieldValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(fieldValue);
    }
    
    /**
     * Loads additional student data if needed
     * Currently a placeholder for future functionality
     */
    private void loadStudentData() {
        // Additional initialization if needed
    }
    
    /**
     * Public getter for userId to be used by ButtonEditor
     * @return The current user ID
     */
    public String getUserId() {
        return userId;
    }
}