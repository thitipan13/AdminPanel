AdminPanelUI - คู่มือใช้งาน
=================================================

ภาพรวม (Overview)
- โปรแกรมแอดมินสำหรับจัดการ "ช่วงภาษีแบบขั้นบันได" รายปี
- รองรับจำนวนช่วงภาษีแบบปรับเปลี่ยนได้ (เพิ่ม/ลบแถว) ต่อปี
- บันทึก/โหลดข้อมูลจากไฟล์ CSV ชื่อ "tax_data.csv" ในโฟลเดอร์โปรเจกต์เดียวกัน

ไฟล์ในโปรเจกต์ (Project Files)
- AdminPanelUI.java: โค้ดหลักทั้งหมดของแอป (UI, โมเดลข้อมูล, อ่าน/เขียน CSV)
- tax_data.csv: ไฟล์เก็บข้อมูลภาษี (สร้างอัตโนมัติเมื่อกด Save ถ้าไม่พบ)

Imports ที่ใช้
- javax.swing.*: คอมโพเนนต์ UI ของ Swing เช่น JFrame, JPanel, JButton, JLabel, JOptionPane, JFormattedTextField
- javax.swing.plaf.FontUIResource: ตั้งค่าฟอนต์ทั่วทั้ง UI ผ่าน UIManager
- javax.swing.JTextField: ช่องกรอกข้อความสำหรับปี
- java.awt.*: คลาสพื้นฐานของ AWT สำหรับเลย์เอาต์/สี/ขนาด เช่น BorderLayout, GridBagLayout, GridBagConstraints, Color, Dimension, Insets, Font
- java.awt.event.*: อีเวนต์และลิสเนอร์ เช่น ActionListener, ActionEvent
- java.text.*: NumberFormat สำหรับจัดรูปแบบจำนวนเต็มใน JFormattedTextField
- java.io.*: อ่าน/เขียนไฟล์ CSV เช่น File, FileWriter, BufferedReader, FileReader, IOException
- java.util.*: โครงสร้างข้อมูลและยูทิลิตี้ เช่น Map, HashMap, Set, TreeSet, Collections, Enumeration

โครงสร้างคลาส (Class Design)
- AdminPanelUI (public):
  - หน้าที่: สร้าง UI, แสดง/แก้ไขข้อมูล, จัดการสถานะปีที่เลือก, อ่าน/เขียน CSV
- ส่วน UI หลัก: แถบบน (Logout/Save), แถบซ้าย (รายการปี/เพิ่มปี/ลบปี), พื้นที่กลาง (ตารางปรับจำนวนแถวได้)
  - สถานะสำคัญ: currentSelectedYear, currentRowComponents (กลุ่มคอมโพเนนต์ของแต่ละแถว), allYearsData, isDirty
  - ค่าคงที่: DATA_FILE="tax_data.csv", DEFAULT_YEARS={2568..2562}, DEFAULT_INITIAL_ROWS=8
- YearTaxData (private static):
  - บทบาท: เก็บช่วงภาษีของปีหนึ่งปี เป็น List<TaxBracket> (เพิ่ม/ลบได้)
- TaxBracket (private static):
  - ฟิลด์: minIncome:Long, maxIncome:Long (แถวสุดท้ายเป็น null), taxRate:Integer (เปอร์เซ็นต์)

วิธีใช้งาน
1) รันโปรแกรม (main ใน AdminPanelUI.java)
2) เลือกปีจากแถบซ้าย หรือเพิ่มปีใหม่ (จำกัดช่วง 2500–2600 และห้ามซ้ำ)
3) จัดการปี:
   - ลบปี: ปุ่ม "ลบ" ที่รายการปี พร้อม Dialog ยืนยัน (จะเลือกปีถัดไปให้อัตโนมัติ)
4) กรอกข้อมูลช่วงภาษีในพื้นที่กลาง:
   - แถวท้ายสุดจะไม่มีช่องรายได้สูงสุด (หมายถึงไม่จำกัดบน)
   - ปุ่ม "เพิ่มช่วง" เพื่อเพิ่มแถวใหม่ท้ายตาราง
   - ปุ่ม "ลบช่วง" ที่แต่ละแถวเพื่อลบ (อย่างน้อยต้องเหลือ 1 แถว)
5) กด Save เพื่อบันทึกทุกปีลง CSV (tax_data.csv)
6) เตือน Unsaved changes:
   - เมื่อสลับปี/กด Logout/ปิดหน้าต่าง หากมีการแก้ไขแล้วยังไม่กด Save ระบบจะแจ้งเตือนให้ยืนยัน

