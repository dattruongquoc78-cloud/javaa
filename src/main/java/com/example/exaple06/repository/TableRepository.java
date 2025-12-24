package com.example.exaple06.repository;

import com.example.exaple06.entity.TableEntity;
import com.example.exaple06.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TableRepository extends JpaRepository<TableEntity, Long> {
    // ✅ THÊM: Tìm bàn theo trạng thái
    List<TableEntity> findByStatus(TableStatus status);
    
    // ✅ THÊM: Tìm bàn theo sức chứa
    List<TableEntity> findByCapacityGreaterThanEqual(Integer capacity);
    
    // ✅ THÊM: Kiểm tra số bàn đã tồn tại
    boolean existsByName(String name);
}