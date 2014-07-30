/**
 * 
 */
package farom.astroidserver;

import farom.astroiddriver.jssc.INDIAstroidDriverJSSC;
import laazotea.indi.INDIException;
import laazotea.indi.server.DefaultINDIServer;


/**
 * @author farom
 *
 */
public class AstroidServerPC extends DefaultINDIServer {
	
    public AstroidServerPC(){
    	super();
    	try {
            loadJavaDriver(INDIAstroidDriverJSSC.class);
          } catch (INDIException e) {
            e.printStackTrace();

            System.exit(-1);
          }
    }

	  /**
	   * Just creates one instance of this server.
	   * @param args 
	   */
	  public static void main(String[] args) {
	    @SuppressWarnings("unused")
		AstroidServerPC s = new AstroidServerPC();  
	  }

}
