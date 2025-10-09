import javax.swing.*;
import javax.swing.plaf.FontUIResource;
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
    private static final int BRACKET_ROWS = 8;           // จำนวนแถวในตารางภาษี
    private static final String DATA_FILE = "tax_data.json";  // ชื่อไฟล์เก็บข้อมูล

    // ตัวแปรสำหรับ UI หลัก
    private JFrame mainWindow;                           // หน้าต่างหลัก
    private int currentSelectedYear = 2568;              // ปีที่เลือกอยู่ในปัจจุบัน
    
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
        private int year;                               // ปี
        private TaxBracket[] brackets;                  // ข้อมูลช่วงภาษี

        public YearTaxData(int year) {
            this.year = year;
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
        private long minIncome = 0;                     // รายได้ต่ำสุด
        private Long maxIncome = null;                  // รายได้สูงสุด (null สำหรับช่วงสุดท้าย)
        private int taxRate = 0;                        // อัตราภาษี (เปอร์เซ็นต์)
    }

    /**
     * Constructor - สร้างและแสดงหน้าต่างโปรแกรม
     */
    public AdminPanelUI() {
        setupGlobalFont();                              // ตั้งค่าฟอนต์
        createMainWindow();                             // สร้างหน้าต่างหลัก
        loadAllDataFromFile();                          // โหลดข้อมูลจากไฟล์
        loadDataForYear(currentSelectedYear);           // แสดงข้อมูลปีปัจจุบัน
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
        mainWindow.setLocationRelativeTo(null);         // วางหน้าต่างตรงกลางจอ

        // สร้าง layout หลัก
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // เพิ่มส่วนต่างๆ เข้าไปใน layout
        mainPanel.add(createTopBar(), BorderLayout.NORTH);        // แถบบน
        mainPanel.add(createYearSidebar(), BorderLayout.WEST);    // แถบซ้าย (ปี)
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

        // สร้าง Admin badge
        JLabel adminBadge = new JLabel("Admin");
        adminBadge.setFont(adminBadge.getFont().deriveFont(Font.BOLD, 24f));
        adminBadge.setOpaque(true);
        adminBadge.setBackground(new Color(255, 239, 204));

        // สร้างปุ่ม Save
        JButton saveButton = new JButton("Save");
        saveButton.setFocusable(false);
        saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD, 24f));
        saveButton.setBackground(new Color(255, 239, 204));
        
        // เพิ่ม event เมื่อกดปุ่ม Save
        saveButton.addActionListener(e -> saveCurrentDataAndWriteToFile());

        topBar.add(adminBadge, BorderLayout.WEST);
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
        JPanel yearsContainer = new JPanel();
        yearsContainer.setLayout(new BoxLayout(yearsContainer, BoxLayout.Y_AXIS));
        yearsContainer.setOpaque(false);

        // เพิ่มปีต่างๆ เข้าไป
        int[] availableYears = {2568, 2567, 2566, 2565, 2564, 2563, 2562};
        for (int year : availableYears) {
            yearsContainer.add(createYearButton(year));
        }

        sidebar.add(yearsContainer, BorderLayout.CENTER);
        return sidebar;
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
        yearButton.addActionListener(e -> switchToYear(year));

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
        table.add(new JLabel("ช่วงเงินได้สุทธิ (บาท) (เรียงลำดับจากน้อยไปมาก)"), gbc);
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
            bracket.minIncome = getValueFromField(minIncomeInputs[i], 0L);
            
            // บันทึกรายได้สูงสุด (ถ้าไม่ใช่แถวสุดท้าย)
            if (i < BRACKET_ROWS - 1 && maxIncomeInputs[i] != null) {
                Long maxValue = getValueFromField(maxIncomeInputs[i], null);
                bracket.maxIncome = maxValue;
            } else {
                bracket.maxIncome = null; // แถวสุดท้ายไม่มีขีดจำกัดบน
            }
            
            // บันทึกอัตราภาษี
            bracket.taxRate = getValueFromField(taxRateInputs[i], 0L).intValue();
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
            String valueText = field.getValue().toString().replace(",", "");
            return Long.parseLong(valueText);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * โหลดข้อมูลของปีที่กำหนดมาแสดงในช่องกรอก
     */
    private void loadDataForYear(int year) {
        YearTaxData yearData = allYearsData.get(year);
        
        // ถ้าไม่มีข้อมูลของปีนี้ ให้สร้างใหม่
        if (yearData == null) {
            yearData = new YearTaxData(year);
            allYearsData.put(year, yearData);
        }
        
        // เติมข้อมูลลงในช่องกรอก
        for (int i = 0; i < BRACKET_ROWS; i++) {
            TaxBracket bracket = yearData.brackets[i];
            
            // เติมรายได้ต่ำสุด
            if (minIncomeInputs[i] != null) {
                minIncomeInputs[i].setValue(bracket.minIncome);
            }
            
            // เติมรายได้สูงสุด (ถ้าไม่ใช่แถวสุดท้าย)
            if (i < BRACKET_ROWS - 1 && maxIncomeInputs[i] != null) {
                maxIncomeInputs[i].setValue(bracket.maxIncome);
            }
            
            // เติมอัตราภาษี
            if (taxRateInputs[i] != null) {
                taxRateInputs[i].setValue(bracket.taxRate);
            }
        }
    }

    /**
     * บันทึกข้อมูลปัจจุบันและเขียนลงไฟล์
     */
    private void saveCurrentDataAndWriteToFile() {
        try {
            // บันทึกข้อมูลปัจจุบันไปยังหน่วยความจำก่อน
            saveCurrentDataToMemory();
            
            // สร้าง JSON และเขียนลงไฟล์
            writeAllDataToFile();
            
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
     * เขียนข้อมูลทุกปีลงไฟล์ JSON
     */
    private void writeAllDataToFile() throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"allYearsData\": {\n");
        
        // เขียนข้อมูลแต่ละปี
        int yearCount = 0;
        for (Map.Entry<Integer, YearTaxData> entry : allYearsData.entrySet()) {
            int year = entry.getKey();
            YearTaxData yearData = entry.getValue();
            
            if (yearCount > 0) {
                json.append(",\n");
            }
            
            json.append("    \"").append(year).append("\": {\n");
            json.append("      \"year\": ").append(year).append(",\n");
            json.append("      \"taxBrackets\": [\n");
            
            // เขียนข้อมูลช่วงภาษีของปีนี้
            for (int i = 0; i < BRACKET_ROWS; i++) {
                TaxBracket bracket = yearData.brackets[i];
                
                json.append("        {\n");
                json.append("          \"minIncome\": ").append(bracket.minIncome).append(",\n");
                
                if (bracket.maxIncome != null) {
                    json.append("          \"maxIncome\": ").append(bracket.maxIncome).append(",\n");
                } else {
                    json.append("          \"maxIncome\": null,\n");
                }
                
                json.append("          \"taxRate\": ").append(bracket.taxRate).append("\n");
                json.append("        }");
                
                if (i < BRACKET_ROWS - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("      ]\n");
            json.append("    }");
            
            yearCount++;
        }
        
        json.append("\n  }\n");
        json.append("}\n");
        
        // เขียนลงไฟล์
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            writer.write(json.toString());
        }
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
            // อ่านไฟล์ทั้งหมด
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            // แยกข้อมูลของแต่ละปี (วิธีง่ายๆ โดยการ parse ข้อความ)
            parseJsonContent(content.toString());
            
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการโหลดไฟล์: " + e.getMessage());
        }
    }

    /**
     * แยกข้อมูล JSON
     */
    private void parseJsonContent(String jsonContent) {
        // การหาข้อความ
        String[] lines = jsonContent.split("\n");
        
        Integer currentYear = null;
        int bracketIndex = 0;
        YearTaxData currentYearData = null;
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            // หาปี
            if (trimmed.startsWith("\"") && trimmed.contains("\":")) {
                try {
                    String yearStr = trimmed.substring(1, trimmed.indexOf("\"", 1));
                    currentYear = Integer.parseInt(yearStr);
                    currentYearData = new YearTaxData(currentYear);
                    allYearsData.put(currentYear, currentYearData);
                    bracketIndex = 0;
                } catch (NumberFormatException e) {
                    // ไม่ใช่ปี ข้าม
                }
            }
            // หาข้อมูลในแต่ละ bracket
            else if (currentYearData != null && bracketIndex < BRACKET_ROWS) {
                if (trimmed.startsWith("\"minIncome\"")) {
                    currentYearData.brackets[bracketIndex].minIncome = extractNumber(trimmed);
                } else if (trimmed.startsWith("\"maxIncome\"")) {
                    if (trimmed.contains("null")) {
                        currentYearData.brackets[bracketIndex].maxIncome = null;
                    } else {
                        currentYearData.brackets[bracketIndex].maxIncome = extractNumber(trimmed);
                    }
                } else if (trimmed.startsWith("\"taxRate\"")) {
                    currentYearData.brackets[bracketIndex].taxRate = (int) extractNumber(trimmed);
                    bracketIndex++; // 1 bracket
                }
            }
        }
    }

    /**
     * ดึงตัวเลขจากบรรทัด JSON
     */
    private long extractNumber(String jsonLine) {
        try {
            int colonIndex = jsonLine.indexOf(':');
            if (colonIndex == -1) return 0;
            
            String valueStr = jsonLine.substring(colonIndex + 1).trim();
            if (valueStr.endsWith(",")) {
                valueStr = valueStr.substring(0, valueStr.length() - 1).trim();
            }
            
            return Long.parseLong(valueStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static void main(String[] args) {
		
        new AdminPanelUI();
    }
}