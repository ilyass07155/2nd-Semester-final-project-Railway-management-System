package railway;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BookingFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    static final Color PR_GREEN      = new Color(1, 84, 36);
    static final Color PR_DARK_GREEN = new Color(0, 50, 18);
    static final Color PR_GOLD       = new Color(255, 215, 0);
    static final Color PR_LIGHT      = new Color(240, 248, 243);

    public BookingFrame() {
        initComponents();
        loadBookings();
    }

    private void initComponents() {
        setTitle("Pakistan Railway — Booking Management");
        setSize(1050, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel main = new JPanel(null);
        main.setBackground(PR_LIGHT);
        setContentPane(main);

        // === HEADER ===
        JPanel header = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(PR_DARK_GREEN);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(PR_GOLD);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        header.setOpaque(false);
        header.setBounds(0, 0, 1050, 60);
        main.add(header);

        JLabel lblTitle = new JLabel("  Booking Management — بکنگ کا نظام");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(PR_GOLD);
        lblTitle.setBounds(10, 12, 550, 34);
        header.add(lblTitle);

        JButton btnBack = makeHeaderBtn("← Dashboard", 910, 15);
        btnBack.addActionListener(e -> dispose());
        header.add(btnBack);

        // === TOOLBAR ===
        JPanel toolbar = new JPanel(null);
        toolbar.setBackground(Color.WHITE);
        toolbar.setBounds(0, 60, 1050, 52);
        toolbar.setBorder(BorderFactory.createMatteBorder(
            0, 0, 1, 0, new Color(210,230,215)));
        main.add(toolbar);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Arial", Font.BOLD, 12));
        lblSearch.setForeground(new Color(80,80,80));
        lblSearch.setBounds(15, 16, 55, 22);
        toolbar.add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 13));
        txtSearch.setBounds(72, 14, 200, 28);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180,210,190),1),
            BorderFactory.createEmptyBorder(2,8,2,8)));
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterTable(); }
        });
        toolbar.add(txtSearch);

        JButton btnAdd = makeToolBtn("+ New Booking", PR_GREEN, 285, 12);
        btnAdd.addActionListener(e -> showAddDialog());
        toolbar.add(btnAdd);

        JButton btnDel = makeToolBtn("✕ Cancel", new Color(160,0,0), 405, 12);
        btnDel.addActionListener(e -> cancelBooking());
        toolbar.add(btnDel);

        JButton btnRef = makeToolBtn("↻ Refresh", new Color(0,100,160), 495, 12);
        btnRef.addActionListener(e -> loadBookings());
        toolbar.add(btnRef);

        // === TABLE ===
        String[] cols = {"ID", "Passenger ID", "Train ID", "Booking Date",
                         "Seat No", "Class", "Status", "Total Fare (PKR)"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setGridColor(new Color(220,235,225));
        table.setSelectionBackground(new Color(198,228,210));
        table.setSelectionForeground(PR_DARK_GREEN);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(PR_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0,36));
        table.setAutoCreateRowSorter(true);

        int[] widths = {50, 100, 80, 120, 70, 100, 90, 130};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Alternate rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t,
                    Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(
                    t, val, sel, foc, r, c);
                if (!sel) comp.setBackground(r%2==0 ? Color.WHITE
                    : new Color(245,251,247));
                return comp;
            }
        });

        // Status badge renderer
        table.getColumnModel().getColumn(6).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t,
                        Object val, boolean sel, boolean foc, int r, int c) {
                    JLabel lbl = new JLabel(
                        val!=null?val.toString():"", SwingConstants.CENTER);
                    lbl.setOpaque(true);
                    String v = val!=null?val.toString().toLowerCase():"";
                    switch(v) {
                        case "confirmed":
                            lbl.setBackground(new Color(220,245,228));
                            lbl.setForeground(new Color(0,120,40)); break;
                        case "cancelled":
                            lbl.setBackground(new Color(250,220,220));
                            lbl.setForeground(new Color(160,0,0)); break;
                        default:
                            lbl.setBackground(new Color(255,245,210));
                            lbl.setForeground(new Color(140,100,0));
                    }
                    lbl.setFont(new Font("Arial", Font.BOLD, 11));
                    return lbl;
                }
            });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(10, 122, 1028, 390);
        scroll.setBorder(BorderFactory.createLineBorder(
            new Color(180,210,190),1));
        main.add(scroll);

        // === FOOTER ===
        JPanel footer = new JPanel(null);
        footer.setBackground(PR_DARK_GREEN);
        footer.setBounds(0, 520, 1050, 30);
        main.add(footer);
        JLabel lf = new JLabel(
            "© 2025 Pakistan Railway  |  Booking Management Module",
            SwingConstants.CENTER);
        lf.setFont(new Font("Arial", Font.PLAIN, 10));
        lf.setForeground(new Color(160,200,175));
        lf.setBounds(0,7,1050,16);
        footer.add(lf);

        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void loadBookings() {
        model.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT * FROM bookings ORDER BY booking_id");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("booking_id"),
                    rs.getInt("passenger_id"),
                    rs.getInt("train_id"),
                    rs.getString("booking_date"),
                    rs.getString("seat_number"),
                    rs.getString("class"),
                    rs.getString("status"),
                    rs.getDouble("total_fare")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(), "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable() {
        String kw = txtSearch.getText().toLowerCase().trim();
        model.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT * FROM bookings ORDER BY booking_id");
            while (rs.next()) {
                String status = rs.getString("status").toLowerCase();
                String cls    = rs.getString("class").toLowerCase();
                String seat   = rs.getString("seat_number").toLowerCase();
                if (status.contains(kw) || cls.contains(kw)
                        || seat.contains(kw)
                        || String.valueOf(rs.getInt("passenger_id")).contains(kw)
                        || String.valueOf(rs.getInt("train_id")).contains(kw)) {
                    model.addRow(new Object[]{
                        rs.getInt("booking_id"),
                        rs.getInt("passenger_id"),
                        rs.getInt("train_id"),
                        rs.getString("booking_date"),
                        rs.getString("seat_number"),
                        rs.getString("class"),
                        rs.getString("status"),
                        rs.getDouble("total_fare")
                    });
                }
            }
        } catch (Exception e) { /* silent */ }
    }

    private void showAddDialog() {
        JDialog dlg = new JDialog(this, "Nai Booking Karein", true);
        dlg.setSize(420, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(null);
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel dHeader = new JPanel(null);
        dHeader.setBackground(PR_GREEN);
        dHeader.setBounds(0, 0, 420, 45);
        dlg.add(dHeader);
        JLabel dTitle = new JLabel("  + Nai Booking Karein");
        dTitle.setFont(new Font("Arial", Font.BOLD, 14));
        dTitle.setForeground(PR_GOLD);
        dTitle.setBounds(0, 10, 420, 25);
        dHeader.add(dTitle);

        String[] labels = {"Passenger ID:", "Train ID:", "Booking Date:",
                           "Seat Number:", "Class:", "Total Fare (PKR):"};
        JTextField[] fields = new JTextField[5];
        JComboBox<String> classBox = new JComboBox<>(
            new String[]{"Economy", "AC Standard", "AC Lower", "AC Upper",
                         "Business"});

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            lbl.setForeground(new Color(60,60,60));
            lbl.setBounds(20, 55+i*42, 120, 22);
            dlg.add(lbl);

            if (i == 4) {
                classBox.setBounds(150, 55+i*42, 240, 28);
                classBox.setFont(new Font("Arial", Font.PLAIN, 13));
                dlg.add(classBox);
            } else {
                int fi = i < 4 ? i : i-1;
                fields[fi] = new JTextField();
                if (i == 2) fields[fi].setText(
                    java.time.LocalDate.now().toString());
                fields[fi].setFont(new Font("Arial", Font.PLAIN, 13));
                fields[fi].setBounds(150, 55+i*42, 240, 28);
                fields[fi].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(180,210,190),1),
                    BorderFactory.createEmptyBorder(2,8,2,8)));
                dlg.add(fields[fi]);
            }
        }

        JButton btnSave = new JButton("Booking Karein");
        btnSave.setFont(new Font("Arial", Font.BOLD, 13));
        btnSave.setBackground(PR_GREEN);
        btnSave.setForeground(Color.WHITE);
        btnSave.setBounds(100, 320, 200, 36);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setBorder(BorderFactory.createLineBorder(PR_GOLD,1));
        btnSave.addActionListener(e -> {
            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO bookings (passenger_id, train_id, " +
                    "booking_date, seat_number, class, status, total_fare) " +
                    "VALUES (?,?,?,?,?,?,?)");
                ps.setInt(1, Integer.parseInt(fields[0].getText()));
                ps.setInt(2, Integer.parseInt(fields[1].getText()));
                ps.setString(3, fields[2].getText());
                ps.setString(4, fields[3].getText());
                ps.setString(5, classBox.getSelectedItem().toString());
                ps.setString(6, "Confirmed");
                ps.setDouble(7, Double.parseDouble(fields[4].getText()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(dlg,
                    "Booking kamyab ho gayi!", "Kamyab",
                    JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadBookings();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg,
                    "Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        dlg.add(btnSave);
        dlg.setVisible(true);
    }

    private void cancelBooking() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Pehle koi booking select karein!", "Warning",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(
            table.convertRowIndexToModel(row), 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Kya aap booking #" + id + " cancel karna chahte hain?",
            "Cancel Confirm", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE bookings SET status='Cancelled' WHERE booking_id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,
                "Booking cancel ho gayi!", "Kamyab",
                JOptionPane.INFORMATION_MESSAGE);
            loadBookings();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton makeToolBtn(String text, Color bg, int x, int y) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBounds(x, y, 110, 28);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(2,8,2,8));
        return btn;
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