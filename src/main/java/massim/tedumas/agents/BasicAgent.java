package massim.tedumas.agents;

import eis.iilang.Action;
import eis.iilang.Percept;
import massim.eismassim.EnvironmentInterface;
import massim.tedumas.MailService;
import massim.tedumas.PerstPercept;
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
import java.util.Date;
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

        classExtent = (IPersistentSet) root.get("PerstPercept");
        if (classExtent == null) {
            classExtent = db.createSet(); // create class extent
            root.put("PerstPercept", classExtent);
        }

        KServices = KieServices.Factory.get();
        KContainer = KServices.getKieClasspathContainer();
        KSession = KContainer.newKieSession("KSession" + name);

        KSession.setGlobal("eis", ei);

        // iterator through all instance of the class
        Iterator i = classExtent.iterator();
        while (i.hasNext()) {
            logger.info("##### " + this.getName() + " #####");
            PerstPercept percepts = (PerstPercept) i.next();
            KSession.insert(percepts.percpt);
            logger.info(percepts.percpt);
        }
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

        PerstPercept pprcpt = new PerstPercept();
        pprcpt.percpt = percepts;
        pprcpt.tmstmp = (new Date()).getTime();
        classExtent.add(pprcpt);

        // !!!Important!!!
        // If this is not uncommented persistence does not work
        //db.commit();

        logger.info(percepts);
        KSession.insert(percepts);
        KSession.fireAllRules();
/*
        //Default code which comes from massim javaagents project
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
 */
        return null;
    }
}