รูปแบบไฟล์ CSV
- คอลัมน์: year,rowIndex,minIncome,maxIncome,taxRate
- ไฟล์รองรับจำนวนแถวต่อปีแบบไม่คงที่ โดย rowIndex เริ่มจาก 0 และเรียงตามบนลงล่าง
- ตัวอย่าง:
  year,rowIndex,minIncome,maxIncome,taxRate
  2568,0,0,150000,0
  2568,1,150001,300000,5

หมายเหตุ
- UI จะอ่าน/แสดงผลจากข้อมูลในหน่วยความจำ (allYearsData) โดยตรง และซิงก์ค่าจากจอไปโมเดลทุกครั้งก่อนเพิ่ม/ลบแถว/สลับปี
- ช่องกรอกเป็นจำนวนเต็ม (Integer) และแนะนำให้เรียงจากรายได้น้อยไปมาก (ยังไม่บังคับตรวจสอบความถูกต้องของช่วง)
- ไฟล์ CSV อ้างอิง path ปัจจุบันของการรันโปรแกรม


รายละเอียดโค้ด
=================================================

คลาสและโมเดลข้อมูล (Classes & Models)
- AdminPanelUI (public): ตัวหลักของโปรแกรม รับผิดชอบ UI ทั้งหมด, จัดการปีที่เลือก, ซิงก์ข้อมูลระหว่างหน้าจอกับหน่วยความจำ, อ่าน/เขียนไฟล์ CSV
- YearTaxData (private static): เก็บรายการช่วงภาษีของแต่ละปีเป็น List<TaxBracket>
- TaxBracket (private static): หนึ่งแถวของช่วงภาษี ประกอบด้วย `minIncome:Long`, `maxIncome:Long|null` (แถวสุดท้ายเป็น null), `taxRate:Integer`

ค่าคงที่สำคัญ (Constants)
- `DATA_FILE`: ชื่อไฟล์ CSV (tax_data.csv)
- `DEFAULT_YEARS`: รายการปีเริ่มต้นที่แสดงใน Sidebar
- `DEFAULT_INITIAL_ROWS`: จำนวนแถวว่างเริ่มต้นเมื่อสร้างปีใหม่
- สี UI: `UI_COLOR_TOPBAR`, `UI_COLOR_BUTTON_LIGHT`, `UI_COLOR_SIDEBAR_BG`, `UI_COLOR_SIDEBAR_BORDER`, `UI_COLOR_ADD_YEAR_BG`, `UI_COLOR_CENTER_BG`

ลำดับการทำงานหลัก (Lifecycle)
1) สร้างอินสแตนซ์ `AdminPanelUI()`
   - `setupGlobalFont()` → ตั้งค่าฟอนต์ Tahoma ให้ทุกคอมโพเนนต์
   - `loadAllDataFromFile()` → โหลด CSV (ถ้ามี) เข้าสู่ `allYearsData`
   - `createMainWindow()` → วางเลย์เอาต์หลัก: แถบบน/แถบปี/ตารางข้อมูล
   - `loadDataForYear(currentSelectedYear)` → แสดงข้อมูลปีปัจจุบัน
2) ผู้ใช้แก้ไขข้อมูล/เพิ่มลบแถว/สลับปี → ระบบติดธง `isDirty` ถ้ามีการแก้ไข
3) กด Save →
   - `commitAllFormattedEdits()` → คอมมิตค่าจากช่องตัวเลข
   - `saveCurrentDataToMemory()` → อ่านค่าจาก UI ลงโมเดลในหน่วยความจำ
   - `writeAllDataToCsv()` → เขียน CSV ตามรูปแบบ

ส่วน UI และเมธอดสำคัญ (UI Sections & Key Methods)
- แถบบน (Top Bar)
  - `createTopBar()`:
    - ปุ่ม `Logout`: ถ้ามีการแก้ไขยังไม่บันทึกจะถามยืนยันผ่าน `confirmProceedIfDirty(...)`
    - ปุ่ม `Save`: เรียก `saveCurrentDataAndWriteToFile()`
  - `confirmProceedIfDirty(String message)`: ถ้า `isDirty` เป็น true จะแสดงยืนยันก่อนทำงานต่อ (สลับปี/ออก/ปิด)

