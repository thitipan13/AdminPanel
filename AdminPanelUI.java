import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;


public class AdminPanelUI {

	// จำนวนแถวช่วงเงินได้สุทธิที่จะแสดงในฟอร์มกลาง
	private static final int BRACKET_ROWS = 8;

	// องค์ประกอบ UI หลัก
	private JFrame frame;                 // หน้าต่างหลัก
	private int selectedYear = 2568;      // ปีที่เลือกอยู่ปัจจุบัน
	private JTextField[] minIncomeFields = new JTextField[BRACKET_ROWS]; // คอลัมน์ช่วงเงินได้ (ต่ำสุด)
	private JTextField[] maxIncomeFields = new JTextField[BRACKET_ROWS]; // คอลัมน์ช่วงเงินได้ (สูงสุด)
	private JTextField[] rateFields = new JTextField[BRACKET_ROWS];      // คอลัมน์อัตราภาษี

	public AdminPanelUI() {
		// ตั้งค่าฟอนต์ระบบให้เป็น Tahoma ทั้งแอป
		setGlobalFont("Tahoma");
		frame = new JFrame("Admin Panel - UI Preview");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1200, 720);
		frame.setLocationRelativeTo(null);

		// โครงหน้าจอหลัก: แถบบน (North) / ปีด้านซ้าย (West) / ฟอร์มกลาง (Center)
		JPanel root = new JPanel(new BorderLayout());
		root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		root.add(buildTopBar(), BorderLayout.NORTH);
		root.add(buildLeftSidebar(), BorderLayout.WEST);
		root.add(buildCenterPanel(), BorderLayout.CENTER);

		frame.setContentPane(root);
		frame.setVisible(true);
	}

	// สร้างแถบบนของหน้าจอ (Badge Admin และปุ่ม Save)
	private JPanel buildTopBar() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(new Color(126, 171, 194));
		panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

		JLabel adminBadge = new JLabel("Admin");
		adminBadge.setFont(adminBadge.getFont().deriveFont(Font.BOLD, 24f));
		adminBadge.setOpaque(true);
		adminBadge.setBackground(new Color(255, 239, 204));

		JButton save = new JButton("Save");
		save.setFocusable(false);
		save.setFont(save.getFont().deriveFont(Font.BOLD, 24f));
		save.setBackground(new Color(255, 239, 204));

		panel.add(adminBadge, BorderLayout.WEST);
		panel.add(save, BorderLayout.EAST);
		return panel;
	}

	// สร้างแถบด้านซ้าย แสดงปีที่สามารถคลิกเลือกได้
	private JPanel buildLeftSidebar() {
		JPanel sidebar = new JPanel(new BorderLayout());
		sidebar.setPreferredSize(new Dimension(260, 0));
		sidebar.setBackground(new Color(232, 245, 252));
		sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(180, 210, 225)));

		JPanel yearsPanel = new JPanel();
		yearsPanel.setLayout(new BoxLayout(yearsPanel, BoxLayout.Y_AXIS));
		yearsPanel.setOpaque(false);

		int[] years = {2568, 2567, 2566, 2565, 2564, 2563, 2562};
		for (int y : years) yearsPanel.add(createYearRow(y)); // loop สร้างแถว(ปี) จาก Array

		sidebar.add(yearsPanel, BorderLayout.CENTER);
		return sidebar;
	}

	// สร้าง 1 แถวสำหรับ 1 ปี
	private JComponent createYearRow(int year) {
		JPanel row = new JPanel(new BorderLayout());
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
		row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 210, 225)));
		row.setBackground(new Color(224, 238, 246));

		JButton yearBtn = new JButton(String.valueOf(year));
		yearBtn.setHorizontalAlignment(SwingConstants.LEFT);
		yearBtn.setContentAreaFilled(false);
		yearBtn.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 10));
		yearBtn.setFont(yearBtn.getFont().deriveFont(Font.BOLD, 18f));
		yearBtn.setActionCommand(String.valueOf(year));
		yearBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent ev) { onSelectYear(year); }
		});

		row.add(yearBtn, BorderLayout.CENTER);
		return row;
	}

	// ส่วนกลาง: หัวข้อใหญ่และฟอร์มช่วงเงินได้ + อัตราภาษี
	private JPanel buildCenterPanel() {
		JPanel center = new JPanel(new BorderLayout());
		center.setBackground(new Color(245, 247, 249));
		center.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

		JPanel titleWrap = new JPanel(new GridBagLayout());
		titleWrap.setOpaque(false);
		JLabel title = new JLabel("ช่วงภาษีแบบขั้นบันได ปี " + selectedYear);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
		titleWrap.add(title);

		center.add(titleWrap, BorderLayout.NORTH);
		center.add(buildBracketGrid(), BorderLayout.CENTER);
		return center;
	}

	// ตาราง 3 คอลัมน์หลัก: min — max | rate จำนวน BRACKET_ROWS แถว
	private JPanel buildBracketGrid() {
		JPanel grid = new JPanel(new GridBagLayout());
		grid.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
		grid.add(new JLabel("ช่วงเงินได้สุทธิ"), gbc);
		gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.5;
		grid.add(new JLabel(""), gbc);
		gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 0.0;
		grid.add(new JLabel("อัตราภาษี"), gbc);

		for (int i = 0; i < BRACKET_ROWS; i++) {
			gbc.weightx = 0.5;
			gbc.gridx = 0; gbc.gridy = i + 1;
			minIncomeFields[i] = new JTextField();
			grid.add(minIncomeFields[i], gbc);

			if (i != BRACKET_ROWS - 1) {
				gbc.gridx = 1; gbc.gridy = i + 1; gbc.weightx = 0;
				grid.add(new JLabel("—", SwingConstants.CENTER), gbc);

				gbc.gridx = 2; gbc.gridy = i + 1; gbc.weightx = 0.5;
				maxIncomeFields[i] = new JTextField();
				grid.add(maxIncomeFields[i], gbc);
			}

			gbc.gridx = 4; gbc.gridy = i + 1; gbc.weightx = 0.25;
			rateFields[i] = new JTextField();
			grid.add(rateFields[i], gbc);
		}

		return grid;
	}

	// เมื่อคลิกเลือกปีทางซ้าย จะรีเฟรชหัวข้อและฟอร์มให้เป็นปีที่เลือก
	private void onSelectYear(int year) {
		selectedYear = year;
		frame.getContentPane().removeAll();
		JPanel root = new JPanel(new BorderLayout());
		root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		root.add(buildTopBar(), BorderLayout.NORTH);
		root.add(buildLeftSidebar(), BorderLayout.WEST);
		root.add(buildCenterPanel(), BorderLayout.CENTER);
		frame.setContentPane(root);
		frame.revalidate();
		frame.repaint();
	}

	// ตั้งค่าฟอนต์เริ่มต้นของ Swing ทุกคอมโพเนนต์
	private static void setGlobalFont(String fontName) {
		java.util.Enumeration<?> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				FontUIResource f = (FontUIResource) value;
				UIManager.put(key, new FontUIResource(new Font(fontName, f.getStyle(), f.getSize())));
			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(AdminPanelUI::new);
	}
}