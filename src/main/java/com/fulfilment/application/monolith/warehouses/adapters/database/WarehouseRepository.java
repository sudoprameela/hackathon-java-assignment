package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;

    this.persist(dbWarehouse);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse dbWarehouse = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (dbWarehouse != null) {
      dbWarehouse.location = warehouse.location;
      dbWarehouse.capacity = warehouse.capacity;
      dbWarehouse.stock = warehouse.stock;
      dbWarehouse.archivedAt = warehouse.archivedAt;
      persist(dbWarehouse);
      getEntityManager().flush();
    }
  }

  @Override
  public void remove(Warehouse warehouse) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'remove'");
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }

  public java.util.List<Warehouse> search(String location, Integer minCapacity, Integer maxCapacity, String sortBy,
      String sortOrder, Integer page, Integer pageSize) {
    StringBuilder queryBuilder = new StringBuilder("archivedAt is null");
    java.util.Map<String, Object> params = new java.util.HashMap<>();

    if (location != null && !location.trim().isEmpty()) {
      queryBuilder.append(" and location = :location");
      params.put("location", location);
    }
    if (minCapacity != null) {
      queryBuilder.append(" and capacity >= :minCapacity");
      params.put("minCapacity", minCapacity);
    }
    if (maxCapacity != null) {
      queryBuilder.append(" and capacity <= :maxCapacity");
      params.put("maxCapacity", maxCapacity);
    }

    String sortField = (sortBy != null && sortBy.equals("capacity")) ? "capacity" : "createdAt";
    io.quarkus.panache.common.Sort.Direction direction = (sortOrder != null && sortOrder.equalsIgnoreCase("desc"))
        ? io.quarkus.panache.common.Sort.Direction.Descending
        : io.quarkus.panache.common.Sort.Direction.Ascending;

    int pageIndex = page != null ? page : 0;
    int size = pageSize != null ? pageSize : 10;
    if (size > 100)
      size = 100;

    return find(queryBuilder.toString(), io.quarkus.panache.common.Sort.by(sortField, direction), params)
        .page(io.quarkus.panache.common.Page.of(pageIndex, size))
        .list()
        .stream()
        .map(DbWarehouse::toWarehouse)
        .toList();
  }
}
