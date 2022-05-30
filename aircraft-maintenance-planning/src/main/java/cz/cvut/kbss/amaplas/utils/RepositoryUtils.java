package cz.cvut.kbss.amaplas.utils;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

public class RepositoryUtils {
    public static Repository createRepo(String endpoint, String username, String password){
        if(endpoint.startsWith("http")) {
            HTTPRepository httpRepo = new HTTPRepository(endpoint);
            if(username != null && password != null && !username.isEmpty() && !password.isEmpty());
                httpRepo.setUsernameAndPassword(username, password);
            return httpRepo;
        }

        return new SailRepository(new MemoryStore());
    }
}
