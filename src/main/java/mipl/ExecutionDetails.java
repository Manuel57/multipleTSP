package mipl;


public class ExecutionDetails {
    private int base;
    private int nDrones;
    private int nPoints;
    private int method;
    private int width;

    public ExecutionDetails(int base,int nDrones,int nPoints,int method,int width) {
      this.base = base;
      this.nDrones = nDrones;
      this.nPoints = nPoints;
      this.method = method; 
      this.width = width;
    }
    
    public int getMethod() {
      return this.method;
     }
    public int getNubferOfNodes() {
      return this.nPoints;
    }
    public int getBase() {
     return this.base;
    }
    public int getWidth() {
     return this.width;
    }
    public int getNubferOfDrones() {
     return this.nDrones;
    }
    public String generateJsFileName() {
        MilpMethod method = Utility.getMethod(this.method);
      return "js_" + this.nDrones + "_" + this.nPoints + "_" + this.width + "_" + this.base + "_" + method + ".js";
    }
    

}
