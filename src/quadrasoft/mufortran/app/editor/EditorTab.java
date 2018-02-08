package quadrasoft.mufortran.app.editor;

import quadrasoft.mufortran.app.forms.QSearcher;
import quadrasoft.mufortran.app.forms.QSearcherReplacer;
import quadrasoft.mufortran.fortran.BinaryManager;
import quadrasoft.mufortran.general.Log;
import quadrasoft.mufortran.general.Session;
import quadrasoft.mufortran.resources.Resources;
import quadrasoft.mufortran.resources.Strings;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

@SuppressWarnings("serial")
public class EditorTab extends JPanel implements ActionListener, KeyListener, MouseListener {
    private final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    public JTabbedPane editorPane = new JTabbedPane();
    private final JButton closeButton = new JButton();
    private final JButton undoButton = new JButton();
    private final JButton redoButton = new JButton();
    private final JButton newButton = new JButton();
    private final JButton openButton = new JButton();
    private final JButton saveButton = new JButton();
    private final JButton saveasButton = new JButton();
    private final JButton cutButton = new JButton();
    private final JButton copyButton = new JButton();
    private final JButton pasteButton = new JButton();
    private final JButton searchButton = new JButton();
    private final JButton nextsearchButton = new JButton();
    private final JButton replaceButton = new JButton();
    private final JToggleButton debugButton = new JToggleButton();
    private final JButton compileButton = new JButton();
    private final JButton runButton = new JButton();
    private final JButton clearProjectButton = new JButton();
    private final JButton linkProjectButton = new JButton();
    private final JButton compileFileButton = new JButton();
    private final JLabel infoSelection = new JLabel();
    private final JTabbedPane utilsPane = new JTabbedPane();

    private final JPanel consoleStuff = new JPanel();
    private JToolBar toolBar;

