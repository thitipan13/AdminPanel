import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.JTextField;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.io.*;
import java.util.*;


/**
 * AdminPanelUI - หน้าต่างแอดมินสำหรับจัดการข้อมูลภาษีแบบขั้นบันได
 */
public class AdminPanelUI {

    // ค่าคงที่สำหรับการตั้งค่า
    private static final String DATA_FILE = "tax_data.csv"; // ชื่อไฟล์เก็บข้อมูล (CSV)
    private static final int[] DEFAULT_YEARS = {2568, 2567, 2566, 2565, 2564, 2563, 2562};
    private static final int DEFAULT_INITIAL_ROWS = 8; // จำนวนแถวเริ่มต้นเมื่อสร้างปีใหม่

    // โทนสี UI ส่วนต่างๆ ของแอป
    private static final Color UI_COLOR_TOPBAR = new Color(126, 171, 194);
    private static final Color UI_COLOR_BUTTON_LIGHT = new Color(255, 239, 204);
    private static final Color UI_COLOR_SIDEBAR_BG = new Color(232, 245, 252);
    private static final Color UI_COLOR_SIDEBAR_BORDER = new Color(180, 210, 225);
    private static final Color UI_COLOR_ADD_YEAR_BG = new Color(224, 238, 246);
    private static final Color UI_COLOR_CENTER_BG = new Color(245, 247, 249);

    // ตัวแปรสำหรับ UI หลัก
    private JFrame mainWindow; // หน้าต่างหลัก
    private int currentSelectedYear = 2568; // ปีที่เลือกอยู่ในปัจจุบัน
    private JTextField yearInputField; // ช่องกรอกปีที่ต้องการเพิ่ม
    private JPanel yearsContainer; // ตัวแปรสำหรับเก็บรายการปีที่มีอยู่
    
    // ตัวแปรสำหรับช่องกรอกข้อมูลในตาราง (แบบปรับเปลี่ยนจำนวนแถวได้)
    // แต่ละแถวของ UI จะถูกเก็บเป็นกลุ่มคอมโพเนนต์
    // เพื่อให้อ่าน/เขียนค่าจากหน้าจอไปยังโมเดลได้ง่าย และรองรับการเพิ่ม/ลบแถว
    private static class RowComponents {
        JFormattedTextField minIncomeField;
        JFormattedTextField maxIncomeField; // แถวสุดท้ายจะไม่แสดง
        JFormattedTextField taxRateField;
        JButton removeButton;
    }
    private java.util.List<RowComponents> currentRowComponents = new ArrayList<>();

    // ตัวแปรสำหรับเก็บข้อมูลทุกปี (เก็บในหน่วยความจำ)
    // คีย์เป็นปี พ.ศ. และค่าเป็นชุดช่วงภาษีของปีนั้นๆ
    private Map<Integer, YearTaxData> allYearsData = new HashMap<>();
    private boolean isDirty = false; // มีการแก้ไขที่ยังไม่บันทึกหรือไม่ (ใช้เตือนก่อนปิด/สลับปี/ออกระบบ)


    /**
     * คลาสสำหรับเก็บข้อมูลภาษีของแต่ละปี
     */
    private static class YearTaxData {
        private java.util.List<TaxBracket> brackets; // ข้อมูลช่วงภาษี (เพิ่ม/ลบแถวได้)

        public YearTaxData(int year) {
            this.brackets = new ArrayList<>();
            // สร้างช่วงภาษีเริ่มต้น (ค่าว่าง)
            for (int i = 0; i < DEFAULT_INITIAL_ROWS; i++) {
                this.brackets.add(new TaxBracket());
            }
        }
    }

    /**
     * คลาสสำหรับเก็บข้อมูลช่วงภาษีแต่ละแถว
     */
    private static class TaxBracket {
        private Long minIncome = null; // รายได้ต่ำสุด
        private Long maxIncome = null; // รายได้สูงสุด (null สำหรับช่วงสุดท้าย)
        private Integer taxRate = null; // อัตราภาษี (เปอร์เซ็นต์)
    }

    /**
     * Constructor - สร้างและแสดงหน้าต่างโปรแกรม
     */
    public AdminPanelUI() {
        setupGlobalFont(); // ตั้งค่าฟอนต์
        loadAllDataFromFile(); // โหลดข้อมูลจากไฟล์
        createMainWindow(); // สร้างหน้าต่างหลัก
        loadDataForYear(currentSelectedYear); // แสดงข้อมูลปีปัจจุบัน
    }

