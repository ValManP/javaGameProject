package model.database;

import java.sql.Connection;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.JTextArea;

public class DBInterface {
    public Connection connection;
    public Statement statement;
    public PreparedStatement preparedStatement;
    public ResultSet resultSet;
    
    private JTextArea log;
    
    public DBInterface (JTextArea log) {
        this.log = log;
    }
    
    // --------ПОДКЛЮЧЕНИЕ К БАЗЕ ДАННЫХ--------
    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:gameDB.db");

        log.append("Connected to DB\n");
    }
    
    public void addUser(String name) throws SQLException {
        preparedStatement = connection.prepareStatement(Q_ADD_USER);
        preparedStatement.setString(1, name);
        preparedStatement.execute();

        log.append("Player "+name+" was added to DB\n");
    }
    
    public int findUserByName(String name) throws SQLException {
        preparedStatement = connection.prepareStatement(Q_FIND_USER_BY_NAME);
        preparedStatement.setString(1, name);
        
        resultSet = preparedStatement.executeQuery();
        
        int user_id = 0;
        while(resultSet.next()) {
            user_id = resultSet.getInt(1);
        }
        
        return user_id;
    }
    
    public String findUserById(int id) throws SQLException {
        preparedStatement = connection.prepareStatement(Q_FIND_USER_BY_ID);
        preparedStatement.setInt(1, id);
        
        resultSet = preparedStatement.executeQuery();
        
        String user_name = null;
        while(resultSet.next()) {
            user_name = resultSet.getString(1);
        }
        
        return user_name;
    }
    
    public void addGame(String user1, String user2,
            int score1, int score2) throws SQLException {
        preparedStatement = connection.prepareStatement(Q_ADD_GAME);
        preparedStatement.setInt(1, findUserByName(user1));
        preparedStatement.setInt(2, findUserByName(user2));
        preparedStatement.setInt(2, score1);
        preparedStatement.setInt(2, score2);

        preparedStatement.setDate(2, (java.sql.Date) new Date());
        preparedStatement.execute();

        log.append("New game between " + user1+
                " and " + user2 + " was added to DB\n");
    }
    
    public int findLastGame(int user1, int user2) throws SQLException {
        preparedStatement = connection.prepareStatement(Q_FIND_LAST_GAME);
        preparedStatement.setInt(1, user1);
        preparedStatement.setInt(2, user2);
        preparedStatement.execute();

        int game_id = 0;
        while(resultSet.next()) {
            game_id = resultSet.getInt(1);
        }
        
        return game_id;
    }
    public ResultSet findUserGames(int user_id) throws SQLException {
        preparedStatement = connection.prepareStatement(Q_FIND_USER_GAMES);
        preparedStatement.setInt(1, user_id);
        preparedStatement.setInt(2, user_id);
        preparedStatement.execute();
        
        return resultSet;
    } 
    private static final String Q_ADD_USER = "INSERT INTO users (user_name) VALUES (?);";
    private static final String Q_ADD_GAME = "INSERT INTO games (player1_id, player2_id, player1_score, player2_score, date) "
            + "VALUES (?, ?, ?, ?, ?);";
    
    private static final String Q_FIND_USER_BY_NAME = "SELECT user_id FROM users WHERE user_name = ?;";
    private static final String Q_FIND_USER_BY_ID = "SELECT user_name FROM users WHERE user_id = ?;";
    private static final String Q_FIND_LAST_GAME = "SELECT game_id "
            + "FROM games "
            + "WHERE user1_id = ? "
            + "and user2_id = ? and rownum = 1 "
            + "ORDER BY date DESC;";
    private static final String Q_FIND_USER_GAMES = "SELECT * "
            + "FROM games "
            + "WHERE user1_id = ? OR user2_id = ?"
            + "ORDER BY date;";
}
