package mipl;

import gurobi.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

interface Func<E extends Integer, T> {
    T execute(E i, E j, E k);
}

/**
 * @author Manuel Lackenbucher, Lisa Knoblinger, Stefan Jessenitschnig
 */
public class ILP {


    /**
     * default timeout of 5 hours
     */
    private final static int DEFAULT_TIMEOUT = 60 * 60 * 5;
    /**
     * the Gurobi environment variable
     */
    private GRBEnv env;
    /**
     * the Gurobi optimization model
     */
    private GRBModel model;
    /**
     * the number of grid points
     */
    private int nGridPoints;
    /**
     * array including all grid points and array including all grid points except the base
     */
    private ArrayList<Integer> gridPoints, gridWithoutBase;
    /**
     * the number of drones
     */
    private int nDrones;
    /**
     * the base
     */
    private int base;
    /**
     * the euclidean distance from grid point i to grid point j
     */
    private double[][] t_ij;
    /**
     * the maximum number of grid points each drone can visit
     */
    private int maxNumOfGP;

    /**
     * Gurobi variable for macimum path length of a drone
     */
    private GRBVar c = null;


    /**
     * thee dimensional array of Gurobi variables
     * <p>
     * 1 - if drone k visits grid point j immediately after grid point i
     * 0 - otherwise
     */
    private GRBVar[][][] x_ijk = null;
    /**
     * used for mtz subtour elimination constraint and represents an enumeration of the grid points visited
     */
    private GRBVar[] ui = null;
    /**
     * thee dimensional array of int variables
     * <p>
     * 1 - if drone k visits grid point j immediately after grid point i
     * 0 - otherwise
     */
    private int[][][] result;
    /**
     * list of grid points each drone has visited
     */
    private List<List<Integer>> tours;

    /**
     * filename where the result will be written to
     */
    private String resultFilename = "result.log";


    private MilpMethod method;


    /**
     * constructor
     *
     * @param nDrones    number of drones
     * @param startPoint base station
     * @param gridPoints list of grid points without base station
     * @param t_ij       euclidean distances from grid point i to grid point j
     * @param log        filename to log
     * @param mnopg      maximum number of grid points each drone is allowed to visit
     * @throws GRBException
     */
    public ILP(int nDrones, int startPoint, ArrayList<Integer> gridPoints, double[][] t_ij, String log, int mnopg, String resultFilename, MilpMethod method) throws GRBException {
        this.env = new GRBEnv(log);
        this.env.set(GRB.DoubleParam.TimeLimit, DEFAULT_TIMEOUT);
        // env.set(GRB.IntParam.NormAdjust, 2);
        // env.set(GRB.IntParam.Method, 1);
        // env.set(GRB.IntParam.Sifting, 2);
        // env.set(GRB.IntParam.Threads, 2);
        // this.env.set(GRB.DoubleParam.MIPGap,2);
        this.model = new GRBModel(this.env);
        this.nDrones = nDrones;
        this.nGridPoints = gridPoints.size() + 1;
        this.gridWithoutBase = gridPoints;
        this.gridPoints = new ArrayList<>();
        this.gridPoints.addAll(this.gridWithoutBase);
        this.gridPoints.add(startPoint);
        this.base = startPoint;
        this.method = method;

        this.t_ij = t_ij;
        this.maxNumOfGP = mnopg;
        this.resultFilename = resultFilename;
    }

    /**
     * solev the ILP
     *
     * @throws GRBException if error occurs during optimization
     */
    public void solveILP() throws GRBException {
        this.initVariables();
        switch (this.method) {
            case MINIMIZE_TOTAL_PATH_LENGTH:
                this.setObjective();
                this.addMaxVisitsConstraint();
                break;
            case MINIMIZE_MAXIMUM_PATH_LENGTH:
                this.setObjective2();
                this.addPathLengthConstraint();
                break;
            case MINIMIZE_MAXIMUMTOTAL_PATH_LENGTH:
                this.setObjective3();
                this.addPathLengthConstraint();
                break;
        }


        this.addIngoingConstraint();
        this.addOutgoingConstraint();
        this.addInOutEqualityConstraing();

        this.addStartConstraint();
        this.addEndConstraint();

        this.addSubtourConstraints();
        this.addSubtourEliminationConstraint();
        model.update();


        this.model.optimize();
        this.model.write(this.resultFilename);
    }

