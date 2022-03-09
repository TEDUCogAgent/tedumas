package massim.tedumas;

import eis.iilang.Percept;

import java.util.List;


public class TPercept {
    public int prcpt_id;
    public long tmstmp;
    public Percept prcpt;

    @Override
    public String toString() {
        return prcpt_id +
                ", " + tmstmp +
                ", " + prcpt;
    }
}
