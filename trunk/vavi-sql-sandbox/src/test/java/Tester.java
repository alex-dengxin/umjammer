import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


/**
 * テストプログラム
 */
public class Tester {
    Connection conn = null;

    public static void main(String[] args) throws Exception {
        new Tester().testVault();
    }

    public void testVault() throws Exception {
        DriverManager.registerDriver(new vavi.sql.jdbc.Driver());
        conn = DriverManager.getConnection("jdbc:vavi:jdbc:" + "ODBCSOURCE", "USER", "PASS");
        
        testCreateTable();
        
        testInsertByStatement();
        testInsertByPreparedStatement();
        
        testSelectByStatement();
        testSelectByPreparedStatement();
    }

    public void testCreateTable() throws Exception {
        Statement stmt = null;

        stmt = conn.createStatement();
        stmt.execute("DROP TABLE MyTest");
        stmt.close();
        
        PreparedStatement pstmt = null;
        
        pstmt = conn.prepareStatement("CREATE TABLE MyTest (mycode INT NOT NULL,mystring VARCHAR(64) NULL)");
        pstmt.execute();
        pstmt.close();
        
        pstmt = conn.prepareStatement("CREATE UNIQUE INDEX IxMyTest ON MyTest (mycode) WITH PRIMARY");
        pstmt.execute();
        pstmt.close();
    }

    /**
     * SQL文埋め込み文字列による INSERT文 および setInt setStringによる
     * INSERT文をテストします。
     * 現状は、項目番号順(1,2,...)にsetXXX のみにしか対応していません。
     */
    public void testInsertByStatement() throws Exception {
        Statement stmt = null;

        stmt = conn.createStatement();
        stmt.execute("INSERT INTO MyTest VALUES (1, '文字列')");
        stmt.close();
    }
    
    /**
     * SQL文埋め込み文字列による INSERT文 および setInt setStringによる
     * INSERT文をテストします。
     * 現状は、項目番号順(1,2,...)にsetXXX のみにしか対応していません。
     */
    public void testInsertByPreparedStatement() throws Exception {
        PreparedStatement pstmt = null;
        
        pstmt = conn.prepareStatement("INSERT INTO MyTest VALUES (?,?)");
        pstmt.setInt(1, 2);
        pstmt.setString(2, "文字列その2");
        pstmt.execute();
        pstmt.close();
    }

    /**
     * SELECTのテスト
     * ResultSet の getInt() getString() が実装されています。
     * このほかに、ResultSetMetaDataが一部実装されています。
     */
    public void testSelectByStatement() throws Exception {
        Statement stmt = null;

        stmt = conn.createStatement();
        
        ResultSet rs = stmt.executeQuery("SELECT * FROM MyTest");
        
        for (int index = 0; rs.next(); index++) {
            System.out.println("検索結果:" + rs.getInt(1) + ",'" +
                    rs.getString(2) + "'");
        }
        
        rs.close();
        stmt.close();
    }

    /**
     * SELECTのテスト
     */
    public void testSelectByPreparedStatement() throws Exception {
        PreparedStatement pstmt = null;

        pstmt = conn.prepareStatement("SELECT * FROM MyTest WHERE mycode=? AND mystring=?");
        pstmt.setInt(1, 2);
        pstmt.setString(2, "文字列その2");
        
        ResultSet rs = pstmt.executeQuery();
        
        for (int index = 0; rs.next(); index++) {
            System.out.println("検索結果:" + rs.getInt(1) + ",'" + rs.getString(2) + "'");
        }
        
        rs.close();
        pstmt.close();
    }
}
