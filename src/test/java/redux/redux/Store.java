package redux.redux;

import redux.action.Action;
import redux.state.RootState;

import java.util.HashSet;
import java.util.function.Consumer;

public class Store {

    public static Store INSTANCE = new Store();
    private RootState state = new RootState();
    private HashSet<Consumer<RootState>> listeners = new HashSet<>();

    public void subscribe(Consumer<RootState> listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Consumer<RootState> listener) {
        listeners.remove(listener);
    }

    public void dispatch(Action action) {
        state = RootState.reduce(state, action);

        for (Consumer<RootState> l : listeners) {
            l.accept(state);
        }
    }

    public RootState getState() {
        return state;
    }
}
