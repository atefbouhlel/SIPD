package test.jdbc.BACKUP;

/**
 * Tests for Backup server
 *
 * @author Matthieu Finiasz
 * @date 2015/05
 */

import java.io.PrintWriter;

import org.inria.jdbc.Backup;

import test.jdbc.Tools;
import test.runner.ITest;

public class Test extends Tools implements ITest {
    
    @Override
    public void run(PrintWriter out, String dbmsHost) throws Exception {
	int ret;
	Backup b = new Backup("http://kisslocalhost");
	ret = b.emptyDB();
	if (ret != 0) {
	    out.println("Empty DB error, return value: " + ret);
	}
	ret = b.register();
	if (ret != 0) {
	    out.println("Register error, return value: " + ret);
	}
	ret = b.authenticate();
	if (ret != 0) {
	    out.println("Authenticate error, return value: " + ret);
	}
    }
}

