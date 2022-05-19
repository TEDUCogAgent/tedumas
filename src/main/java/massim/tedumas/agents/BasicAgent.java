package massim.tedumas.agents;

import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Percept;
import massim.eismassim.EnvironmentInterface;
import massim.tedumas.MailService;
import massim.tedumas.TEDUPercept;
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
import java.util.Random;

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

    //For the one choosen task to be completed
    String nameOfTheTask;
    String deadlineOfTheTask;
    String rewardOfTheTask;
    String[] requirementsOfTheTask = new String[4];


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

        classExtent = (IPersistentSet) root.get("TEDUPercept");
        if (classExtent == null) {
            classExtent = db.createSet(); // create class extent
            root.put("TEDUPercept", classExtent);
        }

        KServices = KieServices.Factory.get();
        KContainer = KServices.getKieClasspathContainer();
        KSession = KContainer.newKieSession("KSession" + name);

        KSession.setGlobal("eis", ei);
        KSession.setGlobal("logger", logger);
        KSession.setGlobal("agent", this);

        int previousPerceptID = -1;
        int currentPerceptID = 0;
        // iterator through all instance of the class
        Iterator i = classExtent.iterator();
        while (i.hasNext()) {
            TEDUPercept tpercept = (TEDUPercept) i.next();
            currentPerceptID = tpercept.stepID;
            if (previousPerceptID != currentPerceptID) {
                logger.info("##### " + this.getName() + " #####");
                previousPerceptID = currentPerceptID;
            }
            KSession.insert(tpercept);
            logger.info(tpercept);
        }
    }

    @Override
    public void handlePercept(Percept percept) {
    }

    @Override
    public void handleMessage(Percept message, String sender) {
    }

    @Override
    public Action step(int stepCount) {
        List<Percept> percepts = getPercepts();
        logger.info("**********  " + this.getName() + "  **********");


        for (Percept percept : percepts) {
            TEDUPercept tPercept = new TEDUPercept();
            tPercept.percept = percept;
            tPercept.timeStamp = (new Date()).getTime();
            tPercept.stepID = stepCount;

            //if(tPercept.percept.getName().equals("lastAction"))
            //System.out.println(tPercept.percept.getParameters().toString());
            /*System.out.println("aaaaaaaa" + percept.getName() + " " + percept.getParameters().toString());
            System.out.println("bbbbbbbb" + percept.getName() + " " + percept.getParameters().isEmpty());
            System.out.println("cccccccc" + percept.getName() + " " + percept.getParameters().size());
            if (percept.getName().equals("lastActionResult"))
                System.out.println("dddddddd" + percept.getName() + " " + percept.getParameters());*/

            classExtent.add(tPercept);
            logger.info(tPercept);
            KSession.insert(tPercept);

            /* Default code which comes from massim javaagents project
            if (percept.getName().equals("actionID")) {
                Parameter param = percept.getParameters().get(0);
                if (param instanceof Numeral) {
                    int id = ((Numeral) param).getValue().intValue();
                    if (id > lastID) {
                        lastID = id;
                        return new Action("move", new Identifier("n"));
                    }
                }
            }*/
        }
        // !!!Important!!!
        // If this is commented, persistence does not work
        //db.commit();
        KSession.fireAllRules();
        return null;
    }

    public String generateRandomDirection() {
        int i = (new Random()).nextInt(4);
        return switch (i) {
            case 1 -> "s";
            case 2 -> "e";
            case 3 -> "w";
            default -> "n";
        };
    }

    public StringBuilder routeTowardsDispenser(Percept percept, TEDUPercept teduPercept){
        StringBuilder route = new StringBuilder();
        int x = Integer.parseInt(percept.getParameters().get(0).toString());
        int y = Integer.parseInt(percept.getParameters().get(1).toString());

        while(x!=0){
            if(x<0){
                route.append("w");
                x++;
            }
            else {
                route.append("e");
                x--;
            }
        }
        while(y!=0){
            if(y<0){
                route.append("n");
                y++;
            }
            else {
                route.append("s");
                y--;
            }
        }
        teduPercept.route = route;
        return route;
    }


    public void parseTaskAndAssign(Percept percept){
        //[task0,195,160,[req(-1,2,b1),req(0,1,b2),req(0,2,b1),req(0,3,b2)]]
        nameOfTheTask = percept.getParameters().toString().substring(1,6);
        deadlineOfTheTask = percept.getParameters().toString().substring(8,11);
        rewardOfTheTask = percept.getParameters().toString().substring(13,16);
        requirementsOfTheTask[0] = percept.getParameters().toString().substring(23,30);
        requirementsOfTheTask[1] = percept.getParameters().toString().substring(36,42);
        requirementsOfTheTask[2] = percept.getParameters().toString().substring(48,54);
        requirementsOfTheTask[3] = percept.getParameters().toString().substring(60,66);
        //logger.info(nameOfTheTask + " " + deadlineOfTheTask + " " + rewardOfTheTask + " " + requirementsOfTheTask[0] + " " + requirementsOfTheTask[1] + " " + requirementsOfTheTask[2] + " " + requirementsOfTheTask[3]);
    }

}
