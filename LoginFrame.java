package railway;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    static final Color PR_GREEN      = new Color(1, 84, 36);
    static final Color PR_DARK_GREEN = new Color(0, 50, 18);
    static final Color PR_GOLD       = new Color(255, 215, 0);

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Pakistan Railway Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // === OUTER PANEL - full green background ===
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(PR_GREEN);
        setContentPane(outer);

        // === TOP HEADER ===
        JPanel topPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(PR_DARK_GREEN);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // diagonal lines
                g2.setColor(new Color(255,255,255,10));
                g2.setStroke(new BasicStroke(1f));
                for (int i = -200; i < 1400; i += 20)
                    g2.drawLine(i, 0, i+200, 200);
                // gold bottom line
                g2.setColor(PR_GOLD);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(0, getHeight()-1,
                    getWidth(), getHeight()-1);
            }
        };
        topPanel.setLayout(new BoxLayout(
            topPanel, BoxLayout.Y_AXIS));
        topPanel.setPreferredSize(new Dimension(0, 200));
        topPanel.setOpaque(false);

        // Logo
        JPanel logoPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int s = Math.min(w, h) - 10;
                int ox = (w-s)/2, oy = (h-s)/2;
                g2.setColor(PR_GOLD);
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(ox, oy, s, s);
                g2.setColor(PR_DARK_GREEN);
                g2.fillOval(ox+2, oy+2, s-4, s-4);
                g2.setColor(PR_GOLD);
                g2.fillOval(ox+8, oy+8, s-16, s-16);
                g2.setColor(PR_DARK_GREEN);
                g2.fillOval(ox+18, oy+8, s-14, s-14);
                // star
                g2.setColor(PR_GOLD);
                int cx=ox+s-18, cy=oy+s/3;
                int or=8, ir=4, pts=5;
                int[] xs=new int[pts*2];
                int[] ys=new int[pts*2];
                double a=-Math.PI/2, step=Math.PI/pts;
                for(int i=0;i<pts*2;i++){
                    int r=(i%2==0)?or:ir;
                    xs[i]=(int)(cx+r*Math.cos(a));
                    ys[i]=(int)(cy+r*Math.sin(a));
                    a+=step;
                }
                g2.fillPolygon(xs,ys,pts*2);
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(100, 100));
        logoPanel.setMaximumSize(new Dimension(100, 100));
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(
            "PAKISTAN RAILWAY", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 32));
        lblTitle.setForeground(PR_GOLD);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUrdu = new JLabel(
            "پاکستان ریلوے", SwingConstants.CENTER);
        lblUrdu.setFont(new Font("Arial", Font.PLAIN, 16));
        lblUrdu.setForeground(new Color(200, 230, 210));
        lblUrdu.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(Box.createVerticalStrut(15));
        topPanel.add(logoPanel);
        topPanel.add(Box.createVerticalStrut(8));
        topPanel.add(lblTitle);
        topPanel.add(Box.createVerticalStrut(4));
        topPanel.add(lblUrdu);
        topPanel.add(Box.createVerticalStrut(15));

        outer.add(topPanel, BorderLayout.NORTH);

        // === CENTER - form card ===
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(PR_GREEN);
        outer.add(center, BorderLayout.CENTER);

        JPanel card = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0,
                    getWidth(), getHeight(), 20, 20);
                g2.setColor(PR_GOLD);
                g2.fillRoundRect(0, 0, getWidth(), 6, 6, 6);
                g2.fillRect(0, 3, getWidth(), 3);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(420, 300));

        JLabel lblSys = new JLabel(
            "Management System — Staff Login",
            SwingConstants.CENTER);
        lblSys.setFont(new Font("Arial", Font.BOLD, 14));
        lblSys.setForeground(PR_GREEN);
        lblSys.setBounds(0, 20, 420, 25);
        card.add(lblSys);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(220, 235, 225));
        sep.setBounds(30, 52, 360, 2);
        card.add(sep);

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Arial", Font.BOLD, 13));
        lblUser.setForeground(new Color(60,60,60));
        lblUser.setBounds(40, 65, 100, 22);
        card.add(lblUser);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUsername.setBounds(40, 90, 340, 40);
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                new Color(180,210,190), 1),
            BorderFactory.createEmptyBorder(4,10,4,10)));
        txtUsername.setBackground(new Color(250,253,251));
        card.add(txtUsername);

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Arial", Font.BOLD, 13));
        lblPass.setForeground(new Color(60,60,60));
        lblPass.setBounds(40, 145, 100, 22);
        card.add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setBounds(40, 170, 340, 40);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                new Color(180,210,190), 1),
            BorderFactory.createEmptyBorder(4,10,4,10)));
        txtPassword.setBackground(new Color(250,253,251));
        card.add(txtPassword);

        JButton btnLogin = new JButton("LOGIN") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed())
                    g2.setColor(PR_DARK_GREEN);
                else if (getModel().isRollover())
                    g2.setColor(new Color(2,110,48));
                else
                    g2.setColor(PR_GREEN);
                g2.fillRoundRect(0, 0,
                    getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                int x=(getWidth()-fm.stringWidth(getText()))/2;
                int y=(getHeight()+fm.getAscent()
                    -fm.getDescent())/2;
                g2.drawString(getText(), x, y);
            }
        };
        btnLogin.setBounds(40, 228, 340, 48);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> doLogin());
        card.add(btnLogin);

        txtPassword.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode()==KeyEvent.VK_ENTER)
                    doLogin();
            }
        });

        center.add(card);

        // === FOOTER ===
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(PR_DARK_GREEN);
        footer.setPreferredSize(new Dimension(0, 35));
        JLabel lblFoot = new JLabel(
            "© 2025 Pakistan Railway — " +
            "Ministry of Railways, Islamabad",
            SwingConstants.CENTER);
        lblFoot.setFont(new Font("Arial", Font.PLAIN, 11));
        lblFoot.setForeground(new Color(180,210,190));
        footer.add(lblFoot, BorderLayout.CENTER);
        outer.add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(
            txtPassword.getPassword()).trim();
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username aur Password dono bharein!",
                "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM users " +
                "WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String role  = rs.getString("role");
                int    uid   = rs.getInt("id");
                JOptionPane.showMessageDialog(this,
                    "Khush Amdeed " + username +
                    "!\nRole: " + role,
                    "Login Kamyab",
                    JOptionPane.INFORMATION_MESSAGE);
                new DashboardFrame(role, uid);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Galat Username ya Password!",
                    "Login Nakaam",
                    JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Database Error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}