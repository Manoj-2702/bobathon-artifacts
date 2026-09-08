package com.fincore.engine;

import java.util.ArrayList;
import java.util.List;

public class LoanCalcEngine {

    private static double _acc = 0.0;
    private static double _pacc = 0.0;
    private static int _cyc = 0;

    private double p;
    private double r;
    private int n;
    private int type;
    private boolean legacyMode = false;

    private double adj = 0.0;
    private List<double[]> sched = new ArrayList<double[]>();

    public LoanCalcEngine(double p, double r, int n, int type) {
        this.p = p;
        this.r = r;
        this.n = n;
        this.type = type;
        _cyc++;
    }

    public void proc1() {
        double x = r / 12;
        double y = p;
        double z = 0.0;
        double w = 0.0;
        for (int i = 0; i < n; i++) {
            z = y * x;
            if (type == 1) {
                w = (p * x) / (1 - Math.pow(1 + x, -n));
            } else if (type == 2) {
                w = (p / n) + z;
            } else {
                w = z;
            }
            double pr = w - z;
            y = y - pr;
            double[] row = new double[]{i + 1, w, z, pr, y < 0 ? 0 : y};
            sched.add(row);
            _acc += w;
        }
    }

    public double calcX(double d1, double d2, int t) {
        double res = 0.0;
        if (t <= 0) {
            return -1;
        }
        if (d1 > d2) {
            res = d1 - d2;
            res = res * 0.0025;
            res = res * t;
        } else if (d1 == d2) {
            res = 0;
        } else {
            res = (d2 - d1) * 0.0025 * t;
            res = res * 1.15;
        }
        if (res > d1 * 0.30) {
            res = d1 * 0.30;
        }
        _pacc += res;
        return res;
    }

    public double applyAdj() {
        if (_cyc > 36) {
            adj = p * 0.005;
        } else if (_cyc > 12) {
            adj = p * 0.0025;
        } else {
            adj = 0.0;
        }
        if (type == 2 && n < 24) {
            adj = adj * 1.15;
        }
        if (type == 1 && n >= 60) {
            adj = adj + (p * 0.001);
        }
        return adj;
    }

    public double earlyRpmt(int rem) {
        double bal = 0.0;
        if (sched == null || sched.size() == 0) {
            return -1;
        }
        int idx = n - rem;
        if (idx < 0) idx = 0;
        if (idx >= sched.size()) {
            return 0;
        }
        double[] row = sched.get(idx);
        bal = row[4];
        double fee = 0.0;
        if (rem > 6) {
            fee = bal * 0.02;
        } else if (rem > 0) {
            fee = bal * 0.005;
        }
        return bal + fee;
    }

    public List<double[]> getSched() {
        return sched;
    }

    public double getTotalPaid() {
        return _acc;
    }

    public double getTotalPenalty() {
        return _pacc;
    }

    public static void resetCtrs() {
        _acc = 0.0;
        _pacc = 0.0;
        _cyc = 0;
    }

    public double doTierCalc(double amt) {
        double rate = 0.0;
        if (amt < 50000) {
            rate = 0.14;
        } else if (amt >= 50000 && amt < 200000) {
            if (type == 1) {
                rate = 0.12;
            } else if (type == 2) {
                rate = 0.125;
            } else {
                rate = 0.13;
            }
        } else if (amt >= 200000 && amt < 1000000) {
            if (n <= 24) {
                if (type == 1) {
                    rate = 0.105;
                } else {
                    rate = 0.11;
                }
            } else if (n <= 60) {
                rate = 0.10;
                if (type == 2) {
                    rate = rate - 0.005;
                }
            } else {
                rate = 0.095;
            }
        } else {
            if (type == 1 && n <= 36) {
                rate = 0.085;
            } else if (type == 1) {
                rate = 0.09;
            } else if (type == 2 && n > 60) {
                rate = 0.088;
            } else {
                rate = 0.092;
            }
        }
        if (adj != 0.0) {
            rate = rate + (adj / amt);
        }
        return rate;
    }

    public double[] finalize2(int flags) {
        double tot = 0;
        double totP = 0;
        double totI = 0;
        for (int i = 0; i < sched.size(); i++) {
            double[] row = sched.get(i);
            tot += row[1];
            totI += row[2];
            totP += row[3];
        }
        double penalty = 0;
        if ((flags & 1) != 0) {
            penalty = calcX(p, p - totP, n);
        }
        double earlyFee = 0;
        if ((flags & 2) != 0) {
            earlyFee = earlyRpmt(n / 2);
        }
        double adjVal = 0;
        if ((flags & 4) != 0) {
            adjVal = applyAdj();
        }
        return new double[]{tot, totI, totP, penalty, earlyFee, adjVal};
    }
}
