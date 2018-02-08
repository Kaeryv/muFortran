package quadrasoft.mufortran.general;

import quadrasoft.mufortran.resources.Strings;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Project {
    private String name;
    private String path;
    private String filename;
    private String buildPath;
    private String compilerPath;
    private String argument;
    private String executionPath;
    private String executableName;
    private String compilerOption;
    private String Author;
    private Date lastEdit;
    private boolean printLog = false;
    private List<String> source;
    private List<String> externals = new ArrayList<String>();

    private boolean selected = false;

    public Project(String arg0) {

        if (arg0.contains("\\")) {

            setFilename(arg0.replaceAll("\\\\", "/"));
        } else {
            setFilename(arg0);
        }
        setName(filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf("."))); // just
        // the
        // name
        // without
        // extension
        setPath(filename.substring(0, filename.lastIndexOf("/") + 1)); // Path
        // is
        // like
        // ..../abcd/
        if (new File(filename).exists()) {

            source = new ArrayList<String>();
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(filename));
                String line;
                this.setArgument("");
                this.setBuildPath("bin/");
                this.setAuthor(System.getProperty("user.name"));
                this.setCompilerPath("gfortran");
                this.setCompilerOption("-o");
                this.setExecutableName(getName());
                printLog = false;
                while ((line = br.readLine()) != null) {
                    if (line.contains("\\")) {
                        line = line.replaceAll("\\\\", "/");
                    }
                    if (line.lastIndexOf("::") != -1) {
                        if (line.substring(0, line.lastIndexOf("::")).replaceAll("\\s+", "")
                                .equalsIgnoreCase("source")) {
                            source.add(line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")));
                        } else if (line.substring(0, line.lastIndexOf("::")).replaceAll("\\s+", "")
                                .equalsIgnoreCase("compiler")) {
                            setCompilerPath(line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")));
                        } else if (line.substring(0, line.lastIndexOf("::")).replaceAll("\\s+", "")
                                .equalsIgnoreCase("argument")) {
                            setArgument(line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")));
                        } else if (line.substring(0, line.lastIndexOf("::")).replaceAll("\\s+", "")
                                .equalsIgnoreCase("buildPath")) {
                            setBuildPath(line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")));
                        } else if (line.substring(0, line.lastIndexOf("::")).replaceAll("\\s+", "")
                                .equalsIgnoreCase("exeName")) {
                            setExecutableName(line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")));
                        } else if (line.substring(0, line.lastIndexOf("::")).replaceAll("\\s+", "")
                                .equalsIgnoreCase("compOptn")) {
                            this.setCompilerOption(line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")));
                        } else if (line.substring(0, line.lastIndexOf("::")).replaceAll("\\s+", "")
                                .equalsIgnoreCase("author")) {
                            this.setAuthor(line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")));
                        } else if (line.substring(0, line.lastIndexOf("::")).replaceAll("\\s+", "")
                                .equalsIgnoreCase("executionPath")) {
                            this.setExecutionPath(line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")));
                        } else if (line.substring(0, line.lastIndexOf("::")).replaceAll("\\s+", "")
                                .equalsIgnoreCase("external")) {
                            externals.add(line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")));
                        } else if (line.substring(0, line.lastIndexOf("::")).replaceAll("\\s+", "")
                                .equalsIgnoreCase("printlog")) {
                            if (line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\"")).equals("true")) {
                                setPrintLog(true);
                            } else {
                                setPrintLog(false);
                            }
                        }
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (Session.getActiveProject() != null)
                Session.getActiveProject().setSelected(false);
            this.setSelected(true);
        } else {
            Log.send("Project file not available.");
        }
    }

    public Project(String arg0, String arg1, String arg2, String arg3) {
        source = new ArrayList<String>();
        source.add("main.f90");
        setName(arg0);
        if (arg1.contains("\\")) {
            arg1 = arg1.replaceAll("\\\\", "/") + "/";
        }
        setPath(arg1 + name + "/");
        printLog = false;
        setFilename(path + name + Strings.s("app:projectextension"));
        setBuildPath(arg2);
        this.setArgument("");
        setCompilerPath(arg3);
        this.setCompilerOption("-o");
        this.setExecutionPath("/");
        this.setAuthor(System.getProperty("user.name"));
        this.setExecutableName(getName());
        if (Session.getActiveProject() != null)
            Session.getActiveProject().setSelected(false);
        this.setSelected(true);
    }

    public Project(String arg0, String arg1, String arg2, String arg3, List<String> arg4) {
        setName(arg0);
        if (arg1.contains("\\")) {
            arg1 = arg1.replaceAll("\\\\", "/") + "/";
        }
        setPath(arg1 + name + "/");
        setFilename(path + name + ".�prj");
        setBuildPath(arg2);
        setCompilerPath(arg3);
        this.setArgument("");
        this.setCompilerOption("-o");
        this.setAuthor(System.getProperty("user.name"));
        this.setExecutableName(getName());
        this.setExecutionPath("/bin/");
        printLog = false;
        source = arg4;
        if (Session.getActiveProject() != null)
            Session.getActiveProject().setSelected(false);
        this.setSelected(true);

    }

    public void createStartFile() throws IOException {
        File file = new File(path + "main.f90");
        if (!file.exists()) {
            new File(path).mkdirs();
            file.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("program main");
        fileWriter.write(System.getProperty("line.separator"));
        fileWriter.write("implicit none");
        fileWriter.write(System.getProperty("line.separator"));
        fileWriter.write("print *,\"hello world !\"");
        fileWriter.write(System.getProperty("line.separator"));
        fileWriter.write("end program");

        fileWriter.close();
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getBuildPath() {
        return buildPath;
    }

    public void setBuildPath(String buildPath) {
        this.buildPath = buildPath;
    }

    public String getCompilerOption() {
        return compilerOption;
    }

    public void setCompilerOption(String compilerOption) {
        this.compilerOption = compilerOption;
    }

    public String getCompilerPath() {
        return compilerPath;
    }

    public void setCompilerPath(String compilerPath) {
        this.compilerPath = compilerPath;
    }

    public String getExecutableName() {
        return executableName;
    }

    public void setExecutableName(String executableName) {
        this.executableName = executableName;
    }

    public String getExecutionPath() {
        return executionPath;
    }

    public void setExecutionPath(String executionPath) {
        this.executionPath = executionPath;
    }

    public List<String> getExternals() {
        return externals;
    }

    public void setExternals(List<String> externals) {
        this.externals = externals;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Date getLastEdit() {
        return lastEdit;
    }

    public void setLastEdit(Date lastEdit) {
        this.lastEdit = lastEdit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjectPath() {
        return "obj/";
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getSource() {
        if (source.contains("changelog.txt"))
            source.remove("changelog.txt");
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }

    public boolean isPrintLog() {
        return printLog;
    }

    public void setPrintLog(boolean printLog) {
        this.printLog = printLog;
    }

    public boolean isSelected() {
        // TODO Auto-generated method stub
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void save() throws IOException {

        File file = new File(filename);
        if (!file.exists()) {
            new File(path).mkdirs();
            new File(path + "changelog.txt").createNewFile();
            file.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("compiler :: \"" + compilerPath + "\"");
        fileWriter.write(System.getProperty("line.separator"));
        fileWriter.write("buildPath :: \"" + buildPath + "\"");
        fileWriter.write(System.getProperty("line.separator"));
        fileWriter.write("argument :: \"" + argument + "\"");
        fileWriter.write(System.getProperty("line.separator"));
        for (String src : source) {
            fileWriter.write("source :: \"" + src + "\"");
            fileWriter.write(System.getProperty("line.separator"));
        }
        for (String ext : externals) {
            fileWriter.write("external :: \"" + ext + "\"");
            fileWriter.write(System.getProperty("line.separator"));
        }
        fileWriter.write("author :: \"" + Author + "\"");
        fileWriter.write(System.getProperty("line.separator"));
        fileWriter.write("exeName :: \"" + executableName + "\"");
        fileWriter.write(System.getProperty("line.separator"));
        fileWriter.write("compOptn :: \"" + compilerOption + "\"");
        fileWriter.write(System.getProperty("line.separator"));
        fileWriter.write("executionPath :: \"" + executionPath + "\"");
        fileWriter.write(System.getProperty("line.separator"));
        if (printLog) {
            fileWriter.write("printLog :: \"" + "true" + "\"");
        } else {
            fileWriter.write("printLog :: \"" + "false" + "\"");
        }
        fileWriter.write(System.getProperty("line.separator"));
        fileWriter.close();
    }
}