    /**
     * ตั้งค่าฟอนต์เริ่มต้นเป็น Tahoma สำหรับทั้งโปรแกรม
     */
    private void setupGlobalFont() {
        java.util.Enumeration<?> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                FontUIResource currentFont = (FontUIResource) value;
                FontUIResource newFont = new FontUIResource(
                    new Font("Tahoma", currentFont.getStyle(), currentFont.getSize())
                );
                UIManager.put(key, newFont);
            }
        }
    }

    /**
     * สร้างหน้าต่างหลักและจัดการ layout
     */
    private void createMainWindow() {
        mainWindow = new JFrame("Admin Panel - จัดการข้อมูลภาษี");
        mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // ป้องกันปิดทับทันที ใช้ถามก่อนถ้ามีการแก้ไข
        mainWindow.setSize(1200, 720);
        mainWindow.setLocationRelativeTo(null); // วางหน้าต่างตรงกลางจอ
        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmProceedIfDirty("มีการแก้ไขที่ยังไม่บันทึก ต้องการปิดโปรแกรมหรือไม่?")) {
                    mainWindow.dispose();
                }
            }
        });

        // สร้าง layout หลัก
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // เพิ่มส่วนต่างๆ เข้าไปใน layout
        mainPanel.add(createTopBar(), BorderLayout.NORTH); // แถบบน
        mainPanel.add(createYearSidebar(), BorderLayout.WEST); // แถบซ้าย (ปี)
        mainPanel.add(createDataInputArea(), BorderLayout.CENTER); // พื้นที่กลาง (ฟอร์ม)

        mainWindow.setContentPane(mainPanel);
        mainWindow.setVisible(true);
    }

    /**
     * สร้างแถบบนที่มี badge Admin และปุ่ม Save
     */
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UI_COLOR_TOPBAR);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // สร้างปุ่ม Logout (มีการเตือนเมื่อมีการแก้ไขที่ยังไม่บันทึก)
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFocusable(false);
        logoutButton.setFont(logoutButton.getFont().deriveFont(Font.BOLD, 24f));
        logoutButton.setBackground(UI_COLOR_BUTTON_LIGHT);
        logoutButton.addActionListener(e -> {
            System.out.println("Logout clicked: " + e.getActionCommand());
            if (confirmProceedIfDirty("มีการแก้ไขที่ยังไม่บันทึก ต้องการออกจากระบบหรือไม่?")) {
                if (mainWindow != null) {
                    mainWindow.dispose();
                }
                //new Login();
            }
        });

        // สร้างปุ่ม Save
        JButton saveButton = new JButton("Save");
        saveButton.setFocusable(false);
        saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD, 24f));
        saveButton.setBackground(UI_COLOR_BUTTON_LIGHT);
        
        // เพิ่ม event เมื่อกดปุ่ม Save (บันทึกค่าจากหน้าจอ -> หน่วยความจำ -> CSV แล้วรีเซ็ตสถานะ isDirty)
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                saveCurrentDataAndWriteToFile();
            }
        });

        topBar.add(logoutButton, BorderLayout.WEST);
        topBar.add(saveButton, BorderLayout.EAST);
        return topBar;
    }

    /**
     * สร้างแถบด้านซ้ายที่แสดงรายการปี
     */
    private JPanel createYearSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(UI_COLOR_SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UI_COLOR_SIDEBAR_BORDER));

        // สร้างพื้นที่สำหรับรายการปี
        yearsContainer = new JPanel();
        yearsContainer.setLayout(new BoxLayout(yearsContainer, BoxLayout.Y_AXIS));
        yearsContainer.setOpaque(false);

        // เพิ่มปีต่างๆ เข้าไป (เรียงจากมากไปน้อย) โดยรวมปีเริ่มต้นกับปีที่เคยเพิ่มไว้
        Set<Integer> yearsToShow = new TreeSet<>(Collections.reverseOrder());
        for (int y : DEFAULT_YEARS) {
            yearsToShow.add(y);
        }
        yearsToShow.addAll(allYearsData.keySet());

        // แสดงปุ่มปีเรียงจากมากไปน้อย
        for (int year : yearsToShow) {
            yearsContainer.add(createYearButton(year));
        }

        sidebar.add(yearsContainer, BorderLayout.CENTER);
        sidebar.add(createAddYearPanel(), BorderLayout.SOUTH); // เพิ่มปีที่ด้านล่างซ้าย
        return sidebar;
    }

    /**
     * สร้าง panel เพิ่มปี
     */
    private JPanel createAddYearPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UI_COLOR_ADD_YEAR_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UI_COLOR_SIDEBAR_BORDER),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // สร้าง label
        JLabel label = new JLabel("เพิ่มปี:");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));

        // สร้างช่องกรอกปี
        yearInputField = new JTextField(8);
        yearInputField.setFont(yearInputField.getFont().deriveFont(14f));
        yearInputField.setText(""); // ค่าเริ่มต้นเป็นค่าว่าง

        // สร้างปุ่มเพิ่ม
        JButton addButton = new JButton("เพิ่ม");
        addButton.setFont(addButton.getFont().deriveFont(Font.BOLD, 14f));
        addButton.setBackground(UI_COLOR_TOPBAR);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);

        // เพิ่ม action listener สำหรับปุ่มเพิ่ม
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                addNewYear();
            }
        });
        
        // เพิ่ม action listener สำหรับกด Enter ในช่องกรอก
        yearInputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                addNewYear();
            }
        });
        
        // จัด layout
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        inputPanel.setOpaque(false);
        inputPanel.add(label);
        inputPanel.add(yearInputField);
        inputPanel.add(addButton);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * เพิ่มปีใหม่
     */
    private void addNewYear() {
        String inputText = yearInputField.getText().trim();
        
        // ตรวจสอบว่ากรอกข้อมูลหรือไม่
        if (inputText.isEmpty()) {
            showWarn("กรุณากรอกปีที่ต้องการเพิ่ม", "ข้อมูลไม่ครบ");
            return;
        }
        
        try {
            int newYear = Integer.parseInt(inputText);
            
            // ตรวจสอบว่าปีนี้มีอยู่แล้วหรือไม่
            if (isDuplicateYear(newYear)) {
                showWarn("ปี " + newYear + " มีอยู่ในระบบแล้ว", "ปีซ้ำ");
                return;
            }
            
            // ตรวจสอบว่าปีอยู่ในช่วงที่เหมาะสม (เช่น 2500-2600)
            if (!isValidYear(newYear)) {
                showWarn("กรุณากรอกปีในช่วง 2500-2600", "ปีไม่ถูกต้อง");
                return;
            }
            
            // เพิ่มปีใหม่
            allYearsData.put(newYear, new YearTaxData(newYear));
            
            // เพิ่มปุ่มปีใหม่ใน sidebar
            yearsContainer.add(createYearButton(newYear), 0); // เพิ่มที่ตำแหน่งแรก
            yearsContainer.revalidate();
            yearsContainer.repaint();
            
            // เคลียร์ช่องกรอก
            yearInputField.setText("");
            
            // แสดงข้อความสำเร็จ
            showInfo("เพิ่มปี " + newYear + " สำเร็จ", "สำเร็จ");
            
            // เปลี่ยนไปแสดงปีที่เพิ่งเพิ่ม
            switchToYear(newYear);
            
        } catch (NumberFormatException ex) {
            showError("กรุณากรอกเฉพาะตัวเลขเท่านั้น", "รูปแบบไม่ถูกต้อง");
        }
    }

    /**
     * สร้างปุ่มสำหรับแต่ละปี
     */
    private JComponent createYearButton(int year) {
        JPanel yearRow = new JPanel(new BorderLayout());
        yearRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        yearRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UI_COLOR_SIDEBAR_BORDER));
        yearRow.setBackground(UI_COLOR_ADD_YEAR_BG);

        JButton yearButton = new JButton(String.valueOf(year));
        yearButton.setHorizontalAlignment(SwingConstants.LEFT);
        yearButton.setContentAreaFilled(false);
        yearButton.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 10));
        yearButton.setFont(yearButton.getFont().deriveFont(Font.BOLD, 18f));
        
        // เพิ่ม event เมื่อคลิกเลือกปี (มีการเตือนถ้าแก้ไขค้าง)
        yearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                switchToYear(year);
            }
        });

        yearRow.add(yearButton, BorderLayout.CENTER);

        // ปุ่มจัดการปี: เปลี่ยนชื่อ/ลบ
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 10));
        actionsPanel.setOpaque(false);
        JButton renameButton = new JButton("เปลี่ยนชื่อ");
        renameButton.setMargin(new Insets(4, 8, 4, 8));
        renameButton.setFocusable(false);
        JButton deleteButton = new JButton("ลบ");
        deleteButton.setMargin(new Insets(4, 8, 4, 8));
        deleteButton.setFocusable(false);

        renameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog(mainWindow, "แก้ไขปี:", String.valueOf(year));
                if (input == null) return; // ยกเลิก
                input = input.trim();
                if (input.isEmpty()) return;
                try {
                    int newYear = Integer.parseInt(input);
                    if (!isValidYear(newYear)) {
                        showWarn("กรุณากรอกปีในช่วง 2500-2600", "ปีไม่ถูกต้อง");
                        return;
                    }
                    if (newYear == year) return;
                    if (isDuplicateYear(newYear)) {
                        showWarn("ปี " + newYear + " มีอยู่แล้ว", "ปีซ้ำ");
                        return;
                    }
                    YearTaxData data = allYearsData.remove(year);
                    allYearsData.put(newYear, data);
                    if (currentSelectedYear == year) {
                        currentSelectedYear = newYear;
                    }
                    refreshUI();
                } catch (NumberFormatException ex) {
                    showError("กรุณากรอกตัวเลขปีที่ถูกต้อง", "รูปแบบไม่ถูกต้อง");
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(mainWindow, "ยืนยันการลบปี " + year + "?", "ยืนยันการลบ", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
                allYearsData.remove(year);
                if (allYearsData.isEmpty()) {
                    int fallback = DEFAULT_YEARS.length > 0 ? DEFAULT_YEARS[0] : 2568;
                    allYearsData.put(fallback, new YearTaxData(fallback));
                    currentSelectedYear = fallback;
                } else if (currentSelectedYear == year) {
                    int nextYear = allYearsData.keySet().stream().max(Integer::compareTo).orElse(currentSelectedYear);
                    currentSelectedYear = nextYear;
                }
                refreshUI();
            }
        });

        actionsPanel.add(renameButton);
        actionsPanel.add(deleteButton);
        yearRow.add(actionsPanel, BorderLayout.EAST);
        return yearRow;
    }

    /**
     * สร้างพื้นที่กลางสำหรับแสดงและกรอกข้อมูล
     */
    private JPanel createDataInputArea() {
        JPanel centerArea = new JPanel(new BorderLayout());
        centerArea.setBackground(UI_COLOR_CENTER_BG);
        centerArea.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // สร้างหัวข้อ
        JPanel titleArea = new JPanel(new GridBagLayout());
        titleArea.setOpaque(false);
        JLabel titleLabel = new JLabel("ช่วงภาษีแบบขั้นบันได ปี " + currentSelectedYear);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 28f));
        titleArea.add(titleLabel);

        centerArea.add(titleArea, BorderLayout.NORTH);
        centerArea.add(createDataInputTable(), BorderLayout.CENTER);
        return centerArea;
    }

    /**
     * สร้างตารางสำหรับกรอกข้อมูล
     */
    private JPanel createDataInputTable() {
        currentRowComponents.clear();
        JPanel table = new JPanel(new GridBagLayout());
        table.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // สร้างหัวตาราง
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        table.add(new JLabel("ช่วงเงินได้สุทธิ (บาท) - (เรียงลำดับจากน้อยไปมาก)"), gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.5;
        table.add(new JLabel(""), gbc);
        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 0.0;
        table.add(new JLabel("อัตราภาษี (%)"), gbc);
        gbc.gridx = 5; gbc.gridy = 0; gbc.weightx = 0.0;
        table.add(new JLabel(""), gbc); // ปุ่มลบ

        YearTaxData yearData = allYearsData.get(currentSelectedYear);
        if (yearData == null) {
            yearData = getOrCreateYearData(currentSelectedYear);
        }

        int row = 1;
        for (int i = 0; i < yearData.brackets.size(); i++) {
            TaxBracket bracket = yearData.brackets.get(i);

            RowComponents rc = new RowComponents();

            // minIncome
            gbc.weightx = 0.5;
            gbc.gridx = 0; gbc.gridy = row;
            rc.minIncomeField = new JFormattedTextField(NumberFormat.getIntegerInstance());
            if (bracket.minIncome != null) rc.minIncomeField.setValue(bracket.minIncome);
            attachDirtyListener(rc.minIncomeField);
            table.add(rc.minIncomeField, gbc);

            boolean isLast = (i == yearData.brackets.size() - 1);

            if (!isLast) {
                gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 0;
                table.add(new JLabel("—", SwingConstants.CENTER), gbc);

                gbc.gridx = 2; gbc.gridy = row; gbc.weightx = 0.5;
                rc.maxIncomeField = new JFormattedTextField(NumberFormat.getIntegerInstance());
                if (bracket.maxIncome != null) rc.maxIncomeField.setValue(bracket.maxIncome);
                attachDirtyListener(rc.maxIncomeField);
                table.add(rc.maxIncomeField, gbc);
            } else {
                rc.maxIncomeField = null; // แถวสุดท้ายไม่มี max
            }

            gbc.gridx = 4; gbc.gridy = row; gbc.weightx = 0.25;
            rc.taxRateField = new JFormattedTextField(NumberFormat.getIntegerInstance());
            if (bracket.taxRate != null) rc.taxRateField.setValue(bracket.taxRate);
            attachDirtyListener(rc.taxRateField);
            table.add(rc.taxRateField, gbc);

            gbc.gridx = 5; gbc.gridy = row; gbc.weightx = 0.0;
            rc.removeButton = new JButton("ลบช่วง"); // ลบแถวนี้ออกจากปีปัจจุบัน
            rc.removeButton.setEnabled(yearData.brackets.size() > 1);
            final int removeIndex = i;
            rc.removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AdminPanelUI.this.removeBracketRow(removeIndex);
                }
            });
            table.add(rc.removeButton, gbc);

            currentRowComponents.add(rc);
            row++;
        }

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("เพิ่มช่วง"); // เพิ่มแถวใหม่ที่ท้ายตาราง
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AdminPanelUI.this.addBracketRow();
            }
        });
        addPanel.add(addButton);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 6; gbc.weightx = 1.0;
        table.add(addPanel, gbc);

        return table;
    }

    // การจัดการแถวช่วงภาษีแบบปรับเปลี่ยนจำนวนแถวได้
    private void addBracketRow() {
        // เก็บค่าปัจจุบันจาก UI ลงหน่วยความจำก่อน แล้วค่อยเพิ่มแถวใหม่
        saveCurrentDataToMemory();
        YearTaxData data = allYearsData.get(currentSelectedYear);
        if (data == null) {
            data = getOrCreateYearData(currentSelectedYear);
        }
        data.brackets.add(new TaxBracket());
        isDirty = true;
        refreshUI();
    }

    private void removeBracketRow(int index) {
        // เก็บค่าปัจจุบันจาก UI ลงหน่วยความจำก่อน แล้วค่อยลบแถวตาม index
        saveCurrentDataToMemory();
        YearTaxData data = allYearsData.get(currentSelectedYear);
        if (data != null && data.brackets.size() > 1 && index >= 0 && index < data.brackets.size()) {
            data.brackets.remove(index);
            isDirty = true;
            refreshUI();
        }
    }

    /**
     * เปลี่ยนไปยังปีที่เลือก
     */
    private void switchToYear(int year) {
        // เตือนถ้าแก้ไขโดยยังไม่เซฟ
        if (!confirmProceedIfDirty("มีการแก้ไขที่ยังไม่บันทึก ต้องการสลับปีหรือไม่?")) {
            return;
        }
        // บันทึกสถานะปัจจุบัน
        saveCurrentDataToMemory();
        
        // เปลี่ยนปี
        currentSelectedYear = year;
        
        // สร้างหน้าจอใหม่
        refreshUI();
    }

    /**
     * รีเฟรช UI ทั้งหมด
     */
    private void refreshUI() {
        mainWindow.getContentPane().removeAll();
        
        JPanel newMainPanel = new JPanel(new BorderLayout());
        newMainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        newMainPanel.add(createTopBar(), BorderLayout.NORTH);
        newMainPanel.add(createYearSidebar(), BorderLayout.WEST);
        newMainPanel.add(createDataInputArea(), BorderLayout.CENTER);
        
        mainWindow.setContentPane(newMainPanel);
        mainWindow.revalidate();
        mainWindow.repaint();
    }

    /**
     * บันทึกข้อมูลจากช่องกรอกไปยังหน่วยความจำ
     */
    private void saveCurrentDataToMemory() {
        // สร้างข้อมูลปีใหม่ถ้ายังไม่มี
        if (!allYearsData.containsKey(currentSelectedYear)) {
            allYearsData.put(currentSelectedYear, new YearTaxData(currentSelectedYear));
        }

        YearTaxData yearData = getOrCreateYearData(currentSelectedYear);

        // ปรับจำนวนแถวข้อมูลให้ตรงกับ UI ปัจจุบัน
        int uiRows = currentRowComponents.size();
        while (yearData.brackets.size() < uiRows) {
            yearData.brackets.add(new TaxBracket());
        }
        while (yearData.brackets.size() > uiRows) {
            yearData.brackets.remove(yearData.brackets.size() - 1);
        }

        for (int i = 0; i < uiRows; i++) {
            RowComponents rc = currentRowComponents.get(i);
            TaxBracket bracket = yearData.brackets.get(i);

            bracket.minIncome = getValueFromField(rc.minIncomeField, null);
            if (i < uiRows - 1 && rc.maxIncomeField != null) {
                bracket.maxIncome = getValueFromField(rc.maxIncomeField, null);
            } else {
                bracket.maxIncome = null; // แถวสุดท้ายไม่มีขีดจำกัดบน
            }
            Long taxValue = getValueFromField(rc.taxRateField, null);
            bracket.taxRate = (taxValue != null) ? taxValue.intValue() : null;
        }
    }

    /**
     * ดึงค่าจากช่องกรอก แล้วแปลงเป็น Long
     */
    private Long getValueFromField(JFormattedTextField field, Long defaultValue) {
        if (field == null || field.getValue() == null) {
            return defaultValue;
        }
        
        try {
            String valueText = field.getValue().toString().replace(",", "").trim();
            if (valueText.isEmpty()) {
                return defaultValue;
            }
            return Long.parseLong(valueText);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * โหลดข้อมูลของปีที่กำหนดมาแสดงในช่องกรอก
     */
    private void loadDataForYear(int year) {
        // UI จะอ่านค่าจาก allYearsData โดยตรงใน createDataInputTable()
        System.out.println("Loading data for year: " + year);
        getOrCreateYearData(year);
        
        // รีเฟรช UI
        if (mainWindow != null) {
            mainWindow.revalidate();
            mainWindow.repaint();
        }
    }

    /**
     * บันทึกข้อมูลปัจจุบันและเขียนลงไฟล์
     */
    private void saveCurrentDataAndWriteToFile() {
        try {
            // บังคับให้คอมมิตค่าจาก JFormattedTextField ทุกตัวก่อนอ่านค่า
            commitAllFormattedEdits();
            // บันทึกข้อมูลปัจจุบันไปยังหน่วยความจำก่อน
            saveCurrentDataToMemory();
            
            // เขียนลงไฟล์ CSV
            writeAllDataToCsv();
            
            // แสดงข้อความสำเร็จ
            JOptionPane.showMessageDialog(
                mainWindow, 
                "บันทึกข้อมูลสำเร็จ!\nไฟล์: " + DATA_FILE, 
                "บันทึกสำเร็จ", 
                JOptionPane.INFORMATION_MESSAGE
            );
            isDirty = false;
            
        } catch (Exception e) {
            // แสดงข้อความผิดพลาด
            JOptionPane.showMessageDialog(
                mainWindow, 
                "เกิดข้อผิดพลาด: " + e.getMessage(), 
                "ข้อผิดพลาด", 
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    // คอมมิตค่าในช่องกรอกตัวเลขให้เป็นค่าจริง ก่อนบันทึก
    private void commitAllFormattedEdits() {
        for (RowComponents rc : currentRowComponents) {
            commitFieldEdit(rc.minIncomeField);
            commitFieldEdit(rc.maxIncomeField);
            commitFieldEdit(rc.taxRateField);
        }
    }

    private void commitFieldEdit(JFormattedTextField field) {
        if (field == null) return;
        try {
            field.commitEdit();
        } catch (ParseException ignored) {
            // หากแปลงไม่ได้ ให้คงค่าเดิมไว้
        }
    }

    // ติดตามการแก้ไขเพื่อเตือน Unsaved changes
    private void attachDirtyListener(JFormattedTextField field) {
        if (field == null) return;
        field.addPropertyChangeListener("value", new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent ignored) {
                isDirty = true;
            }
        });
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { isDirty = true; }
        });
    }

    private boolean confirmProceedIfDirty(String message) {
        if (!isDirty) return true;
        int option = JOptionPane.showConfirmDialog(
            mainWindow,
            message,
            "เตือนการเปลี่ยนแปลง",
            JOptionPane.YES_NO_OPTION
        );
        return option == JOptionPane.YES_OPTION;
    }

    // ===== Helper methods =====
    private void showInfo(String message, String title) {
        JOptionPane.showMessageDialog(mainWindow, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarn(String message, String title) {
        JOptionPane.showMessageDialog(mainWindow, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(mainWindow, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private boolean isValidYear(int year) {
        return year >= 2500 && year <= 2600;
    }

    private boolean isDuplicateYear(int year) {
        return allYearsData.containsKey(year);
    }

    /**
     * ดึงข้อมูลปีจาก MAP allYearsData ถ้าไม่มีให้สร้างใหม่และใส่เข้าไปก่อนส่งกลับ
     * @param year ปี พ.ศ. ที่ต้องการข้อมูล
     * @return YearTaxData ของปีนั้น (ไม่มีทางเป็น null)
     */
    private YearTaxData getOrCreateYearData(int year) {
        YearTaxData data = allYearsData.get(year);
        if (data == null) {
            data = new YearTaxData(year);
            allYearsData.put(year, data);
        }
        return data;
    }

    /**
     * เขียนข้อมูลทุกปีลงไฟล์ CSV
     */
    private void writeAllDataToCsv() throws IOException {
        StringBuilder csv = new StringBuilder();
        // header
        csv.append("year,rowIndex,minIncome,maxIncome,taxRate\n");
        java.util.List<Integer> years = new ArrayList<>(allYearsData.keySet());
        years.sort(Collections.reverseOrder());
        for (int year : years) {
            YearTaxData yearData = allYearsData.get(year);
            for (int i = 0; i < yearData.brackets.size(); i++) {
                TaxBracket bracket = yearData.brackets.get(i);
                csv.append(year).append(",")
                   .append(i).append(",")
                   .append(bracket.minIncome == null ? "" : bracket.minIncome.toString()).append(",")
                   .append(bracket.maxIncome == null ? "" : bracket.maxIncome.toString()).append(",")
                   .append(bracket.taxRate == null ? "" : bracket.taxRate.toString())
                   .append("\n");
            }
        }
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            writer.write(csv.toString());
        }
        System.out.println("Data saved to CSV successfully: " + DATA_FILE);
    }

    /**
     * โหลดข้อมูลทุกปีจากไฟล์
     */
    private void loadAllDataFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("ไม่พบไฟล์ข้อมูล จะเริ่มต้นด้วยข้อมูลว่าง");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // อ่าน CSV ทั้งหมด
            System.out.println("==== เริ่มโหลดข้อมูลจากไฟล์ CSV ====");
            parseCsvContent(reader);
            System.out.println("==== โหลดข้อมูล CSV สำเร็จ: " + allYearsData.size() + " ปี ====");
            
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการโหลดไฟล์: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * แปลง CSV เป็นข้อมูลในหน่วยความจำ
     * รูปแบบ: year,rowIndex,minIncome,maxIncome,taxRate
     */
    private void parseCsvContent(BufferedReader reader) throws IOException {
        String line = reader.readLine(); // header
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(",", -1);
            if (parts.length < 5) continue;
            int year = Integer.parseInt(parts[0].trim());
            int rowIndex = Integer.parseInt(parts[1].trim());
            Long minIncome = parts[2].trim().isEmpty() ? null : Long.parseLong(parts[2].trim());
            Long maxIncome = parts[3].trim().isEmpty() ? null : Long.parseLong(parts[3].trim());
            Integer taxRate = parts[4].trim().isEmpty() ? null : Integer.parseInt(parts[4].trim());

            YearTaxData yearData = allYearsData.get(year);
            if (yearData == null) {
                yearData = new YearTaxData(year);
                allYearsData.put(year, yearData);
            }
            // ขยายลิสต์ให้มีขนาดรองรับ rowIndex ตามข้อมูล CSV
            while (yearData.brackets.size() <= rowIndex) {
                yearData.brackets.add(new TaxBracket());
            }
            yearData.brackets.get(rowIndex).minIncome = minIncome;
            yearData.brackets.get(rowIndex).maxIncome = maxIncome;
            yearData.brackets.get(rowIndex).taxRate = taxRate;
        }
    }


    public static void main(String[] args) {
		
        new AdminPanelUI();
    }

}
