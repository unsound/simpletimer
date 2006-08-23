import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;

public class SimpleTimer extends JFrame {
    private boolean cancel, startMode;
    private JPanel backgroundPanel;
    private JPanel northPanel;
    private JPanel middlePanel;
    private JPanel southPanel;
    private JLabel statusLabel;
    private JLabel hoursLabel;
    private JLabel minutesLabel;
    private JLabel secondsLabel;
    private JTextField hoursField;
    private JTextField minutesField;
    private JTextField secondsField;
    private JButton startStopConfirmButton;
    //private JButton cancelButton;
    
    public SimpleTimer() {
	super("SimpleTimer");
	cancel = false;
	startMode = true;

	backgroundPanel = new JPanel();
	northPanel = new JPanel();
	middlePanel = new JPanel();
	southPanel = new JPanel();
	statusLabel = new JLabel("Timer stopped");
	hoursLabel = new JLabel("Hours:");
	minutesLabel = new JLabel("Minutes:");
	secondsLabel = new JLabel("Seconds:");
	hoursField = new JTextField(3);
	minutesField = new JTextField(2);
	secondsField = new JTextField(2);
	startStopConfirmButton = new JButton("Start");
	//cancelButton = new JButton("Cancel");
	
	backgroundPanel.setLayout(new BorderLayout());
	backgroundPanel.setBorder(new javax.swing.border.EmptyBorder(5, 5, 5, 5));
	northPanel.setLayout(new FlowLayout());
	southPanel.setLayout(new FlowLayout());
	hoursField.setText("0");
	minutesField.setText("0");
	secondsField.setText("0");
	
	startStopConfirmButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    startStopConfirmButtonActionPerformed();
		}
	    });
	/*
	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    cancel = true;
		    setAllEnabled(true);
		    statusLabel.setText("Timer stopped");
		}
	    });
	*/
	
	northPanel.add(hoursLabel);
	northPanel.add(hoursField);
	northPanel.add(minutesLabel);
	northPanel.add(minutesField);
	northPanel.add(secondsLabel);
	northPanel.add(secondsField);
	
	middlePanel.add(statusLabel);
	
	southPanel.add(startStopConfirmButton);
	//southPanel.add(cancelButton);
	
	backgroundPanel.add(middlePanel, BorderLayout.NORTH);
	backgroundPanel.add(northPanel, BorderLayout.CENTER);
	backgroundPanel.add(southPanel, BorderLayout.SOUTH);
	
	setDefaultCloseOperation(EXIT_ON_CLOSE);
	getContentPane().add(backgroundPanel);
	pack();
	setLocationRelativeTo(null);
    }

    public void setAllEnabled(boolean b) {
	hoursField.setEnabled(b);
	minutesField.setEnabled(b);
	secondsField.setEnabled(b);
	//startStopConfirmButton.setEnabled(b);
	//cancelButton.setEnabled(b);
    }
    public void startStopConfirmButtonActionPerformed() {
	final SimpleTimer thisObject = this;
	if(startMode) {
	    startMode = false;
	    startStopConfirmButton.setText("Stop");
	    setAllEnabled(false);
	    final long startTime = System.currentTimeMillis();
	    final long endTime = startTime + 1000*(Integer.parseInt(hoursField.getText())*3600 + 
						   Integer.parseInt(minutesField.getText())*60 + 
						   Integer.parseInt(secondsField.getText()));
	    SwingWorker sw = new SwingWorker() {
		    public Object construct() {
			long currentTime = startTime;
			long oldSecondsLeft = -1;
			synchronized(thisObject) {
			    while(currentTime < endTime && !cancel) {
				final long secondsLeft = (endTime-currentTime-1)/1000 + 1;
				if(secondsLeft != oldSecondsLeft) {
				    SwingUtilities.invokeLater(new Runnable() {
					    public void run() {
						setTitle("SimpleTimer (" + secondsLeft + " seconds left)");
						statusLabel.setText("SimpleTimer (" + secondsToHMSString(secondsLeft) + " seconds left)");
					    }});
				    oldSecondsLeft = secondsLeft;
				}
				try { thisObject.wait(100); }
				catch(Exception e) {}
				currentTime = System.currentTimeMillis();
			    }
			    SwingUtilities.invokeLater(new Runnable() {
				    public void run() {
					setTitle("SimpleTimer");
				    }});
			    if(!cancel) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					    statusLabel.setText("Timed out. Waiting for user to confirm...");
					    startStopConfirmButton.setText("Confirm");
					}
				    });
				short count = 0;
				while(!cancel) {
				    Toolkit.getDefaultToolkit().beep();
				    try { thisObject.wait((count%3!=1)?500:250); }
				    catch(InterruptedException e) {
					e.printStackTrace();
				    }
				    ++count;
				}
			    }
			    cancel = false;
			}
			//setAllEnabled(true);
			return null;
		    }
		};
	    sw.start();
	    //setAllEnabled(true);
	}
	else {
	    startMode = true;
	    startStopConfirmButton.setText("Start");
	    synchronized(thisObject) {
		cancel = true;
		thisObject.notifyAll();
	    }
	    setAllEnabled(true);
	    statusLabel.setText("Timer stopped");
	}
    }
    
    private static String secondsToHMSString(long seconds) {
	long minutes = seconds / 60;
	long hours = minutes / 60;
	minutes -= hours*60;
	seconds -= (hours*60*60 + minutes*60);
	String result;
	if(hours > 0) result = hours + "h " +  minutes + "m " + seconds + "s";
	else if(minutes > 0) result = minutes + "m " + seconds + "s";
	else result = seconds + "s";
	return result;
    }
    
    public static void main(String[] args) {
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
