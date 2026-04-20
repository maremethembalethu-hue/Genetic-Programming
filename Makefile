JAVAC = javac
JAVA = java

MAIN_GP = EnergyPredictGP
MAIN_SBGP = EnergyPredictSBGP

SRC = EnergyPredictGP.java EnergyPredictSBGP.java Node.java NodePt.java

compile:
	$(JAVAC) $(SRC)

run-gp:
	@echo Running Standard GP (EnergyPredictGP)...
	$(JAVA) $(MAIN_GP)

run-sbgp:
	@echo Running Structural-Based GP (EnergyPredictSBGP)...
	$(JAVA) $(MAIN_SBGP)

clean:
	rm -f *.class