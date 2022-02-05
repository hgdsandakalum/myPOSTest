
import java.awt.Color;
import java.awt.HeadlessException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.WindowConstants;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author Scarlet
 */
public class MainFrame extends javax.swing.JFrame {
    private Connection conn;
    private String currentDate;
    private int currentInvoiceID = 0;
    private int totalInvoicePrice = 0;
    static final String DB_URL = "jdbc:mysql://localhost:3306/test_db?";
    static final String USER = "user";
    static final String PASS = "1234";
     /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
    }
    
    public void init() {
        setSize(800, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        dbConnection();
        
        currentDate = LocalDate.now().toString();
        dateInvoice.setText(currentDate);
        getCurrentInvoiceID();
        invoiceID.setText(String.valueOf(currentInvoiceID));
        getProductData();
        changePrintButtonStatus();
    }
    
    public void dbConnection() {
        try{           
            Class.forName("com.mysql.cj.jdbc.Driver");  // MySQL database connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);                           
        }
        catch(Exception e){
            e.printStackTrace();
        }       
    }
    
    public void clearProducts() {
        productID.setText("");
        productIName.setText("");
        productIPrice.setText("");
        productQty.setText("");
    }
    
    public void getCurrentInvoiceID() {
        try 
        {
            String sql = "SELECT * FROM invoice_hed ORDER BY Invoice_Hed_id DESC LIMIT 1";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery(sql);
            while(rs.next()) //retrieving values from database and storing it!
            {
                currentInvoiceID = rs.getInt("Invoice_Hed_id") + 1;
            }
        }
        catch (HeadlessException | SQLException e){
           productValidate.showMessageDialog(this, e.getMessage());
        }

    }
    
    public void getProductData() {
            try 
            {
                String newItemID = itemID.getText();
                int newQty = Integer.parseInt(qty.getText());
                
                String sql = "SELECT * FROM products WHERE Products_id='"+newItemID+"'";
                PreparedStatement pst = conn.prepareStatement(sql);
                ResultSet rs = pst.executeQuery(sql);
                if (rs.next()) {
                    description.setText(rs.getString("Products_Name"));
                    int priceByQty = rs.getInt("Products_price") * newQty;
                    price.setText(String.valueOf(priceByQty));
                } else {
                    description.setText("No Item Found.");
                }
            }
            catch (HeadlessException | SQLException e){
               productValidate.showMessageDialog(this, e.getMessage());
            }
    }
    
    public void populateReportTable(String date1, String date2) {
            try 
            {   
                String sql = "SELECT * FROM invoice_hed WHERE Invoice_Hed_Date BETWEEN '"+date1+"' AND '"+date2+"' ";
                PreparedStatement pst = conn.prepareStatement(sql);
                ResultSet rs = pst.executeQuery(sql);
                
                DefaultTableModel model = (DefaultTableModel) itemTable1.getModel();
                SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
                int i = 0;
                
                while (rs.next()) {
                    int invoice_Hed_id = Integer.parseInt(rs.getString("Invoice_Hed_id"));
                    Date invoice_Hed_Date = rs.getDate("Invoice_Hed_Date");
                    double invoice_Hed_Amount = Double.parseDouble(rs.getString("Invoice_Hed_Amount"));
                    String invoice_Hed_Customer = rs.getString("Invoice_Hed_Customer");
                    model.addRow(new Object[] {invoice_Hed_id, invoice_Hed_Date, invoice_Hed_Amount, invoice_Hed_Customer});
                    i++;
                }
                
            }
            catch (HeadlessException | SQLException e){
               productValidate.showMessageDialog(this, e.getMessage());
            }
    }
    
    public void clearReportTable() {
        DefaultTableModel model = (DefaultTableModel) itemTable1.getModel();
        model.setRowCount(0);
    }
    
    public void changePrintButtonStatus() {
        if (!printBtn.isEnabled())  {
            printBtn.setEnabled(true);
        } else {
            printBtn.setEnabled(false);
        }
    }
    
    public void populateInvoiceTable() {
            try 
            {
                String sql = "SELECT * FROM invoice_details WHERE Invoice_Hed_Invoice_Hed_id='"+currentInvoiceID+"'";
                PreparedStatement pst = conn.prepareStatement(sql);
                ResultSet rs = pst.executeQuery(sql);
                
                DefaultTableModel model = (DefaultTableModel) itemTable.getModel();
                model.addColumn("Item");
                model.addColumn("Description");
                model.addColumn("Qty");
                model.addColumn("Price");
                int i = 0;
                
                while (rs.next()) {
                    String invoice_Details_qty = rs.getString("Invoice_Details_qty");
                    String invoice_Details_amount = rs.getString("Invoice_Details_amount");
                    totalInvoicePrice = totalInvoicePrice + Integer.parseInt(invoice_Details_amount);
                    String invoice_Hed_Invoice_Hed_id = rs.getString("Invoice_Hed_Invoice_Hed_id");
                    String products_Products_id = rs.getString("Products_Products_id");
//                    model.insertRow(itemTable.getRowCount(), new Object[] {products_Products_id, "test", invoice_Details_qty, invoice_Details_amount});
                    model.addRow(new Object[] {products_Products_id, "test", invoice_Details_qty, invoice_Details_amount});
                    i++;
                }
                totalPrice.setText("Rs. " + totalInvoicePrice);
                
            }
            catch (HeadlessException | SQLException e){
               productValidate.showMessageDialog(this, e.getMessage());
            }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        productValidate = new javax.swing.JOptionPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        customer = new javax.swing.JTextField();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        dateInvoice = new javax.swing.JLabel();
        invoiceID = new javax.swing.JLabel();
        javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
        qty = new javax.swing.JTextField();
        javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
        description = new javax.swing.JLabel();
        itemID = new javax.swing.JTextField();
        javax.swing.JLabel jLabel10 = new javax.swing.JLabel();
        price = new javax.swing.JLabel();
        javax.swing.JLabel jLabel12 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        itemTable = new javax.swing.JTable();
        totalPrice = new javax.swing.JLabel();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        processBtn = new javax.swing.JButton();
        clearBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel9 = new javax.swing.JLabel();
        productID = new javax.swing.JTextField();
        productIName = new javax.swing.JTextField();
        javax.swing.JLabel jLabel11 = new javax.swing.JLabel();
        productIPrice = new javax.swing.JTextField();
        javax.swing.JLabel jLabel13 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel14 = new javax.swing.JLabel();
        productQty = new javax.swing.JTextField();
        saveBtn = new javax.swing.JButton();
        clearBtn2 = new javax.swing.JButton();
        updateBtn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jDatePicker1 = new org.jdatepicker.JDatePicker();
        jDatePicker2 = new org.jdatepicker.JDatePicker();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        searchBtn = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        itemTable1 = new javax.swing.JTable();
        clearBtn3 = new javax.swing.JButton();
        printBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(800, 600));

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(800, 600));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel3.setHorizontalAlignment(JLabel.CENTER);
        jLabel3.setLabelFor(jPanel1);
        jLabel3.setText("Invoice");
        jLabel3.setAlignmentY(0.0F);
        jLabel3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel3.setInheritsPopupMenu(false);

        jLabel1.setText("Date");

        jLabel2.setText("Invoice ID");

        jLabel4.setText("Customer");

        dateInvoice.setText("2014-12-11");
        dateInvoice.setToolTipText("");
        dateInvoice.setBorder(BorderFactory.createLineBorder(Color.black));
        dateInvoice.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        dateInvoice.setName("dateField"); // NOI18N

        invoiceID.setText("10");
        invoiceID.setToolTipText("");
        invoiceID.setBorder(BorderFactory.createLineBorder(Color.black));
        invoiceID.setName("invoiceID"); // NOI18N

        jLabel7.setText("Item ID");

        qty.setText("1");
        qty.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                qtyFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                qtyFocusLost(evt);
            }
        });
        qty.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                qtyPropertyChange(evt);
            }
        });

        jLabel8.setText("Description");

        description.setToolTipText("");
        description.setBorder(BorderFactory.createLineBorder(Color.black));
        description.setName("invoiceID"); // NOI18N

        itemID.setName("itemID"); // NOI18N
        itemID.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                itemIDFocusLost(evt);
            }
        });
        itemID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemIDActionPerformed(evt);
            }
        });
        itemID.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                itemIDPropertyChange(evt);
            }
        });
        itemID.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                itemIDKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                itemIDKeyTyped(evt);
            }
        });

        jLabel10.setText("Qty");

        price.setToolTipText("");
        price.setBorder(BorderFactory.createLineBorder(Color.black));
        price.setName("invoiceID"); // NOI18N

        jLabel12.setText("Price");

        itemTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        itemTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(itemTable);

        totalPrice.setToolTipText("");
        totalPrice.setBorder(BorderFactory.createLineBorder(Color.black));
        totalPrice.setName("invoiceID"); // NOI18N

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("Total");

        processBtn.setText("Process");
        processBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processBtnActionPerformed(evt);
            }
        });

        clearBtn.setText("Clear");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(totalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(processBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(31, 31, 31))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(240, 240, 240)
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(248, 248, 248))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(58, 58, 58)
                        .addComponent(dateInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(invoiceID, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(56, 56, 56)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(customer, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(157, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel7)
                            .addComponent(itemID, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel8)
                            .addComponent(description, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(qty, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(price, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12)))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(qty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(dateInvoice))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(customer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(invoiceID))
                        .addGap(35, 35, 35)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(itemID, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(description, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(price, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel5)
                    .addComponent(totalPrice, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                    .addComponent(processBtn)
                    .addComponent(clearBtn))
                .addGap(139, 139, 139))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {description, itemID, price, qty});

        jTabbedPane1.addTab("Invoice", jPanel1);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel6.setHorizontalAlignment(JLabel.CENTER);
        jLabel6.setLabelFor(jPanel1);
        jLabel6.setText("Product");
        jLabel6.setAlignmentY(0.0F);
        jLabel6.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel6.setInheritsPopupMenu(false);

        jLabel9.setText("Product ID");

        jLabel11.setText("Product Name");

        jLabel13.setText("Price");

        jLabel14.setText("Qty");

        saveBtn.setLabel("Save");
        saveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveBtnActionPerformed(evt);
            }
        });

        clearBtn2.setText("Clear");
        clearBtn2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtn2ActionPerformed(evt);
            }
        });

        updateBtn.setLabel("Update");
        updateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(232, 232, 232)
                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)
                .addGap(268, 268, 268))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(107, 107, 107)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(jLabel13)
                            .addComponent(jLabel11)
                            .addComponent(jLabel9))
                        .addGap(41, 41, 41)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(productQty, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(productIPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(productIName, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(productID, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(93, 93, 93)
                        .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(clearBtn2, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {clearBtn2, saveBtn, updateBtn});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGap(37, 37, 37)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel9)
                    .addComponent(productID, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel11)
                    .addComponent(productIName, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel13)
                    .addComponent(productIPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel14)
                    .addComponent(productQty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(63, 63, 63)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(clearBtn2)
                    .addComponent(updateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(384, 384, 384))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel11, jLabel13, jLabel14, jLabel9, productID, productIName, productIPrice, productQty});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {clearBtn2, saveBtn, updateBtn});

        jTabbedPane1.addTab("Product", jPanel2);

        jDatePicker1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDatePicker1ActionPerformed(evt);
            }
        });

        jDatePicker2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDatePicker2ActionPerformed(evt);
            }
        });

        jLabel15.setText("Date From");

        jLabel16.setText("Date To");

        searchBtn.setText("Search");
        searchBtn.setMaximumSize(new java.awt.Dimension(35, 23));
        searchBtn.setMinimumSize(new java.awt.Dimension(35, 23));
        searchBtn.setPreferredSize(new java.awt.Dimension(35, 23));
        searchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBtnActionPerformed(evt);
            }
        });

        itemTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Invoice No", "Date", "Amount", "Customer"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Double.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        itemTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(itemTable1);
        if (itemTable1.getColumnModel().getColumnCount() > 0) {
            itemTable1.getColumnModel().getColumn(0).setResizable(false);
            itemTable1.getColumnModel().getColumn(1).setResizable(false);
            itemTable1.getColumnModel().getColumn(2).setResizable(false);
            itemTable1.getColumnModel().getColumn(3).setResizable(false);
        }

        clearBtn3.setText("Clear");
        clearBtn3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtn3ActionPerformed(evt);
            }
        });

        printBtn.setText("Print");
        printBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel15)
                    .addComponent(jDatePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(searchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jDatePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addGap(44, 44, 44))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(printBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(42, 42, 42)
                        .addComponent(clearBtn3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2)))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jDatePicker1, jDatePicker2});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jLabel15))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel16)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jDatePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jDatePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(searchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(printBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearBtn3, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(216, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jDatePicker1, jDatePicker2});

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {clearBtn3, printBtn});

        jTabbedPane1.addTab("Reports", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 734, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void itemIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_itemIDActionPerformed

    private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
        // TODO add your handling code here:
        if(productID.getText().length()==0)  // Checking for empty field
            productValidate.showMessageDialog(null, "Empty fields detected ! Please fill up all fields");
        else if(productIName.getText().length()==0)  // Checking for empty field
            productValidate.showMessageDialog(null, "Empty fields detected ! Please fill up all fields");
        else if(productIPrice.getText().length()==0)  // Checking for empty field
            productValidate.showMessageDialog(null, "Empty fields detected ! Please fill up all fields");
        else if(productQty.getText().length()==0)  // Checking for empty field
            productValidate.showMessageDialog(null, "Empty fields detected ! Please fill up all fields");
       
        else{
            try {
                int product_id = 0;
                int product_qty = 0;
                int product_price = 0;
                
                product_id = Integer.parseInt(productID.getText());
                product_qty = Integer.parseInt(productQty.getText());
                product_price = Integer.parseInt(productIPrice.getText());
//                String str="INSERT INTO products "
//                + "(Products_id,Products_Name,Products_price,Products_qty) "
//                + "VALUES(?,?,?,?)";
                String str="INSERT INTO products VALUES (?,?,?,?)";
                PreparedStatement pst = conn.prepareStatement(str);
                pst.setString(1, String.valueOf(product_id));
                pst.setString(2, productIName.getText());
                pst.setString(3, String.valueOf(product_price));
                pst.setString(4, String.valueOf(product_qty));
                pst.execute();
                productValidate.showMessageDialog(null, "Successfully Saved!");
                clearProducts();
            }
            catch (NumberFormatException ex){ 
                productValidate.showMessageDialog(null, "Product ID, Product Quantity and Product Price must be Integers");
            }
            catch (HeadlessException | SQLException e){
                productValidate.showMessageDialog(this, e.getMessage());
            }
        }        
    }//GEN-LAST:event_saveBtnActionPerformed

    private void jDatePicker1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDatePicker1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jDatePicker1ActionPerformed

    private void jDatePicker2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDatePicker2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jDatePicker2ActionPerformed

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchBtnActionPerformed
        // TODO add your handling code here:
        clearReportTable();
        LocalDate date1 = ((GregorianCalendar) jDatePicker1.getModel().getValue()).toZonedDateTime().toLocalDate();
        LocalDate date2 = ((GregorianCalendar) jDatePicker2.getModel().getValue()).toZonedDateTime().toLocalDate();
        
        int dateRangeValidate = date2.compareTo(date1);
        if (dateRangeValidate < 0) {
            productValidate.showMessageDialog(null, "'Date To' must be greater than 'Date From'");
        } else {
            populateReportTable(date1.toString(), date2.toString());
            changePrintButtonStatus();
        }
    }//GEN-LAST:event_searchBtnActionPerformed

    private void clearBtn3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtn3ActionPerformed
        // TODO add your handling code here:
        clearReportTable();
    }//GEN-LAST:event_clearBtn3ActionPerformed

    private void printBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printBtnActionPerformed
        // TODO add your handling code here:
        try {
            String date1 = ((GregorianCalendar) jDatePicker1.getModel().getValue()).toZonedDateTime().toLocalDate().toString();
            String date2 = ((GregorianCalendar) jDatePicker2.getModel().getValue()).toZonedDateTime().toLocalDate().toString();
            
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("dateFrom", date1);
            parameters.put("dateTo", date2);
            
            JasperDesign jdesign = JRXmlLoader.load("C:\\Users\\Scarlet\\Documents\\NetBeansProjects\\myPOSPracticle\\src\\invoiceReport.jrxml");
            
            String sql = "SELECT * FROM invoice_hed WHERE Invoice_Hed_Date BETWEEN '"+date1+"' AND '"+date2+"' ";
            
            JRDesignQuery updateQuery = new JRDesignQuery();
            updateQuery.setText(sql);
            
            jdesign.setQuery(updateQuery);
            JasperReport jreport = JasperCompileManager.compileReport(jdesign);
            JasperPrint jprint = JasperFillManager.fillReport(jreport, parameters, conn);
            
            JasperViewer.viewReport(jprint);
        }   
        catch (JRException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_printBtnActionPerformed

    private void updateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateBtnActionPerformed
        // TODO add your handling code here:
        try {
            int product_id = 0;
            int product_qty = 0;
            int product_price = 0;
            
            product_id = Integer.parseInt(productID.getText());
            product_qty = Integer.parseInt(productQty.getText());
            product_price = Integer.parseInt(productIPrice.getText());
            
            String value1 = String.valueOf(product_id);
            String value2 = productIName.getText();
            String value3 = String.valueOf(product_price);
            String value4 = String.valueOf(product_qty);

            String sql="Update products set Products_id='"+value1+"', Products_Name='"+value2+"', Products_price='"+value3+"', Products_qty='"+value4+"' where Products_id='"+value1+"' ";
            PreparedStatement pst = conn.prepareStatement(sql);
            int updateCount = pst.executeUpdate();
            System.out.println("Update Count = " + updateCount);
            if (updateCount == 0) {
                productValidate.showMessageDialog(null, "Data Update Unsucessfull! Error in entered data");
            } else {
                productValidate.showMessageDialog(null, "Data Updated Successfully!");
                clearProducts();
            }
        }
        catch (NumberFormatException ex){ 
            productValidate.showMessageDialog(null, "Product ID, Product Quantity and Product Price must be Integers");
        }
        catch(SQLException e){
            productValidate.showMessageDialog(null,e);
        }
    }//GEN-LAST:event_updateBtnActionPerformed

    private void clearBtn2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtn2ActionPerformed
        // TODO add your handling code here:
        clearProducts();
    }//GEN-LAST:event_clearBtn2ActionPerformed

    private void itemIDPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_itemIDPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_itemIDPropertyChange

    private void itemIDFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_itemIDFocusLost
        // TODO add your handling code here:
        if (itemID.getText() != null) {
            getProductData();
        }
    }//GEN-LAST:event_itemIDFocusLost

    private void itemIDKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_itemIDKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_itemIDKeyTyped

    private void itemIDKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_itemIDKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_itemIDKeyPressed

    private void qtyFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_qtyFocusGained
        // TODO add your handling code here:
        if (qty.getText() != null) {
            getProductData();
        }
    }//GEN-LAST:event_qtyFocusGained

    private void qtyFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_qtyFocusLost
        // TODO add your handling code here:
        if (qty.getText() != null) {
            getProductData();
        }
    }//GEN-LAST:event_qtyFocusLost

    private void qtyPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_qtyPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_qtyPropertyChange

    private void processBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processBtnActionPerformed
        // TODO add your handling code here:
        if(itemID.getText().length()==0)  // Checking for empty field
            productValidate.showMessageDialog(null, "Empty fields detected ! Please fill up all fields");
        else if(qty.getText().length()==0)  // Checking for empty field
            productValidate.showMessageDialog(null, "Empty fields detected ! Please fill up all fields");
        else{
            try {
                int item_id = 0;
                int item_qty = 0;
                
                item_id = Integer.parseInt(itemID.getText());
                item_qty = Integer.parseInt(qty.getText());
                String str="INSERT INTO invoice_details "
                        + "(Invoice_Details_qty,Invoice_Details_amount,Invoice_Hed_Invoice_Hed_id,Products_Products_id) "
                        + "VALUES (?,?,?,?)";
                PreparedStatement pst = conn.prepareStatement(str);
                pst.setString(1, String.valueOf(item_qty));
                pst.setString(2, price.getText());
                pst.setString(3, invoiceID.getText());
                pst.setString(4, String.valueOf(item_id));
                pst.execute();
                populateInvoiceTable();
//                productValidate.showMessageDialog(null, "Successfully Added!");
            }
            catch (NumberFormatException ex){ 
                productValidate.showMessageDialog(null, "Product ID, Product Quantity and Product Price must be Integers");
            }
            catch (HeadlessException | SQLException e){
                productValidate.showMessageDialog(this, e.getMessage());
            }
        }        
    }//GEN-LAST:event_processBtnActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
        
        
        
        
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearBtn;
    private javax.swing.JButton clearBtn2;
    private javax.swing.JButton clearBtn3;
    private javax.swing.JTextField customer;
    private javax.swing.JLabel dateInvoice;
    private javax.swing.JLabel description;
    private javax.swing.JLabel invoiceID;
    private javax.swing.JTextField itemID;
    private javax.swing.JTable itemTable;
    private javax.swing.JTable itemTable1;
    private org.jdatepicker.JDatePicker jDatePicker1;
    private org.jdatepicker.JDatePicker jDatePicker2;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel price;
    private javax.swing.JButton printBtn;
    private javax.swing.JButton processBtn;
    private javax.swing.JTextField productID;
    private javax.swing.JTextField productIName;
    private javax.swing.JTextField productIPrice;
    private javax.swing.JTextField productQty;
    private javax.swing.JOptionPane productValidate;
    private javax.swing.JTextField qty;
    private javax.swing.JButton saveBtn;
    private javax.swing.JButton searchBtn;
    private javax.swing.JLabel totalPrice;
    private javax.swing.JButton updateBtn;
    // End of variables declaration//GEN-END:variables
}
