package redux.state;

import redux.action.Action;

public class RootState {
    public StatMaxingGuiState statMaxingGui = new StatMaxingGuiState();
    public NetworkState network = new NetworkState();

    public static RootState reduce(RootState state, Action action) {
        state.statMaxingGui = StatMaxingGuiState.reduce(state.statMaxingGui, action);
        state.network = NetworkState.reduce(state.network, action);

        return state;
    }
}
