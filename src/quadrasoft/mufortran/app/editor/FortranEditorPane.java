package quadrasoft.mufortran.app.editor;

import quadrasoft.mufortran.general.Session;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FortranEditorPane extends JTextPane {
    /**
     * This class handles all things referred to syntax highlighting, printing text.
     */
    private static final long serialVersionUID = 2;
    private final String[] keywords = {
            /*
             * Types
			 */
            "integer", "real", "complex",
			/*
			 * FORTRAN 77
			 */
            "assign", "backspace", "block data", "call", "close", "common", "continue", "data", "dimension", "do",
            "else", "else if", "end", "endfile", "endif", "entry", "equivalence", "external", "format", "function",
            "goto", "if", "implicit", "inquire", "intrinsic", "open", "parameter", "pause", "print", "program", "read",
            "return", "rewind", "rewrite", "save", "stop", "subroutine", "then", "write",
			/*
			 * FORTRAN 90
			 */
            "allocatable", "allocate", "case", "contains", "cycle", "deallocate", "elsewhere", "exit", "include",
            "interface", "intent", "module", "namelist", "nullify", "only", "operator", "optional", "pointer",
            "private", "procedure", "public", "recursive", "result", "select", "sequence", "target", "use", "while",
            "where",
			/*
			 * FORTRAN 95
			 */
            "elemental", "forall", "pure.",
			/*
			 * FORTRAN 2003
			 */
            "abstract", "associate", "asynchronous", "bind", "class", "deferred", "enum", "enumerator", "extends",
            "final", "flush", "generic", "import", "non_overridable", "nopass", "pass", "protected", "value",
            "volatile", "wait",
			/*
			 * FORTRAN 2008
			 */
            "block", "codimension", "do concurrent", "contiguous", "critical", "error stop", "submodule", "sync all",
            "sync images", "sync memory", "lock", "unlock"};
    // Styles that are common to all editing panes
    private Style normalStyle;

    private Style functionStyle;

    private Style numberStyle;

    private Style operatorStyle;

    private Style stringStyle;

    private Style pgplotStyle;

    private Style commentStyle;

    private StyledDocument doc;

    private String courant;
    private String[] operators = {"^", "+", "-", "*", "/", "|", "::", "=", "%"};
    private String[] pgplots = {"PGBEGIN", "PGSLW", "PGSCH", "PGPAGE", "PGPAGE", "PGSWIN", "PGBOX", "PGLABEL", "PGSCI",
            "PGSLS", "PGLINE", "PGEND", "PGVSTD", "PGSCRN", "PGPOINT", "PGERAS", "PGRECT", "PGSCF", "PGLAB", "PGBEG",
            "PGSUBP", "PGSFS", "PGCIRC"};

    public FortranEditorPane() {
        super();
        this.initPane();
        this.initStyles();
        this.initFonts();

    }

    private static int countLines(String str) {
        String[] lines = str.split("\r\n|\r|\n");
        return lines.length;
    }

    public void colorise(boolean all) {
        try {
            String text = getDoc().getText(0, getDoc().getLength());
            int curs, i, j, k;
            String tmp = getCourant();
            setCourant(text);

            if (!tmp.equals(text) && !all) {

                curs = this.getCaretPosition();
                k = text.length() - tmp.length();
                if ((i = curs - 50) < 0)
                    i = 0;
                if ((j = curs + 50) > text.length())
                    j = text.length();

                if (k > 0)
                    if ((i -= k) < 0)
                        i = 0;

                for (; i > 0; i--) {
                    if (text.charAt(i) == '\n')
                        break;
                    else if (text.charAt(i) == '\r')
                        break;
                }
                k = text.indexOf("\n", j);
                if (k == -1)
                    k = text.indexOf("\r", j);
                if (k == -1)
                    k = text.length();
                j = k;

                this.colorise(i, j, text.substring(i, j));

                this.setStyledDocument(getDoc());
                this.setCaretPosition(curs);
            } else if (all)
                this.colorise(0, getDoc().getLength(), text);

        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
    }

    public void colorise(int length) {
        try {
            String text = getDoc().getText(0, getDoc().getLength());
            int curs, i, j, k;

            if (!getCourant().equals(text)) {

                curs = this.getCaretPosition();
                k = length;
                if ((i = curs - 50) < 0)
                    i = 0;
                if ((j = curs + 50) > text.length())
                    j = text.length();

                if (k > 0)
                    if ((i -= k) < 0)
                        i = 0;

                for (; i > 0; i--) {
                    if (text.charAt(i) == '\n')
                        break;
                    else if (text.charAt(i) == '\r')
                        break;
                }
                k = text.indexOf("\n", j);
                if (k == -1)
                    k = text.indexOf("\r", j);
                if (k == -1)
                    k = text.length();
                j = k;

                this.colorise(i, j, text.substring(i, j));

                this.setStyledDocument(getDoc());
                this.setCaretPosition(curs);
            }

            setCourant(getDoc().getText(0, getDoc().getLength()));
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
    }

    public void colorise(int start, int end, String text) {
        int i = 0, j = 0, f = 0, d = 0;

        Matcher matcher;

        getDoc().setCharacterAttributes(start, end - start, normalStyle, true);

        colorise(keywords, functionStyle, text, start, true);
        colorise(operators, operatorStyle, text, start, false);
        colorise(pgplots, pgplotStyle, text, start, true);

        matcher = Pattern.compile("\\\".*?\\\"", Pattern.MULTILINE).matcher(text);
        d = f = 0;
        while (matcher.find(f)) {
            f = matcher.end();
            d = matcher.start();
            getDoc().setCharacterAttributes(d + start, f - d, stringStyle, true);
        }
        matcher = Pattern.compile("\\!.*", Pattern.MULTILINE).matcher(text);
        i = j = 0;
        while (matcher.find(i)) {
            i = matcher.end();
            j = matcher.start();
            getDoc().setCharacterAttributes(j + start, i - j, commentStyle, true);
        }

    }

    public void colorise(String[] tab, Style style, String text, int start, boolean additional_checks) {
        try {
            String mot;
            int k, i, j;
            if (tab == null)
                return;
            for (k = 0; k < tab.length; k++) {
                mot = tab[k];
                i = j = 0;

                while ((j = text.indexOf(mot, i)) != -1) {
                    i = j;
                    if (additional_checks) {
                        String after = getDoc().getText(i + start + mot.length(), 1);
                        if ((after.equals("\n") || after.equals(" ") || after.equals("(") || after.equals(","))
                                || after.equals("*") || after.equals((")"))) {
                            if (i + start > 0) {
                                String before = getDoc().getText(i + start - 1, 1);
                                if ((before.equals("\n") || before.equals(" ") || before.equals("(") || before.equals(","))
                                        || before.equals("*")) {
                                    getDoc().setCharacterAttributes(i + start, mot.length(), style, true);
                                }
                            } else {
                                getDoc().setCharacterAttributes(i + start, mot.length(), style, true);
                            }
                        }
                    } else {
                        getDoc().setCharacterAttributes(i + start, mot.length(), style, true);
                    }
                    i += mot.length();
                }
            }
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getCourant() {
        return courant;
    }

    public void setCourant(String courant) {
        this.courant = courant;
    }

    public StyledDocument getDoc() {
        return doc;
    }

    public void setDoc(StyledDocument doc) {
        this.doc = doc;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    private void initFonts() {
        // Style de base
        StyleConstants.setForeground(normalStyle, Color.black);
        StyleConstants.setBold(normalStyle, false);
        StyleConstants.setFontSize(normalStyle, Integer.parseInt(Session.parameter("FontSize")));

        // Style des functions standard
        StyleConstants.setForeground(functionStyle, new Color(0, 102, 153));
        StyleConstants.setBold(functionStyle, false);

        // Style des nombres
        StyleConstants.setForeground(numberStyle, new Color(150, 0, 0));
        StyleConstants.setBold(numberStyle, false);

        // Style des commentaires
        StyleConstants.setForeground(commentStyle, new Color(150, 150, 150));
        StyleConstants.setBold(commentStyle, false);

        // Style des chaines de caract�res
        StyleConstants.setForeground(stringStyle, new Color(75, 130, 79));
        StyleConstants.setBold(stringStyle, false);

        // Style des op�rateurs
        StyleConstants.setForeground(operatorStyle, new Color(236, 132, 44));
        StyleConstants.setBold(operatorStyle, false);

        // Style pgplot
        StyleConstants.setForeground(pgplotStyle, new Color(250, 0, 150));
        StyleConstants.setBold(pgplotStyle, false);

    }

    public void initPane() {
        this.setFont(
                new Font(Session.parameter("FontName"), Font.PLAIN, Integer.parseInt(Session.parameter("FontSize"))));
        this.setCaretColor(Color.BLUE);
        setDoc(this.getStyledDocument());
        setCourant(new String(""));
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AbstractDocument doc = (AbstractDocument) this.getDocument();
        doc.setDocumentFilter(new NewLineFilter());
        TabStop[] tabstops = new TabStop[30];
        for (int i = 0; i < 30; i++) {
            tabstops[i] = new TabStop(100 + i * 100);
        }
        TabSet tabs = new TabSet(tabstops);
        AttributeSet paraSet = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, tabs);
        this.setParagraphAttributes(paraSet, false);
    }

    private void initStyles() {
        normalStyle = getDoc().addStyle("normal", null);
        functionStyle = getDoc().addStyle("function", normalStyle);
        numberStyle = getDoc().addStyle("number", normalStyle);
        operatorStyle = getDoc().addStyle("operator", normalStyle);
        stringStyle = getDoc().addStyle("string", normalStyle);
        pgplotStyle = getDoc().addStyle("pgplot", normalStyle);
        commentStyle = getDoc().addStyle("comment", normalStyle);
    }

    public void search(String text) {
        int carret = this.getCaretPosition();
        int pos = getCourant().indexOf(text, carret);

        if (pos != -1) {
            this.select(pos, pos + text.length());
        } else {
            carret = 0;
            if ((pos = getCourant().indexOf(text, carret)) != -1) {
                this.select(pos, pos + text.length());
            }
        }
    }

    public void search(String text, int line) {
        int carret = this.getCaretPosition();
        int pos = getCourant().indexOf(text, carret);
        if (pos != -1) {
            int goon = 0;
            while (countLines(getCourant().substring(0, pos)) < line - 1 && goon < countLines(getCourant())) {
                pos = getCourant().indexOf(text, carret);
                goon++;
            }
            this.select(pos, pos + text.length());
        } else {
            carret = 0;
            if ((pos = getCourant().indexOf(text, carret)) != -1) {
                this.select(pos, pos + text.length());
            }
        }
    }

    public void updateCourant() {
        try {
            this.setCourant(getDoc().getText(0, getDoc().getLength()));
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
    }

}

class NewLineFilter extends DocumentFilter {
    private String addWhiteSpace(Document doc, int offset) throws BadLocationException {
        StringBuilder whiteSpace = new StringBuilder("\n");
        Element rootElement = doc.getDefaultRootElement();
        int line = rootElement.getElementIndex(offset);
        int i = rootElement.getElement(line).getStartOffset();

        while (true) {
            String temp = doc.getText(i, 1);

            if (temp.equals(" ") || temp.equals("\t")) {
                whiteSpace.append(temp);
                i++;
            } else
                break;
        }

        return whiteSpace.toString();
    }

    @Override
    public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
        if ("\n".equals(str))
            str = addWhiteSpace(fb.getDocument(), offs);
        else if ("\t".equals(str)) {
            str = "    ";
        }
        //super.insertString(fb, offs, str, a);

    }

    @Override
    public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a) throws BadLocationException {
        if ("\n".equals(str))
            str = addWhiteSpace(fb.getDocument(), offs);
        else if ("\t".equals(str)) {
            str = "    ";
            String old_text = fb.getDocument().getText(offs,length);
            str = "    " + old_text.replaceAll("\\\n","\\\n    ");
            // We use spaces for indentation. Do not use tabs in your code, this is bad.
            // "You should set your editor to emit spaces when you hit the tab key."
            // cf. Google code style. Clean code is google code.
        }
        super.replace(fb, offs, length, str, a);
    }

}
