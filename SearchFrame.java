package railway;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SearchFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbFrom;
    private JComboBox<String> cmbTo;
    private JComboBox<String> cmbClass;

    static final Color PR_GREEN      = new Color(1, 84, 36);
    static final Color PR_DARK_GREEN = new Color(0, 50, 18);
    static final Color PR_GOLD       = new Color(255, 215, 0);
    static final Color PR_LIGHT      = new Color(240, 248, 243);

    public SearchFrame() {
        initComponents();
        loadCities();
        searchTrains();
    }

    private void initComponents() {
        setTitle("Pakistan Railway — Train Search");
        setSize(1000, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel main = new JPanel(null);
        main.setBackground(PR_LIGHT);
        setContentPane(main);

        // === HEADER ===
        JPanel header = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(PR_DARK_GREEN);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(PR_GOLD);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        header.setOpaque(false);
        header.setBounds(0, 0, 1000, 60);
        main.add(header);

        JLabel lblTitle = new JLabel("  Train Search — ٹرین تلاش کریں");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(PR_GOLD);
        lblTitle.setBounds(10, 12, 500, 34);
        header.add(lblTitle);

        JButton btnBack = makeHeaderBtn("← Dashboard", 860, 15);
        btnBack.addActionListener(e -> dispose());
        header.add(btnBack);

        // === SEARCH PANEL ===
        JPanel searchPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(210, 230, 215));
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        searchPanel.setOpaque(false);
        searchPanel.setBounds(0, 60, 1000, 90);
        main.add(searchPanel);

        // FROM label & dropdown
        JLabel lblFrom = new JLabel("From:");
        lblFrom.setFont(new Font("Arial", Font.BOLD, 13));
        lblFrom.setForeground(new Color(60,60,60));
        lblFrom.setBounds(20, 28, 50, 22);
        searchPanel.add(lblFrom);

        cmbFrom = new JComboBox<>();
        cmbFrom.setFont(new Font("Arial", Font.PLAIN, 13));
        cmbFrom.setBounds(72, 25, 180, 30);
        cmbFrom.setBorder(BorderFactory.createLineBorder(
            new Color(180,210,190),1));
        searchPanel.add(cmbFrom);

        // TO label & dropdown
        JLabel lblTo = new JLabel("To:");
        lblTo.setFont(new Font("Arial", Font.BOLD, 13));
        lblTo.setForeground(new Color(60,60,60));
        lblTo.setBounds(270, 28, 30, 22);
        searchPanel.add(lblTo);

        cmbTo = new JComboBox<>();
        cmbTo.setFont(new Font("Arial", Font.PLAIN, 13));
        cmbTo.setBounds(305, 25, 180, 30);
        cmbTo.setBorder(BorderFactory.createLineBorder(
            new Color(180,210,190),1));
        searchPanel.add(cmbTo);

        // CLASS label & dropdown
        JLabel lblClass = new JLabel("Class:");
        lblClass.setFont(new Font("Arial", Font.BOLD, 13));
        lblClass.setForeground(new Color(60,60,60));
        lblClass.setBounds(505, 28, 45, 22);
        searchPanel.add(lblClass);

        cmbClass = new JComboBox<>(new String[]{
            "All Classes", "Economy", "AC Standard",
            "AC Lower", "AC Upper", "Business"});
        cmbClass.setFont(new Font("Arial", Font.PLAIN, 13));
        cmbClass.setBounds(555, 25, 160, 30);
        cmbClass.setBorder(BorderFactory.createLineBorder(
            new Color(180,210,190),1));
        searchPanel.add(cmbClass);

        // SEARCH BUTTON
        JButton btnSearch = new JButton("Search Trains");
        btnSearch.setFont(new Font("Arial", Font.BOLD, 13));
        btnSearch.setBackground(PR_GREEN);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBounds(735, 22, 140, 34);
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.setBorder(BorderFactory.createLineBorder(PR_GOLD,1));
        btnSearch.addActionListener(e -> searchTrains());
        searchPanel.add(btnSearch);

        // RESET BUTTON
        JButton btnReset = new JButton("Reset");
        btnReset.setFont(new Font("Arial", Font.BOLD, 13));
        btnReset.setBackground(new Color(100,100,100));
        btnReset.setForeground(Color.WHITE);
        btnReset.setBounds(885, 22, 90, 34);
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.setBorder(BorderFactory.createEmptyBorder());
        btnReset.addActionListener(e -> {
            cmbFrom.setSelectedIndex(0);
            cmbTo.setSelectedIndex(0);
            cmbClass.setSelectedIndex(0);
            searchTrains();
        });
        searchPanel.add(btnReset);

        // Result count label
        JLabel lblResults = new JLabel("Showing all available trains");
        lblResults.setFont(new Font("Arial", Font.PLAIN, 11));
        lblResults.setForeground(new Color(100,100,100));
        lblResults.setBounds(20, 62, 400, 18);
        lblResults.setName("resultLabel");
        searchPanel.add(lblResults);

        // === RESULTS TABLE ===
        String[] cols = {"Train Name", "Number", "From",
                         "To", "Departure", "Arrival",
                         "Seats", "Fare (PKR)", "Status"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(34);
        table.setGridColor(new Color(220,235,225));
        table.setSelectionBackground(new Color(198,228,210));
        table.setSelectionForeground(PR_DARK_GREEN);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(PR_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.setShowVerticalLines(true);
        table.setAutoCreateRowSorter(true);

        int[] widths = {160, 90, 110, 120, 90, 90, 60, 100, 80};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Alternate row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t,
                    Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(
                    t, val, sel, foc, r, c);
                if (!sel) {
                    comp.setBackground(r%2==0 ? Color.WHITE
                        : new Color(245,251,247));
                }
                return comp;
            }
        });

        // Status badge
        table.getColumnModel().getColumn(8).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t,
                        Object val, boolean sel, boolean foc, int r, int c) {
                    JLabel lbl = new JLabel(
                        val!=null?val.toString():"", SwingConstants.CENTER);
                    lbl.setOpaque(true);
                    String v = val!=null?val.toString().toLowerCase():"";
                    if (v.equals("active")) {
                        lbl.setBackground(new Color(220,245,228));
                        lbl.setForeground(new Color(0,120,40));
                    } else {
                        lbl.setBackground(new Color(250,220,220));
                        lbl.setForeground(new Color(160,0,0));
                    }
                    lbl.setFont(new Font("Arial", Font.BOLD, 11));
                    return lbl;
                }
            });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(10, 160, 978, 390);
        scroll.setBorder(BorderFactory.createLineBorder(
            new Color(180,210,190),1));
        main.add(scroll);

        // === BOOK SELECTED BUTTON ===
        JButton btnBook = new JButton("Book Selected Train");
        btnBook.setFont(new Font("Arial", Font.BOLD, 13));
        btnBook.setBackground(new Color(0,100,160));
        btnBook.setForeground(Color.WHITE);
        btnBook.setBounds(390, 558, 200, 34);
        btnBook.setFocusPainted(false);
        btnBook.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBook.setBorder(BorderFactory.createLineBorder(
            new Color(0,150,220),1));
        btnBook.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this,
                    "Pehle koi train select karein!", "Warning",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            String trainName = (String) model.getValueAt(
                table.convertRowIndexToModel(row), 0);
            String from = (String) model.getValueAt(
                table.convertRowIndexToModel(row), 2);
            String to = (String) model.getValueAt(
                table.convertRowIndexToModel(row), 3);
            String fare = model.getValueAt(
                table.convertRowIndexToModel(row), 7).toString();
            JOptionPane.showMessageDialog(this,
                "Train: " + trainName + "\n" +
                "Route: " + from + " → " + to + "\n" +
                "Fare: PKR " + fare + "\n\n" +
                "Booking ke liye Manage Bookings mein jayein.",
                "Train Info", JOptionPane.INFORMATION_MESSAGE);
        });
        main.add(btnBook);

        // === FOOTER ===
        JPanel footer = new JPanel(null);
        footer.setBackground(PR_DARK_GREEN);
        footer.setBounds(0, 560, 1000, 30);
        main.add(footer);
        JLabel lf = new JLabel(
            "© 2025 Pakistan Railway  |  Train Search Module",
            SwingConstants.CENTER);
        lf.setFont(new Font("Arial", Font.PLAIN, 10));
        lf.setForeground(new Color(160,200,175));
        lf.setBounds(0,7,1000,16);
        footer.add(lf);

        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    // Load unique cities from database into dropdowns
    private void loadCities() {
        try {
            cmbFrom.addItem("All Cities");
            cmbTo.addItem("All Cities");
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT DISTINCT source FROM trains " +
                "UNION SELECT DISTINCT destination FROM trains " +
                "ORDER BY source");
            while (rs.next()) {
                String city = rs.getString(1);
                cmbFrom.addItem(city);
                cmbTo.addItem(city);
            }
        } catch (Exception e) { /* silent */ }
    }

    private void searchTrains() {
        model.setRowCount(0);
        try {
            String from  = cmbFrom.getSelectedItem().toString();
            String to    = cmbTo.getSelectedItem().toString();

            StringBuilder sql = new StringBuilder(
                "SELECT * FROM trains WHERE status='Active'");

            if (!from.equals("All Cities"))
                sql.append(" AND source='").append(from).append("'");
            if (!to.equals("All Cities"))
                sql.append(" AND destination='").append(to).append("'");

            sql.append(" ORDER BY departure_time");

            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                sql.toString());
            int count = 0;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("train_name"),
                    rs.getString("train_number"),
                    rs.getString("source"),
                    rs.getString("destination"),
                    rs.getString("departure_time"),
                    rs.getString("arrival_time"),
                    rs.getInt("total_seats"),
                    rs.getDouble("fare"),
                    rs.getString("status")
                });
                count++;
            }

            // Update result label
            for (Component c : ((JPanel)getContentPane()
                    .getComponent(1)).getComponents()) {
                if ("resultLabel".equals(c.getName())) {
                    ((JLabel)c).setText(count + " train(s) mili — " +
                        from + " se " + to + " tak");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton makeHeaderBtn(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setBackground(PR_GOLD);
        btn.setForeground(PR_DARK_GREEN);
        btn.setBounds(x, y, 120, 28);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder());
        return btn;
    }
}