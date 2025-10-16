AdminPanelUI - คู่มือใช้งาน
=================================================

ภาพรวม (Overview)
- โปรแกรมแอดมินสำหรับจัดการ "ช่วงภาษีแบบขั้นบันได" รายปี
- แต่ละปีมี 8 แถว (BRACKET_ROWS = 8); แถวสุดท้ายไม่มีค่ารายได้สูงสุด
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
  - ส่วน UI หลัก: แถบบน (Logout/Save), แถบซ้าย (รายการปี/เพิ่มปี), พื้นที่กลาง (ตาราง 8 แถว)
  - สถานะสำคัญ: currentSelectedYear, ชุดช่องกรอก (min/max/taxRate), allYearsData
  - ค่าคงที่: BRACKET_ROWS=8, DATA_FILE="tax_data.csv", DEFAULT_YEARS={2568..2562}
- YearTaxData (private static):
  - บทบาท: เก็บช่วงภาษีของปีหนึ่งปี เป็นอาเรย์ TaxBracket[8]
- TaxBracket (private static):
  - ฟิลด์: minIncome:Long, maxIncome:Long (แถวสุดท้ายเป็น null), taxRate:Integer (เปอร์เซ็นต์)

วิธีใช้งาน
1) รันโปรแกรม (main ใน AdminPanelUI.java)
2) เลือกปีจากแถบซ้าย หรือเพิ่มปีใหม่ (จำกัดช่วง 2500–2600 และห้ามซ้ำ)
3) กรอกข้อมูลแต่ละแถว:
   - ทุกแถว: minIncome, taxRate (จำนวนเต็ม)
   - แถว 1–7: maxIncome (จำนวนเต็ม); แถว 8 ปล่อยว่าง (ไม่จำกัดบน)
4) กด Save เพื่อบันทึกทุกปีลง CSV (tax_data.csv)
5) สลับปีได้ทุกเมื่อ ข้อมูลในหน้าจอจะถูกเก็บไว้ในหน่วยความจำทันที; ต้องกด Save เพื่อเขียนลงไฟล์

รูปแบบไฟล์ CSV
- คอลัมน์: year,rowIndex,minIncome,maxIncome,taxRate
- ตัวอย่าง:
  year,rowIndex,minIncome,maxIncome,taxRate
  2568,0,0,150000,0
  2568,1,150001,300000,5

หมายเหตุ
- มี 8 แถวคงที่เสมอ
- แถวสุดท้าย maxIncome เป็นค่าว่าง (null) เพื่อหมายถึงไม่จำกัดบน
- ช่องกรอกเป็นจำนวนเต็ม (Integer) และแนะนำให้เรียงจากรายได้น้อยไปมาก (โปรแกรมไม่บังคับตรวจสอบความถูกต้องของช่วง)
- ไฟล์ CSV อ้างอิง path ปัจจุบันของการรันโปรแกรม