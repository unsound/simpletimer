package org.catacombae.simpletimer;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
        JMenu fileMenu = new JMenu("File");

        JMenuItem newWindowMenuItem = new JMenuItem("New window");
        newWindowMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                (new SimpleTimer()).setVisible(true);
            }
        });
        newWindowMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        fileMenu.add(newWindowMenuItem);

        JMenuItem closeWindowMenuItem = new JMenuItem("Close window");
        closeWindowMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SimpleTimer.this.dispose();
            }
        });
        closeWindowMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        fileMenu.add(closeWindowMenuItem);

        JRadioButtonMenuItem timerOption = new JRadioButtonMenuItem("Timer");
        timerOption.setSelected(true);
        timerOption.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                    if(!(mainPanel instanceof SimpleTimerPanel)) {
                        System.err.println("Switching to timer...");
                        mainPanel = createNewSimpleTimerPanel();
                        /* Stop whatever the other guy is doing... */
                        getContentPane().removeAll();
                        getContentPane().add(mainPanel);
                        pack();
                    }
		}
	    });
        timerOption.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        JRadioButtonMenuItem stopwatchOption =
                new JRadioButtonMenuItem("Stopwatch");
        stopwatchOption.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                    if(!(mainPanel instanceof SimpleStopwatchPanel)) {
                        System.err.println("Switching to stopwatch...");
                        mainPanel = createNewSimpleStopwatchPanel();
                        /* Stop whatever the other guy is doing... */
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

	JMenu optionsMenu = new JMenu("Options");
        optionsMenu.add(timerOption);
        optionsMenu.add(stopwatchOption);

	JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(optionsMenu);
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
                                }
                            });
                            if(!cancel) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        p.setStatusLabelText("Timed out. " +
                                                "Waiting for user to " +
                                                "confirm...");
                                        p.setControlButtonText("Confirm");
                                    }
                                });
                                short count = 0;
                                while(!cancel) {
                                    Toolkit.getDefaultToolkit().beep();
                                    try {
                                        final long waitInterval =
                                                ((count % 3) != 1) ? 500 : 250;
                                        syncObject.wait(waitInterval);
                                    }
                                    catch(InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    ++count;
                                }
                            }

                            p.setControlButtonText("Start");
                            setAllEnabled(p, true);
                            p.setStatusLabelText("Timer stopped");
                            startMode = true;

                            cancel = false;
                            syncObject.notifyAll();
                        }
                        //setAllEnabled(true);
                        return null;
                    }
                };
                sw.start();
                //setAllEnabled(true);
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
