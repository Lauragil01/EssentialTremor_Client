package signals;

public class Signals {

    private ACC acc;
    private EMG emg;

    public Signals(ACC acc, EMG emg) {
        this.acc = acc;
        this.emg = emg;
    }

    public ACC getAcc() {
        return acc;
    }

    public void setAcc(ACC acc) {
        this.acc = acc;
    }

    public EMG getEmg() {
        return emg;
    }

    public void setEmg(EMG emg) {
        this.emg = emg;
    }
}
