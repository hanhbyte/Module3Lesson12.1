package DAO;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.User;

public class UserDAO implements IUserDAO {

  public static final String INSERT_USER_SQL =
      "INSERT_INTO users" + "(name, email, country) VALUES" + "(?,?,?)";
  public static final String SELECT_USER_BY_ID = "select id, name, email, country from users where id =?";
  public static final String SELECT_ALL_USERS = "select*from users";
  public static final String DELETE_USERS_SQL = "delete from users where id=?";
  public static final String UPDATE_USERS_SQL = "update users set name=?, email=?, country=?, where id=?";
  private static final String SQL_INSERT = "INSERT INTO EMPLOYEE (NAME, SALARY, CREATED_DATE) VALUES (?,?,?)";
  private static final String SQL_UPDATE = "UPDATE EMPLOYEE SET SALARY=? WHERE NAME=?";
  private static final String SQL_TABLE_CREATE = "CREATE TABLE EMPLOYEE"

      + "("

      + " ID serial,"

      + " NAME varchar(100) NOT NULL,"

      + " SALARY numeric(15, 2) NOT NULL,"

      + " CREATED_DATE timestamp,"

      + " PRIMARY KEY (ID)"

      + ")";
  private static final String SQL_TABLE_DROP = "DROP TABLE IF EXISTS EMPLOYEE";
  private String jdbcURL = "jdbc:mysql://localhost:3306/demo?useSSL=false";
  private String jdbcUsername = "root";
  private String jdbcPassword = "hanh1234";


  public UserDAO() {
  }

  protected Connection getConnection() {
    Connection connection = null;
    try {
      Class.forName("com.mysql.jdbc.Driver");
      connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return connection;
  }

  @Override
  public void insertUser(User user) throws SQLException {
    System.out.println(INSERT_USER_SQL);
    try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(
        INSERT_USER_SQL)) {
      preparedStatement.setString(1, user.getName());
      preparedStatement.setString(2, user.getEmail());
      preparedStatement.setString(3, user.getCountry());
      System.out.println(preparedStatement);
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      printSQLException(e);
    }
  }

