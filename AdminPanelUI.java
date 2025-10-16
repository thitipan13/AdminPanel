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
    private static final int BRACKET_ROWS = 8; // จำนวนแถวในตารางภาษี
    private static final String DATA_FILE = "tax_data.csv"; // ชื่อไฟล์เก็บข้อมูล (CSV)
    private static final int[] DEFAULT_YEARS = {2568, 2567, 2566, 2565, 2564, 2563, 2562};

    // ตัวแปรสำหรับ UI หลัก
    private JFrame mainWindow; // หน้าต่างหลัก
    private int currentSelectedYear = 2568; // ปีที่เลือกอยู่ในปัจจุบัน
    private JTextField yearInputField; // ช่องกรอกปีที่ต้องการเพิ่ม
    private JPanel yearsContainer; // ตัวแปรสำหรับเก็บรายการปีที่มีอยู่
    
    // ตัวแปรสำหรับช่องกรอกข้อมูลในตาราง
    private JFormattedTextField[] minIncomeInputs = new JFormattedTextField[BRACKET_ROWS];
    private JFormattedTextField[] maxIncomeInputs = new JFormattedTextField[BRACKET_ROWS];
    private JFormattedTextField[] taxRateInputs = new JFormattedTextField[BRACKET_ROWS];

    // ตัวแปรสำหรับเก็บข้อมูลทุกปี (เก็บในหน่วยความจำ)
    private Map<Integer, YearTaxData> allYearsData = new HashMap<>();


    /**
     * คลาสสำหรับเก็บข้อมูลภาษีของแต่ละปี
     */
    private static class YearTaxData {
        private TaxBracket[] brackets; // ข้อมูลช่วงภาษี

        public YearTaxData(int year) {
            this.brackets = new TaxBracket[BRACKET_ROWS];
            // สร้างช่วงภาษีเริ่มต้น (ค่าว่าง)
            for (int i = 0; i < BRACKET_ROWS; i++) {
                this.brackets[i] = new TaxBracket();
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
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setSize(1200, 720);
        mainWindow.setLocationRelativeTo(null); // วางหน้าต่างตรงกลางจอ

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
        topBar.setBackground(new Color(126, 171, 194));
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // สร้างปุ่ม Logout
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFocusable(false);
        logoutButton.setFont(logoutButton.getFont().deriveFont(Font.BOLD, 24f));
        logoutButton.setBackground(new Color(255, 239, 204));
        logoutButton.addActionListener(e -> {
            System.out.println("Logout clicked: " + e.getActionCommand());
            if (mainWindow != null) {
                mainWindow.dispose();
            }
            //new Login();
        });

        // สร้างปุ่ม Save
        JButton saveButton = new JButton("Save");
        saveButton.setFocusable(false);
        saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD, 24f));
        saveButton.setBackground(new Color(255, 239, 204));
        
        // เพิ่ม event เมื่อกดปุ่ม Save
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
        sidebar.setBackground(new Color(232, 245, 252));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(180, 210, 225)));

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
        panel.setBackground(new Color(224, 238, 246));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 210, 225)),
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
        addButton.setBackground(new Color(126, 171, 194));
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
            JOptionPane.showMessageDialog(
                mainWindow,
                "กรุณากรอกปีที่ต้องการเพิ่ม",
                "ข้อมูลไม่ครบ",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        try {
            int newYear = Integer.parseInt(inputText);
            
            // ตรวจสอบว่าปีนี้มีอยู่แล้วหรือไม่
            if (allYearsData.containsKey(newYear)) {
                JOptionPane.showMessageDialog(
                    mainWindow,
                    "ปี " + newYear + " มีอยู่ในระบบแล้ว",
                    "ปีซ้ำ",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            
            // ตรวจสอบว่าปีอยู่ในช่วงที่เหมาะสม (เช่น 2500-2600)
            if (newYear < 2500 || newYear > 2600) {
                JOptionPane.showMessageDialog(
                    mainWindow,
                    "กรุณากรอกปีในช่วง 2500-2600",
                    "ปีไม่ถูกต้อง",
                    JOptionPane.WARNING_MESSAGE
                );
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
            JOptionPane.showMessageDialog(
                mainWindow,
                "เพิ่มปี " + newYear + " สำเร็จ",
                "สำเร็จ",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // เปลี่ยนไปแสดงปีที่เพิ่งเพิ่ม
            switchToYear(newYear);
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                mainWindow,
                "กรุณากรอกเฉพาะตัวเลขเท่านั้น",
                "รูปแบบไม่ถูกต้อง",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * สร้างปุ่มสำหรับแต่ละปี
     */
    private JComponent createYearButton(int year) {
        JPanel yearRow = new JPanel(new BorderLayout());
        yearRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        yearRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 210, 225)));
        yearRow.setBackground(new Color(224, 238, 246));

        JButton yearButton = new JButton(String.valueOf(year));
        yearButton.setHorizontalAlignment(SwingConstants.LEFT);
        yearButton.setContentAreaFilled(false);
        yearButton.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 10));
        yearButton.setFont(yearButton.getFont().deriveFont(Font.BOLD, 18f));
        
        // เพิ่ม event เมื่อคลิกเลือกปี
        yearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                switchToYear(year);
            }
        });

        yearRow.add(yearButton, BorderLayout.CENTER);
        return yearRow;
    }

    /**
     * สร้างพื้นที่กลางสำหรับแสดงและกรอกข้อมูล
     */
    private JPanel createDataInputArea() {
        JPanel centerArea = new JPanel(new BorderLayout());
        centerArea.setBackground(new Color(245, 247, 249));
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

        // สร้างแถวข้อมูล
        for (int rowIndex = 0; rowIndex < BRACKET_ROWS; rowIndex++) {
            createTableRow(table, gbc, rowIndex);
        }

        return table;
    }

    /**
     * สร้างแถวเดียวในตาราง
     */
    private void createTableRow(JPanel table, GridBagConstraints gbc, int rowIndex) {
        // คอลัมน์ที่ 1: รายได้ต่ำสุด
        gbc.weightx = 0.5;
        gbc.gridx = 0; gbc.gridy = rowIndex + 1;
        minIncomeInputs[rowIndex] = new JFormattedTextField(NumberFormat.getIntegerInstance());
        table.add(minIncomeInputs[rowIndex], gbc);

        // สำหรับแถวที่ไม่ใช่แถวสุดท้าย จะมีช่อง "ถึง" และรายได้สูงสุด
        if (rowIndex != BRACKET_ROWS - 1) {
            // เครื่องหมาย "—"
            gbc.gridx = 1; gbc.gridy = rowIndex + 1; gbc.weightx = 0;
            table.add(new JLabel("—", SwingConstants.CENTER), gbc);

            // คอลัมน์ที่ 2: รายได้สูงสุด
            gbc.gridx = 2; gbc.gridy = rowIndex + 1; gbc.weightx = 0.5;
            maxIncomeInputs[rowIndex] = new JFormattedTextField(NumberFormat.getIntegerInstance());
            table.add(maxIncomeInputs[rowIndex], gbc);
        }

        // คอลัมน์ที่ 3: อัตราภาษี
        gbc.gridx = 4; gbc.gridy = rowIndex + 1; gbc.weightx = 0.25;
        taxRateInputs[rowIndex] = new JFormattedTextField(NumberFormat.getIntegerInstance());
        table.add(taxRateInputs[rowIndex], gbc);
    }

    /**
     * เปลี่ยนไปยังปีที่เลือก
     */
    private void switchToYear(int year) {
        // บันทึกข้อมูลปีปัจจุบันก่อนเปลี่ยน
        saveCurrentDataToMemory();
        
        // เปลี่ยนปี
        currentSelectedYear = year;
        
        // สร้างหน้าจอใหม่
        refreshUI();
        
        // โหลดข้อมูลปีใหม่
        loadDataForYear(currentSelectedYear);
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
        
        YearTaxData yearData = allYearsData.get(currentSelectedYear);
        
        // บันทึกข้อมูลจากช่องกรอกแต่ละแถว
        for (int i = 0; i < BRACKET_ROWS; i++) {
            TaxBracket bracket = yearData.brackets[i];
            
            // บันทึกรายได้ต่ำสุด
            bracket.minIncome = getValueFromField(minIncomeInputs[i], null);
            
            // บันทึกรายได้สูงสุด (ถ้าไม่ใช่แถวสุดท้าย)
            if (i < BRACKET_ROWS - 1 && maxIncomeInputs[i] != null) {
                Long maxValue = getValueFromField(maxIncomeInputs[i], null);
                bracket.maxIncome = maxValue;
            } else {
                bracket.maxIncome = null; // แถวสุดท้ายไม่มีขีดจำกัดบน
            }
            
            // บันทึกอัตราภาษี
            Long taxValue = getValueFromField(taxRateInputs[i], null);
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
        System.out.println("Loading data for year: " + year); // debug output
        
        YearTaxData yearData = allYearsData.get(year);
        
        // ถ้าไม่มีข้อมูลของปีนี้ ให้สร้างใหม่
        if (yearData == null) {
            System.out.println("No data found for year " + year + ", creating new data");
            yearData = new YearTaxData(year);
            allYearsData.put(year, yearData);
        } else {
            System.out.println("Found existing data for year " + year);
        }
        
        // เติมข้อมูลลงในช่องกรอก
        for (int i = 0; i < BRACKET_ROWS; i++) {
            TaxBracket bracket = yearData.brackets[i];
            
            // เติมรายได้ต่ำสุด
            if (minIncomeInputs[i] != null) {
                if (bracket.minIncome != null) {
                    minIncomeInputs[i].setValue(bracket.minIncome);
                    System.out.println("Set minIncome[" + i + "] = " + bracket.minIncome);
                } else {
                    minIncomeInputs[i].setValue(null);
                }
            }
            
            // เติมรายได้สูงสุด (ถ้าไม่ใช่แถวสุดท้าย)
            if (i < BRACKET_ROWS - 1 && maxIncomeInputs[i] != null) {
                if (bracket.maxIncome != null) {
                    maxIncomeInputs[i].setValue(bracket.maxIncome);
                    System.out.println("Set maxIncome[" + i + "] = " + bracket.maxIncome);
                } else {
                    maxIncomeInputs[i].setValue(null);
                }
            }
            
            // เติมอัตราภาษี
            if (taxRateInputs[i] != null) {
                if (bracket.taxRate != null) {
                    taxRateInputs[i].setValue(bracket.taxRate);
                    System.out.println("Set taxRate[" + i + "] = " + bracket.taxRate);
                } else {
                    taxRateInputs[i].setValue(null);
                }
            }
        }
        
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

    /**
     * เขียนข้อมูลทุกปีลงไฟล์ CSV
     */
    private void writeAllDataToCsv() throws IOException {
        StringBuilder csv = new StringBuilder();
        // header
        csv.append("year,rowIndex,minIncome,maxIncome,taxRate\n");
        for (Map.Entry<Integer, YearTaxData> entry : allYearsData.entrySet()) {
            int year = entry.getKey();
            YearTaxData yearData = entry.getValue();
            for (int i = 0; i < BRACKET_ROWS; i++) {
                TaxBracket bracket = yearData.brackets[i];
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
            if (rowIndex >= 0 && rowIndex < BRACKET_ROWS) {
                yearData.brackets[rowIndex].minIncome = minIncome;
                yearData.brackets[rowIndex].maxIncome = maxIncome;
                yearData.brackets[rowIndex].taxRate = taxRate;
            }
        }
    }


    public static void main(String[] args) {
		
        new AdminPanelUI();
    }

}
