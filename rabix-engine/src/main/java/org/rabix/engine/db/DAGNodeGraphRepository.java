package org.rabix.engine.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;
import org.rabix.common.helper.JSONHelper;
import org.rabix.db.DBException;
import org.rabix.db.transaction.JdbcTransactionHolder;
import org.rabix.engine.model.DAGNodeRecord.DAGNodeGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAGNodeGraphRepository {

  private final static Logger logger = LoggerFactory.getLogger(DAGNodeGraphRepository.class);
  
  private static final String INSERT_DAG_NODE = "INSERT INTO DAG_NODE (ID,DAG) VALUES (?,?::json);";

  private static final String SELECT_DAG_NODE = "WITH RECURSIVE flattened AS (\r\n\tSELECT null AS parent, replace(cast (dag->'id' AS TEXT), '\"', '') AS id, dag AS node, dag->'isContainer' AS is_container, id AS root_external_id\r\n\tFROM dag_node WHERE ( dag->>'children' ) IS NOT NULL\r\nUNION ALL\r\n\tSELECT replace(cast (f.node->'id' AS TEXT), '\"', '') AS parent, replace(cast (json_array_elements(f.node->'children')->'id' AS TEXT), '\"', '') AS id, json_array_elements(f.node->'children') AS node, json_array_elements(f.node->'children')->'isContainer' AS is_container, f.root_external_id AS root_external_id\r\n\tFROM flattened f WHERE (f.node->'children') IS NOT NULL\r\n)\r\nSELECT parent, id, node, is_container, root_external_id FROM flattened\r\nWHERE id=? AND root_external_id=?;"; 
                
  public void insert(DAGNodeGraph dagNode, String contextId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(INSERT_DAG_NODE);
      stmt.setString(1, contextId);

      PGobject appObject = new PGobject();
      appObject.setType("json");
      appObject.setValue(JSONHelper.writeObject(dagNode));
      stmt.setObject(2, appObject);
      
      stmt.executeUpdate();
    } catch (SQLException e) {
      logger.error("Failed to insert DAGNode " + dagNode, e);
      throw new DBException("Failed to insert DAGNode " + dagNode, e);
    }
  }
  
  public DAGNodeGraph find(String id, String contextId) throws DBException {
    PreparedStatement stmt = null;
    try {
      Connection c = JdbcTransactionHolder.getCurrentTransaction().getConnection();

      stmt = c.prepareStatement(SELECT_DAG_NODE);
      stmt.setString(1, id);
      stmt.setString(2, contextId);

      ResultSet result = stmt.executeQuery();
      List<DAGNodeGraph> dagNodes = convertToDAGNodes(result);
      stmt.close();

      return dagNodes.size() == 1? dagNodes.get(0) : null;
    } catch (SQLException e) {
      logger.error("Failed to find DAGNodeGraph for id=" + id + " and rootId=" + contextId, e);
      throw new DBException("Failed to find DAGNodeGraph for id=" + id + " and rootId=" + contextId, e);
    }
  }
  
  private List<DAGNodeGraph> convertToDAGNodes(ResultSet resultSet) throws SQLException {
    List<DAGNodeGraph> result = new ArrayList<>();

    while (resultSet.next()) {
      try {
      String node = resultSet.getString("NODE");
      result.add(JSONHelper.readObject(node, DAGNodeGraph.class));
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    return result;
  }
}
