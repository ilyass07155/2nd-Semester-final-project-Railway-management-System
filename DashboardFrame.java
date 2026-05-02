package railway;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DashboardFrame extends JFrame {

    private final int userId;
    private final String userRole;

    public DashboardFrame(String role, int userId) {
        this.userRole = role;
        this.userId   = userId;
        initComponents();
    }

    private void initComponents() {
        setTitle("Pakistan Railway MS - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(240,245,242));
        setContentPane(main);

        // === HEADER ===
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0,50,18));
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createMatteBorder(
            0,0,3,0,new Color(255,215,0)));
        main.add(header, BorderLayout.NORTH);

        // Left side of header - logo + title
        JPanel headerLeft = new JPanel(new FlowLayout(
            FlowLayout.LEFT, 15, 10));
        headerLeft.setOpaque(false);

        // Mini logo
        JPanel miniLogo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,215,0));
                g2.fillOval(2,2,46,46);
                g2.setColor(new Color(0,50,18));
                g2.fillOval(4,4,42,42);
                g2.setColor(new Color(255,215,0));
                g2.fillOval(8,8,32,32);
                g2.setColor(new Color(0,50,18));
                g2.fillOval(16,8,30,30);
                g2.setColor(new Color(255,215,0));
                int cx=38,cy=22,or=7,ir=3,pts=5;
                int[] x=new int[pts*2],y=new int[pts*2];
                double a=-Math.PI/2,s=Math.PI/pts;
                for(int i=0;i<pts*2;i++){
                    int r=(i%2==0)?or:ir;
                    x[i]=(int)(cx+r*Math.cos(a));
                    y[i]=(int)(cy+r*Math.sin(a));
                    a+=s;
                }
                g2.fillPolygon(x,y,pts*2);
            }
        };
        miniLogo.setOpaque(false);
        miniLogo.setPreferredSize(new Dimension(52,52));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(
            titlePanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(
            "PAKISTAN RAILWAY MANAGEMENT SYSTEM");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(new Color(255,215,0));

        JLabel lblSub = new JLabel(
            "Khush Amdeed, " + userRole +
            "  |  " + java.time.LocalDate.now());
        lblSub.setFont(new Font("Arial", Font.PLAIN, 13));
        lblSub.setForeground(new Color(180,220,195));

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblSub);

        headerLeft.add(miniLogo);
        headerLeft.add(titlePanel);
        header.add(headerLeft, BorderLayout.CENTER);

        // Logout button
        JPanel headerRight = new JPanel(new FlowLayout(
            FlowLayout.RIGHT, 15, 20));
        headerRight.setOpaque(false);
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogout.setBackground(new Color(180,0,0));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setPreferredSize(new Dimension(90,36));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(BorderFactory.createLineBorder(
            new Color(255,215,0),1));
        btnLogout.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this,
                "Kya aap logout karna chahte hain?",
                "Logout", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame();
            }
        });
        headerRight.add(btnLogout);
        header.add(headerRight, BorderLayout.EAST);

        // === CONTENT AREA ===
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(240,245,242));
        content.setBorder(BorderFactory.createEmptyBorder(
            20,30,20,30));
        main.add(content, BorderLayout.CENTER);

        // STATS ROW
        JPanel statsRow = new JPanel(new GridLayout(1,4,15,0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0,90));

        String[] statLabels = {
            "Trains","Passengers","Bookings","Users"};
        String[] statTables = {
            "trains","passengers","bookings","users"};
        Color[] statColors  = {
            new Color(1,84,36),  new Color(0,100,160),
            new Color(140,80,0), new Color(120,0,120)
        };

        for (int i = 0; i < 4; i++) {
            final String tbl = statTables[i];
            final Color  col = statColors[i];

            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(220,230,220),1),
                BorderFactory.createEmptyBorder(5,5,5,5)));

            JPanel topBar = new JPanel();
            topBar.setBackground(col);
            topBar.setPreferredSize(new Dimension(0,5));
            card.add(topBar, BorderLayout.NORTH);

            JLabel numLbl = new JLabel("...",
                SwingConstants.CENTER);
            numLbl.setFont(new Font("Arial", Font.BOLD, 32));
            numLbl.setForeground(col);
            card.add(numLbl, BorderLayout.CENTER);

            JLabel nameLbl = new JLabel(statLabels[i],
                SwingConstants.CENTER);
            nameLbl.setFont(new Font("Arial", Font.PLAIN, 13));
            nameLbl.setForeground(new Color(100,100,100));
            nameLbl.setPreferredSize(new Dimension(0,25));
            card.add(nameLbl, BorderLayout.SOUTH);

            statsRow.add(card);

            new Thread(() -> {
                try {
                    Connection conn =
                        DBConnection.getConnection();
                    ResultSet rs = conn.createStatement()
                        .executeQuery(
                            "SELECT COUNT(*) FROM " + tbl);
                    if (rs.next()) {
                        String cnt =
                            String.valueOf(rs.getInt(1));
                        SwingUtilities.invokeLater(
                            () -> numLbl.setText(cnt));
                    }
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(
                        () -> numLbl.setText("N/A"));
                }
            }).start();
        }

        content.add(statsRow, BorderLayout.NORTH);

        // CENTER - menu cards + password button
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(
            BorderFactory.createEmptyBorder(20,0,0,0));
        content.add(centerPanel, BorderLayout.CENTER);

        // Section label
        JLabel lblSec = new JLabel(
            "Quick Access — Manage System");
        lblSec.setFont(new Font("Arial", Font.BOLD, 15));
        lblSec.setForeground(new Color(60,60,60));
        lblSec.setPreferredSize(new Dimension(0,30));
        centerPanel.add(lblSec, BorderLayout.NORTH);

        // 2x2 grid of menu buttons
        JPanel menuGrid = new JPanel(new GridLayout(2,2,15,15));
        menuGrid.setOpaque(false);
        menuGrid.setBorder(
            BorderFactory.createEmptyBorder(10,0,10,0));
        centerPanel.add(menuGrid, BorderLayout.CENTER);

        String[] menuTitles = {
            "Manage Trains",
            "Manage Passengers",
            "Manage Bookings",
            "Search Trains"
        };
        String[] menuSubs = {
            "Train ki maloomat dekhein aur update karein",
            "Musafir ka record manage karein",
            "Tickets aur reservations dekhein",
            "Routes aur seats search karein"
        };
        Color[] menuColors = {
            new Color(1,84,36),
            new Color(0,100,160),
            new Color(140,80,0),
            new Color(100,0,100)
        };

        for (int i = 0; i < 4; i++) {
            final int   idx = i;
            final Color bc  = menuColors[i];
            final String title = menuTitles[i];
            final String sub   = menuSubs[i];

            JButton menuBtn = new JButton() {
                private boolean hov = false;
                {
                    addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e){
                            hov=true; repaint();}
                        public void mouseExited(MouseEvent e){
                            hov=false; repaint();}
                    });
                }
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(hov ? bc : Color.WHITE);
                    g2.fillRoundRect(0,0,
                        getWidth(),getHeight(),12,12);
                    g2.setColor(bc);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(1,1,
                        getWidth()-2,getHeight()-2,12,12);
                    g2.setFont(new Font("Arial",Font.BOLD,16));
                    g2.setColor(hov?Color.WHITE:bc);
                    g2.drawString(title,20,
                        getHeight()/2-8);
                    g2.setFont(new Font("Arial",Font.PLAIN,12));
                    g2.setColor(hov
                        ?new Color(210,235,215)
                        :new Color(110,110,110));
                    g2.drawString(sub,20,
                        getHeight()/2+14);
                    g2.setFont(new Font("Arial",Font.BOLD,22));
                    g2.setColor(hov?Color.WHITE:bc);
                    g2.drawString("→",getWidth()-40,
                        getHeight()/2+8);
                }
                @Override
                protected void paintBorder(Graphics g){}
            };
            menuBtn.setContentAreaFilled(false);
            menuBtn.setFocusPainted(false);
            menuBtn.setCursor(
                new Cursor(Cursor.HAND_CURSOR));
            menuBtn.addActionListener(
                e -> handleMenu(idx));
            menuGrid.add(menuBtn);
        }

        // Change Password button
        JPanel bottomPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        JButton btnCP = new JButton("Change Password");
        btnCP.setFont(new Font("Arial", Font.BOLD, 13));
        btnCP.setBackground(new Color(140,100,0));
        btnCP.setForeground(Color.WHITE);
        btnCP.setPreferredSize(new Dimension(220,42));
        btnCP.setFocusPainted(false);
        btnCP.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCP.setBorder(BorderFactory.createLineBorder(
            new Color(255,215,0),1));
        btnCP.addActionListener(e -> changePassword());
        bottomPanel.add(btnCP);
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        // === FOOTER ===
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(0,50,18));
        footer.setPreferredSize(new Dimension(0,32));
        JLabel lblFoot = new JLabel(
            "© 2025 Pakistan Railway  |  " +
            "Ministry of Railways, Islamabad  |  v1.0",
            SwingConstants.CENTER);
        lblFoot.setFont(new Font("Arial", Font.PLAIN, 11));
        lblFoot.setForeground(new Color(160,200,175));
        footer.add(lblFoot, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void handleMenu(int idx) {
        SwingUtilities.invokeLater(() -> {
            switch (idx) {
                case 0: new TrainFrame();     break;
                case 1: new PassengerFrame(); break;
                case 2: new BookingFrame();   break;
                case 3: new SearchFrame();    break;
            }
        });
    }

    private void changePassword() {
        JPasswordField oldPass     = new JPasswordField();
        JPasswordField newPass     = new JPasswordField();
        JPasswordField confirmPass = new JPasswordField();
        Object[] fields = {
            "Purana Password:", oldPass,
            "Naya Password:",   newPass,
            "Confirm Password:", confirmPass
        };
        int result = JOptionPane.showConfirmDialog(this,
            fields, "Password Tabdeel Karein",
            JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;
        String oldP = new String(oldPass.getPassword());
        String newP = new String(newPass.getPassword());
        String conP = new String(confirmPass.getPassword());
        if (!newP.equals(conP)) {
            JOptionPane.showMessageDialog(this,
                "Naya password match nahi karta!",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET password=? " +
                "WHERE id=? AND password=?");
            ps.setString(1, newP);
            ps.setInt(2, userId);
            ps.setString(3, oldP);
            int rows = ps.executeUpdate();
            JOptionPane.showMessageDialog(this,
                rows>0
                    ?"Password tabdeel ho gaya!"
                    :"Purana password galat hai!",
                rows>0?"Kamyab":"Error",
                rows>0
                    ?JOptionPane.INFORMATION_MESSAGE
                    :JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage());
        }
    }
}