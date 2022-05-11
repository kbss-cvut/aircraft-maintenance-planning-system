package cz.cvut.kbss.amaplas.services;

import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class IdentifierService {

    public URI composeIdentifier(String prefix, String fragment) {
        return URI.create(prefix + "/" + fragment);
    }

}
