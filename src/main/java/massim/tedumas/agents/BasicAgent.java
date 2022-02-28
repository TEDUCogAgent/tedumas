package massim.tedumas.agents;

import eis.iilang.*;
import massim.eismassim.EnvironmentInterface;
import massim.tedumas.MailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.List;

/**
 * A very basic agent.
 */
public class BasicAgent extends Agent {
    private static final Logger
            logger = LogManager.getLogger(BasicAgent.class);
    private int lastID = -1;
    KieServices kieServices;
    KieContainer kContainer;
    KieBase KBase;
    KieSession KSession;
    /**
     * Constructor.
     * @param name    the agent's name
     * @param mailbox the mail facility
     */
    public BasicAgent(String name, MailService mailbox, EnvironmentInterface ei) {
        super(name, mailbox);
        kieServices = KieServices.Factory.get();
        kContainer = kieServices.getKieClasspathContainer();

        KBase = kContainer.getKieBase("KBase" + name);
        KSession = kContainer.newKieSession("KSession" + name);
        KSession.setGlobal("eis", ei);
    }

    @Override
    public void handlePercept(Percept percept) {}

    @Override
    public void handleMessage(Percept message, String sender) {}

    @Override
    public Action step() {
        List<Percept> percepts = getPercepts();
        logger.info("**********  " + this.getName() + "  **********");
        for (Percept percept : percepts) {
            logger.info(percept);
            KSession.insert(percept);
            KSession.fireAllRules();
            if (percept.getName().equals("actionID")) {
                Parameter param = percept.getParameters().get(0);
                if (param instanceof Numeral) {
                    int id = ((Numeral) param).getValue().intValue();
                    if (id > lastID) {
                        lastID = id;
                        return new Action("move", new Identifier("n"));
                    }
                }
            }
        }
        return null;
    }
}
