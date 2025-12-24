package com.example.exaple06.Service;

import com.example.exaple06.entity.TableEntity;
import com.example.exaple06.enums.TableStatus;
import com.example.exaple06.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TableService {
    
    private final TableRepository tableRepository;

    public List<TableEntity> getAllTables() {
        return tableRepository.findAll();
    }

    public Optional<TableEntity> getTableById(Long id) {
        return tableRepository.findById(id);
    }

    public TableEntity createTable(TableEntity table) {
        if (tableRepository.existsByName(table.getName())) {
            throw new RuntimeException("Tên bàn đã tồn tại: " + table.getName());
        }
        table.setStatus(TableStatus.FREE);
        return tableRepository.save(table);
    }

    public TableEntity updateTableStatus(Long id, TableStatus status) {
        return tableRepository.findById(id)
                .map(table -> {
                    table.setStatus(status);
                    return tableRepository.save(table);
                })
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại"));
    }

    public void deleteTable(Long id) {
        tableRepository.deleteById(id);
    }

    // ✅ THÊM: Tìm bàn theo trạng thái
    public List<TableEntity> getTablesByStatus(TableStatus status) {
        return tableRepository.findByStatus(status);
    }

    // ✅ THÊM: Tìm bàn theo sức chứa
    public List<TableEntity> getTablesByCapacity(Integer minCapacity) {
        return tableRepository.findByCapacityGreaterThanEqual(minCapacity);
    }
}