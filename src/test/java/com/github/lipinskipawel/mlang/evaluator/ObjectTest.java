package com.github.lipinskipawel.mlang.evaluator;

import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyString;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

final class ObjectTest implements WithAssertions {

    @Test
    void should_create_unique_hash_keys() {
        final var string1 = new MonkeyString("Hello world");
        final var string2 = new MonkeyString("Hello world");
        final var diff1 = new MonkeyString("random text");
        final var diff2 = new MonkeyString("random text");

        assertThat(string1.hashKey()).usingRecursiveComparison().isEqualTo(string2.hashKey());
        assertThat(diff1.hashKey()).usingRecursiveComparison().isEqualTo(diff2.hashKey());
        assertThat(string1.hashKey()).usingRecursiveComparison().isNotEqualTo(diff1.hashKey());
    }
}
