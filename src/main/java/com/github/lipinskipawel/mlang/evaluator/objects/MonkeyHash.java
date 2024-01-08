package com.github.lipinskipawel.mlang.evaluator.objects;

import java.util.HashMap;
import java.util.Map;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.HASH_OBJ;
import static java.util.stream.Collectors.joining;

public final class MonkeyHash extends MonkeyObject {
    public record HashPair(MonkeyObject key, MonkeyObject value) {
    }

    private final Map<HashKey, HashPair> pairs;

    public MonkeyHash() {
        this.pairs = new HashMap<>();
    }

    @Override
    public ObjectType type() {
        return HASH_OBJ;
    }

    @Override
    public String inspect() {
        return "{"
                + pairs.values()
                .stream()
                .map(hashPair -> hashPair.key.inspect() + ": " + hashPair.value.inspect())
                .collect(joining(", "))
                + "}";
    }

    public void put(MonkeyObject key, MonkeyObject value) {
        final var hash = ((Hashable) key).hashKey();
        pairs.put(hash, new HashPair(key, value));
    }

    public int size() {
        return pairs.size();
    }

    public HashPair getHashPair(HashKey key) {
        return pairs.get(key);
    }
}
