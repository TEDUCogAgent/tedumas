package massim.tedumas.agents;

import eis.iilang.*;
import massim.eismassim.EnvironmentInterface;
import massim.tedumas.MailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.garret.perst.IPersistentSet;
import org.garret.perst.Index;
import org.garret.perst.Storage;
import org.garret.perst.StorageFactory;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.Iterator;
import java.util.List;

/**
 * A very basic agent.
 */
public class BasicAgent extends Agent {
    private static final Logger
            logger = LogManager.getLogger(BasicAgent.class);
    private int lastID = -1;
    KieServices KServices;
    KieContainer KContainer;
    KieBase KBase;
    KieSession KSession;
    Storage db;
    Index root;
    IPersistentSet classExtent;

    /**
     * Constructor.
     *
     * @param name    the agent's name
     * @param mailbox the mail facility
     */
    public BasicAgent(String name, MailService mailbox, EnvironmentInterface ei) {
        super(name, mailbox);

        db = StorageFactory.getInstance().createStorage();
        db.open(".\\db\\" + name + ".dbs", Storage.DEFAULT_PAGE_POOL_SIZE);

        root = (Index) db.getRoot(); // get storage root
        if (root == null) {
            // Root is not yet defined: storage is not initialized
            root = db.createIndex(String.class, // key type
                    true); // unique index
            db.setRoot(root);
        }

        classExtent = (IPersistentSet) root.get("eis.iilang.Percept");
        if (classExtent == null) {
            classExtent = db.createSet(); // create class extent
            root.put("eis.iilang.Percept", classExtent);
        }

        KServices = KieServices.Factory.get();
        KContainer = KServices.getKieClasspathContainer();
        KSession = KContainer.newKieSession("KSession" + name);
        logger.info("##### " + this.getName() + " #####");
        // iterator through all instance of the class
        Iterator i = classExtent.iterator();
        while (i.hasNext()) {
            Percept percept = (Percept) i.next();
            logger.info(percept);
            KSession.insert(percept);
            if (percept.getName().equals("Step_End"))
                logger.info("##### " + this.getName() + " #####");
        }
        KSession.setGlobal("eis",ei);
        //KSession.fireAllRules();
    }

    @Override
    public void handlePercept(Percept percept) {
    }

    @Override
    public void handleMessage(Percept message, String sender) {
    }

    @Override
    public Action step() {
        List<Percept> percepts = getPercepts();
        logger.info("**********  " + this.getName() + "  **********");


        for (Percept percept : percepts) {
            logger.info(percept);
            KSession.insert(percept);
            KSession.fireAllRules();
            classExtent.add(percept);
        }

        Percept perceptTemp = new Percept("Step_End");
        logger.info(perceptTemp);
        KSession.insert(perceptTemp);
        classExtent.add(perceptTemp);
        db.commit();
        for (Percept percept : percepts) {
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
