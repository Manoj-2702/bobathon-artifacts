package com.fincore.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class RptGen {

    private static final String CFG = "/opt/fincore/config/rpt.properties";
    private static final String SCHEMA = "FINCORE_V2_SCHEMA";

    private Properties props = new Properties();
    private Connection conn = null;
    private String fmt = "TXT";

    public RptGen() {
        try {
            props.load(new FileInputStream(CFG));
            fmt = props.getProperty("rpt.fmt", "TXT");
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String u = props.getProperty("db.url", "jdbc:mysql://localhost:3306/" + SCHEMA);
            String us = props.getProperty("db.user", "fincore");
            String pw = props.getProperty("db.pass", "change_me");
            conn = DriverManager.getConnection(u, us, pw);
        } catch (Exception e) {
        }
    }

    public String gen(int rptId, String p1, String p2) {
        String sql = "";
        StringBuffer sb = new StringBuffer();
        if (rptId == 1) {
            sql = "SELECT acct_id, bal, status, cr_dt FROM " + SCHEMA + ".FC_ACCT"
                + " WHERE cr_dt BETWEEN '" + p1 + "' AND '" + p2 + "'"
                + " AND status IN (1,3,4) ORDER BY cr_dt ASC";
        } else if (rptId == 2) {
            sql = "SELECT l.acct_id, l.bal, l.r, l.n, l.tp, a.status"
                + " FROM " + SCHEMA + ".FC_LOAN l"
                + " JOIN " + SCHEMA + ".FC_ACCT a ON l.acct_id = a.acct_id"
                + " WHERE l.status = 1 AND a.status = 1"
                + " AND l.bal > " + p1;
        } else if (rptId == 3) {
            sql = "SELECT acct_id, SUM(amt) as tot, COUNT(*) as cnt"
                + " FROM " + SCHEMA + ".FC_TXN"
                + " WHERE txn_dt >= '" + p1 + "'"
                + " AND tp IN (1,2,5)"
                + " GROUP BY acct_id HAVING tot > " + p2;
        } else if (rptId == 4) {
            sql = "SELECT a.acct_id, a.cr_dt, l.bal, l.r"
                + " FROM " + SCHEMA + ".FC_ACCT a"
                + " LEFT JOIN " + SCHEMA + ".FC_LOAN l ON a.acct_id = l.acct_id"
                + " WHERE a.status = 4";
        } else {
            return "ERR:UNKNOWN_RPT";
        }

        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (fmt.equals("CSV")) {
                sb.append(getHdr(rptId));
                sb.append("\n");
                while (rs.next()) {
                    int cols = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= cols; i++) {
                        sb.append(rs.getString(i) == null ? "" : rs.getString(i));
                        if (i < cols) sb.append(",");
                    }
                    sb.append("\n");
                }
            } else {
                sb.append("=== FINCORE REPORT [" + rptId + "] ===\n");
                sb.append("FROM: " + p1 + "  TO: " + p2 + "\n");
                sb.append("---\n");
                while (rs.next()) {
                    int cols = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= cols; i++) {
                        sb.append(rs.getMetaData().getColumnName(i));
                        sb.append("=");
                        sb.append(rs.getString(i));
                        if (i < cols) sb.append(" | ");
                    }
                    sb.append("\n");
                }
                sb.append("---\n");
                sb.append("END\n");
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            return "ERR:" + e.getMessage();
        }
        return sb.toString();
    }

    private String getHdr(int rptId) {
        if (rptId == 1) return "acct_id,bal,status,cr_dt";
        if (rptId == 2) return "acct_id,bal,r,n,tp,status";
        if (rptId == 3) return "acct_id,tot,cnt";
        if (rptId == 4) return "acct_id,cr_dt,bal,r";
        return "";
    }

    public String genBatch(int[] ids, String p1, String p2) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < ids.length; i++) {
            out.append("--- RPT " + ids[i] + " ---\n");
            out.append(gen(ids[i], p1, p2));
            out.append("\n");
        }
        return out.toString();
    }

    public void setFmt(String f) {
        this.fmt = f;
    }
}
