package db.everything;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class EverythingGUI extends JFrame {
    private FileService fileService = FileService.getInstance();

    private final JMenuBar mnuBar = new JMenuBar();
    private final JMenu menu = new JMenu("File");
    private final JMenuItem mnuIndex = new JMenuItem("Index All Files");

    private final JTextField txtSearch = new JTextField();
    private final JTable tblFiles = new JTable(new Object[0][0],
            new String[]{"Name", "Path", "Size", "Date"});

    private JScrollPane scrollPane = new JScrollPane(tblFiles);

    public static void main(String[] args) {
        EverythingGUI gui = new EverythingGUI();
        gui.setTitle("My Everything");
        gui.setDefaultCloseOperation(EXIT_ON_CLOSE);
        gui.setUpHandlers();
        gui.buildMenuBar();
        gui.makeMainPanel();

        gui.pack();
        gui.setLocationRelativeTo(null);
        gui.setVisible(true);
    }

    private void makeMainPanel() {
        final Panel panel = new Panel();
        panel.setPreferredSize(new Dimension(800, 500));

        LayoutManager layoutManager = new BorderLayout();

        panel.setLayout(layoutManager);

        panel.add(BorderLayout.NORTH, txtSearch);
        panel.add(BorderLayout.CENTER, scrollPane);


        setContentPane(panel);

        tblFiles.setShowGrid(false);
        updateFilesList();
    }

    private void setUpHandlers() {
        mnuIndex.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "Index All Files", "", JOptionPane.INFORMATION_MESSAGE);
                fileService.reindexTable();
                updateFilesList();
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                textChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                textChanged();
            }
        });
    }

    private void buildMenuBar() {
        setJMenuBar(mnuBar);
        mnuBar.add(menu);
        menu.add(mnuIndex);
    }

    private void textChanged() {
        updateFilesList(txtSearch.getText());
    }

    private void updateFilesList(String query) {
        List<File> list = fileService.search(query);
        showDataInTable(list);
    }

    private void updateFilesList() {
        updateFilesList(null);
    }

    private void showDataInTable(List<File> list) {
        tblFiles.setModel(new DefaultTableModel(list.stream().map(File::getValuesArray).toArray(String[][]::new),
                new String[]{"Name", "Path", "Size", "Date"}));
    }
}