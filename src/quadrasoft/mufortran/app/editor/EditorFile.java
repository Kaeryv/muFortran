package quadrasoft.mufortran.app.editor;

import quadrasoft.mufortran.app.forms.QSearcher;
import quadrasoft.mufortran.app.forms.QSearcherReplacer;
import quadrasoft.mufortran.general.Log;
import quadrasoft.mufortran.general.Session;
import quadrasoft.mufortran.display.TextLineNumber;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("serial")
public class EditorFile extends JScrollPane implements DocumentListener, KeyListener, MouseListener {

    private static int newFilesCount;
    private final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    private final FortranEditorPane editor = new FortranEditorPane();
    boolean edited = false;
    public UndoManager undoManager = new UndoManager() {
        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            UndoableEdit edit = e.getEdit();

            editor.colorise(false);
            setEdited(true);
            if (edit instanceof AbstractDocument.DefaultDocumentEvent) {

                AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) edit;
                if (!event.getType().equals(DocumentEvent.EventType.CHANGE)) {
                    super.addEdit(edit);
                }
            }
        }
    };
    JPanel mainPane = new JPanel();
    TextLineNumber tln;
    private String path = new String("");
    private int editCount = 0;
    private boolean needReopen = false;

    public EditorFile(String filename) {

        if (!filename.equals("")) {
            this.setPath(filename);
            this.setName(filename.substring(filename.lastIndexOf("/") + 1));
            // Loading data from file
            File file = new File(filename);
            try {
                FileInputStream fileStream = new FileInputStream(file);
                editor.read(fileStream, editor);
                editor.initPane();
                fileStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            setNewFilesCount(getNewFilesCount() + 1);
            this.setName("untitled " + EditorFile.getNewFilesCount());
        }

        editor.setDoc(editor.getStyledDocument());
        try {
            editor.setCourant(editor.getDoc().getText(0, editor.getDoc().getLength()));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        editor.colorise(true);
        editor.addKeyListener(this);
        editor.addMouseListener(this);
        editor.getDocument().addUndoableEditListener(undoManager);
        editor.getDoc().addDocumentListener(new MyDocumentListener());

        editor.setBorder(BorderFactory.createEtchedBorder());

        // undoManager.setLimit(5000);
        tln = new TextLineNumber(editor);
        ((DefaultCaret) tln.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainPane.setLayout(new BorderLayout());
        mainPane.add(editor, BorderLayout.CENTER);
        mainPane.add(tln, BorderLayout.WEST);
        try {
            tln.render(countLines(editor.getDoc().getText(0, editor.getDoc().getLength())));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        this.setViewportView(mainPane);
        this.getVerticalScrollBar().setUnitIncrement(20);
        setEdited(false);

    }

    private static int countLines(String str) {
        String[] lines = str.split("\r\n|\r|\n");
        return lines.length;
    }

    public static int getNewFilesCount() {
        return newFilesCount;
    }

    public static void setNewFilesCount(int newFilesCount) {
        EditorFile.newFilesCount = newFilesCount;
    }

    private final void backup() {
        String storagePlace = this.getPath() + ".bak";
        Log.send("Autosaving " + this.getName());

        if (!new File(storagePlace).exists()) {
            try {
                System.out.println(storagePlace);
                new File(storagePlace).createNewFile();
            } catch (IOException e) {
                Log.send("Error: Failed to create backup for the active file.");
                e.printStackTrace();
            }
        }
        save(storagePlace);
        editor.requestFocus();
    }

    public boolean callSave() {
        /*
		 * This function is called to check if the document has unsaved change, if it
		 * does, the functions asks the user to save.
		 */
        if (isEdited()) {
            Log.send("Found modifications on the file, check before closing.\n");
            String tellThemTheName;
            if (!getPath().equals(""))
                tellThemTheName = "The active document " + getPath().substring(getPath().lastIndexOf("/") + 1);
            else
                tellThemTheName = "A new file";
            int jp = JOptionPane.showConfirmDialog(new JFrame(),
                    tellThemTheName + " has been modified.\nDo you want save it?");
            if (jp == JOptionPane.OK_OPTION) {
                save();
                return true;
            } else if (jp == JOptionPane.CANCEL_OPTION) {
                return false;
            } else if (jp == JOptionPane.NO_OPTION) {
                return true;
            } else if (jp == JOptionPane.CLOSED_OPTION) {
                return false;
            }
            return true;
        }
        return true;
    }

    public boolean canRedo() {
        return undoManager.canRedo();
    }

    public boolean canUndo() {
        return undoManager.canUndo();
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
    }

    public int getEditCount() {
        return editCount;
    }

    public void setEditCount(int editCount) {
        this.editCount = editCount;
    }

    public FortranEditorPane getEditor() {
        return editor;

    }

    public String getPath() {
        return path;
    }

    public void setPath(String var_1) {
        path = var_1;
    }

    @Override
    public void insertUpdate(DocumentEvent de) {

    }

    public boolean isEdited() {
        return edited;

    }

    public void setEdited(boolean value) {
        edited = value;
    }

    public boolean isNeedReopen() {
        return needReopen;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getModifiers() == mask) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_Z:
                    if (this.canUndo())
                        undo();
                    break;
                case KeyEvent.VK_Y:
                    if (this.canRedo())
                        redo();
                    break;
                case KeyEvent.VK_S:
                    this.save();
                    break;
                //case KeyEvent.VK_F:
                    //new QSearcher(this.editor);
                    //this.editor.requestFocus();
                //    break;

                case KeyEvent.VK_F:
                    new QSearcherReplacer(this.editor);
                    //this.editor.requestFocus();

                    break;
            }
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getModifiers() != mask) {
        } else if (e.getKeyCode() == KeyEvent.VK_V || e.getKeyCode() == KeyEvent.VK_X)
            editor.colorise(false);
    }

    @Override
    public void keyTyped(KeyEvent arg0) {

    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    public void redo() {
        undoManager.redo();
        editor.colorise(false);
    }

    @Override
    public void removeUpdate(DocumentEvent de) {

    }

    void save() {
		/*
		 * This function is the default save option, the file is saved at it's actual
		 * path. If the file has no proper path, the function calls saveAs()
		 */
        Log.send("Saving : \"" + getPath() + "\"");
        if (!getPath().equals("")) {
            save(getPath());
        } else
            saveAs();
    }

    public void save(String filename) {
        try {
            String text = editor.getDoc().getText(0, editor.getDoc().getLength());
            text = text.replaceAll("\\n", "\r\n");
            // Write file content
            File file = new File(filename);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(text);
            fileWriter.close();
            // File is set as unedited
            this.setEdited(false);
            // Backup file is not needed anymore
            if (new File(this.getPath() + ".bak").exists() && !filename.contains(".bak")) {
                new File(this.getPath() + ".bak").delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveAs() {
		/*
		 * Saves a document opened in �Fort in a new location. The user is prompted a
		 * location on filesystem.
		 */
        // We propose the project folder.
        String directory = Session.getWorkDir();
        JFileChooser chooser = new JFileChooser(directory);
        chooser.removeChoosableFileFilter(chooser.getFileFilter());
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            // We instantly get rid of nasty windows-only filesystem
            setPath(chooser.getSelectedFile().getAbsolutePath().replaceAll("\\\\", "/"));
            save(getPath());
            // We re-open the file from the new path.
            needReopen = true;
        }

    }

    public void searchLine(String strLine, String word) {
        editor.search(word, Integer.parseInt(strLine));
        editor.requestFocus();
    }

    public void undo() {
        undoManager.undo();
    }

    public void updateLines() {
        editor.getDocument().addDocumentListener(this);
        this.changedUpdate(null);
    }

    protected class MyDocumentListener implements DocumentListener {
        @Override
        public void changedUpdate(DocumentEvent e) {
            // displayEditInfo(e);
        }

        private void displayEditInfo(DocumentEvent e) {
            if (Session.isAutoSave() && !getPath().equals("")) {
                editCount++;
                if (editCount >= Session.getAutoSaveTreshold()) {
                    backup();
                    editCount = 0;
                }
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            displayEditInfo(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            displayEditInfo(e);
        }
    }

}
