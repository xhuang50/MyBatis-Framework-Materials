package assignment;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import pojo.User;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class MyBatisV1 {
    private Properties properties = new Properties();

    @Test
    public void test() {
        loadProperties("jdbc.properties");
        List<User> users1 = selectList("queryUserById", 1);
        System.out.println("by id: " + users1);
        List<User> users2 = selectList("queryUserByName", "Catie");
        System.out.println("by name: " + users2);
        Map params = new HashMap();
        params.put("username", "Catie");
        params.put("gender", "female");
        List<User> users3 = selectList("queryUserByParams", params);
        System.out.println(users3);

    }

    private void loadProperties(String location) {
        InputStream inputStream = null;
        try {
            inputStream = this.getClass().getClassLoader().getResourceAsStream(location);
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T> List<T> selectList(String stmtId, Object param) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<T> results = new ArrayList<>();
        try {
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName(properties.getProperty("db.driver"));
            dataSource.setUrl(properties.getProperty("db.url"));
            dataSource.setUsername(properties.getProperty("db.username"));
            dataSource.setPassword(properties.getProperty("db.password"));
            connection = dataSource.getConnection();

            String sql = properties.getProperty("db.sql."+ stmtId);
            statement = connection.prepareStatement(sql);
//            statement.setObject(1, param);
            if (param instanceof String || param instanceof Integer){
                statement.setObject(1, param);
            } else if (param instanceof Map){
                Map paramMap = (Map) param;
                String[] columnNames = properties.getProperty("db.sql."+stmtId+".columnNames").split(",");
                for (int i = 0; i < columnNames.length; i++) {
                    String columnName = columnNames[i];
                    Object value = paramMap.get(columnName);
                    statement.setObject(i+1, value);
                }
            } else {
                //TODO
            }
            resultSet = statement.executeQuery();

            String className = properties.getProperty("db.sql."+stmtId+".resultClassName");
            Class clazz = Class.forName(className);
            Object result = null;
            while(resultSet.next()){
                result = clazz.newInstance();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Field field = clazz.getDeclaredField(columnName);
                    field.setAccessible(true);
                    field.set(result, resultSet.getObject(i));
                }
                results.add((T) result);
            }
            return results;
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if (resultSet != null){
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null){
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
