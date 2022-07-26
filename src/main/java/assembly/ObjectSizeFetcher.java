package assembly;

import java.lang.instrument.Instrumentation;

class ObjectSizeFetcher {
    private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
    }

    public static int getObjectSize(Object o) {
        return (int) instrumentation.getObjectSize(o);
    }
}