package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.model.MaintenanceGroup;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.QueryResultMapper;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.cache.annotation.Cacheable;

import java.net.URI;
import java.util.List;

public class MaintenanceGroupDao extends BaseDao<MaintenanceGroup> {

    public static final String MAINTENANCE_GROUP = "queries/analysis/maintenance-group.sparql";

    protected QueryResultMapper<MaintenanceGroup> groupQueryResultMapper =
            new QueryResultMapper<>(MAINTENANCE_GROUP) {
        @Override
        public MaintenanceGroup convert() {
            MaintenanceGroup maintenanceGroup = new MaintenanceGroup();
            mandatory("group", URI::create, maintenanceGroup::setEntityURI);
            mandatory("abbreviation", maintenanceGroup::setAbbreviation);
            return maintenanceGroup;
        }
    };

    public MaintenanceGroupDao(EntityManager em, Rdf4JDao rdf4JDao) {
        super(MaintenanceGroup.class, em, rdf4JDao);
    }

    @Cacheable
    public List<MaintenanceGroup> findAll(){
        return load(groupQueryResultMapper, null);
    }


}
