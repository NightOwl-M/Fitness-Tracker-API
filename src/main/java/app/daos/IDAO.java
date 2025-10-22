package app.daos;

import java.util.List;

public interface IDAO<T, ID> {
    List<T> findAll();
    T find(ID id);
    T create(T entity);
    T update(ID id, T in);   // returnér null hvis ikke fundet
    boolean delete(ID id);   // false hvis ikke fundet
}