- แถบซ้าย (Sidebar: รายการปี)
  - `createYearSidebar()`: สร้างส่วนรายการปีและส่วนเพิ่มปี
  - `createAddYearPanel()`: แสดง `yearInputField` และปุ่ม “เพิ่ม”
  - `addNewYear()`: ตรวจค่าที่กรอก, เช็กช่วงปี 2500–2600, เช็กปีซ้ำ, เพิ่มปีใหม่ลง `allYearsData`, เพิ่มปุ่มปีใน Sidebar และสลับไปปีนั้น
- `createYearButton(int year)`: สร้างแถวปีพร้อมปุ่ม “ลบ”
  - `switchToYear(int year)`: ถ้ามี unsaved changes จะถามก่อน จากนั้นสลับปีและ `refreshUI()`
  - `refreshUI()`: สร้างคอนเทนต์ใหม่ของหน้าต่าง (TopBar/Sidebar/Center) แล้ว revalidate/repaint

- พื้นที่กลาง (Center: ตารางข้อมูล)
  - `createDataInputArea()`: หัวข้อปี + ตารางข้อมูล (`createDataInputTable()`)
  - `createDataInputTable()`: วาดหัวคอลัมน์, วนลูปสร้างแต่ละแถวตาม `YearTaxData.brackets` ของปีปัจจุบัน
    - แต่ละแถวประกอบด้วย: `minIncomeField`, (ไม่ใช่แถวสุดท้าย → `maxIncomeField`), `taxRateField`, ปุ่ม “ลบช่วง”
    - ปุ่ม “เพิ่มช่วง” ที่ท้ายตาราง เพื่อเพิ่มแถวใหม่ท้ายสุด
  - `addBracketRow()`: เซฟค่าจาก UI ลงโมเดลก่อน แล้วเพิ่มแถวว่างใหม่ท้ายรายการ → `refreshUI()`
  - `removeBracketRow(int index)`: เซฟค่าจาก UI ลงโมเดลก่อน แล้วลบแถวตาม index (คงอย่างน้อย 1 แถว) → `refreshUI()`

ซิงก์ข้อมูลหน้าจอ ↔ โมเดล (Sync)
- `saveCurrentDataToMemory()`: อ่านค่าจาก `currentRowComponents` ไปยัง `allYearsData[currentSelectedYear]`
  - ปรับจำนวนแถวในโมเดลให้ตรงกับ UI
  - ใช้ `getValueFromField(...)` สำหรับอ่าน Long/Integer (รองรับค่าว่าง)
  - แถวสุดท้ายบังคับ `maxIncome = null` เพื่อสื่อว่าช่วงสุดท้ายไม่จำกัดบน
- `commitAllFormattedEdits()`, `commitFieldEdit(...)`: คอมมิตค่าจาก `JFormattedTextField` เพื่อให้ได้ค่าล่าสุดก่อนบันทึก
- `getValueFromField(JFormattedTextField, Long)`: แปลงค่าช่องกรอกเป็น Long หรือคืนค่า default เมื่อว่าง/รูปแบบไม่ถูกต้อง

อ่าน/เขียนไฟล์ (Persistence)
- `saveCurrentDataAndWriteToFile()`: ลำดับการเซฟทั้งหมด + แจ้งผลสำเร็จ/ผิดพลาด
- `writeAllDataToCsv()`: สร้างสตริง CSV ตามหัวคอลัมน์ `year,rowIndex,minIncome,maxIncome,taxRate` แล้วเขียนลงไฟล์ `tax_data.csv`
- `loadAllDataFromFile()`: ตรวจไฟล์ ถ้ามีให้เปิดอ่าน → `parseCsvContent(...)`
- `parseCsvContent(BufferedReader)`: อ่านทีละบรรทัด, แปลงค่าเป็นประเภทข้อมูล, เติมลง `allYearsData` โดยขยายลิสต์แถวตาม `rowIndex`

เมธอดผู้ช่วย (Helpers)
- `showInfo/showWarn/showError`: แสดง Dialog แบบข้อมูล/เตือน/ผิดพลาด
- `isValidYear(int)`: ตรวจว่าปีอยู่ในช่วง 2500–2600
- `isDuplicateYear(int)`: ตรวจว่าปีนั้นถูกสร้างไว้แล้วใน `allYearsData`
- `getOrCreateYearData(int)`: ดึงข้อมูลปี ถ้าไม่มีให้สร้างใหม่และเก็บก่อนส่งกลับ

