package railway;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class PassengerFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    static final Color PR_GREEN      = new Color(1, 84, 36);
    static final Color PR_DARK_GREEN = new Color(0, 50, 18);
    static final Color PR_GOLD       = new Color(255, 215, 0);

    public PassengerFrame() {
        initComponents();
        loadPassengers();
    }

    private void initComponents() {
        setTitle("Pakistan Railway — Passenger Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(240, 248, 243));
        setContentPane(main);

        // === TOP SECTION (header + toolbar together) ===
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        main.add(topSection, BorderLayout.NORTH);

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PR_DARK_GREEN);
        header.setPreferredSize(new Dimension(0, 65));
        header.setBorder(BorderFactory.createMatteBorder(
            0, 0, 3, 0, PR_GOLD));
        topSection.add(header, BorderLayout.NORTH);

        JLabel lblTitle = new JLabel(
            "   Passenger Management — مسافر کا نظام");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(PR_GOLD);
        header.add(lblTitle, BorderLayout.CENTER);

        JPanel headerRight = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 15, 14));
        headerRight.setOpaque(false);
        JButton btnBack = new JButton("← Dashboard");
        btnBack.setFont(new Font("Arial", Font.BOLD, 12));
        btnBack.setBackground(PR_GOLD);
        btnBack.setForeground(PR_DARK_GREEN);
        btnBack.setPreferredSize(new Dimension(130, 36));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.setBorder(BorderFactory.createEmptyBorder());
        btnBack.addActionListener(e -> dispose());
        headerRight.add(btnBack);
        header.add(headerRight, BorderLayout.EAST);

        // TOOLBAR
        JPanel toolbar = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setBackground(Color.WHITE);
        toolbar.setPreferredSize(new Dimension(0, 55));
        toolbar.setBorder(BorderFactory.createMatteBorder(
            0, 0, 1, 0, new Color(210, 230, 215)));
        topSection.add(toolbar, BorderLayout.SOUTH);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Arial", Font.BOLD, 13));
        toolbar.add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(250, 32));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                new Color(180, 210, 190), 1),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterTable();
            }
        });
        toolbar.add(txtSearch);

        JButton btnAdd = makeBtn(
            "+ Add Passenger", PR_GREEN, 150, 32);
        btnAdd.addActionListener(e ->
            showAddEditDialog(false, -1));
        toolbar.add(btnAdd);

        JButton btnEdit = makeBtn(
            "✎ Edit", new Color(140, 100, 0), 90, 32);
        btnEdit.addActionListener(e -> editSelected());
        toolbar.add(btnEdit);

        JButton btnDel = makeBtn(
            "✕ Delete", new Color(160, 0, 0), 90, 32);
        btnDel.addActionListener(e -> deletePassenger());
        toolbar.add(btnDel);

        JButton btnRef = makeBtn(
            "↻ Refresh", new Color(0, 100, 160), 100, 32);
        btnRef.addActionListener(e -> loadPassengers());
        toolbar.add(btnRef);

        // === TABLE ===
        String[] cols = {
            "ID", "Full Name", "CNIC",
            "Phone", "Email", "Address"
        };
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.setGridColor(new Color(220, 235, 225));
        table.setSelectionBackground(new Color(198, 228, 210));
        table.setSelectionForeground(PR_DARK_GREEN);
        table.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(PR_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(
            new Dimension(0, 40));
        table.setAutoCreateRowSorter(true);

        // Alternate row colors
        table.setDefaultRenderer(Object.class,
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object val, boolean sel,
                        boolean foc, int r, int c) {
                    Component comp =
                        super.getTableCellRendererComponent(
                            t, val, sel, foc, r, c);
                    if (!sel)
                        comp.setBackground(r % 2 == 0
                            ? Color.WHITE
                            : new Color(245, 251, 247));
                    return comp;
                }
            });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(
            5, 10, 5, 10, new Color(240, 248, 243)));
        main.add(scroll, BorderLayout.CENTER);

        // === FOOTER ===
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(PR_DARK_GREEN);
        footer.setPreferredSize(new Dimension(0, 32));
        JLabel lf = new JLabel(
            "© 2025 Pakistan Railway  |  " +
            "Passenger Management Module",
            SwingConstants.CENTER);
        lf.setFont(new Font("Arial", Font.PLAIN, 11));
        lf.setForeground(new Color(160, 200, 175));
        footer.add(lf, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JButton makeBtn(String text, Color bg,
            int w, int h) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(w, h));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(
            2, 8, 2, 8));
        return btn;
    }

    private void loadPassengers() {
        model.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement()
                .executeQuery(
                    "SELECT passenger_id, full_name, " +
                    "cnic, phone, email, address " +
                    "FROM passengers " +
                    "ORDER BY passenger_id");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("passenger_id"),
                    rs.getString("full_name"),
                    rs.getString("cnic"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable() {
        String kw = txtSearch.getText()
            .toLowerCase().trim();
        model.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement()
                .executeQuery(
                    "SELECT * FROM passengers " +
                    "ORDER BY passenger_id");
            while (rs.next()) {
                String name = rs.getString("full_name")
                    .toLowerCase();
                String cnic = rs.getString("cnic")
                    .toLowerCase();
                String phone = rs.getString("phone")
                    .toLowerCase();
                if (name.contains(kw)
                        || cnic.contains(kw)
                        || phone.contains(kw)) {
                    model.addRow(new Object[]{
                        rs.getInt("passenger_id"),
                        rs.getString("full_name"),
                        rs.getString("cnic"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                    });
                }
            }
        } catch (Exception e) { /* silent */ }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Pehle koi passenger select karein!",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(
            table.convertRowIndexToModel(row), 0);
        showAddEditDialog(true, id);
    }

    private void showAddEditDialog(
            boolean isEdit, int passId) {
        JDialog dlg = new JDialog(this,
            isEdit ? "Passenger Edit Karein"
                   : "Naya Passenger Add Karein", true);
        dlg.setSize(480, 410);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(null);
        dlg.getContentPane().setBackground(Color.WHITE);

        // Dialog header
        JPanel dh = new JPanel(null);
        dh.setBackground(PR_GREEN);
        dh.setBounds(0, 0, 480, 48);
        dlg.add(dh);

        JLabel dTitle = new JLabel(isEdit
            ? "  ✎ Passenger Edit Karein"
            : "  + Naya Passenger Add Karein");
        dTitle.setFont(new Font("Arial", Font.BOLD, 15));
        dTitle.setForeground(PR_GOLD);
        dTitle.setBounds(0, 12, 480, 25);
        dh.add(dTitle);

        String[] labels = {
            "Full Name:", "CNIC:", "Phone:",
            "Email:", "Address:"
        };
        JTextField[] fields = new JTextField[5];

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            lbl.setForeground(new Color(60, 60, 60));
            lbl.setBounds(20, 62 + i * 50, 110, 22);
            dlg.add(lbl);

            fields[i] = new JTextField();
            fields[i].setFont(
                new Font("Arial", Font.PLAIN, 13));
            fields[i].setBounds(140, 62 + i * 50, 310, 32);
            fields[i].setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                        new Color(180, 210, 190), 1),
                    BorderFactory.createEmptyBorder(
                        2, 8, 2, 8)));
            dlg.add(fields[i]);
        }

        if (isEdit) {
            try {
                Connection conn =
                    DBConnection.getConnection();
                PreparedStatement ps =
                    conn.prepareStatement(
                        "SELECT * FROM passengers " +
                        "WHERE passenger_id=?");
                ps.setInt(1, passId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    fields[0].setText(
                        rs.getString("full_name"));
                    fields[1].setText(
                        rs.getString("cnic"));
                    fields[2].setText(
                        rs.getString("phone"));
                    fields[3].setText(
                        rs.getString("email"));
                    fields[4].setText(
                        rs.getString("address"));
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage());
            }
        }

        JButton btnSave = new JButton(
            isEdit ? "Update Karein" : "Save Karein");
        btnSave.setFont(new Font("Arial", Font.BOLD, 13));
        btnSave.setBackground(PR_GREEN);
        btnSave.setForeground(Color.WHITE);
        btnSave.setBounds(140, 360, 200, 38);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setBorder(BorderFactory.createLineBorder(
            PR_GOLD, 1));
        btnSave.addActionListener(e -> {
            if (fields[0].getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg,
                    "Full Name zaroor bharein!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Connection conn =
                    DBConnection.getConnection();
                String sql = isEdit
                    ? "UPDATE passengers SET " +
                      "full_name=?, cnic=?, phone=?, " +
                      "email=?, address=? " +
                      "WHERE passenger_id=?"
                    : "INSERT INTO passengers " +
                      "(full_name, cnic, phone, " +
                      "email, address) " +
                      "VALUES (?,?,?,?,?)";
                PreparedStatement ps =
                    conn.prepareStatement(sql);
                ps.setString(1,
                    fields[0].getText().trim());
                ps.setString(2,
                    fields[1].getText().trim());
                ps.setString(3,
                    fields[2].getText().trim());
                ps.setString(4,
                    fields[3].getText().trim());
                ps.setString(5,
                    fields[4].getText().trim());
                if (isEdit) ps.setInt(6, passId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(dlg,
                    isEdit
                        ? "Passenger update ho gaya!"
                        : "Naya passenger add ho gaya!",
                    "Kamyab",
                    JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadPassengers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        dlg.add(btnSave);
        dlg.setVisible(true);
    }

    private void deletePassenger() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Pehle koi passenger select karein!",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(
            table.convertRowIndexToModel(row), 0);
        String name = (String) model.getValueAt(
            table.convertRowIndexToModel(row), 1);
        int c = JOptionPane.showConfirmDialog(this,
            "Kya aap '" + name +
            "' delete karna chahte hain?",
            "Confirm", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM passengers " +
                "WHERE passenger_id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,
                "Passenger delete ho gaya!", "Kamyab",
                JOptionPane.INFORMATION_MESSAGE);
            loadPassengers();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}