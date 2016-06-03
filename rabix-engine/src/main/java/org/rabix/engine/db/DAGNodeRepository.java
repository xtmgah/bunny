package org.rabix.engine.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.postgresql.util.PGobject;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.JSONHelper;
import org.rabix.db.DBException;
import org.rabix.db.transaction.JdbcTransactionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

public class DAGNodeRepository {

  private final static Logger logger = LoggerFactory.getLogger(DAGNodeRepository.class);
  
  private static final String SELECT_DAG_NODE = "SELECT * FROM DAG_NODE WHERE ID=? AND CONTEXT_ID=?;";
  
  private static final String INSERT_DAG_NODE = "INSERT INTO DAG_NODE (ID,CONTEXT_ID,APPLICATION,SCATTER_METHOD,INPUT_PORTS,OUTPUT_PORTS,DEFAULTS) VALUES (?,?,?::json,?,?::json,?::json,?::json);";

  public void insert(DAGNode dagNode, String contextId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(INSERT_DAG_NODE);

      stmt.setString(1, dagNode.getId());
      stmt.setString(2, contextId);

      PGobject appObject = new PGobject();
      appObject.setType("json");
      if (dagNode.getApp() == null) {
        appObject.setValue(null);
      } else {
        appObject.setValue(dagNode.getApp().serialize());
      }
      stmt.setObject(3, appObject);
      
      stmt.setString(4, dagNode.getScatterMethod().name());
      
      PGobject inputPortsObj = new PGobject();
      inputPortsObj.setType("json");
      if (dagNode.getInputPorts() == null) {
        inputPortsObj.setValue(null);
      } else {
        inputPortsObj.setValue(JSONHelper.writeObject(dagNode.getInputPorts()));
      }
      stmt.setObject(5, inputPortsObj);
      
      PGobject outputPortsObj = new PGobject();
      outputPortsObj.setType("json");
      if (dagNode.getInputPorts() == null) {
        outputPortsObj.setValue(null);
      } else {
        outputPortsObj.setValue(JSONHelper.writeObject(dagNode.getOutputPorts()));
      }
      stmt.setObject(6, outputPortsObj);
      
      PGobject defaultsObj = new PGobject();
      defaultsObj.setType("json");
      if (dagNode.getDefaults() == null) {
        defaultsObj.setValue(null);
      } else {
        defaultsObj.setValue(JSONHelper.writeObject(dagNode.getDefaults()));
      }
      stmt.setObject(7, defaultsObj);
      
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to insert DAGNode " + dagNode, e);
      throw new DBException("Failed to insert DAGNode " + dagNode, e);
    }
  }
  
  public DAGNode find(String id, String contextId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_DAG_NODE);
      stmt.setString(1, id);
      stmt.setString(2, contextId);

      ResultSet result = stmt.executeQuery();
      List<DAGNode> dagNodes = convertToDAGNodes(result);
      stmt.close();

      return dagNodes.size() == 1 ? dagNodes.get(0) : null;
    } catch (SQLException e) {
      logger.error("Failed to find DAGNode for id=" + id + " and contextId=" + contextId, e);
      throw new DBException("Failed to find DAGNode for id=" + id + " and contextId=" + contextId, e);
    }
  }
  
  private List<DAGNode> convertToDAGNodes(ResultSet resultSet) throws SQLException {
    List<DAGNode> result = new ArrayList<>();

    while (resultSet.next()) {
      String id = resultSet.getString("ID");
      String application = resultSet.getString("APPLICATION");
      String scatterMethod = resultSet.getString("SCATTER_METHOD");
      String inputPorts = resultSet.getString("INPUT_PORTS");
      String outputPorts = resultSet.getString("OUTPUT_PORTS");
      String defaults = resultSet.getString("DEFAULTS");

      String applicationDataUri = URIHelper.createDataURI(application);
      
      Application applicationObj = null;
      try {
        applicationObj = BindingsFactory.create(applicationDataUri).loadAppObject(applicationDataUri);
      } catch (BindingException e) {
        throw new SQLException("Failed to deserialize application " + application);
      }
      List<DAGLinkPort> inputPortsObj = JSONHelper.readObject(inputPorts, new TypeReference<List<DAGLinkPort>>() {});
      List<DAGLinkPort> outputPortsObj = JSONHelper.readObject(outputPorts, new TypeReference<List<DAGLinkPort>>() {});
      ScatterMethod scatterMethodObj = scatterMethod != null ? ScatterMethod.valueOf(scatterMethod) : null;
      Map<String, Object> defaultsObj = JSONHelper.readMap(defaults);
      result.add(new DAGNode(id, inputPortsObj, outputPortsObj, scatterMethodObj, applicationObj, defaultsObj));
    }
    return result;
  }
  
}