การตรวจสอบ/พฤติกรรมสำคัญ (Validation & Behavior)
- ช่วงปีเพิ่ม: จำกัด 2500–2600 และห้ามซ้ำกับปีที่มีอยู่
- แถวสุดท้ายของช่วงภาษี: `maxIncome` เป็น `null` เพื่อบอกว่าไม่มีขีดจำกัดบน
- Unsaved changes (`isDirty`): เปลี่ยนเป็น true เมื่อแก้ไขค่าใดๆ; มี Dialog เตือนเมื่อสลับปี/ออก/ปิด ถ้ายังไม่ Save

จุดต่อยอด
- สามารถเพิ่มการตรวจสอบความถูกต้องของช่วงภาษี (ไม่ทับซ้อน/เรียงจากน้อยไปมาก) ก่อนบันทึก
- สามารถย้ายการเก็บไฟล์ไปที่โฟลเดอร์เฉพาะ และรองรับเลือกพาธไฟล์
- แยกคลาส UI เป็นไฟล์ย่อยตามส่วน (TopBar/Sidebar/Center) หากโค้ดเติบโตมากขึ้น


โค้ดตัวอย่าง
=================================================

1) Auto-commas in number fields
- ใช้ `JFormattedTextField(NumberFormat.getIntegerInstance())` เพื่อให้แสดงคอมมากลุ่มหลักพันและรับเฉพาะตัวเลข

private static JFormattedTextField createIntegerField() {
    JFormattedTextField field = new JFormattedTextField(NumberFormat.getIntegerInstance());
    field.setHorizontalAlignment(SwingConstants.RIGHT);
    return field;
}

// การใช้งานภายในตารางข้อมูล (ตัวอย่าง)
RowComponents rc = new RowComponents();
rc.minIncomeField = createIntegerField();
if (bracket.minIncome != null) rc.minIncomeField.setValue(bracket.minIncome);

rc.maxIncomeField = createIntegerField();
if (bracket.maxIncome != null) rc.maxIncomeField.setValue(bracket.maxIncome);

rc.taxRateField = createIntegerField();
if (bracket.taxRate != null) rc.taxRateField.setValue(bracket.taxRate);

2) Load CSV on startup
- เรียกโหลดในคอนสตรัคเตอร์ และแปลง CSV เป็นข้อมูลในหน่วยความจำ จากนั้นจึงวาด UI และแสดงปีปัจจุบัน

public AdminPanelUI() {
    setupGlobalFont();          // ตั้งค่าฟอนต์
    loadAllDataFromFile();      // โหลดข้อมูลจากไฟล์ ถ้ามี
    createMainWindow();         // สร้างหน้าต่างหลักและเลย์เอาต์
    loadDataForYear(currentSelectedYear); // แสดงข้อมูลปีปัจจุบันในตาราง
}

// โหลดข้อมูลทุกปีจากไฟล์ CSV
private void loadAllDataFromFile() {
    File file = new File(DATA_FILE); // DATA_FILE = "tax_data.csv"
    if (!file.exists()) {
        System.out.println("ไม่พบไฟล์ข้อมูล จะเริ่มต้นด้วยข้อมูลว่าง");
        // สามารถเตรียมปีเริ่มต้น/แถวเริ่มต้นได้ตามค่าดีฟอลต์
        return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        System.out.println("==== เริ่มโหลดข้อมูลจากไฟล์ CSV ====");
        parseCsvContent(reader); // เติมค่าเข้า allYearsData
        System.out.println("==== โหลดข้อมูล CSV สำเร็จ ====");
    } catch (Exception e) {
        e.printStackTrace();
        showError("ไม่สามารถอ่านไฟล์ข้อมูลได้", "ข้อผิดพลาดการโหลดข้อมูล");
    }
}

// แปลง CSV เป็นข้อมูลในหน่วยความจำ
// รูปแบบไฟล์: year,rowIndex,minIncome,maxIncome,taxRate
private void parseCsvContent(BufferedReader reader) throws IOException {
    String line = reader.readLine(); // ข้ามบรรทัดหัวคอลัมน์
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
        while (yearData.brackets.size() <= rowIndex) {
            yearData.brackets.add(new TaxBracket());
        }
        yearData.brackets.get(rowIndex).minIncome = minIncome;
        yearData.brackets.get(rowIndex).maxIncome = maxIncome;
        yearData.brackets.get(rowIndex).taxRate = taxRate;
    }
}
