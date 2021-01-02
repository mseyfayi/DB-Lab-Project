package db.everything;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EverythingGUI extends JFrame {
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
//        updateFilesList("");
    }

    private void setUpHandlers() {
        mnuIndex.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Remove this code and add code for indexing all files in the database:
                JOptionPane.showMessageDialog(null, "Index All Files",
                        "", JOptionPane.INFORMATION_MESSAGE);
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
        // TODO: search in the db for files whose name contain txtSearch's text
        updateFilesList(txtSearch.getText());
    }

    private void updateFilesList(String query) {
        // TODO this is just a demo (it shows all files in My Documents). Make appropriate changes:
        myDocumentsFiles();
    }

    // TODO This is just a demo (to show you how you can fill a JTable):
    private void myDocumentsFiles() {
        String myDocuments = System.getProperty("user.home");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

        try (Stream<Path> files = Files.list(Paths.get(myDocuments))) {
            List<Object[]> results = files
                    .map(f -> {
                        try {
                            BasicFileAttributes attribs = Files.readAttributes(f, BasicFileAttributes.class);

                            return new Object[]{f.toFile().getName(),
                                    f.getParent().toFile().getAbsolutePath(),
                                    attribs.size(),
                                    sdf.format(attribs.creationTime().toMillis())
                            };
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).collect(Collectors.toList());


            tblFiles.setModel(new DefaultTableModel(results.toArray(new Object[0][]),
                    new String[]{"Name", "Path", "Size", "Date"}));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}