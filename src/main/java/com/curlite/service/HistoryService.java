package com.curlite.service;

import com.curlite.model.History;
import com.curlite.repository.HistoryRepository;
import java.util.List;

public class HistoryService {

    private final HistoryRepository historyRepository = new HistoryRepository();

    public void saveExecution(String method, String url, int status, long responseTime) {
        historyRepository.insert(method, url, status, responseTime);
    }

    public List<History> getRecent() {
        return historyRepository.findRecent();
    }
}
