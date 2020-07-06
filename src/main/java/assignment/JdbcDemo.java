package assignment;

import org.junit.Test;

import java.sql.*;

public class JdbcDemo {

    @Test
    public void test(){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/kkb", "root", "root");
            String sql = "select address from user where username = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, "王五");
            resultSet = statement.executeQuery();

            while(resultSet.next()){
                System.out.println(resultSet.getString("address"));
//                System.out.println(resultSet.getString(0));
                System.out.println(resultSet.getString(1));
            }
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
            if (connection!=null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
