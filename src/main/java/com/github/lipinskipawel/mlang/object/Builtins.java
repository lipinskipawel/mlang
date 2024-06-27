package com.github.lipinskipawel.mlang.object;

import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyArray;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyBuiltin;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyError;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyInteger;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyObject;
import com.github.lipinskipawel.mlang.evaluator.objects.MonkeyString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.lipinskipawel.mlang.evaluator.objects.ObjectType.ARRAY_OBJ;

public final class Builtins {
    private final List<Builtin> builtins = builtins();

    public Builtins() {
    }

    private List<Builtin> builtins() {
        return List.of(
                new Builtin("len", new MonkeyBuiltin() {
                    @Override
                    public MonkeyObject builtin(List<MonkeyObject> objects) {
                        if (objects.size() != 1) {
                            return newError("wrong number of arguments. got=%d, want=1", objects.size());
                        }
                        return switch (objects.get(0).type()) {
                            case STRING_OBJ -> new MonkeyInteger(((MonkeyString) objects.get(0)).value().length());
                            case ARRAY_OBJ -> {
                                final var array = (MonkeyArray) objects.get(0);
                                yield new MonkeyInteger(array.elements().size());
                            }
                            default -> newError("argument to [len] not supported, got %s", objects.get(0).type());
                        };
                    }
                }),
                new Builtin("puts", new MonkeyBuiltin() {
                    @Override
                    public MonkeyObject builtin(List<MonkeyObject> objects) {
                        objects.stream()
                                .map(MonkeyObject::inspect)
                                .forEach(System.out::println);
                        return null;
                    }
                }),
                new Builtin("first", new MonkeyBuiltin() {
                    @Override
                    public MonkeyObject builtin(List<MonkeyObject> objects) {
                        if (objects.size() != 1) {
                            return newError("wrong number of arguments. got=%d, want=1", objects.size());
                        }
                        if (objects.get(0).type() != ARRAY_OBJ) {
                            return newError("argument to 'first' must be ARRAY, got %s", objects.get(0).type());
                        }
                        final var array = (MonkeyArray) objects.get(0);
                        if (!array.elements().isEmpty()) {
                            return array.elements().get(0);
                        }
                        return null;
                    }
                }),
                new Builtin("last", new MonkeyBuiltin() {
                    @Override
                    public MonkeyObject builtin(List<MonkeyObject> objects) {
                        if (objects.size() != 1) {
                            return newError("wrong number of arguments. got=%d, want=1", objects.size());
                        }
                        if (objects.get(0).type() != ARRAY_OBJ) {
                            return newError("argument to 'last' must be ARRAY, got %s", objects.get(0).type());
                        }
                        final var array = (MonkeyArray) objects.get(0);
                        final var length = array.elements().size();
                        if (length > 0) {
                            return array.elements().get(length - 1);
                        }
                        return null;
                    }
                }),
                new Builtin("rest", new MonkeyBuiltin() {
                    @Override
                    public MonkeyObject builtin(List<MonkeyObject> objects) {
                        if (objects.size() != 1) {
                            return newError("wrong number of arguments. got=%d, want=1", objects.size());
                        }
                        if (objects.get(0).type() != ARRAY_OBJ) {
                            return newError("argument to 'rest' must be ARRAY, got %s", objects.get(0).type());
                        }
                        final var array = (MonkeyArray) objects.get(0);
                        final var length = array.elements().size();
                        if (length > 0) {
                            final var copy = new ArrayList<>(array.elements());
                            copy.remove(0);
                            return new MonkeyArray(copy);
                        }
                        return null;
                    }
                }),
                new Builtin("push", new MonkeyBuiltin() {
                    @Override
                    public MonkeyObject builtin(List<MonkeyObject> objects) {
                        if (objects.size() != 2) {
                            return newError("wrong number of arguments. got=%d, want=2", objects.size());
                        }
                        if (objects.get(0).type() != ARRAY_OBJ) {
                            return newError("argument to 'push' must be ARRAY, got %s", objects.get(0).type());
                        }
                        final var array = (MonkeyArray) objects.get(0);
                        final var copy = new ArrayList<>(array.elements());
                        copy.add(objects.get(1));
                        return new MonkeyArray(copy);
                    }
                })
        );
    }

    public Optional<MonkeyBuiltin> findBuiltIn(String builtInFunction) {
        return builtins.stream()
                .filter(it -> it.name().equals(builtInFunction))
                .map(Builtin::builtin)
                .findFirst();
    }

    private static MonkeyError newError(String message, Object... object) {
        return new MonkeyError(message.formatted(object));
    }
}
