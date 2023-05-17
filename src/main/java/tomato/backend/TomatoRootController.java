package tomato.backend;

import tomato.backend.data.TomatoData;

import java.util.ArrayList;

public class TomatoRootController {

    private final ArrayList<Controller> controllers;
    private TomatoData data;

    public TomatoRootController(TomatoData data) {
        this.data = data;
        controllers = new ArrayList<>();
    }

    public void addController(Controller c) {
        controllers.add(c);
    }

    public void dispose() {
        for (Controller c : controllers) {
            c.dispose();
        }
    }
}
