package com.thesnellai.luckydna.arenas;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ArenaRegistry {

    private final Map<ArenaCode, ArenaAdapter> adapters;

    public ArenaRegistry(List<ArenaAdapter> arenaAdapters) {
        this.adapters = arenaAdapters.stream()
                .collect(Collectors.toMap(
                        ArenaAdapter::arenaCode,
                        Function.identity()
                ));
    }

    public ArenaAdapter getAdapter(ArenaCode arenaCode) {
        ArenaAdapter adapter = adapters.get(arenaCode);

        if (adapter == null) {
            throw new IllegalArgumentException("Unsupported Arena: " + arenaCode);
        }

        return adapter;
    }

    public List<ArenaSpecification<? extends ArenaRules>> availableArenas() {
        return adapters.values()
                .stream()
                .map(ArenaAdapter::specification)
                .toList();
    }
}