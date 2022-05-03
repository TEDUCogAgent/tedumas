package massim.tedumas;

import eis.iilang.Percept;

import java.util.List;


public class TPercept {
    public int stepID;
    public long timeStamp;
    public Percept percept;

    @Override
    public String toString() {
        return stepID +
                ", " + timeStamp +
                ", " + percept;
    }
}
