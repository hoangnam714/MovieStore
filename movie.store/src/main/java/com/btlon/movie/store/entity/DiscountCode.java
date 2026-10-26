package com.btlon.movie.store.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "discount_codes")
public class DiscountCode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String code;
    @Column(length = 255)
    private String description;
    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "usage_limit")
    private Integer usageLimit;
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;
    @Column(nullable = false)
    private Boolean active = true;

    public DiscountCode() {}

    public boolean isUsableOn(LocalDate day) {
        return Boolean.TRUE.equals(active)
                && (startDate == null || !day.isBefore(startDate))
                && (endDate == null || !day.isAfter(endDate))
                && (usageLimit == null || usedCount < usageLimit);
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    public Integer getUsedCount() { return usedCount; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
