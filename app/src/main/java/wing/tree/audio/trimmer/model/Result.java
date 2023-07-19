package wing.tree.audio.trimmer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Result {
    public double duration;
    public String amplitudes;

    public List<Integer> amplitudesAsList() {
        if(amplitudes == null || amplitudes.isEmpty())
            return Collections.emptyList();

        String[] log = amplitudes.split("\n");
        List<Integer> amplitudes = new ArrayList<>();

        for (String amplitude : log) {
            if(amplitude.isEmpty()) {
                break;
            }

            amplitudes.add(Integer.valueOf(amplitude));
        }

        return amplitudes;
    }
}
