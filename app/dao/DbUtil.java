package dao;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.*;
import play.db.DB;
import play.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作
 * User: wenzhihong
 * Date: 12-9-28
 * Time: 下午4:48
 */
public abstract class DbUtil {
    public static QueryRunner extractRunner = new QueryRunner();

    //提取数据的数据库在配制中的名称, 对应于application.conf里的 db_名称.url的配制信息. 如果要改动请两个一起改.
    public static final String EXTRACT_DB_CONF_NAME = "extract";

    //把数据库查询的行处理成 Map
    public static final RowProcessor MAP_ROW_PROCESSOR = new MapRowProcessor();

    //处理一行
    public static final RowProcessor ROW_PROCESSOR = new BasicRowProcessor();

    /**
     * 返回提取数据的数据库连接
     */
    public static Connection getExtractDBConnection() {
        return DB.getDBConfig(EXTRACT_DB_CONF_NAME, false).getConnection();
    }

    /**
     * 在提取数据的数据库上执行sql. (一般是执行对数据库有更新的那种)
     */
    public static boolean execute4ExtractDB(String SQL) {
        return DB.getDBConfig(EXTRACT_DB_CONF_NAME, false).execute(SQL);
    }

    /**
     * 在提取数据的数据库上执行sql语句(查询类)
     */
    public static ResultSet executeQuery4ExtractDB(String SQL) {
        return DB.getDBConfig(EXTRACT_DB_CONF_NAME, false).executeQuery(SQL);
    }

    /**
     * 查询sql返回单个bean. 如果没有的话, 返回null
     */
    public static <T> T queryExtractDBSingleBean(String sql, Class<T> cl, Object... params) {
        Connection conn = getExtractDBConnection();
        ResultSetHandler<T> h = new BeanHandler<T>(cl);
        T t = null;
        try {
            t = extractRunner.query(conn, sql, h, params);
            return t;
        } catch (SQLException ex) {
            throw new DatabaseException(ex.getMessage(), ex);
        }
    }

    /**
     * 查询sql返回Bean list. 如果没有的话, 返回的list长度为0
     */
    public static <T> List<T> queryExtractDBBeanList(String sql, Class<T> cl, Object... params) {
        Connection conn = getExtractDBConnection();
        ResultSetHandler<List<T>> h = new BeanListHandler<T>(cl);
        try {
            return extractRunner.query(conn, sql, h, params);
        } catch (SQLException ex) {
            throw new DatabaseException(ex.getMessage(), ex);
        }
    }



    /**
     * 用handler处理查询记录
     *
     * @return
     */
    public static <T> T queryExtractDbWithHandler(String sql, ResultSetHandler<T> rsh, Object... params) {
        Connection conn = getExtractDBConnection();
        try {
            return extractRunner.query(conn, sql, rsh, params);
        } catch (SQLException ex) {
            throw new DatabaseException(ex.getMessage(), ex);
        }
    }


    /**
     * 用于count语句的.只查总数
     *
     * @return
     */
    public static Long queryCount(String sql, Object... params) {
        Connection conn = getExtractDBConnection();
        try {
            return extractRunner.query(conn, sql, new ScalarHandler<Long>(), params);
        } catch (SQLException ex) {
            throw new DatabaseException(ex.getMessage(), ex);
        }
    }

    /**
     * 查询单条记录, 转成一个map. 注意, 这里的map的key值为小写的
     */
    public static Map<String, Object> querySingleMap(String sql, Object... params) {
        Connection conn = getExtractDBConnection();
        ResultSetHandler<Map<String, Object>> h = new MapHandler(MAP_ROW_PROCESSOR);
        Map t = null;
        try {
            t = extractRunner.query(conn, sql, h, params);
        } catch (SQLException ex) {
            throw new DatabaseException(ex.getMessage(), ex);
        }
        return t;
    }

    /**
     * 查询多条记录, 转成list<map>. 注意, 这里的map的key值为小写的
     * 如果没有,则返回的list长度为0
     */
    public static List<Map<String, Object>> queryMapList(String sql, Object... params) {
        Connection conn = getExtractDBConnection();
        ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler(MAP_ROW_PROCESSOR);
        try {
            return extractRunner.query(conn, sql, h, params);
        } catch (SQLException ex) {
            throw new DatabaseException(ex.getMessage(), ex);
        }
    }


}
