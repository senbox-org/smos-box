package org.esa.beam.smos.visat;

import org.openide.modules.OnStart;
import org.openide.modules.OnStop;
import org.openide.windows.OnShowing;

import java.util.concurrent.Callable;

public class Module {

    private Module() {
    }

    @OnStart
    public static class StartOp implements Runnable {

        @Override
        public void run() {

        }
    }

    @OnShowing()
    public static class ShowingOp implements Runnable {

        @Override
        public void run() {

        }
    }

    @OnStop
    public static class StopOp implements Runnable {

        @Override
        public void run() {
        }
    }

    @OnStop
    public static class MaybeStopOp implements Callable {

        @Override
        public Boolean call() {
            return Boolean.TRUE;
        }
    }
}
