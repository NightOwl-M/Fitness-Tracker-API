// app.services.IService.java
package app.services;

import java.util.List;

public interface IService<DTO, ID> {
    List<DTO> findAll();
    DTO findById(ID id);
    DTO create(DTO dto);
    DTO update(ID id, DTO dto);
    void delete(ID id);
}
