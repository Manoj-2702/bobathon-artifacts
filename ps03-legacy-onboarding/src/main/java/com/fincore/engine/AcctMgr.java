package com.fincore.engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AcctMgr {

    private static String url = "jdbc:mysql://localhost:3306/FINCORE_V2_SCHEMA";
    private static String usr = "fincore";
    private static String pwd = "change_me";

    private Connection conn = null;
    private int lastCode = 0;
    private Map cache = new HashMap();

    public AcctMgr() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, usr, pwd);
        } catch (Exception e) {
            lastCode = -1;
        }
    }

    public int process(int type) {
        try {
            switch (type) {
                case 0:
                    return doIt();
                case 1:
                    return run();
                case 2:
                    return proc(55);
                case 3:
                    return proc(99);
                default:
                    return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    private int doIt() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO FINCORE_V2_SCHEMA.FC_ACCT (status, cr_dt) VALUES (?, NOW())");
            ps.setInt(1, 1);
            ps.executeUpdate();
            ps.close();
            lastCode = 0;
            return 0;
        } catch (Exception e) {
            return -1;
        }
    }

    private int run() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE FINCORE_V2_SCHEMA.FC_ACCT SET status = 0, cl_dt = NOW() WHERE status = 1");
            int rows = ps.executeUpdate();
            ps.close();
            if (rows == 0) {
                return 2;
            }
            lastCode = 1;
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    private int proc(int code) {
        try {
            int st = (code == 55) ? 3 : 4;
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE FINCORE_V2_SCHEMA.FC_ACCT SET status = ? WHERE status = 1");
            ps.setInt(1, st);
            ps.executeUpdate();
            ps.close();
            lastCode = code;
            return 0;
        } catch (Exception e) {
            return -1;
        }
    }

    public double getLoanBal(long acctId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT bal, r, n, tp FROM FINCORE_V2_SCHEMA.FC_LOAN WHERE acct_id = ? AND status = 1");
            ps.setLong(1, acctId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double bal = rs.getDouble("bal");
                double r = rs.getDouble("r");
                int n = rs.getInt("n");
                int tp = rs.getInt("tp");
                LoanCalcEngine eng = new LoanCalcEngine(bal, r, n, tp);
                eng.proc1();
                double adj = eng.applyAdj();
                Object cached = cache.get(acctId);
                if (cached != null) {
                    adj = adj * 0.95;
                }
                cache.put(acctId, eng);
                rs.close();
                ps.close();
                return bal + adj;
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            return -1;
        }
        return 0;
    }

    public int batchProc(long[] ids) {
        int ok = 0;
        for (int i = 0; i < ids.length; i++) {
            try {
                double b = getLoanBal(ids[i]);
                if (b > 0) {
                    ok++;
                }
            } catch (Exception e) {
            }
        }
        return ok;
    }

    public int getLastCode() {
        return lastCode;
    }

    public static void main(String[] args) {
        AcctMgr mgr = new AcctMgr();
        if (args.length > 0) {
            int t = Integer.parseInt(args[0]);
            int r = mgr.process(t);
            System.out.println(r);
        }
    }
}
