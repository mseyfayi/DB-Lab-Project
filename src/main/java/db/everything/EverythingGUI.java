package db.everything;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class EverythingGUI extends JFrame {
    private FileService fileService = FileService.getInstance();

    private final String[] columns = {"Name", "Path", "Size", "Date"};

    private final JMenuBar mnuBar = new JMenuBar();

    private final JMenu menuFile = new JMenu("File");
    private final JMenuItem mnuFileIndex = new JMenuItem("Index All Files");

    private final JMenu menuSort = new JMenu("Sort");
    private final ButtonGroup mnuSortGroup = new ButtonGroup();
    private final JMenuItem[] mnuSortItems = new JRadioButtonMenuItem[5];
    private String selectedSort = "";

    private final JMenu menuSearch = new JMenu("Search");
    private final JMenuItem mnuSearchMatchCase = new JCheckBoxMenuItem("Match Case", false);
    private boolean isMatchCheckSelected = false;

    private JPopupMenu contextMenu;
    private final JTextField txtSearch = new JTextField();
    private final JTable tblFiles = new JTable(new Object[0][0], columns);

    private JScrollPane scrollPane = new JScrollPane(tblFiles);

    private final Panel panel = new Panel();

    private EverythingGUI() {
        mnuSortItems[0] = new JRadioButtonMenuItem("None", true);
        for (int i = 0; i < columns.length; i++)
            mnuSortItems[i + 1] = new JRadioButtonMenuItem(columns[i], false);
    }

    private void buildContextMenu(String fullPath, String dir) {
        contextMenu = new JPopupMenu();
        JMenuItem ctmOpen = new JMenuItem("Open");
        JMenuItem ctmOpenPath = new JMenuItem("Open Path");
        ctmOpen.addActionListener(actionEvent -> {
            try {
                Desktop.getDesktop().open(new File(fullPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        ctmOpenPath.addActionListener(actionEvent -> {
            try {
                Desktop.getDesktop().open(new File(dir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        contextMenu.add(ctmOpen);
        contextMenu.add(ctmOpenPath);
    }

    private void makeMainPanel() {
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
        mnuFileIndex.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "Index All Files", "", JOptionPane.INFORMATION_MESSAGE);
                fileService.reindexTable();
                updateFilesList();
            }
        });

        mnuSearchMatchCase.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isMatchCheckSelected = !isMatchCheckSelected;
                updateFilesList(txtSearch.getText());
            }
        });

        for (JMenuItem mnuSortItem : mnuSortItems) {
            mnuSortItem.addMouseListener(
                    new MouseInputAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            selectedSort = mnuSortItem.getText();
                            changeSortCheckboxesState(mnuSortItem);
                            menuSort.revalidate();
                            updateFilesList(txtSearch.getText());
                        }
                    }
            );
        }

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

        tblFiles.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent mouseEvent) {
                        int row = tblFiles.rowAtPoint(mouseEvent.getPoint());

                        String dir = tblFiles.getValueAt(row, 1).toString();
                        String name = tblFiles.getValueAt(row, 0).toString();
                        String fullPath = dir + '/' + name;

                        if (!mouseEvent.isPopupTrigger()) {
                            if (contextMenu != null)
                                contextMenu.setVisible(false);

                            if (mouseEvent.getClickCount() == 2) {
                                try {
                                    Desktop.getDesktop().open(new File(fullPath));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            buildContextMenu(fullPath, dir);
                            contextMenu.setVisible(true);
                            contextMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
                        }
                    }
                }
        );
    }

    private void changeSortCheckboxesState(JMenuItem source) {
        for (JMenuItem mnuSortItem : mnuSortItems) {
            mnuSortItem.setSelected(source == mnuSortItem);
        }
    }

    private void buildMenuBar() {
        setJMenuBar(mnuBar);

        mnuBar.add(menuFile);
        menuFile.add(mnuFileIndex);

        mnuBar.add(menuSort);
        for (JMenuItem mnuSortItem : mnuSortItems) {
            mnuSortGroup.add(mnuSortItem);
            menuSort.add(mnuSortItem);
        }

        mnuBar.add(menuSearch);
        menuSearch.add(mnuSearchMatchCase);
    }

    private void textChanged() {
        updateFilesList(txtSearch.getText());
    }

    private void updateFilesList(String query) {
        List<IFile> list = fileService.search(query, selectedSort, isMatchCheckSelected);
        showDataInTable(list);
    }

    private void updateFilesList() {
        updateFilesList(null);
    }

    private void showDataInTable(List<IFile> list) {
        String[][] matrix = list.stream().map(IFile::getValuesArray).toArray(String[][]::new);
        DefaultTableModel tableModel = new DefaultTableModel(matrix, columns) {
            @Override
            public boolean isCellEditable(int i, int i1) {
                return false;
            }
        };
        tblFiles.setModel(tableModel);
        tblFiles.revalidate();
        panel.repaint();
    }

    private void buildView() {
        setTitle("My Everything");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUpHandlers();
        buildMenuBar();
        makeMainPanel();
    }

    private void runWatcher() {
        Thread thread = new Thread(() -> fileService.fileWatch(() -> updateFilesList(txtSearch.getText())));
        thread.start();
    }

    private void run() {
        buildView();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        runWatcher();
    }

    public static void main(String[] args) {
        EverythingGUI gui = new EverythingGUI();
        gui.run();
    }
}