import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class TranslationClient extends JFrame {
    private JTextField txtHost, txtPort;
    private JComboBox<String> cbLang;
    private JTextArea txtLog;
    private File selectedFile;
    private JLabel lblFileName;

    // --- DEEP PURPLE PALETTE ---
    private final Color BG_MAIN = new Color(18, 11, 28);          // Nền tím đen ngoài cùng
    private final Color BG_PANEL = new Color(26, 18, 38);         // Nền panel sáng hơn một chút
    private final Color ACCENT_COLOR = new Color(168, 85, 247);   // Màu tím sáng làm điểm nhấn
    private final Color TEXT_MAIN = new Color(243, 244, 246);     // Chữ trắng xám dễ đọc
    private final Color TEXT_MUTED = new Color(156, 163, 175);    // Chữ xám mờ cho thông tin phụ
    private final Color BORDER_COLOR = new Color(60, 46, 85);     // Viền màu tím mờ
    private final Color LOG_BG = new Color(10, 6, 15);            // Nền log console cực tối
    private final Color INPUT_BG = new Color(20, 14, 30);         // Nền cho ô nhập liệu (Textfield)

    public TranslationClient() {
        setupTheme();
        setTitle("Translation Proxy | v2.0 - Stable");
        setSize(600, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);
        
        initComponents();
    }

    private void setupTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Label.foreground", TEXT_MAIN);
            UIManager.put("Panel.background", BG_MAIN);
        } catch (Exception ignored) {}
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- 1. TOP HEADER ---
        JLabel title = new JLabel("NETWORK TRANSLATOR SYSTEM", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ACCENT_COLOR); // Dùng màu tím sáng cho tiêu đề
        add(title, BorderLayout.NORTH);

        // --- 2. CENTER CONTENT ---
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.weightx = 1;

        // Group: Connection
        JPanel pnlConn = createModernPanel("MẠNG & KẾT NỐI");
        pnlConn.setLayout(new GridLayout(2, 2, 10, 10));
        pnlConn.add(createLabel("IP Server:"));
        txtHost = createInputField("127.0.0.1");
        pnlConn.add(txtHost);
        pnlConn.add(createLabel("Cổng (Port):"));
        txtPort = createInputField("9999");
        pnlConn.add(txtPort);

        // Group: Source
        JPanel pnlSource = createModernPanel("DỮ LIỆU ĐẦU VÀO");
        pnlSource.setLayout(new BorderLayout(10, 10));
        cbLang = new JComboBox<>(new String[]{"Vietnamese (vi)", "Japanese (ja)", "English (en)", "Spanish (es)"});
        cbLang.setBackground(Color.RED);       // Đổi nền thành tối
        cbLang.setForeground(TEXT_MAIN);      // Chữ sáng
        cbLang.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Nút chọn file dùng màu tím trầm để phân biệt với nút Gửi
        JButton btnFile = createModernButton("CHỌN FILE", new Color(45, 35, 60)); 
        lblFileName = new JLabel("Chưa chọn file...");
        lblFileName.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblFileName.setForeground(TEXT_MUTED);
        
        JPanel fileBox = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fileBox.setOpaque(false);
        fileBox.add(btnFile);
        fileBox.add(lblFileName);
        
        pnlSource.add(cbLang, BorderLayout.NORTH);
        pnlSource.add(fileBox, BorderLayout.CENTER);

        gbc.gridy = 0; mainPanel.add(pnlConn, gbc);
        gbc.gridy = 1; mainPanel.add(pnlSource, gbc);
        add(mainPanel, BorderLayout.CENTER);

        // --- 3. BOTTOM AREA (LOG & ACTION) ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        // Nút gửi dùng màu tím sáng nổi bật
        JButton btnSend = createModernButton("GỬI YÊU CẦU DỊCH", ACCENT_COLOR);
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSend.setPreferredSize(new Dimension(0, 45));

        txtLog = new JTextArea(8, 0);
        txtLog.setEditable(false);
        txtLog.setBackground(LOG_BG);
        txtLog.setForeground(TEXT_MAIN);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtLog.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane spLog = new JScrollPane(txtLog);
        spLog.setBorder(new TitledBorder(new LineBorder(BORDER_COLOR), "NHẬT KÝ HOẠT ĐỘNG (LOG)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), TEXT_MAIN));
        spLog.getVerticalScrollBar().setBackground(BG_PANEL); // Đổi màu thanh cuộn

        bottomPanel.add(btnSend, BorderLayout.NORTH);
        bottomPanel.add(spLog, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- EVENTS ---
        btnFile.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = jfc.getSelectedFile();
                lblFileName.setText(selectedFile.getName());
                log("[INFO] Đã tải file: " + selectedFile.getName());
            }
        });

        btnSend.addActionListener(e -> {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this, "Hãy chọn file trước!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new Thread(this::executeTask).start();
        });
    }

    private void executeTask() {
        String ip = txtHost.getText().trim();
        int port = Integer.parseInt(txtPort.getText().trim());
        String lang = ((String)cbLang.getSelectedItem()).split("\\(")[1].replace(")", "");

        log(">>> [SOCKET] Đang kết nối tới " + ip + ":" + port);
        try (Socket s = new Socket(ip, port)) {
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            DataInputStream in = new DataInputStream(s.getInputStream());

            // Đóng gói Header
            String json = String.format("{\"lang\":\"%s\",\"filename\":\"%s\"}", lang, selectedFile.getName());
            log(">>> [SEND] Dữ liệu Metadata (" + json.length() + " bytes)");
            sendBlock(out, json.getBytes("UTF-8"));

            // Đóng gói File
            byte[] data = Files.readAllBytes(selectedFile.toPath());
            log(">>> [SEND] Dữ liệu File (" + data.length + " bytes)");
            sendBlock(out, data);

            // Chờ nhận
            log("<<< [WAIT] Server đang xử lý...");
            byte[] headRes = readBlock(in);
            log("<<< [RECV] Phản hồi: " + new String(headRes));
            
            byte[] bodyRes = readBlock(in);
            File outF = new File(selectedFile.getParent(), "AI_TRANS_" + selectedFile.getName());
            Files.write(outF.toPath(), bodyRes);
            
            log("[SUCCESS] Đã lưu kết quả tại: " + outF.getName());
            JOptionPane.showMessageDialog(this, "Dịch thành công!", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            log("[ERR] Lỗi kết nối: " + ex.getMessage());
        }
    }

    // --- HELPERS ---
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_MAIN);
        return lbl;
    }

    private JPanel createModernPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(BG_PANEL);
        p.setBorder(new CompoundBorder(
            new TitledBorder(new LineBorder(BORDER_COLOR, 1, true), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), ACCENT_COLOR),
            new EmptyBorder(5, 10, 10, 10)
        ));
        return p;
    }

    private JTextField createInputField(String text) {
        JTextField f = new JTextField(text);
        f.setBackground(INPUT_BG);           // Nền tối cho Textfield
        f.setForeground(TEXT_MAIN);          // Chữ sáng
        f.setCaretColor(ACCENT_COLOR);       // Con trỏ nhấp nháy màu tím
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR), new EmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBackground(bgColor); // Màu nền mặc định là xám, sẽ đổi khi hover
        b.setForeground(Color.GRAY);
        b.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR), new EmptyBorder(8, 15, 8, 15)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hiệu ứng hover nhạt màu đi một chút
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { 
                b.setBackground(bgColor.brighter()); 
            }
            public void mouseExited(java.awt.event.MouseEvent e) { 
                b.setBackground(bgColor); 
            }
        });
        return b;
    }

    private void sendBlock(DataOutputStream out, byte[] d) throws IOException {
        out.writeInt(d.length);
        out.write(d);
        out.flush();
    }

    private byte[] readBlock(DataInputStream in) throws IOException {
        int len = in.readInt();
        byte[] d = new byte[len];
        in.readFully(d);
        return d;
    }

    private void log(String m) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(" " + m + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TranslationClient().setVisible(true));
    }
}