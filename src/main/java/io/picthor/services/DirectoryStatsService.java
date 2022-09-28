package io.picthor.services;

import io.picthor.data.dao.DirectoryDao;
import io.picthor.data.entity.Directory;
import io.picthor.data.entity.DirectoryStats;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DirectoryStatsService {

    private final PublishSubject<Boolean> syncSubject = PublishSubject.create();
    private final DirectoryDao directoryDao;

    public DirectoryStatsService(DirectoryDao directoryDao) {
        syncSubject.sample(5, TimeUnit.SECONDS, true).subscribe(id -> processSync(), Throwable::printStackTrace);
        this.directoryDao = directoryDao;
    }

    public void syncStats() {
        syncSubject.onNext(true);
    }

    private void processSync() {
        StopWatch sw = new StopWatch();
        sw.start();
        directoryDao.updateFileNumStats();
        sw.split();
        log.debug("Updating file num stats took: {}", sw.formatSplitTime());

        directoryDao.updateDirNumStats();
        sw.split();
        log.debug("Updating dir num stats took: {}", sw.formatSplitTime());

        List<Directory> roots = directoryDao.findByType(Directory.Type.ROOT);
        for (Directory root : roots) {
            Map<Integer, List<Directory>> levels = new HashMap<>();
            Map<Long, DirectoryStats> statsMap = new HashMap<>();
            directoryDao.fetchTree(root);

            levels.put(0, List.of(root));
            root.getStats().resetChildStats();
            statsMap.put(root.getId(), root.getStats());
            levels(levels, 1, root);

            int maxLevel = levels.keySet().stream().max(Integer::compare).orElse(0);
            for (int i = maxLevel; i >= 0; i--) {
                for (Directory directory : levels.get(i)) {
                    Long parentId = directory.getParentId();
                    DirectoryStats dirStats = directory.getStats();
                    if (parentId != null) {
                        DirectoryStats parStats;
                        if (dirStats == null) {
                            directory.setStats(new DirectoryStats());
                            dirStats = directory.getStats();
                        }
                        if (!statsMap.containsKey(parentId) || statsMap.get(parentId) == null) {
                            parStats = directory.getParent().getStats();
                            if (parStats == null) parStats = new DirectoryStats();
                            parStats.resetChildStats();
                            statsMap.put(parentId, parStats);
                        }

                        parStats = statsMap.get(parentId);
                        parStats.setChildDirsNum(parStats.getChildDirsNum() + dirStats.getDirsNum() + dirStats.getChildDirsNum());
                        parStats.setChildFilesNum(parStats.getChildFilesNum() + dirStats.getFilesNum() + dirStats.getChildFilesNum());
                        parStats.setChildSizeBytes(parStats.getChildSizeBytes() + dirStats.getChildSizeBytes() + dirStats.getSizeBytes());
                    }
                }
            }
            directoryDao.persistStatsMaps(statsMap);
        }

        sw.split();
        log.debug("Updating directory tree stats took: {}", sw.formatSplitTime());
        sw.stop();
    }

    private void levels(Map<Integer, List<Directory>> levels, int level, Directory dir) {
        if (!levels.containsKey(level)) {
            levels.put(level, new ArrayList<>());
        }
        if (dir.getChildren() != null) {
            levels.get(level).addAll(dir.getChildren());
            for (Directory child : dir.getChildren()) {
                levels(levels, level + 1, child);
            }
        }
    }

}