    public EditorTab() {
        super();
        this.setLayout(new BorderLayout());
        this.initComponents();
        this.updateButtons();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (editorPane.getSelectedIndex() != -1)
            ((EditorFile) editorPane.getSelectedComponent()).getEditor().requestFocus();
        // ((EditorFile) editorPane.getSelectedComponent()).requestFocus();
        if (e.getSource().equals(undoButton))
            undo();
        else if (e.getSource().equals(redoButton))
            redo();
        else if (e.getSource().equals(newButton)) {
            newFile();
        } else if (e.getSource().equals(openButton)) {
            String filename;
            String directory = Session.getWorkDir();
            JFileChooser chooser = new JFileChooser(directory);
            FileFilter filter = new FileNameExtensionFilter("Fortran source", "f", "f90", "f95", "F", "F90");
            chooser.removeChoosableFileFilter(chooser.getFileFilter());
            chooser.addChoosableFileFilter(filter);
            filter = new FileNameExtensionFilter("Other text files", "txt", "mod", "log", "info", "dat");
            chooser.addChoosableFileFilter(filter);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                filename = chooser.getSelectedFile().getAbsolutePath();
                open(filename.replaceAll("\\\\", "/"));
                // ((EditorFile)
                // editorPane.getSelectedComponent()).undoManager.discardAllEdits();
                ((EditorFile) editorPane.getSelectedComponent()).getEditor().addMouseListener(this);
                updateButtons();
            }
        } else if (e.getSource().equals(saveButton)) {
            if (editorPane.getSelectedIndex() != -1) {
                ((EditorFile) editorPane.getSelectedComponent()).save();
                saveButton.setEnabled(false);
                saveasButton.setEnabled(false);
                if (((EditorFile) editorPane.getSelectedComponent()).isNeedReopen()) {
                    String temp1 = ((EditorFile) editorPane.getSelectedComponent()).getPath();
                    editorPane.remove((editorPane.getSelectedComponent()));
                    open(temp1);
                }
            }

        } else if (e.getSource().equals(saveasButton)) {
            if (editorPane.getSelectedIndex() != -1)
                ((EditorFile) editorPane.getSelectedComponent()).saveAs();
            saveButton.setEnabled(false);
            saveasButton.setEnabled(false);
        } else if (e.getSource().equals(copyButton)) {
            if (editorPane.getSelectedIndex() != -1)
                ((EditorFile) editorPane.getSelectedComponent()).getEditor().copy();
        } else if (e.getSource().equals(cutButton)) {
            if (editorPane.getSelectedIndex() != -1) {
                ((EditorFile) editorPane.getSelectedComponent()).getEditor().cut();
                updateButtons();
                ((EditorFile) editorPane.getSelectedComponent()).getEditor().colorise(false);
            }
        } else if (e.getSource().equals(closeButton)) {
            close();
        } else if (e.getSource().equals(pasteButton)) {
            if (editorPane.getSelectedIndex() != -1) {
                ((EditorFile) editorPane.getSelectedComponent()).getEditor().paste();
                ((EditorFile) editorPane.getSelectedComponent()).getEditor().colorise(false);
                updateButtons();
            }
        } else if (e.getSource().equals(searchButton)) {
            if (editorPane.getSelectedIndex() != -1) {
                new QSearcher(((EditorFile) editorPane.getSelectedComponent()).getEditor());
                ((EditorFile) editorPane.getSelectedComponent()).getEditor().requestFocus();
            }
        } else if (e.getSource().equals(replaceButton)) {
            if (editorPane.getSelectedIndex() != -1) {
                new QSearcherReplacer(((EditorFile) editorPane.getSelectedComponent()).getEditor());
                ((EditorFile) editorPane.getSelectedComponent()).getEditor().requestFocus();
            }
        } else if (e.getSource().equals(linkProjectButton)) {
            if (Session.getActiveProject() != null) {
                if (iHaveEditedFilesHere()) {
                    if (JOptionPane.showConfirmDialog(new JFrame(),
                            "Better save changes before compilation.") == JOptionPane.OK_OPTION) {
                        saveAll();
                    }
                }
                BinaryManager.bindProject(Session.getActiveProject());
                BinaryManager.linkProject();
            } else {
                Log.send("Open a project first.");
            }
        } else if (e.getSource().equals(clearProjectButton)) {
            if (Session.getActiveProject() != null) {
                File folder = new File(
                        Session.getActiveProject().getPath() + Session.getActiveProject().getObjectPath());
                File fList[] = folder.listFiles();
                // Searches .mod
                for (int i = 0; i < fList.length; i++) {
                    String pes = fList[i].getName();
                    if (pes.endsWith(".mod")) {
                        // and deletes
                        fList[i].delete();
                    }
                }
            }
        } else if (e.getSource().equals(compileFileButton)) {
            if (Session.getActiveProject() != null) {
                if (editorPane.getSelectedComponent() != null) {
                    if (((EditorFile) editorPane.getSelectedComponent()).isEdited()) {
                        if (JOptionPane.showConfirmDialog(new JFrame(),
                                "Better save this file before compilation.") == JOptionPane.OK_OPTION) {
                            ((EditorFile) editorPane.getSelectedComponent()).save();
                        }
                    }
                    BinaryManager.bindProject(Session.getActiveProject());
                    BinaryManager.debug(debugButton.isSelected());
                    BinaryManager.compileFile(((EditorFile) editorPane.getSelectedComponent()).getPath());
                }

            } else {
                Log.send("Open a project first.");
            }
        }
        if (e.getSource().equals(compileButton)) {
            if (Session.getActiveProject() != null) {
                if (iHaveEditedFilesHere()) {
                    if (JOptionPane.showConfirmDialog(new JFrame(),
                            "Better save changes before compilation.") == JOptionPane.OK_OPTION) {
                        saveAll();
                    }
                }
                BinaryManager.bindProject(Session.getActiveProject());
                BinaryManager.debug(debugButton.isSelected());
                BinaryManager.compileProject();
            } else {
                Log.send("Open a project first.");
            }
        } else if (e.getSource().equals(runButton)) {
            if (Session.getActiveProject() != null) {
                BinaryManager.bindProject(Session.getActiveProject());
                BinaryManager.launchProject();
                ;
            } else {
                Log.send("Open a project first.");
            }
        } else if (e.getSource().equals(debugButton)) {
            if (debugButton.isSelected())
                debugButton.setIcon(Resources.getImageResource("icon.redbug"));
            else
                debugButton.setIcon(Resources.getImageResource("icon.bug"));
        }
    }

    public void close() {

        if (editorPane.getSelectedIndex() != -1) {
            Log.send("Closing active file");
            if (((EditorFile) editorPane.getSelectedComponent()).callSave())
                editorPane.remove(editorPane.getSelectedIndex());
        } else {
            Log.send("Nothing to close.");
        }

    }

    private boolean iHaveEditedFilesHere() {
        for (Component file : editorPane.getComponents()) {
            if (((EditorFile) file).isEdited())
                return true;
        }
        return false;
    }

    private void initComponents() {
        Border emptyBorder = BorderFactory.createEmptyBorder();

        infoSelection.setText(Strings.s("Forms:NoSelection"));

        newButton.setIcon(Resources.getImageResource("icon.newfile"));
        newButton.addActionListener(this);
        newButton.setOpaque(false);
        newButton.setBorder(emptyBorder);
        newButton.setToolTipText("Create new File");

        openButton.setIcon(Resources.getImageResource("icon.folder"));
        openButton.setPreferredSize(new Dimension(30, 20));
        openButton.addActionListener(this);
        openButton.setOpaque(false);
        openButton.setBorder(emptyBorder);
        openButton.setToolTipText("Open File");

        saveButton.setIcon(Resources.getImageResource("icon.save"));
        saveButton.setPreferredSize(new Dimension(30, 20));
        saveButton.addActionListener(this);
        saveButton.setToolTipText(KeyEvent.getKeyModifiersText(mask) + "+S");
        saveButton.setOpaque(false);
        saveButton.setBorder(emptyBorder);
        saveButton.setToolTipText("Save File");

        saveasButton.setIcon(Resources.getImageResource("icon.saveas"));
        saveasButton.setPreferredSize(new Dimension(30, 20));
        saveasButton.addActionListener(this);
        saveasButton.setOpaque(false);
        saveasButton.setBorder(emptyBorder);
        saveasButton.setToolTipText("Save File as");

        undoButton.setIcon(Resources.getImageResource("icon.undo"));
        undoButton.setPreferredSize(new Dimension(30, 20));
        undoButton.addActionListener(this);
        undoButton.setToolTipText(KeyEvent.getKeyModifiersText(mask) + "+Z");
        undoButton.setOpaque(false);
        undoButton.setBorder(emptyBorder);
        undoButton.setToolTipText("Undo");

        redoButton.setIcon(Resources.getImageResource("icon.redo"));
        redoButton.setPreferredSize(new Dimension(30, 20));
        redoButton.addActionListener(this);
        redoButton.setToolTipText(KeyEvent.getKeyModifiersText(mask) + "+Y");
        redoButton.setOpaque(false);
        redoButton.setBorder(emptyBorder);
        redoButton.setToolTipText("Redo");

        copyButton.setIcon(Resources.getImageResource("icon.copy"));
        copyButton.setPreferredSize(new Dimension(30, 20));
        copyButton.addActionListener(this);
        copyButton.setToolTipText(KeyEvent.getKeyModifiersText(mask) + "+C");
        copyButton.setOpaque(false);
        copyButton.setBorder(emptyBorder);
        copyButton.setToolTipText("Copy");

        cutButton.setIcon(Resources.getImageResource("icon.cut"));
        cutButton.setPreferredSize(new Dimension(30, 20));
        cutButton.addActionListener(this);
        cutButton.setToolTipText(KeyEvent.getKeyModifiersText(mask) + "+X");
        cutButton.setOpaque(false);
        cutButton.setBorder(emptyBorder);
        cutButton.setToolTipText("Cut");

        pasteButton.setIcon(Resources.getImageResource("icon.paste"));
        pasteButton.setPreferredSize(new Dimension(30, 20));
        pasteButton.addActionListener(this);
        pasteButton.setToolTipText(KeyEvent.getKeyModifiersText(mask) + "+V");
        pasteButton.setOpaque(false);
        pasteButton.setBorder(emptyBorder);
        pasteButton.setToolTipText("Paste");

        searchButton.setIcon(Resources.getImageResource("icon.magnifier"));
        searchButton.setPreferredSize(new Dimension(30, 20));
        searchButton.addActionListener(this);
        searchButton.setToolTipText(KeyEvent.getKeyModifiersText(mask) + "+F");
        searchButton.setOpaque(false);
        searchButton.setBorder(emptyBorder);
        searchButton.setToolTipText("Search");
        searchButton.setVisible(false);

        replaceButton.setIcon(Resources.getImageResource("icon.magnifier"));
        replaceButton.setPreferredSize(new Dimension(30, 20));
        replaceButton.addActionListener(this);
        replaceButton.setToolTipText(KeyEvent.getKeyModifiersText(mask) + "+R");
        replaceButton.setOpaque(false);
        replaceButton.setBorder(emptyBorder);
        replaceButton.setToolTipText("Search and replace");

        compileButton.addActionListener(this);
        compileButton.setIcon(Resources.getImageResource("icon.build"));
        compileButton.setOpaque(false);
        compileButton.setBorder(emptyBorder);
        compileButton.setToolTipText("Compile project");

        linkProjectButton.setIcon(Resources.getImageResource("icon.link"));
        linkProjectButton.setOpaque(false);
        linkProjectButton.setBorder(emptyBorder);
        linkProjectButton.setToolTipText("Link project");
        linkProjectButton.addActionListener(this);

        compileFileButton.setIcon(Resources.getImageResource("icon.build_file"));
        compileFileButton.setOpaque(false);
        compileFileButton.setBorder(emptyBorder);
        compileFileButton.setToolTipText("Compile active file (tab).");
        compileFileButton.addActionListener(this);

        runButton.setIcon(Resources.getImageResource("icon.play"));
        runButton.setOpaque(false);
        runButton.setBorder(emptyBorder);
        runButton.setToolTipText("Run project executable");
        runButton.addActionListener(this);

        clearProjectButton.setOpaque(false);
        clearProjectButton.setIcon(Resources.getImageResource("icon.eraser"));
        clearProjectButton.addActionListener(this);
        clearProjectButton.setToolTipText("Clear project's mod files.");

        closeButton.addActionListener(this);
        closeButton.setIcon(Resources.getImageResource("icon.cancel"));
        closeButton.setOpaque(false);
        closeButton.setBorder(emptyBorder);
        closeButton.setToolTipText("Close selected file");

        debugButton.addActionListener(this);
        debugButton.setIcon(Resources.getImageResource("icon.bug"));
        debugButton.setOpaque(false);
        debugButton.setBorder(emptyBorder);
        debugButton.setToolTipText("Enable debug configuration");

        toolBar = new JToolBar();
        toolBar.add(newButton);
        toolBar.addSeparator();
        toolBar.add(openButton);
        toolBar.addSeparator();
        toolBar.add(closeButton);
        toolBar.addSeparator();
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.add(saveasButton);
        toolBar.addSeparator();
        toolBar.add(undoButton);
        toolBar.add(redoButton);
        toolBar.addSeparator();
        toolBar.add(copyButton);
        toolBar.add(cutButton);
        toolBar.add(pasteButton);
        toolBar.addSeparator();
        toolBar.add(searchButton);
        toolBar.addSeparator();
        toolBar.add(replaceButton);
        toolBar.addSeparator();
        toolBar.add(clearProjectButton);
        toolBar.add(compileFileButton);
        toolBar.add(compileButton);
        toolBar.addSeparator();
        toolBar.add(debugButton);
        toolBar.addSeparator();
        toolBar.add(linkProjectButton);
        toolBar.addSeparator();
        toolBar.add(runButton);

        closeButton.setEnabled(false);
        redoButton.setEnabled(false);
        undoButton.setEnabled(false);
        saveButton.setEnabled(false);
        saveasButton.setEnabled(false);
        cutButton.setEnabled(false);
        copyButton.setEnabled(false);
        pasteButton.setEnabled(false);
        searchButton.setEnabled(false);
        nextsearchButton.setEnabled(false);
        replaceButton.setEnabled(false);
        toolBar.setFloatable(true);
        toolBar.setRollover(true);

        utilsPane.setPreferredSize(new Dimension(800, 200));

        consoleStuff.setLayout(new BorderLayout());
        consoleStuff.add(infoSelection, BorderLayout.CENTER);
        consoleStuff.setBorder(BorderFactory.createEtchedBorder());
        editorPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                updateButtons();
            }
        });

        this.add(editorPane);
        this.add(consoleStuff, BorderLayout.SOUTH);
        this.add(toolBar, BorderLayout.NORTH);
    }

    public boolean isOpenend(String path) {
        for (Component temp : editorPane.getComponents()) {
            if (((EditorFile) temp).getPath().equalsIgnoreCase(path)) {
                editorPane.setSelectedComponent(temp);
                return true;
            }
        }
        return false;
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

        updateButtons();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if (e.getSource() instanceof FortranEditorPane) {
            FortranEditorPane temp = (FortranEditorPane) e.getSource();
            if (temp.getSelectedText() != null && temp.getSelectedText() != "") {
                infoSelection.setText(Strings.s("Forms:SelectedChars") + temp.getSelectedText().length());
            } else {
                infoSelection.setText(Strings.s("Forms:NoSelection"));
            }
        }

    }

    public void newFile() {
        Log.send("Adding new file");
        editorPane.addTab("untitled " + (EditorFile.getNewFilesCount() + 1), new EditorFile(""));
        editorPane.setSelectedIndex(editorPane.getTabCount() - 1);
        ((EditorFile) editorPane.getSelectedComponent()).getEditor().addKeyListener(this);
        ((EditorFile) editorPane.getSelectedComponent()).getEditor().addMouseListener(this);
        updateButtons();
    }

    public void open(String filename) {
        if (filename != null && new File(filename).exists() && !isOpenend(filename)) {
            Log.send("Opening " + "\"" + filename + "\"");
            editorPane.addTab(filename.substring(filename.lastIndexOf("/") + 1, filename.length()),
                    new EditorFile(filename));
            editorPane.setSelectedIndex(editorPane.getComponentCount() - 1);
            ((EditorFile) editorPane.getSelectedComponent()).getEditor().addKeyListener(this);
            ((EditorFile) editorPane.getSelectedComponent()).getEditor().addMouseListener(this);
        } else if (isOpenend(filename)) {
            Log.send("File already loaded.");
        } else {
            // Log.send("Error: Unable to load "+filename);
        }
    }

    public void redo() {
        if (editorPane.getSelectedIndex() != -1) {
            int i = 0;
            try {
                while (((EditorFile) editorPane.getSelectedComponent()).canRedo() && i < 2) {
                    ((EditorFile) editorPane.getSelectedComponent()).redo();
                    if (!((EditorFile) editorPane.getSelectedComponent()).getEditor().getDoc()
                            .getText(0,
                                    ((EditorFile) editorPane.getSelectedComponent()).getEditor().getDoc().getLength())
                            .equals(((EditorFile) editorPane.getSelectedComponent()).getEditor().getCourant())) {
                        ((EditorFile) editorPane.getSelectedComponent()).getEditor().updateCourant();
                        i++;
                    }
                }
                if (i >= 2)
                    ((EditorFile) editorPane.getSelectedComponent()).undo();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            updateButtons();
            ((EditorFile) editorPane.getSelectedComponent()).getEditor().updateCourant();
            ((EditorFile) editorPane.getSelectedComponent()).getEditor().addMouseListener(this);

        }
    }

    public boolean saveAll() {
        for (Component file : editorPane.getComponents()) {
            if (file instanceof EditorFile) {
                if (!((EditorFile) file).callSave()) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
        return true;

    }

    public void searchLineInActiveFile(String line, String word) {
        ((EditorFile) editorPane.getSelectedComponent()).searchLine(line, word);
    }

    public void undo() {
        if (editorPane.getSelectedIndex() != -1) {
            try {
                while (((EditorFile) editorPane.getSelectedComponent()).canUndo()
                        && ((EditorFile) editorPane.getSelectedComponent()).getEditor().getDoc()
                        .getText(0,
                                ((EditorFile) editorPane.getSelectedComponent()).getEditor().getDoc()
                                        .getLength())
                        .equals(((EditorFile) editorPane.getSelectedComponent()).getEditor().getCourant())) {
                    ((EditorFile) editorPane.getSelectedComponent()).undoManager.undo();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            updateButtons();
            ((EditorFile) editorPane.getSelectedComponent()).getEditor().updateCourant();
        }
    }

    public void updateButtons() {
        if (editorPane.getSelectedIndex() != -1) {
            undoButton.setEnabled(((EditorFile) editorPane.getSelectedComponent()).canUndo());
            redoButton.setEnabled(((EditorFile) editorPane.getSelectedComponent()).canRedo());
            closeButton.setEnabled(true);
            cutButton.setEnabled(true);
            copyButton.setEnabled(true);
            pasteButton.setEnabled(true);
            searchButton.setEnabled(true);
            nextsearchButton.setEnabled(true);
            replaceButton.setEnabled(true);
            compileFileButton.setEnabled(true);
            // commentButton.setEnabled(true);
            // unCommentButton.setEnabled(true);
            if (((EditorFile) editorPane.getSelectedComponent()).isEdited()) {
                saveButton.setEnabled(true);
                saveasButton.setEnabled(true);
            } else {
                saveButton.setEnabled(false);
                saveasButton.setEnabled(false);
            }

        } else {
            closeButton.setEnabled(false);
            redoButton.setEnabled(false);
            undoButton.setEnabled(false);
            saveButton.setEnabled(false);
            saveasButton.setEnabled(false);
            cutButton.setEnabled(false);
            copyButton.setEnabled(false);
            pasteButton.setEnabled(false);
            searchButton.setEnabled(false);
            nextsearchButton.setEnabled(false);
            replaceButton.setEnabled(false);
            compileFileButton.setEnabled(false);
            // commentButton.setEnabled(false);
            // unCommentButton.setEnabled(false);
        }

    }

}
