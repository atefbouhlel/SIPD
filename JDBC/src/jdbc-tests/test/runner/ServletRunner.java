package test.runner;

import org.inria.jdbc.Util;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.jdbc.schemaIndexInfo.Data;

/**
 * A simple servlet to run the JDBC tests
 */
public class ServletRunner extends HttpServlet {

  public void init(ServletConfig servletconfig) throws ServletException {
    super.init(servletconfig);
  }

  /**
   * Servlet {@code javax.servlet.http.HttpServlet#doGet} dispatcher.
   */
  public void doGet(HttpServletRequest req, HttpServletResponse res)
  throws ServletException, IOException {
    String testname = req.getParameter("test"); // the requested test
    String dbmsHost = req.getParameter("dbmshost"); // the target DBMS host
    String part = req.getParameter("part");
    res.setContentType("text/plain"); // raw unformatted text
    if (part == null) {
      ITest.Runner.runTest(testname, res.getWriter(), dbmsHost);
    }else{
      short partOfTheCode = Short.parseShort(part);
      ITestTearing.Runner.runTest(testname, res.getWriter(), dbmsHost, partOfTheCode);
    }
  } /**/

  public static void main(String[] args) {
    Util.setTEPO(Data.Comment.names);
    Util.setTEPO(Data.Concept.names);
    Util.setTEPO(Data.Concept.dataTypes);
    Util.setTEPO(Data.Concept.units);
    Util.setTEPO(Data.Episode.names);
    Util.setTEPO(Data.Event.days);
    Util.setTEPO(Data.Event.months);
    Util.setTEPO(Data.Event.years);
    Util.setTEPO(Data.Formulaire.names);
    Util.setTEPO(Data.Info.positions);
    Util.setTEPO(Data.Info.valChars);
    Util.setTEPO(Data.Info.valNums);
    Util.setTEPO(Data.MatricePatient.autorisations);
    Util.setTEPO(Data.Role.names);
    Util.setTEPO(Data.UserDMSP.adress);
    Util.setTEPO(Data.UserDMSP.firstNames);
    Util.setTEPO(Data.UserDMSP.lastNames);
    Util.setTEPO(Data.UserDMSP.MF);
    Util.setTEPO(Data.UserDMSP.tel1);
    Util.setTEPO(Data.UserDMSP.tel2);
    Util.setTEPO(Data.UserDMSP.towns);
    Util.setTEPO(Data.UserDMSP.userTypes);
    Util.setTEPO(Data.UserDMSP.zipCodes);
    System.out.println("init over");
  }
}
