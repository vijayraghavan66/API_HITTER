package com.curlite.service;

import com.curlite.model.Collection;
import com.curlite.model.SavedRequest;
import com.curlite.repository.CollectionRepository;
import com.curlite.repository.RequestRepository;
import java.util.List;
import java.util.Optional;

public class CollectionService {

    private final CollectionRepository collectionRepository = new CollectionRepository();
    private final RequestRepository requestRepository = new RequestRepository();

    public Collection createCollection(Collection incoming) {
        String name = incoming.getName() == null || incoming.getName().isBlank() ? "New Collection" : incoming.getName();
        long id = collectionRepository.create(name);

        List<SavedRequest> requests = incoming.getRequests();
        if (requests != null) {
            requestRepository.saveForCollection(id, requests);
        }

        return getCollection(id).orElseThrow(() -> new IllegalStateException("Collection creation failed"));
    }

    public List<Collection> getCollections() {
        List<Collection> collections = collectionRepository.findAll();
        collections.forEach(c -> c.setRequests(requestRepository.findByCollectionId(c.getId())));
        return collections;
    }

    public Optional<Collection> getCollection(long id) {
        Optional<Collection> collection = collectionRepository.findById(id);
        collection.ifPresent(c -> c.setRequests(requestRepository.findByCollectionId(id)));
        return collection;
    }

    public boolean deleteCollection(long id) {
        return collectionRepository.delete(id);
    }
}
