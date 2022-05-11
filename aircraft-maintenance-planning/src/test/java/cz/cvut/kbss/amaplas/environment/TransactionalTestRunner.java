package cz.cvut.kbss.amaplas.environment;

import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

public abstract class TransactionalTestRunner {

    @Autowired
    protected PlatformTransactionManager txManager;

    protected void transactional(Runnable procedure) {
        Transaction.execute(txManager, procedure);
    }

    protected void enableRdfsInference(EntityManager em) {
        transactional(() -> Environment.addModelStructureForRdfsInference(em));
    }
}
