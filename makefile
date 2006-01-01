CC = javac              # kompilator
FLAGS = #-source 1.4      # flaggor
SRCFILES = *.java
RM = del
RMFILES = *.class

simpletimer: 
	$(RM) $(RMFILES)
	$(CC) $(FLAGS) $(SRCFILES)
