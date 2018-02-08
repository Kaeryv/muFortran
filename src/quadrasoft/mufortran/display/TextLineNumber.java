package quadrasoft.mufortran.display;

import quadrasoft.mufortran.app.editor.FortranEditorPane;
import quadrasoft.mufortran.general.Session;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class TextLineNumber extends JTextPane implements DocumentListener, CaretListener {

    private static final long serialVersionUID = 1L;
    private int lineNumber = 1;
    private StyledDocument doc;

    public TextLineNumber(FortranEditorPane editor) {
        StyledDocument lineDoc = this.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_RIGHT);
        lineDoc.setParagraphAttributes(0, lineDoc.getLength(), center, false);
        this.setFont(editor.getFont());
        this.setText("1 ");
        this.setEditable(false);
        this.setBackground(new Color(60, 60, 70));
        this.setForeground(new Color(220, 230, 255));
        doc = editor.getStyledDocument();
        editor.getDocument().addDocumentListener(this);
        editor.addCaretListener(this);
        this.setFont(
                new Font(Session.parameter("FontName"), Font.PLAIN, Integer.parseInt(Session.parameter("FontSize"))));
        new LinePainter(editor, new Color(200, 100, 30, 20));
    }

    private static int countLines(String str) {
        String[] lines = str.split("\r\n|\r|\n");
        return lines.length;
    }

    @Override
    public void caretUpdate(CaretEvent arg0) {
    }

    @Override
    public void changedUpdate(DocumentEvent arg0) {
        // render(false);
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public void insertUpdate(DocumentEvent arg0) {
        String content;

        try {
            content = doc.getText(0, doc.getLength());
            int lines = countLines(content);
            if (this.getLineNumber() != lines) {
                render(lines);
                this.setLineNumber(lines);
            } else {

            }
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void removeUpdate(DocumentEvent arg0) {
        String content;

        try {
            content = doc.getText(0, doc.getLength());
            int lines = countLines(content);
            if (this.getLineNumber() != lines) {
                render(lines);
                this.setLineNumber(lines);
            } else {

            }
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void render(int lineNbr) {
        lineNbr++;
        this.setText("");
        for (int i = 1; i < lineNbr; i++) {
            this.setText(this.getText() + i + " \n");
        }

    }

}
