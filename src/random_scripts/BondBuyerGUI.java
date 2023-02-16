//package random_scripts;
//
//
//import org.osbot.rs07.script.Script;
//
//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import java.awt.*;
//
//public class BondBuyerGUI extends JFrame {
//    private final JDialog mainDialog;
////    private final JComboBox<Tree> treeSelector;
//    private boolean started;
//    JLabel maxPriceLabel = new JLabel("Bond price:");
//    JTable buyTable = new JTable(100, 2);
//    JTable sellTable = new JTable(100, 2);
//    JButton startButton = new JButton("Start");
//    JButton loadButton = new JButton("Load");
//    JButton saveButton = new JButton("Save");
//
//    public static void main(String[] args) {
//        (new BondBuyerGUI((Script)null)).setVisible(true);
//    }
//
//
//
//    public BondBuyerGUI(Script script) {
//        mainDialog = new JDialog();
//        mainDialog.setTitle("Gandhalf GUI tester");
//        mainDialog.setModal(true);
//        mainDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
//
//        JPanel mainPanel = new JPanel();
//        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
//        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
//        mainDialog.getContentPane().add(mainPanel);
//
//        JTextField maxPriceField = new JTextField(10);
//        mainPanel.add(maxPriceLabel);
//        mainPanel.add(maxPriceField);
//
//        JLabel timeLabel = new JLabel("How long before stopping script:");
//        JTextField timeField = new JTextField(10);
//        mainPanel.add(timeLabel);
//        mainPanel.add(timeField);
//
//        startButton.addActionListener(e -> {
//            started = true;
//
//            close();
//        });
//        mainPanel.add(startButton);
//        saveButton.addActionListener(e -> {
//            String presetName = JOptionPane.showInputDialog("Enter preset name");
//            System.out.println(presetName);
//            close();
//        });
//        mainPanel.add(saveButton);
//
//        mainDialog.pack();
//    }
//
//    public void startScript() {
//        if (this.validateSetings()) {
//            this.settings = this.generateSettings();
//            this.printSettings();
//            this.complete = true;
//            this.dispose();
//        }
//    }
//
//    public void open() {
//        mainDialog.setVisible(true);
//    }
//
//    public void close() {
//        mainDialog.setVisible(false);
//        mainDialog.dispose();
//    }
//}
//