    private void addPathLengthConstraint() throws GRBException {
        GRBLinExpr exp;
        for (int k = 0; k < nDrones; k++) {
            exp = new GRBLinExpr();
            for (int i = 0; i < nGridPoints; i++) {
                for (int j = 0; j < nGridPoints; j++) {
                    if (i != j) {
                        exp.addTerm(t_ij[i][j], x_ijk[i][j][k]);
                    }
                }
            }
            this.model.addConstr(exp, GRB.LESS_EQUAL, c, "max_" + k);
        }


    }

    private void addEndConstraint() throws GRBException {
        GRBLinExpr expr;
        for (int k = 0; k < nDrones; k++) {
            expr = new GRBLinExpr();
            for (Integer i : this.gridWithoutBase)
                expr.addTerm(1, this.x_ijk[i][this.base][k]);

            model.addConstr(expr, GRB.EQUAL, 1, "endDepot" + k);
        }
    }

    private void setObjective2() throws GRBException {
        GRBLinExpr exp = new GRBLinExpr();
        exp.addTerm(1, c);
        this.model.setObjective(exp, GRB.MINIMIZE);

    }

    /**
     * disposes the model and the environment
     *
     * @throws GRBException
     */
    public void dispose() throws GRBException {
        this.model.dispose();
        this.env.dispose();
    }

    /**
     * initializes all variables
     *
     * @throws GRBException
     */
    private void initVariables() throws GRBException {
        this.x_ijk = new GRBVar[this.nGridPoints][this.nGridPoints][this.nDrones];
        for (int k = 0; k < nDrones; k++) {
            for (int i : this.gridPoints) {
                for (int j : this.gridPoints) {
                    if (i != j) {
                        this.x_ijk[i][j][k] = model.addVar(0, 1, 0, GRB.BINARY, "x_" + i + "_" + j + "_" + k);
                    }
                }
            }
        }
        this.c = model.addVar(0, Double.POSITIVE_INFINITY, 0, GRB.CONTINUOUS, "c");
        this.ui = new GRBVar[this.nGridPoints];
        this.model.update();
        for (int i = 0; i < this.nGridPoints; i++) {
            this.ui[i] = model.addVar(2, this.nGridPoints + 1, 0, GRB.INTEGER, "u_" + i);
        }
        this.model.update();

    }

    /**
     * sets the objective
     * objective - minimizing the total path length
     *
     * @throws GRBException
     */
    private void setObjective() throws GRBException {
        GRBLinExpr exp = new GRBLinExpr();
        for (int k = 0; k < this.nDrones; k++) {
            for (int i : gridPoints) {
                for (int j : gridPoints) {
                    if (i != j) {
                        exp.addTerm(this.t_ij[i][j], this.x_ijk[i][j][k]);
                    }
                }
            }
        }
        this.model.setObjective(exp, GRB.MINIMIZE);
    }

    private void setObjective3() throws GRBException {
        GRBLinExpr exp = new GRBLinExpr();
        exp.addTerm(10, c);
        for (int k = 0; k < this.nDrones; k++) {
            for (int i : gridPoints) {
                for (int j : gridPoints) {
                    if (i != j) {
                        exp.addTerm(this.t_ij[i][j], this.x_ijk[i][j][k]);
                    }
                }
            }
        }
        this.model.setObjective(exp, GRB.MINIMIZE);

    }

    private void addSubtourConstraints() throws GRBException {

        GRBLinExpr[] expr2 = new GRBLinExpr[nDrones];
        GRBLinExpr[] expr3 = new GRBLinExpr[nDrones];
        int i, j, l;

        for (int ci = 0; ci < this.nGridPoints - 1; ci++) {
            i = gridWithoutBase.get(ci);

            for (int cj = ci + 1; cj < this.nGridPoints - 1; cj++) {
                j = gridWithoutBase.get(cj);
                for (int k = 0; k < nDrones; k++) {
                    expr2[k] = new GRBLinExpr();
                    expr2[k].addTerm(1, x_ijk[i][j][k]);
                    expr2[k].addTerm(1, x_ijk[j][i][k]);
                    model.addConstr(expr2[k], GRB.LESS_EQUAL, 1, "subl2_" + i + "_" + j + "_" + k);
                }

                for (int cl = cj + 1; cl < this.nGridPoints - 1; cl++) {
                    l = gridWithoutBase.get(cl);
                    for (int k = 0; k < nDrones; k++) {
                        expr3[k] = new GRBLinExpr();
                        expr3[k].add(expr2[k]);
                        expr3[k].addTerm(1, x_ijk[i][l][k]);
                        expr3[k].addTerm(1, x_ijk[l][i][k]);
                        expr3[k].addTerm(1, x_ijk[j][l][k]);
                        expr3[k].addTerm(1, x_ijk[l][j][k]);
                        model.addConstr(expr3[k], GRB.LESS_EQUAL, 2, "subl3_" + i + "_" + j + "_" + l + "_" + k);
                    }

                }
            }

        }
    }


