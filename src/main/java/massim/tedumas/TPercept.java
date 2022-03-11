package massim.tedumas;

import eis.iilang.Percept;

import java.util.List;


public class TPercept {
    public int prcptid;
    public long tmstmp;
    public Percept prcpt;

    @Override
    public String toString() {
        return prcptid +
                ", " + tmstmp +
                ", " + prcpt;
    }
}
