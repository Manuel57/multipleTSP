package mipl;

import java.util.Scanner;

/**
 * Main class
 *
 * @author Manuel Lackenbucher
 **/
public class Main {

    public static void main(String[] args) {
        Utility.initLog("log.log");
        try {
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Utility.process("input.txt");
                        //Utility.executeMilp(60, 0, 3, 2, MilpMethod.MINIMIZE_TOTAL_PATH_LENGTH, "text.js");
                        //calculateTours();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t1.start();
            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    Scanner scanner = new Scanner(System.in);
                    while (t1.isAlive()) {
                        int in = scanner.nextInt();
                        if (in == 0) {
                            Utility.stopCurrent();
                        }
                    }
                }
            });
            t2.start();
            // Utility.executeMilp(6, 0, 3, 2, MilpMethod.MINIMIZE_TOTAL_PATH_LENGTH, "text.js");

            // calculateTours();  //  berechnen

            //Utility.convertResultFilesToCSV("results3.csv");  // dateien in tabelle zusammenfassen

            // Utility.convertResultFilesToLatex("resultsTexNew.txt");
            //Utility.convertJsToLatex("newone.txt"); // in latex zeichnen


            // !!!!!! NACHFOLGENDES EINFACH IGNORIEREN !!!!!!!!


            //2x4;4;0;1;[[0,3,0],[0,5,4,0],[0,7,0],[0,2,6,1,0]]

            //3x4|3|4|0|[4,0,1,5,4],[4,8,4],[4,6,2,3,7,11,10,9,4]

            //grid size	number of drones	base station	method	tours


            //nPoints,nDrones,base,width,method . . .

            /*
            PrintWriter pw = new PrintWriter("next.txt");

            try (BufferedReader br = new BufferedReader(new FileReader("resultGood2.txt"))) {
                String line = "";
                br.readLine();
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.contains("x")) {
                        String vals[] = line.split("\\|");
                        int nPoints = Integer.parseInt(vals[0].split("x")[0]) * Integer.parseInt(vals[0].split("x")[1]);
                        int nDrones = Integer.parseInt(vals[1]);
                        int base = Integer.parseInt(vals[2]);
                        int width = Integer.parseInt(vals[0].split("x")[1]);
                        int method = Integer.parseInt(vals[3]);
                        if (method == 1)
                            pw.println(String.format("%d,%d,%d,%d,%d", nPoints, nDrones, base, width, 0));
                    }
                }
            }

            pw.close();
*/
/*
            PrintWriter pw = new PrintWriter("resultGood2.txt");
            try (BufferedReader br = new BufferedReader(new FileReader("results.csv"))) {
                String line = "";
                br.readLine();
                while ((line = br.readLine()) != null) {
                    String l = line.split(";")[4];
                    int base = Integer.parseInt(line.split(";")[2]);
                    // l = l.subSequence(1,l.length()-1).toString();
                    String[] tours = l.split("\\[");
                    String result = "";
                    //  String json = result.split("=")[1];
                    org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
                    JSONArray array = (JSONArray) ((JSONArray) parser.parse(l));
                    JSONArray newArray = new JSONArray();
                    System.out.println(array);
                    for (Object obj : array) {

                        JSONArray arr = (JSONArray) obj;

                        ArrayList<Integer> list = new ArrayList<>();
                        if (Integer.parseInt(arr.get(0).toString()) != base) {
                            int cnt = -1;
                            for (int i = 0; i < arr.size() && cnt == -1; i++) {
                                if (Integer.parseInt(arr.get(i).toString()) == base) {
                                    cnt = i;
                                    break;
                                }
                                list.add(Integer.parseInt(arr.get(i).toString()));
                            }
                            ArrayList<Integer> n = new ArrayList();
                            for (int i = cnt; i < arr.size(); i++) {
                                n.add(Integer.parseInt(arr.get(i).toString()));
                            }
                            list.remove(0);
                            n.addAll(list);
                            n.add(base);

                            newArray.add(n);
                        } else {
                            list.addAll(arr);
                            newArray.add(list);

                        }
                    }

                    line = line.substring(0, line.indexOf("[")).replace(";", "|") + newArray.toJSONString().replace("[[", "[").replace("]]", "]");
                    pw.println(line);
                }
            }
            pw.close();
*/
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