    /**
     * add the constraint that each grid point has exactly one ingoing and one outgoing edge
     *
     * @param f         the function which returns either the ingoing or the outgoing edge
     * @param direction name of the constraint - ingoing or outgoing
     * @throws GRBException
     */
    private void addInOutConstraint(Func<Integer, GRBVar> f, String direction) throws GRBException {
        GRBLinExpr exp;

        for (int i : this.gridWithoutBase) {
            exp = new GRBLinExpr();
            for (int k = 0; k < this.nDrones; k++) {
                for (int j : gridPoints) {
                    if (i != j) {
                        exp.addTerm(1, f.execute(i, j, k));
                    }
                }
            }
            this.model.addConstr(exp, GRB.EQUAL, 1, direction + "_" + i);
        }

    }

    /**
     * adds outgoing edges constraing
     *
     * @throws GRBException
     */
    private void addOutgoingConstraint() throws GRBException {
        this.addInOutConstraint((i, j, k) -> x_ijk[i][j][k], "outgoing");
    }

    /**
     * adds ingoing edges constraint
     *
     * @throws GRBException
     */
    private void addIngoingConstraint() throws GRBException {
        this.addInOutConstraint((i, j, k) -> x_ijk[j][i][k], "ingoing");
    }

    /**
     * adds constraint - each drone has to start at the base station
     *
     * @throws GRBException
     */
    private void addStartConstraint() throws GRBException {
        GRBLinExpr exp;
        for (int k = 0; k < this.nDrones; k++) {
            exp = new GRBLinExpr();
            for (int i : this.gridWithoutBase) {
                exp.addTerm(1, this.x_ijk[this.base][i][k]);
            }
            this.model.addConstr(exp, GRB.EQUAL, 1, "startdepod_" + k);

        }
    }

    /**
     * adds constraint - each drone has to get back to the base station at the end
     *
     * @throws GRBException
     */
/*    private void addEndDepotConstraint() throws GRBException {
        GRBLinExpr expr;
        for (int k = 0; k < nDrones; k++) {
            expr = new GRBLinExpr();
            for (Integer i : this.gridWithoutBase)
                expr.addTerm(1, this.x_ijk[i][this.base][k]);
            model.addConstr(expr, GRB.EQUAL, 1, "endDepot" + k);
        }
    }
*/

    /**
     * adds constraint - each grid point is entered and left by the same drone
     *
     * @throws GRBException
     */
    private void addInOutEqualityConstraing() throws GRBException {
        GRBLinExpr exp;
        for (int k = 0; k < this.nDrones; k++) {
            for (int i = 0; i < this.nGridPoints; i++) {
                exp = new GRBLinExpr();
                for (int j : gridPoints) {
                    if (i != j) {
                        exp.addTerm(1, this.x_ijk[j][i][k]);
                    }
                }
                for (int j : gridPoints) {
                    if (i != j) {
                        exp.addTerm(-1, this.x_ijk[i][j][k]);
                    }
                }
                model.addConstr(exp, GRB.EQUAL, 0, "drone_" + k + "_at_" + i);
            }
        }
    }

    /**
     * adds constraint - each drone visits not more than manNumOfGP grid point
     *
     * @throws GRBException
     */
    private void addMaxVisitsConstraint() throws GRBException {
        GRBLinExpr exp;
        for (int k = 0; k < this.nDrones; k++) {
            exp = new GRBLinExpr();
            for (int i : this.gridWithoutBase) {
                for (int j : this.gridWithoutBase) {
                    if (i != j)
                        exp.addTerm(1, this.x_ijk[i][j][k]);
                }
            }
            this.model.addConstr(exp, GRB.LESS_EQUAL, this.maxNumOfGP - 1, "maxNumberVisitations_" + k);
        }
    }