  @Override
  public User selectUser(int id) {
    User user = null;
    // Step 1: Establishing a Connection
    try (Connection connection = getConnection();
        // Step 2:Create a statement using connection object
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_ID)) {
      preparedStatement.setInt(1, id);
      System.out.println(preparedStatement);
      // Step 3: Execute the query or update query
      ResultSet rs = preparedStatement.executeQuery();

      // Step 4: Process the ResultSet object.
      while (rs.next()) {
        String name = rs.getString("name");
        String email = rs.getString("email");
        String country = rs.getString("country");
        user = new User(id, name, email, country);
      }
    } catch (SQLException e) {
      printSQLException(e);
    }
    return user;
  }

  @Override
  public List<User> selectAllUsers() {

    // using try-with-resources to avoid closing resources (boiler plate code)
    List<User> users = new ArrayList<>();
    // Step 1: Establishing a Connection
    try (Connection connection = getConnection();

        // Step 2:Create a statement using connection object
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_USERS)) {
      System.out.println(preparedStatement);
      // Step 3: Execute the query or update query
      ResultSet rs = preparedStatement.executeQuery();

      // Step 4: Process the ResultSet object.
      while (rs.next()) {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String country = rs.getString("country");
        users.add(new User(id, name, email, country));
      }
    } catch (SQLException e) {
      printSQLException(e);
    }
    return users;
  }

  @Override
  public boolean deleteUser(int id) throws SQLException {
    boolean rowDeleted;
    try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(
        DELETE_USERS_SQL)) {
      statement.setInt(1, id);
      rowDeleted = statement.executeUpdate() > 0;
    }
    return rowDeleted;
  }

  @Override
  public boolean updateUser(User user) throws SQLException {
    boolean rowUpdated;
    try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(
        UPDATE_USERS_SQL)) {
      statement.setString(1, user.getName());
      statement.setString(2, user.getEmail());
      statement.setString(3, user.getCountry());
      statement.setInt(4, user.getId());

      rowUpdated = statement.executeUpdate() > 0;
    }
    return rowUpdated;
  }

  @Override
  public User getUserById(int id) {
    User user = null;
    String query = "{CALL get_by_user_by_id(?)}";

    try (Connection connection = getConnection();
        PreparedStatement callableStatement = connection.prepareStatement(query)) {
      callableStatement.setInt(1, id);
      ResultSet resultSet = callableStatement.executeQuery();
      while (resultSet.next()) {
        String name = resultSet.getString("name");
        String email = resultSet.getString("email");
        String country = resultSet.getString("country");
        user = new User(id, name, email, country);
      }
    } catch (SQLException e) {
      printSQLException(e);
    }
    return user;
  }

  @Override
  public void insertUserStore(User user) throws SQLException {
    String query = "{CALL insert_user(?,?,?)}";
    try (Connection connection = getConnection();
        CallableStatement callableStatement = connection.prepareCall(query)) {
      callableStatement.setString(1, user.getName());
      callableStatement.setString(2, user.getEmail());
      callableStatement.setString(3, user.getCountry());
      System.out.println(callableStatement);
      callableStatement.executeUpdate();
    } catch (SQLException e) {
      printSQLException(e);
    }
  }

  @Override
  public void addUserTransaction(User user, int[] permision) {
    Connection connection = null;
    PreparedStatement pstmt = null;
    PreparedStatement pstmtAssignment = null;
    ResultSet resultSet = null;
    try {
      connection = getConnection();
      connection.setAutoCommit(false);
      pstmt = connection.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS);
      pstmt.setString(1, user.getName());
      pstmt.setString(2, user.getEmail());
      pstmt.setString(3, user.getCountry());
      int rowAffected = pstmt.executeUpdate();
      resultSet = pstmt.getGeneratedKeys();
      int userId = 0;
      if (resultSet.next()) {
        userId = resultSet.getInt(1);
        if (rowAffected == 1) {
          String sqlPivot = "INSERT INTO user_permision(user_id, permision_id)" + "VALUES(?,?)";
          pstmtAssignment = connection.prepareStatement(sqlPivot);
          for (int permisionId : permision) {
            pstmtAssignment.setInt(1, userId);
            pstmtAssignment.setInt(2, permisionId);
            pstmtAssignment.executeUpdate();
          }
          connection.commit();
        } else {
          connection.rollback();
        }
      }
    } catch (SQLException ex) {
      try {
        if (connection == null) {
          connection.rollback();
        }
      } catch (SQLException e) {
        System.out.println(e.getMessage());
      }
      System.out.println(ex.getMessage());
    } finally {
      try {
        if (resultSet != null) {
          resultSet.close();
        }

        if (pstmt != null) {
          pstmt.close();
        }

        if (pstmtAssignment != null) {
          pstmtAssignment.close();
        }

        if (connection != null) {
          connection.close();
        }
      } catch (SQLException e) {
        System.out.println(e.getMessage());
      }
    }
  }

  @Override
  public void insertUpdateWithoutTransaction() {
    try (Connection connection = getConnection();
        Statement statement = connection.createStatement();
        PreparedStatement preInsert = connection.prepareStatement(SQL_INSERT);
        PreparedStatement preUpdate = connection.prepareStatement(SQL_UPDATE)) {
      statement.execute(SQL_TABLE_DROP);
      statement.execute(SQL_TABLE_CREATE);
      preInsert.setString(1, "MyNhat");
      preInsert.setBigDecimal(2, new BigDecimal(10));
      preInsert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
      preInsert.execute();
      preInsert.setString(1, "Hanh");
      preInsert.setBigDecimal(2, new BigDecimal(20));
      preInsert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
      preInsert.execute();
      preUpdate.setBigDecimal(2, new BigDecimal(999.99));
      preUpdate.setString(2, "MyNhat");
      preUpdate.execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void printSQLException(SQLException ex) {
    for (Throwable e : ex) {
      if (e instanceof SQLException) {
        e.printStackTrace(System.err);
        System.err.println("SQLState: " + ((SQLException) e).getSQLState());
        System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
        System.err.println("Message: " + e.getMessage());
        Throwable t = ex.getCause();
        while (t != null) {
          System.out.println("Cause: " + t);
          t = t.getCause();
        }
      }
    }
  }
}
