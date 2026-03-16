JAVAC = javac
JAVA = java

MAIN = EnergyPredictGP
SRC = EnergyPredictGP.java Node.java NodePt.java


compile:
	$(JAVAC) $(SRC)

run:
	$(JAVA) $(MAIN)

clean:
	rm -f *.class