package kassuk.addon.blackout.utils;

@FunctionalInterface
public interface EpicInterface<T, E> {
    E get(T t);
}
