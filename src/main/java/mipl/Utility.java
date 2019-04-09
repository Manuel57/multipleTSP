package mipl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Utility {

    private static Logger LOG = Logger.getLogger("Utility");
    private static ILP CURRENT_ILP = null;

    private static JSONObject readTours(File file) throws IOException, ParseException {
        JSONObject o = null;


        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String result = "";
            String l = "";
            while ((l = br.readLine()) != null) {
                result += " " + l;
            }

            String json = result.split("=")[1];
            org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
            o = (JSONObject) ((JSONObject) parser.parse(json)).get("Tours");
        }

        return o;
    }

    private static File[] getFilesSorted(File dir) {
        File[] files = dir.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.getName().split("_").length < 8)
                    return 1;
                String sp1[] = o1.getName().split("_");
                String sp2[] = o2.getName().split("_");
                int r = 0;
                r = Integer.parseInt(sp1[2]) - Integer.parseInt(sp2[2]);
                if (r == 0) {
                    r = (Integer.parseInt(sp1[2]) / Integer.parseInt(sp1[3])) - (Integer.parseInt(sp2[2]) / Integer.parseInt(sp2[3]));
                    if (r == 0) {
                        r = Integer.parseInt(sp1[3]) - Integer.parseInt(sp2[3]);
                        if (r == 0) {
                            r = Integer.parseInt(sp1[1]) - Integer.parseInt(sp2[1]);
                            if (r == 0) {
                                r = sp1[6].compareTo(sp1[6]);
                                if (r == 0) {
                                    r = Integer.parseInt(sp1[4]) - Integer.parseInt(sp2[4]);
                                }
                            }
                        }
                    }
                }
                return r;
            }
        });
        return files;
    }


    public static void convertResultFilesToCSV(String fileName) throws IOException, ParseException {
        File dir = new File("./files");
        PrintWriter bw = new PrintWriter(new FileWriter("results.csv"));
        bw.println("grid;nDrones;base:method;tours");


        File[] files = getFilesSorted(dir);

        for (File f : files) {
            if (f.getName().endsWith(".js") && !f.getName().equals("values.js")) {
                JSONObject o = readTours(f);

                JSONArray tours = ((JSONArray) o.get("Tours"));

                //String m = f.getName().split("_")[6];

                int method = getMethod(f.getName());

                int base = Integer.parseInt(f.getName().split("_")[4]);//((JSONArray) tours.get(0)).get(0).toString());
                int width = Integer.parseInt(o.get("width").toString());
                int nNodes = (Integer.parseInt((String) o.get("numberOfPoints").toString()));

                bw.print((nNodes / width) + "x" + o.get("width"));
                bw.print(";" + tours.size() + ";" + base + ";" + method + ";" + tours.toJSONString());
                bw.println();
            }
        }
        bw.close();

    }

    public static int getMethod(String fName) {
        int method = -1;
        String m = fName.split("_")[6];
        if (m.equalsIgnoreCase("MAXIMUM"))
            method = 1;
        else if (m.equalsIgnoreCase("MAXIMUMTOTAL"))
            method = 2;
        else
            method = 0;
        return method;
    }


    public static void convertResultFilesToLatex(String fileName) throws IOException, ParseException {
        File dir = new File("./files");
        PrintWriter bw = new PrintWriter(new FileWriter(fileName));
        bw.println("\\documentclass{article}\n" +
                "\n" +
                "% set font encoding for PDFLaTeX, XeLaTeX, or LuaTeX\n" +
                "\\usepackage{ifxetex,ifluatex}\n" +
                "\\newif\\ifxetexorluatex\n" +
                "\\ifxetex\n" +
                "  \\xetexorluatextrue\n" +
                "\\else\n" +
                "  \\ifluatex\n" +
                "    \\xetexorluatextrue\n" +
                "  \\else\n" +
                "    \\xetexorluatexfalse\n" +
                "  \\fi\n" +
                "\\fi\n" +
                "\n" +
                "\\ifxetexorluatex\n" +
                "  \\usepackage{fontspec}\n" +
                "\\else\n" +
                "  \\usepackage[T1]{fontenc}\n" +
                "  \\usepackage[utf8]{inputenc}\n" +
                "  \\usepackage{lmodern}\n" +
                "\\fi\n" +
                "\n" +
                "\\usepackage{hyperref}\n" +
                "\n" +
                "\n" +
                "% Enable SageTeX to run SageMath code right inside this LaTeX file.\n" +
                "% http://mirrors.ctan.org/macros/latex/contrib/sagetex/sagetexpackage.pdf\n" +
                "% \\usepackage{sagetex}\n" +
                "\n" +
                "\\begin{document}");

        bw.println("\\begin{tabular}{ccccl}\n" +
                " grid size&number of drones&base station&method&tours\\\\\n" +
                " \\hline");


        File[] files = getFilesSorted(dir);
        for (File f : files) {
            if (f.getName().endsWith(".js") && f.getName().startsWith("js_")) {
                JSONObject o = readTours(f);

                JSONArray tours = ((JSONArray) o.get("Tours"));

                //String m = f.getName().split("_")[6];

                int method = getMethod(f.getName());

                int base = Integer.parseInt(f.getName().split("_")[4]);//((JSONArray) tours.get(0)).get(0).toString());
                int width = Integer.parseInt(o.get("width").toString());
                int nNodes = (Integer.parseInt((String) o.get("numberOfPoints").toString()));

                bw.print((nNodes / width) + "x" + o.get("width"));
                bw.print(" & " + tours.size() + " & " + base + " & " + method + " & " + tours.toJSONString());
                bw.println("\\\\");
            }
        }

        bw.println(" \\end{tabular}\n" +
                "\n" +
                "\\end{document}");
        bw.close();
    }


    public static void convertJsToLatex(String filename) throws IOException, ParseException {
        String[] colors = new String[]{"blue", "red", "green", "orange", "yellow"};
        File dir = new File("./files");
        PrintWriter bw = new PrintWriter(new FileWriter(filename));
        bw.println("\\documentclass{article}\n" +
                "\n" +
                "\\usepackage{times}\n" +
                "\\usepackage{ucs}\n" +
                "\\usepackage[utf8x]{inputenc}\n" +
                "\\usepackage[T1]{fontenc}\n" +
                "\\usepackage[german, english]{babel}\n" +
                "\\usepackage[sort,numbers]{natbib}\n" +
                "\\thispagestyle{empty}\n" +
                "\\usepackage{amsmath,amssymb,amsthm}\n" +
                "\\usepackage{comment}\n" +
                "\\usepackage{here}\n" +
                "\\usepackage{tikz}\n" +
                "\\usetikzlibrary{calc}\n" +
                "\\usetikzlibrary{decorations.pathmorphing,patterns}\n" +
                "\\usetikzlibrary{calc,patterns,decorations.markings}\n" +
                "\\usepackage{pdflscape}\n" +
                "\\usepackage{tikzscale}\n" +
                "\\usepackage{morefloats}\n" +
                "\\usepackage{filecontents}\n" +
                "\\setlength{\\bibsep}{0.0pt}\n" +
                "\\usepackage[textsize=tiny]{todonotes}\n" +
                "\n" +
                "\\usepackage{pgf}\n" +
                "\\usepackage{graphics}\n" +
                "\\usepackage[margin=1in]{geometry}\n" +
                "\\maxdeadcycles=1000\n" +
                "\\usetikzlibrary{arrows,automata}\n" +
                " %to enable backgrounds in tikz graphics -> edges do not overlay vertices\n" +
                "\\usetikzlibrary{backgrounds}\n" +
                "\\pgfdeclarelayer{myback}\n" +
                "\\pgfsetlayers{background,myback,main}\n" +
                " %to still have the possibility of exploiting the background layer\n" +
                "\n" +
                "\n" +
                "\\begin{document}");

        int cntFigures = 1;
        for (File f : getFilesSorted(dir)) {
            if (f.getName().endsWith(".js") && !f.getName().equals("values.js")) {
                System.out.println(f.getName());
                JSONObject o = readTours(f);

                JSONArray tours = ((JSONArray) o.get("Tours"));

                //------------------


                //-----------------
                //String m = f.getName().split("_")[6];

                int method = getMethod(f.getName());

                int base = Integer.parseInt(f.getName().split("_")[4]);//((JSONArray) tours.get(0)).get(0).toString());
                int width = Integer.parseInt(o.get("width").toString());
                int nNodes = (Integer.parseInt((String) o.get("numberOfPoints").toString()));

                ArrayList<ArrayList<Integer>> pointsAtHeight = new ArrayList<>();
                bw.println("\\begin{figure}[htp]\n" +
                        "\\begin{center}\n" +
                        "\\resizebox{\\ifdim\\width>\\linewidth.9\\linewidth\\else\\width\\fi}{!}{\n" +
                        "\\begin{tikzpicture}[->,>=stealth',shorten >=1pt,auto,node distance=1.5cm,semithick]\n" +
                        "  \\tikzstyle{every circle}=[fill=white,fill opacity=.85,draw=black,text=black,text opacity=1]");


                bw.println("\\node[circle] (0) {0};");
                pointsAtHeight.add(new ArrayList<Integer>());
                for (int i = 1; i < nNodes; i++) {
                    pointsAtHeight.add(new ArrayList<Integer>());
                    if (i % width == 0) {
                        bw.println("\\node[circle] (" + i + ") [below of=" + (i - width) + "]{" + i + "};");
                    } else
                        bw.println("\\node[circle] (" + i + ") [right of=" + (i - 1) + "]{" + i + "};");
                }

                bw.println("\\begin{pgfonlayer}{myback}");
                int cnt = 0;
                for (Object obj : tours) {
                    JSONArray arr = ((JSONArray) obj);
                    bw.println("\\path[draw=" + colors[cnt] + ",thick,solid]");
                    for (int i = 0; i < arr.size() - 1; i++) {
                        int y = Math.floorDiv((Integer.parseInt(arr.get(i).toString())), width);
                        if (contains(pointsAtHeight.get(y), Integer.parseInt(arr.get(i).toString()), Integer.parseInt(arr.get(i + 1).toString()))) {
                            bw.format("(%d) edge [bend right=20] node {} (%d)%n", arr.get(i), arr.get(i + 1));
                        } else
                            bw.format("(%d) edge node {} (%d)%n", arr.get(i), arr.get(i + 1));
                        pointsAtHeight.get(y).add(Integer.parseInt(arr.get(i).toString()));
                    }
                    bw.println(";");
                    cnt++;
                }
                bw.println("  \\end{pgfonlayer}\\end{tikzpicture}\n" +
                        "}\n");
                bw.format("\\caption[drones503]{drones %d method %d vertices %d base %d }%n\\label{drones %d method %d vertices %d base %d}%n\\end{center}%n\\end{figure}%n%n", tours.size(), method, nNodes, base, tours.size(), method, nNodes, base);

                if (cntFigures % 35 == 0)
                    bw.println("\\clearpage");
                cntFigures++;
            }
        }

        System.out.println(getFilesSorted(dir).length);
        bw.println("\\end{document}");

        bw.close();
    }

    private static void execute(int node, int base, int width, int nDrones, MilpMethod method, String jsFilename) {
        double[][] t_ij = new double[node][node];

        ArrayList<Integer> grid = new ArrayList<>();


        int xDist, yDist;
        for (Integer i = 0; i < node; i++)
            if (i != base)
                grid.add(i);

        for (int i = 0; i < node; i++) {
            for (int j = i + 1; j < node; j++) {
                xDist = j % width - i % width;
                yDist = Math.floorDiv(j, width) - Math.floorDiv(i, width);
                t_ij[i][j] = Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2));
                t_ij[j][i] = t_ij[i][j];
            }
        }

        try {
            System.out.println((int) Math.ceil((double) node / nDrones));
            ILP ilp = new ILP(nDrones, base, grid, t_ij, "ilp.log", (int) Math.ceil((double) node / nDrones), "result.lp", method);
            CURRENT_ILP = ilp;
            ilp.solveILP();
            ilp.setResult();
            ilp.printResult(width);
            ilp.exportResult(width, jsFilename);
            //File htmlFile = new File("html/draw.html");
            //    Desktop.getDesktop().browse(htmlFile.toURI());
            ilp.dispose();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.INFO, jsFilename);

        }
    }

    public static void executeMilp(int node, int base, int width, int nDrones, MilpMethod method) {
        execute(node, base, width, nDrones, method, "js_" + nDrones + "_" + node + "_" + width + "_" + base + "_" + method + ".js");
    }

    public static void executeMilp(int node, int base, int width, int nDrones, MilpMethod method, String jsFilename) {
        execute(node, base, width, nDrones, method, jsFilename);
    }


    private static boolean contains(ArrayList<Integer> points, int point1, int point2) {
       /* for (Integer i : points) {
            if (point1 < i && i < point2)
                return true;
            if (point1 > i && i > point2)
                return true;
            if(point2 == i || point1 == i)
                return true;
        }*/
        return false;
    }

    public static MilpMethod getMethod(int method) {
        MilpMethod m = null;
        switch (method) {
            case 0:
                m = MilpMethod.MINIMIZE_TOTAL_PATH_LENGTH;
                break;
            case 1:
                m = MilpMethod.MINIMIZE_MAXIMUM_PATH_LENGTH;
                break;
            case 2:
                m = MilpMethod.MINIMIZE_MAXIMUMTOTAL_PATH_LENGTH;
                break;

        }
        return m;
        //(this.method == 1 ? MilpMethod.MINIMIZE_MAXIMUM_PATH_LENGTH : method==2 ? MilpMethod.MINIMIZE_MAXIMUMTOTAL_PATH_LENGTH : MilpMethod.MINIMIZE_TOTAL_PATH_LENGTH);
    }

    public static void process(String inputFilename) throws Exception {
        ArrayList<ExecutionDetails> executions = readInputFile(inputFilename);
        MilpMethod m;
        for (ExecutionDetails ed : executions) {
            m = getMethod(ed.getMethod());
            executeMilp(ed.getNubferOfNodes(), ed.getBase(), ed.getWidth(), ed.getNubferOfDrones(), m, ed.generateJsFileName());
        }
    }


    public static ArrayList<ExecutionDetails> readInputFile(String inputFilename) throws Exception {
        ArrayList<ExecutionDetails> executions = new ArrayList<ExecutionDetails>();
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilename))) {
            String currentLine;

            int base;
            int nDrones;
            int nPoints;
            int method;
            int width;

            while ((currentLine = br.readLine()) != null) {
                String[] in = currentLine.split(",");
                nPoints = Integer.parseInt(in[0]);
                nDrones = Integer.parseInt(in[1]);
                base = Integer.parseInt(in[2]);
                width = Integer.parseInt(in[3]);
                method = Integer.parseInt(in[4]);
                executions.add(new ExecutionDetails(base, nDrones, nPoints, method, width));
            }
        }
        return executions;
    }

    public static void initLog(String s) {
        FileHandler fh;

        try {

            // This block configure the logger with handler and formatter
            fh = new FileHandler(s);
            LOG.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopCurrent() {
        if (CURRENT_ILP != null)
            CURRENT_ILP.stop();
    }
}