    /**
     * polinomial many subtour elimination constraints by MTZ
     *
     * @throws GRBException
     */
    private void addSubtourEliminationConstraint() throws GRBException {
        GRBLinExpr lh;
        GRBLinExpr rh;

        for (Integer i : gridWithoutBase) {
            for (Integer j : gridWithoutBase) {
                if (!i.equals(j)) {
                    lh = new GRBLinExpr();
                    lh.addTerm(1, ui[i]);
                    lh.addTerm(-1, ui[j]);
                    lh.addConstant(1);
                    rh = new GRBLinExpr();
                    rh.addConstant(nGridPoints);
                    for (int k = 0; k < nDrones; k++) {
                        rh.addTerm(-nGridPoints, x_ijk[i][j][k]);
                    }
                    model.addConstr(lh, GRB.LESS_EQUAL, rh, "constraintMTZ" + i + "_" + j);
                }
            }
        }
    }

    /**
     * prints the result to the console
     */
    public void printResult() {
        for (int k = 0; k < nDrones; k++) {
            System.out.format("%n Tour: %-2d -------------- %n", k);
            for (int i = 0; i < nGridPoints; i++) {
                for (int j = 0; j < nGridPoints; j++) {
                    if (result[i][j][k] > 0)
                        System.out.println(i + "_" + j + "_" + k + " --- " + this.t_ij[i][j]);
                }
                System.out.format("%n");
            }
        }
    }

    public void stop() {
        if (this.model != null)
            this.model.terminate();
    }

    class Point {
        public int j;
        public int i;

        Point(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }


    public void printResult(int width) {


        ArrayList<ArrayList<Point>> t = new ArrayList<>();

        for (int k = 0; k < nDrones; k++) {
            t.add(new ArrayList<>());
            for (int i = 0; i < nGridPoints; i++) {
                for (int j = 0; j < nGridPoints; j++) {
                    if (this.result[i][j][k] >= 1) {
                        t.get(k).add(new Point(i, j));
                    }
                }
            }
        }
        ArrayList<ArrayList<Point>> tour = new ArrayList<>();

        for (int k = 0; k < nDrones; k++) {
            ArrayList<Point> l = t.get(k);
            ArrayList<Point> tt = new ArrayList<>();

            Point m = (Point) l.stream().filter((el) -> el.i == this.base).toArray()[0];
            tt.add(m);

            while (!l.isEmpty()) {
                int i = 0;
                Point n = l.get(0);
                while (i < l.size() && n.i != m.j) {
                    n = l.get(i);
                    i++;
                }
                tt.add(n);
                l.remove(n);
                m = n;
            }
            tour.add(tt);
        }

        this.tours = new ArrayList<>();
        String s = "";
        for (int k = 0; k < nDrones; k++) {
            ArrayList<Integer> al = new ArrayList<>();
            s += String.format("Tour %d%n---------------------%n", k);
            System.out.format("Tour %d%n---------------------%n", k);
            for (Point p : tour.get(k)) {
                al.add(p.i);
                System.out.format("From %d to %d%n", p.i, p.j);
                s += String.format("From %d to %d%n", p.i, p.j);
            }
            this.tours.add(al);
            System.out.println();
            s += String.format("%n \n");
        }

        try (FileWriter file = new FileWriter("result_" + this.nDrones + "_" + this.nGridPoints + "_" + width + "_" + this.base + "_" + this.method + ".txt")) {
            file.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * writes the result to a js file so that it can be used wor drawing the tours
     *
     * @param width    the width of the grid
     * @param filename the filename
     */
    public void exportResult(int width, String filename) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("width", width);
            obj.put("numberOfPoints", this.nGridPoints);

            JSONArray arr = new JSONArray();

            JSONArray company;
            int i = 0;
            for (List<Integer> l : this.tours) {
                company = new JSONArray();
                company.addAll(l);
                arr.add(company);
                i++;
            }
            obj.put("Tours", arr);

            JSONObject o = new JSONObject();
            o.put("Tours", obj);

            try (FileWriter file = new FileWriter(filename)) {
                file.write("var tours=");
                file.write(o.toJSONString());
                System.out.println("Successfully Copied JSON Object to File...");
                System.out.println("\nJSON Object: " + o);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * sets the result
     *
     * @throws GRBException
     */
    public void setResult() throws GRBException {
        result = new int[nGridPoints][nGridPoints][nDrones];
        this.model.update();
        for (int k = 0; k < nDrones; k++) {
            for (Integer i : gridPoints) {
                for (Integer j : gridPoints) {
                    if (!i.equals(j)) {
                        if (x_ijk[i][j][k].get(GRB.DoubleAttr.X) > 0.8)
                            result[i][j][k] = 1;
                    }
                }
            }
        }
    }
}
