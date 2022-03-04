package massim.tedumas.agents;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import eis.iilang.*;
import massim.eismassim.EnvironmentInterface;
import massim.tedumas.MailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.drools.persistence.api.PersistenceContextManager;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import javax.persistence.Persistence;
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
    private PersistenceContextManager jpm;
    /**
     * Constructor.
     * @param name    the agent's name
     * @param mailbox the mail facility
     */
    public BasicAgent(String name, MailService mailbox, EnvironmentInterface ei) {
        super(name, mailbox);

        PoolingDataSource ds = new PoolingDataSource();
        ds.setUniqueName( "jdbc/DS" + name );
        ds.setClassName( "org.h2.jdbcx.JdbcDataSource" );
        ds.setMaxPoolSize( 3 );
        ds.setAllowLocalTransactions( true );
        ds.getDriverProperties().put( "user", "sa" );
        ds.getDriverProperties().put( "password", "sasa" );
        ds.getDriverProperties().put( "URL", "jdbc:h2:file:./jdbc/DB" + name );
        ds.init();

        KServices = KieServices.Factory.get();

        Environment env = KServices.newEnvironment();
        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
                Persistence.createEntityManagerFactory( "Persist" + name));
        env.set( EnvironmentName.TRANSACTION_MANAGER,
                TransactionManagerServices.getTransactionManager() );

        KContainer = KServices.getKieClasspathContainer();
        KieStoreServices kstore = KServices.getStoreServices();
        KSession = kstore.newKieSession(KContainer.getKieBase("KBase" + name), null, env);
        KSession.setGlobal("eis", ei);
        KSession.fireAllRules();

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
