package de.mc_anura.core.database;

import de.mc_anura.core.AnuraCore;
import de.mc_anura.core.AnuraThread;
import de.mc_anura.core.database.MySQL.PreparedUpdate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ErrorAppender extends AbstractAppender {

    private static final String[] errorFilterStarts = new String[] {
        "**** SERVER IS RUNNING",
        "The server will make no attempt",
        "Please see http://www.spigotmc",
        "To change this, set",
        "handleDisconnection",
        "Whilst this makes it possible to use BungeeCord",
        "[SimpleBackup] Unable to backup file",
        "A manual (plugin-induced) save has been detected",
        "[NoCheatPlus] The Minecraft version seems to be more recent",
        "Invalid statistic in"
    };

    private static final String[] errorFilterContains = new String[] {
        "moved too quickly",
        "moved wrongly!",
        "was kicked for floating too long!"
    };

    public ErrorAppender() {
        super("AnuraAppender", new AbstractFilter() {
        }, new AbstractStringLayout(Charset.defaultCharset()) {

            @Override
            public byte[] toByteArray(LogEvent le) {
                return new byte[0];
            }

            @Override
            public String getContentType() {
                return "";
            }

            @Override
            public Map<String, String> getContentFormat() {
                return new HashMap<>();
            }

            @Override
            public String toSerializable(LogEvent le) {
                return "";
            }
        });
        this.start();
    }

    @Override
    public void append(LogEvent le) {
        if (le.getLevel().equals(Level.ERROR) || le.getLevel().equals(Level.FATAL) || le.getLevel().equals(Level.WARN)) {
            String msg = le.getMessage().getFormattedMessage();
            if (msg != null) {
                for (String s : errorFilterStarts) {
                    if (msg.startsWith(s)) return;
                }
                for (String s : errorFilterContains) {
                    if (msg.contains(s)) return;
                }
            }
            Runnable save = () -> saveError(msg, le.getThrown(), "Logger: " + le.getLoggerName(), "Thread: " + le.getThreadName());
            if (!AnuraCore.getInstance().isEnabled()) {
                save.run();
            } else {
                AnuraThread.async(save);
            }
        }
    }

    public static void saveError(String error, Throwable ex, String... info) {
        StringBuilder infoStr = new StringBuilder();
        for (String s : info) {
            infoStr.append(s).append("\n");
        }
        DB.queryUpdate(DB.getFirstKey((id) -> saveStackTrace(id, ex)), "INSERT INTO errors (error, exception, message, info, timestamp) VALUES (?, ?, ?, ?, ?)", error, ex == null ? null : ex.getClass().toString(),
                                      ex == null ? "" : ex.getLocalizedMessage(), infoStr.toString(), System.currentTimeMillis() / 1000);
    }

    private static void saveStackTrace(int id, Throwable ex) {
        if (ex == null || (ex.getStackTrace().length == 0 && ex.getCause() == null)) {
            return;
        }
        PreparedUpdate upd = DB.queryPrepUpdate("INSERT INTO errorStack (errorId, className, fileName, methodName, lineNumber, nativeMethod) VALUES (?, ?, ?, ?, ?, ?)");
        if (upd == null) return;
        List<TraceElement> elems = Arrays.stream(ex.getStackTrace()).map((elem) -> (TraceElement) new TraceElementStack(elem)).collect(Collectors.toList());
        while (ex.getCause() != null) {
            ex = ex.getCause();
            elems.add(new TraceElementString("Caused by: ", ex.getLocalizedMessage(), ex.getClass().getName()));
            elems.addAll(Arrays.stream(ex.getStackTrace()).map((elem) -> (TraceElement) new TraceElementStack(elem)).collect(Collectors.toList()));
        }
        elems.forEach((elem) -> upd.add(id, elem.getClassName(), elem.getFileName(), elem.getMethodName(), elem.getLineNumber(), elem.isNative()));
        upd.done();
    }

    private static class TraceElementString extends TraceElement {

        private final String[] msg;

        private TraceElementString(String... msg) {
            this.msg = msg;
        }

        @Override
        String getClassName() {
            return msg.length > 0 ? msg[0] : "";
        }

        @Override
        String getFileName() {
            return msg.length > 1 ? msg[1] : "";
        }

        @Override
        String getMethodName() {
            return msg.length > 2 ? msg[2] : "";
        }

        @Override
        int getLineNumber() {
            return 0;
        }

        @Override
        boolean isNative() {
            return false;
        }

    }

    private static class TraceElementStack extends TraceElement {

        private final StackTraceElement elem;

        private TraceElementStack(StackTraceElement elem) {
            this.elem = elem;
        }

        @Override
        String getClassName() {
            return elem.getClassName();
        }

        @Override
        String getFileName() {
            return elem.getFileName();
        }

        @Override
        String getMethodName() {
            return elem.getMethodName();
        }

        @Override
        int getLineNumber() {
            return elem.getLineNumber();
        }

        @Override
        boolean isNative() {
            return elem.isNativeMethod();
        }

    }

    private static abstract class TraceElement {

        abstract String getClassName();
        abstract String getFileName();
        abstract String getMethodName();
        abstract int getLineNumber();
        abstract boolean isNative();
    }
}
