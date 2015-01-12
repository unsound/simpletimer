package org.catacombae.simpletimer;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class SimpleTimer extends JFrame {
    private JPanel mainPanel;
    private StoppableController curController = null;

    public SimpleTimer() {
	super("SimpleTimer");

        mainPanel = createNewSimpleTimerPanel();

	setupMenus();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	getContentPane().add(mainPanel);
	pack();
	setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(curController != null) {
                    curController.stop();
                }
            }
        });
    }

    private SimpleTimerPanel createNewSimpleTimerPanel() {
        SimpleTimerPanel p;

        if(curController != null) {
            curController.stop();
        }

        SimpleTimerController simpleTimerController =
                new SimpleTimerController();
        p = new SimpleTimerPanel(simpleTimerController);

        curController = simpleTimerController;

        return p;
    }

    private SimpleStopwatchPanel createNewSimpleStopwatchPanel() {
        SimpleStopwatchPanel p;

        if(curController != null) {
            curController.stop();
        }

        SimpleStopwatchController simpleStopwatchController =
                new SimpleStopwatchController();
        p = new SimpleStopwatchPanel(simpleStopwatchController);

        curController = simpleStopwatchController;

        return p;
    }

    private void setupMenus() {
        /* Menus. */
        JMenu fileMenu = new JMenu("File");
        JMenu optionsMenu = new JMenu("Options");
        final JMenu savedMenu = new JMenu("Saved");

        /* Items in 'File' menu. */
        JMenuItem newWindowMenuItem = new JMenuItem("New window");
        JMenuItem closeWindowMenuItem = new JMenuItem("Close window");
        JMenuItem saveTimerItem = new JMenuItem("Save timer");
        final JMenu deleteSavedTimerMenu = new JMenu("Delete saved timer");

        /* Items in 'Options' menu. */
        JRadioButtonMenuItem timerOption = new JRadioButtonMenuItem("Timer");
        JRadioButtonMenuItem stopwatchOption =
                new JRadioButtonMenuItem("Stopwatch");

        final ActionListener savedMenuItemActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if(source instanceof JMenuItem) {
                    try {
                        loadSavedTimer(((JMenuItem) source).getText());
                    } catch(BackingStoreException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                else {
                    throw new RuntimeException("Unexpected source for menu " +
                            "event: " + source);
                }
            }
        };

        final ActionListener deleteSavedTimerMenuItemActionListener =
            new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if(source instanceof JMenuItem) {
                    try {
                        deleteSavedTimer(((JMenuItem) source).getText());
                        setupSavedTimersMenu(savedMenu,
                                savedMenuItemActionListener);
                        setupSavedTimersMenu(deleteSavedTimerMenu, this);
                    } catch(BackingStoreException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                else {
                    throw new RuntimeException("Unexpected source for menu " +
                            "event: " + source);
                }
            }
        };

        newWindowMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                (new SimpleTimer()).setVisible(true);
            }
        });
        newWindowMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        fileMenu.add(newWindowMenuItem);

        closeWindowMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(curController != null) {
                    curController.stop();
                }

                SimpleTimer.this.dispose();
            }
        });
        closeWindowMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        fileMenu.add(closeWindowMenuItem);

        saveTimerItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!(mainPanel instanceof SimpleTimerPanel)) {
                    return;
                }

                String name = JOptionPane.showInputDialog(SimpleTimer.this,
                        "Please enter the name of the saved timer",
                        "Timer name", JOptionPane.PLAIN_MESSAGE);
                if(name != null) {
                    try {
                        saveTimer(name, (SimpleTimerPanel) mainPanel);
                        setupSavedTimersMenu(savedMenu,
                                savedMenuItemActionListener);
                        setupSavedTimersMenu(deleteSavedTimerMenu,
                                deleteSavedTimerMenuItemActionListener);
                    } catch(BackingStoreException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        fileMenu.add(saveTimerItem);

        setupSavedTimersMenu(deleteSavedTimerMenu,
                deleteSavedTimerMenuItemActionListener);

        fileMenu.add(deleteSavedTimerMenu);

        timerOption.setSelected(true);
        timerOption.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                    if(!(mainPanel instanceof SimpleTimerPanel)) {
                        mainPanel = createNewSimpleTimerPanel();
                        getContentPane().removeAll();
                        getContentPane().add(mainPanel);
                        pack();
                    }
		}
	    });
        timerOption.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        stopwatchOption.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                    if(!(mainPanel instanceof SimpleStopwatchPanel)) {
                        mainPanel = createNewSimpleStopwatchPanel();
                        getContentPane().removeAll();
                        getContentPane().add(mainPanel);
                        pack();
                    }
		}
	    });
        stopwatchOption.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

	ButtonGroup styleOptionGroup = new ButtonGroup();
        styleOptionGroup.add(timerOption);
        styleOptionGroup.add(stopwatchOption);

        optionsMenu.add(timerOption);
        optionsMenu.add(stopwatchOption);

        setupSavedTimersMenu(savedMenu, savedMenuItemActionListener);

	JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(optionsMenu);
        menuBar.add(savedMenu);
	setJMenuBar(menuBar);
    }

    private static interface StoppableController {
        public void stop();
    };

    private class SimpleTimerController
            implements SimpleTimerPanel.Controller, StoppableController
    {
        private boolean cancel;
        private boolean startMode;

        public SimpleTimerController() {
            cancel = false;
            startMode = true;
        }

        public void setAllEnabled(SimpleTimerPanel p, boolean b) {
            p.setHoursFieldEnabled(b);
            p.setMinutesFieldEnabled(b);
            p.setSecondsFieldEnabled(b);
        }

        public void startStopConfirmButtonActionPerformed(
                final SimpleTimerPanel p)
        {
            final Object syncObject = this;
            final boolean isStart;

            synchronized(syncObject) {
                if(startMode) {
                    isStart = true;
                    startMode = false;
                }
                else {
                    isStart = false;
                }
            }

            if(isStart) {
                p.setControlButtonText("Stop");
                setAllEnabled(p, false);
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + 1000 * (
                        Integer.parseInt(p.getHoursFieldText()) * 3600 +
                        Integer.parseInt(p.getMinutesFieldText()) * 60 +
                        Integer.parseInt(p.getSecondsFieldText()));
                SwingWorker sw = new SwingWorker() {
                    private void reportTime(long secondsLeft) {
                        setTitle("SimpleTimer (" + secondsLeft + " seconds " +
                                "left)");
                        p.setStatusLabelText("SimpleTimer (" +
                                secondsToHMSString(secondsLeft) + " seconds " +
                                "left)");
                    }

                    public Object construct() {
                        long currentTime = startTime;
                        long oldSecondsLeft = -1;
                        synchronized(syncObject) {
                            while(currentTime < endTime && !cancel) {
                                final long secondsLeft =
                                        (endTime-currentTime-1)/1000 + 1;
                                if(secondsLeft != oldSecondsLeft) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            reportTime(secondsLeft);
                                        }
                                    });
                                    oldSecondsLeft = secondsLeft;
                                }
                                try { syncObject.wait(100); }
                                catch(Exception e) {}
                                currentTime = System.currentTimeMillis();
                            }
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    setTitle("SimpleTimer");

                                    if(MacUtils.isMac()) {
                                        MacUtils.requestUserAttention(true);
                                    }
                                }
                            });
                            if(!cancel) {
                                short count = 0;
                                while(!cancel) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            long secondsSinceTimeout =
                                                    (System.currentTimeMillis() -
                                                    endTime) / 1000;
                                            p.setStatusLabelText("Timed out " +
                                                    "(+" +
                                                    secondsSinceTimeout + " " +
                                                    "s). Waiting for " +
                                                    "user to confirm...");
                                            p.setControlButtonText("Confirm");
                                        }
                                    });

                                    if(count != 1 && count != 4) {
                                        Toolkit.getDefaultToolkit().beep();
                                    }

                                    try {
                                        syncObject.wait(250);
                                    }
                                    catch(InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    count = (short) ((count + 1) % 5);
                                }
                            }

                            p.setControlButtonText("Start");
                            setAllEnabled(p, true);
                            long secondsSinceTimeout =
                                    (System.currentTimeMillis() - endTime) /
                                    1000;
                            p.setStatusLabelText("Timer stopped at " +
                                    (secondsSinceTimeout >= 0 ? "+" : "") +
                                    secondsSinceTimeout + " s.");
                            startMode = true;

                            cancel = false;
                            syncObject.notifyAll();
                        }

                        return null;
                    }
                };
                sw.start();
            }
            else {
                stop();
            }
        }

        public void stop() {
            Object syncObject = this;

            synchronized(syncObject) {
                if(!startMode) {
                    cancel = true;
                    syncObject.notifyAll();

                    while(cancel) {
                        try {
                            syncObject.wait();
                        }
                        catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private class SimpleStopwatchController
            implements SimpleStopwatchPanel.Controller, StoppableController
    {
        private boolean cancel;
        private boolean startMode;

        public SimpleStopwatchController() {
            cancel = false;
            startMode = true;
        }

        public void startStopButtonActionPerformed(final SimpleStopwatchPanel p)
        {
            final Object syncObject = this;
            final boolean isStart;

            synchronized(syncObject) {
                if(startMode) {
                    isStart = true;
                    startMode = false;
                }
                else {
                    isStart = false;
                }
            }

            if(isStart) {
                final long startTime = System.currentTimeMillis();

                p.setRunning(true);

                SwingWorker sw = new SwingWorker() {
                    private void reportTime(long tenthsOfSeconds,
                            boolean updateTitle)
                    {
                        final long seconds = tenthsOfSeconds / 10;

                        if(updateTitle) {
                            setTitle("SimpleTimer (" + seconds + " " +
                                    "seconds)");
                        }

                        p.setTimeLabelText(seconds + "." +
                                (tenthsOfSeconds % 10) + " seconds");
                    }

                    public Object construct() {
                        long currentTime = startTime;
                        long oldTenthsOfSeconds = -1;
                        long oldSeconds = -1;

                        synchronized(syncObject) {
                            while(!cancel) {
                                final long tenthsOfSeconds =
                                        (currentTime - startTime) / 100;
                                final long seconds =
                                        tenthsOfSeconds / 10;

                                if(tenthsOfSeconds != oldTenthsOfSeconds) {
                                    final boolean updateTitle =
                                            seconds != oldSeconds;
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            reportTime(tenthsOfSeconds,
                                                    updateTitle);
                                        }
                                    });

                                    oldTenthsOfSeconds = tenthsOfSeconds;
                                    oldSeconds = seconds;
                                }

                                try { syncObject.wait(25); }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }

                                currentTime = System.currentTimeMillis();
                            }

                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    setTitle("SimpleTimer");
                                }
                            });

                            p.setRunning(false);
                            startMode = true;

                            cancel = false;
                            syncObject.notifyAll();
                        }

                        return null;
                    }
                };

                sw.start();
            }
            else {
                stop();
            }
        }

        public void stop() {
            final Object syncObject = this;

            synchronized(syncObject) {
                if(!startMode) {
                    cancel = true;
                    syncObject.notifyAll();

                    while(cancel) {
                        try {
                            syncObject.wait();
                        }
                        catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static String secondsToHMSString(long seconds) {
	long minutes = seconds / 60;
	long hours = minutes / 60;
	minutes -= hours*60;
	seconds -= (hours*60*60 + minutes*60);

        String result;
        if(hours > 0) {
            result = hours + "h " +  minutes + "m " + seconds + "s";
        }
        else if(minutes > 0) {
            result = minutes + "m " + seconds + "s";
        }
        else {
            result = seconds + "s";
        }

	return result;
    }


    private static Preferences lookupTimersNode(boolean createNodesIfMissing)
            throws BackingStoreException
    {
        Preferences p = Preferences.userNodeForPackage(SimpleTimer.class);

        /* Look up the 'saved' node. */
        if(!createNodesIfMissing && !p.nodeExists("saved")) {
            return null;
        }

        p = p.node("saved");

        /* Look up the 'timers' node. */
        if(!createNodesIfMissing && !p.nodeExists("timers")) {
            return null;
        }

        return p.node("timers");
    }

    private static void setupSavedTimersMenu(JMenu savedMenu, ActionListener al)
    {
        savedMenu.removeAll();

        try {
            Preferences p = lookupTimersNode(false);
            if(p == null) {
                return;
            }

            /* Populate the menu with the names of the saved timers. */
            for(String s : p.childrenNames()) {
                JMenuItem curItem = new JMenuItem(s);
                curItem.addActionListener(al);
                savedMenu.add(curItem);
            }
        } catch(BackingStoreException bse) {
            bse.printStackTrace();
        }
    }

    private void loadSavedTimer(String name) throws BackingStoreException {
        Preferences p = Preferences.userNodeForPackage(SimpleTimer.class);

        /* Look up the 'saved' node. */
        if(!p.nodeExists("saved")) {
            throw new RuntimeException("No 'saved' node in package user node.");
        }

        p = p.node("saved");

        /* Look up the 'timers' node. */
        if(!p.nodeExists("timers")) {
            throw new RuntimeException("No 'timers' node in 'saved' node.");
        }

        p = p.node("timers");

        /* Look up the named node that we are attempting to load. */
        if(!p.nodeExists(name)) {
            throw new RuntimeException("No '" + name + "' node in 'timers' " +
                    "node.");
        }

        p = p.node(name);

        /* Create new timer with default data from the named node. */
        SimpleTimer newTimer = new SimpleTimer();
        SimpleTimerPanel panel = (SimpleTimerPanel) newTimer.mainPanel;
        panel.setHoursFieldText("" + p.getLong("hours", 0));
        panel.setMinutesFieldText("" + p.getLong("minutes", 0));
        panel.setSecondsFieldText("" + p.getLong("seconds", 0));
        panel.setDescriptionAreaText(p.get("description", ""));
        newTimer.setVisible(true);
    }

    private static void saveTimer(String name, SimpleTimerPanel panel)
            throws BackingStoreException
    {
        Preferences p = lookupTimersNode(true);

        /* Get or create the named node. */
        p = p.node(name);

        p.putLong("hours", Long.parseLong(panel.getHoursFieldText()));
        p.putLong("minutes", Long.parseLong(panel.getMinutesFieldText()));
        p.putLong("seconds", Long.parseLong(panel.getSecondsFieldText()));
        p.put("description", panel.getDescriptionAreaText());
        p.flush();
    }

    private static void deleteSavedTimer(String name)
            throws BackingStoreException
    {
        Preferences p = lookupTimersNode(false);

        if(p != null && p.nodeExists(name)) {
            p = p.node(name);

            p.removeNode();
            p.flush();
        }
        else {
            throw new RuntimeException("Unexpected: Node '" + name + "' " +
                    "disappeared behind our backs.");
        }
    }

    public static void main(String[] args) {
        if(System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

       	try {
	    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
	    /*
	     * Beskrivning av olika look&feels:
	     *   http://java.sun.com/docs/books/tutorial/uiswing/misc/plaf.html
	     */
	}
	catch(Exception e) {
	    //Gick det inte? jaja, ingen fara
	}
	(new SimpleTimer()).setVisible(true);
    }
}

/**
 * This is the 3rd version of SwingWorker (also known as
 * SwingWorker 3), an abstract class that you subclass to
 * perform GUI-related work in a dedicated thread.  For
 * instructions on and examples of using this class, see:
 * 
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 *
 * Note that the API changed slightly in the 3rd version:
 * You must now invoke start() on the SwingWorker after
 * creating it.
 */
abstract class SwingWorker {
    private Object value;  // see getValue(), setValue()

    /** 
     * Class to maintain reference to current worker thread
     * under separate synchronization control.
     */
    private static class ThreadVar {
        private Thread thread;
        ThreadVar(Thread t) { thread = t; }
        synchronized Thread get() { return thread; }
        synchronized void clear() { thread = null; }
    }

    private ThreadVar threadVar;

    /** 
     * Get the value produced by the worker thread, or null if it 
     * hasn't been constructed yet.
     */
    protected synchronized Object getValue() { 
        return value; 
    }

    /** 
     * Set the value produced by worker thread 
     */
    private synchronized void setValue(Object x) { 
        value = x; 
    }

    /** 
     * Compute the value to be returned by the <code>get</code> method. 
     */
    public abstract Object construct();

    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    public void finished() {
    }

    /**
     * A new method that interrupts the worker thread.  Call this method
     * to force the worker to stop what it's doing.
     */
    public void interrupt() {
        Thread t = threadVar.get();
        if (t != null) {
            t.interrupt();
        }
        threadVar.clear();
    }

    /**
     * Return the value created by the <code>construct</code> method.  
     * Returns null if either the constructing thread or the current
     * thread was interrupted before a value was produced.
     * 
     * @return the value created by the <code>construct</code> method
     */
    public Object get() {
        while (true) {  
            Thread t = threadVar.get();
            if (t == null) {
                return getValue();
            }
            try {
                t.join();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // propagate
                return null;
            }
        }
    }


    /**
     * Start a thread that will call the <code>construct</code> method
     * and then exit.
     */
    public SwingWorker() {
        final Runnable doFinished = new Runnable() {
           public void run() { finished(); }
        };

        Runnable doConstruct = new Runnable() { 
            public void run() {
                try {
                    setValue(construct());
                }
                finally {
                    threadVar.clear();
                }

                SwingUtilities.invokeLater(doFinished);
            }
        };

        Thread t = new Thread(doConstruct);
        threadVar = new ThreadVar(t);
    }

    /**
     * Start the worker thread.
     */
    public void start() {
        Thread t = threadVar.get();
        if (t != null) {
            t.start();
        }
    }
}
