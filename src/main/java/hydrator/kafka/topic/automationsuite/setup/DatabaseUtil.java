package hydrator.kafka.topic.automationsuite.setup;

import hydrator.kafka.topic.automationsuite.setup.objects.AgentSkills;
import hydrator.kafka.topic.automationsuite.setup.reports.Log;
import lombok.Getter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    private final Connection connection;
    private final List<AgentSkills> dataFromAgentTable = new ArrayList<>();
    private int countOfAgentNo;
    @Getter
    private long lastUpdateTime;

    public DatabaseUtil(Connection connection) {
        this.connection = connection;
    }

    public void fetchDataFromDB(String query) {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            Log.info("Executing query on MySQL");

            while (rs.next()) {
                int busNo = rs.getInt("bus_no");
                int agentNo = rs.getInt("agent_no");
                int skill = rs.getInt("skill_no");
                String sourceCluster = rs.getString("source_cluster");
                AgentSkills record = new AgentSkills(busNo, agentNo, skill, sourceCluster);
                dataFromAgentTable.add(record);

                Log.info("Query executed successfully");
            }
        } catch (SQLException e) {
            Log.error("SQL error during query execution");
            e.printStackTrace();
        }
    }

    public void fetchTheCountOfDataFromDB(String query, String columnName) {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            Log.info("Executing query on MySQL");

            if (rs.next()) {
                countOfAgentNo = rs.getInt(columnName);
            } else {
                Log.info("No data found.");
            }

            Log.info("Query executed successfully");

        } catch (SQLException e) {
            Log.error("SQL error during query execution");
            e.printStackTrace();
        }
    }

    public void fetchLastUpdateTimeFromDB(String query) {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            Log.info("Executing query on MySQL");

            if (rs.next()) {
                lastUpdateTime = rs.getLong("last_update_time_ms");
            } else {
                Log.info("No data found.");
            }

            Log.info("Query executed successfully");

        } catch (SQLException e) {
            Log.error("SQL error during query execution");
            e.printStackTrace();
        }
    }

    public int getCountOfData() {
        return countOfAgentNo;
    }

    public List<AgentSkills> getDataFromDBList() {
        return dataFromAgentTable;
    }

}



