package railway;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TrainFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    static final Color PR_GREEN      = new Color(1, 84, 36);
    static final Color PR_DARK_GREEN = new Color(0, 50, 18);
    static final Color PR_GOLD       = new Color(255, 215, 0);
    static final Color PR_LIGHT      = new Color(240, 248, 243);

    public TrainFrame() {
        initComponents();
        loadTrains();
    }

    private void initComponents() {
        setTitle("Pakistan Railway — Train Management");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);

        JPanel main = new JPanel(null);
        main.setBackground(PR_LIGHT);
        setContentPane(main);

        // === HEADER ===
        JPanel header = new JPanel(null);
        header.setBackground(PR_DARK_GREEN);
        header.setBounds(0, 0, 1000, 60);
        main.add(header);

        JPanel goldLine = new JPanel();
        goldLine.setBackground(PR_GOLD);
        goldLine.setBounds(0, 57, 1000, 3);
        header.add(goldLine);

        JLabel lblTitle = new JLabel(
            "  Train Management — ٹرین کا نظام");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(PR_GOLD);
        lblTitle.setBounds(10, 12, 600, 34);
        header.add(lblTitle);

        JButton btnBack = new JButton("← Dashboard");
        btnBack.setFont(new Font("Arial", Font.BOLD, 11));
        btnBack.setBackground(PR_GOLD);
        btnBack.setForeground(PR_DARK_GREEN);
        btnBack.setBounds(860, 15, 120, 28);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.setBorder(BorderFactory.createEmptyBorder());
        btnBack.addActionListener(e -> dispose());
        header.add(btnBack);

        // === TOOLBAR ===
        JPanel toolbar = new JPanel(null);
        toolbar.setBackground(Color.WHITE);
        toolbar.setBounds(0, 60, 1000, 52);
        toolbar.setBorder(BorderFactory.createMatteBorder(
            0, 0, 1, 0, new Color(210, 230, 215)));
        main.add(toolbar);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Arial", Font.BOLD, 12));
        lblSearch.setForeground(new Color(80, 80, 80));
        lblSearch.setBounds(15, 16, 60, 22);
        toolbar.add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 13));
        txtSearch.setBounds(75, 14, 200, 28);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                new Color(180, 210, 190), 1),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterTable(); }
        });
        toolbar.add(txtSearch);

        JButton btnAdd = makeBtn("+ Add Train", PR_GREEN, 290, 12, 110);
        btnAdd.addActionListener(e -> showAddEditDialog(false, -1));
        toolbar.add(btnAdd);

        JButton btnEdit = makeBtn("✎ Edit",
            new Color(140, 100, 0), 410, 12, 90);
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this,
                    "Pehle koi train select karein!",
                    "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) model.getValueAt(
                table.convertRowIndexToModel(row), 0);
            showAddEditDialog(true, id);
        });
        toolbar.add(btnEdit);

        JButton btnDel = makeBtn("✕ Delete",
            new Color(160, 0, 0), 510, 12, 90);
        btnDel.addActionListener(e -> deleteTrain());
        toolbar.add(btnDel);

        JButton btnRef = makeBtn("↻ Refresh",
            new Color(0, 100, 160), 610, 12, 95);
        btnRef.addActionListener(e -> loadTrains());
        toolbar.add(btnRef);

        // === TABLE ===
        String[] cols = {
            "ID", "Train Name", "Number", "Source",
            "Destination", "Departure", "Arrival",
            "Seats", "Fare (PKR)", "Status"
        };
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setGridColor(new Color(220, 235, 225));
        table.setSelectionBackground(new Color(198, 228, 210));
        table.setSelectionForeground(PR_DARK_GREEN);
        table.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(PR_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(
            new Dimension(0, 36));
        table.setAutoCreateRowSorter(true);

        int[] widths = {40,150,90,100,110,85,85,55,90,80};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i)
                .setPreferredWidth(widths[i]);

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

        // Status badge renderer
        table.getColumnModel().getColumn(9).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object val, boolean sel,
                        boolean foc, int r, int c) {
                    JLabel lbl = new JLabel(
                        val != null ? val.toString() : "",
                        SwingConstants.CENTER);
                    lbl.setOpaque(true);
                    String v = val != null
                        ? val.toString().toLowerCase() : "";
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
        scroll.setBounds(10, 122, 978, 415);
        scroll.setBorder(BorderFactory.createLineBorder(
            new Color(180, 210, 190), 1));
        main.add(scroll);

        // === FOOTER ===
        JPanel footer = new JPanel(null);
        footer.setBackground(PR_DARK_GREEN);
        footer.setBounds(0, 548, 1000, 30);
        main.add(footer);

        JLabel lf = new JLabel(
            "© 2025 Pakistan Railway  |  Train Management Module",
            SwingConstants.CENTER);
        lf.setFont(new Font("Arial", Font.PLAIN, 10));
        lf.setForeground(new Color(160, 200, 175));
        lf.setBounds(0, 7, 1000, 16);
        footer.add(lf);

        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    // ===================================================
    // LOAD TRAINS FROM DATABASE
    // ===================================================
    private void loadTrains() {
        model.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                JOptionPane.showMessageDialog(this,
                    "Database connection failed!",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String sql = "SELECT train_id, train_name, " +
                "train_number, source, destination, " +
                "departure_time, arrival_time, " +
                "total_seats, fare, status FROM trains " +
                "ORDER BY train_id";
            ResultSet rs = conn.createStatement()
                .executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("train_id"),
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
            }
            rs.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading trains:\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================================================
    // SEARCH / FILTER
    // ===================================================
    private void filterTable() {
        String kw = txtSearch.getText().toLowerCase().trim();
        model.setRowCount(0);
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT train_id, train_name, " +
                "train_number, source, destination, " +
                "departure_time, arrival_time, " +
                "total_seats, fare, status FROM trains " +
                "ORDER BY train_id";
            ResultSet rs = conn.createStatement()
                .executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString("train_name")
                    .toLowerCase();
                String num  = rs.getString("train_number")
                    .toLowerCase();
                String src  = rs.getString("source")
                    .toLowerCase();
                String dst  = rs.getString("destination")
                    .toLowerCase();
                if (name.contains(kw) || num.contains(kw)
                        || src.contains(kw)
                        || dst.contains(kw)) {
                    model.addRow(new Object[]{
                        rs.getInt("train_id"),
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
                }
            }
        } catch (Exception e) { /* silent */ }
    }

    // ===================================================
    // ADD / EDIT DIALOG
    // ===================================================
    private void showAddEditDialog(boolean isEdit, int trainId) {
        JDialog dlg = new JDialog(this,
            isEdit ? "Train Edit Karein"
                   : "Nai Train Add Karein", true);
        dlg.setSize(440, 530);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(null);
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel dh = new JPanel(null);
        dh.setBackground(PR_GREEN);
        dh.setBounds(0, 0, 440, 45);
        dlg.add(dh);

        JLabel dTitle = new JLabel(isEdit
            ? "  ✎ Train Edit Karein"
            : "  + Nai Train Add Karein");
        dTitle.setFont(new Font("Arial", Font.BOLD, 14));
        dTitle.setForeground(PR_GOLD);
        dTitle.setBounds(0, 10, 440, 25);
        dh.add(dTitle);

        String[] labels = {
            "Train Name:", "Train Number:", "Source City:",
            "Destination:", "Departure:", "Arrival:",
            "Total Seats:", "Fare (PKR):", "Status:"
        };
        JTextField[] fields = new JTextField[8];
        JComboBox<String> statusBox = new JComboBox<>(
            new String[]{"Active", "Inactive"});

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            lbl.setForeground(new Color(60, 60, 60));
            lbl.setBounds(20, 55 + i * 38, 120, 22);
            dlg.add(lbl);

            if (i < 8) {
                fields[i] = new JTextField();
                fields[i].setFont(
                    new Font("Arial", Font.PLAIN, 13));
                fields[i].setBounds(
                    150, 55 + i * 38, 260, 28);
                fields[i].setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                            new Color(180,210,190),1),
                        BorderFactory.createEmptyBorder(
                            2,8,2,8)));
                dlg.add(fields[i]);
            } else {
                statusBox.setBounds(
                    150, 55 + i * 38, 260, 28);
                statusBox.setFont(
                    new Font("Arial", Font.PLAIN, 13));
                dlg.add(statusBox);
            }
        }

        if (isEdit) {
            try {
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM trains WHERE train_id=?");
                ps.setInt(1, trainId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    fields[0].setText(
                        rs.getString("train_name"));
                    fields[1].setText(
                        rs.getString("train_number"));
                    fields[2].setText(rs.getString("source"));
                    fields[3].setText(
                        rs.getString("destination"));
                    fields[4].setText(
                        rs.getString("departure_time"));
                    fields[5].setText(
                        rs.getString("arrival_time"));
                    fields[6].setText(String.valueOf(
                        rs.getInt("total_seats")));
                    fields[7].setText(String.valueOf(
                        rs.getDouble("fare")));
                    statusBox.setSelectedItem(
                        rs.getString("status"));
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
        btnSave.setBounds(110, 465, 200, 36);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setBorder(BorderFactory.createLineBorder(
            PR_GOLD, 1));
        btnSave.addActionListener(e -> {
            try {
                if (fields[0].getText().trim().isEmpty()
                        || fields[1].getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dlg,
                        "Train Name aur Number zaroor bharein!",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Connection conn = DBConnection.getConnection();
                String sql = isEdit
                    ? "UPDATE trains SET train_name=?, " +
                      "train_number=?, source=?, " +
                      "destination=?, departure_time=?, " +
                      "arrival_time=?, total_seats=?, " +
                      "fare=?, status=? WHERE train_id=?"
                    : "INSERT INTO trains (train_name, " +
                      "train_number, source, destination, " +
                      "departure_time, arrival_time, " +
                      "total_seats, fare, status) " +
                      "VALUES (?,?,?,?,?,?,?,?,?)";
                PreparedStatement ps =
                    conn.prepareStatement(sql);
                ps.setString(1, fields[0].getText().trim());
                ps.setString(2, fields[1].getText().trim());
                ps.setString(3, fields[2].getText().trim());
                ps.setString(4, fields[3].getText().trim());
                ps.setString(5, fields[4].getText().trim());
                ps.setString(6, fields[5].getText().trim());
                ps.setInt(7, fields[6].getText().trim()
                    .isEmpty() ? 0
                    : Integer.parseInt(
                        fields[6].getText().trim()));
                ps.setDouble(8, fields[7].getText().trim()
                    .isEmpty() ? 0
                    : Double.parseDouble(
                        fields[7].getText().trim()));
                ps.setString(9,
                    statusBox.getSelectedItem().toString());
                if (isEdit) ps.setInt(10, trainId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(dlg,
                    isEdit ? "Train update ho gayi!"
                           : "Nai train add ho gayi!",
                    "Kamyab", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadTrains();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg,
                    "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dlg.add(btnSave);
        dlg.setVisible(true);
    }

    // ===================================================
    // DELETE TRAIN
    // ===================================================
    private void deleteTrain() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Pehle koi train select karein!",
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(
            table.convertRowIndexToModel(row), 0);
        String name = (String) model.getValueAt(
            table.convertRowIndexToModel(row), 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Kya aap '" + name + "' delete karna chahte hain?",
            "Delete Confirm", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM trains WHERE train_id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this,
                "Train delete ho gayi!", "Kamyab",
                JOptionPane.INFORMATION_MESSAGE);
            loadTrains();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================================================
    // HELPER — make toolbar button
    // ===================================================
    private JButton makeBtn(String text, Color bg,
            int x, int y, int w) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBounds(x, y, w, 28);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(
            2, 8, 2, 8));
        return btn;
    }
}